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

import cloudKV.CloudKV;
import cloudKV.CloudKVBuilder;
import helpers.C;
import helpers.Helper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import security.Auth;

/**
 *
 * @author karan
 */
public class CloudDB {

    // Variables
    private String status;
    private StringBuilder statusTrace;
    private Connection coreConn;
    private Connection conn;
    private HashMap<String, Connection> connMap;
    private boolean isAuthenticated;
    private User userObj;
    private ServerUser serverUserObj;
    private Device currentDevice;
    private CloudKV ckv;

    // Static Values.
    public static class CRUD {

        public static final short READ = 0;
        public static final short WRITE = 1;
        public static final short UPDATE = 2;
        public static final short DELETE = 3;
    }

    static class TableMetaType {

        static final short SIMPLE = 0;
        static final short MULTI_TENANT = 2;
        static final short SYNCABLE = 4;
        static final short SYNCABLE_MULTI_TENANT = 8;

    }

    // This is used for caching.
    /*private static HashMap<String, User> usersMap;
    private static HashMap<String, ServerUser> serverUsersMap;
    private static HashMap<String, short[]> tableMeta;*/
    // Final values that can be called to validate values.
    public final String TAG = "CloudDB";

    // This will maintain all the types of all the column data.
    public static class Type {

        public static final int SHORT = 1000;
        public static final int INTEGER = 1001;
        public static final int LONG = 1002;
        public static final int FLOAT = 1003;
        public static final int DOUBLE = 1004;
        public static final int BOOLEAN = 1005;
        public static final int STRING = 1006;
        public static final int BYTE = 1007;
        public static final int BYTE_ARR = 1008;
    }

    // Constructors.
    // Default Initialization.
    CloudDB() {
        // This will only be used by the authentication classes.
        try {
            // Initiate the Cluster Connection.
            isAuthenticated = false;
            if (connMap == null) {
                connMap = new HashMap<>();
            }
            if (coreConnInit()) {
                // The Core Connection was initiate Properly.
                // The Connection to the 36E Business database is established.
                // Continue with the Validation.
                // Start the Authentication of the User.
                addStatus("Initiated Core Connection");
                // Setup the Table Meta Cache if it is empty.
                setupTableMeta();

            } else {
                // The Core Database Connection was not initialised.
                addStatus("ERR : Core Database not Connected");
            }
        } catch (Exception e) {
            // There was an Error.
            isAuthenticated = false;
        }
    }

    // We require this initialization, with the authentication and table.
    // This constructor version is to be called on normal bases, to interact with the database
    // We use this constructor to authenticate the normal / server user according to token. (N : Normal User | S : Server User)
    public CloudDB(String token) {
        try {
            // Here we check the token.
            if (connMap == null) {
                connMap = new HashMap<>();
            }
            if (coreConnInit()) {
                // The Core Connection was initiate Properly.
                // The Connection to the 36E Business database is established.
                addStatus("Initiated the Core Connection");
                // Setup the Table Meta Cache if it is empty.
                setupTableMeta();

                // Continue with the Validation.
                // Start the Authentication of the User.
                // Initialize the Users Caching Layer.
                if (Auth.validate(token)) {
                    String[] tokens = Auth.extractToken(token);
                    if (tokens[0] == null) {
                        // The Token is not valid.
                        isAuthenticated = false;
                        addStatus("Token Extraction Error");
                    } else {
                        // The Token is Valid.
                        if (tokens[0].endsWith("N")) {
                            // The User is Probably a Normal User.
                            isAuthenticated = authenticateUser(token, tokens[0], tokens[1]);
                        } else if (tokens[0].endsWith("S")) {
                            // The User is Probably a Server User.
                            isAuthenticated = authenticateServerUser(token, tokens[0], tokens[1]);
                        } else {
                            // The Token is probably not valid.
                            isAuthenticated = false;
                            addStatus("Token InValid");
                        }
                    }
                } else {
                    // It is not a valid token.
                    isAuthenticated = false;
                    addStatus("Token InValid");
                }
            } else {
                // The Core Database Connection was not initialised.
                addStatus("ERR : Core Database not Connected");
            }

        } catch (Exception e) {
            // There was an Error.
            isAuthenticated = false;
        } finally {
            // Lets Go for the Clean up process.
            // This is to prevent memory leaks from the Cache
            cleanup();
        }
    }

    private void setupTableMeta() {
        try {

            // Fill the Table Meta Cache if it is empty.
            if (ckv.count() > 0) {  // This Would mean that this has ran before, so that there would be values in the database.
                // There is a Table Cache.
                // We do not have to do anything.
            } else {
                try {
                    // Here we will Get the Table Meta 
                    // Create the SQL
                    String tableMetaSQL = "SELECT id, database_id, multi_tenant, syncable FROM tables";
                    // Create the Prepared Statement.
                    PreparedStatement tableMetaPrepStmt = coreConn.prepareStatement(tableMetaSQL);
                    // Execute the Query.
                    ResultSet tableMetaRes = tableMetaPrepStmt.executeQuery();

                    // Check if there are tables in the system.
                    if (tableMetaRes.isBeforeFirst()) {
                        // Store the Table Meta in the Cache.
                        // Extract the Meta Data from the Result Set.
                        while (tableMetaRes.next()) {
                            // Extract the data.
                            JSONArray tableMetaJArr = new JSONArray();
                            tableMetaJArr.add(tableMetaRes.getBoolean("multi_tenant") ? (short) 1 : (short) 0);
                            tableMetaJArr.add(tableMetaRes.getBoolean("syncable") ? (short) 1 : (short) 0);
                            ckv.put(tableMetaRes.getString("database_id")
                                                + '.' + tableMetaRes.getString("id"),
                                                tableMetaJArr);
                        }
                        addStatus("Added Table Caches");
                    } else {
                        // There are no tables in the database.
                        // There is some Error.
                        addStatus("No Tables Found in the System");
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
            }
        } catch (Exception e) {
            // There was an Error.
            addStatus("Table Meta Setter Err : " + e.getLocalizedMessage());
        }
    }

    // Here we will initialize the All the Required systems, 
    // And authenticate the user will all the required systems and connect to the correct database.
    // These are helper methods to setup the connections with the required databases.
    // Here we make sure that the Cloud Core database is connected 
    // And the connection ready to be used.
    private boolean coreConnInit() {
        try {
            // Connect to the "Cloud Core" main database.
            // Create the IP Address of the DB Servers to connect to.
            // Lets Initialize and get all the caches.
            Class.forName("org.postgresql.Driver");

            ckv = CloudKVBuilder.CreateInstance(C.Clust.CLOUD_KV_API_KEY);

            // Nullify the User objects.
            userObj = null;
            serverUserObj = null;

            // Lets Start with the Core Connection.
            if (coreConn == null || coreConn.isClosed()) {
                // Lets Create a connection.
                coreConn = DriverManager.getConnection("jdbc:postgresql://"
                                    + C.Clust.DB_CLUSTER_IP
                                    + "/"
                                    + C.Clust.CORE_DB
                                    + "?sslmode=" + C.Clust.SSL_STATUS
                                    + "&loadBalanceHosts=true", C.Clust.CLOUD_USER, "");
                //    connPConf.setPassword(password);	// TODO : Should be put on when we launch
                addStatus("Connection Established with Core DB");
                return true;

            } else {
                // The Connection Pool is already initialized.
                // We don't need to do anything.
                addStatus("Connection Established with Core DB");
                return true;
            }
        } catch (Exception e) {
            // There was an Error.
            addStatus("Core Conn Err : " + Helper.Error.getErrorMessage(e) + " : " + Helper.Error.getPrintStack(e));
            return false;
        }
    }

    // Here we setup the Connection to the actual database trying to be connected by the user.
    private boolean connInit(String dbName, String user, String password) {
        try {
            // Connect to the "Cloud Core" main database.
            // Create the IP Address of the DB Servers to connect to.
            // Validate that the user and the password are not null.
            if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
                // The username or the password is not valid.
                return false;
            } else {
                // Here we will Create a Connection with the Specific Database.
                Class.forName("org.postgresql.Driver");

                if (connMap.containsKey(dbName)) {
                    // The Connection Pool has already been created.
                    // Here we do not have to do anything.
                    addStatus("Connection Specific pool already set");
                    conn = connMap.get(dbName);
                    if (conn == null) {
                        // The Connection is null & Probably died in the Hashmap.
                        conn = DriverManager.getConnection("jdbc:postgresql://"
                                            + C.Clust.DB_CLUSTER_IP
                                            + "/"
                                            + dbName
                                            + "?sslmode=" + C.Clust.SSL_STATUS
                                            + "&loadBalanceHosts=true", user, "");
                        //    connPConf.setPassword(password);	// TODO : Should be put on when we launch
                        connMap.put(dbName, conn);
                    }
                    addStatus("Connection Established with Specific DB");
                    return true;
                } else {
                    // The Connection has not yet been created.
                    conn = DriverManager.getConnection("jdbc:postgresql://"
                                        + C.Clust.DB_CLUSTER_IP
                                        + "/"
                                        + dbName
                                        + "?sslmode=" + C.Clust.SSL_STATUS
                                        + "&loadBalanceHosts=true", user, "");
                    //    connPConf.setPassword(password);	// TODO : Should be put on when we launch
                    connMap.put(dbName, conn);
                    addStatus("Connection Established with Specific DB");
                    return true;
                }
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Get the User Object.
    boolean validateCRUD(String tableName, short crud) {
        try {
            // Try to validate the user.
            addStatus("Going to Validate CRUD");
            return isServerUser() ? true : userObj.validateCRUD(tableName, crud);
        } catch (Exception e) {
            // There was an Errror.
            return false;
        }
    }

    boolean validateRoleRLS(long roleID, short crud) {
        try {
            // Try to validate the user.
            return isServerUser() ? true : userObj.validateRowRLS(roleID, crud);
        } catch (Exception e) {
            // There was an Errror.
            return false;
        }
    }

    boolean validateGroupRLS(long groupID, short crud) {
        try {
            // Try to validate the user.
            return isServerUser() ? true : userObj.validateGroupRLS(groupID, crud);
        } catch (Exception e) {
            // There was an Errror.
            return false;
        }
    }

    // Here we will Authenticate the User & Device with Token, and Setup the Required Data.
    private boolean authenticateUser(String token, String userToken, String deviceToken) {
        try {
            // Let's Try to Authenticate the Token.
            if (userToken.length() == 160 && (deviceToken.length() == 96 || deviceToken.length() == 128)) {
                // Lets Validate the Tokens.
                // Check if We need to add the Padding of User Token into the Device Token.
                if (deviceToken.length() == 96) {
                    // Lets add the Padding of the user token to device token.
                    deviceToken = Auth.addUserPaddingToDeviceToken(userToken, deviceToken);
                }
                // The Device Token and All Params are correct.
                // Check the Caching Layer.
                if (ckv.contains(userToken)) {
                    // The user is in the Caching Layer.
                    addStatus("Already Authenticated, found in Caching Layer.");
                    userObj = User.CreateInstance((JSONObject) new JSONParser()
                                        .parse(ckv.getString(userToken)));
                    addStatus("User Obj : " + (userObj == null ? "Is Null" : "Exists"));
                    // Get the Device Details.
                    // Check if the device exists in the cache.
                    currentDevice = userObj.getDevice(deviceToken);
                    if (currentDevice == null) {
                        // The Device was not found.
                        // Get the Device Data from the DB.
                        String deviceSQL = "SELECT token, user_token, user_id, "
                                            + "device_token, device_uid, push_token FROM user_device_auth "
                                            + "WHERE token = ? AND user_token = ? AND device_token = ?";
                        // Prepare the Statement.
                        PreparedStatement prepStmt = coreConn.prepareStatement(deviceSQL);
                        // Bind the Params.
                        prepStmt.setString(1, token);
                        prepStmt.setString(2, userToken);
                        prepStmt.setString(3, deviceToken);
                        // Execute the query.
                        ResultSet userDeviceRes = prepStmt.executeQuery();
                        try {
                            // Lets Extract the Data of the Device.
                            if (userDeviceRes.isBeforeFirst()) {
                                // The Device was found.
                                // Extract the data.
                                addStatus("User Device Authenticated");
                                userDeviceRes.next();
                                // Initialize the Device Object.
                                Device device = new Device((UUID) userDeviceRes.getObject("device_uid"),
                                                    userDeviceRes.getString("device_token"));
                                // Add the Push Data.
                                device.setPushToken(userDeviceRes.getString("push_token"));
                                // Add the Device to the User Object, and Push the Change to the Caching layer.
                                userObj.addDevice(deviceToken, device);
                                // Add to the Caching layer.
                                currentDevice = device;
                                ckv.put(userToken, userObj.toJSONObject());
                            } else {
                                // The Device was not found.
                                addStatus("User Device Not Authenticated");
                                ckv.put(userToken, userObj.toJSONObject());
                                return false;
                            }
                        } catch (Exception e) {
                            // There was an Error.
                            addStatus("User Device Not Authenticated");
                            ckv.put(userToken, userObj.toJSONObject());
                            return false;
                        }
                    }
                    ckv.put(userToken, userObj.toJSONObject());
                    return connInit(userObj.getDatabaseID(), userObj.getDatabaseCred()[0],
                                        userObj.getDatabaseCred()[1]);
                } else {
                    // The user is not in the Caching Layer.
                    // Let's Authenticate using the database.
                    // Create the SQL Statement.
                    String userSQL = "SELECT user_device_auth.user_id as user_id, "
                                        + "user_device_auth.token as token, "
                                        + "user_device_auth.user_token as user_token, "
                                        + "user_device_auth.device_token as device_token, "
                                        + "user_device_auth.device_uid as device_uid, "
                                        + "user_device_auth.push_token as push_token, "
                                        + "user_device_auth.tenant_id as tenant_id, "
                                        + "user_device_auth.database_id as database_id, "
                                        + "user_device_auth.device_uid as device_uid, "
                                        + "user_device_auth.username as username, "
                                        + "user_device_auth.password as password, "
                                        + "user_device_auth.is_active as is_active, "
                                        + "table_user_map.table_id as table_id, "
                                        + "table_user_map.read as table_read, "
                                        + "table_user_map.write as table_write, "
                                        + "table_user_map.edit as table_edit, "
                                        + "table_user_map.remove as table_remove "
                                        + "FROM user_device_auth "
                                        + "INNER JOIN table_user_map ON user_device_auth.user_id = table_user_map.user_id "
                                        + "WHERE user_device_auth.token = ? "
                                        + "AND user_device_auth.user_token = ? AND user_device_auth.device_token = ?";
                    // Prepare the Statement.
                    PreparedStatement prepStmt = coreConn.prepareStatement(userSQL);
                    // Bind the Statements.
                    prepStmt.setString(1, token);
                    prepStmt.setString(2, userToken);
                    prepStmt.setString(3, deviceToken);
                    // Execute the Query.
                    ResultSet userAuthRes = prepStmt.executeQuery();
                    // Check the Result.
                    if (userAuthRes.isBeforeFirst()) {
                        // The Device and User was authenticated.
                        // Get the Data of the User and the Device.
                        addStatus("User Was Authenticated in the DB.");
                        userObj = null;
                        // Get the Related data.
                        while (userAuthRes.next()) {
                            // Here let's check if the User data has already been removed or not.
                            // Here lets get the device data of the user.
                            // Check if the User Object is empty.
                            if (userObj == null) {
                                // Add the device to the user.
                                if (userAuthRes.getBoolean("is_active")) {
                                    // The User is Active, so lets continue.
                                    Device device = new Device((UUID) userAuthRes.getObject("device_uid"),
                                                        userAuthRes.getString("token"));
                                    // Add the Push Data.
                                    device.setPushToken(userAuthRes.getString("push_token"));
                                    // Here lets get the user data.
                                    // Get the User Data.
                                    userObj = new User((UUID) userAuthRes.getObject("user_id"),
                                                        userAuthRes.getString("token"), device,
                                                        userAuthRes.getString("database_id"),
                                                        userAuthRes.getLong("tenant_id"));
                                    // Set the DB Connection Credentials.
                                    userObj.setDatabaseCred(userAuthRes.getString("username"), userAuthRes.getString("password"));
                                    userObj.setActive(userAuthRes.getBoolean("is_active"));
                                } else {
                                    // The user is suspended.
                                    addStatus("User is Suspended");
                                    return false;
                                }
                            }
                            //// Now we will start the Authorization System.
                            // Now we can retrieve the Table Based Authentication of the System.
                            short[] tableCRUD = new short[4];
                            // Extract the data.
                            tableCRUD[0] = userAuthRes.getShort("table_read");
                            tableCRUD[1] = userAuthRes.getShort("table_write");
                            tableCRUD[2] = userAuthRes.getShort("table_edit");
                            tableCRUD[3] = userAuthRes.getShort("table_remove");
                            // Add it to the hashmap.
                            userObj.putTableACL(userAuthRes.getString("table_id"), tableCRUD);
                        }
                        currentDevice = userObj.getDevice(deviceToken);
                        // Get the Roles and Groups of the F-RLS of the User.
                        rolesGroupsFRLSSetup();	// Does all the Roles and Groups Authentication.

                        ckv.put(userToken, userObj.toJSONObject());

                        // Set the User object into the Caching Layer.
                        return connInit(userObj.getDatabaseID(), userObj.getDatabaseCred()[0],
                                            userObj.getDatabaseCred()[1]);
                    } else {
                        // The Authentication failed.
                        // The User might not be assigned to any tables yet.
                        // Lets Authenticate with the User and Device Auth Table.
                        // Get the Device Data from the DB.
                        String deviceSQL = "SELECT token, user_token, user_id, database_id, "
                                            + "tenant_id, username, password, "
                                            + "device_token, device_uid, push_token, is_active "
                                            + "FROM user_device_auth "
                                            + "WHERE token = ? AND user_token = ? AND device_token = ?";
                        // Prepare the Statement.
                        PreparedStatement userPrepStmt = coreConn.prepareStatement(deviceSQL);
                        // Bind the Params.
                        userPrepStmt.setString(1, token);
                        userPrepStmt.setString(2, userToken);
                        userPrepStmt.setString(3, deviceToken);
                        // Execute the query.
                        ResultSet userDeviceRes = userPrepStmt.executeQuery();
                        try {
                            // Lets Extract the Data of the Device.
                            if (userDeviceRes.isBeforeFirst()) {
                                // The Device was found.
                                // Extract the data.
                                addStatus("User Device Authenticated");
                                // Check if the User is Active.
                                userDeviceRes.next();
                                if (userDeviceRes.getBoolean("is_active")) {
                                    addStatus("User is Active");
                                    // Get the User Data.
                                    if (userObj == null) {
                                        // Add the device to the user.
                                        // The User is Active, so lets continue.
                                        Device device = new Device((UUID) userDeviceRes.getObject("device_uid"),
                                                            userDeviceRes.getString("token"));
                                        // Add the Push Data.
                                        device.setPushToken(userDeviceRes.getString("push_token"));
                                        // Here lets get the user data.
                                        // Get the User Data.
                                        userObj = new User((UUID) userDeviceRes.getObject("user_id"),
                                                            userDeviceRes.getString("token"), device,
                                                            userDeviceRes.getString("database_id"),
                                                            userDeviceRes.getLong("tenant_id"));
                                        currentDevice = device;
                                        // Set the DB Connection Credentials.
                                        userObj.setDatabaseCred(userDeviceRes.getString("username"), userDeviceRes.getString("password"));
                                        userObj.setActive(userDeviceRes.getBoolean("is_active"));

                                    }
                                    // Get the Roles and Groups of the F-RLS of the User.
                                    rolesGroupsFRLSSetup();	// Does all the Roles and Groups Authentication.
                                    // Add to the Caching layer.
                                    ckv.put(userToken, userObj.toJSONObject());
                                    // Set the User object into the Caching Layer.
                                    return connInit(userObj.getDatabaseID(), userObj.getDatabaseCred()[0],
                                                        userObj.getDatabaseCred()[1]);
                                } else {
                                    // The user is Suspended.
                                    addStatus("User is Suspended");
                                    return false;
                                }
                            } else {
                                // The Device was not found.
                                addStatus("User Device Not Authenticated");
                                return false;
                            }
                        } catch (Exception e) {
                            // There was an Error.
                            addStatus("User Device Not Authenticated");
                            return false;
                        }

                    }
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Here we will Authenticate the User & Device with Token, and Setup the Required Data.
    private boolean authenticateServerUser(String token, String userToken, String serverToken) {
        try {
            // Let's Try to Authenticate the Token.
            if (userToken.length() == 160 && serverToken.length() == 96) {
                // Lets Validate the Tokens.
                // The Device Token and All Params are correct.
                // Check the Caching Layer.
                //addStatus("Tokens of Proper Length");
                if (ckv.contains(userToken)) {
                    // The user is in the Caching Layer.
                    addStatus("Already Authenticated, found in Caching Layer.");
                    serverUserObj = ServerUser.CreateInstance((JSONObject) new JSONParser()
                                        .parse(ckv.getString(userToken)));
                    // Get the Device Details.
                    return connInit(serverUserObj.getDatabaseID(), serverUserObj.getDatabaseCred()[0],
                                        serverUserObj.getDatabaseCred()[1]);
                } else {
                    // The user is not in the Caching Layer.
                    // Let's Authenticate using the database.
                    // Create the SQL Statement.
                    addStatus("User Not Found in Cache");
                    String userSQL = "SELECT token, user_token, server_token, user_id, server_id, "
                                        + "database_id, username, password FROM user_server_auth "
                                        + "WHERE token = ? AND user_token = ? AND server_token = ?";
                    // Prepare the Statement.
                    PreparedStatement prepStmt = coreConn.prepareStatement(userSQL);
                    // Bind the Statements.
                    prepStmt.setString(1, token);
                    prepStmt.setString(2, userToken);
                    prepStmt.setString(3, serverToken);
                    // Execute the Query.
                    ResultSet userServerRes = prepStmt.executeQuery();
                    // Check the Result.
                    if (userServerRes.isBeforeFirst()) {
                        // The Device and User was authenticated.
                        // Get the Data of the User and the Device.
                        addStatus("Server User was Authenticated in the DB.");
                        // Get the Device Data.
                        userServerRes.next();
                        // Get the User Data.
                        serverUserObj = new ServerUser((UUID) userServerRes.getObject("user_id"),
                                            userServerRes.getString("user_token"), userServerRes.getLong("server_id"),
                                            userServerRes.getString("server_token"), userServerRes.getString("database_id"));
                        serverUserObj.setDatabaseCred(userServerRes.getString("username"),
                                            userServerRes.getString("password"));
                        // Set the User object into the Caching Layer.
                        ckv.put(userToken, serverUserObj.toJSONObject());
                        // Create the Connection to the Database.
                        return connInit(serverUserObj.getDatabaseID(), serverUserObj.getDatabaseCred()[0],
                                            serverUserObj.getDatabaseCred()[1]);
                    } else {
                        // The Authentication failed.
                        addStatus("Server User Not Authenticated in the DB.");
                        return false;
                    }
                }
            } else {
                addStatus("Server User Not Authenticated in the DB.");
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Here we will Get the F-RLS Authorization of the User.
    private boolean rolesGroupsFRLSSetup() {
        try {
            if (userObj != null) {
                // Setup the Roles F-RLS of the User.
                try {
                    // Create the SQL Statement.
                    String rolesSQL = "SELECT user_id, role_id, read, write, edit, remove, time_stamp "
                                        + "FROM user_role_map WHERE user_id = ?";
                    // Create the prepared statement.
                    PreparedStatement rolesPrepStmt = coreConn.prepareStatement(rolesSQL);
                    // Bind the Params
                    rolesPrepStmt.setObject(1, userObj.getUserID());
                    // Execute the query.
                    ResultSet roleRes = rolesPrepStmt.executeQuery();
                    // Check if there are any roles the user is connected to.
                    if (roleRes.isBeforeFirst()) {
                        // There are roles the user is registered to.
                        // Extract the Roles of the user.
                        while (roleRes.next()) {
                            // Extract the data and add it to the user object.
                            short[] roleRLS = new short[4];
                            // Extract the data.
                            roleRLS[0] = roleRes.getShort("read");
                            roleRLS[1] = roleRes.getShort("write");
                            roleRLS[2] = roleRes.getShort("edit");
                            roleRLS[3] = roleRes.getShort("remove");
                            // Add it to the hashmap.
                            userObj.putRoleRLS(roleRes.getLong("role_id"), roleRLS);
                        }
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                // Set up the Group F-RLS into the User.
                try {
                    // Create the SQL Statement.
                    String groupsSQL = "SELECT user_id, group_id, read, write, edit, remove, time_stamp "
                                        + "FROM user_group_map WHERE user_id = ?";
                    // Create the prepared statement.
                    PreparedStatement groupPrepStmt = coreConn.prepareStatement(groupsSQL);
                    // Bind the Params
                    groupPrepStmt.setObject(1, userObj.getUserID());
                    // Execute the query.
                    ResultSet groupRes = groupPrepStmt.executeQuery();
                    // Check if there are any roles the user is connected to.
                    if (groupRes.isBeforeFirst()) {
                        // There are roles the user is registered to.
                        // Extract the Roles of the user.
                        while (groupRes.next()) {
                            // Extract the data and add it to the user object.
                            short[] groupRLS = new short[4];
                            // Extract the data.
                            groupRLS[0] = groupRes.getShort("read");
                            groupRLS[1] = groupRes.getShort("write");
                            groupRLS[2] = groupRes.getShort("edit");
                            groupRLS[3] = groupRes.getShort("remove");
                            // Add it to the hashmap.
                            userObj.putGroupRLS(groupRes.getLong("group_id"), groupRLS);
                        }
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Get the user authentication.
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    // Create the Normal Sync User.
    public User createUser(long tenantID, boolean isActive) {
        try {
            // Here we will Create a new normal / sync user
            // Generate a random string for the user token.
            // This should be just 159 characters as the last character will be added here itself.
            String token = Helper.StringManu.generate(159, Helper.StringManu.Type.DEFAULT)
                                + 'N';
            // Create the SQL Statement.
            String userSQL = "INSERT INTO users (token, tenant_id, database_id, username, password, is_active) "
                                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING user_id, time_stamp";
            // Prepare the Statement.
            PreparedStatement userPrepStmt = coreConn.prepareStatement(userSQL);
            userPrepStmt.setString(1, token);
            userPrepStmt.setLong(2, tenantID);
            userPrepStmt.setString(3, serverUserObj.getDatabaseID());
            userPrepStmt.setString(4, serverUserObj.getDatabaseCred()[0]);
            userPrepStmt.setString(5, serverUserObj.getDatabaseCred()[1]);
            userPrepStmt.setBoolean(6, isActive);

            // Execute the Query.
            ResultSet userRes = userPrepStmt.executeQuery();
            // Check if it is inserted.
            if (userRes.isBeforeFirst()) {
                // The user has been created.
                // Create a user object and return it.
                userRes.next();
                User inUserObj = new User((UUID) userRes.getObject("user_id"), token,
                                    null, serverUserObj.getDatabaseID(), tenantID);
                inUserObj.setTimeStamp(userRes.getString("time_stamp"));
                // Return the Object.
                return inUserObj;
            } else {
                // The user has not been created.
                return null;
            }
        } catch (Exception e) {
            // There was an Error.
            //addStatus("User Creation Err : " + Helper.Error.getErrorMessage(e));
            return null;
        }
    }

    // Suspend users.
    public boolean suspendUser(final UUID userUID) {
        try {
            // Here let's suspend the user 
            // First let's delete all the devices logged into that user in the user device auth.
            try {
                String suspendSQL = "DELETE FROM user_device_auth WHERE user_id = ?";
                // Prepare the Statement.
                PreparedStatement suspendPrepStmt = coreConn.prepareStatement(suspendSQL);
                // Bind the Statements.
                suspendPrepStmt.setObject(1, userUID);
                // Execute the Query.
                suspendPrepStmt.executeUpdate();
            } catch (Exception e) {
                // There was an Error.
            }
            try {
                // Validate if the user is deleted
                // The user's device authentications were properly deleted.
                // Let's now delete the Devices of the user.
                String deviceSQL = "DELETE FROM devices WHERE user_id = ?";
                // Prepare the Statement.
                PreparedStatement devicePrepStmt = coreConn.prepareStatement(deviceSQL);
                // Bind the Statements.
                devicePrepStmt.setObject(1, userUID);
                // Execute the Query.
                devicePrepStmt.executeUpdate();
            } catch (Exception e) {
                // THere was an Error.
            }
            try {
                // Validate if the user is deleted
                // Now we will update the user as a suspended user.
                String suspendSQL = "UPDATE users SET is_active = false "
                                    + "WHERE user_id = ? AND database_id = ? RETURNING token";
                // Prepare the Statement.
                PreparedStatement suspendPrepStmt = coreConn.prepareStatement(suspendSQL);
                // Bind the Statement.
                suspendPrepStmt.setObject(1, userUID);
                suspendPrepStmt.setString(2, getDatabaseID());
                // Execute the Query.
                ResultSet suspendedRes = suspendPrepStmt.executeQuery();
                if (suspendedRes.isBeforeFirst()) {
                    // The User is Suspended at the database level.
                    // We need to Update the Cache.
                    // Lets sync this data to the devices of the user that is suspended.
                    try {
                        // Here we will Push the data to the devices of the user.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Now we will send the data to the device.
                                    // Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    JSONObject colIJObj = new JSONObject();
                                    colIJObj.put("value", false);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("is_active", colIJObj);

                                    // Create the Where JSON to be sent.
                                    JSONObject whereJObj = new JSONObject();
                                    whereJObj.put("name", "user_id");
                                    whereJObj.put("condition", "EQUAL");
                                    whereJObj.put("data", userUID.toString());

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
                                        .sendDataWithUserID(msgJObj, userUID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                    try {
                        suspendedRes.next();
                        // Remove the User from the Cache as well.
                        return ckv.remove(suspendedRes.getString("token"));
                    } catch (Exception e) {
                        // There was an Error
                        return false;
                    }
                } else {
                    // The user is not suspended.
                    return false;
                }
            } catch (Exception e) {
                // There was an Error.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Activate the User.
    public boolean activateUser(final UUID userUID) {
        try {
            // Here let's Activate the user 
            // Only a Server User can remove a normal user.
            if (isServerUser()) {
                // Now we will update the user as a suspended user.
                String activateSQL = "UPDATE users SET is_active = true WHERE user_id = ? AND database_id = ?";
                // Prepare the Statement.
                PreparedStatement activatePrepStmt = coreConn.prepareStatement(activateSQL);
                // Bind the Statement.
                activatePrepStmt.setObject(1, userUID);
                activatePrepStmt.setString(2, getDatabaseID());
                // Execute the Query.
                int i = activatePrepStmt.executeUpdate();

                // Lets sync this data to the devices of the user that is suspended.
                try {
                    // Here we will Push the data to the devices of the user.
                    if (i > 0) {
                        // The user is activated, lets update all the devices.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Now we will send the data to the device.
                                    // Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    JSONObject colIJObj = new JSONObject();
                                    colIJObj.put("value", true);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("is_active", colIJObj);

                                    // Create the Where JSON to be sent.
                                    JSONObject whereJObj = new JSONObject();
                                    whereJObj.put("name", "user_id");
                                    whereJObj.put("condition", "EQUAL");
                                    whereJObj.put("data", userUID.toString());

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
                                        .sendDataWithUserID(msgJObj, userUID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return i > 0;
            } else {
                // It is not a server user.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Delete / Remove Users from database.
    public boolean removeUser(final UUID userUID) {
        try {
            // Lets Remove the User from the Database.
            // Only a Server User can remove a normal user.
            if (isServerUser()) {
                // Currently Connected as a Server user.
                // Delete all the Users Devices.
                try {
                    String removeUserSQL = "DELETE FROM user_device_auth WHERE user_id = ?";
                    // Prepare the statement.
                    PreparedStatement removePrepStmt = coreConn.prepareStatement(removeUserSQL);
                    // Bind the Parameters.
                    removePrepStmt.setObject(1, userUID);
                    // Execute The Statement.
                    int deleteRes = removePrepStmt.executeUpdate();

                    // Create the SQL Statement.
                    removeUserSQL = "DELETE FROM devices WHERE user_id = ?";
                    // Prepare the statement.
                    removePrepStmt = coreConn.prepareStatement(removeUserSQL);
                    // Bind the Parameters.
                    removePrepStmt.setObject(1, userUID);
                    // Execute The Statement.
                    deleteRes = removePrepStmt.executeUpdate();
                } catch (Exception e) {
                    // There was an Error.
                }
                // Remove all the table ACL.
                try {
                    String removeUserSQL = "DELETE FROM table_user_map WHERE user_id = ?";
                    // Prepare the statement.
                    PreparedStatement removePrepStmt = coreConn.prepareStatement(removeUserSQL);
                    // Bind the Parameters.
                    removePrepStmt.setObject(1, userUID);
                    // Execute The Statement.
                    int deleteRes = removePrepStmt.executeUpdate();
                } catch (Exception e) {
                    // There was an Error.
                }

                // Remove all the Roles & Groups RLS.
                // Remove the Roles RLS from the Table.
                try {
                    String removeUserSQL = "DELETE FROM user_role_map WHERE user_id = ?";
                    // Prepare the statement.
                    PreparedStatement removePrepStmt = coreConn.prepareStatement(removeUserSQL);
                    // Bind the Parameters.
                    removePrepStmt.setObject(1, userUID);
                    // Execute The Statement.
                    int deleteRes = removePrepStmt.executeUpdate();
                } catch (Exception e) {
                    // There was an Error.
                }

                // Remove the Group RLS from the Table.
                try {
                    String removeUserSQL = "DELETE FROM user_group_map WHERE user_id = ?";
                    // Prepare the statement.
                    PreparedStatement removePrepStmt = coreConn.prepareStatement(removeUserSQL);
                    // Bind the Parameters.
                    removePrepStmt.setObject(1, userUID);
                    // Execute The Statement.
                    int deleteRes = removePrepStmt.executeUpdate();
                } catch (Exception e) {
                    // There was an Error.
                }

                // Create the SQL Statement.
                String removeUserSQL = "DELETE FROM users WHERE user_id = ? AND database_id = ? RETURNING token";
                // Prepare the statement.
                PreparedStatement removePrepStmt = coreConn.prepareStatement(removeUserSQL);
                // Bind the Parameters.
                removePrepStmt.setObject(1, userUID);
                removePrepStmt.setString(2, getDatabaseID());
                // Execute the Query.
                ResultSet deleteRes = removePrepStmt.executeQuery();
                if (deleteRes.isBeforeFirst()) {
                    // The User is Suspended at the database level.
                    // Lets sync this data to the devices of the user that is suspended.
                    try {
                        // Here we will Push the data to the devices of the user.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Now we will send the data to the device.// Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    JSONObject colIJObj = new JSONObject();
                                    colIJObj.put("value", false);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("is_active", colIJObj);

                                    // Create the Where JSON to be sent.
                                    JSONObject whereJObj = new JSONObject();
                                    whereJObj.put("name", "user_id");
                                    whereJObj.put("condition", "EQUAL");
                                    whereJObj.put("data", userUID.toString());

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
                                            .sendDataWithUserID(msgJObj, userUID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                    // We need to Update the Cache.
                    try {
                        deleteRes.next();
                        // Remove the User from the Cache as well.
                        return ckv.remove(deleteRes.getString("token"));
                    } catch (Exception e) {
                        // There was an Error
                        return false;
                    }
                } else {
                    // The user is not suspended.
                    return false;
                }
            } else {
                // It is not a server user.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Register a Device to a User.
    public Device registerDevice(String userToken, Device.Type type, String deviceID, long applicationID, String pushToken) {
        try {
            // Lets Register the device to the user.
            // Validate the Type.
            // The Type Exists.
            // Get the Data of the user from the users table.
            String userSQL = "SELECT user_id, token, tenant_id, username, password, is_active "
                                + "FROM users WHERE token = ? AND database_id = ?";
            // Prepare a Statement.
            PreparedStatement userPrepStmt = coreConn.prepareStatement(userSQL);
            // Bind the Params.
            userPrepStmt.setString(1, userToken);
            userPrepStmt.setString(2, getDatabaseID());
            // Execute the Query.
            ResultSet userRes = userPrepStmt.executeQuery();
            // Validate if the user exists.
            if (userRes.isBeforeFirst()) {
                // Check if the User is Active.
                userRes.next();
                if (userRes.getBoolean("is_active")) {
                    // Check if the Registered Device ID Exists.
                    String deviceSQL = "SELECT device_uid, device_id, user_id FROM devices "
                                        + "WHERE device_id = ? AND user_id = ? AND database_id = ?";
                    // Prepare the Statement.
                    PreparedStatement devicePrepStmt = coreConn.prepareStatement(deviceSQL);
                    // Bind the Params.
                    devicePrepStmt.setString(1, deviceID);
                    devicePrepStmt.setObject(2, UUID.fromString(userRes.getObject("user_id").toString()));
                    devicePrepStmt.setString(3, getDatabaseID());
                    // Execute the Query.
                    ResultSet deviceRes = devicePrepStmt.executeQuery();
                    // Check if there is a device like that.
                    if (deviceRes.isBeforeFirst()) {
                        // Remove the Device from the database.
                        deviceRes.next();
                        removeDevice(UUID.fromString(deviceRes.getObject("device_uid").toString()));
                    }
                    // Create the Device Token.
                    String device96Token = Helper.StringManu.generate(96, Helper.StringManu.Type.DEFAULT);
                    // Add the Padding bits to the Token.
                    String device128Token = Auth.addUserPaddingToDeviceToken(userToken, device96Token);
                    // Create the SQL Statement.
                    String regDeviceSQL = "INSERT INTO devices (token, type, device_id, database_id, push_token, user_id) "
                                        + "VALUES (?, ?, ?, ?, ?, ?) RETURNING token, device_uid, time_stamp";
                    // Prepare the Statement.
                    PreparedStatement regDevicePrepStmt = coreConn.prepareStatement(regDeviceSQL);
                    // Bind the Params.
                    regDevicePrepStmt.setString(1, device128Token);
                    regDevicePrepStmt.setString(2, type.toString().toLowerCase());
                    regDevicePrepStmt.setString(3, deviceID);
                    regDevicePrepStmt.setString(4, getDatabaseID());
                    regDevicePrepStmt.setString(5, pushToken);
                    regDevicePrepStmt.setObject(6, UUID.fromString(userRes.getObject("user_id").toString()));
                    // Execute the Query.
                    ResultSet regDeviceRes = regDevicePrepStmt.executeQuery();
                    // Check if the query executed successfully.
                    if (regDeviceRes.isBeforeFirst()) {
                        // The Device was registered successfully.
                        // Get the Device Data, and create the device.
                        regDeviceRes.next();
                        String userDeviceToken = Auth.generateToken(userToken, device96Token);
                        Device device = new Device((UUID) regDeviceRes.getObject("device_uid"), userDeviceToken);
                        device.setPushToken(pushToken);
                        device.setDeviceID(deviceID);
                        device.setType(Device.Type.valueOf(type.toString().toUpperCase()));
                        device.setTimeStamp(regDeviceRes.getString("time_stamp"));

                        // Let's insert into the User Device Auth Table.
                        String userDeviceAuthSQL = "INSERT INTO user_device_auth (token, user_token, device_token,"
                                            + "user_id, device_uid, push_token, database_id, tenant_id, username, password, is_active) "
                                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING token";
                        // Prepare the Statement.
                        PreparedStatement authUserDevicePrepStmt = coreConn.prepareStatement(userDeviceAuthSQL);
                        // Bind the Params.
                        authUserDevicePrepStmt.setString(1, userDeviceToken);
                        authUserDevicePrepStmt.setString(2, userToken);
                        authUserDevicePrepStmt.setString(3, device128Token);
                        authUserDevicePrepStmt.setObject(4, UUID.fromString(userRes.getString("user_id")));
                        authUserDevicePrepStmt.setObject(5, device.getDeviceUID());
                        authUserDevicePrepStmt.setString(6, pushToken);
                        authUserDevicePrepStmt.setString(7, serverUserObj.getDatabaseID());
                        authUserDevicePrepStmt.setLong(8, userRes.getLong("tenant_id"));
                        authUserDevicePrepStmt.setString(9, serverUserObj.getDatabaseCred()[0]);
                        authUserDevicePrepStmt.setString(10, serverUserObj.getDatabaseCred()[1]);
                        authUserDevicePrepStmt.setBoolean(11, userRes.getBoolean("is_active"));
                        // Execute the Query.
                        ResultSet authUserDeviceRes = authUserDevicePrepStmt.executeQuery();
                        if (authUserDeviceRes.isBeforeFirst()) {
                            // The Auth User was inserted.
                            // Add the Device to the Caching layer.
                            try {
                                // Get the User Object from the Cache.
                                if (ckv.contains(userToken)) {
                                    // The User Object exists in Cache.
                                    // Get from the Cache.
                                    User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                                        .parse(ckv.getString(userToken)));
                                    userObj.addDevice(device128Token, device);
                                    // Add the User Object to the Cache.
                                    ckv.put(userObj.getToken(), userObj.toJSONObject());
                                }
                            } catch (Exception e) {
                                // There was an Error
                            }
                            return device;
                        } else {
                            return null;
                        }
                    } else {
                        // The Device was not registered successfully.
                        return null;
                    }
                } else {
                    // The user is not active and cannot register a new device.
                    return null;
                }
            } else {
                // The user does'nt exist.
                addStatus("User Does'nt Exist.");
                return null;
            }
        } catch (Exception e) {
            // There was an Error.
            return null;
        }
    }

    // Get the Devices of the User or the System.
    public Device[] getDevices(UUID userID, UUID deviceUID, int limit, int offset) {
        try {
            // Here we will get the devices of the user if the user id is specified.
            // Create the SQL Statement.
            String devicesSQL = "SELECT device_uid, token, user_id, type, "
                                + "device_id, database_id, push_token, time_stamp "
                                + "FROM devices WHERE database_id = ?";
            // Check if the User ID has been provided or the user is a normal user.
            boolean isUserIDProvided = false;
            if (isNormalUser()) {
                // The user is a normal user, let's add the user id.
                devicesSQL = devicesSQL + " AND user_id = ?";
                isUserIDProvided = true;
                userID = userObj.getUserID();
            } else {
                // The use is a server user. Lets check if the user has provided the user id.
                if (userID != null) {
                    // They have provided a user id.
                    // Let's add the user id.
                    devicesSQL = devicesSQL + " AND user_id = ?";
                    isUserIDProvided = true;
                }
            }
            // Lets now check if the device id has been provided.
            boolean isDeviceIDProvided = false;
            if (deviceUID != null) {
                // The device id has been provided.
                // Check if it is the first where clause or not.
                // Now lets add the device id.
                devicesSQL = devicesSQL + " AND device_uid = ?";
                isDeviceIDProvided = true;
            }
            // Add the Limit to the SQL Statement.
            if (limit > 0) {
                devicesSQL = devicesSQL + " LIMIT " + limit;
            }
            // Add the offset to the SQL Statement.
            if (offset > 0) {
                devicesSQL = devicesSQL + " OFFSET " + offset;
            }
            // Now lets prepare the statement.
            PreparedStatement devicesPrepStmt = coreConn.prepareStatement(devicesSQL);
            // Lets bind the params if required.
            int paramIndex = 1;
            devicesPrepStmt.setString(1, getDatabaseID());
            if (isUserIDProvided) {
                devicesPrepStmt.setObject(++paramIndex, userID);
            }
            if (isDeviceIDProvided) {
                devicesPrepStmt.setObject(++paramIndex, deviceUID);
            }
            // Lets execute the prepared statements.
            ResultSet devicesRes = devicesPrepStmt.executeQuery();
            // Extract the data.
            if (devicesRes.isBeforeFirst()) {
                // There are devices to be extracted.
                List<Device> devices = new ArrayList<>();
                while (devicesRes.next()) {
                    // Let's Extract the Data of the Device from the row.
                    Device device = new Device(UUID.fromString(devicesRes
                                        .getObject("device_uid").toString()),
                                        null,
                                        devicesRes.getString("token"));
                    // Let's extract all the other data.
                    device.setType(Device.Type.valueOf(devicesRes.getString("type").toUpperCase()));
                    device.setPushToken(devicesRes.getString("push_token"));
                    device.setTimeStamp(devicesRes.getString("time_stamp"));
                    device.setDeviceID(devicesRes.getString("device_id"));
                    device.setUserID(UUID.fromString(devicesRes.getObject("user_id").toString()));
                    // Add the Device to the devices arraylist.
                    devices.add(device);
                }
                // Lets return the devices array.
                return devices.toArray(new Device[devices.size()]);
            } else {
                // There are no devices to be extracted.
                return new Device[0];
            }
        } catch (Exception e) {
            // There was an Error.
            return new Device[0];
        }
    }

    // Get the Current device that is connected if it is a normal user.
    public Device getCurrentDevice() {
        try {
            // Here we will Get the Current Device that is Connected.
            if (isNormalUser() && currentDevice != null) {
                // It is a Normal User.
                // There is a Current Device.
                return currentDevice;
            } else {
                // It is not a Normal User.
                // There is no Current Device.
                return null;
            }
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }
    
    // Edit the Device with a new push token.
    public int updatePushToken(String deviceUserToken, String newPushToken) {
        try {
            // Here we will try to update the push token related tables.
            // Update the Device User Auth Table.
            // Create the SQL Statement
            if (isServerUser()) {
                String deviceUserAuthSQL = "UPDATE user_device_auth SET push_token = ? "
                                    + "WHERE device_token = ? AND database_id = ? RETURNING user_token, token";
                // Create the Prepared statement.
                PreparedStatement deviceUserAuthPrepStmt = coreConn.prepareStatement(deviceUserAuthSQL);
                // Bind the Params.
                deviceUserAuthPrepStmt.setString(1, newPushToken);
                deviceUserAuthPrepStmt.setString(2, deviceUserToken);
                deviceUserAuthPrepStmt.setString(3, getDatabaseID());
                // Execute the query.
                ResultSet updatedRes = deviceUserAuthPrepStmt.executeQuery();

                // Update the Devices Table.
                // Create the SQL Statement
                String deviceSQL = "UPDATE devices SET push_token = ? WHERE token = ? AND database_id = ?";
                // Create the Prepared statement.
                PreparedStatement devicePrepStmt = coreConn.prepareStatement(deviceSQL);
                // Bind the Params.
                devicePrepStmt.setString(1, newPushToken);
                devicePrepStmt.setString(2, deviceUserToken);
                devicePrepStmt.setString(3, getDatabaseID());
                // Execute the query.
                int updated = devicePrepStmt.executeUpdate();
                // Update the Push token in the cache as well.
                try {
                    if (updatedRes.next()) {
                        // Check if the User Object is in the Cache.
                        if (ckv.contains(updatedRes.getString("user_token"))) {
                            // Get the User Object from the Cache.
                            User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                                .parse(ckv.getString(updatedRes.getString("user_token"))));
                            Device device = userObj.getDevice(deviceUserToken);
                            device.setPushToken(newPushToken);
                            // Push into the user object.
                            userObj.addDevice(deviceUserToken, device);
                            // Add the User to the cache.
                            ckv.put(userObj.getToken(), userObj.toJSONObject());
                        }
                        // Lets update the Device with the Old Token, with the New Token.
                        try {
                            // Here we will send the update to that device.
                            // Create the Column JSON to be sent.
                            JSONObject colJObj = new JSONObject();
                            JSONObject colIJObj = new JSONObject();
                            colIJObj.put("value", newPushToken);
                            colIJObj.put("type", "VALUE");
                            colJObj.put("user_token", colIJObj);

                            // Create the Where JSON to be sent.
                            JSONObject whereJObj = new JSONObject();
                            whereJObj.put("name", "token");
                            whereJObj.put("condition", "EQUAL");
                            whereJObj.put("data", updatedRes.getString("token"));

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
                                    .addDeviceToken(newPushToken)
                                    .putMessage(msgJObj)
                                    .executeAsync();
                        } catch (Exception e) {
                            // There was an Error.
                        }
                    }
                } catch (Exception e) {
                    // There was an Error
                }
                return updated;
            } else {
                return 0;
            }
        } catch (Exception e) {
            // There was an Error.
            return 0;
        }
    }

    // Unregister / Remove Device from a User.
    public boolean removeDevice(UUID deviceUID) {
        try {
            // Lets Remove the device from the user.
            // Only a Server User can remove a device.
            if (isServerUser()) {
                // Currently Connected as a Server user.
                // First lets delete the device user auth.
                // Create the SQL Statement.
                String removeUserSQL = "DELETE FROM user_device_auth WHERE device_uid = ?"
                                    + " AND database_id = ? RETURNING token, user_token, push_token";
                // Prepare the statement.
                PreparedStatement removePrepStmt = coreConn.prepareStatement(removeUserSQL);
                // Bind the Parameters.
                removePrepStmt.setObject(1, deviceUID);
                removePrepStmt.setString(2, getDatabaseID());
                // Execute The Statement.
                ResultSet deleteRes = removePrepStmt.executeQuery();

                // Create the SQL Statement.
                removeUserSQL = "DELETE FROM devices WHERE device_uid = ? AND database_id = ? ";
                // Prepare the statement.
                removePrepStmt = coreConn.prepareStatement(removeUserSQL);
                // Bind the Parameters.
                removePrepStmt.setObject(1, deviceUID);
                removePrepStmt.setString(2, getDatabaseID());
                // Execute The Statement.
                int deleted = removePrepStmt.executeUpdate();
                // Remove the Device from the Cache.
                try {
                    if (deleteRes.next()) {
                        // Get the User Object from the Cache.
                        if (ckv.contains(deleteRes.getString("user_token"))) {
                            // Get the User Object from the Cache.
                            User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                                .parse(ckv.getString(deleteRes.getString("user_token"))));
                            // The Device was delete from the User Device Auth, and we will remove it from the Cache.
                            userObj.removeDevice(deleteRes.getString("token"));
                            // Add this user object into the Cache.
                            ckv.put(userObj.getToken(), userObj.toJSONObject());
                        }
                        // Lets update the Device with the Old Token, with the New Token.
                        try {
                            // Here we will send the update to that device.
                            // Create the Column JSON to be sent.
                            JSONObject colJObj = new JSONObject();
                            JSONObject colIJObj = new JSONObject();
                            colIJObj.put("value", "false");
                            colIJObj.put("type", "VALUE");
                            colJObj.put("is_active", colIJObj);

                            // Create the Where JSON to be sent.
                            JSONObject whereJObj = new JSONObject();
                            whereJObj.put("name", "token");
                            whereJObj.put("condition", "EQUAL");
                            whereJObj.put("data", deleteRes.getString("token"));

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
                                .addDeviceToken(deleteRes.getString("push_token"))
                                .putMessage(msgJObj)
                                .executeAsync();
                        } catch (Exception e) {
                            // There was an Error.
                        }
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                // Check if executed.
                return deleted != 0;
            } else {
                // It is not a server user.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Get the User.
    public User getUser() {
        return userObj;
    }

    // Database ID / Name of the User.
    public String getDatabaseID() {
        return isServerUser() ? serverUserObj.getDatabaseID() : userObj.getDatabaseID();
    }

    // Get the Server User.
    ServerUser getServerUser() {
        return serverUserObj;
    }

    // Get the Connection of the database, but should only be package specific.
    Connection getConn() {
        return conn;
    }

    // Allow the user to get the Cloud Core Database Connection.
    Connection getCoreConn() {
        // Get the Cloud Core Connection.
        return coreConn;
    }

    // Here we set the status and the status trace.
    private void addStatus(String status) {
        try {
            // Here we append the status to the status trace and status.
            if (statusTrace == null) {
                statusTrace = new StringBuilder();
            }
            this.status = status;
            statusTrace.append(status)
                                .append('\n');
        } catch (Exception e) {
            // There was an Error.
        }
    }

    // This will retrieve the whole status trace.
    public String getStatusTrace() {
        return statusTrace.toString();
    }

    // This will retrieve just the status of the cloud object.
    public String getStatus() {
        return status;
    }

    public boolean isServerUser() {
        return serverUserObj != null && isAuthenticated();
    }

    public boolean isNormalUser() {
        return userObj != null && isAuthenticated();
    }

    // Here we will allow only the Respective Server User to access the data.
    public User[] getUsers(long tenantID, int limit, int offset) {
        try {
            // Here we validate if it is a server user.
            if (isServerUser()) {
                // It is a server user.
                // Let's get all the Users from the data.
                String userSQL = "SELECT user_id, token, database_id, tenant_id, "
                                    + "is_active, time_stamp FROM users WHERE database_id = ?";
                // Add the Tenant ID.
                if (tenantID > 0) {
                    userSQL = userSQL + " AND tenant_id = ?";
                }
                // Add the Limit to the SQL Statement.
                if (limit > 0) {
                    userSQL = userSQL + " LIMIT " + limit;
                }
                // Add the offset to the SQL Statement.
                if (offset > 0) {
                    userSQL = userSQL + " OFFSET " + offset;
                }
                // Prepare the Statement.
                PreparedStatement userPrepStmt = coreConn.prepareStatement(userSQL);
                userPrepStmt.setString(1, getDatabaseID());
                if (tenantID > 0) {
                    // Bind the Params.
                    userPrepStmt.setLong(2, tenantID);
                }
                // Execute the Query.
                ResultSet usersRes = userPrepStmt.executeQuery();
                // Validate the Result.
                if (usersRes.isBeforeFirst()) {
                    // There exists Users.
                    // Extract all the users from the result.
                    List<User> normalUserObjs = new ArrayList<>();
                    while (usersRes.next()) {
                        try {
                            // Extract the data from the result set.
                            User normalUserObj = new User((UUID) usersRes.getObject("user_id"),
                                                usersRes.getString("token"), null,
                                                usersRes.getString("database_id"), usersRes.getLong("tenant_id"));
                            normalUserObj.setActive(usersRes.getBoolean("is_active"));
                            // Get the Devices Linked to that User.
                            String deviceSQL = "SELECT device_uid, token, device_id, "
                                                + "type, push_token FROM devices WHERE user_id = ? AND database_id = ?";
                            // Prepare the Statement.
                            PreparedStatement devicePrepStmt = coreConn.prepareStatement(deviceSQL);
                            // Bind the Queries.
                            devicePrepStmt.setObject(1, normalUserObj.getUserID());
                            devicePrepStmt.setString(2, getDatabaseID());
                            // Execute Statement
                            ResultSet devicesRes = devicePrepStmt.executeQuery();
                            // Validate the Devices.
                            if (devicesRes.isBeforeFirst()) {
                                // There are devices.
                                while (devicesRes.next()) {
                                    // Lets add devices to the user.
                                    Device deviceObj = new Device((UUID) devicesRes.getObject("device_uid"),
                                                        devicesRes.getString("token"));
                                    deviceObj.setPushToken(devicesRes.getString("push_token"));
                                    deviceObj.setType(Device.Type.valueOf(devicesRes.getString("type").toUpperCase()));
                                    deviceObj.setDeviceID(devicesRes.getString("device_id"));
                                    // Add the Device to the user object.
                                    normalUserObj.addDevice(devicesRes.getString("token"), deviceObj);
                                }
                            } else {
                                // No devices found of that user.
                            }
                            normalUserObjs.add(normalUserObj);
                        } catch (Exception e) {
                            // There was an Error.
                        }
                    }
                    return normalUserObjs.toArray(new User[normalUserObjs.size()]);
                } else {
                    // No Users Found.
                    return new User[0];
                }
            } else {
                // It is not a server user.
                return new User[0];
            }
        } catch (Exception e) {
            // There was an Error.
            addStatus("Er : " + Helper.Error.getPrintStack(e));
            return new User[0];
        }
    }

    // CRUDs over the Table ACL.
    // Get the Table Level ACL of a User.
    public HashMap<String, short[]> getTableCRUD(UUID userID) {
        try {
            // Get the Table Level ACL of each table.
            // Only a server user has this priviledge.
            if (isServerUser()) {
                // It is a server user.
                // Create the SQL String.
                String tableACLSQL = "SELECT table_id, read, write, edit, remove "
                                    + "FROM table_user_map "
                                    + "WHERE user_id = ?";
                // Prepare the statement.
                PreparedStatement tableACLPrepStmt = coreConn.prepareStatement(tableACLSQL);
                // Bind the Params.
                tableACLPrepStmt.setObject(1, userID);
                // Execute the Query.
                ResultSet tableACLRes = tableACLPrepStmt.executeQuery();
                // Check if there is data retrieved.
                if (tableACLRes.isBeforeFirst()) {
                    // There are tables linked to this user.
                    // Lets retrieve the data.
                    HashMap<String, short[]> tableACL = new HashMap<>();
                    while (tableACLRes.next()) {
                        short[] tableCRUD = new short[4];
                        // Extract the data.
                        tableCRUD[0] = tableACLRes.getShort("read");
                        tableCRUD[1] = tableACLRes.getShort("write");
                        tableCRUD[2] = tableACLRes.getShort("edit");
                        tableCRUD[3] = tableACLRes.getShort("remove");
                        // Add it to the hashmap.
                        tableACL.put(tableACLRes.getString("table_id"), tableCRUD);
                    }
                    // Return the hashmap.
                    return tableACL;
                } else {
                    // There are no tables linked to this user.
                    return new HashMap<>();
                }
            } else {
                // Not Priviledged.
                return new HashMap<>();
            }
        } catch (Exception e) {
            // There was an Error.
            return new HashMap<>();
        }
    }

    // Add the Table Level ACL of the User.
    public boolean grantTableCRUD(final UUID userID, final String tableID, final short read,
                        final short write, final short edit, final short delete) {
        try {
            // Only a Server User can grant into the table.
            if (isServerUser()) {
                // Now lets add the Grant into the table.
                // Create the SQL String.
                String tableCRUDSQL = "INSERT INTO table_user_map (table_id, "
                                    + "user_id, read, write, edit, remove) VALUES (?, ?, ?, ?, ?, ?)";
                // Prepare the Statement.
                PreparedStatement tableCRUDPrepStmt = coreConn.prepareStatement(tableCRUDSQL);
                // Bind the Params.
                tableCRUDPrepStmt.setString(1, tableID);
                tableCRUDPrepStmt.setObject(2, userID);
                tableCRUDPrepStmt.setShort(3, read);
                tableCRUDPrepStmt.setShort(4, write);
                tableCRUDPrepStmt.setShort(5, edit);
                tableCRUDPrepStmt.setShort(6, delete);
                // Execute the Query.
                int inserted = tableCRUDPrepStmt.executeUpdate();
                try {
                    // Lets update the Cache.
                    String userToken = getUserToken(userID);
                    // Get the User Object from the User Cache.
                    if (ckv.contains(userToken)) {
                        User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                            .parse(ckv.getString(userToken)));
                        // Update into the Object.
                        userObj.putTableACL(tableID, new short[]{read, write, edit, delete});
                        // Add the User Object back into the database.
                        ckv.put(userToken, userObj.toJSONObject());
                    }
                    // If the Authorization has been inserted, lets sent to the devices
                    try {
                        // Lets Send the Data to the Devices.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Here we will send the update to that device.
                                    // Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    colJObj.put("read", read);
                                    colJObj.put("write", write);
                                    colJObj.put("edit", edit);
                                    colJObj.put("remove", delete);
                                    colJObj.put("table_id", tableID);
                                    colJObj.put("user_id", userID.toString());

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "INSERT");
                                    msgJObj.put("table_name", "tables_acl_");
                                    msgJObj.put("columns", colJObj);
                                    msgJObj.put("update_time", System.currentTimeMillis());
                                    // Here lets extract the RLSs that we have to send the data to.
                                    msgJObj.put("sync_id_", UUID.randomUUID());

                                    // Send the data to the device.
                                    new Sync(null)
                                                        .push()
                                                        .sendDataWithUserID(msgJObj, userID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return inserted > 0;
            } else {
                // Not a server user.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Update the Table Level ACL of the User.
    public boolean updateGrantTableCRUD(final UUID userID, final String tableID,
                        final short read, final short write, final short edit, final short delete) {
        try {
            // Only a Server User can grant into the table.
            if (isServerUser()) {
                // Now lets add the Grant into the table.
                // Create the SQL String.
                String tableCRUDSQL = "UPDATE table_user_map SET read = ?, write = ?, "
                                    + "edit = ?, remove = ? WHERE table_id = ? AND user_id = ?";
                // Prepare the Statement.
                addStatus(tableCRUDSQL);
                PreparedStatement tableCRUDPrepStmt = coreConn.prepareStatement(tableCRUDSQL);
                // Bind the Params.
                tableCRUDPrepStmt.setShort(1, read);
                tableCRUDPrepStmt.setShort(2, write);
                tableCRUDPrepStmt.setShort(3, edit);
                tableCRUDPrepStmt.setShort(4, delete);
                tableCRUDPrepStmt.setString(5, tableID);
                tableCRUDPrepStmt.setObject(6, userID);
                // Execute the Query.
                int updated = tableCRUDPrepStmt.executeUpdate();
                try {
                    // Lets update the Cache.
                    String userToken = getUserToken(userID);
                    // Get the User Object from the User Cache.
                    if (ckv.contains(userToken)) {
                        User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                            .parse(ckv.getString(userToken)));
                        // Update into the Object.
                        userObj.putTableACL(tableID, new short[]{read, write, edit, delete});
                        // Add the User Object back into the database.
                        ckv.put(userToken, userObj.toJSONObject());
                    }
                    // Lets now update the devices
                    try {
                        // Here we will send the update to that device.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Now we will send the data to the device.
                                    // Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    JSONObject colIJObj = new JSONObject();
                                    // Add the Read Column.
                                    colIJObj.put("value", read);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("read", colIJObj);
                                    // Add the Write Column.
                                    colIJObj.put("value", write);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("write", colIJObj);
                                    // Add the Edit Column
                                    colIJObj.put("value", edit);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("edit", colIJObj);
                                    // Add the Remove Column
                                    colIJObj.put("value", delete);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("remove", colIJObj);

                                    // Create the Where JSON to be sent.
                                    JSONObject whereJObj = new JSONObject();
                                    whereJObj.put("name", "table_id");
                                    whereJObj.put("condition", "EQUAL");
                                    whereJObj.put("data", tableID);
                                    // Add the Additional Where Clause.
                                    JSONObject userWhereJObj = new JSONObject();
                                    userWhereJObj.put("name", "user_id");
                                    userWhereJObj.put("condition", "EQUAL");
                                    userWhereJObj.put("data", userID.toString());
                                    whereJObj.put("and", userWhereJObj);

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "UPDATE");
                                    msgJObj.put("table_name", "table_acl_");
                                    msgJObj.put("columns", colJObj);
                                    msgJObj.put("wheres", whereJObj);
                                    msgJObj.put("update_time", System.currentTimeMillis());
                                    // Here lets extract the RLSs that we have to send the data to.
                                    msgJObj.put("sync_id_", UUID.randomUUID());
                                    // Send the data to the device.
                                    new Sync(null)
                                            .push()
                                            .sendDataWithUserID(msgJObj, userID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                addStatus(tableCRUDSQL);
                return updated > 0;
            } else {
                // Not a server user.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Revoke a Priviledge given to a user over a table.
    public boolean revokeGrantTableCRUD(final UUID userID, final String tableID) {
        try {
            // Only a server user can revoke a priviledge.
            if (isServerUser()) {
                // It is a server user.
                // Create the SQL.
                String revokeSQL = "DELETE FROM table_user_map where table_id = ? and user_id = ?";
                // Create the Prepared statement.
                PreparedStatement revokePrepStmt = coreConn.prepareStatement(revokeSQL);
                // Bind the Params.
                revokePrepStmt.setString(1, tableID);
                revokePrepStmt.setObject(2, userID);
                // Execute the Query.
                int deleted = revokePrepStmt.executeUpdate();
                try {
                    // Lets update the Cache.
                    String userToken = getUserToken(userID);
                    // Get the User Object from the User Cache.
                    if (ckv.contains(userToken)) {
                        User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                            .parse(ckv.getString(userToken)));
                        // Update into the Object.
                        userObj.removeTableACL(tableID);
                        // Add the User Object back into the database.
                        ckv.put(userToken, userObj.toJSONObject());
                    }
                    // Now lets revoke the permission from all the devices
                    try {
                        // Here we will send the update to that device.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Now we will send the data to the device.
                                    // Create the Where JSON to be sent.
                                    JSONObject whereJObj = new JSONObject();
                                    whereJObj.put("name", "table_id");
                                    whereJObj.put("condition", "EQUAL");
                                    whereJObj.put("data", tableID);
                                    // Add the Additional Where Clause.
                                    JSONObject userWhereJObj = new JSONObject();
                                    userWhereJObj.put("name", "user_id");
                                    userWhereJObj.put("condition", "EQUAL");
                                    userWhereJObj.put("data", userID.toString());
                                    whereJObj.put("and", userWhereJObj);

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "DELETE");
                                    msgJObj.put("table_name", "table_acl_");
                                    msgJObj.put("wheres", whereJObj);
                                    msgJObj.put("update_time", System.currentTimeMillis());
                                    // Here lets extract the RLSs that we have to send the data to.
                                    msgJObj.put("sync_id_", UUID.randomUUID());
                                    // Send the data to the device.
                                    new Sync(null)
                                        .push()
                                        .sendDataWithUserID(msgJObj, userID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return deleted > 0;
            } else {
                // It is a sync user.
                // Not allowed to revoke a user's priviledge.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // CRUDs over the Role RLS.
    // Get the Role RLS of a User.
    public HashMap<Long, short[]> getRoleRLS(UUID userID) {
        try {
            // Get the Table Level ACL of each table.
            // Only a server user has this priviledge.
            if (isServerUser()) {
                // It is a server user.
                // Create the SQL String.
                String roleRLSSQL = "SELECT role_id, read, write, edit, remove "
                                    + "FROM user_role_map "
                                    + "WHERE user_id = ?";
                // Prepare the statement.
                PreparedStatement roleRLSPrepStmt = coreConn.prepareStatement(roleRLSSQL);
                // Bind the Params.
                roleRLSPrepStmt.setObject(1, userID);
                // Execute the Query.
                ResultSet roleRLSRes = roleRLSPrepStmt.executeQuery();
                // Check if there is data retrieved.
                if (roleRLSRes.isBeforeFirst()) {
                    // There are tables linked to this user.
                    // Lets retrieve the data.
                    HashMap<Long, short[]> roleRLSMap = new HashMap<>();
                    while (roleRLSRes.next()) {
                        short[] roleRLS = new short[4];
                        // Extract the data.
                        roleRLS[0] = roleRLSRes.getShort("read");
                        roleRLS[1] = roleRLSRes.getShort("write");
                        roleRLS[2] = roleRLSRes.getShort("edit");
                        roleRLS[3] = roleRLSRes.getShort("remove");
                        // Add it to the hashmap.
                        roleRLSMap.put(roleRLSRes.getLong("role_id"), roleRLS);
                    }
                    // Return the hashmap.
                    return roleRLSMap;
                } else {
                    // There are no tables linked to this user.
                    return new HashMap<>();
                }
            } else {
                // Not Priviledged.
                return new HashMap<>();
            }
        } catch (Exception e) {
            // There was an Error.
            return new HashMap<>();
        }
    }

    // Add the Role RLS of the User.
    public boolean grantRoleRLS(final UUID userID, final long roleID, final short read,
                        final short write, final short edit, final short delete) {
        try {
            // Only a Server User can grant into the table.
            if (isServerUser()) {
                // Now lets add the Grant into the table.
                // Create the SQL String.
                String roleRLSSQL = "INSERT INTO user_role_map (role_id, "
                                    + "user_id, read, write, edit, remove) VALUES (?, ?, ?, ?, ?, ?)";
                // Prepare the Statement.
                PreparedStatement roleRLSPrepStmt = coreConn.prepareStatement(roleRLSSQL);
                // Bind the Params.
                roleRLSPrepStmt.setLong(1, roleID);
                roleRLSPrepStmt.setObject(2, userID);
                roleRLSPrepStmt.setShort(3, read);
                roleRLSPrepStmt.setShort(4, write);
                roleRLSPrepStmt.setShort(5, edit);
                roleRLSPrepStmt.setShort(6, delete);
                // Execute the Query.
                int inserted = roleRLSPrepStmt.executeUpdate();
                try {
                    // Lets update the Cache.
                    String userToken = getUserToken(userID);
                    // Get the User Object from the User Cache.
                    if (ckv.contains(userToken)) {
                        User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                            .parse(ckv.getString(userToken)));
                        // Update into the Object.
                        userObj.putRoleRLS(roleID, new short[]{read, write, edit, delete});
                        // Add the User Object back into the database.
                        ckv.put(userToken, userObj.toJSONObject());
                    }
                    // Lets update all the devices of the user.
                    try {
                        // Lets Send the Data to the Devices.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Here we will send the update to that device.
                                    // Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    colJObj.put("read", read);
                                    colJObj.put("write", write);
                                    colJObj.put("edit", edit);
                                    colJObj.put("remove", delete);
                                    colJObj.put("role_id", roleID);
                                    colJObj.put("user_id", userID.toString());

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "INSERT");
                                    msgJObj.put("table_name", "role_rls_");
                                    msgJObj.put("columns", colJObj);
                                    msgJObj.put("update_time", System.currentTimeMillis());
                                    // Here lets extract the RLSs that we have to send the data to.
                                    msgJObj.put("sync_id_", UUID.randomUUID());

                                    // Send the data to the device.
                                    new Sync(null)
                                        .push()
                                        .sendDataWithUserID(msgJObj, userID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return inserted > 0;
            } else {
                // Not a server user.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Update the Role RLS of the User.
    public boolean updateGrantRoleRLS(final UUID userID, final long roleID,
                        final short read, final short write, final short edit, final short delete) {
        try {
            // Only a Server User can grant into the table.
            if (isServerUser()) {
                // Now lets add the Grant into the table.
                // Create the SQL String.
                String roleRLSSQL = "UPDATE user_role_map SET read = ?, write = ?, "
                                    + "edit = ?, remove = ? WHERE user_id = ? AND role_id = ?";
                // Prepare the Statement.
                PreparedStatement roleRLSPrepStmt = coreConn.prepareStatement(roleRLSSQL);
                // Bind the Params.
                roleRLSPrepStmt.setShort(1, read);
                roleRLSPrepStmt.setShort(2, write);
                roleRLSPrepStmt.setShort(3, edit);
                roleRLSPrepStmt.setShort(4, delete);
                roleRLSPrepStmt.setObject(5, userID);
                roleRLSPrepStmt.setLong(6, roleID);
                // Execute the Query.
                int updated = roleRLSPrepStmt.executeUpdate();
                try {
                    // Lets update the Cache.
                    String userToken = getUserToken(userID);
                    // Get the User Object from the User Cache.
                    if (ckv.contains(userToken)) {
                        User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                            .parse(ckv.getString(userToken)));
                        // Update into the Object.
                        userObj.putRoleRLS(roleID, new short[]{read, write, edit, delete});
                        // Add the User Object back into the database.
                        ckv.put(userToken, userObj.toJSONObject());
                    }

                    // Lets now update the devices
                    try {
                        // Here we will send the update to that device.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Now we will send the data to the device.// Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    JSONObject colIJObj = new JSONObject();
                                    // Add the Read Column.
                                    colIJObj.put("value", read);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("read", colIJObj);
                                    // Add the Write Column.
                                    colIJObj.put("value", write);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("write", colIJObj);
                                    // Add the Edit Column
                                    colIJObj.put("value", edit);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("edit", colIJObj);
                                    // Add the Remove Column
                                    colIJObj.put("value", delete);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("remove", colIJObj);

                                    // Create the Where JSON to be sent.
                                    JSONObject whereJObj = new JSONObject();
                                    whereJObj.put("name", "role_id");
                                    whereJObj.put("condition", "EQUAL");
                                    whereJObj.put("data", roleID);
                                    // Add the Additional Where Clause.
                                    JSONObject userWhereJObj = new JSONObject();
                                    userWhereJObj.put("name", "user_id");
                                    userWhereJObj.put("condition", "EQUAL");
                                    userWhereJObj.put("data", userID.toString());
                                    whereJObj.put("and", userWhereJObj);

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "UPDATE");
                                    msgJObj.put("table_name", "role_rls_");
                                    msgJObj.put("columns", colJObj);
                                    msgJObj.put("wheres", whereJObj);
                                    msgJObj.put("update_time", System.currentTimeMillis());
                                    // Here lets extract the RLSs that we have to send the data to.
                                    msgJObj.put("sync_id_", UUID.randomUUID());
                                    // Send the data to the device.
                                    new Sync(null)
                                            .push()
                                            .sendDataWithUserID(msgJObj, userID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return updated > 0;
            } else {
                // Not a server user.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Delete the Role RLS of the User.
    public boolean revokeGrantRoleRLS(final UUID userID, final long roleID) {
        try {
            // Only a server user can revoke a priviledge.
            if (isServerUser()) {
                // It is a server user.
                // Create the SQL.
                String revokeSQL = "DELETE FROM user_role_map where user_id = ? AND role_id = ?";
                // Create the Prepared statement.
                PreparedStatement revokePrepStmt = coreConn.prepareStatement(revokeSQL);
                // Bind the Params.
                revokePrepStmt.setObject(1, userID);
                revokePrepStmt.setLong(2, roleID);
                // Execute the Query.
                int deleted = revokePrepStmt.executeUpdate();
                try {
                    // Lets update the Cache.
                    String userToken = getUserToken(userID);
                    // Get the User Object from the User Cache.
                    if (ckv.contains(userToken)) {
                        User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                            .parse(ckv.getString(userToken)));
                        // Update into the Object.
                        userObj.removeRolsRLS(roleID);
                        // Add the User Object back into the database.
                        ckv.put(userToken, userObj.toJSONObject());
                    }
                    // Now lets revoke the permission from all the devices
                    try {
                        // Here we will send the update to that device.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Now we will send the data to the device.
                                    // Create the Where JSON to be sent.
                                    JSONObject whereJObj = new JSONObject();
                                    whereJObj.put("name", "role_id");
                                    whereJObj.put("condition", "EQUAL");
                                    whereJObj.put("data", roleID);
                                    // Add the Additional Where Clause.
                                    JSONObject userWhereJObj = new JSONObject();
                                    userWhereJObj.put("name", "user_id");
                                    userWhereJObj.put("condition", "EQUAL");
                                    userWhereJObj.put("data", userID.toString());
                                    whereJObj.put("and", userWhereJObj);

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "DELETE");
                                    msgJObj.put("table_name", "role_rls_");
                                    msgJObj.put("wheres", whereJObj);
                                    msgJObj.put("update_time", System.currentTimeMillis());
                                    // Here lets extract the RLSs that we have to send the data to.
                                    msgJObj.put("sync_id_", UUID.randomUUID());
                                    // Send the data to the device.
                                    new Sync(null)
                                        .push()
                                        .sendDataWithUserID(msgJObj, userID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return deleted > 0;
            } else {
                // It is a sync user.
                // Not allowed to revoke a user's priviledge.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // CRUDs over the Group RLS.
    // Get the Group RLS of a User.
    public HashMap<Long, short[]> getGroupRLS(UUID userID) {
        try {
            // Get the Table Level ACL of each table.
            // Only a server user has this priviledge.
            if (isServerUser()) {
                // It is a server user.
                // Create the SQL String.
                String groupRLSSQL = "SELECT group_id, read, write, edit, remove "
                                    + "FROM user_group_map "
                                    + "WHERE user_id = ?";
                // Prepare the statement.
                PreparedStatement groupRLSPrepStmt = coreConn.prepareStatement(groupRLSSQL);
                // Bind the Params.
                groupRLSPrepStmt.setObject(1, userID);
                // Execute the Query.
                ResultSet groupRLSRes = groupRLSPrepStmt.executeQuery();
                // Check if there is data retrieved.
                if (groupRLSRes.isBeforeFirst()) {
                    // There are tables linked to this user.
                    // Lets retrieve the data.
                    HashMap<Long, short[]> groupRLSMap = new HashMap<>();
                    while (groupRLSRes.next()) {
                        short[] groupRLS = new short[4];
                        // Extract the data.
                        groupRLS[0] = groupRLSRes.getShort("read");
                        groupRLS[1] = groupRLSRes.getShort("write");
                        groupRLS[2] = groupRLSRes.getShort("edit");
                        groupRLS[3] = groupRLSRes.getShort("remove");
                        // Add it to the hashmap.
                        groupRLSMap.put(groupRLSRes.getLong("group_id"), groupRLS);
                    }
                    // Return the hashmap.
                    return groupRLSMap;
                } else {
                    // There are no tables linked to this user.
                    return new HashMap<>();
                }
            } else {
                // Not Priviledged.
                return new HashMap<>();
            }
        } catch (Exception e) {
            // There was an Error.
            return new HashMap<>();
        }
    }

    // Add the Group RLS of the User.
    public boolean grantGroupRLS(final UUID userID, final long groupID,
                        final short read, final short write, final short edit, final short delete) {
        try {
            // Only a Server User can grant into the table.
            if (isServerUser()) {
                // Now lets add the Grant into the table.
                // Create the SQL String.
                String groupRLSSQL = "INSERT INTO user_group_map (group_id, "
                                    + "user_id, read, write, edit, remove) VALUES (?, ?, ?, ?, ?, ?)";
                // Prepare the Statement.
                PreparedStatement groupRLSPrepStmt = coreConn.prepareStatement(groupRLSSQL);
                // Bind the Params.
                groupRLSPrepStmt.setLong(1, groupID);
                groupRLSPrepStmt.setObject(2, userID);
                groupRLSPrepStmt.setShort(3, read);
                groupRLSPrepStmt.setShort(4, write);
                groupRLSPrepStmt.setShort(5, edit);
                groupRLSPrepStmt.setShort(6, delete);
                // Execute the Query.
                int inserted = groupRLSPrepStmt.executeUpdate();
                try {
                    // Lets update the Cache.
                    String userToken = getUserToken(userID);
                    // Get the User Object from the User Cache.
                    if (ckv.contains(userToken)) {
                        User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                            .parse(ckv.getString(userToken)));
                        // Update into the Object.
                        userObj.putGroupRLS(groupID, new short[]{read, write, edit, delete});
                        // Add the User Object back into the database.
                        ckv.put(userToken, userObj.toJSONObject());
                    }
                    // Lets update all the devices of the user.
                    try {
                        // Lets Send the Data to the Devices.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Here we will send the update to that device.
                                    // Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    colJObj.put("read", read);
                                    colJObj.put("write", write);
                                    colJObj.put("edit", edit);
                                    colJObj.put("remove", delete);
                                    colJObj.put("group_id", groupID);
                                    colJObj.put("user_id", userID.toString());

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "INSERT");
                                    msgJObj.put("table_name", "group_rls_");
                                    msgJObj.put("columns", colJObj);
                                    msgJObj.put("update_time", System.currentTimeMillis());
                                    // Here lets extract the RLSs that we have to send the data to.
                                    msgJObj.put("sync_id_", UUID.randomUUID());

                                    // Send the data to the device.
                                    new Sync(null)
                                            .push()
                                            .sendDataWithUserID(msgJObj, userID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return inserted > 0;
            } else {
                // Not a server user.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Update the Group RLS of the User.
    public boolean updateGrantGroupRLS(final UUID userID, final long groupID,
                        final short read, final short write, final short edit, final short delete) {
        try {
            // Only a Server User can grant into the table.
            if (isServerUser()) {
                // Now lets add the Grant into the table.
                // Create the SQL String.
                String groupRLSSQL = "UPDATE user_group_map SET read = ?, write = ?, "
                                    + "edit = ?, remove = ? WHERE user_id = ? AND group_id = ?";
                // Prepare the Statement.
                PreparedStatement groupRLSPrepStmt = coreConn.prepareStatement(groupRLSSQL);
                // Bind the Params.
                groupRLSPrepStmt.setShort(1, read);
                groupRLSPrepStmt.setShort(2, write);
                groupRLSPrepStmt.setShort(3, edit);
                groupRLSPrepStmt.setShort(4, delete);
                groupRLSPrepStmt.setObject(5, userID);
                groupRLSPrepStmt.setLong(6, groupID);
                // Execute the Query.
                int updated = groupRLSPrepStmt.executeUpdate();
                try {
                    // Lets update the Cache.
                    String userToken = getUserToken(userID);
                    // Get the User Object from the User Cache.
                    if (ckv.contains(userToken)) {
                        User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                            .parse(ckv.getString(userToken)));
                        // Update into the Object.
                        userObj.putGroupRLS(groupID, new short[]{read, write, edit, delete});
                        // Add the User Object back into the database.
                        ckv.put(userToken, userObj);
                    }
                    // Lets now update the devices
                    try {
                        // Here we will send the update to that device.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Now we will send the data to the device.// Create the Column JSON to be sent.
                                    JSONObject colJObj = new JSONObject();
                                    JSONObject colIJObj = new JSONObject();
                                    // Add the Read Column.
                                    colIJObj.put("value", read);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("read", colIJObj);
                                    // Add the Write Column.
                                    colIJObj.put("value", write);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("write", colIJObj);
                                    // Add the Edit Column
                                    colIJObj.put("value", edit);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("edit", colIJObj);
                                    // Add the Remove Column
                                    colIJObj.put("value", delete);
                                    colIJObj.put("type", "VALUE");
                                    colJObj.put("remove", colIJObj);

                                    // Create the Where JSON to be sent.
                                    JSONObject whereJObj = new JSONObject();
                                    whereJObj.put("name", "group_id");
                                    whereJObj.put("condition", "EQUAL");
                                    whereJObj.put("data", groupID);
                                    // Add the Additional Where Clause.
                                    JSONObject userWhereJObj = new JSONObject();
                                    userWhereJObj.put("name", "user_id");
                                    userWhereJObj.put("condition", "EQUAL");
                                    userWhereJObj.put("data", userID.toString());
                                    whereJObj.put("and", userWhereJObj);

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "UPDATE");
                                    msgJObj.put("table_name", "group_rls_");
                                    msgJObj.put("columns", colJObj);
                                    msgJObj.put("wheres", whereJObj);
                                    msgJObj.put("update_time", System.currentTimeMillis());
                                    // Here lets extract the RLSs that we have to send the data to.
                                    msgJObj.put("sync_id_", UUID.randomUUID());
                                    // Send the data to the device.
                                    new Sync(null)
                                        .push()
                                        .sendDataWithUserID(msgJObj, userID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return updated > 0;
            } else {
                // Not a server user.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Delete the Group RLS of the User.
    public boolean revokeGrantGroupRLS(final UUID userID, final long groupID) {
        try {
            // Only a server user can revoke a priviledge.
            if (isServerUser()) {
                // It is a server user.
                // Create the SQL.
                String revokeSQL = "DELETE FROM user_group_map where user_id = ? AND group_id = ?";
                // Create the Prepared statement.
                PreparedStatement revokePrepStmt = coreConn.prepareStatement(revokeSQL);
                // Bind the Params.
                revokePrepStmt.setObject(1, userID);
                revokePrepStmt.setLong(2, groupID);
                // Execute the Query.
                int deleted = revokePrepStmt.executeUpdate();
                try {
                    // Lets update the Cache.
                    String userToken = getUserToken(userID);
                    // Get the User Object from the User Cache.
                    if (ckv.contains(userToken)) {
                        User userObj = User.CreateInstance((JSONObject) new JSONParser()
                                            .parse(ckv.getString(userToken)));
                        // Update into the Object.
                        userObj.removeGroupRLS(groupID);
                        // Add the User Object back into the database.
                        ckv.put(userToken, userObj.toJSONObject());
                    }
                    // Now lets revoke the permission from all the devices
                    try {
                        // Here we will send the update to that device.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Now we will send the data to the device.
                                    // Create the Where JSON to be sent.
                                    JSONObject whereJObj = new JSONObject();
                                    whereJObj.put("name", "group_id");
                                    whereJObj.put("condition", "EQUAL");
                                    whereJObj.put("data", groupID);
                                    // Add the Additional Where Clause.
                                    JSONObject userWhereJObj = new JSONObject();
                                    userWhereJObj.put("name", "user_id");
                                    userWhereJObj.put("condition", "EQUAL");
                                    userWhereJObj.put("data", userID.toString());
                                    whereJObj.put("and", userWhereJObj);

                                    // Create the Message JSON to be sent.
                                    JSONObject msgJObj = new JSONObject();
                                    msgJObj.put("type", "DELETE");
                                    msgJObj.put("table_name", "group_rls_");
                                    msgJObj.put("wheres", whereJObj);
                                    msgJObj.put("update_time", System.currentTimeMillis());
                                    // Here lets extract the RLSs that we have to send the data to.
                                    msgJObj.put("sync_id_", UUID.randomUUID());
                                    // Send the data to the device.
                                    new Sync(null)
                                            .push()
                                            .sendDataWithUserID(msgJObj, userID, true);
                                } catch (Exception e) {
                                    // There was an Error.
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        // There was an Error.
                    }
                } catch (Exception e) {
                    // There was an Error.
                }
                return deleted > 0;
            } else {
                // It is a sync user.
                // Not allowed to revoke a user's priviledge.
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Lets get the User Token from the database using the user id.
    private String getUserToken(UUID userID) {
        try {
            // Lets update in the cache.
            // Get the Token of the User ID.
            // Create the SQL statement.
            if (isServerUser() && isAuthenticated()) {
                String userSQL = "SELECT user_id, token FROM users WHERE user_id = ?";
                // Prepare the Statement.
                PreparedStatement userPrepStmt = coreConn.prepareStatement(userSQL);
                // Bind the Params.
                userPrepStmt.setObject(1, userID);
                // Execute the Query.
                ResultSet userRes = userPrepStmt.executeQuery();
                // Check if the user exists.
                if (userRes.isBeforeFirst()) {
                    // The user exists.
                    userRes.next();
                    // Get the user Token.
                    return userRes.getString("token");
                } else {
                    // The user does'nt exist.
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            // There was an Error.
            return null;
        }
    }

    // Lets Get the User Data Result Set.
    private ResultSet getUserData(UUID userID) {
        try {
            // Lets update in the cache.
            // Get the Token of the User ID.
            // Create the SQL statement.
            if (isServerUser() && isAuthenticated()) {
                String userSQL = "SELECT * FROM users WHERE user_id = ?";
                // Prepare the Statement.
                PreparedStatement userPrepStmt = coreConn.prepareStatement(userSQL);
                // Bind the Params.
                userPrepStmt.setObject(1, userID);
                // Execute the Query.
                ResultSet userRes = userPrepStmt.executeQuery();
                // Check if the user exists.
                if (userRes.isBeforeFirst()) {
                    // The user exists.
                    userRes.next();
                    // Get the user Token.
                    return userRes;
                } else {
                    // The user does'nt exist.
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            // There was an Error.
            return null;
        }
    }

    // Close the Connections and related resources, for a graceful handoff.
    // This will assist for the next call to use this same resources from the pool.
    public boolean close() {
        try {
            // Here we will close the required resources.
            // First we close the Core Connection.
            coreConn.close();
            // Now we close the Database Specific Connection.
            conn.close();
            // Lets Close the Key Value Store of the Database.
            ckv.close();
            // Now lets close all the other Resources we are using.
            // TODO : Close Unneccesary Resources.
            cleanup();
            // Set the Close as complete
            addStatus("All the Required Resources have been closed.");
            return true;

        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Check if the Table is to be Multi-Tenant.
    public boolean isMultiTenant(String tableID) {
        try {
            // Check if the Table is Multitenant.
            JSONArray tableCRUDJArr = (JSONArray) new JSONParser()
                                .parse(ckv.getString(getDatabaseID() + '.' + tableID));
            return Short.parseShort(tableCRUDJArr.get(0).toString()) == 1;
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Check if the Table is to be Syncable.
    public boolean isSyncable(String tableID) {
        try {
            // Check if the Table is Multitenant.
            JSONArray tableCRUDJArr = (JSONArray) new JSONParser()
                                .parse(ckv.getString(getDatabaseID() + '.' + tableID));
            return Short.parseShort(tableCRUDJArr.get(1).toString()) == 1;
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // This would be the Clean up process.
    // It would prevent memory leaks.
    private static void cleanup() {
        try {
            // Here we will clean up the Class.
            // TODO : Here we will do a clean up with the Hashmaps and all those kinda things.
            //	      We will handle this later.
        } catch (Exception e) {
            // There was an Error.
        }
    }

}
