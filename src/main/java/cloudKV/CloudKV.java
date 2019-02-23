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
package cloudKV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author karan
 */
public class CloudKV {

    // Variables.
    private String status;
    private StringBuilder statusTrace;
    private long apiKeyID = 0;
    private Connection conn;

    // Constructors.
    // The Default Constructor.
    private CloudKV() {
        // We don't need this Constructor.
    }

    // The API Key ID Constructor.
    CloudKV(long apiKeyIDL) {
        try {
            // Here we will Set the API Key ID.
            this.apiKeyID = apiKeyIDL;
            addStatus("Starting the Cloud KV");
        } catch (Exception er) {
            // There was an Error.
        }
    }

    // Methods.
    // Set the Database Connection.
    boolean setConn(Connection connection) {
        try {
            // Set the Database Connection.
            this.conn = connection;
            return true;
        } catch (Exception er) {
            // There was an Error.
            return false;
        }
    }

    // Put into the database. (Upsert)
    public boolean put(String key, Object value) {
        try {
            // Here we will Set the data into the database.
            // Create the SQL.
            String sql = "UPSERT INTO key_value_pairs (api_id, key, value) VALUES (?, ?, ?)";
            // Prepare the Statement.
            PreparedStatement kvPrepStmt = conn.prepareStatement(sql);
            kvPrepStmt.setLong(1, apiKeyID);
            kvPrepStmt.setString(2, key);
            kvPrepStmt.setString(3, value.toString());
            // Execute the Query.
            int kvPut = kvPrepStmt.executeUpdate();
            return kvPut > 0;
        } catch (Exception er) {
            // There was an Error.
            addStatus("Put Err : " + er.getLocalizedMessage());
            return false;
        }
    }

    // Contains in the Database.
    public boolean contains(String key) {
        try {
            // Here we will Check if the data exists the database.
            // Create the SQL.
            String sql = "SELECT api_id, key FROM key_value_pairs WHERE api_id = ? AND key = ?";
            // Prepare the Statement.
            PreparedStatement kvPrepStmt = conn.prepareStatement(sql);
            kvPrepStmt.setLong(1, apiKeyID);
            kvPrepStmt.setString(2, key);
            // Execute the Query.
            ResultSet kvContains = kvPrepStmt.executeQuery();
            return kvContains.isBeforeFirst();
        } catch (Exception er) {
            // There was an Error.
            addStatus("Contains Err : " + er.getLocalizedMessage());
            return false;
        }
    }

    // Get from the Database. (Handle different types of Gets.)
    // The Main Get.
    private Object getResult(String key) {
        try {
            // Here we will Get the Result Set of the Data.
            // Create the SQL.
            String sql = "SELECT api_id, key, value FROM key_value_pairs WHERE api_id = ? AND key = ?";
            // Prepare the Statement.
            PreparedStatement kvPrepStmt = conn.prepareStatement(sql);
            kvPrepStmt.setLong(1, apiKeyID);
            kvPrepStmt.setString(2, key);
            // Execute the Query.
            ResultSet kvGet = kvPrepStmt.executeQuery();
            if (kvGet.isBeforeFirst()) {
                // There is a Key Value to Give in return.
                kvGet.next();
                return kvGet.getObject("value");
            } else {
                // There is no Value to Give.
                return null;
            }
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }

    // Default (Normal) Get
    public Object get(String key) {
        try {
            // Here we will Get the data from the database.
            return getResult(key);
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }

    // The Get String.
    public String getString(String key) {
        try {
            // Here we will Get the data from the database.
            Object kvObj = getResult(key);
            if (kvObj != null) {
                // Get the Data from the Object.
                return kvObj.toString();
            } else {
                // There is no Value to this Object.
                return null;
            }
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }

    // The Get in Short.
    public short getShort(String key) {
        try {
            // Here we will Get the data from the database.
            Object kvObj = getResult(key);
            if (kvObj != null) {
                // Get the Data from the Object.
                return Short.parseShort(kvObj.toString());
            } else {
                // There is no Value to this Object.
                return -1;
            }
        } catch (Exception er) {
            // There was an Error.
            return -1;
        }
    }

    // The Get in Short.
    public int getInt(String key) {
        try {
            // Here we will Get the data from the database.
            Object kvObj = getResult(key);
            if (kvObj != null) {
                // Get the Data from the Object.
                return Integer.parseInt(kvObj.toString());
            } else {
                // There is no Value to this Object.
                return -1;
            }
        } catch (Exception er) {
            // There was an Error.
            return -1;
        }
    }

    // The Get in Short.
    public float getFloat(String key) {
        try {
            // Here we will Get the data from the database.
            Object kvObj = getResult(key);
            if (kvObj != null) {
                // Get the Data from the Object.
                return Float.parseFloat(kvObj.toString());
            } else {
                // There is no Value to this Object.
                return -1;
            }
        } catch (Exception er) {
            // There was an Error.
            return -1;
        }
    }

    // The Get in Short.
    public long getLong(String key) {
        try {
            // Here we will Get the data from the database.
            Object kvObj = getResult(key);
            if (kvObj != null) {
                // Get the Data from the Object.
                return Long.parseLong(kvObj.toString());
            } else {
                // There is no Value to this Object.
                return -1;
            }
        } catch (Exception er) {
            // There was an Error.
            return -1;
        }
    }

    // The Get in Short.
    public double getDouble(String key) {
        try {
            // Here we will Get the data from the database.
            Object kvObj = getResult(key);
            if (kvObj != null) {
                // Get the Data from the Object.
                return Double.parseDouble(kvObj.toString());
            } else {
                // There is no Value to this Object.
                return -1;
            }
        } catch (Exception er) {
            // There was an Error.
            return -1;
        }
    }

    // Count the Number of the Entries in the database.
    public long count() {
        try {
            // Here we will Count the Number of Entries in the Database.
            // Create the SQL.
            String sql = "SELECT COUNT(*) FROM key_value_pairs WHERE api_id = ?";
            // Prepare the Statement.
            PreparedStatement kvPrepStmt = conn.prepareStatement(sql);
            kvPrepStmt.setLong(1, apiKeyID);
            // Execute the Query.
            ResultSet kvGet = kvPrepStmt.executeQuery();
            if (kvGet.isBeforeFirst()) {
                // Get the Count of the Keys.
                kvGet.next();
                return kvGet.getLong(1);
            } else {
                // There are no Keys.
                return 0;
            }
        } catch (Exception er) {
            // There was an Error.
            addStatus("Count Err : " + er.getLocalizedMessage());
            return 0;
        }
    }
    
    // Let the User Remove a Value using a Key.
    public boolean remove(String key) {
        try {
            // Here we will Remove the Keys.
            // Create the SQL.
            String sql = "DELETE FROM key_value_pairs WHERE api_id = ? AND key = ?";
            // Prepare the Statement.
            PreparedStatement kvPrepStmt = conn.prepareStatement(sql);
            kvPrepStmt.setLong(1, apiKeyID);
            kvPrepStmt.setString(2, key);
            // Execute the Query.
            int kvRemove = kvPrepStmt.executeUpdate();
            return kvRemove > 0;
        } catch (Exception er) {
            // There was an Error.
            return false;
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

    public boolean close() {
        try {
            // Lets Close the Connection to the database.
            conn.close();
            return true;
        } catch (Exception er) {
            // There was an Error.
            return false;
        }
    }
    
}
