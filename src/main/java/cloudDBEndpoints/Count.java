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
import cloudDB.Join;
import cloudDB.Select;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author karan
 */
public class Count extends HttpServlet {

    // Variables of the class.
    private String status = "";
    private String joinWithTable;
    private List<String> tablesBeingCalled;
    
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
			// Create the Update Query.
			// Setup with the required table.
			Select select = new Select(cdb);
			// Set the Select to get the Count of the Tables.
			select.count(null);
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
				select = setupJoins(select, ((JSONObject) new JSONParser().parse(request.getParameter("joins"))));
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
				    result.next();
				    respObj.put("success", result.getObject(1));
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
		    respObj.put("error", "Post Not Supported in this API.");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    respObj.put("error", "Post Not Supported in this API.");
		    cdb.close();
		    return respObj;
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "Post Not Supported in this API.");
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
		    respObj.put("error", "Put Not Supported in this API.");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    respObj.put("error", "Put Not Supported in this API.");
		    cdb.close();
		    return respObj;
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "Put Not Supported in this API.");
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
		    respObj.put("error", "Delete Not Supported in this API.");
		cdb.close();
		return respObj;
	    } else {
		if (cdb.isAuthenticated()) {
		    // The Cloud DB connection was successful.
		    // The user is authenticated also.
		    // Let's get the data required.
		    respObj.put("error", "Delete Not Supported in this API.");
		    cdb.close();
		    return respObj;
		} else {
		    // The User was not authenticated.
		    respObj.put("error", "Delete Not Supported in this API.");
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
		case 2: // This means it is of the AND Type.
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

    // Set up the Join for the Select.
    private Select setupJoins(Select select, JSONObject joinJObj) {
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
	    tablesBeingCalled.add(joinJObj.get("name").toString());
	    if (joinJObj.containsKey("join_with_table")) {
		if (tablesBeingCalled.contains(joinJObj.get("join_with_table").toString())) {
		    joinWithTable = joinJObj.get("join_with_table").toString();
		}
	    }
	    select.addJoin(new Join(sqlType, joinJObj.get("name").toString(),
		    joinWithTable + '.' + joinJObj.get("column_name").toString(),
		    joinJObj.get("name").toString() + '.' + joinJObj.get("join_with_column_name").toString()));
	    // Check if there are more joins.
	    if (joinJObj.containsKey("join")) {
		// There are more Joins.
		joinWithTable = joinJObj.get("name").toString();
		select = setupJoins(select, (JSONObject) joinJObj.get("join"));
	    }
	    return select;
	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

}
