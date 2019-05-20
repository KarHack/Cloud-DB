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

/**
 *
 * @author karan This class will be used to join the queries in the pulling
 * class. This will handle some of the processing required for the joining.
 *
 */
public class Join {

    // Variables.
    private CloudDB cdb;
    private String type;
    private String tableName, tableNameAS;
    private String columnName;
    private String joinOnColumnName;
    private StringBuilder joinSQL;

    // Static values.
    public static class Type {

	public static final String INNER_JOIN = "INNER JOIN";
	public static final String JOIN = "JOIN";
	public static final String OUTTER_JOIN = "OUTTER JOIN";
	public static final String LEFT_JOIN = "LEFT JOIN";
	public static final String RIGHT_JOIN = "RIGHT JOIN";
	public static final String FULL_JOIN = "FULL JOIN";
    }

    // Default Constructor.
    private Join() {
	// Not needed Default Constructor.
    }

    // This is the Constructor that we need.
    public Join(String type, String tableName, String columnName, String joinOnColumnName) {
	try {
	    // We get the Join statements that the user wants to join.
	    this.type = type;
	    this.columnName = columnName.replaceAll(" ", "");
	    this.joinOnColumnName = joinOnColumnName.replaceAll(" ", "");
	    this.tableName = tableName.replaceAll(" ", "");
	    joinSQL = new StringBuilder();

	    // Build the Join SQL.
	    // Add the type of the Join.
	    switch (type) {
		case Type.INNER_JOIN:
		    joinSQL.append(" INNER JOIN ");
		    break;
		case Type.JOIN:
		    joinSQL.append(" JOIN ");
		    break;
		case Type.OUTTER_JOIN:
		    joinSQL.append(" OUTTER JOIN ");
		    break;
		case Type.LEFT_JOIN:
		    joinSQL.append(" LEFT JOIN ");
		    break;
		case Type.RIGHT_JOIN:
		    joinSQL.append(" RIGHT JOIN ");
		    break;
		case Type.FULL_JOIN:
		    joinSQL.append(" FULL JOIN ");
		    break;
	    }
	    // Add the Table Name of the Join.
	    joinSQL.append(this.tableName);
	    // Add the Column Name of the Join.
	    joinSQL.append(" ON ")
		    .append(this.columnName)
		    .append('=')
		    .append(this.joinOnColumnName);
	} catch (Exception e) {
	    // There was an Error.
	}
    }
    
    // This is the Constructor with the AS Keyword.
    public Join(String type, String tableName, String tableNameAS, String columnName, String joinOnColumnName) {
	try {
	    // We get the Join statements that the user wants to join.
	    this.type = type;
	    this.columnName = columnName.replaceAll(" ", "");
	    this.joinOnColumnName = joinOnColumnName.replaceAll(" ", "");
	    this.tableName = tableName.replaceAll(" ", "");
	    this.tableNameAS = tableNameAS.replaceAll(" ", "");
	    joinSQL = new StringBuilder();

	    // Build the Join SQL.
	    // Add the type of the Join.
	    switch (type) {
		case Type.INNER_JOIN:
		    joinSQL.append(" INNER JOIN ");
		    break;
		case Type.JOIN:
		    joinSQL.append(" JOIN ");
		    break;
		case Type.OUTTER_JOIN:
		    joinSQL.append(" OUTTER JOIN ");
		    break;
		case Type.LEFT_JOIN:
		    joinSQL.append(" LEFT JOIN ");
		    break;
		case Type.RIGHT_JOIN:
		    joinSQL.append(" RIGHT JOIN ");
		    break;
		case Type.FULL_JOIN:
		    joinSQL.append(" FULL JOIN ");
		    break;
	    }
	    // Add the Table Name of the Join.
	    joinSQL.append(this.tableName);
            if (tableNameAS != null && tableNameAS != "") {
                joinSQL.append(" AS ")
                                    .append(this.tableNameAS);
            }
	    // Add the Column Name of the Join.
	    joinSQL.append(" ON ")
		    .append(this.columnName)
		    .append(" = ")
		    .append(this.joinOnColumnName);
	} catch (Exception e) {
	    // There was an Error.
	}
    }

    // Get the type of the Join.
    public String getType() {
	return type;
    }

    // Get the Column Name.
    public String getColumnName() {
	return columnName;
    }

    // Get the table name.
    String getTableName() {
	return tableName;
    }

    // Get the Join On Column Name.
    public String getJoinOnColumnName() {
	return joinOnColumnName;
    }

    public String getJoinSQL() {
	return joinSQL.toString();
    }

}
