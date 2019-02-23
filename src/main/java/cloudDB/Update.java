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

import helpers.C;
import helpers.Helper;
import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 *
 * @author karan This class will be used to Create the Update Statement, to
 * update data into the table.
 *
 */
public class Update {

    // Variables.
    private CloudDB cdb;
    private String status;
    private StringBuilder statusTrace;
    private String tableName;
    private StringBuilder columnNameBuild;
    private StringBuilder whereClauseBuilder;
    private ArrayList<Object> colBindObjs;
    private ArrayList<Object> whereBindObjs;
    private StringBuilder returnColBuilder;
    private boolean isError = false;
    private boolean isSystemTable = false;
    private boolean isNormalSecuritySet = false;
    private boolean firstUserDefinedWhere = true;
    private long updateTime = 0;

    // Static Constant Values.
    // The Type of Operation Update Column.
    public enum O {
        ADD, SUBSCRACT, MULTIPLY, DIVIDE, MODULAR
    }

    // The Type of Update Column
    public enum T {
        VALUE, COLUMN, CONCAT, OPERATION, DEFAULT
    }

    // Constructor.
    // Default Constructor.
    public Update() {
        // Default Constructor.
    }

    // The Constructor that has context.
    public Update(CloudDB cdb) {
        try {
            // Lets authenticate the user.
            if (cdb.isAuthenticated()) {
                // The user is authenticated.
                this.cdb = cdb;
                //addStatus("User is Authenticated");
                columnNameBuild = new StringBuilder();
                whereClauseBuilder = new StringBuilder();
                colBindObjs = new ArrayList<>();
                whereBindObjs = new ArrayList<>();
                returnColBuilder = new StringBuilder();
            } else {
                // The user is not authenticated.
                isError = true;
                //addStatus("User is NOT Authenticated");
            }
        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Error : " + Helper.Error.getErrorMessage(e));
        }
    }

    // Ad the Context into the Update here.
    public Update with(CloudDB cdb) {
        try {
            // Lets authenticate the user.
            if (cdb.isAuthenticated()) {
                // The user is authenticated.
                this.cdb = cdb;
                columnNameBuild = new StringBuilder();
                whereClauseBuilder = new StringBuilder();
                colBindObjs = new ArrayList<>();
                whereBindObjs = new ArrayList<>();
                returnColBuilder = new StringBuilder();
                return this;
            } else {
                // The user is not authenticated.
                isError = true;
                //addStatus("User is NOT Authenticated");
                return this;
            }
        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // The usable constructor.
    public Update into(String tableName) {
        try {
            // Here we add the table name to be updated.
            if (this.tableName == null) {
                //  This the First time the table name is being set.
                if (cdb.validateCRUD(tableName, CloudDB.CRUD.UPDATE)) {
                    // The user is allowed to update into this table.
                    this.tableName = tableName.replaceAll(" ", "");
                    // Lets add the normal user security.
                    if (cdb.isNormalUser()) {
                        // The user is a normal user.
                        // Should only be allowed to update a couple type of RLS IDs.
                        // Add the Multi-Tenancy Clause if Required for the table.
                        boolean isMultiTenant = false;
                        if (cdb.isMultiTenant(tableName)) {
                            // The table is to be multi tenant
                            addWhere(new Where("tenant_id_", Where.Type.EQUAL, cdb.getUser().getTenantID()));
                            isMultiTenant = true;
                            isNormalSecuritySet = true;
                        }
                        // Add the RLS Clause
                        if (cdb.isSyncable(tableName)) {
                            // The table is to be syncable.
                            // Check if the Multi-tenant security was set.
                            if (isMultiTenant) {
                                // The Multi-tenant, add the connector param.
                                whereClauseBuilder.append(" AND ")
                                                    .append('(')
                                                    .append(new Where()
                                                                        .setupRLSClause(cdb.getUser(), tableName, C.QueryType.UPDATE))
                                                    .append(')');
                            } else {
                                // It is not Multi-tenant.
                                whereClauseBuilder.append('(')
                                                    .append(new Where()
                                                            .setupRLSClause(cdb.getUser(), tableName, C.QueryType.UPDATE))
                                                    .append(')');
                            }
                            isNormalSecuritySet = true;
                        }
                    }
                    // Initiate the Required Variables.
                    //addStatus("User is Authorized to Update this Table");
                    return this;
                } else {
                    // The user is not allowed to update into this table.
                    //addStatus("User is NOT Authorized to Update this Table");
                    isError = true;
                    return this;
                }
            } else {
                // The Table name has already been set.
                return this;
            }

        } catch (Exception e) {
            // There was an Error.
            //addStatus("Table Error : " + Helper.Error.getErrorMessage(e));
            isError = true;
            return this;
        }
    }

    // This will be used to update the system tables.
    public Update intoSystem(String tableName) {
        try {
            // Here we add the table name to be updated.
            if (this.tableName == null) {
                //  This the First time the table name is being set.
                if (cdb.isServerUser()) {
                    // The user is allowed to update into this table.
                    this.tableName = tableName.replaceAll(" ", "");
                    // Initiate the Required Variables.
                    isSystemTable = true;
                    //addStatus("User is Authorized to Update this Table");
                    return this;
                } else {
                    // The user is not allowed to update into this table.
                    //addStatus("User is NOT Authorized to Update this Table");
                    isError = true;
                    return this;
                }
            } else {
                // The Table name has already been set.
                return this;
            }

        } catch (Exception e) {
            // There was an Error.
            //addStatus("Table Error : " + Helper.Error.getErrorMessage(e));
            isError = true;
            return this;
        }
    }

    /* Different Types of Updates available. */
    // Set the Columns needed to the user.
    public Update addColumn(String columnName, Object columnValue) {
        try {
            // Here we get the Column Name and the Column Value to be added by the User.
            // Check if this is the Second Column and add the comma.
            // Only let to proceed if the Column does'nt have the special rows.
            if (cdb.isNormalUser() && columnName.charAt(columnName.length() - 1) == '_') {
                // This column is not a valid column and must be left only for the system to edit.
                addStatus("User cannot edit " + columnName + " System Column");
            } else {
                // This Columns is valid.
                if (columnNameBuild.length() > 0) {
                    columnNameBuild.append(", ");
                }
                // Add the Column to the Column Name Query String.
                columnNameBuild.append(columnName.replaceAll(" ", ""))
                                    .append(" = ")
                                    .append("? ");
                colBindObjs.add(columnValue);
            }
            return this;

        } catch (Exception e) {
            // There was an Error.
            isError = true;
            addStatus("Add Column Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Set the Columns needed to the user to make String operations with another column.
    public Update addColumn(String columnName, String otherColumnName) {
        try {
            // Here we get the Column Name and the Column Value to be added by the User.
            // Check if this is the Second Column and add the comma.
            // Only let to proceed if the Column does'nt have the special rows.
            if (cdb.isNormalUser() && columnName.charAt(columnName.length() - 1) == '_') {
                // This column is not a valid column and must be left only for the system to edit.
                addStatus("User cannot edit " + columnName + " System Column");
            } else {
                // This Columns is valid.
                if (columnNameBuild.length() > 0) {
                    columnNameBuild.append(", ");
                }
                // Add the Column to the Column Name Query String.
                columnNameBuild.append(columnName.replaceAll(" ", ""))
                                    .append(" = ")
                                    .append(otherColumnName.replaceAll(" ", ""));
            }
            return this;
        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Add Column Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Set the Column to allow to update the string with a concatinate of a column data and another string.
    public Update addColumn(String columnName, String concatColumnName, String concatString, boolean isAfterColumn) {
        try {
            // Here we get the Column Name and the Column Value to be added by the User.
            // Check if this is the Second Column and add the comma.
            // Only let to proceed if the Column does'nt have the special rows.
            if (cdb.isNormalUser() && columnName.charAt(columnName.length() - 1) == '_') {
                // This column is not a valid column and must be left only for the system to edit.
                addStatus("User cannot edit " + columnName + " System Column");
            } else {
                // This Columns is valid.
                if (columnNameBuild.length() > 0) {
                    columnNameBuild.append(", ");
                }
                // Add the Column to the Column Name Query String.
                if (isAfterColumn) {
                    // The User wants to add the String after the Column.
                    columnNameBuild.append(columnName.replaceAll(" ", ""))
                                        .append(" = ")
                                        .append("concat(")
                                        .append(concatColumnName.replaceAll(" ", ""))
                                        .append(", ?) ");
                } else {
                    // The User wants to add the String before the Column.
                    columnNameBuild.append(columnName.replaceAll(" ", ""))
                                        .append(" = ")
                                        .append("concat(?, ")
                                        .append(concatColumnName.replaceAll(" ", ""))
                                        .append(") ");
                }
                colBindObjs.add(concatString);
            }
            return this;
        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Add Column Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Set the Column to allow for math operations on the column. (Integer number)
    public Update addColumn(String columnName, String operationalColumnName, O operation, long operationValue) {
        try {
            // Here we get the Column Name and the Column Value to be added by the User.
            // Check if this is the Second Column and add the comma.
            // Only let to proceed if the Column does'nt have the special rows.
            if (cdb.isNormalUser() && columnName.charAt(columnName.length() - 1) == '_') {
                // This column is not a valid column and must be left only for the system to edit.
                addStatus("User cannot edit " + columnName + " System Column");
            } else {
                // This Columns is valid.
                if (columnNameBuild.length() > 0) {
                    columnNameBuild.append(", ");
                }
                // Add the Column to the Column Name Query String.
                columnNameBuild.append(columnName.replaceAll(" ", ""))
                                    .append(" = ")
                                    .append(operationalColumnName.replaceAll(" ", ""));
                // Select the Right operator.
                switch (operation) {
                    case ADD:
                        columnNameBuild.append('+');
                        break;
                    case SUBSCRACT:
                        columnNameBuild.append('-');
                        break;
                    case MULTIPLY:
                        columnNameBuild.append('*');
                        break;
                    case DIVIDE:
                        columnNameBuild.append('/');
                        break;
                    case MODULAR:
                        columnNameBuild.append('%');
                        break;
                    default:
                        return null;
                }
                // Add the Bindable String.
                columnNameBuild.append(" ? ");
                colBindObjs.add(operationValue);
            }
            return this;
        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Add Column Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Set the Column to allow for math operations on the column. (Floating point number)
    public Update addColumn(String columnName, String operationalColumnName, O operation, double operationValue) {
        try {
            // Here we get the Column Name and the Column Value to be added by the User.
            // Check if this is the Second Column and add the comma.
            // Only let to proceed if the Column does'nt have the special rows.
            if (cdb.isNormalUser() && columnName.charAt(columnName.length() - 1) == '_') {
                // This column is not a valid column and must be left only for the system to edit.
                addStatus("User cannot edit " + columnName + " System Column");
            } else {
                // This Columns is valid.
                if (columnNameBuild.length() > 0) {
                    columnNameBuild.append(", ");
                }
                // Add the Column to the Column Name Query String.
                columnNameBuild.append(columnName.replaceAll(" ", ""))
                                    .append(" = ")
                                    .append(operationalColumnName.replaceAll(" ", ""));
                // Select the Right operator.
                switch (operation) {
                    case ADD:
                        columnNameBuild.append('+');
                        break;
                    case SUBSCRACT:
                        columnNameBuild.append('-');
                        break;
                    case MULTIPLY:
                        columnNameBuild.append('*');
                        break;
                    case DIVIDE:
                        columnNameBuild.append('/');
                        break;
                    case MODULAR:
                        columnNameBuild.append('%');
                        break;
                    default:
                        return null;
                }
                // Add the Bindable String.
                columnNameBuild.append(" ? ");
                colBindObjs.add(operationValue);
            }
            return this;
        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Add Column Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Set the Column to the Default Value.
    public Update addColumn(String columnName) {
        try {
            // Here we will set the column name to the default value.
            // Check if this is the Second Column and add the comma.
            // Only let to proceed if the Column does'nt have the special rows.
            if (cdb.isNormalUser() && columnName.charAt(columnName.length() - 1) == '_') {
                // This column is not a valid column and must be left only for the system to edit.
                addStatus("User cannot edit " + columnName + " System Column");
            } else {
                // This Columns is valid.
                if (columnNameBuild.length() > 0) {
                    columnNameBuild.append(", ");
                }
                // Add the Column to the Column Name Query String.
                columnNameBuild.append(columnName.replaceAll(" ", ""))
                                    .append(" = ")
                                    .append("DEFAULT ");
            }
            return this;

        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Add Column Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // The System Columns will be updated in the below method.
    private Update addColumnInternal(ColumnData columnData) {
        try {
            // We will add the Column using the internal method.
            if (columnNameBuild.length() > 0) {
                columnNameBuild.append(", ");
            }
            // Add the Column to the Column Name Query String.
            columnNameBuild.append(columnData.getColumn())
                                .append(" = ")
                                .append("? ");
            colBindObjs.add(columnData.get());
            return this;
        } catch (Exception e) {
            // THere was an Error.
            isError = true;
            //addStatus("Add Column Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Set the Column to the Default Value.
    private Update addColumnInternal(String columnName, boolean setDefault) {
        try {
            // Here we will set the column name to the default value.
            // Check if this is the Second Column and add the comma.
            // Only let to proceed if the Column does'nt have the special rows.
            if (setDefault) {
                // This Columns is valid.
                if (columnNameBuild.length() > 0) {
                    columnNameBuild.append(", ");
                }
                // Add the Column to the Column Name Query String.
                columnNameBuild.append(columnName.replaceAll(" ", ""))
                                    .append(" = ")
                                    .append("DEFAULT ");
            }
            return this;

        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Add Column Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Setter and Getter of the Update Time.
    public Update setUpdateTime(long updateTime) {
        // Lets set the update time.
        this.updateTime = updateTime;
        return this;
    }

    // Set the Where Clauses needed to run the query.
    public Update addWhere(Where clause) {
        try {
            // Add the where to the system, with the default connector.
            // Check if the Normal User Security Check is added.
            if (isNormalSecuritySet) {
                // The Noraml User Security Check has been added.
                // Check if this is the First User Defined Where.
                if (firstUserDefinedWhere) {
                    // Add the Connector into the Clause.
                    clause.setConnector(true);
                }
            }
            if (whereClauseBuilder.length() > 0) {
                whereClauseBuilder.append(clause.getConnector());
            }
            whereClauseBuilder.append(clause.getColumnName())
                                .append(clause.operation())
                                .append('?');
            whereBindObjs.add(clause.getColumnData());
            firstUserDefinedWhere = false;
            return this;
        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Add Where Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Add the Return Column Name.
    public Update addReturningColumn(String columnName) {
        try {
            // Here we add the returning column that has to be added.
            if (returnColBuilder.length() > 0) {
                // There is another return column added
                returnColBuilder.append(',');
            }
            returnColBuilder.append(" ")
                                .append(columnName.replaceAll(" ", ""));
            return this;
        } catch (Exception e) {
            // There was an Error.
            isError = true;
            //addStatus("Add Returning Column Error : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Allow the User to Get the SQL Statement Generated.
    public String getSQL() {
        try {
            // Here we will Generate the SQL Statement.
            // Assemble the Main Statement.
            // Check if the user is allowed to update.
            if (isSuccessful()) {
                // Build the SQL Statement.
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("UPDATE ")
                                    .append(tableName)
                                    .append(" SET ")
                                    .append(columnNameBuild)
                                    .append(" WHERE ")
                                    .append(whereClauseBuilder);

                //  Add the Return Columns if there exist.
                if (returnColBuilder.length() > 0) {
                    sqlBuilder.append(" RETURNING ")
                                        .append(returnColBuilder);
                }
                return sqlBuilder.toString();
            } else {
                // There was an Error in the Update.
                return "Update Error : " + getStatusTrace();
            }
        } catch (Exception e) {
            // There was an Error.
            return "Error : " + Helper.Error.getErrorMessage(e);
        }
    }

    // Generate the Prepared statement for easier usability.
    public PreparedStatement getPreparedStatement() {
        try {
            // Perform the required actions before the creation of the SQL.
            if (cdb.isSyncable(tableName) || updateTime > 0) {
                // The table is a syncable table or update time is given.
                addColumnInternal(new ColumnData("update_time_", updateTime));
            }

            // Here we prepare the statement using the Connection Provided.
            String sqlQuery = getSQL();
            // Prepare the Statement.
            PreparedStatement prepStmt = cdb.getConn().prepareStatement(sqlQuery);
            ArrayList<Object> bindObjs = new ArrayList<>();
            bindObjs.addAll(colBindObjs);
            bindObjs.addAll(whereBindObjs);
            // Bind the Column Data &
            // Bind the Where Clause Data.
            for (int bindCount = 1; bindCount <= bindObjs.size(); bindCount++) {
                // Bind the Values to the Database.
                prepStmt.setObject(bindCount, bindObjs.get(bindCount - 1));
            }
            return prepStmt;

        } catch (Exception e) {
            // There waa an Error.
            return null;
        }
    }

    // Here we generate the SQL Prepared Statement for the User, that will connect only to the core database.
    public PreparedStatement getPrepStmtCore() {
        try {
            // Here we will generate the SQL Statement with the placeholders.
            // Perform the required actions before the creation of the SQL.
            if (updateTime > 0) {
                // The update time is given.
                addColumnInternal(new ColumnData("update_time_", updateTime));
            }

            // And we bind the Queries.
            String sqlQuery = getSQL();
            // Prepare the Statement.
            PreparedStatement prepStmt = cdb.getCoreConn().prepareStatement(sqlQuery);
            ArrayList<Object> bindObjs = new ArrayList<>();
            bindObjs.addAll(colBindObjs);
            bindObjs.addAll(whereBindObjs);
            // Bind the Column Data &
            // Bind the Where Clause Data.
            for (int bindCount = 1; bindCount <= bindObjs.size(); bindCount++) {
                // Bind the Values to the Database.
                prepStmt.setObject(bindCount, bindObjs.get(bindCount - 1));
            }
            return prepStmt;

        } catch (Exception e) {
            // There was an Error.
            return null;
        }
    }

    // Get the Status.
    public String getStatus() {
        return status;
    }

    // Get if the System Errored out.
    public boolean isSuccessful() {
        return !isError;
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

}
