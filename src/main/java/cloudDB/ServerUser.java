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
package cloudDB;

import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author karan
 */
public class ServerUser {

    // Variables.
    private String status;
    private String databaseID = null;
    private UUID userUID;
    private String token;
    private long serverID = 0;
    private String serverToken;
    private String[] dbCredentials;

    // Constructor.
    private ServerUser() {
	// We do not require this constructor.
    }

    // Constructor to Set the required details for the server user.
    ServerUser(UUID serverUserID, String token, long serverID, String serverToken, String databaseID) {
	// Set the required data.
	this.userUID = serverUserID;
	this.serverID = serverID;
	this.token = token;
	this.serverToken = serverToken;
	this.databaseID = databaseID;
	dbCredentials = new String[2];
    }

    // Required Methods.
    // Get the User ID.
    public UUID getUserID() {
	return userUID;
    }

    // Get the User Token.
    public String getToken() {
	return token;
    }

    // Get the Database id.
    public String getDatabaseID() {
	return databaseID;
    }

    // Get the Status of the User.
    public String getStatus() {
	return status;
    }

    // Set the DB Credentials.
    public void setDatabaseCred(String username, String password) {
	this.dbCredentials[0] = username;
	this.dbCredentials[1] = password;
    }
    
    // Get the DB Credentials.
    public String[] getDatabaseCred() {
	return dbCredentials;
    }

    
    // Convert the ServerUser Object to a JSON Object.
    public JSONObject toJSONObject() {
        try {
            // Here we will Convert the Server User Object to JSON Object.
            JSONObject jObj = new JSONObject();
            // Add User Data into the JSON Object.
            jObj.put("database_id", databaseID);
            jObj.put("user_uid", userUID.toString());
            jObj.put("token", token);
            jObj.put("server_id", serverID);
            jObj.put("server_token", serverToken);

            // Add the Database Credentials.
            JSONArray dbCredJArr = new JSONArray();
            dbCredJArr.add(dbCredentials[0]);
            dbCredJArr.add(dbCredentials[1]);
            jObj.put("db_credentials", dbCredJArr);

            return jObj;
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }

    // Create an Instance of Server User, using JSON Object.
    static ServerUser CreateInstance(JSONObject serverUserJObj) {
        try {
            // Here we will Create an Instance of the Device Object.
            ServerUser serverUser = new ServerUser();
            serverUser.databaseID = serverUserJObj.get("database_id").toString();
            serverUser.userUID = UUID.fromString(serverUserJObj.get("user_uid").toString());
            serverUser.token = serverUserJObj.get("token").toString();
            serverUser.serverToken = serverUserJObj.get("server_token").toString();
            serverUser.serverID = Long.parseLong(serverUserJObj.get("server_id").toString());
            // Add the Database Credentials.
            JSONArray dbCredJArr = (JSONArray) new JSONParser().parse(serverUserJObj.get("db_credentials").toString());
            serverUser.dbCredentials = new String[]{dbCredJArr.get(0).toString(), dbCredJArr.get(1).toString()};
            return serverUser;
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }
}
