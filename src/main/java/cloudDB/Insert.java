/*
 * 
 * 36TH ELEMENT LICENSE 1.0
 *
 * This is a project of 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * This project is a closed source and proprietary software package.
 * None of the contents of this software is to be used for uses not intended,
 * And no one is to interface with the software in methods not defined or previously decided by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * No changes should be done to this project without prior authorization by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 2017 (C) 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 
 * 
 */
package cloudDB;

import helpers.C;
import helpers.Helper;
import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 *
 * @author karan This class will be used to help insert data into the database.
 *
 */
public class Insert {

    // Variables.
    private CloudDB cdb;
    private String tableName;
    private StringBuilder columnDataBuilder;
    private StringBuilder columnPlaceHolderBuilder;
    private ArrayList<Object> bindObjs;
    private StringBuilder returnColBuilder;
    private String status;
    private long tenantID;
    private short rlsType;
    private long rlsID;
    private String syncID;
    private boolean isError = false;
    private StringBuilder statusTrace;
    private boolean isSystemTable = false;
    private String updateTime = "";

    // Constructors.
    // Default Constructor.
    public Insert() {
	// Default Constructor.
	rlsType = 0;
	tenantID = 0;
	rlsID = 0;
    }

    // Constructor to allow to add the context.
    public Insert(CloudDB cdb) {
	try {
	    // Lets set the required dsta.
	    if (cdb.isAuthenticated()) {
		// The user is authenticated.
		this.cdb = cdb;
		rlsType = 0;
		tenantID = 0;
		rlsID = 0;
		isError = false;
		addStatus("User is Authenticated, Insert is Ready.");
	    } else {
		// The user is unauthenticated.
		isError = false;
		addStatus("User not Authenticated");
	    }
	} catch (Exception e) {
	    // There was an Error.
	    isError = false;
	    addStatus("Cloud DB is Null");
	}
    }

    // Add the Context to the Insert.
    public Insert with(CloudDB cdb) {
	try {
	    if (cdb.isAuthenticated()) {
		// The user is authenticated.
		this.cdb = cdb;
		return this;
	    } else {
		// The user is not authenticated.
		addStatus("User not Authenticated");
		return null;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    addStatus("Cloud DB is Null");
	    return null;
	}
    }

    // Main Constructor will be used to set the table name.
    public Insert into(String tableName) {
	try {
	    // Here we will set the table name.
	    // Authentication.
	    if (this.tableName == null) {
		if (cdb.validateCRUD(tableName, CloudDB.CRUD.WRITE)) {
		    this.tableName = tableName.replaceAll(" ", "");
		    // The user is allowed to write into the table.
		    // Initiate the required variables.
		    columnDataBuilder = new StringBuilder();
		    columnPlaceHolderBuilder = new StringBuilder();
		    bindObjs = new ArrayList<>();
		    returnColBuilder = new StringBuilder();
		    addStatus("Valid CRUD");
		    return this;
		} else {
		    // The user is not authorized to write into this table.
		    addStatus("Not Authorized to Write into Table");
		    this.tableName = null;
		    return this;
		}
	    } else {
		// The Table name has already been set.
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    this.tableName = null;
	    addStatus("Error with the Cloud DB Validation");
	    return this;
	}
    }

    // This will be used to set the system table related to Cloud Core.
    public Insert intoSystem(String tableName) {
	try {
	    // Here we will set the table name.
	    // Authentication.
	    if (this.tableName == null) {
		if (cdb.isServerUser()) {
		    this.tableName = tableName.replaceAll(" ", "");
		    // The user is allowed to write into the table.
		    // Initiate the required variables.
		    columnDataBuilder = new StringBuilder();
		    columnPlaceHolderBuilder = new StringBuilder();
		    bindObjs = new ArrayList<>();
		    returnColBuilder = new StringBuilder();
		    addStatus("Valid User");
		    isSystemTable = true;
		    return this;
		} else {
		    // The user is not authorized to write into this table.
		    addStatus("Not Authorized to Write into Table");
		    this.tableName = null;
		    return this;
		}
	    } else {
		// The Table name has already been set.
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    this.tableName = null;
	    addStatus("Error with the Cloud DB Validation");
	    return this;
	}
    }

    // Add the Column Data.
    public Insert putColumn(ColumnData columnData) {
	try {
	    // Here we will add the column data into the Column SQL & Binders.
	    // Only let to proceed if the Column does'nt have the special rows.
	    if (columnData.getColumn().charAt(columnData.getColumn().length() - 1) == '_') {
		// This column is not a valid column and must be left only for the system to edit.
		addStatus("User cannot add " + columnData.getColumn() + " System Column");
	    } else {
		// This Columns is valid.
		if (columnDataBuilder.length() > 0) {
		    columnDataBuilder.append(", ");
		    columnPlaceHolderBuilder.append(", ");
		}
		columnDataBuilder.append(columnData.getColumn());
		columnPlaceHolderBuilder.append("?");
		// Put the Bind Object.
		bindObjs.add(columnData.get());
	    }
	    return this;
	} catch (Exception e) {
	    // There was an Error.
	    addStatus("Put Column Error : " + Helper.Error.getErrorMessage(e));
	    return this;
	}
    }

    private Insert putColumnInternal(ColumnData columnData) {
	try {
	    // Lets add the Columns for internal use.
	    if (columnDataBuilder.length() > 0) {
		columnDataBuilder.append(", ");
		columnPlaceHolderBuilder.append(", ");
	    }
	    columnDataBuilder.append(columnData.getColumn());
	    columnPlaceHolderBuilder.append("?");
	    // Put the Bind Object.
	    bindObjs.add(columnData.get());
	    return this;
	} catch (Exception e) {
	    // THere was an Error.
	    addStatus("Put Column Error : " + Helper.Error.getErrorMessage(e));
	    return this;
	}
    }

    // Set the Tenant ID of the Row to be inserted.
    // Has to be the same tenant of the user.
    public Insert setTenantID(long tenantID) {
	try {
	    // Here we will add the Role ID given by the User to the Statement.
	    // Check if the Table is syncable, then only add the row level security.
	    if (cdb.isMultiTenant(tableName)) {
		// Check if the user is a normal user.
		if (cdb.isNormalUser()) {
		    // Lets validate if the User is allowed to write with this role.
		    if (cdb.getUser().validateCRUD(tableName, CloudDB.CRUD.WRITE)) {
			// The user is allowed to write with this role.
			this.tenantID = cdb.getUser().getTenantID();
			return this;
		    } else {
			// The user is not allowed to write in this tenant id.
			this.tenantID = 0;
			return null;
		    }
		} else if (cdb.isServerUser()) {
		    // The user is a server user, and can directly add the rls id.
		    // The user is allowed to write in this tenant.
		    this.tenantID = tenantID;
		    return this;
		} else {
		    // Not a recognized user.
		    this.tenantID = 0;
		    return null;
		}
	    } else {
		// The table is not syncable, so we will not add any RLS.
		this.tenantID = 0;
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    this.tenantID = 0;
	    return null;
	}
    }

    // Set the Role ID of the Row to be inserted.
    // Has to be a role of the user.
    public Insert setRoleID(long roleID) {
	try {
	    // Here we will add the Role ID given by the User to the Statement.
	    // Check if the Table is syncable, then only add the row level security.
	    if (cdb.isSyncable(tableName)) {
		// Check if the user is a normal user.
		if (cdb.isNormalUser()) {
		    // Lets validate if the User is allowed to write with this role.
		    if (cdb.getUser().validateRowRLS(roleID, CloudDB.CRUD.WRITE)) {
			// The user is allowed to write with this role.
			rlsType = C.RLS.ROW_RLS;
			rlsID = roleID;
			//addStatus("User is Authorized to Write in this Role");
			return this;
		    } else {
			// The user is not allowed to write with this role.
			rlsType = C.RLS.NO_RLS;
			rlsID = 0;
			//addStatus("User is NOT Authorized to Write in this Role");
			isError = true;
			return this;
		    }
		} else if (cdb.isServerUser()) {
		    // The user is a server user, and can directly add the rls id.
		    // The user is allowed to write with this role.
		    rlsType = C.RLS.ROW_RLS;
		    rlsID = roleID;
		    //addStatus("User is a Server User");
		    return this;
		} else {
		    // Not a recognized user.
		    rlsType = C.RLS.NO_RLS;
		    rlsID = 0;
		    //addStatus("User is NOT Authorized to Write in this Role");
		    isError = true;
		    return this;
		}
	    } else {
		// The table is not syncable, so we will not add any RLS.
		rlsType = C.RLS.NO_RLS;
		rlsID = 0;
		//addStatus("Table is not a Syncable Table.");
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    rlsType = C.RLS.NO_RLS;
	    rlsID = 0;
	    //addStatus("Error : " + Helper.Error.getErrorMessage(e));
	    isError = true;
	    return this;
	}
    }

    // Get the RLS Data.
    public short getRLSType() {
	return rlsType;
    }

    public long getRLSID() {
	return rlsID;
    }

    // Set the Group ID of the Row to be inserted.
    // Has to be a Group of the user.
    public Insert setGroupID(long groupID) {
	try {
	    // Here we will add the Group ID given by the User to the Statement.
	    // Check if the Table is syncable, then only add the row level security.
	    if (cdb.isSyncable(tableName)) {
		// Check if the user is a normal user.
		if (cdb.isNormalUser()) {
		    // Lets validate if the User is allowed to write with this role.
		    if (cdb.getUser().validateGroupRLS(groupID, CloudDB.CRUD.WRITE)) {
			// The user is allowed to write with this role.
			rlsType = C.RLS.GROUP_RLS;
			rlsID = groupID;
			//addStatus("User is Authorized to Write in this Group");
			return this;
		    } else {
			// The user is not allowed to write with this role.
			rlsType = C.RLS.NO_RLS;
			rlsID = 0;
			isError = true;
			//addStatus("User is NOT Authorized to Write in this Group");
			isError = true;
			return this;
		    }
		} else if (cdb.isServerUser()) {
		    // The user is a server user, and can directly add the rls id.
		    // The user is allowed to write with this role.
		    rlsType = C.RLS.GROUP_RLS;
		    rlsID = groupID;
		    //addStatus("User is a Server User");
		    return this;
		} else {
		    // Not a recognized user.
		    rlsType = C.RLS.NO_RLS;
		    rlsID = 0;
		    //addStatus("User is NOT Authorized to Write in this Group");
		    isError = true;
		    return this;
		}
	    } else {
		// The table is not syncable, so we will not add any RLS.
		rlsType = C.RLS.NO_RLS;
		rlsID = 0;
		//addStatus("Table is not a Syncable Table.");
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    rlsType = C.RLS.NO_RLS;
	    rlsID = 0;
	    addStatus("Error : " + Helper.Error.getErrorMessage(e));
	    isError = true;
	    return this;
	}
    }

    // Set the Sync ID.
    public Insert setSyncID(String syncID) {
	try {
	    // Add the Sync ID if there is.
	    if (syncID.trim().length() == 36) {
		// There is Sync ID.
		putColumnInternal(new ColumnData("sync_id_", syncID));
		//addStatus("Added the Sync ID : " + syncID);
	    }
	    return this;
	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

    // Lets setup the update time getter and setter.
    // Set up the Setter of the Update time.
    public Insert setUpdateTime(String updateTime) {
	this.updateTime = updateTime;
	return this;
    }

    // Setup the Getter of the Update time.
    private String getUpdateTime() {
	return updateTime;
    }

    // Add the Return Column Name.
    public Insert addReturningColumn(String columnName) {
	try {
	    // Here we add the returning column that has to be added.
	    if (returnColBuilder.length() > 0) {
		// There is another return column added
		returnColBuilder.append(',');
	    }
	    returnColBuilder.append(" ")
		    .append(columnName.replaceAll(" ", ""));
	    return this;

	} catch (Exception e) {
	    // There was an Error.
	    return this;
	}
    }

    // Here we generate the SQL Statement for the User.
    public String getSQL() {
	try {
	    // Here we generate the SQL Statement.
	    // Attach the Main Select statement.
	    StringBuilder sqlBuilder = new StringBuilder();
	    // Check if a normal user.
	    if (cdb.isNormalUser()) {
		// The user is a normal user.
		// Check if the table is a multi-tenant table.
		if (cdb.isMultiTenant(tableName)) {
		    // The Table is multi-tenant.
		    tenantID = cdb.getUser().getTenantID();
		}
	    }

	    // Check if the table is a syncable table.
	    if (cdb.isSyncable(tableName)) {
		// The table is a syncable table.
		// Lets add the RLS system.
		if (rlsID == 0 || rlsType == 0) {
		    // The table is a syncable table, but the RLS Values are not provided.
		    // So we will not let it complete the process.
		    isError = true;
		    //addStatus("User NOT Authenticated to Write in this RLS");
		} else {
		    // The user has provided the RLS Data.
		    putColumnInternal(new ColumnData("rls_id_", rlsID));
		    putColumnInternal(new ColumnData("rls_type_", rlsType));
		    // Set up the Update time.
		    try {
			if (!updateTime.trim().isEmpty()) {
			    // The update time has been added.
			    putColumnInternal(new ColumnData("update_time_", updateTime));
			}
		    } catch (Exception e) {
			// There was an Error.
		    }
		}
	    }

	    // Add the Multi-tenancy if the table is Multi-tenant and not a system table.
	    if (cdb.isMultiTenant(tableName)) {
		// The table is Multi-tenant.
		putColumnInternal(new ColumnData("tenant_id_", tenantID));
		//addStatus("Added the Tenant ID : " + tenantID);
	    }

	    // Validate if the User can procceed.
	    if (isSuccessful()) {
		// There are no errors in the Insert.
		// Generate the Insert Query.
		sqlBuilder.append("INSERT INTO ")
			// Add the Table Name.
			.append(tableName)
			.append(" (")
			// Add the Column Names to the Insert Statement.
			.append(columnDataBuilder)
			.append(")")
			.append(" VALUES (")
			// Add the Place holders for the Prepared Statements.
			.append(columnPlaceHolderBuilder)
			.append(")");
		//  Add the Return Columns if there exist.
		if (returnColBuilder.length() > 0) {
		    sqlBuilder.append(" RETURNING ")
			    .append(returnColBuilder);
		}
	    } else {
		// The user is not allowed to write.
		sqlBuilder.append("Error in Insert");
	    }
	    return sqlBuilder.toString();

	} catch (Exception e) {
	    // There was an Error.
	    return Helper.Error.getErrorMessage(e);
	}
    }

    // Here we generate the SQL Prepared Statement for the User.
    public PreparedStatement getPreparedStatement() {
	try {
	    // Here we will generate the SQL Statement with the placeholders.
	    // And we bind the Queries.
	    String sqlQuery = getSQL();
	    // Prepare the Statement.
	    PreparedStatement prepStmt = cdb.getConn().prepareStatement(sqlQuery);
	    for (int bindCount = 1; bindCount <= bindObjs.size(); bindCount++) {
		// Bind the Values to the Database.
		prepStmt.setObject(bindCount, bindObjs.get(bindCount - 1));
	    }
	    return prepStmt;

	} catch (Exception e) {
	    // There was an Error.
	    //addStatus("Err : " + Helper.Error.getErrorMessage(e);
	    return null;
	}
    }

    // Here we generate the SQL Prepared Statement for the User, that will connect only to the core database.
    public PreparedStatement getPrepStmtCore() {
	try {
	    // Here we will generate the SQL Statement with the placeholders.
	    // And we bind the Queries.
	    String sqlQuery = getSQL();
	    // Prepare the Statement.
	    PreparedStatement prepStmt = cdb.getCoreConn().prepareStatement(sqlQuery);
	    for (int bindCount = 1; bindCount <= bindObjs.size(); bindCount++) {
		// Bind the Values to the Database.
		prepStmt.setObject(bindCount, bindObjs.get(bindCount - 1));
	    }
	    return prepStmt;

	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

    public String getStatus() {
	return status;
    }

    // Get if the System Errored out.
    public boolean isSuccessful() {
	return !isError;
    }

    // Here we set the status and the status trace.
    private void addStatus(String status) {
	try {
	    // Here we append the status to the status trace and status.
	    if (statusTrace == null) {
		statusTrace = new StringBuilder();
	    }
	    this.status = status;
	    statusTrace.append(status)
		    .append('\n');
	} catch (Exception e) {
	    // There was an Error.
	}
    }

    // This will retrieve the whole status trace.
    public String getStatusTrace() {
	return statusTrace.toString();
    }

}
