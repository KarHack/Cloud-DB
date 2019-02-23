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
/*
 * This Will be used to Help with the Parameter Interaction of Waiter API.
 */
public class ParamData {
    // Global Variables.

    private String param;
    private Object data;

    // Usable Constructor.
    public ParamData(String paramName) {
	this.param = paramName;
    }

    // Setters and Getters.
    public ParamData put(Object objData) {
	this.data = objData;
	return this;
    }

    public String getParam() {
	return param;
    }

    public Object getData() {
	return data;
    }
}
