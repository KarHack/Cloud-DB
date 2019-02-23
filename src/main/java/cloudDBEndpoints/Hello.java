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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

/**
 *
 * @author karan
 */
public class Hello extends HttpServlet {

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
		respObj.put("type", "GET");
		respObj.put("status", cdb.getStatusTrace());
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    respObj.put("success", "User Authenticated");
		    respObj.put("type", "GET");
		    respObj.put("status", cdb.getStatusTrace());
		    cdb.close();
		    return respObj;
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "User Not Authenticated");
		    respObj.put("type", "GET");
		    respObj.put("status", cdb.getStatusTrace());
		    cdb.close();
		    return respObj;
		}
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e) + " : " + Helper.Error.getPrintStack(e));
	    respObj.put("type", "GET");
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
		respObj.put("type", "POST");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    respObj.put("success", "User Authenticated");
		    respObj.put("type", "POST");
		    cdb.close();
		    return respObj;
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "User Not Authenticated");
		    respObj.put("type", "POST");
		    cdb.close();
		    return respObj;
		}
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    respObj.put("type", "POST");
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
		respObj.put("type", "PUT");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    respObj.put("success", "User Authenticated");
		    respObj.put("type", "PUT");
		    cdb.close();
		    return respObj;
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "User Not Authenticated");
		    respObj.put("type", "PUT");
		    cdb.close();
		    return respObj;
		}
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    respObj.put("type", "PUT");
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
		respObj.put("type", "DELETE");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    respObj.put("success", "User Authenticated");
		    respObj.put("type", "DELETE");
		    cdb.close();
		    return respObj;
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "User Not Authenticated");
		    respObj.put("type", "DELETE");
		    cdb.close();
		    return respObj;
		}
	    }
	} catch (Exception e) {
	    // There was an Error.
	    respObj.put("error", Helper.Error.getErrorMessage(e));
	    respObj.put("type", "DELETE");
	    return respObj;
	}
    }
}
