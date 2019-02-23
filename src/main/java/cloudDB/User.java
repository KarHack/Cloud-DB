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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author karan
 */
public class User {

    // Variables.
    private String status;
    private String databaseID = null;
    private long tenantID = 0;
    private UUID userUID;
    private String token;   // This is just the User Token. (160 Characters)
    private HashMap<String, Device> devices;	// Storing all the Devices related to the User.	// 128 Char Device Token
    private HashMap<Long, short[]> roles;	// Storing all the Roles and Their CRUDs. (F-RLS)
    private HashMap<Long, short[]> groups;	// Storing all the Groups and Their CRUDs. (F-RLS)
    private HashMap<String, short[]> tableCrud;
    private String[] dbCredentials;
    private String timeStamp;
    private boolean isActive;

    // Constructor.
    private User() {
        // We do not require this constructor.
        // Initiate the required variables.
        devices = new HashMap<>();
        roles = new HashMap<>();
        groups = new HashMap<>();
        dbCredentials = new String[2];
        tableCrud = new HashMap<>();
    }

    // Constructor to Set the required details.
    User(UUID userUID, String token, Device device, String databaseID, long tenantID) {
        // Set the required data.
        this.userUID = userUID;
        this.token = token;
        this.databaseID = databaseID;
        this.tenantID = tenantID;
        // Initiate the required variables.
        devices = new HashMap<>();
        roles = new HashMap<>();
        groups = new HashMap<>();
        dbCredentials = new String[2];
        tableCrud = new HashMap<>();
        // Add the Device Token into the System.
        try {
            // Now we will try to put the device.
            devices.put(device.getDeviceToken(), device);
        } catch (Exception e) {
            // There was an Error.
        }
    }

    // Get the User ID.
    public UUID getUserID() {
        return userUID;
    }

    // Get the User Token.
    public String getToken() {
        return token;
    }

    // Lets clear the caches.
    // Clear the Device.
    public boolean removeDevice(String token) {
        // Lets remove the device.
        return devices.remove(token) != null;
    }

    // Remove all devices.
    public boolean removeAllDevices() {
        // Lets Remove all the Devices.
        devices.clear();
        return devices.isEmpty();
    }

    // Clear the Tables ACL.
    public boolean removeTableACL(String tableID) {
        // Lets remove the Table.
        return tableCrud.remove(tableID) != null;
    }

    // Remove all the Tables.
    public boolean removeAllTableACLs() {
        // Lets Remove all the Tables.
        tableCrud.clear();
        return tableCrud.isEmpty();
    }

    // Clear the Roles F-RLS.
    public boolean removeRolsRLS(long roleID) {
        // Lets remove the Role.
        return roles.remove(roleID) != null;
    }

    // Remove all the Roles.
    public boolean removeAllRoles() {
        // Lets Remove all the Roles.
        roles.clear();
        return roles.isEmpty();
    }

    // Clear the Group F-RLS.
    public boolean removeGroupRLS(long groupID) {
        // Lets remove the Groups.
        return groups.remove(groupID) != null;
    }

    // Remove all the Groups
    public boolean removeAllGroups() {
        // Lets Remove all the Groups.
        groups.clear();
        return groups.isEmpty();
    }

    // Attach the tenant id.
    void setTenantID(long tenantID) {
        this.tenantID = tenantID;
    }

    // Get the tenant id.
    public long getTenantID() {
        return tenantID;
    }

    // Get the Database id.
    public String getDatabaseID() {
        return databaseID;
    }

    // Get the Status of the User.
    public String getStatus() {
        return status;
    }

    // Get Device Info.
    public Device getDevice(String deviceToken) {
        try {
            // Lets Search for the Device in the Devices List.
            return devices.get(deviceToken);
        } catch (Exception e) {
            // There was an Error.
            return null;
        }
    }

    // Get all the Devices of the user.
    public Device[] getDevices() {
        try {
            // Get all the Devices from the Map.
            Iterator deviceNames = devices.keySet().iterator();
            List<Device> deviceArr = new ArrayList<>();
            while (deviceNames.hasNext()) {
                // Get the Device from the HashMap.
                deviceArr.add(devices.get(deviceNames.next().toString()));
            }
            // Return the Devices from the Arraylist.
            return deviceArr.toArray(new Device[deviceArr.size()]);
        } catch (Exception e) {
            // There was an Error.
            return new Device[0];
        }
    }

    // Get the Device Map.
    public HashMap<String, Device> getDeviceMap() {
        try {
            // Lets Search for the Device in the Devices List.
            return devices;
        } catch (Exception e) {
            // There was an Error.
            return new HashMap<>();
        }
    }

    // Add a device to the system.
    boolean addDevice(String deviceToken, Device device) {
        try {
            // Here ew will add the Device to the Cache.
            if (deviceToken.length() == 128) {
                devices.put(deviceToken, device);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Set the DB Credentials.
    void setDatabaseCred(String username, String password) {
        this.dbCredentials[0] = username;
        this.dbCredentials[1] = password;
    }

    // Get the DB Credentials.
    String[] getDatabaseCred() {
        return dbCredentials;
    }

    // Set the Required CRUDs for the Tables.
    // Set the CRUDs for the Roles.
    void putRoleRLS(long roleID, short[] roleRLS) {
        try {
            // Set the role into the cache.
            roles.put(roleID, roleRLS);
        } catch (Exception e) {
            // There was an Error.
        }
    }

    void setRoleRLS(HashMap<Long, short[]> roleRLS) {
        this.roles = roleRLS;
    }

    // Get the CRUDs for a Role.
    public short[] getRole(long roleID) {
        return roles.get(roleID);
    }

    // Get all the Roles in the User Object.
    HashMap<Long, short[]> getRoleMap() {
        return roles;
    }

    // Get all the Groups in the User Object.
    HashMap<Long, short[]> getGroupMap() {
        return groups;
    }

    // Set the CRUDs for the Roles.
    void putGroupRLS(long groupID, short[] groupRLS) {
        try {
            // Set the role into the cache.
            groups.put(groupID, groupRLS);
        } catch (Exception e) {
            // There was an Error.
        }
    }

    void setGroupRLS(HashMap<Long, short[]> groupRLS) {
        this.groups = groupRLS;
    }

    // Get the CRUDs for a Role.
    public short[] getGroup(long groupID) {
        return groups.get(groupID);
    }

    // Get the CRUD of the user with respect to the table.
    // Here we validate the Read from the table.
    boolean validateCRUD(String tableName, short crud) {
        try {
            if (tableCrud.containsKey(tableName)) {
                // The user is a normal user.
                // Lets validate this user.
                status = "User Table Found";
                return tableCrud.get(tableName)[crud] == 1;
            } else {
                status = "User Table Not Found";
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Here we validate the Priviledge of the User in that Role.
    boolean validateRowRLS(long roleID, short crud) {
        try {
            if (roles.containsKey(roleID)) {
                // The user is a normal user.
                // Lets validate this user.
                return roles.get(roleID)[crud] == 1;
            } else {
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Here we validate the Priviledge of the User in that Group.
    boolean validateGroupRLS(long groupID, short crud) {
        try {
            if (groups.containsKey(groupID)) {
                // The user is a normal user.
                // Lets validate this user.
                return groups.get(groupID)[crud] == 1;
            } else {
                return false;
            }
        } catch (Exception e) {
            // There was an Error.
            return false;
        }
    }

    // Here we will set the ACL CRUD for the tables.
    boolean putTableACL(String tableID, short[] tableCRUD) {
        try {
            // Set the CRUD.
            tableCrud.put(tableID, tableCRUD);
            return true;
        } catch (Exception e) {
            // There was an Error
            return false;
        }
    }

    // Here we will set the ACL CRUD of all the tables in bulk.
    void setTableACL(HashMap<String, short[]> tableCRUD) {
        this.tableCrud = tableCRUD;
    }

    // Set the Timestamp.
    void setTimeStamp(String timestamp) {
        this.timeStamp = timestamp;
    }

    // Get the Timestamp of this user.
    public String getTimeStamp() {
        return timeStamp;
    }

    // Get and set the User Active Flag.
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    // Lets Get the Data of this Object in a JSON Object.
    public JSONObject toJSONObject() {
        try {
            // Here we will Convert this Object into a JSON Object.
            JSONObject jObj = new JSONObject();
            // Add User Data into the JSON Object.
            jObj.put("database_id", databaseID);
            jObj.put("tenant_id", tenantID);
            jObj.put("user_uid", userUID.toString());
            jObj.put("user_token", token);
            jObj.put("is_active", isActive);

            // Add the Database Credentials.
            JSONArray dbCredJArr = new JSONArray();
            dbCredJArr.add(dbCredentials[0]);

            dbCredJArr.add(dbCredentials[1]);

            jObj.put("db_credentials", dbCredJArr);

            // Add the Roles of the User into the JSON Object.
            try {
                // Here we will Get & Add the Roles of the User.
                JSONObject roleJObjs = new JSONObject();
                for (Map.Entry<Long, short[]> roleEntry : roles.entrySet()) {
                    // Now we will Get the Device data and add it to the Device JSON Object.
                    // Create the JSON Array.
                    JSONArray roleCRUDJArr = new JSONArray();
                    roleCRUDJArr.add(0, roleEntry.getValue()[0]);
                    roleCRUDJArr.add(1, roleEntry.getValue()[1]);
                    roleCRUDJArr.add(2, roleEntry.getValue()[2]);
                    roleCRUDJArr.add(3, roleEntry.getValue()[3]);
                    // Add to the Roles JSON Object.
                    roleJObjs.put(Long.parseLong(roleEntry.getKey().toString()), roleCRUDJArr);
                }
                // Add the Roles JSON Objects to the User JSON Object.
                jObj.put("roles", roleJObjs);
            } catch (Exception er) {
                // There was an Error.
            }

            // Add the Groups of the User into the JSON Object.
            try {
                // Here we will Get & Add the Groups of the User.
                JSONObject groupJObjs = new JSONObject();
                for (Map.Entry<Long, short[]> groupEntry : groups.entrySet()) {
                    // Now we will Get the Device data and add it to the Device JSON Object.
                    // Create the JSON Array.
                    JSONArray groupCRUDJArr = new JSONArray();
                    groupCRUDJArr.add(0, groupEntry.getValue()[0]);
                    groupCRUDJArr.add(1, groupEntry.getValue()[1]);
                    groupCRUDJArr.add(2, groupEntry.getValue()[2]);
                    groupCRUDJArr.add(3, groupEntry.getValue()[3]);
                    // Add to the Roles JSON Object.
                    groupJObjs.put(Long.parseLong(groupEntry.getKey().toString()), groupCRUDJArr);
                }
                // Add the Groups JSON Objects to the User JSON Object.
                jObj.put("groups", groupJObjs);
            } catch (Exception er) {
                // There was an Error.
            }

            // Add the Table CRUD ACL of the User into the JSON Object.
            try {
                // Here we will Get & Add the Table CRUD ACL of the User.
                JSONObject tableJObjs = new JSONObject();
                for (Map.Entry<String, short[]> tableEntry : tableCrud.entrySet()) {
                    // Now we will Get the Device data and add it to the Device JSON Object.
                    // Create the JSON Array.
                    JSONArray tableCRUDJArr = new JSONArray();
                    tableCRUDJArr.add(0, tableEntry.getValue()[0]);
                    tableCRUDJArr.add(1, tableEntry.getValue()[1]);
                    tableCRUDJArr.add(2, tableEntry.getValue()[2]);
                    tableCRUDJArr.add(3, tableEntry.getValue()[3]);
                    // Add to the Tables JSON Object.
                    tableJObjs.put(tableEntry.getKey(), tableCRUDJArr);
                }
                // Add the Tables JSON Objects to the User JSON Object.
                jObj.put("tables", tableJObjs);
            } catch (Exception er) {
                // There was an Error.
            }

            // Add the Devices into the JSON Object.
            try {
                // Here we will Get & Add the Devices.
                JSONObject deviceJObjs = new JSONObject();
                // Let Read from the HashMap.
                for (Map.Entry<String, Device> deviceEntry : devices.entrySet()) {
                    try {
                        deviceJObjs.put(deviceEntry.getKey(), deviceEntry.getValue().toJSONObject());
                    } catch (Exception er) {
                        // There was an Error.
                    }
                }
                // Add the Device JSON Objects to the User JSON Object.
                if (deviceJObjs != null || !deviceJObjs.isEmpty()) {
                    jObj.put("devices", deviceJObjs);
                }
            } catch (Exception er) {
                // There was an Error.
            }

            return jObj;
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }

    // Here we will Create an Instance of the User Object & Return it to the Caller.
    static User CreateInstance(JSONObject userJObj) {
        try {
            // Here we will Create the Object.
            User userObj = new User();
            // Add the Required Data.
            userObj.databaseID = userJObj.get("database_id").toString();
            userObj.tenantID = Long.parseLong(userJObj.get("tenant_id").toString());
            userObj.userUID = UUID.fromString(userJObj.get("user_uid").toString());
            userObj.token = userJObj.get("user_token").toString();
            userObj.isActive = Boolean.parseBoolean(userJObj.get("is_active").toString());

            // Add the Database Credentials.
            JSONArray dbCredJArr = (JSONArray) new JSONParser().parse(userJObj.get("db_credentials").toString());
            userObj.dbCredentials = new String[]{dbCredJArr.get(0).toString(), dbCredJArr.get(1).toString()};

            
            // Lets now add the Access Priviledges.
            // Add the Tables.
            try {
                // Create the Tables & Add them to the User Object.
                HashMap<String, short[]> tablesMap = new HashMap<>();
                JSONObject tableJObjs = (JSONObject) userJObj.get("tables");
                Object[] tableArr = tableJObjs.keySet().toArray();
                for (Object tableArr1 : tableArr) {
                    try {
                        // Here we will Get the Roles & Add to the HashMap.
                        String key = tableArr1.toString();
                        JSONArray tableJArr = (JSONArray) tableJObjs.get(key);
                        short[] tableCRUD = new short[4];
                        tableCRUD[0] = Short.parseShort(tableJArr.get(0).toString());
                        tableCRUD[1] = Short.parseShort(tableJArr.get(1).toString());
                        tableCRUD[2] = Short.parseShort(tableJArr.get(2).toString());
                        tableCRUD[3] = Short.parseShort(tableJArr.get(3).toString());
                        tablesMap.put(key, tableCRUD);
                    } catch (Exception er) {
                        // There was an Error.
                    }
                }
                userObj.tableCrud = tablesMap;
            } catch (Exception er) {
                // There was an Error.
            }
            
            // Add the Roles.
            try {
                // Create the Roles & Add them to the User Object.
                HashMap<Long, short[]> rolesMap = new HashMap<>();
                JSONObject roleJObjs = (JSONObject) userJObj.get("roles");
                Object[] roleArr = roleJObjs.keySet().toArray();
                for (Object roleArr1 : roleArr) {
                    try {
                        // Here we will Get the Roles & Add to the HashMap.
                        String key = roleArr1.toString();
                        JSONArray roleJArr = (JSONArray) roleJObjs.get(key);
                        short[] roleCRUD = new short[4];
                        roleCRUD[0] = Short.parseShort(roleJArr.get(0).toString());
                        roleCRUD[1] = Short.parseShort(roleJArr.get(1).toString());
                        roleCRUD[2] = Short.parseShort(roleJArr.get(2).toString());
                        roleCRUD[3] = Short.parseShort(roleJArr.get(3).toString());
                        rolesMap.put(Long.parseLong(key), roleCRUD);
                    }catch (Exception er) {
                        // There was an Error.
                    }
                }
                userObj.roles = rolesMap;
            } catch (Exception er) {
                // There was an Error.
            }
            
            // Add the Groups.
            try {
                // Create the Groups & Add them to the User Object.
                HashMap<Long, short[]> groupsMap = new HashMap<>();
                JSONObject groupJObjs = (JSONObject) userJObj.get("groups");
                Object[] groupArr = groupJObjs.keySet().toArray();
                for (Object groupArr1 : groupArr) {
                    try {
                        // Here we will Get the Roles & Add to the HashMap.
                        String key = groupArr1.toString();
                        JSONArray groupJArr = (JSONArray) groupJObjs.get(key);
                        short[] groupCRUD = new short[4];
                        groupCRUD[0] = Short.parseShort(groupJArr.get(0).toString());
                        groupCRUD[1] = Short.parseShort(groupJArr.get(1).toString());
                        groupCRUD[2] = Short.parseShort(groupJArr.get(2).toString());
                        groupCRUD[3] = Short.parseShort(groupJArr.get(3).toString());
                        groupsMap.put(Long.parseLong(key), groupCRUD);
                    } catch (Exception er) {
                        // There was an Error.
                    }
                }
                userObj.groups = groupsMap;
            } catch (Exception er) {
                // There was an Error.
            }
                        
            // Add the Devices.
            try {
                // Create the Objects & Add to the User Object.
                HashMap<String, Device> devicesMap = new HashMap<>();
                JSONObject deviceJObjs = (JSONObject) userJObj.get("devices");
                Object[] deviceArr = deviceJObjs.keySet().toArray();
                for (Object deviceArr1 : deviceArr) {
                    try {
                        // Here we will Generate the Device JSON, and Add it to the Devices Map.
                        String key = deviceArr1.toString();
                        JSONObject deviceJObj = (JSONObject) deviceJObjs.get(key);
                        Device deviceObj = Device.CreateInstance(deviceJObj);
                        devicesMap.put(key, deviceObj);
                    } catch (Exception er) {
                        // There was an Error.
                    }
                }
                userObj.devices = devicesMap;
            } catch (Exception er) {
                // There was an Error.
            }
            
            return userObj;
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }

}
