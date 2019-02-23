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
public class Groups extends HttpServlet {
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
            out.println("Unknown Version");
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
            out.println("Unknown Version");
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

            // Only Server User is Allowed to Access this Data.
            if (cdb.isServerUser() && cdb.isAuthenticated()) {
                // Lets Build the Statement to allow to get the Accounts or Account requested.
                Select select = new Select()
                                    .with(cdb)
                                    .addColumn("id")
                                    .addColumn("name")
                                    .addColumn("tenant_id")
                                    .addColumn("database_id")
                                    .addColumn("time_stamp")
                                    .fromSystem(C.Tables.GROUPS)
                                    .addWhere(new Where("database_id", Where.Type.EQUAL, cdb.getDatabaseID()));
                // Check if to get just one role count.
                if (request.getParameterMap().containsKey("group_id")) {
                    select.addWhere(new Where("id",
                                        Where.Type.EQUAL, Long.parseLong(request.getParameter("group_id"))));
                }
                // Check if there is a tenant id.
                if (request.getParameterMap().containsKey("tenant_id")) {
                    select.addWhere(new Where("tenant_id",
                                        Where.Type.EQUAL, Long.parseLong(request.getParameter("tenant_id"))));
                }
                // Check if there is a Name of the Group.
                if (request.getParameterMap().containsKey("name")) {
                    select.addWhere(new Where("name",
                                        Where.Type.EQUAL, request.getParameter("name")));
                }

                // Execute the query.
                try {
                    ResultSet groupData = select.getPrepStmtCore().executeQuery();
                    // Extract the Data.
                    if (groupData.isBeforeFirst()) {
                        // Found some data. Lets extract it and send it to the user.
                        JSONArray groupJArr = new JSONArray();
                        while (groupData.next()) {
                            JSONObject groupJObj = new JSONObject();
                            groupJObj.put("id", groupData.getLong("id"));
                            groupJObj.put("name", groupData.getString("name"));
                            groupJObj.put("tenant_id", groupData.getLong("tenant_id"));
                            groupJObj.put("time_stamp", groupData.getString("time_stamp"));
                            // Add the Json Object to the main object.
                            groupJArr.add(groupJObj);
                        }
                        // Add the Array to the Main Response Object.
                        respObj.put("success", groupJArr);
                        cdb.close();
                        return respObj;
                    } else {
                        // There is no data in the database roleording to the query.
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

            // Only the Server User can Access this Data.
            if (cdb.isServerUser() && cdb.isAuthenticated()) {
                // Lets Build the Statement to allow to get the Accounts or Account requested.
                // Insert the Company Data.
                Insert insert = new Insert()
                                    .with(cdb)
                                    .intoSystem(C.Tables.GROUPS)
                                    .putColumn(new ColumnData("name", request.getParameter("name")))
                                    .putColumn(new ColumnData("tenant_id", request.getParameter("tenant_id")))
                                    .putColumn(new ColumnData("database_id", cdb.getDatabaseID()))
                                    .addReturningColumn("id")
                                    .addReturningColumn("time_stamp");
                // Execute the Query, and extract the required data.
                final ResultSet result = insert.getPrepStmtCore().executeQuery();
                if (result.isBeforeFirst()) {
                    // Extract the Group ID.
                    result.next();
                    respObj.put("success", result.getLong("id"));
                    // Now lets update all the devices of the user of the tenant.
                    try {
                        // Lets Send the Data to the Devices.
                        final long tenantID = Long.parseLong(request.getParameter("tenant_id").replaceAll(" ", ""));
                        final String groupName = request.getParameter("name").replaceAll(" ", "");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Here we will send the update to that device.
                                    // Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    colJObj.put("id", result.getLong("id"));
                                    colJObj.put("tenant_id", tenantID);
                                    colJObj.put("name", groupName);
                                    colJObj.put("time_stamp", result.getString("time_stamp"));

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "INSERT");
                                    msgJObj.put("table_name", "groups_");
                                    msgJObj.put("columns", colJObj);
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
                } else {
                    // The data was not inserted.
                    // There was an Error.
                    respObj.put("error", "Role Not Added");
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

            // Only Server User can use this API.
            if (cdb.isServerUser() && cdb.isAuthenticated()) {
                // Lets Build the Statement to allow to Update the Account requested.
                Update update = new Update()
                                    .with(cdb)
                                    .intoSystem(C.Tables.GROUPS)
                                    .addWhere(new Where("database_id", Where.Type.EQUAL, cdb.getDatabaseID()));
                // Add the needed columns.
                if (request.getParameterMap().containsKey("name")) {
                    update.addColumn(C.Tables.Roles.NAME, (Object) request.getParameter(C.Tables.Roles.NAME));
                }
                update.addWhere(new Where("id", Where.Type.EQUAL, Long.parseLong(request.getParameter("group_id"))))
                                    .addReturningColumn("id")
                                    .addReturningColumn("tenant_id");
                // Execute the Update.
                final ResultSet result = update.getPrepStmtCore().executeQuery();
                if (result.isBeforeFirst()) {
                    // The Data was succesfully Updated.
                    result.next();
                    respObj.put("success", result.getLong("id"));

                    // Lets now update the devices
                    try {
                        // Here we will send the update to that device.
                        // Now we will send the data to the device.
                        // Create the Column JSON to be sent.
                        final JSONObject colJObj = new JSONObject();
                        final JSONObject colIJObj = new JSONObject();
                        final String groupName = request.getParameter(C.Tables.Roles.NAME);
                        // Add the Name Column.
                        if (request.getParameterMap().containsKey("name")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        // Here we will send the data to the devices
                                        colIJObj.put("value", groupName);
                                        colIJObj.put("type", "VALUE");
                                        colJObj.put("name", colIJObj);
                                        // Create the Where JSON to be sent.
                                        JSONObject whereJObj = new JSONObject();
                                        whereJObj.put("name", "id");
                                        whereJObj.put("condition", "EQUAL");
                                        whereJObj.put("data", result.getLong("id"));

                                        // Create the Message JSON to be sent.
                                        JSONObject msgJObj = new JSONObject();
                                        msgJObj.put("type", "UPDATE");
                                        msgJObj.put("table_name", "groups_");
                                        msgJObj.put("columns", colJObj);
                                        msgJObj.put("wheres", whereJObj);
                                        msgJObj.put("update_time", System.currentTimeMillis());
                                        // Here lets extract the RLSs that we have to send the data to.
                                        msgJObj.put("sync_id_", UUID.randomUUID());
                                        // Send the data to the device.
                                        new Sync(null)
                                                            .push()
                                                            .sendDataWithTenantID(msgJObj, result.getLong("tenant_id"), true);
                                    } catch (Exception e) {
                                        // There was an Error.
                                    }
                                }
                            }).start();
                        }
                    } catch (Exception e) {
                        // There was an Error.
                    }
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
            // Only the Server User can Use this API.
            if (cdb.isServerUser() && cdb.isAuthenticated()) {
                // Lets Build the Statement to allow to get the Accounts or Account requested.
                Delete delete = new Delete(cdb)
                                    .fromSystem("groups")
                                    .addWhere(new Where("id", Where.Type.EQUAL, Long.parseLong(request.getParameter("group_id"))))
                                    .addWhere(new Where("database_id", Where.Type.EQUAL, cdb.getDatabaseID()))
                                    .addReturningColumn("tenant_id");
                final ResultSet deleteRes = delete.getPrepStmtCore().executeQuery();
                respObj.put(deleteRes.isBeforeFirst() ? "success" : "error", deleteRes.isBeforeFirst() ? 1 : 0);
                // Send the Data to the Devices.
                try {
                    // Here we will send the data to the users.
                    final long groupID = Long.parseLong(request.getParameter("group_id"));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Now we will send the data to the device.
                                // Create the Where JSON to be sent.
                                deleteRes.next();
                                JSONObject whereJObj = new JSONObject();
                                whereJObj.put("name", "id");
                                whereJObj.put("condition", "EQUAL");
                                whereJObj.put("data", groupID);

                                // Create the Message JSON to be sent.
                                JSONObject msgJObj = new JSONObject();
                                msgJObj.put("type", "DELETE");
                                msgJObj.put("table_name", "groups_");
                                msgJObj.put("wheres", whereJObj);
                                msgJObj.put("update_time", System.currentTimeMillis());
                                // Here lets extract the RLSs that we have to send the data to.
                                msgJObj.put("sync_id_", UUID.randomUUID());
                                // Send the data to the device.
                                new Sync(null)
                                                    .push()
                                                    .sendDataWithTenantID(msgJObj, deleteRes.getLong("tenant_id"), true);
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
