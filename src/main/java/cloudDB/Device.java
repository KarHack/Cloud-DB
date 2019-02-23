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
import org.json.simple.JSONObject;
import security.Auth;

/**
 *
 * @author karan
 */
public class Device {

    // Variables.
    //private String status;
    private UUID deviceUID;
    private String token;	// This is the full token, that includes the User Token and the Device Token.	(256 Characters)
    private String deviceToken;	// This is just the device token.   (128 Characters)
    private String pushToken;
    private Type type;
    private String deviceID;
    private String timeStamp;
    private UUID userID;

    // Static variables.
    // Static Types.
    public enum Type {
        WINDOWS, WEB, ANDROID, IOS
    }

    // Constructors.
    // Default Constructor.
    private Device() {
        // Nothing to do here.
    }

    // Constructor that we require.
    public Device(UUID deviceUID, String token) {
        try {
            // Set the Required Items.
            this.deviceUID = deviceUID;
            this.token = token;
            String[] userDeviceTokens = Auth.extractToken(token);
            this.deviceToken = Auth.addUserPaddingToDeviceToken(userDeviceTokens[0], userDeviceTokens[1]);
        } catch (Exception e) {
            // There was an Error.
        }
    }

    public Device(UUID deviceUID, String token, String deviceToken) {
        try {
            // Set the Required Items.
            this.deviceUID = deviceUID;
            this.token = token;
            this.deviceToken = deviceToken;
        } catch (Exception e) {
            // There was an Error.
        }
    }

    // Required Methods.
    void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    // Here we get the required data.
    public UUID getDeviceUID() {
        return deviceUID;
    }

    public String getPushToken() {
        return pushToken;
    }

    public String getToken() {
        return token;
    }

    // This method automatically calculates the device token.
    void setToken(String token) {
        try {
            // Here we set the token and then calculate the device token as well.
            // Set the full token.
            this.token = token;
            String[] userDeviceTokens = Auth.extractToken(token);
            this.deviceToken = Auth.addUserPaddingToDeviceToken(userDeviceTokens[0], userDeviceTokens[1]);
        } catch (Exception e) {
            // There was an Error.
        }
    }

    // Get the device token.
    public String getDeviceToken() {
        return deviceToken;
    }

    // Set the Device type.
    void setType(Type type) {
        this.type = type;
    }

    // Get the Device type.
    public Type getType() {
        return type;
    }

    // Get and Set the Timestamp of this device.
    void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    // Set and Get the Device ID.
    void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    void setUserID(UUID userID) {
        this.userID = userID;
    }

    public UUID getUserID() {
        return userID;
    }

    // Convert this Device Object to a JSON Object.
    public JSONObject toJSONObject() {
        try {
            // Here we Create a JSON Object & return to the User.
            JSONObject deviceJObj = new JSONObject();
            // Add the Device Data to the JSON Object.
            deviceJObj.put("device_uid", deviceUID.toString());
            deviceJObj.put("token", token);
            deviceJObj.put("device_token", deviceToken);
            deviceJObj.put("push_token", pushToken);
            try {
                // Lets Try to Retrieve Less Necessary Objects.
                deviceJObj.put("device_id", deviceID);
                deviceJObj.put("user_id", userID.toString());
                deviceJObj.put("type", type.name());
            } catch (Exception er) {
                // There was an Error.
                // Let it be if we cannot retrieve these data points
            }
            return deviceJObj;
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }

    // Create an Instance of Device, using JSON Object.
    static Device CreateInstance(JSONObject deviceJObj) {
        try {
            // Here we will Create an Instance of the Device Object.
            Device device = new Device();
            device.deviceUID = UUID.fromString(deviceJObj.get("device_uid").toString());
            device.token = deviceJObj.get("token").toString();
            device.deviceToken = deviceJObj.get("device_token").toString();
            device.pushToken = deviceJObj.get("push_token").toString();
            try {
                // Lets Try to add Less Necessary Objects.
                device.deviceID = deviceJObj.get("device_id").toString();
                device.type = Type.valueOf(deviceJObj.get("type").toString());
                device.userID = UUID.fromString(deviceJObj.get("user_id").toString());
            } catch (Exception er) {
                // There was an Error.
            }
            return device;
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }

}
