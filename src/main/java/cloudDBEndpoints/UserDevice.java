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
public class UserDevice extends HttpServlet {

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
		    // Check if the required params are given.
		    UUID userID = null;
		    UUID deviceID = null;
		    int limit = 0;
		    int offset = 0;
		    if (request.getParameterMap().containsKey("user_id")) {
			// The user id is provided.
			userID = UUID.fromString(request.getParameter("user_id"));
		    }
		    if (request.getParameterMap().containsKey("device_uid")) {
			// The user id is provided.
			deviceID = UUID.fromString(request.getParameter("device_uid"));
		    }
		    if (request.getParameterMap().containsKey("limit")) {
			// The user id is provided.
			limit = Integer.parseInt(request.getParameter("limit"));
		    }
		    if (request.getParameterMap().containsKey("offset")) {
			// The user id is provided.
			offset = Integer.parseInt(request.getParameter("offset"));
		    }
		    // Run the Query.
		    Device[] devices = cdb.getDevices(userID, deviceID, limit, offset);
		    // Extract the data from the device array.
		    JSONArray deviceJArr = new JSONArray();
		    for (Device device : devices) {
			try {
			    // Here we will Extract the Device Data.
			    JSONObject deviceJObj = new JSONObject();
			    deviceJObj.put("device_id", device.getDeviceID());
			    deviceJObj.put("token", device.getDeviceToken());
			    deviceJObj.put("device_uid", device.getDeviceUID().toString());
			    deviceJObj.put("push_token", device.getPushToken());
			    deviceJObj.put("type", device.getType().toString());
			    deviceJObj.put("user_id", device.getUserID().toString());
			    // Add the Device JObject to the JArray.
			    deviceJArr.add(deviceJObj);
			} catch (Exception e) {
			    // There was an Error.
			}
		    }
		    // Lets return the device json array.
		    respObj.put("success", deviceJArr);
		    cdb.close();
		    return respObj;
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
    // This API can only be used by a server user
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
		    if (cdb.isServerUser()) {
			// The user is a server user, and can register a device.
			Device device = cdb.registerDevice(request.getParameter("user_token"),
				Device.Type.valueOf(request.getParameter("type").toUpperCase()),
				request.getParameter("device_id"),
				319833710866497537l,
				request.getParameter("push_token"));
			// Validate if the device was registered successfully.
			if (device != null) {
			    // The Device is registered successfully.
			    // Extract the Required data from the device object and return.
			    JSONObject deviceJObj = new JSONObject();
			    deviceJObj.put("device_uid", device.getDeviceUID().toString());
			    deviceJObj.put("token", device.getDeviceToken());
			    respObj.put("success", deviceJObj);
			} else {
			    // The Device is not registered successfully.
			    respObj.put("error", "Device not Created because User is set Inactive");
			}
			cdb.close();
			return respObj;
		    } else {
			// The user is a normal user.
			respObj.put("error", "Normal User Cannot Directly Register Device");
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
    // This will be used to update the push token of every device.
    // This can only be done
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
		    if (cdb.isServerUser()) {
			// If this is a server user.
			int updated = cdb.updatePushToken(request.getParameter("device_token"),
				request.getParameter("new_push_token"));
			respObj.put(updated > 0 ? "success" : "error", updated);
		    } else {
			// The user is a normal user.
			respObj.put("error", "Sync Users cannot update push tokens.");
		    }
		    cdb.close();
		    return respObj;
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
		    if (cdb.isServerUser()) {
			// The user is a server user.
			boolean removedDevice = cdb.removeDevice(UUID.fromString(request.getParameter("device_uid")));
			respObj.put(removedDevice ? "success" : "error", removedDevice);
			cdb.close();
			return respObj;
		    } else {
			// It is a normal user.
			respObj.put("error", "Normal User Cannot Unregister Devices.");
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
