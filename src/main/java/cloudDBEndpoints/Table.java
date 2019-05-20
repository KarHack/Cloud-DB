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
package cloudDBEndpoints;

import cloudDB.CloudDB;
import cloudDB.Select;
import cloudDB.Sync;
import cloudDB.Update;
import cloudDB.Where;
import helpers.C;
import helpers.Helper;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author karan
 */
public class Table extends HttpServlet {
    // Variables of the class.

    // Static variables of the class.
    // Can be used to cache some values.
    // This API is the main Endpoint for Connections for the Client Library.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	response.setContentType("text/html;charset=UTF-8");
	PrintWriter out = response.getWriter();
	try {
	    // The user is trying to Get some data.
	    // Version Control.
	    float version = Float.parseFloat(request.getParameter(C.Params.VERSION_NO));
	    if (version == 0.1f) {
		out.print(getV0_1(request));
	    } else {
		out.println("Unknown Version");
	    }
	    out.close();
	} catch (Exception e) {
	    // There was an Error.
	    out.println("Unknown Version");
	    out.close();
	}
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	response.setContentType("text/html;charset=UTF-8");
	PrintWriter out = response.getWriter();
	try {
	    // The user is trying to Insert some data.
	    // Version Control.
	    float version = Float.parseFloat(request.getParameter(C.Params.VERSION_NO));
	    if (version == 0.1f) {
		out.print(postV0_1(request));
	    } else {
		out.println("Unknown Version");
	    }
	    out.close();
	} catch (Exception e) {
	    // There was an Error.
	    out.println("Unknown Version");
	    out.close();
	}
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	response.setContentType("text/html;charset=UTF-8");
	PrintWriter out = response.getWriter();
	try {
	    // The user is trying to Insert some data.
	    // Version Control.
	    float version = Float.parseFloat(request.getParameter(C.Params.VERSION_NO));
	    if (version == 0.1f) {
		out.print(putV0_1(request));
	    } else {
		out.println("Unknown Version");
	    }
	    out.close();
	} catch (Exception e) {
	    // There was an Error.
	    out.println(Helper.Error.getErrorMessage(e));
	    out.close();
	}
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	response.setContentType("text/html;charset=UTF-8");
	PrintWriter out = response.getWriter();
	try {
	    // The user is trying to Insert some data.
	    // Version Control.
	    float version = Float.parseFloat(request.getParameter(C.Params.VERSION_NO));
	    if (version == 0.1f) {
		out.print(deleteV0_1(request));
	    } else {
		out.println("Unknown Version");
	    }
	    out.close();
	} catch (Exception e) {
	    // There was an Error.
	    out.println(Helper.Error.getErrorMessage(e));
	    out.close();
	}
    }

    /**
     * Version Controlling of the API. Functions of all Versions of the Above
     * Request Types.
     *
     */
    // Get Version 0.1
    private JSONObject getV0_1(HttpServletRequest request) {
	// Initialise the Required Variables.
	JSONObject respObj = new JSONObject();
	try {
	    // Get the Required Params
	    String token = request.getParameter(C.Params.TOKEN);
	    // Set up the user and cloud db object for interfacing with the database.
	    CloudDB cdb = new CloudDB(token);
	    // Only a Server User Can Use This API.
	    if (cdb.isServerUser() && cdb.isAuthenticated()) {
		// Lets Build the Statement to allow to get the Accounts or Account requested.
		Select select = new Select()
			.with(cdb)
			.addColumn(C.Tables.Table.ID, "table_id")
			.addColumn(C.Tables.Table.NAME)
			.addColumn(C.Tables.Table.DATABASE_ID)
			.addColumn(C.Tables.Table.SYNCABLE)
                        .addColumn(C.Tables.Table.MULTI_TENANT)
			.addColumn(C.Tables.Table.TIME_STAMP)
			.from(C.Tables.TABLES)
			.addWhere(new Where("database_id", Where.Type.EQUAL, cdb.getDatabaseID()));
		// Check if to get just one dbount.
		if (request.getParameterMap().containsKey("table_id")) {
		    select.addWhere(new Where(C.Tables.Table.ID,
			    Where.Type.EQUAL, request.getParameter("table_id")));
		}
		// Execute the query.
		try {
		    ResultSet tbData = select.getPrepStmtCore().executeQuery();
		    // Extract the Data.
		    if (tbData.isBeforeFirst()) {
			// Found some data. Lets extract it and send it to the user.
			JSONArray tbJArr = new JSONArray();
			while (tbData.next()) {
			    JSONObject tbJObj = new JSONObject();
			    tbJObj.put("id", tbData.getString("table_id"));
			    tbJObj.put(C.Tables.Table.NAME, tbData.getString(C.Tables.Table.NAME));
			    tbJObj.put(C.Tables.Table.DATABASE_ID, tbData.getString(C.Tables.Table.DATABASE_ID));
			    tbJObj.put(C.Tables.Table.SYNCABLE, tbData.getBoolean(C.Tables.Table.SYNCABLE));
			    tbJObj.put("timestamp", tbData.getString(C.Tables.Table.TIME_STAMP));
			    // Add the Json Object to the main object.
			    tbJArr.add(tbJObj);
			}
			// Add the Array to the Main Response Object.
			respObj.put("success", tbJArr);
			cdb.close();
			return respObj;
		    } else {
			// There is no data in the table dbording to the query.
			respObj.put("error", "No Data Found");
			cdb.close();
			return respObj;
		    }
		} catch (Exception e) {
		    respObj.put("error", "Extract : " + Helper.Error.getErrorMessage(e));
		    cdb.close();
		    return respObj;
		}
	    } else {
		// It is not a server users.
		respObj.put("error", "Not Priviledged User");
		cdb.close();
		return respObj;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    return respObj;
	}
    }

    // This won't be able to create a table, as only the root can create a table.
    // This will only handle the mapping of a role to a table and other cloud core entries.
    // Post Version 0.1
    private JSONObject postV0_1(HttpServletRequest request) {
	// Initialise the Required Variables.
	JSONObject respObj = new JSONObject();
	try {
	    // Get the Required Params
	    String token = request.getParameter(C.Params.TOKEN);
	    // Set up the user and cloud db object for interfacing with the database.
	    CloudDB cdb = new CloudDB(token);
	    // Only a Server User Can Use This API.
	    if (cdb.isServerUser() && cdb.isAuthenticated()) {
		// Create table in the system.
		// String tbName = cdb.createTable();
		// Lets Build the Statement to allow to get the Accounts or Account requested.
		// Insert the Company Data.
		/*Insert insert = new Insert()
			.with(cdb)
			.into(C.Tables.TABLES)
			.putColumn(new ColumnData(C.Tables.Table.ID, request.getParameter("table_id")))
			.putColumn(new ColumnData(C.Tables.Table.NAME, request.getParameter(C.Tables.Table.NAME)))
			.putColumn(new ColumnData(C.Tables.Table.DATABASE_ID, request.getParameter(C.Tables.Table.DATABASE_ID)))
			.addReturningColumn(C.Tables.Table.ID);
		// Execute the Query, and extract the required data.
		ResultSet result = insert.getPrepStmtCore().executeQuery();
		if (result.isBeforeFirst()) {
		    // Extract the Table ID.
		    result.next();
		    respObj.put("success", result.getString(C.Tables.Table.ID));
		} else {
		    // The data was not inserted.
		    // There was an Error.
		    respObj.put("error", "Table Not Created");
		}*/
		respObj.put("error", "Insert not Supported in this Version");
		cdb.close();
		return respObj;
	    } else {
		// It is not a server users.
		respObj.put("error", "Not Priviledged User");
		cdb.close();
		return respObj;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    return respObj;
	}
    }

    // Put Version 0.1
    private JSONObject putV0_1(HttpServletRequest request) {
	// Initialise the Required Variables.
	JSONObject respObj = new JSONObject();
	try {
	    // Get the Required Params
	    String token = request.getParameter(C.Params.TOKEN);
	    // Set up the user and cloud db object for interfacing with the database.
	    CloudDB cdb = new CloudDB(token);
	    // Only a Server User Can Use This API.
	    if (cdb.isServerUser() && cdb.isAuthenticated()) {
		// Lets Build the Statement to allow to get the Accounts or Account requested.
		Update update = new Update(cdb)
			.into("tables")
			.addWhere(new Where("id", Where.Type.EQUAL, request.getParameter("table_id")))
			.addWhere(new Where("database_id", Where.Type.EQUAL, cdb.getDatabaseID()));
		boolean doExecute = false;
		// Check if we have to change the name column
		if (request.getParameterMap().containsKey("name")) {
		    // Add the Column Change
		    update = update.addColumn("name", (Object) request.getParameter("name"));
		    update.addReturningColumn("name");
		    doExecute = true;
		}
		// Check if we have to change the syncable column
		if (request.getParameterMap().containsKey("syncable")) {
		    // Add the Column Change.
		    update = update.addColumn("syncable", (Object) request.getParameter("syncable"));
		    update.addReturningColumn("syncable");
		    doExecute = true;
		}
		if (doExecute) {
		    // The update is good to be executed.
		    ResultSet updateRes = update.getPrepStmtCore().executeQuery();
		    respObj.put(updateRes.isBeforeFirst() ? "success" : "error", updateRes.isBeforeFirst() ? 1 : 0);
		    // Lets push the change to the devices if the table is syncable now.
		    updateRes.next();
		    if (updateRes.getBoolean("syncable") && request.getParameterMap().containsKey("name")) {
			// The table is a syncable table.
			// Lets send the new table name to the system.
			final String tableName = request.getParameter("name").replaceAll(" ", "");
			final String tableID = request.getParameter("table_id").replaceAll(" ", "");
			final String databaseID = cdb.getDatabaseID();
			new Thread(new Runnable() {
			    @Override
			    public void run() {
				try {
				    // Here we will Send the change to all the devices.
				    // Now we will send the data to the device.
				    // Create the Column JSON to be sent.
				    JSONObject colJObj = new JSONObject();
				    JSONObject colIJObj = new JSONObject();
				    // Add the Name Column.
				    colIJObj.put("value", tableName);
				    colIJObj.put("type", "VALUE");
				    colJObj.put("read", colIJObj);

				    // Create the Where JSON to be sent.
				    JSONObject whereJObj = new JSONObject();
				    whereJObj.put("name", "id");
				    whereJObj.put("condition", "EQUAL");
				    whereJObj.put("data", tableID);

				    // Create the Message JSON to be sent.
				    JSONObject msgJObj = new JSONObject();
				    msgJObj.put("type", "UPDATE");
				    msgJObj.put("table_name", "table_");
				    msgJObj.put("columns", colJObj);
				    msgJObj.put("wheres", whereJObj);
				    msgJObj.put("update_time", System.currentTimeMillis());
				    // Here lets extract the RLSs that we have to send the data to.
				    msgJObj.put("sync_id_", UUID.randomUUID());
				    // Send the data to the device.
				    new Sync(null)
					    .push()
					    .sendDataWithDatabaseID(msgJObj, databaseID, true);
				} catch (Exception e) {
				    // There was an Error.
				}
			    }
			}).start();
		    }
		    cdb.close();
		    return respObj;
		} else {
		    respObj.put("error", "No Column to Update");
		    cdb.close();
		    return respObj;
		}
	    } else {
		// It is not a server users.
		respObj.put("error", "Not Priviledged User");
		cdb.close();
		return respObj;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    return respObj;
	}
    }

    // Delete Version 0.1
    private JSONObject deleteV0_1(HttpServletRequest request) {
	// Initialise the Required Variables.
	JSONObject respObj = new JSONObject();
	try {
	    // Get the Required Params
	    String token = request.getParameter(C.Params.TOKEN);
	    // Set up the user and cloud db object for interfacing with the database.
	    CloudDB cdb = new CloudDB(token);
	    // Only a Server User Can Use This API.
	    if (cdb.isServerUser() && cdb.isAuthenticated()) {
		// Lets Build the Statement to allow to get the Accounts or Account requested.
		respObj.put("error", "Delete Not Supported in this Version");
		cdb.close();
		return respObj;
	    } else {
		// It is not a server users.
		respObj.put("error", "Not Priviledged User");
		cdb.close();
		return respObj;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    return respObj;
	}
    }

}
