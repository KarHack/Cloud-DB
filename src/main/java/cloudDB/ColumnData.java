/*
 * 
 * 36E CLOUD CONNECTOR
 * This is a project of 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * This project is a closed source and proprietary software package.
 * None of the contents of this software is to be used for uses not intended,
 * And no one is to interface with the software in methods not defined or previously decided by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * No changes should be done to this project without prior authorization by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 2017 (C) 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 
 */
package cloudDB;

import org.json.simple.parser.JSONParser;

/**
 *
 * @author karan
 */
public class ColumnData {

    // Column data class object.
    // Variables.
    // Main Object variable, will be filled by all, only used if needed.
    private Object objData;

    // State Holder.
    private String column;

    // The Constructor.
    private ColumnData() {
	// We don't require this.
    }

    // The usable Constructor.
    public ColumnData(String columnName) {
	this.column = columnName.replaceAll(" ", "");
    }

    // The Constructor to allow the Bind Object and the Column Name to be put together.
    public ColumnData(String columnName, Object columnData) {
	this.column = columnName.replaceAll(" ", "");
	// Validate if the Column data is json.
	try {
	    // Let us check if it is a json.
	    new JSONParser().parse(columnData.toString());
	    // It is a json, save it as a string.
	    this.objData = columnData.toString();
	} catch (Exception e) {
	    // It is not a json.
	    this.objData = columnData;
	}
    }

    // The object setters come here.
    public ColumnData put(Object columnData) {
	// This is the main object putter. Will be used as a non typing method.
	objData = columnData;
	return this;
    }

    // Make the Getter.    
    public Object get() {
	// Here we will return the main object.
	return objData;
    }

    public String getColumn() {
	return column;
    }

}
