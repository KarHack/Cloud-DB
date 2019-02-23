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
import helpers.C;
import helpers.Helper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
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
public class UserRoleRLS extends HttpServlet {

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
		    // Only the Server user can get the ACL of the Users.
		    if (cdb.isServerUser()) {
			// It is a server user.
			// Get the required params like User ID.
			HashMap<Long, short[]> roleRLS = cdb
				.getRoleRLS(UUID.fromString(request.getParameter("user_id")));
			// Extract the Table ACL from the Hashmap.
			Iterator<Long> roleIt = roleRLS.keySet().iterator();
			JSONArray roleRLSJArr = new JSONArray();
			while (roleIt.hasNext()) {
			    // Extract the data.
			    long roleID = roleIt.next();
			    short[] roleCRUD = roleRLS.get(roleID);
			    JSONObject roleRLSJObj = new JSONObject();
			    roleRLSJObj.put("role_id", roleID);
			    roleRLSJObj.put("read", roleCRUD[0]);
			    roleRLSJObj.put("write", roleCRUD[1]);
			    roleRLSJObj.put("edit", roleCRUD[2]);
			    roleRLSJObj.put("remove", roleCRUD[3]);
			    // Add the JSON Object into the JSON Array.
			    roleRLSJArr.add(roleRLSJObj);
			}
			// Add the table ACL to the Response JSON Object.
			respObj.put("success", roleRLSJArr);
			cdb.close();
			return respObj;
		    } else {
			// It is a normal sync user, and not authorized for this API.
			respObj.put("error", "User Not Authorized");
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
		    // Only the Server User Can Add the Priviledge of a sync user.
		    if (cdb.isServerUser()) {
			// It is a server user.
			boolean granted = cdb.grantRoleRLS(
				UUID.fromString(request.getParameter("user_id")),
				Long.parseLong(request.getParameter("role_id")),
				Boolean.parseBoolean(request.getParameter("read")) ? (short) 1 : (short) 0,
				Boolean.parseBoolean(request.getParameter("write")) ? (short) 1 : (short) 0,
				Boolean.parseBoolean(request.getParameter("edit")) ? (short) 1 : (short) 0,
				Boolean.parseBoolean(request.getParameter("remove")) ? (short) 1 : (short) 0);
			respObj.put(granted ? "success" : "error", granted);
			cdb.close();
			return respObj;
		    } else {
			// It is a normal user.
			respObj.put("error", "User Not Authorized");
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
		    // Let's get the data required
		    // Only the Server User Can Add the Priviledge of a sync user.
		    if (cdb.isServerUser()) {
			// It is a server user.
			boolean updatedGrant = cdb.updateGrantRoleRLS(
				UUID.fromString(request.getParameter("user_id")),
				Long.parseLong(request.getParameter("role_id")),
				Boolean.parseBoolean(request.getParameter("read")) ? (short) 1 : (short) 0,
				Boolean.parseBoolean(request.getParameter("write")) ? (short) 1 : (short) 0,
				Boolean.parseBoolean(request.getParameter("edit")) ? (short) 1 : (short) 0,
				Boolean.parseBoolean(request.getParameter("remove")) ? (short) 1 : (short) 0);
			respObj.put(updatedGrant ? "success" : "error", updatedGrant);
			cdb.close();
			return respObj;
		    } else {
			// It is a normal user.
			respObj.put("error", "User Not Authorized");
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
		    // Only a server user can revoke a priviledge.
		    if (cdb.isServerUser()) {
			// It is a server user.
			boolean revoked = cdb.revokeGrantRoleRLS(
				UUID.fromString(request.getParameter("user_id")),
				Long.parseLong(request.getParameter("role_id")));
			respObj.put(revoked ? "success" : "error", revoked);
			cdb.close();
			return respObj;
		    } else {
			// It is a normal user.
			respObj.put("error", "User Not Authorized");
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
