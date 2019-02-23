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
package waiter;

import helpers.Helper;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 *
 * @author karan This Class will be responsible for Calling the Required API and
 * sending the Params. It will make sure that the API will be called. It will
 * also return the response to the user asynchronous using a callback. This
 * class will talk to the Waiter Manager. The waiter will keep a status of API
 * Call.
 *
 *
 */
public class Waiter {

    // Implementations
    /*
    // Simple Implementation of the Library.    
    new Waiter()
	   .url("URL OF THE API")
	   .endpoint("ENDPOINT OF THE API")
	   .method(Waiter.CallMethod.POST)
	   .addParam(new ParamData("Param Name").put("Param Value"))
	   .setOnResponseReceived(new ResponseListener() {
	       @Override
	       public void onDataChanged(boolean isSuccess, String response) {
		   try {
		       // Here we will receive the response of the user.

		   } catch (Exception e) {
		       // There was an Error.
		   }
	       }
	   })
	   .execute();  OR .executeAsync();  

     */
    // Variables
    private String url;
    private String endpoint;
    private ArrayList<ParamData> paramDatas;
    private String status;
    private StringBuilder statusTrace;
    private CallMethod callMethod = CallMethod.GET;
    private String id;
    private String data;
    private ResponseListener responseListener;
    private ProtocolType protocolType = ProtocolType.HTTP;

    // Set the Method Enums
    public enum CallMethod {
	GET, POST, PUT, DELETE
    }
    
    public enum ProtocolType {
	HTTP, HTTPS
    }

    public Waiter() {
	try {
	    // Initialize the Required Variables.
	    paramDatas = new ArrayList<>();
	    // Now we will communicate with the Waiter Manager to Clear all the Running Queries from the system.
	    id = WaiterManager.init(this);
	    // Add the status.
	    addStatus("Initialized");
	} catch (Exception e) {
	    // There was an Error.
	}
    }
    
    // Lets set the Protocol used for the api.
    // Set the Protocol to HTTP
    public Waiter http() {
	protocolType = ProtocolType.HTTP;
	return this;
    }
    
    // Set the Protocol to HTTPS
    public Waiter https() {
	protocolType = ProtocolType.HTTPS;
	return this;
    }

    // Lets set the URL of the API that needs to be called.
    public Waiter url(String url) {
	try {
	    // Lets set the API.
	    this.url = url;
	    // Add the status.
	    addStatus("URL Added");
	    // Lets update our status with the manager.
	    WaiterManager.update(this);
	    return this;
	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

    // Lets set the Endpoint of the API.
    public Waiter endpoint(String endpoint) {
	try {
	    // Lets set the API.
	    this.endpoint = endpoint;
	    addStatus("Endpoint Added");
	    // Lets update our status with the manager.
	    WaiterManager.update(this);
	    return this;
	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

    // Set the Method to be used to make the request.
    public Waiter method(CallMethod method) {
	try {
	    // Lets set the Method.
	    callMethod = method;
	    addStatus("Method Set");
	} catch (Exception e) {
	    // There was an Error.
	    addStatus("Method Not Set");
	}
	// Lets update our status with the manager.
	WaiterManager.update(this);
	return this;
    }

    // Lets allow the user to set the params that he wants to send to the API.
    // We will also allow the user to add params one at a time.
    public Waiter addParam(ParamData paramData) {
	try {
	    // Lets try to add the params to the list.
	    paramDatas.add(paramData);
	} catch (Exception e) {
	    // There was an Error.
	    addStatus("Param Err : " + Helper.Error.getErrorMessage(e));
	}
	// Lets update our status with the manager.
	WaiterManager.update(this);
	return this;
    }

    // The callback for the user.
    public Waiter setOnResponseReceived(ResponseListener responseListener) {
	this.responseListener = responseListener;
	return this;
    }

    // Lets Execute the Request.
    public boolean execute() {
	try {
	    // Here we will validate the run, and execute the query.
	    // Lets validate if the Request is allowed to be run.
	    boolean canExecute = true;
	    if (url == null || url.trim().length() == 0) {
		// The URL is not provided.
		canExecute = false;
	    }
	    // Lets run the Request if Allowed.
	    if (canExecute) {
		// Execute the Request.
		addStatus("Executing");
		// Setup the Waiter Manager to Insert into the database.
		WaiterManager.begin(this);
		// Lets create another thread.
		asyncStart();
		return true;
	    } else {
		// There was an Error, and we cannot execute the request.
		return false;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    return false;
	}
    }

    // Execute Request Async.
    public boolean executeAsync() {
	try {
	    // Here we will validate the run, and execute the query.
	    // Lets validate if the Request is allowed to be run.
	    boolean canExecute = true;
	    if (url == null || url.trim().length() == 0) {
		// The URL is not provided.
		canExecute = false;
	    }
	    // Lets run the Request if Allowed.
	    if (canExecute) {
		// Execute the Request.
		// Lets create another thread.
		addStatus("Executing Async");
		// Setup the Waiter Manager to Insert into the database.
		WaiterManager.begin(this);
		new Thread(new Runnable() {
		    @Override
		    public void run() {
			try {
			    // Here we will run the Async Function.
			    asyncStart();
			} catch (Exception e) {
			    // There was an Error.
			}
		    }
		}).start();
		return true;
	    } else {
		// There was an Error, and we cannot execute the request.
		return false;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    return false;
	}
    }

    // Required Getters and Setters.
    public String getID() {
	return id;
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

    // This will retrieve just the status of this object.
    public String getStatus() {
	return status;
    }

    /*
     * 
     * This class will make the Calls to the Server.
     * Only one instance of this Class will be Created.
     * The Waiter will call this class for Hitting the API.
     * 
     */
    // This will just call the Async task.
    private void asyncStart() {
	try {
	    // Now we will Run the Async Task.
	    StringBuilder paramBody = new StringBuilder();
	    // Here the request will be formed and sent to the server.
	    for (int paramCount = 0; paramCount < paramDatas.size(); paramCount++) {
		try {
		    // Here we get each param and add it to the string builder.
		    // To form the params body.
		    // Get the Param from the array.
		    ParamData param = paramDatas.get(paramCount);
		    paramBody.append(param.getParam()).append('=')
			    .append(URLEncoder.encode(param.getData().toString(), "UTF-8"));
		    if (paramCount < paramDatas.size() - 1) {
			paramBody.append('&');
		    }
		} catch (Exception e) {
		    // There was an Error.
		}
	    }
	    // Add the URL.
	    String urlStr = (protocolType == ProtocolType.HTTPS ? "https://" : "http://" ) + this.url
		    + (this.endpoint != null || this.endpoint.isEmpty() ? "/" + this.endpoint : "");

	    // Now we will Call the API According to the Method.
	    if (callMethod == CallMethod.POST) {
		try {
		    // The POST Version of API Calling.
		    URL obj = new URL(urlStr);
		    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		    //add reuqest header
		    con.setRequestMethod("POST");
		    con.setRequestProperty("User-Agent", "Mozilla/5.0");
		    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		    // Send post request
		    con.setDoOutput(true);
		    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		    wr.writeBytes(paramBody.toString());
		    wr.flush();
		    wr.close();

		    int responseCode = con.getResponseCode();
		    BufferedReader in = new BufferedReader(
			    new InputStreamReader(con.getInputStream()));
		    String inputLine;
		    StringBuilder response = new StringBuilder();

		    while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		    }
		    in.close();

		    // Return the Result.
		    if (responseListener != null) {
			responseListener.onDataChanged((responseCode == 200), response.toString());
		    }
		} catch (Exception e) {
		    // There was an Error.
		    addStatus("POST Err : " + Helper.Error.getErrorMessage(e));
		    if (responseListener != null) {
			responseListener.onDataChanged(false, "Err : " + Helper.Error.getErrorMessage(e));
		    }
		}
	    } else {
		try {
		    // The GET, PUT, DELETE Version of the API Calling.
		    urlStr = urlStr + ((paramDatas.size() > 0) ? "?" + paramBody.toString() : "");
		    URL urlObj = new URL(urlStr);
		    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

		    if (null != callMethod) // Method of API.
		    {
			switch (callMethod) {
			    case GET:
				conn.setRequestMethod("GET");
				break;
			    case PUT:
				conn.setRequestMethod("PUT");
				break;
			    case DELETE:
				conn.setRequestMethod("DELETE");
				break;
			    default:
				break;
			}
		    } else {
			// Set the Default Method be GET.
			conn.setRequestMethod("GET");
		    }

		    // Add request header
		    conn.setRequestProperty("User-Agent", "Mozilla/5.0");

		    int responseCode = conn.getResponseCode();
		    BufferedReader in = new BufferedReader(
			    new InputStreamReader(conn.getInputStream()));
		    String inputLine;
		    StringBuilder response = new StringBuilder();

		    while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		    }
		    in.close();

		    // The response of the API.
		    // Lets return the response to the user, if he is waiting for it.
		    if (responseListener != null) {
			responseListener.onDataChanged((responseCode == 200), response.toString());
		    }
		} catch (Exception e) {
		    // There was an Error.
		    addStatus(callMethod.toString() + " Err : " + Helper.Error.getErrorMessage(e));
		    if (responseListener != null) {
			responseListener.onDataChanged(false, "Err : " + Helper.Error.getErrorMessage(e));
		    }
		}
	    }
	} catch (Exception e) {
	    // There was an Error.
	    addStatus("Caller Err : " + Helper.Error.getErrorMessage(e));
	    if (responseListener != null) {
		responseListener.onDataChanged(false, "Err : " + Helper.Error.getErrorMessage(e));
	    }
	}
    }
}
