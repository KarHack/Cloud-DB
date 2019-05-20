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
 * @author karan This class will be used to generate the select statement with
 * all required, Joins using all security measures.
 *
 */
public class Select {

    // Variables.
    private CloudDB cdb;
    private String status;
    private StringBuilder statusTrace;
    private String tableName;
    private StringBuilder columnNameBuilder;
    private StringBuilder joinsBuilder;
    private StringBuilder whereClauseBuilder;
    private StringBuilder orderByClauseBuilder;
    private ArrayList<Object> bindObjs;
    private boolean isNormalSecuritySet = false;
    private boolean firstUserDefinedWhere = true;
    private boolean isError = false;
    private Type type = Type.RESULTSET;
    private boolean isSystemTable = false;

    // This will hold all the types of Queries this is.
    public enum Type {
        RESULTSET, COUNT, SUM
    }

    public enum OrderType {
        ASC, DESC
    }

    // Default Constructor.
    public Select() {
        try {
            // Default Constructor.
            // We will have to allow the user to set the context as well.
            // Initiate the required variables.
            if (statusTrace == null) {
                statusTrace = new StringBuilder();
            }
            columnNameBuilder = new StringBuilder();
            joinsBuilder = new StringBuilder();
            whereClauseBuilder = new StringBuilder();
            orderByClauseBuilder = new StringBuilder();
            bindObjs = new ArrayList<>();
            addStatus("Initiated");
        } catch (Exception e) {
            // There was an Error.
        }
    }

    // This constructor will allow to add the context and the Cloud DB as well.
    public Select(CloudDB cdb) {
        try {
            if (cdb.isAuthenticated()) {
                // The user is authenticated.
                this.cdb = cdb;
                // Initiate the required variables.
                if (statusTrace == null) {
                    statusTrace = new StringBuilder();
                }
                columnNameBuilder = new StringBuilder();
                joinsBuilder = new StringBuilder();
                whereClauseBuilder = new StringBuilder();
                orderByClauseBuilder = new StringBuilder();
                bindObjs = new ArrayList<>();
                addStatus("User is Authenticated, Initializing.");
            } else {
                // The user is not authenticated.
                isError = true;
                addStatus("User is Not Authenticated.");
            }
        } catch (Exception e) {
            // There was an Error.
            addStatus("Cloud DB is Null");
            isError = true;
        }
    }

    // This method allows the User to set the Context if the user chose the first constructor.
    public Select with(CloudDB cdb) {
        try {
            if (cdb.isAuthenticated()) {
                // The user is authenticated.
                this.cdb = cdb;
                addStatus("CloudDB Added");
                return this;
            } else {
                // The User is Not Authenticated.
                isError = true;
                addStatus("User is Not Authenticated");
                return null;
            }
        } catch (Exception e) {
            // There was an Error.
            addStatus("Cloud DB is Null");
            isError = true;
            return null;
        }
    }

    // The Constructor to also set the Table to be read.
    public Select from(String tableName) {
        try {
            // Check for authenticated.
            addStatus("Going to Authenticate " + tableName);
            if (this.tableName == null) {
                if (cdb.validateCRUD(tableName, CloudDB.CRUD.READ)) {
                    addStatus("User Authorized to Read from the Table");
                    // The user has provided
                    // We will remove all spaces and then add the table name.
                    // Authenticate the User against the Table.
                    this.tableName = tableName.trim().replaceAll(" ", "");
                    addStatus("Set Table");
                    // The Security has been completed
                    // Add the Normal User Specific Where Clauses.
                    if (cdb.isNormalUser()) {
                        // Add the Multi-Tenancy Clause if Required for the table.
                        boolean isMultiTenant = false;
                        if (cdb.isMultiTenant(tableName)) {
                            // The table is to be multi tenant
                            addWhere(new Where(tableName + ".tenant_id_", Where.Type.EQUAL, cdb.getUser().getTenantID()));
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
                                                    .append(new Where().setupRLSClause(cdb.getUser(), tableName, C.QueryType.SELECT))
                                                    .append(')');
                            } else {
                                // It is not Multi-tenant.
                                whereClauseBuilder.append('(')
                                                    .append(new Where().setupRLSClause(cdb.getUser(), tableName, C.QueryType.SELECT))
                                                    .append(')');
                            }
                            isNormalSecuritySet = true;
                        }
                    }
                    return this;
                } else {
                    // The user does'nt have authority to read from this table.
                    addStatus("Not Authorized to Read Table");
                    isError = true;
                    return null;
                }
            } else {
                // The Table name has already been set.
                return this;
            }
        } catch (Exception e) {
            // There was an Error.
            addStatus(Helper.Error.getErrorMessage(e));
            this.tableName = null;
            isError = true;
            return null;
        }
    }

    // This is to read from system tables, like the Cloud Core Tables.
    public Select fromSystem(String tableName) {
        try {
            // Check for authenticated.
            addStatus("Going to Authenticate " + tableName);
            if (this.tableName == null) {
                if (cdb.isServerUser()) {
                    addStatus("User Authorized to Read from the Table");
                    // The user has provided
                    // We will remove all spaces and then add the table name.
                    // Authenticate the User against the Table.
                    this.tableName = tableName.trim().replaceAll(" ", "");
                    addStatus("Set Table");
                    // The Security has been completed
                    isSystemTable = true;
                    return this;
                } else {
                    // The user does'nt have authority to read from this table.
                    addStatus("Not Authorized to Read Table");
                    isError = true;
                    return null;
                }
            } else {
                // The Table name has already been set.
                return this;
            }
        } catch (Exception e) {
            // There was an Error.
            addStatus(Helper.Error.getErrorMessage(e));
            this.tableName = null;
            isError = true;
            return null;
        }
    }

    // This is to read from the system tables, but only accessable to the Classes in this package.
    protected Select fromSystem(Sync sync, String tableName) {
        try {
            // Check for authenticated.
            addStatus("Going to Authenticate " + tableName);
            if (this.tableName == null) {
                if (cdb.isNormalUser()) {
                    addStatus("User Authorized to Read from the Table");
                    // The user has provided
                    // We will remove all spaces and then add the table name.
                    // Authenticate the User against the Table.
                    this.tableName = tableName.trim().replaceAll(" ", "");
                    addStatus("Set Table");
                    // The Security has been completed
                    isSystemTable = true;
                    return this;
                } else {
                    // The user does'nt have authority to read from this table.
                    addStatus("Not Authorized to Read Table");
                    isError = true;
                    return null;
                }
            } else {
                // The Table name has already been set.
                return this;
            }
        } catch (Exception e) {
            // There was an Error.
            addStatus(Helper.Error.getErrorMessage(e));
            this.tableName = null;
            isError = true;
            return null;
        }
    }

    // Get the Table Name.
    public String getTableName() {
        return tableName;
    }

    // Set the Type of Query as Count.
    public Select count(String columnName) {
        try {
            // Now we will add the Count Column.
            type = Type.COUNT;
            if (columnName == null || columnName.trim().isEmpty()) {
                // There is no Column Name Specifically to be Counted.
                columnNameBuilder = new StringBuilder(" COUNT(*) ");
            } else {
                // There is a Column Name to be returned.
                String columnNameN = columnName.replaceAll(" ", "");
                columnNameBuilder = new StringBuilder();
                columnNameBuilder.append(" COUNT(")
                                    .append(columnNameN)
                                    .append(") ");
            }
            return this;
        } catch (Exception e) {
            // There was an Error.
            return this;
        }
    }

    // Set the Type of Query as Sum.
    boolean hasSumColumns = false;

    public Select sum(String columnName) {
        try {
            // Here we will Add the Sum Column.
            type = Type.SUM;
            if (hasSumColumns) {
                columnNameBuilder.append(",");
            } else {
                // This is the First time.
                hasSumColumns = true;
                columnNameBuilder = new StringBuilder();
            }
            columnNameBuilder.append(" SUM(")
                                .append(columnName)
                                .append(") as ")
                                .append(columnName);
            return this;
        } catch (Exception e) {
            // There was an Error.
            return this;
        }
    }

    // Let the user to add the Columns to the Query.
    // Allow the user to add columns.
    public Select addColumn(String columnName) {
        try {
            // Add a column name to the array.
            // Before adding to the view
            String columnNameN = columnName.replaceAll(" ", "");
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
    public Select addColumn(String columnName, String identifiedBy) {
        try {
            // Add a column name to the array.
            // Before adding to the view
            String columnNameN = columnName.replaceAll(" ", "")
                                + " AS " + identifiedBy.replaceAll(" ", "");
            if (columnNameBuilder.length() > 0) {
                columnNameBuilder.append(", ");
            }
            columnNameBuilder.append(columnNameN);
        } catch (Exception e) {
            // There was an Error.
        }
        return this;
    }

    // Let the User to add Joins and Join Multiple Queries in one query.
    public Select addJoin(Join join) {
        try {
            // Here we will add the join that the user is trying to add.
            // Validate the Join.
            if (cdb.validateCRUD(join.getTableName(), CloudDB.CRUD.READ)) {
                joinsBuilder.append(join.getJoinSQL());
                return this;
            } else {
                // The user is not authorized to read from this table.
                return null;
            }

        } catch (Exception e) {
            // There was an Error.
            addStatus("Join Er : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Let the User to add Where clauses to the System.
    public Select addWhere(Where clause) {
        try {
            // Add the where to the system, with the default connector.
            if (clause != null) {
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
                bindObjs.add(clause.getColumnData());
                firstUserDefinedWhere = false;
            }
            return this;

        } catch (Exception e) {
            // There was an Error.
            addStatus("Er : " + Helper.Error.getErrorMessage(e));
            return this;
        }
    }

    // Let the User to add the Order by Clause.
    // Order by using Asc.
    public Select addOrderBy(String orderingColumn) {
        try {
            // Here we will add the ordering column.
            if (orderByClauseBuilder.length() > 0) {
                orderByClauseBuilder.append(", ");
            }
            orderByClauseBuilder.append(orderingColumn);
            return this;
        } catch (Exception e) {
            // There was an Error.
            return this;
        }
    }

    // Order by using Asc / Desc.
    public Select addOrderBy(String orderingColumn, String orderBy) {
        try {
            // Here we will add the ordering column.
            if (orderByClauseBuilder.length() > 0) {
                orderByClauseBuilder.append(", ");
            }
            try {
                switch (OrderType.valueOf(orderBy)) {
                    case ASC:
                        orderByClauseBuilder.append(orderingColumn)
                                            .append(" ASC ");
                        break;
                    case DESC:
                        orderByClauseBuilder.append(orderingColumn)
                                            .append(" DESC ");
                        break;
                    default:
                        orderByClauseBuilder.append(orderingColumn);
                        break;
                }
            } catch (Exception e) {
                // There was an Error 
                orderByClauseBuilder.append(orderingColumn);
            }
            return this;
        } catch (Exception e) {
            // There was an Error.
            return this;
        }
    }

    // Here we generate the SQL Statement for the User.
    public String getSQL() {
        try {
            // Here we generate the SQL Statement.
            // Attach the Main Select statement.
            StringBuilder sqlBuilder = new StringBuilder();
            if (columnNameBuilder.length() == 0) {
                columnNameBuilder.append(" * ");
            }
            sqlBuilder.append("SELECT ")
                                .append(columnNameBuilder)
                                .append(" FROM ")
                                .append(tableName);
            // Add the Join.
            if (joinsBuilder.length() > 0) {
                sqlBuilder.append(joinsBuilder);
            }
            // Add the Where Clauses.
            if (whereClauseBuilder.length() > 0) {
                sqlBuilder.append(" WHERE ")
                                    .append(whereClauseBuilder);
            }
            // Add the Order by Clause.
            if (orderByClauseBuilder.length() > 0) {
                sqlBuilder.append(" ORDER BY ")
                                    .append(orderByClauseBuilder);
            }

            return sqlBuilder.toString();

        } catch (Exception e) {
            // There was an Error.
            return Helper.Error.getErrorMessage(e);
        }
    }

    // Here we generate the SQL Prepared Statement for the User.
    public PreparedStatement getPreparedStatement() {
        try {
            // Here we will generate the SQL Statement with the placeholders.
            // And we bind the Queries.
            String sqlQuery = getSQL();
            // Prepare the Statement.
            PreparedStatement prepStmt = cdb.getConn().prepareStatement(sqlQuery);
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

    // Here we generate the SQL Prepared Statement for the User, that will connect only to the core database.
    public PreparedStatement getPrepStmtCore() {
        try {
            // Here we will generate the SQL Statement with the placeholders.
            // And we bind the Queries.
            String sqlQuery = getSQL();
            // Prepare the Statement.
            PreparedStatement prepStmt = cdb.getCoreConn().prepareStatement(sqlQuery);
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

    public String getStatus() {
        return status;
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

    // Get the Bind Objects.
    public ArrayList<Object> getBindObjs() {
        // Get the Bind Objects.
        return bindObjs;
    }

    // Get if there is an Error.
    public boolean isSuccessful() {
        return !isError;
    }

}
