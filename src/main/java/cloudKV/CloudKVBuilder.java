/*
 * 
 * 36TH ELEMENT LICENSE 1.0
 *
 * This is a project of 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * This project is a closed source and proprietary software package.
 * None of the contents of this software is to be used for uses not intended,
 * And no one is to interface with the software in methods not defined or previously decided by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * No changes should be done to this project without prior authorization by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 2018 (C) 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 
 * 
 */
package cloudKV;

import helpers.C;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author karan This Class will Help Build in Building an Instance of the Cloud
 * Key Value Database.
 *
 */
public class CloudKVBuilder {

    // Variables.
    // Static Variables
    // Constructors.
    private CloudKVBuilder() {
        // We don't need this Constructor.
    }

    // Methods.
    public static CloudKV CreateInstance(String apiKey) {
        try {
            // Here we will Validate with the database if the API Key is Correct.
            Connection conn = null;
            try {
                // Here we will Connect with the database.
                Class.forName("org.postgresql.Driver");

                conn = DriverManager.getConnection("jdbc:postgresql://"
                                    + C.Clust.DB_CLUSTER_IP
                                    + "/"
                                    + C.Clust.CLOUD_KV
                                    + "?sslmode=" + C.Clust.SSL_STATUS
                                    + "&loadBalanceHosts=true", C.Clust.CLOUD_KV_USER, "");
            } catch (Exception er) {
                // There was an Error.
            }

            // Here we will Check if the API Key is Valid.
            try {
                // Here we will Validate the API Key.
                if (conn == null) {
                    // There is no connection.
                    return null;
                } else {
                    // There is a connection, lets Get the API Key.
                    // Create the SQL.
                    String apiKeyValidateSQL = "SELECT * FROM api_keys WHERE api_key = ?";
                    // Create the Prepared Statement.
                    PreparedStatement apiKeyPrepStmt = conn.prepareStatement(apiKeyValidateSQL);
                    apiKeyPrepStmt.setString(1, apiKey);
                    // Execute the Query.
                    ResultSet apiKeyRes = apiKeyPrepStmt.executeQuery();

                    // Check if there is an API Key, that matches this API Key.
                    if (apiKeyRes.isBeforeFirst()) {
                        // There is an API Key.
                        // Get the API Key ID from the ResultSet.
                        apiKeyRes.next();
                        long apiKeyID = apiKeyRes.getLong("id");
                        // Create the Cloud Key Value Object.
                        CloudKV ckv = new CloudKV(apiKeyID);
                        // Set the Connection.
                        ckv.setConn(conn);
                        return ckv;
                    } else {
                        // There is no API Key that Matches.
                        return null;
                    }
                }
            } catch (Exception er) {
                // There was an Error.
                return null;
            }
        } catch (Exception er) {
            // There was an Error.
            return null;
        }
    }

}
