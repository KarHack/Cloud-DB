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

import cloudDBEndpoints.TableDataSync;
import helpers.C;
import helpers.Helper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import waiter.ParamData;
import waiter.Waiter;

/**
 *
 * @author karan
 *
 * This Class will Handle everything that is related to syncing of data. From
 * the Archiving Protocol To the Data being pushed to the Devices
 *
 */
public class Sync {

    // Variables.
    private CloudDB cdb = null;
    private Pull pull = null;
    private Push push = null;
    private String status;
    private StringBuilder statusTrace;

    // Static Variables.
    // Constructors.
    private Sync() {
        // Default Constructor.
    }

    // The Usable Constructor.
    public Sync(CloudDB cdb) {
        try {
            // Here we will Initialize the Required Variables and States.
            this.cdb = cdb;
            statusTrace = new StringBuilder();
        } catch (Exception e) {
            // There was an Error.
        }
    }

    // Methods.
    // To be used to pull data from the server.
    public Pull pull() {
        try {
            // Here we create the Pull Object.
            pull = new Pull();
            return pull;
        } catch (Exception e) {
            // There was an Error.
            return null;
        }
    }

    // To be used to Push data from the server to the device.
    public Push push() {
        try {
            // Here we create the Pull Object.
            push = new Push();
            return push;
        } catch (Exception e) {
            // There was an Error.
            return null;
        }
    }

    // The whole sync environement will be divided into Pull and Push.
    // The Archieval Protocol comes here.
    public class Pull {

        // Variables.
        private String tableName;
        private Select select;
        private TableDataSync.TableTenancyType tenancyType;

        // Constructors.
        protected Pull() {
            // Default Constructor.
        }

        // Methods.
        // Lets set the Table name.
        public Pull fromTable(String tableName) {
            try {
                // Here we will Validate and Set the Table name.
                // Validate the Table Name.
                if (cdb.isNormalUser()) {
                    // The user is a normal user.
                    select = new Select(cdb)
                                        .from(tableName);
                    return this;
                } else {
                    this.tableName = null;
                    return null;
                }
            } catch (Exception e) {
                // There was an Error.
                this.tableName = null;
                return null;
            }
        }

        // Lets set the System Table.
        public Pull fromSystemTable(String tableName) {
            try {
                // Here we will Validate and Set the Table name.
                // Validate the Table Name.
                if (cdb.isNormalUser()) {
                    // The user is a normal user.
                    select = new Select(cdb)
                                        .fromSystem(Sync.this, tableName);
                    return this;
                } else {
                    this.tableName = null;
                    return null;
                }
            } catch (Exception e) {
                // There was an Error.
                this.tableName = null;
                return null;
            }
        }

        // Lets now set the Tenancy Type.
        public Pull setTenancyType(TableDataSync.TableTenancyType tenancyType) {
            try {
                // Here we will set the tenancy Type.
                switch (tenancyType) {
                    case NONE:
                        // There is no Tenancy Type.
                        break;
                    case DATABASE:
                        // It is the Database ID Tenancy Type.
                        select.addWhere(new Where("database_id", Where.Type.EQUAL, cdb.getDatabaseID()));
                        break;
                    case RLS:
                        // It is the RLS ID Tenancy Type.
                        //select.addWhere(new Where("database_id", Where.Type.EQUAL, cdb.getUser().ge));
                        break;
                    case TENANT:
                        // It is the Tenant ID Tenancy Type.
                        select.addWhere(new Where("tenant_id", Where.Type.EQUAL, cdb.getUser().getTenantID()));
                        select.addWhere(new Where(false, "tenant_id", Where.Type.EQUAL, 0));
                        break;
                    case USER:
                        // It is the User ID Tenancy Type.
                        select.addWhere(new Where("user_id", Where.Type.EQUAL, cdb.getUser().getUserID()));
                        break;
                    default:
                        // There is no Tenancy type That matches.
                        break;
                }
                return this;
            } catch (Exception e) {
                // There was an Error.
                return null;
            }
        }

        // Lets now Execute the Query.
        public ResultSet execute() {
            try {
                // Here we will Execute the Query and Return the ResultSet.
                return select.getPrepStmtCore().executeQuery();
            } catch (Exception e) {
                // There was an Error.
                return null;
            }
        }

    }

    // The Data Push System will Come here.
    public class Push {

        // Variables.
        private JSONArray devicePushTokens;
        private Object message;

        // Constructor.
        public Push() {
            try {
                // Lets initialize the required variables.
                message = "";
                devicePushTokens = new JSONArray();
            } catch (Exception e) {
                // There was an Error.
            }
        }

        // Methods.
        // Lets allow the system to do an autobuild of the device tokens to be sent to using the RLS of the Row.
        // This is the Real Method.
        public void sendDataWithRLS(final JSONObject msgJObj, final short rlsType, final long rlsID, final boolean isSystemTable) {
            try {
                // Here we will Sync the data with all the other devices and other users.
                // We will start pushing the data to the devices & users.
                try {
                    // Here we will push data to the devices & the users.
                    // Lets get all the device ids & tokens for the data to be pushed.
                    CloudDB sysCdb = new CloudDB();
                    Connection conn = sysCdb.getCoreConn();
                    StringBuilder sqlBuild = new StringBuilder();
                    try {
                        // Create the SQL Statement.
                        switch (rlsType) {
                            case 1:
                                // Its a role type RLS.
                                sqlBuild.append("SELECT user_role_map.user_id as role_user_id, ")
                                                    .append("user_role_map.role_id as role_id, ")
                                                    .append("devices.device_uid as device_uid, ")
                                                    .append("devices.user_id as device_user_id, ")
                                                    .append("devices.push_token as push_token ")
                                                    .append("FROM user_role_map ")
                                                    .append("INNER JOIN devices ON user_role_map.user_id = devices.user_id ")
                                                    .append("WHERE user_role_map.role_id = ?");
                                break;
                            case 2:
                                // Its a group type RLS.
                                sqlBuild.append("SELECT user_group_map.user_id as group_user_id, ")
                                                    .append("user_group_map.group_id as group_id, ")
                                                    .append("devices.device_uid as device_uid, ")
                                                    .append("devices.user_id as device_user_id, ")
                                                    .append("devices.push_token as push_token ")
                                                    .append("FROM user_group_map ")
                                                    .append("INNER JOIN devices ON user_group_map.user_id = devices.user_id ")
                                                    .append("WHERE user_group_map.group_id = ?");
                                break;
                            default:
                                break;
                        }
                        // Lets build the statement.
                        // Lets prepare the statement.
                        PreparedStatement prepStmt = conn.prepareStatement(sqlBuild.toString());
                        prepStmt.setLong(1, rlsID);
                        // Lets execute the query.
                        ResultSet deviceRes = prepStmt.executeQuery();
                        if (deviceRes != null) {
                            // The devices result is not null.
                            if (deviceRes.isBeforeFirst()) {
                                // There are devices to be retrieved.
                                // Lets add the Devices to be sent.
                                while (deviceRes.next()) {
                                    // Lets read from the dataset.
                                    addDeviceToken(deviceRes.getString("push_token"));
                                }
                                // Now lets set the message to be sent.
                                putMessage(msgJObj);
                                // Now lets send the message to the devices using Cloud Push Service.
                                internalExecutor(true);
                            }
                        }
                    } catch (Exception e) {
                        // There was an Error.
                    }
                    // Close the Connection.
                    sysCdb.close();
                } catch (Exception e) {
                    // There was an Error.
                    addStatus("Err : " + Helper.Error.getErrorMessage(e));
                }
            } catch (Exception e) {
                // There was an Error.
                addStatus("Main Err : " + Helper.Error.getErrorMessage(e));
            }
        }

        // This method will be used as a default.
        public void sendDataWithRLS(final JSONObject msgJObj, final short rlsType, final long rlsID) {
            sendDataWithRLS(msgJObj, rlsType, rlsID, false);
        }

        // Lets allow the system to do an autobuild of the devices to be synced using the User ID.
        // This is the Real Method.
        public void sendDataWithUserID(final JSONObject msgJObj, final UUID userID, final boolean isSystemTable) {
            try {
                // Here we will find the devices to sync with and push the data to those devices.
                try {
                    // Here we will push data to the devices & the users.
                    // Lets get all the device ids & tokens for the data to be pushed.
                    CloudDB sysCdb = new CloudDB();
                    Connection conn = sysCdb.getCoreConn();
                    try {
                        // Create the SQL Statement.
                        String sql = "SELECT device_uid, user_id, type, push_token"
                                            + " FROM devices WHERE user_id = ?";

                        // Lets build the statement.
                        // Lets prepare the statement.
                        PreparedStatement prepStmt = conn.prepareStatement(sql);
                        prepStmt.setObject(1, userID);
                        // Lets execute the query.
                        ResultSet deviceRes = prepStmt.executeQuery();
                        if (deviceRes != null) {
                            // The devices result is not null.
                            if (deviceRes.isBeforeFirst()) {
                                // There are devices to be retrieved.
                                // Lets add the Devices to be sent.
                                while (deviceRes.next()) {
                                    // Lets read from the dataset.
                                    addDeviceToken(deviceRes.getString("push_token"));
                                }
                                // Now lets set the message to be sent.
                                putMessage(msgJObj);
                                // Now lets send the message to the devices using Cloud Push Service.
                                internalExecutor(true);
                            }
                        }
                    } catch (Exception e) {
                        // There was an Error.
                    }
                    // Close the Connection.
                    sysCdb.close();
                } catch (Exception e) {
                    // There was an Error.
                    addStatus("Err : " + Helper.Error.getErrorMessage(e));
                }
            } catch (Exception e) {
                // There was an Error.
            }
        }

        // This method will be used as a default.
        public void sendDataWithUserID(final JSONObject msgJObj, final UUID userID) {
            sendDataWithUserID(msgJObj, userID, false);
        }

        // Lets allow the system to do an autobuild of the devices using the tenant id.
        // This is the Real Method.
        public void sendDataWithTenantID(final JSONObject msgJObj, final long tenantID, final boolean isSystemTable) {
            try {
                // Here we will find the devices to sync with and push the data to those devices.
                try {
                    // Here we will push data to the devices & the users.
                    // Lets get all the device ids & tokens for the data to be pushed.
                    CloudDB sysCdb = new CloudDB();
                    Connection conn = sysCdb.getCoreConn();
                    try {
                        // Create the SQL Statement.
                        String sql = "SELECT users.user_id as users_user_id, "
                                            + "users.tenant_id as tenant_id, "
                                            + "users.database_id as users_database_id, "
                                            + "users.is_active as is_active, "
                                            + "devices.device_uid as device_uid, "
                                            + "devices.user_id as device_user_id, "
                                            + "devices.push_token as device_push_token, "
                                            + "devices.device_id as device_id "
                                            + "FROM users "
                                            + "INNER JOIN devices ON users.user_id = devices.user_id "
                                            + "WHERE users.tenant_id = ?";

                        // Lets build the statement.
                        // Lets prepare the statement.
                        PreparedStatement prepStmt = conn.prepareStatement(sql);
                        prepStmt.setLong(1, tenantID);
                        // Lets execute the query.
                        ResultSet deviceRes = prepStmt.executeQuery();
                        if (deviceRes != null) {
                            // The devices result is not null.
                            if (deviceRes.isBeforeFirst()) {
                                // There are devices to be retrieved.
                                // Lets add the Devices to be sent.
                                while (deviceRes.next()) {
                                    // Lets read from the dataset.
                                    addDeviceToken(deviceRes.getString("push_token"));
                                }
                                // Now lets set the message to be sent.
                                putMessage(msgJObj);
                                // Now lets send the message to the devices using Cloud Push Service.
                                internalExecutor(true);
                            }
                        }
                    } catch (Exception e) {
                        // There was an Error.
                    }
                    // Close the Connection.
                    sysCdb.close();
                } catch (Exception e) {
                    // There was an Error.
                    addStatus("Err : " + Helper.Error.getErrorMessage(e));
                }
            } catch (Exception e) {
                // There was an Error.
            }
        }

        // This method will be used as a default.
        public void sendDataWithTenantID(final JSONObject msgJObj, final long tenantID) {
            sendDataWithTenantID(msgJObj, tenantID, false);
        }

        // Lets sync the data with all the devices in a database.
        // This is the Real Method.
        public void sendDataWithDatabaseID(final JSONObject msgJObj, final String databaseID, final boolean isSystemTable) {
            try {
                // Here we will find the devices to sync with and push the data to those devices.
                try {
                    // Here we will push data to the devices & the users.
                    // Lets get all the device ids & tokens for the data to be pushed.
                    CloudDB sysCdb = new CloudDB();
                    Connection conn = sysCdb.getCoreConn();
                    try {
                        // Create the SQL Statement.
                        String sql = "SELECT device_uid, user_id, database_id, type, push_token"
                                            + " FROM devices WHERE database_id = ?";

                        // Lets build the statement.
                        // Lets prepare the statement.
                        PreparedStatement prepStmt = conn.prepareStatement(sql);
                        prepStmt.setString(1, databaseID);
                        // Lets execute the query.
                        ResultSet deviceRes = prepStmt.executeQuery();
                        if (deviceRes != null) {
                            // The devices result is not null.
                            if (deviceRes.isBeforeFirst()) {
                                // There are devices to be retrieved.
                                // Lets add the Devices to be sent.
                                while (deviceRes.next()) {
                                    // Lets read from the dataset.
                                    addDeviceToken(deviceRes.getString("push_token"));
                                }
                                // Now lets set the message to be sent.
                                putMessage(msgJObj);
                                // Now lets send the message to the devices using Cloud Push Service.
                                internalExecutor(true);
                            }
                        }
                    } catch (Exception e) {
                        // There was an Error.
                    }
                    // Close the Connection.
                    sysCdb.close();
                } catch (Exception e) {
                    // There was an Error.
                    addStatus("Err : " + Helper.Error.getErrorMessage(e));
                }
            } catch (Exception e) {
                // There was an Error.
            }
        }

        // This method will be used as a default.
        public void sendDataWithDatabaseID(final JSONObject msgJObj, final String databaseID) {
            sendDataWithDatabaseID(msgJObj, databaseID, false);
        }

        // Lets add the Tokens of the Devices.
        public Push addDeviceToken(String pushToken) {
            try {
                // Add the Device Push token.
                if (pushToken != null && pushToken.length() == 128) {
                    // Its a Valid Push Token.
                    if (devicePushTokens.contains(pushToken)) {
                        // We do not need to add this push token.
                    } else {
                        // This is the First time being added.
                        devicePushTokens.add(pushToken);
                    }
                    return this;
                } else {
                    // It is not a valid push token.
                    return null;
                }
            } catch (Exception e) {
                // There was an Error.
                return null;
            }
        }

        // Add the Message to be delivered.
        public Push putMessage(Object message) {
            try {
                // Check if message is not empty.
                if (message != null) {
                    // The Message is not empty.
                    this.message = message;
                    return this;
                } else {
                    // The message is null.
                    return null;
                }
            } catch (Exception e) {
                // There was an Error.
                return null;
            }
        }

        // Now we will send the Data to Cloud Push for Delivery.
        public void execute() {
            try {
                // Here we send the message to Cloud Push
                internalExecutor(false);
            } catch (Exception e) {
                // There was an Error.
            }
        }

        // Now we will send the Data to Cloud Push for Delivery Asynchronisely.
        public void executeAsync() {
            try {
                // Here we send the message to Cloud Push Async.
                internalExecutor(true);
            } catch (Exception e) {
                // There was an Error.
            }
        }

        // This is the actual method that handles sending the Data to Cloud Push.
        private void internalExecutor(boolean executeAsync) {
            try {
                // Lets Send the Data.		
                Waiter waiter = new Waiter()
                                    .url(C.Clust.CLOUD_PUSH_IP)
                                    .endpoint("Send/Message")
                                    .method(Waiter.CallMethod.POST)
                                    .addParam(new ParamData("device_tokens").put(devicePushTokens))
                                    .addParam(new ParamData("message").put(message));
                // Now we will execute the Waiter according to the Caller's request.
                if (executeAsync) {
                    // The Caller wants to execute asynchronisely.
                    waiter.executeAsync();
                } else {
                    // The Caller wants to execute synchronisely.
                    waiter.execute();
                }
            } catch (Exception e) {
                // There was an Error.
                addStatus("Executor Err : " + Helper.Error.getErrorMessage(e));
            }
        }
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
}
