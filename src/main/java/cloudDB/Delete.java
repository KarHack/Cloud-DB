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
 * @author karan We will use this class to create delete statements with the
 * required security.
 *
 */
public class Delete {

    // Variables.
    private CloudDB cdb;
    private String tableName;
    private StringBuilder whereClauseBuilder;
    private ArrayList<Object> whereBindObjs;
    private StringBuilder returnColBuilder;
    private long tenantID;
    private short rlsType;
    private long rlsID;
    private boolean isError = false;
    private String status;
    private StringBuilder statusTrace;
    private boolean isSystemTable = false;

    // Constructor.
    // Default Constructor.
    public Delete() {
	// Default Constructor. We don't Need this constructor.
    }

    // Constructor to allow the addition of the Cloud DB.
    public Delete(CloudDB cdb) {
	try {
	    // Authorize the User.
	    if (cdb.isAuthenticated()) {
		// The User is authorized
		this.cdb = cdb;
		addStatus("User Authenticated");
	    } else {
		// The user is not authorized.
		isError = true;
		addStatus("User not Authenticated");
	    }
	} catch (Exception e) {
	    // There was an Error.
	    isError = true;
	    addStatus("Error : " + Helper.Error.getErrorMessage(e));
	}
    }

    // Add the Cloud DB instance.
    public Delete with(CloudDB cdb) {
	try {
	    // Authorize the User.
	    if (cdb.isAuthenticated()) {
		// The User is authorized
		this.cdb = cdb;
		addStatus("User Authenticated");
		return this;
	    } else {
		// The user is not authorized.
		isError = true;
		addStatus("User not Authenticated");
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    isError = true;
	    addStatus("Error : " + Helper.Error.getErrorMessage(e));
	    return this;
	}
    }

    // This Constructor we will use to set the table name.
    public Delete from(String tableName) {
	try {
	    // Set the Table Name.
	    // Authentication.
	    if (cdb.validateCRUD(tableName, CloudDB.CRUD.DELETE)) {
		this.tableName = tableName.replaceAll(" ", "");
		// Initiate the Required Variables.
		whereClauseBuilder = new StringBuilder();
		whereBindObjs = new ArrayList<>();
		returnColBuilder = new StringBuilder();
		addStatus("User Authorized to Delete from the Table");
		return this;
	    } else {
		// The user is not authorized to delete from this table.
		isError = true;
		addStatus("User NOT Authorized to Delete from the Table");
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    isError = true;
	    addStatus("Error : " + Helper.Error.getErrorMessage(e));
	    return this;
	}
    }

    // This will be used to Delete from the System Table.
    public Delete fromSystem(String tableName) {
	try {
	    // Set the Table Name.
	    // Authentication.
	    if (cdb.isServerUser()) {
		this.tableName = tableName.replaceAll(" ", "");
		// Initiate the Required Variables.
		whereClauseBuilder = new StringBuilder();
		whereBindObjs = new ArrayList<>();
		returnColBuilder = new StringBuilder();
		addStatus("User Authorized to Delete from the Table");
		isSystemTable = true;
		return this;
	    } else {
		// The user is not authorized to delete from this table.
		isError = true;
		addStatus("User NOT Authorized to Delete from the Table");
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    isError = true;
	    addStatus("Error : " + Helper.Error.getErrorMessage(e));
	    return this;
	}
    }

    // Set the Where Clauses here.
    public Delete addWhere(Where clause) {
	try {
	    // Add the where to the system, with the default connector.
	    if (whereClauseBuilder.length() > 0) {
		whereClauseBuilder.append(clause.getConnector());
	    }
	    whereClauseBuilder.append(clause.getColumnName())
		    .append(clause.operation())
		    .append('?');
	    whereBindObjs.add(clause.getColumnData());

	    return this;

	} catch (Exception e) {
	    // There was an Error.
	    return this;
	}
    }

    // Add the Return Column Name.
    public Delete addReturningColumn(String columnName) {
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

    // Set the Role ID of the Row to be inserted.
    // Has to be a role of the user.
    public Delete setRoleID(long roleID) {
	try {
	    // Here we will add the Role ID given by the User to the Statement.
	    // Check if the Table is syncable, then only add the row level security.
	    if (cdb.isSyncable(tableName)) {
		// Check if the user is a normal user.
		if (cdb.isNormalUser()) {
		    // Lets validate if the User is allowed to write with this role.
		    if (cdb.getUser().validateRowRLS(roleID, CloudDB.CRUD.DELETE)) {
			// The user is allowed to write with this role.
			rlsType = C.RLS.ROW_RLS;
			rlsID = roleID;
			return this;
		    } else {
			// The user is not allowed to write with this role.
			rlsType = C.RLS.NO_RLS;
			rlsID = 0;
			isError = true;
			addStatus("User not Authorized to Delete from this Role");
			return this;
		    }
		} else if (cdb.isServerUser()) {
		    // The user is a server user, and can directly add the rls id.
		    // The user is allowed to write with this role.
		    rlsType = C.RLS.ROW_RLS;
		    rlsID = roleID;
		    return this;
		} else {
		    // Not a recognized user.
		    rlsType = C.RLS.NO_RLS;
		    rlsID = 0;
		    isError = true;
		    addStatus("User not Authorized to Delete from this Role");
		    return this;
		}
	    } else {
		// The table is not syncable, so we will not add any RLS.
		rlsType = C.RLS.NO_RLS;
		rlsID = 0;
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    rlsType = C.RLS.NO_RLS;
	    rlsID = 0;
	    isError = true;
	    addStatus("Error : " + Helper.Error.getErrorMessage(e));
	    return this;
	}
    }

    // Set the Group ID of the Row to be inserted.
    // Has to be a Group of the user.
    public Delete setGroupID(long groupID) {
	try {
	    // Here we will add the Group ID given by the User to the Statement.
	    // Check if the Table is syncable, then only add the row level security.
	    if (cdb.isSyncable(tableName)) {
		// Check if the user is a normal user.
		if (cdb.isNormalUser()) {
		    // Lets validate if the User is allowed to Delete with this role.
		    if (cdb.getUser().validateGroupRLS(groupID, CloudDB.CRUD.DELETE)) {
			// The user is allowed to write with this role.
			rlsType = C.RLS.GROUP_RLS;
			rlsID = groupID;
			return this;
		    } else {
			// The user is not allowed to write with this Group.
			rlsType = C.RLS.NO_RLS;
			rlsID = 0;
			isError = true;
			addStatus("User not Authorized to Delete from this Group");
			return this;
		    }
		} else if (cdb.isServerUser()) {
		    // The user is a server user, and can directly add the rls id.
		    // The user is allowed to write with this role.
		    rlsType = C.RLS.GROUP_RLS;
		    rlsID = groupID;
		    return this;
		} else {
		    // Not a recognized user.
		    rlsType = C.RLS.NO_RLS;
		    rlsID = 0;
		    isError = true;
		    addStatus("User not Authorized to Delete from this Group");
		    return this;
		}
	    } else {
		// The table is not syncable, so we will not add any RLS.
		rlsType = C.RLS.NO_RLS;
		rlsID = 0;
		return this;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    rlsType = C.RLS.NO_RLS;
	    rlsID = 0;
	    isError = true;
	    addStatus("Error : " + Helper.Error.getErrorMessage(e));
	    return this;
	}
    }

    // Allow the User to Get the SQL Statement Generated.
    public String getSQL() {
	try {
	    try {
		// Here we will Generate the SQL Statement.
		// Here we will add the Tenant ID.
		if (cdb.isNormalUser()) {
		    tenantID = cdb.getUser().getTenantID();
		}
		// Add the Multi-tenancy if required.
		if (cdb.isMultiTenant(tableName) && tenantID != 0 && !isSystemTable) {
		    // Add tbe Tenant to the Where.
		    addWhere(new Where("tenant_id_", Where.Type.EQUAL, tenantID));
		}
                
		// Add the RLS System.
		if (cdb.isSyncable(tableName)) {
		    // The table required RLS.
		    if (rlsID == 0 && cdb.isNormalUser()) {
			// The user should not be allowed to execute this statement.
			// Lets add the user's RLS Where system.
			// Check if the Multi-tenant security was set.
			if (whereClauseBuilder.length() > 0) {
			    // The Multi-tenant, add the connector param.
			    whereClauseBuilder
				    .append(" AND ").append('(')
				    .append(new Where()
					    .setupRLSClause(cdb.getUser(), tableName, C.QueryType.DELETE))
                                                .append(')');
			} else {
			    // It is not Multi-tenant.
			    whereClauseBuilder.append('(')
				    .append(new Where()
					    .setupRLSClause(cdb.getUser(), tableName, C.QueryType.DELETE))
                                                .append(')');
			}
		    } else if (cdb.isNormalUser()) {
			// The RLS is provided.
			// Add the Where clause.
			addWhere(new Where("rls_id_", Where.Type.EQUAL, rlsID));
			addWhere(new Where("rls_type_", Where.Type.EQUAL, rlsType));
		    }
		}
	    } catch (Exception e) {
		// There was an Error.
		isError = true;
		addStatus("SQL Security : " + Helper.Error.getErrorMessage(e));
	    }
            
	    // Validate if the User can proceed with the delete.
	    if (isSuccessful()) {
		// Attach the Main Statement.
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("DELETE FROM ")
			.append(tableName)
			.append(" WHERE ")
			.append(whereClauseBuilder);

		//  Add the Return Columns if there exist.
		if (returnColBuilder.length() > 0) {
		    sqlBuilder.append(" RETURNING ")
			    .append(returnColBuilder);
		}
		return sqlBuilder.toString();
	    } else {
		return "Delete Error : " + status;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    return "Error : " + Helper.Error.getErrorMessage(e);
	}
    }

    // Generate the Prepared statement for easier usability.
    public PreparedStatement getPreparedStatement() {
	try {
	    // Here we prepare the statement using the Connection Provided.
	    String sqlQuery = getSQL();
	    // Prepare the Statement.
	    PreparedStatement prepStmt = cdb.getConn().prepareStatement(sqlQuery);
	    // Bind the Where Clause Data.
	    for (int bindCount = 1; bindCount <= whereBindObjs.size(); bindCount++) {
		// Bind the Values to the Database.
		prepStmt.setObject(bindCount, whereBindObjs.get(bindCount - 1));
	    }
	    return prepStmt;

	} catch (Exception e) {
	    // There waa an Error.
	    addStatus("Error : " + Helper.Error.getErrorMessage(e));
	    return null;
	}
    }

    // Generate the Prepared statement for easier usability.
    public PreparedStatement getPrepStmtCore() {
	try {
	    // Here we prepare the statement using the Connection Provided.
	    String sqlQuery = getSQL();
	    // Prepare the Statement.
	    PreparedStatement prepStmt = cdb.getCoreConn().prepareStatement(sqlQuery);
	    // Bind the Where Clause Data.
	    for (int bindCount = 1; bindCount <= whereBindObjs.size(); bindCount++) {
		// Bind the Values to the Database.
		prepStmt.setObject(bindCount, whereBindObjs.get(bindCount - 1));
	    }
	    return prepStmt;

	} catch (Exception e) {
	    // There waa an Error.
	    return null;
	}
    }

    // Get the Status.
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
