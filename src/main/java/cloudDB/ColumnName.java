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

/**
 *
 * @author karan
 */
// This class will be used to state the Columns from the table that are required to be extracted.
public class ColumnName {

    // Variables required.
    final private ArrayList<String> columnNameArr;
    final private StringBuilder columnNameBuilder;

    // Constructor.
    public ColumnName() {
	// This is a normal constructor.
	columnNameArr = new ArrayList<>();
	columnNameBuilder = new StringBuilder();
    }

    // Get the array of the columns.
    public String[] getColumnNames() {
	return columnNameArr.toArray(new String[columnNameArr.size()]);
    }

    // Get the Columns in a string format.
    public String getColumnSQL() {
	try {
	    // Give the Column Names in a Simple String Format.
	    return columnNameBuilder.toString();

	} catch (Exception e) {
	    // There was an Error.
	    return " * ";
	}
    }

    // Allow the user to add columns.
    public ColumnName add(String columnName) {
	try {
	    // Add a column name to the array.
	    // Before adding to the view
	    String columnNameN = columnName.replaceAll(" ", "");
	    columnNameArr.add(columnNameN);
	    if (columnNameBuilder.length() > 0) {
		columnNameBuilder.append(", ");
	    }
	    columnNameBuilder.append(columnNameN);
	} catch (Exception e) {
	    // There was an Error.
	}
	return this;
    }

    // Allow the user to add columns and have an identifier set.
    public ColumnName add(String columnName, String identifiedBy) {
	try {
	    // Add a column name to the array.
	    // Before adding to the view
	    String columnNameN = columnName.replaceAll(" ", "")
		    + " AS " + identifiedBy.replaceAll(" ", "");
	    columnNameArr.add(columnNameN);
	    if (columnNameBuilder.length() > 0) {
		columnNameBuilder.append(", ");
	    }
	    columnNameBuilder.append(columnNameN);
	} catch (Exception e) {
	    // There was an Error.
	}
	return this;
    }

}
