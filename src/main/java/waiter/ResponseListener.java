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

/**
 *
 * @author karan
 */
// Response Reciever interface.
public abstract class ResponseListener {
    public abstract void onDataChanged(boolean isSuccess, String response);
}
