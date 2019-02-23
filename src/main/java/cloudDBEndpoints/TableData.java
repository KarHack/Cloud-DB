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
import cloudDB.Join;
import cloudDB.Result;
import cloudDB.Select;
import cloudDB.Sync;
import cloudDB.Update;
import cloudDB.Where;
import helpers.C;
import helpers.Helper;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author karan
 */
public class TableData extends HttpServlet {

    // Variables of the class.
    private String status;
    private String joinWithTable;
    private List<String> tablesBeingCalled;
    private long deletedTimeI = 0;

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
            if (version == 0.1f || version == 0.11f) {
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
            tablesBeingCalled = new ArrayList<>();
            // Proceed with Inserting the Data from the database as requested by the user.
            if (cdb == null) {
                // There was an Error, and the Cloud DB Connection is null.
                respObj.put("error", "User Not Authenticated");
                cdb.close();
                return respObj;
            } else {
                // The Cloud DB connection was successful.
                if (cdb.isAuthenticated()) {
                    // The user is authenticated also.
                    // Let's get the data required.
                    try {
                        // Create the Select Query.
                        // Setup with the required table.
                        Select select = new Select(cdb);
                        // Provide the Columns and their data.
                        // Here we extract the columns from the JSON Object.
                        try {
                            // Check if there are any columns that we have to get.
                            if (request.getParameterMap().containsKey("columns")) {
                                // There are columns that have to be selected.
                                // Parse the columns from the Request.
                                JSONArray colJArr = (JSONArray) new JSONParser().parse(request.getParameter("columns"));
                                // Get all the Columns along with their data.
                                for (int columnCount = 0; columnCount < colJArr.size(); columnCount++) {
                                    try {
                                        // Add the Column into the Insert query.
                                        try {
                                            // Lets try to add it with the AS column
                                            JSONObject colJObj = (JSONObject) colJArr.get(columnCount);
                                            select.addColumn(colJObj.keySet().iterator().next().toString(),
                                                                colJObj.get(colJObj.keySet().iterator().next().toString()).toString());
                                        } catch (Exception e) {
                                            // There was an Error.
                                            // It is a simple string, and ot an AS column
                                            select.addColumn(colJArr.get(columnCount).toString());
                                        }
                                    } catch (Exception e) {
                                        // There was an Error.
                                        //respObj.put("error" + columnCount, "Column Data Parsing " + e.getLocalizedMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // There was an Error.
                            respObj.put("error", "Column Data Parsing");
                            cdb.close();
                            return respObj;
                        }

                        // Set the table to be read from.
                        select.from(request.getParameter("table_name"));
                        joinWithTable = select.getTableName();
                        tablesBeingCalled.add(request.getParameter("table_name"));
                        // Add any join clauses if required.
                        try {
                            // Check if there are any join clauses.
                            if (request.getParameterMap().containsKey("joins")) {
                                // There is a join clause.
                                // Add the Join to the Select.
                                if (Float.parseFloat(request.getParameter(C.Params.VERSION_NO)) == 0.1f) {
                                    select = setupJoinsV1(select, ((JSONObject) new JSONParser().parse(request.getParameter("joins"))));
                                }
                                // Check if there was an Error in the Where Section.
                                if (select == null) {
                                    // There was an Error in the Where Section.
                                    respObj.put("error", "There was an Error in the Join Clause.");
                                    cdb.close();
                                    return respObj;
                                }
                            } else {
                                // There are no join clauses.
                            }
                        } catch (Exception e) {
                            // There was Error.
                            respObj.put("error", "Join Parsing");
                            cdb.close();
                            return respObj;
                        }
                        // Add any where clauses if required.
                        try {
                            // Check if there are any where clauses.
                            if (request.getParameterMap().containsKey("wheres")) {
                                // There is a where clause.
                                JSONObject whereJObj = (JSONObject) new JSONParser().parse(request.getParameter("wheres"));
                                select = setupWheres(select, whereJObj, cdb.isNormalUser() ? 1 : 0);
                                // Check if there was an Error in the Where Section.
                                if (select == null) {
                                    // There was an Error in the Where Section.
                                    respObj.put("error", "There was an Error in the Where Clause.");
                                    cdb.close();
                                    return respObj;
                                }
                            } else {
                                // There is no where clause.
                                // Add nothing to the update statment. (The Query might Error Out).
                            }
                        } catch (Exception e) {
                            // There was an Error in getting and parsing the Where Clauses.
                            respObj.put("error", "Where Parsing");
                            cdb.close();
                            return respObj;
                        }

                        // Add the Order by functionality if user has asked for it.
                        try {
                            // Here we will add the order by functionality if the user has asked for it.
                            if (request.getParameterMap().containsKey("order_by")) {
                                // The user wants the data to be ordered by functionality.
                                try {
                                    JSONArray orderByJArr = (JSONArray) new JSONParser().parse(request.getParameter("order_by"));
                                    for (int i = 0; i < orderByJArr.size(); i++) {
                                        // Check if it is a json object or normal string.
                                        try {
                                            // Lets Check if it is in ASC / DESC.
                                            JSONObject orderColJObj = (JSONObject) new JSONParser().parse(orderByJArr.get(i).toString());
                                            String colName = orderColJObj.keySet().iterator().next().toString();
                                            select.addOrderBy(colName, orderColJObj.get(colName).toString());
                                        } catch (ParseException pe) {
                                            // There was a Parsing Error.
                                            select.addOrderBy(orderByJArr.get(i).toString());
                                        }
                                    }
                                } catch (ParseException pe) {
                                    // There was a Parse Error.
                                    // If it was a JSON Parse error then we will get that one param.

                                }
                            }
                        } catch (Exception e) {
                            // There was an Error.
                        }

                        if (select != null) {
                            if (select.isSuccessful()) {
                                // Let's execute the update.
                                // The Select is Safe.
                                // Get the Prepared Statement.
                                PreparedStatement prepStmt = select.getPreparedStatement();
                                // Execute the query according to the result requested.
                                // The user wants data to be returned by the query.
                                ResultSet result = prepStmt.executeQuery();
                                // Check if the write is successful or not.
                                if (result.isBeforeFirst()) {
                                    // The write was successful.
                                    // There is a Resultset to be Read, as the user wants some data to be returned.
                                    Result res = new Result(result);
                                    respObj.put("success", res.getJSONResult());
                                } else {
                                    // The write was not successful.
                                    respObj.put("success", "No Rows Found");
                                }
                                cdb.close();
                                return respObj;
                            } else {
                                // The Select is Errored.
                                respObj.put("error", "Error : " + select.getStatus());
                                cdb.close();
                                return respObj;
                            }
                        } else {
                            // The update is null.
                            respObj.put("error", "Select is Null");
                            cdb.close();
                            return respObj;
                        }
                    } catch (Exception e) {
                        // There was an Error.
                        respObj.put("error", "Unauthorized Err : " + Helper.Error.getErrorMessage(e));
                        cdb.close();
                        return respObj;
                    }
                } else {
                    // User is not Authenticated.
                    respObj.put("error", "User Not Authenticated");
                    respObj.put("status", cdb.getStatusTrace());
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
            // Set up the user and cloud db object for interfacing with the database.
            CloudDB cdb = new CloudDB(token);
            // Proceed with Inserting the Data from the database as requested by the user.
            if (cdb == null) {
                // There was an Error, and the Cloud DB Connection is null.
                respObj.put("error", "User Not Authenticated");
                cdb.close();
                return respObj;
            } else {
                // The Cloud DB connection was successful.
                if (cdb.isAuthenticated()) {
                    // The user is authenticated also.
                    // Let's get the data required.
                    try {
                        // Create the Insert Query.
                        Insert insert = new Insert(cdb)
                                            .into(request.getParameter("table_name"));
                        // Add the Tenancy Column to the insert if normal user.
                        try {
                            // Add the Tenancy Column.
                            if (cdb.isServerUser() && request.getParameterMap().containsKey("tenant_id")) {
                                insert.setTenantID(Long.parseLong(request.getParameter("tenant_id")));
                            }
                        } catch (Exception e) {
                            // There was an Error.
                            respObj.put("error", "Multi-Tenancy Error : " + Helper.Error.getErrorMessage(e));
                            cdb.close();
                            return respObj;
                        }
                        try {
                            // Add the Update time if the update time is given.
                            if (request.getParameterMap().containsKey("update_time_")) {
                                // There is an update time.
                                insert.setUpdateTime(request.getParameter("update_time_"));
                            }
                        } catch (Exception e) {
                            // There was an Error.
                        }

                        // Add the RLS if the User Has provided.
                        try {
                            // Add the RLS.
                            if (request.getParameterMap().containsKey("rls_id_")
                                                && request.getParameterMap().containsKey("rls_type_")) {
                                // The Request Contains RLS details.
                                if (Short.parseShort(request.getParameter("rls_type_")) == 1) {
                                    // The Row is to be inserted using Role Based RLS.
                                    insert.setRoleID(Long.parseLong(request.getParameter("rls_id_")));
                                } else if (Short.parseShort(request.getParameter("rls_type_")) == 2) {
                                    // The Row is to be inserted using Role Based RLS.
                                    insert.setGroupID(Long.parseLong(request.getParameter("rls_id_")));
                                }
                            }
                        } catch (Exception e) {
                            // There was an Error.
                        }

                        // Add the Columns and their data.
                        try {
                            // Parse the columns from the Request.
                            JSONObject colJObj = (JSONObject) new JSONParser().parse(request.getParameter("columns"));
                            // Get all the Columns along with their data.
                            Object[] colNameArr = colJObj.keySet().toArray();
                            for (Object colName : colNameArr) {
                                try {
                                    // Add the Column into the Insert query.
                                    insert.putColumn(new ColumnData(colName.toString(), colJObj.get(colName.toString())));
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        } catch (Exception e) {
                            // There was an Error.
                            respObj.put("error", "Column Data Parsing");
                            cdb.close();
                            return respObj;
                        }
                        // Set the Sync ID if it exists.
                        try {
                            // Here we will add the Sync ID if it exists.
                            if (request.getParameterMap().containsKey("sync_id")) {
                                // There is a sync id sent from the user.
                                insert.setSyncID(request.getParameter("sync_id"));
                            }
                        } catch (Exception e) {
                            // There was an Error.
                        }
                        boolean dataReturned = false;
                        boolean userNeedsDataReturn = false;
                        try {
                            // Check if there is any Returning Columns that the user wants.
                            // Check if this table is a syncable table.
                            if (cdb.isSyncable(request.getParameter("table_name"))) {
                                // Check if the User requires data to be returned.
                                dataReturned = true;
                                insert.addReturningColumn("*");
                                userNeedsDataReturn = request.getParameterMap().containsKey("returning_columns");
                            } else if (request.getParameterMap().containsKey("returning_columns")) {
                                // The user wants data to be returned.
                                dataReturned = false;
                                // Parse the returning columns from the Request.
                                JSONArray colJArr = (JSONArray) new JSONParser().parse(request.getParameter("returning_columns"));
                                // Get all the returning Columns along with their data.
                                if (colJArr.isEmpty() || colJArr.size() == 0) {
                                    // There are no returing columns.
                                    userNeedsDataReturn = false;
                                } else {
                                    // The Request has specified a few returning columns.
                                    for (int colCount = 0; colCount < colJArr.size(); colCount++) {
                                        try {
                                            // Add the returning Column into the Insert query.
                                            insert.addReturningColumn(colJArr.get(colCount).toString());
                                            userNeedsDataReturn = true;
                                            dataReturned = true;
                                        } catch (Exception e) {
                                            // There was an Error.
                                        }
                                    }
                                }
                            } else {
                                // The user doen'st want any returning query.
                                dataReturned = false;
                            }
                        } catch (Exception e) {
                            // There was an Error.
                            respObj.put("error", "Returning Column Data Parsing");
                            cdb.close();
                            return respObj;
                        }
                        try {
                            if (insert.isSuccessful()) {
                                // Get the Prepared Statement.
                                PreparedStatement prepStmt = insert.getPreparedStatement();
                                // Execute the query according to the result requested.
                                if (dataReturned) {
                                    // The user wants data to be returned by the query.
                                    ResultSet result = prepStmt.executeQuery();
                                    // Check if the write is successful or not.
                                    if (result.isBeforeFirst()) {
                                        // The write was successful.
                                        // There is a Resultset to be Read, as the user wants some data to be returned.
                                        Result res = new Result(result);
                                        JSONArray jarr = res.getJSONResult();
                                        if (userNeedsDataReturn) {
                                            // The user wants data to be returned.
                                            respObj.put("success", jarr);
                                        } else {
                                            // The user does not want data to be returned.
                                            respObj.put("success", "Insert Successful");
                                        }
                                        try {
                                            // Lets Extract the data to be pushed to the devices.
                                            if (cdb.isSyncable(request.getParameter("table_name"))) {
                                                final JSONObject syncJObj = new JSONObject();
                                                syncJObj.put("columns", jarr.get(0));
                                                syncJObj.put("sync_id_", ((JSONObject) jarr.get(0)).get("sync_id_").toString());
                                                syncJObj.put("type", "WRITE");
                                                syncJObj.put("table_name", request.getParameter("table_name"));
                                                try {
                                                    // Now lets sync the data with all the other devices & other users.
                                                    // Sync the data with the users.
                                                    final short rlsType = insert.getRLSType();
                                                    final long rlsID = insert.getRLSID();
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                // Send the Data to the Related Devcies.
                                                                Sync syncer = new Sync(null);
                                                                syncer.push().sendDataWithRLS(syncJObj, rlsType, rlsID);
                                                            } catch (Exception e) {
                                                                // There was an Error.
                                                            }
                                                        }
                                                    }).start();
                                                } catch (Exception e) {
                                                    // There was an Error.
                                                }
                                            }
                                        } catch (Exception e) {
                                            // There was an Error.
                                        }
                                    } else {
                                        // The write was not successful.
                                        respObj.put("error", "No Rows Inserted");
                                    }
                                    cdb.close();
                                    return respObj;
                                } else {
                                    // The user does'nt want any return value.
                                    int resultInt = prepStmt.executeUpdate();
                                    respObj.put(resultInt > 0 ? "success" : "error",
                                                        resultInt > 0 ? "Insert Successful" : "No Rows Inserted");
                                    cdb.close();
                                    return respObj;
                                }
                            } else {
                                // There was an error in the insert statement.
                                respObj.put("error", "Insert Error : " + insert.getStatus());
                                cdb.close();
                                return respObj;
                            }
                        } catch (Exception e) {
                            // There was an Error.
                            respObj.put("error", "Insert Error : " + Helper.Error.getErrorMessage(e));
                            cdb.close();
                            return respObj;
                        }
                    } catch (Exception e) {
                        // There was an Error.
                        respObj.put("error", "User is Unauthorized " + Helper.Error.getErrorMessage(e));
                        cdb.close();
                        return respObj;
                    }
                } else {
                    // User is not Authenticated.
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
            // Set up the user and cloud db object for interfacing with the database.
            CloudDB cdb = new CloudDB(token);
            // Proceed with Inserting the Data from the database as requested by the user.
            if (cdb == null) {
                // There was an Error, and the Cloud DB Connection is null.
                respObj.put("error", "User Not Authenticated");
                cdb.close();
                return respObj;
            } else {
                // The Cloud DB connection was successful.
                if (cdb.isAuthenticated()) {
                    // The user is authenticated also.
                    // Let's get the data required.
                    try {
                        // Create the Update Query.
                        // Setup with the required table.
                        Update update = new Update(cdb)
                                            .into(request.getParameter("table_name"));
                        // Provide the Columns and their data.
                        // Here we extract the columns from the JSON Object.
                        try {
                            // Parse the columns from the Request.
                            try {
                                // Add the Update time if the update time is given.
                                if (request.getParameterMap().containsKey("update_time_")) {
                                    // There is an update time.
                                    update.setUpdateTime(Long.parseLong(request.getParameter("update_time_")));
                                }
                            } catch (Exception e) {
                                // There was an Error.
                            }

                            // Lets Extract the Columns and add it to the update.
                            JSONObject columnsJObj = (JSONObject) new JSONParser().parse(request.getParameter("columns"));
                            // Get all the Columns along with their data.
                            Object[] colNameArr = columnsJObj.keySet().toArray();
                            for (Object colNameArr1 : colNameArr) {
                                try {
                                    // Add the Column into the Insert query.
                                    String columnName = colNameArr1.toString();
                                    JSONObject colJObj = (JSONObject) columnsJObj.get(columnName);
                                    switch (Update.T.valueOf(colJObj.get("type").toString())) {
                                        case VALUE:
                                            update.addColumn(columnName, colJObj.get("value"));
                                            break;
                                        case COLUMN:
                                            update.addColumn(columnName,
                                                                colJObj.get("column").toString().replaceAll(" ", ""));
                                            break;
                                        case CONCAT:
                                            update.addColumn(columnName,
                                                                colJObj.get("column").toString().replaceAll(" ", ""),
                                                                colJObj.get("value").toString().replaceAll(" ", ""),
                                                                Boolean.parseBoolean(colJObj.get("is_after_column").toString()));
                                            break;
                                        case OPERATION:
                                            // Here we check if its an integer or a floating point.
                                            boolean isInteger = true;
                                            try {
                                                // Here we will check the type.
                                                String[] columnValueArr = colJObj.get("value").toString().split(".");
                                                if (columnValueArr.length > 1) {
                                                    // It may be a floating point.
                                                    int colDecimalValue = Integer.parseInt(columnValueArr[1]);
                                                    isInteger = colDecimalValue == 0;
                                                } else {
                                                    // Its definitely an integer.
                                                    isInteger = true;
                                                }
                                            } catch (NumberFormatException er) {
                                                // There was an Error.
                                                // Probably its a floating point.
                                                isInteger = false;
                                            }

                                            // Here we will set the data into the database accordingly.
                                            if (isInteger) {
                                                // The column value is a Integer value.
                                                switch (Update.O.valueOf(colJObj.get("code").toString())) {
                                                    case ADD:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.ADD,
                                                                            Long.parseLong(colJObj.get("value").toString()));
                                                        break;
                                                    case SUBSCRACT:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.SUBSCRACT,
                                                                            Long.parseLong(colJObj.get("value").toString()));
                                                        break;
                                                    case MULTIPLY:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.MULTIPLY,
                                                                            Long.parseLong(colJObj.get("value").toString()));
                                                        break;
                                                    case DIVIDE:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.DIVIDE,
                                                                            Long.parseLong(colJObj.get("value").toString()));
                                                        break;
                                                    case MODULAR:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.MODULAR,
                                                                            Long.parseLong(colJObj.get("value").toString()));
                                                        break;
                                                    default:
                                                        cdb.close();
                                                        return null;

                                                }
                                            } else {
                                                // The Column Value is a Floating Value.
                                                switch (Update.O.valueOf(colJObj.get("code").toString())) {
                                                    case ADD:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.ADD,
                                                                            Double.parseDouble(colJObj.get("value").toString()));
                                                        break;
                                                    case SUBSCRACT:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.SUBSCRACT,
                                                                            Double.parseDouble(colJObj.get("value").toString()));
                                                        break;
                                                    case MULTIPLY:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.MULTIPLY,
                                                                            Double.parseDouble(colJObj.get("value").toString()));
                                                        break;
                                                    case DIVIDE:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.DIVIDE,
                                                                            Double.parseDouble(colJObj.get("value").toString()));
                                                        break;
                                                    case MODULAR:
                                                        update.addColumn(columnName,
                                                                            colJObj.get("column").toString().replaceAll(" ", ""),
                                                                            Update.O.MODULAR,
                                                                            Double.parseDouble(colJObj.get("value").toString()));
                                                        break;
                                                    default:
                                                        cdb.close();
                                                        return null;

                                                }
                                            }
                                            break;
                                        default:
                                            cdb.close();
                                            return null;
                                    }
                                } catch (Exception e) {
                                    // There was an Error.
                                    respObj.put("error", "Column Parsing : " + Helper.Error.getErrorMessage(e));
                                    cdb.close();
                                    return respObj;
                                }
                            }
                        } catch (Exception e) {
                            // There was an Error.
                            respObj.put("error", "Column Data Parsing");
                            cdb.close();
                            return respObj;
                        }

                        // Add any where clauses if required.
                        try {
                            // Check if there are any where clauses.
                            if (request.getParameterMap().containsKey("wheres")) {
                                // There is a where clause.
                                JSONObject whereJObj = (JSONObject) new JSONParser().parse(request.getParameter("wheres"));
                                update = setupWheres(update, whereJObj, cdb.isNormalUser() ? 1 : 0);
                                // Check if there was an Error in the Where Section.
                                if (update == null) {
                                    // There was an Error in the Where Section.
                                    respObj.put("error", "There was an Error in the Where Clause.");
                                    cdb.close();
                                    return respObj;
                                }
                            } else {
                                // There is no where clause.
                                // Add nothing to the update statment. (The Query might Error Out).
                            }
                        } catch (Exception e) {
                            // There was an Error in getting and parsing the Where Clauses.
                            respObj.put("error", "Where Parsing");
                            cdb.close();
                            return respObj;
                        }

                        // Add the update time.
                        // Create the Update system.
                        if (update != null) {
                            boolean dataReturned = false;
                            boolean userNeedsDataReturn = false;
                            try {
                                // Check if there is any Returning Columns that the user wants.
                                if (request.getParameterMap().containsKey("returning_columns")) {
                                    // The table is not a syncable table but the user wants a returning column.
                                    // Parse the returning columns from the Request.
                                    JSONArray colJArr = (JSONArray) new JSONParser().parse(request.getParameter("returning_columns"));
                                    // Get all the returning Columns along with their data.
                                    if (colJArr.isEmpty() || colJArr.size() == 0) {
                                        // There are no returing columns.
                                        userNeedsDataReturn = false;
                                    } else {
                                        // The Request has specified a few returning columns.
                                        for (int colCount = 0; colCount < colJArr.size(); colCount++) {
                                            try {
                                                // Add the returning Column into the Insert query.
                                                update.addReturningColumn(colJArr.get(colCount).toString());
                                                userNeedsDataReturn = true;
                                                dataReturned = true;
                                            } catch (Exception e) {
                                                // There was an Error.
                                            }
                                        }
                                    }
                                }

                                // Set the syncability of the table.
                                if (cdb.isSyncable(request.getParameter("table_name"))) {
                                    // The table is syncable, but the user doe'snt want any return.
                                    dataReturned = true;

                                    try {
                                        // Add the Update time of the update.
                                        if (request.getParameterMap().containsKey("update_time_")) {
                                            // The update time exists.
                                            update.setUpdateTime(Long.parseLong(request.getParameter("update_time_")));
                                        }
                                    } catch (Exception e) {
                                        // There was an error
                                    }

                                    // Add the Returning columns.
                                    update.addReturningColumn("rls_id_");
                                    update.addReturningColumn("rls_type_");
                                    update.addReturningColumn("sync_id_");
                                    update.addReturningColumn("update_time_");
                                }
                            } catch (Exception e) {
                                // There was an Error.
                                respObj.put("error", "Returning Column Data Parsing");
                                cdb.close();
                                return respObj;
                            }
                            // Validate if we can proceed.
                            if (update.isSuccessful()) {
                                // Update was successful.
                                // Let's execute the update.
                                // Get the Prepared Statement.
                                PreparedStatement prepStmt = update.getPreparedStatement();
                                // Execute the query according to the result requested.
                                if (dataReturned) {
                                    // The user wants data to be returned by the query.
                                    ResultSet result = prepStmt.executeQuery();
                                    // Check if the write is successful or not.
                                    if (result.isBeforeFirst()) {
                                        // The write was successful.
                                        // There is a Resultset to be Read, as the user wants some data to be returned.
                                        Result res = new Result(result);
                                        final JSONArray resJArr = res.getJSONResult();
                                        if (userNeedsDataReturn) {
                                            // The user needs to be sent the returned data.
                                            respObj.put("success", resJArr);
                                        } else {
                                            // The user does'nt not need to be sent the returned data.
                                            respObj.put("success", "Update Successful");
                                        }
                                        try {
                                            // Lets send the data to the devices.
                                            if (cdb.isSyncable(request.getParameter("table_name"))) {
                                                // Lets sync the update with all the devices & users to be updated.
                                                final String tableName = request.getParameter("table_name");
                                                final JSONObject colJObj = ((JSONObject) new JSONParser().parse(request.getParameter("columns")));
                                                final JSONObject whereJObj = ((JSONObject) new JSONParser().parse(request.getParameter("wheres")));
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            // Lets send the Data to the RLS.
                                                            for (int rowIndex = 0; rowIndex < resJArr.size(); rowIndex++) {
                                                                try {
                                                                    // Get the Row JSON.
                                                                    JSONObject syncJObj = new JSONObject();
                                                                    JSONObject rowJObj = (JSONObject) resJArr.get(rowIndex);
                                                                    syncJObj.put("type", "UPDATE");
                                                                    syncJObj.put("table_name", tableName);
                                                                    syncJObj.put("columns", colJObj);
                                                                    syncJObj.put("wheres", whereJObj);
                                                                    syncJObj.put("update_time", rowJObj.get("update_time_").toString());
                                                                    // Here lets extract the RLSs that we have to send the data to.
                                                                    syncJObj.put("sync_id_", rowJObj.get("sync_id_").toString());
                                                                    // Send the data to the devices using the syncer system.
                                                                    try {
                                                                        // Now lets sync the data with all the other devices & other users.
                                                                        // Sync the data with the users.
                                                                        Sync syncer = new Sync(null);
                                                                        syncer.push().sendDataWithRLS(syncJObj,
                                                                                            Short.parseShort(rowJObj.get("rls_type_").toString()),
                                                                                            Long.parseLong(rowJObj.get("rls_id_").toString()));
                                                                    } catch (Exception e) {
                                                                        // There was an Error.
                                                                    }
                                                                } catch (Exception e) {
                                                                    // There was an Error.
                                                                }
                                                            }
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
                                        // The write was not successful.
                                        respObj.put("error", "No Rows Updated");
                                    }
                                    cdb.close();
                                    return respObj;
                                } else {
                                    // The user does'nt want any return value & also this does'nt need to be synced.
                                    int resultInt = prepStmt.executeUpdate();
                                    respObj.put(resultInt > 0 ? "success" : "error",
                                                        resultInt > 0 ? "Update Successful" : "No Rows Updated");
                                    cdb.close();
                                    return respObj;
                                }
                            } else {
                                // The update has an Error.
                                respObj.put("error", "Update Error : " + update.getStatus());
                                cdb.close();
                                return respObj;
                            }
                        } else {
                            // The update is null.
                            respObj.put("error", "Update is Null");
                            cdb.close();
                            return respObj;
                        }
                    } catch (Exception e) {
                        // There was an Error.
                        respObj.put("error", "Unauthorized Err : " + Helper.Error.getErrorMessage(e));
                        cdb.close();
                        return respObj;
                    }
                } else {
                    // User is not Authenticated.
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
            // Set up the user and cloud db object for interfacing with the database.
            CloudDB cdb = new CloudDB(token);
            // Proceed with Inserting the Data from the database as requested by the user.
            if (cdb == null) {
                // There was an Error, and the Cloud DB Connection is null.
                respObj.put("error", "User Not Authenticated");
                cdb.close();
                return respObj;
            } else {
                // The Cloud DB connection was successful.
                if (cdb.isAuthenticated()) {
                    // The user is authenticated also.
                    // Let's get the data required.
                    try {
                        // Create the Update Query.
                        // Setup with the required table.
                        Delete delete = new Delete(cdb)
                                            .from(request.getParameter("table_name"));

                        try {
                            // Add the RLS Security Where Clause if exists.
                            if (request.getParameterMap().containsKey("rls_id")
                                                && request.getParameterMap().containsKey("rls_type")) {
                                // Add the RLS Security to the Delete.
                                switch (Short.parseShort(request.getParameter("rls_type"))) {
                                    case 1:
                                        // The RLS Type is Role Based.
                                        delete.setRoleID(Long.parseLong(request.getParameter("rls_id")));
                                        break;
                                    case 2:
                                        // The RLS Type is Role Based.
                                        delete.setGroupID(Long.parseLong(request.getParameter("rls_id")));
                                        break;
                                    default:
                                        // There was an Error in the RLS.
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            // There was an Error.
                            respObj.put("error", "Security Error : " + Helper.Error.getErrorMessage(e));
                            cdb.close();
                            return respObj;
                        }

                        // Add any where clauses if required.
                        try {
                            // Check if there are any where clauses.
                            if (request.getParameterMap().containsKey("wheres")) {
                                // There is a where clause.
                                JSONObject whereJObj = (JSONObject) new JSONParser().parse(request.getParameter("wheres"));
                                delete = setupWheres(delete, whereJObj, 0);
                                // Check if there was an Error in the Where Section.
                                if (delete == null) {
                                    // There was an Error in the Where Section.
                                    respObj.put("error", "Where Clause.");
                                    cdb.close();
                                    return respObj;
                                }
                            } else {
                                // There is no where clause.
                                // Add nothing to the Delete statment. (The Query might Error Out).
                                respObj.put("error", "All Delete Not Allowed");
                                cdb.close();
                                return respObj;
                            }
                        } catch (Exception e) {
                            // There was an Error in getting and parsing the Where Clauses.
                            respObj.put("error", "Where Parsing");
                            cdb.close();
                            return respObj;
                        }

                        if (delete != null) {
                            boolean dataReturned = false;
                            boolean userNeedsDataReturn = false;
                            try {
                                // Check if there is any Returning Columns that the user wants.
                                if (request.getParameterMap().containsKey("returning_columns")) {
                                    // The table is not a syncable table but the user wants a returning column.
                                    // Parse the returning columns from the Request.
                                    JSONArray colJArr = (JSONArray) new JSONParser().parse(request.getParameter("returning_columns"));
                                    // Get all the returning Columns along with their data.
                                    if (colJArr.isEmpty() || colJArr.size() == 0) {
                                        // There are no returing columns.
                                        userNeedsDataReturn = false;
                                    } else {
                                        // The Request has specified a few returning columns.
                                        for (int colCount = 0; colCount < colJArr.size(); colCount++) {
                                            try {
                                                // Add the returning Column into the Insert query.
                                                delete.addReturningColumn(colJArr.get(colCount).toString());
                                                userNeedsDataReturn = true;
                                                dataReturned = true;
                                            } catch (Exception e) {
                                                // There was an Error.
                                            }
                                        }
                                    }
                                }

                                // Set the syncability of the table.
                                if (cdb.isSyncable(request.getParameter("table_name"))) {
                                    // The table is syncable, but the user doe'snt want any return.
                                    dataReturned = true;
                                    delete.addReturningColumn("rls_id_");
                                    delete.addReturningColumn("rls_type_");
                                    delete.addReturningColumn("sync_id_");
                                }
                            } catch (Exception e) {
                                // There was an Error.
                                respObj.put("error", "Returning Column Data Parsing");
                                cdb.close();
                                return respObj;
                            }

                            // Let's execute the delete.
                            // Execute the query according to the result requested.
                            // The user does'nt want any return value.
                            if (delete.isSuccessful()) {
                                try {
                                    // The delete statement is successful.
                                    if (dataReturned) {
                                        // The data is being returned from the database.
                                        ResultSet result = delete.getPreparedStatement().executeQuery();
                                        // Check if the write is successful or not.
                                        if (result.isBeforeFirst()) {
                                            // The write was successful.
                                            // There is a Resultset to be Read, as the user wants some data to be returned.
                                            Result res = new Result(result);
                                            final JSONArray resJArr = res.getJSONResult();
                                            if (userNeedsDataReturn) {
                                                // The user needs to be sent the returned data.
                                                respObj.put("success", resJArr);
                                            } else {
                                                // The user does'nt not need to be sent the returned data.
                                                respObj.put("success", "Deleted Successfully");
                                            }

                                            try {
                                                // Lets send the data to the devices.
                                                if (cdb.isSyncable(request.getParameter("table_name"))) {
                                                    // Lets sync the update with all the devices & users to be updated.
                                                    final String tableName = request.getParameter("table_name");
                                                    final JSONObject whereJObj = ((JSONObject) new JSONParser().parse(request.getParameter("wheres")));
                                                    // Send the Data according to the RLS.
                                                    if (request.getParameterMap().containsKey(("delete_time_"))) {
                                                        deletedTimeI = Long.parseLong(request.getParameter("delete_time_"));
                                                    }

                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                // Start The Syncing.
                                                                for (int rowIndex = 0; rowIndex < resJArr.size(); rowIndex++) {
                                                                    try {
                                                                        // Get the Row JSON.
                                                                        JSONObject syncJObj = new JSONObject();
                                                                        syncJObj.put("type", "DELETE");
                                                                        syncJObj.put("table_name", tableName);
                                                                        syncJObj.put("wheres", whereJObj);
                                                                        JSONObject rowJObj = (JSONObject) resJArr.get(rowIndex);
                                                                        // Here lets extract the RLSs that we have to send the data to.
                                                                        syncJObj.put("sync_id_", rowJObj.get("sync_id_").toString());

                                                                        // Add the Delete Time.
                                                                        if (deletedTimeI != 0) {
                                                                            syncJObj.put("delete_time", deletedTimeI);
                                                                        }

                                                                        // Send the data to the devices using the syncer system.
                                                                        // Now lets sync the data with all the other devices & other users.
                                                                        // Sync the data with the users.
                                                                        new Sync(null).push().sendDataWithRLS(syncJObj,
                                                                                            Short.parseShort(rowJObj.get("rls_type_").toString()),
                                                                                            Long.parseLong(rowJObj.get("rls_id_").toString()));
                                                                    } catch (Exception e) {
                                                                        // There was an Error.
                                                                    }

                                                                }
                                                            } catch (Exception e) {
                                                                // THere was an Error
                                                            }
                                                        }
                                                    }).start();

                                                }
                                            } catch (Exception e) {
                                                // There was an Error.
                                            }
                                        } else {
                                            // The write was not successful.
                                            respObj.put("success", "No Rows Deleted");
                                        }
                                    } else {
                                        // The data is not being returned from the database.
                                        int deleteInt = delete.getPreparedStatement().executeUpdate();
                                        respObj.put("success",
                                                            deleteInt > 0 ? "Deleted Successfully" : "No Rows Deleted");
                                    }
                                    cdb.close();
                                    return respObj;
                                } catch (Exception e) {
                                    // There was an Error.
                                    respObj.put("error", Helper.Error.getErrorMessage(e)
                                                        + "  ::::  Delete Err : " + delete.getStatus());
                                    cdb.close();
                                    return respObj;
                                }
                            } else {
                                // The Delete was Unsuccesful.
                                respObj.put("error", "Delete Error : " + delete.getStatus());
                                cdb.close();
                                return respObj;
                            }
                        } else {
                            // The update is null.
                            respObj.put("error", "Delete is Null");
                            cdb.close();
                            return respObj;
                        }
                    } catch (Exception e) {
                        // There was an Error.
                        respObj.put("error", "Unauthorized Err : " + Helper.Error.getErrorMessage(e));
                        cdb.close();
                        return respObj;
                    }
                } else {
                    // User is not Authenticated.
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

    // These are the Helper Methods for this class.
    // Set up the where for the select.
    private Select setupWheres(Select select, JSONObject whereJObj, int type) {
        try {
            // Here we will extract the Where clause and add it to the update.
            switch (type) {
                case 0: // This means it is of the neutral type, the first call.
                    select.addWhere(new Where(whereJObj.get("name").toString(),
                                        Where.Type.valueOf(whereJObj.get("condition").toString()),
                                        whereJObj.get("data")));
                    break;
                case 1: // This means it is of the AND Type.
                    select.addWhere(new Where(true, whereJObj.get("name").toString(),
                                        Where.Type.valueOf(whereJObj.get("condition").toString()),
                                        whereJObj.get("data")));
                    break;
                case 2: // This means it is of the OR Type.
                    select.addWhere(new Where(false, whereJObj.get("name").toString(),
                                        Where.Type.valueOf(whereJObj.get("condition").toString()),
                                        whereJObj.get("data")));
                    break;
            }
            // Check if there are any more where clauses, attached.
            if (whereJObj.containsKey("and")) {
                // There is another where which is connected to as an AND.
                select = setupWheres(select, (JSONObject) whereJObj.get("and"), 1);
            } else if (whereJObj.containsKey("or")) {
                // There is another where which is connected to as an OR.
                select = setupWheres(select, (JSONObject) whereJObj.get("or"), 2);
            }
            // Return the New Update Statement.
            return select;
        } catch (Exception e) {
            // There was an Error.
            status = "Error : " + Helper.Error.getErrorMessage(e);
            return null;
        }
    }

    // Set up the where for the update.
    private Update setupWheres(Update update, JSONObject whereJObj, int type) {
        try {
            // Here we will extract the Where clause and add it to the update.
            switch (type) {
                case 0: // This means it is of the neutral type, the first call.
                    update.addWhere(new Where(whereJObj.get("name").toString(),
                                        Where.Type.valueOf(whereJObj.get("condition").toString()),
                                        whereJObj.get("data")));
                    break;
                case 1: // This means it is of the AND Type.
                    update.addWhere(new Where(true, whereJObj.get("name").toString(),
                                        Where.Type.valueOf(whereJObj.get("condition").toString()),
                                        whereJObj.get("data")));
                    break;
                case 2: // This means it is of the AND Type.
                    update.addWhere(new Where(false, whereJObj.get("name").toString(),
                                        Where.Type.valueOf(whereJObj.get("condition").toString()),
                                        whereJObj.get("data")));
                    break;
            }
            // Check if there are any more where clauses, attached.
            if (whereJObj.containsKey("and")) {
                // There is another where which is connected to as an AND.
                update = setupWheres(update, (JSONObject) whereJObj.get("and"), 1);
            } else if (whereJObj.containsKey("or")) {
                // There is another where which is connected to as an OR.
                update = setupWheres(update, (JSONObject) whereJObj.get("or"), 2);
            }
            // Return the New Update Statement.
            return update;
        } catch (Exception e) {
            // There was an Error.
            status = "Error : " + Helper.Error.getErrorMessage(e);
            return null;
        }
    }

    // Set up the where for the delete
    private Delete setupWheres(Delete delete, JSONObject whereJObj, int type) {
        try {
            // Here we will extract the Where clause and add it to the update.
            switch (type) {
                case 0: // This means it is of the neutral type, the first call.
                    delete.addWhere(new Where(whereJObj.get("name").toString(),
                                        Where.Type.valueOf(whereJObj.get("condition").toString()),
                                        whereJObj.get("data")));
                    break;
                case 1: // This means it is of the AND Type.
                    delete.addWhere(new Where(true, whereJObj.get("name").toString(),
                                        Where.Type.valueOf(whereJObj.get("condition").toString()),
                                        whereJObj.get("data")));
                    break;
                case 2: // This means it is of the OR Type.
                    delete.addWhere(new Where(false, whereJObj.get("name").toString(),
                                        Where.Type.valueOf(whereJObj.get("condition").toString()),
                                        whereJObj.get("data")));
                    break;
            }
            // Check if there are any more where clauses, attached.
            if (whereJObj.containsKey("and")) {
                // There is another where which is connected to as an AND.
                delete = setupWheres(delete, (JSONObject) whereJObj.get("and"), 1);
            } else if (whereJObj.containsKey("or")) {
                // There is another where which is connected to as an OR.
                delete = setupWheres(delete, (JSONObject) whereJObj.get("or"), 2);
            }
            // Return the New Update Statement.
            return delete;
        } catch (Exception e) {
            // There was an Error.
            status = "Error : " + Helper.Error.getErrorMessage(e);
            return null;
        }
    }

    // Set up the Join for the Select.
    /*
    {
        "type": "FULL JOIN",
        "name": "accounts",
        "column_name": "id",
        "join_with_column_name": "id"
    }
     */
    private Select setupJoinsV1(Select select, JSONObject joinJObj) {
        try {
            // Lets Join the Statements.
            String sqlType = Join.Type.JOIN;
            switch (joinJObj.get("type").toString()) {
                case "JOIN":
                    sqlType = Join.Type.JOIN;
                    break;
                case "INNER JOIN":
                    sqlType = Join.Type.INNER_JOIN;
                    break;
                case "LEFT JOIN":
                    sqlType = Join.Type.LEFT_JOIN;
                    break;
                case "RIGHT JOIN":
                    sqlType = Join.Type.RIGHT_JOIN;
                    break;
                case "OUTTER JOIN":
                    sqlType = Join.Type.OUTTER_JOIN;
                    break;
                case "FULL JOIN":
                    sqlType = Join.Type.FULL_JOIN;
                    break;
                default:
                    sqlType = Join.Type.JOIN;
            }
            if (tablesBeingCalled.contains(joinJObj.get("name").toString())) {
                tablesBeingCalled.add(joinJObj.get("name").toString());
            }
            if (tablesBeingCalled.contains(joinJObj.get("join_with_table").toString())) {
                tablesBeingCalled.add(joinJObj.get("join_with_table").toString());
            }
            joinWithTable = joinJObj.get("join_with_table").toString();
            select.addJoin(new Join(sqlType, joinJObj.get("join_with_table").toString(),
                                joinJObj.get("name").toString() + '.' + joinJObj.get("column_name").toString(),
                                joinWithTable + '.' + joinJObj.get("join_with_column_name").toString()));
            // Check if there are more joins.
            if (joinJObj.containsKey("join")) {
                // There are more Joins.
                joinWithTable = joinJObj.get("name").toString();
                select = setupJoinsV1(select, (JSONObject) joinJObj.get("join"));
            }
            return select;
        } catch (Exception e) {
            // There was an Error.
            return null;
        }
    }

}
