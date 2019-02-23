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

import helpers.Helper;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author karan This class will be used to Log all the Activities into the
 * database. It will help log the activities of the Super User, as well as the
 * all other types of users. Other types of logging required in the system will
 * be done using this class.
 *
 */
public class Logger {

    // Variables.
    private String status;
    private String statusTrace;
    private CloudDB cdb;
    private boolean isUserLog = false;

    // Static Variables.
    // Constructors.
    // Default Constructor.
    public Logger() {
	try {
	    // Here we will initiate the required.
	    // Connect to the Database.
	    this.cdb = new CloudDB();
	    isUserLog = false;

	} catch (Exception e) {
	    // There was an Error.
	    addStatus("Constructor : " + Helper.Error.getErrorMessage(e));
	}
    }

    // The actual constructor that we will be using.
    public Logger(CloudDB cdb) {
	try {
	    // Here we will initiate the required.
	    // Connect to the Database.
	    this.cdb = cdb;
	    isUserLog = true;

	} catch (Exception e) {
	    // There was an Error.
	    addStatus("Constructor : " + Helper.Error.getErrorMessage(e));
	}
    }

    // Helps for the Debugging Process.
    private void addStatus(String status) {
	try {
	    // Here we will add the status to the Status and Trace.
	    this.status = status;
	    this.statusTrace = this.statusTrace + " |::| " + status;
	} catch (Exception e) {
	    // There was an Error.
	}
    }

    // Lets log the Super User Queries in the database.
    public void superUserQuery(String query, String result) {
	try {
	    // Insert into the super users query logging table.
	    // Only allow the user to log this data if it is a user based log.
	    if (isUserLog) {
		// The User is allowed to log this data.
		// Lets insert the log into the database.
		String inQry = "INSERT INTO super_admins_queries "
			+ "(super_admin_id, query, result) VALUES (?, ?, ?)";
		// Create the prepared statement.
		PreparedStatement logPrepStmt = cdb.getCoreConn().prepareStatement(inQry);
		// Lets Bind the Values into the Prepared Statement.
		//logPrepStmt.setObject(1, cdb.getSuperUser().getUserID()); // TODO
		logPrepStmt.setString(2, query);
		logPrepStmt.setString(3, result);
		// Execute the Query.
		logPrepStmt.execute();
	    }
	} catch (Exception e) {
	    // There was an Error.
	}
    }

    // Lets Log the Normal User Queries
    public void userQuery(String query, String result) {
	try {
	    // Insert into the users query logging table.
	    // Only allow the user to log this data if it is a user based log.
	    if (isUserLog) {
		// The User is allowed to log this data.
		// Lets insert the log into the database.
		String inQry = "INSERT INTO user_queries "
			+ "(user_id, tenant_id, device_id, query, result) VALUES (?, ?, ?, ?, ?)";
		// Create the prepared statement.
		PreparedStatement logPrepStmt = cdb.getCoreConn().prepareStatement(inQry);
		// Lets Bind the Values into the Prepared Statement.
		//logPrepStmt.setObject(1, cdb.getUserObj().getUserID()); // TODO
		//logPrepStmt.setLong(2, cdb.getUserTenantID()); // TODO
		//logPrepStmt.setObject(3, cdb.getDeviceUID()); // TODO
		logPrepStmt.setString(4, query);
		logPrepStmt.setString(5, result);
		// Execute the Query.
		logPrepStmt.execute();
	    }
	} catch (Exception e) {
	    // There was an Error.
	}
    }

    // Lets Log the Server User Queries.
    public void serverUserQuery(String query, String result) {
	try {
	    // Insert into the users query logging table.
	    // Only allow the user to log this data if it is a user based log.
	    if (isUserLog) {
		// The User is allowed to log this data.
		// Lets insert the log into the database.
		String inQry = "INSERT INTO server_user_queries "
			+ "(server_user_id, app_server_id, query, result) VALUES (?, ?, ?, ?)";
		// Create the prepared statement.
		PreparedStatement logPrepStmt = cdb.getCoreConn().prepareStatement(inQry);
		// Lets Bind the Values into the Prepared Statement.
		//logPrepStmt.setObject(1, cdb.getUserObj().getUserID()); // TODO
		//logPrepStmt.setLong(2, cdb.getAppServerUID()); // TODO
		logPrepStmt.setString(3, query);
		logPrepStmt.setString(4, result);
		// Execute the Query.
		logPrepStmt.execute();
	    }
	} catch (Exception e) {
	    // There was an Error.
	}
    }

    // Lets Log the Project Activities in the Related Table.
    public void project(String logStack) {
	try {
	    // Insert into the users query logging table.
	    // Only allow the user to log this data if it is a user based log.
	    // The User is allowed to log this data.
	    // Lets insert the log into the database.
	    String inQry = "INSERT INTO project_logs "
		    + "(project_id, log_stack) VALUES (?, ?)";
	    // Create the prepared statement.
	    PreparedStatement logPrepStmt = cdb.getCoreConn().prepareStatement(inQry);
	    // Lets Bind the Values into the Prepared Statement.
	    //logPrepStmt.setObject(1, cdb.getProjectID()); // TODO
	    logPrepStmt.setString(2, logStack);
	    // Execute the Query.
	    logPrepStmt.execute();
	} catch (Exception e) {
	    // There was an Error.
	}
    }

    // Lets Log the APIs called.
    public void api(String userType, String api, HttpServletRequest paramReq, short versionNum) {
	try {
	    // Insert into the users query logging table.
	    // Only allow the user to log this data if it is a user based log.
	    // The User is allowed to log this data.
	    // Lets insert the log into the database.
	    String inQry = "INSERT INTO api_logs "
		    + "(user_id, user_type, api, params, version_no) VALUES (?, ?, ?, ?, ?)";
	    // Create the prepared statement.
	    PreparedStatement logPrepStmt = cdb.getCoreConn().prepareStatement(inQry);
	    // Lets Bind the Values into the Prepared Statement.
	    //logPrepStmt.setObject(1, cdb.getUserObj().getUserID()); // TODO
	    logPrepStmt.setString(2, userType);
	    logPrepStmt.setString(3, api);
	    // Create the params string.
	    StringBuilder paramBuilder = new StringBuilder();
	    Enumeration<String> it = paramReq.getParameterNames();
	    while (it.hasMoreElements()) {
		String next = it.nextElement();
		paramBuilder.append(next)
			.append(" :: ")
			.append(paramReq.getParameter(next));
	    }
	    logPrepStmt.setString(4, paramBuilder.toString());
	    logPrepStmt.setShort(5, versionNum);
	    // Execute the Query.
	    logPrepStmt.execute();
	} catch (Exception e) {
	    // There was an Error.
	}
    }
}
