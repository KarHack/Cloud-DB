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
package cloudDBEndpoints;

import helpers.Helper;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author karan This class will help in the interpretation of the ResultSet, Or
 * the Result from the Database. It will be used to Convert the Result from the
 * database to a JSON Array, of JSON Objects. The JSON Objects will be the Rows
 * of data.
 *
 */
public class Result {

    // Variables.
    ResultSet resultSet;
    String status;

    // Static Variables.
    // Constructors.
    private Result() {
	// We don't wanna expose the Default Constructor.
    }

    // The Constructor with Some Authentication.
    public Result(ResultSet resultSet) {
	// This Constructor will have the ResultSet as the Parameter.
	this.resultSet = resultSet;
    }

    // Methods.
    public JSONArray getJSONResult() {
	// This method, will convert the Provided ResultSet into a JSON Array,
	try {
	    // Here we Convert the ResultSet provided into the JSON Array.
	    JSONArray resultJArr = new JSONArray();
	    // Check if there is any rows to be read from the ResultSet.
	    if (resultSet.isBeforeFirst()) {
		// There are rows to fetch from ResultSet.
		ResultSetMetaData resultSetMeta = resultSet.getMetaData();
		int numColumns = resultSetMeta.getColumnCount();
		// Get the Columns from the Row.
		while (resultSet.next()) {
		    try {
			JSONObject colDataJObj = new JSONObject();
			for (int colCount = 1; colCount <= numColumns; colCount++) {
			    try {
				String columnName = resultSetMeta.getColumnName(colCount);
				// Extract the Column Data according to the data type.
				switch (resultSetMeta.getColumnType(colCount)) {
				    case java.sql.Types.ARRAY:
					colDataJObj.put(columnName, resultSet.getArray(columnName));
					break;
				    case java.sql.Types.BOOLEAN:
					colDataJObj.put(columnName, resultSet.getBoolean(columnName));
					break;
				    case java.sql.Types.BLOB:
					colDataJObj.put(columnName, resultSet.getBlob(columnName));
					break;
				    case java.sql.Types.BIGINT:
					colDataJObj.put(columnName, resultSet.getLong(columnName));
					break;
				    case java.sql.Types.DOUBLE:
					colDataJObj.put(columnName, resultSet.getDouble(columnName));
					break;
				    case java.sql.Types.FLOAT:
					colDataJObj.put(columnName, resultSet.getFloat(columnName));
					break;
				    case java.sql.Types.INTEGER:
					colDataJObj.put(columnName, resultSet.getInt(columnName));
					break;
				    case java.sql.Types.NVARCHAR:
					colDataJObj.put(columnName, resultSet.getNString(columnName));
					break;
				    case java.sql.Types.VARCHAR:
					colDataJObj.put(columnName, resultSet.getString(columnName));
					break;
				    case java.sql.Types.TINYINT:
					colDataJObj.put(columnName, resultSet.getInt(columnName));
					break;
				    case java.sql.Types.SMALLINT:
					colDataJObj.put(columnName, resultSet.getInt(columnName));
					break;
				    case java.sql.Types.DATE:
					colDataJObj.put(columnName, resultSet.getDate(columnName).toString());
					break;
				    case java.sql.Types.TIMESTAMP:
					colDataJObj.put(columnName, resultSet.getTimestamp(columnName).toString());
					break;
				    default:
					colDataJObj.put(columnName, resultSet.getObject(columnName).toString());
					break;
				}
			    } catch (Exception e) {
				// There was an Error.
				status = status + " ::: Col Err : " + Helper.Error.getErrorMessage(e);

			    }
			}
			// Add the Column Data JSON Object to the JSON Array.
			resultJArr.add(colDataJObj);
		    } catch (Exception e) {
			// There was an Error.
			status = status + " ::: Row Err : " + Helper.Error.getErrorMessage(e);
		    }
		}
	    }
	    // Return the Result JSon Array.
	    return resultJArr;
	} catch (Exception e) {
	    // There was an Error.
	    JSONObject errJObj = new JSONObject();
	    errJObj.put("error", Helper.Error.getErrorMessage(e));
	    JSONArray errJArr = new JSONArray();
	    errJArr.add(errJObj);
	    return errJArr;
	}
    }

    public String getStatus() {
	return status;
    }
}
