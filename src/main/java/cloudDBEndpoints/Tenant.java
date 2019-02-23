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
import cloudDB.ColumnData;
import cloudDB.Delete;
import cloudDB.Insert;
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
public class Tenant extends HttpServlet {
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
	    // Only a Server User can use this API.
	    if (cdb.isServerUser() && cdb.isAuthenticated()) {
		// Lets Build the Statement to allow to get the Accounts or Account requested.
		Select select = new Select()
			.with(cdb)
			.addColumn(C.Tables.Tenants.ID, "tenant_id")
			.addColumn(C.Tables.Tenants.NAME)
			.addColumn(C.Tables.Tenants.DATABASE_ID)
			.addColumn(C.Tables.Tenants.TIME_STAMP)
			.fromSystem(C.Tables.TENANTS)
			.addWhere(new Where(C.Tables.Tenants.DATABASE_ID, Where.Type.EQUAL, cdb.getDatabaseID()));
		// Check if to get just one.
		if (request.getParameterMap().containsKey("tenant_id")) {
		    select.addWhere(new Where(C.Tables.Tenants.ID,
			    Where.Type.EQUAL, Long.parseLong(request.getParameter("tenant_id"))));
		}
		// Execute the query.
		try {
		    ResultSet tenantData = select.getPrepStmtCore().executeQuery();
		    // Extract the Data.
		    if (tenantData.isBeforeFirst()) {
			// Found some data. Lets extract it and send it to the user.
			JSONArray tenantJArr = new JSONArray();
			while (tenantData.next()) {
			    JSONObject tenantJObj = new JSONObject();
			    tenantJObj.put("id", tenantData.getLong("tenant_id"));
			    tenantJObj.put(C.Tables.Tenants.NAME, tenantData.getString(C.Tables.Tenants.NAME));
			    tenantJObj.put(C.Tables.Tenants.DATABASE_ID, tenantData.getString(C.Tables.Tenants.DATABASE_ID));
			    tenantJObj.put("timestamp", tenantData.getString(C.Tables.Tenants.TIME_STAMP));
			    // Add the Json Object to the main object.
			    tenantJArr.add(tenantJObj);
			}
			// Add the Array to the Main Response Object.
			respObj.put("success", tenantJArr);
			cdb.close();
			return respObj;
		    } else {
			// There is no data in the database tenantording to the query.
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

    // Post Version 0.1
    private JSONObject postV0_1(HttpServletRequest request) {
	// Initialise the Required Variables.
	JSONObject respObj = new JSONObject();
	try {
	    // Get the Required Params
	    String token = request.getParameter(C.Params.TOKEN);
	    // Set up the user and cloud db object for interfacing with the database.
	    CloudDB cdb = new CloudDB(token);
	    // Only the Server User can Use this API.
	    if (cdb.isServerUser() && cdb.isAuthenticated()) {
		// Lets Build the Statement to allow to get the Accounts or Account requested.
		// Insert the Company Data.
		Insert insert = new Insert()
			.with(cdb)
			.intoSystem(C.Tables.TENANTS)
			.putColumn(new ColumnData(C.Tables.Tenants.NAME, request.getParameter(C.Tables.Tenants.NAME)))
			.putColumn(new ColumnData(C.Tables.Tenants.DATABASE_ID, cdb.getDatabaseID()))
			.addReturningColumn(C.Tables.Tenants.ID);
		// Execute the Query, and extract the required data.
		ResultSet result = insert.getPrepStmtCore().executeQuery();
		if (result.isBeforeFirst()) {
		    // Extract the Account ID.
		    result.next();
		    respObj.put("success", result.getLong(C.Tables.Tenants.ID));
		} else {
		    // The data was not inserted.
		    // There was an Error.
		    respObj.put("error", "Tenant Not Added");
		}
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
	    // Only a Server User Can use this API.
	    if (cdb.isServerUser() && cdb.isAuthenticated()) {
		// Lets Build the Statement to allow to Update the Account requested.
		Update update = new Update()
			.with(cdb)
			.intoSystem(C.Tables.TENANTS)
			// Add the needed columns.
			.addColumn(C.Tables.Tenants.NAME, (Object) request.getParameter(C.Tables.Tenants.NAME))
			// Add the Multi-Database Where Security.
			.addWhere(new Where(C.Tables.Tenants.DATABASE_ID, Where.Type.EQUAL, cdb.getDatabaseID()))
			// Add the Where.Type.
			.addWhere(new Where(true, C.Tables.Tenants.ID, Where.Type.EQUAL, Long.parseLong(request.getParameter("tenant_id"))))
			// We need to get the id of the updated field.
			.addReturningColumn(C.Tables.Tenants.ID);
		// Execute the Update.
		ResultSet result = update.getPrepStmtCore().executeQuery();
		if (result.isBeforeFirst()) {
		    // The Data was succesfully Updated.
		    result.next();
		    respObj.put("success", result.getLong(C.Tables.Tenants.ID));
		} else {
		    // The Data was not updated.
		    respObj.put("error", "Not Updated");
		}
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
	    respObj.put("error", "Main Err : " + Helper.Error.getErrorMessage(e));
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
	    // Only Server User can use this API.
	    if (cdb.isServerUser() && cdb.isAuthenticated()) {
		// Lets Build the Statement to allow to get the Accounts or Account requested.
		Delete delete = new Delete(cdb)
			.fromSystem("tenants")
			.addWhere(new Where("id", Where.Type.EQUAL, Long.parseLong(request.getParameter("tenant_id"))))
			.addWhere(new Where("database_id", Where.Type.EQUAL, cdb.getDatabaseID()));
		int deleteRes = delete.getPrepStmtCore().executeUpdate();
		respObj.put(deleteRes == 0 ? "error" : "success", deleteRes);
		try {
		    // Lets now push this data to all the tenant's devices.
		    final long tenantID = Long.parseLong(request.getParameter("tenant_id"));
		    new Thread(new Runnable() {
			@Override
			public void run() {
			    try {
				// Here we will send the data to the devices.
				// Now we will send the data to the device.
				// Create the Column JSON to be sent.
				JSONObject colJObj = new JSONObject();
				JSONObject colIJObj = new JSONObject();
				// Add the Read Column.
				colIJObj.put("value", false);
				colIJObj.put("type", "VALUE");
				colJObj.put("is_active", colIJObj);

				// Create the Where JSON to be sent.
				JSONObject whereJObj = new JSONObject();
				whereJObj.put("name", "tenant_id");
				whereJObj.put("condition", "EQUAL");
				whereJObj.put("data", tenantID);

				// Create the Message JSON to be sent.
				JSONObject msgJObj = new JSONObject();
				msgJObj.put("type", "UPDATE");
				msgJObj.put("table_name", "users_");
				msgJObj.put("columns", colJObj);
				msgJObj.put("wheres", whereJObj);
				msgJObj.put("update_time", System.currentTimeMillis());
				// Here lets extract the RLSs that we have to send the data to.
				msgJObj.put("sync_id_", UUID.randomUUID());
				// Send the data to the device.
				new Sync(null)
					.push()
					.sendDataWithTenantID(msgJObj, tenantID, true);
			    } catch (Exception e) {
				// There was an Error.
			    }
			}
		    }).start();
		} catch (Exception e) {
		    // There was an Error.
		}
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
