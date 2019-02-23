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

import java.util.HashMap;
import helpers.Helper;

/**
 *
 * @author karan Waiter Manager is supposed to Keep a track of all the waiters
 * that are currently running. It will maintain the status of every waiter.
 *
 */
public class WaiterManager {

    // Variables
    private static String status;
    private static StringBuilder statusTrace;
    private static HashMap<String, Waiter> waiters;
    private static boolean initialRun = true;

    // Methods.
    // This will allow the waiters to start and function properly.
    public static String init(Waiter waiter) {
	try {
	    // Here we will perform the required initialization.
	    if (waiters == null) {
		// Initialize the Waiter Map.
		waiters = new HashMap<>();
	    }
	    // We will also flush the database if we require to.
	    if (initialRun) {
		// This is the first run.
		// The initial run has been completed
		initialRun = false;
	    }

	    // Add the Waiter to the HashMap.
	    String waiterID = Helper.StringManu.generate(12);
	    waiters.put(waiterID, waiter);
	    // Send the ID to the Waiter.
	    return waiterID;
	} catch (Exception e) {
	    // There was an Error.
	    addStatus("Init Err : " + Helper.Error.getErrorMessage(e));
	    // Tell the Waiter there was an Error.
	    return "Err : " + Helper.Error.getErrorMessage(e);
	}
    }

    // This will update the cache.
    public static void update(Waiter waiter) {
	try {
	    // Add the Value.
	    waiters.put(waiter.getID(), waiter);
	} catch (Exception e) {
	    // There was an error.
	}
    }

    // This will insert that the query is running here.
    public static void begin(Waiter waiter) {
	try {
	    // Let's insert the waiter as running in the database.
	    // Here we will update the Cache.
	    // Add the Value.
	    waiters.put(waiter.getID(), waiter);
	} catch (Exception e) {
	    // There was an error.
	}
    }

    // This will delete the waiter from the running requests.
    public static void end(Waiter waiter) {
	try {
	    // Here we will remove the waiter from the requests table, as the waiter is completed its request.
	    // Remove the Waiter from the cache of waiters.
	    waiters.remove(waiter.getID());
	} catch (Exception e) {
	    // There was an Error.
	}
    }

    // Here we set the status and the status trace.
    private static void addStatus(String statuss) {
	try {
	    // Here we append the status to the status trace and status.
	    if (statusTrace == null) {
		statusTrace = new StringBuilder();
	    }
	    status = statuss;
	    statusTrace.append(statuss)
		    .append('\n');
	} catch (Exception e) {
	    // There was an Error.
	}
    }

    // This will retrieve the whole status trace.
    public static String getStatusTrace() {
	return statusTrace.toString();
    }

    // This will retrieve just the status of the cloud object.
    public static String getStatus() {
	return status;
    }

}
