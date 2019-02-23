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
import cloudDB.Device;
import cloudDB.User;
import helpers.C;
import helpers.Helper;
import java.io.IOException;
import java.io.PrintWriter;
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
public class UserSync extends HttpServlet {

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
	    out.println("Unknown Version : " + Helper.Error.getErrorMessage(e));
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
	    out.println("Unknown Version : " + Helper.Error.getErrorMessage(e));
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
	    out.println("Unknown Version : " + Helper.Error.getErrorMessage(e));
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
	    out.println("Unknown Version : " + Helper.Error.getErrorMessage(e));
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
	    // Proceed with Inserting the Data from the database as requested by the user.
	    if (cdb == null) {
		// There was an Error, and the Cloud DB Connection is null.
		respObj.put("error", "User Not Authenticated");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    // Check if the User is a server user.
		    if (cdb.isServerUser()) {
			// It is a server user.
			// Get all the Users in the System.
			long tenantID = 0;
			int limit = 0;
			int offset = 0;
			// Get the Params from the data.
			if (request.getParameterMap().containsKey("tenant_id")) {
			    tenantID = Long.parseLong(request.getParameter("tenant_id"));
			}
			if (request.getParameterMap().containsKey("limit")) {
			    limit = Integer.parseInt(request.getParameter("limit"));
			}
			if (request.getParameterMap().containsKey("offset")) {
			    offset = Integer.parseInt(request.getParameter("offset"));
			}
			// Get the users according to the client request.
			cloudDB.User[] users = cdb.getUsers(tenantID, limit, offset);
			// Show to the Client.
			JSONArray userJArr = new JSONArray();
			for (User user : users) {
			    // Create the User Json Object.
			    JSONObject userJObj = new JSONObject();
			    // Add the User Details.
			    userJObj.put("user_id", user.getUserID().toString());
			    userJObj.put("tenant_id", user.getTenantID());
			    userJObj.put("is_active", user.isActive());
			    // Get the Devices from the User Object.
			    JSONArray deviceJArr = new JSONArray();
			    for (Device device : user.getDevices()) {
				try {
				    // Extract the Devices from the User Object.
				    JSONObject deviceJObj = new JSONObject();
				    deviceJObj.put("uid", device.getDeviceUID().toString());
				    deviceJObj.put("id", device.getDeviceID());
				    deviceJObj.put("type", device.getType().toString().toLowerCase());
				    deviceJObj.put("push_token", device.getPushToken());
				    // Add the Device JObject into the JArray.
				    deviceJArr.add(deviceJObj);
				} catch (Exception e) {
				    // There was an Error.
				}
			    }
			    // Add the Devices Array into the User JObject.
			    userJObj.put("devices", deviceJArr);
			    // Add the User JObject into the User JArray.
			    userJArr.add(userJObj);
			}
			// Add the Users Json Object into the Response JObject.
			respObj.put("success", userJArr);
			cdb.close();
			return respObj;
		    } else {
			// It is a syncable user, so let's not allow the user to go ahead.
			respObj.put("error", "Not Enough Priviledges");
			cdb.close();
			return respObj;
		    }
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "User Not Authenticated");
		    cdb.close();
		    return respObj;
		}
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
	    // Check if there is a server id.
	    // Then the user is trying to login as a server user.
	    // Set up the user and cloud db object for interfacing with the database.
	    CloudDB cdb = new CloudDB(token);
	    // Proceed with Inserting the Data from the database as requested by the user.
	    if (cdb == null) {
		// There was an Error, and the Cloud DB Connection is null.
		respObj.put("error", "User Not Authenticated");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    // Check if the User is a server user.
		    if (cdb.isServerUser()) {
			// It is a server user.
			// Get the Parameters required and Create the User.
			User user = cdb.createUser(Long.parseLong(request.getParameter("tenant_id")),
				Boolean.parseBoolean(request.getParameter("is_active")));
			// Give the User's Data.
			if (user == null) {
			    // User not created.
			    respObj.put("error", "User Not Created");
			    respObj.put("status", cdb.getStatusTrace());
			} else {
			    // User Created.
			    JSONObject userJObj = new JSONObject();
			    userJObj.put("user_id", user.getUserID().toString());
			    userJObj.put("tenant_id", user.getTenantID());
			    userJObj.put("token", user.getToken());
			    userJObj.put("time_stamp", user.getTimeStamp());
			    // Add the User Object to the Response.
			    respObj.put("success", userJObj);
			}
			cdb.close();
			return respObj;
		    } else {
			// It is a syncable user, so let's not allow the user to go ahead.
			respObj.put("error", "Not Enough Priviledges");
			cdb.close();
			return respObj;
		    }
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "User Not Authenticated");
		    cdb.close();
		    return respObj;
		}
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    return respObj;
	}
    }

    // Put Version 0.1
    private JSONObject putV0_1(HttpServletRequest request) {
	JSONObject respObj = new JSONObject();
	try {
	    // Get the Required Params
	    String token = request.getParameter(C.Params.TOKEN);
	    // Check if there is a server id.
	    // Then the user is trying to login as a server user.
	    // Set up the user and cloud db object for interfacing with the database.
	    CloudDB cdb = new CloudDB(token);
	    // Proceed with Inserting the Data from the database as requested by the user.
	    if (cdb == null) {
		// There was an Error, and the Cloud DB Connection is null.
		respObj.put("error", "User Not Authenticated");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    // Check if the User is a server user.
		    if (cdb.isServerUser()) {
			// It is a server user.
			// Lets Get the Params to update into the user.
			if (Boolean.parseBoolean(request.getParameter("is_active"))) {
			    // Here we have to activate the user.
			    boolean activated = cdb.activateUser(UUID.fromString(request.getParameter("user_id")));
			    respObj.put(activated ? "success" : "error", activated ? "Activated" : "Not Activated");
			} else {
			    // Here we have to suspend the user.
			    boolean suspended = cdb.suspendUser(UUID.fromString(request.getParameter("user_id")));
			    respObj.put(suspended ? "success" : "error", suspended ? "Suspended" : "Not Suspended");
			}
			cdb.close();
			return respObj;
		    } else {
			// It is a syncable user, so let's not allow the user to go ahead.
			respObj.put("error", "Not Enough Priviledges");
			cdb.close();
			return respObj;
		    }
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "User Not Authenticated");
		    cdb.close();
		    return respObj;
		}
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    return respObj;
	}
    }

    // Delete Version 0.1
    private JSONObject deleteV0_1(HttpServletRequest request) {
	JSONObject respObj = new JSONObject();
	try {
	    // Get the Required Params
	    String token = request.getParameter(C.Params.TOKEN);
	    // Check if there is a server id.
	    // Then the user is trying to login as a server user.
	    // Set up the user and cloud db object for interfacing with the database.
	    CloudDB cdb = new CloudDB(token);
	    // Proceed with Inserting the Data from the database as requested by the user.
	    if (cdb == null) {
		// There was an Error, and the Cloud DB Connection is null.
		respObj.put("error", "User Not Authenticated");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    // Check if the User is a server user.
		    if (cdb.isServerUser()) {
			// It is a server user.
			// Lets remove the user.
			boolean isRemoved = cdb.removeUser(UUID.fromString(request.getParameter("user_id")));
			respObj.put(isRemoved ? "success" : "error", isRemoved ? "User Removed" : "User Not Removed");
			cdb.close();
			return respObj;
		    } else {
			// It is a syncable user, so let's not allow the user to go ahead.
			respObj.put("error", "Not Enough Priviledges");
			cdb.close();
			return respObj;
		    }
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "User Not Authenticated");
		    cdb.close();
		    return respObj;
		}
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    return respObj;
	}
    }
}
