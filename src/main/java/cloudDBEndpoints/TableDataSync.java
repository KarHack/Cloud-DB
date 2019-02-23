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
import cloudDB.Result;
import cloudDB.Select;
import cloudDB.Sync;
import cloudDB.Sync.Pull;
import helpers.C;
import helpers.Helper;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
public class TableDataSync extends HttpServlet {

    // Variables of the class.
    // Static variables of the class.
    // Can be used to cache some values.
    // Constants for the System.
    public enum TableTenancyType {
        NONE, DATABASE, TENANT, USER, RLS
    }

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
            } else if (version == 0.2f) {
                out.print(getV0_2(request));
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
                if (cdb.isAuthenticated() && cdb.isNormalUser()) {
                    // The Cloud DB connection was successful.
                    // The user is authenticated also.
                    // Let's get the data required.
                    if (request.getServletPath().equalsIgnoreCase("/Table/Data/Sync")) {
                        // It is trying to sync a normal user based table.
                        // We will push the data to the user using the sockets system.
                        // Get the Table to be synced.
                        String tableName = request.getParameter("table_name");
                        // Lets now sync the data to that user's device.
                        PreparedStatement prepStmt = new Select(cdb)
                                            .from(tableName)
                                            .getPreparedStatement();
                        // Execute the Query and Get the Number of Rows.
                        // Also we then will create a new thread and push all the data through cloud push.
                        ResultSet rowRes = prepStmt.executeQuery();
                        respObj.put("success", new Result(rowRes).getJSONResult());
                        cdb.close();
                        return respObj;
                    } else if (request.getServletPath().equalsIgnoreCase("/System/Table/Data/Sync")) {
                        if (request.getParameterMap().containsKey("tenancy_type")) {
                            // It is trying to sync the system tables.
                            // We will Get the Data of the Table and Send in the Response.
                            String tableName = request.getParameter("table_name");

                            // Now we will create the Sync Object to assist with the syncing.
                            Pull pull = new Sync(cdb)
                                                .pull()
                                                .fromSystemTable(tableName)
                                                .setTenancyType(TableTenancyType.valueOf(request.getParameter("tenancy_type")));
                            // Execute the query.
                            try {
                                ResultSet syncData = pull.execute();
                                // Extract the Data.
                                if (syncData.isBeforeFirst()) {
                                    // Found some data. Lets extract it and send it to the user.
                                    // There is a Resultset to be Read, as the user wants some data to be returned.
                                    Result res = new Result(syncData);
                                    respObj.put("success", res.getJSONResult());
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
                            // No tenancy provided.
                            respObj.put("error", "Please provide Tenancy Type");
                        }
                    } else {
                        // Wrong API is Redirected here.
                        respObj.put("error", "No such API Exists");
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
            respObj.put("error", Helper.Error.getErrorMessage(e) + " : " + Helper.Error.getPrintStack(e));
            return respObj;
        }
    }

    // Get Version 0.2
    private JSONObject getV0_2(HttpServletRequest request) {
        // Initialize the Required Variables.
        JSONObject respObj = new JSONObject();
        try {
            // Get the Required Variables.
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
                // Here we will Validate if the User is Authorized.
                if (cdb.isAuthenticated() && cdb.isNormalUser()) {
                    // The Cloud DB connection was successful.
                    // The user is authenticated also.
                    // Let's get the data required.
                    if (request.getServletPath().equalsIgnoreCase("/Table/Data/Sync")) {
                        // It is trying to sync a normal user based table.
                        // Get the Data to be synced.
                        try {
                            // Here we will create the Select.
                            // Setup with the required table.
                            // Get the Table to be synced.
                            final String tableName = request.getParameter("table_name");
                            // Lets now sync the data to that user's device.
                            final CloudDB cdbL = cdb;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        // Here we will Create the Select Object.
                                        // Get the Data from the Database & Finally Sending the data back to the user,
                                        // using Cloud Push.
                                        PreparedStatement prepStmt = new Select(cdbL)
                                                .from(tableName)
                                                .getPreparedStatement();
                                        // Lets Now Get the Data from the Database.
                                        ResultSet tbRes = prepStmt.executeQuery();
                                        if (tbRes.isBeforeFirst()) {
                                            // There is data to be synced.
                                            // Now lets Read the data & Sync it with the Current Device.
                                            String pushToken = cdbL.getCurrentDevice().getPushToken();
                                            // Let Get the Data to be sent in a JSON Format.
                                            JSONArray resJArr = new Result(tbRes).getJSONResult();
                                            for (int jObjIndex = 0; jObjIndex < resJArr.size(); jObjIndex++) {
                                                try {
                                                    // Here we will Get the JSON Object from the Array.
                                                    JSONObject resJObj = (JSONObject) resJArr.get(jObjIndex);
                                                    // Lets send this message to the Push Token.
                                                    new Sync(cdbL)
                                                        .push()
                                                        .addDeviceToken(pushToken)
                                                        .putMessage(resJObj)
                                                        .execute();
                                                } catch (Exception er) {
                                                    // There was an Error.
                                                }
                                            }
                                        } else {
                                            // There is no data to be synced.
                                        }
                                    } catch (Exception er) {
                                        // There was an Error.
                                    }
                                }
                            })
                                                .start();
                        } catch (Exception er) {
                            // There was an Error.
                            respObj.clear();
                            respObj.put("error", "Error in Syncing : " + er.getLocalizedMessage());
                        }
                    } else {
                        // Wrong API is Redirected here.
                        respObj.clear();
                        respObj.put("error", "No such API Exists");
                    }
                    cdb.close();
                    return respObj;
                } else {
                    // The user is not authorized to sync this table.
                    respObj.put("error", "User Not Authenticated");
                    cdb.close();
                    return respObj;
                }
            }
        } catch (Exception er) {
            // There was an Error.
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
                    respObj.put("success", "User Authenticated");
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
                    respObj.put("success", "User Authenticated");
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
                    respObj.put("success", "User Authenticated");
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
}
