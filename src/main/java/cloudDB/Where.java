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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author karan
 */
public class Where {

    // Variables
    private String columnName;
    private Object columnData;
    private Type operation;
    private boolean andConnector;

    // Static Variables.
    public enum Type {
        EQUAL, NOT_EQUAL, LESSER, GREATER, LESSER_EQUAL, GREATER_EQUAL
    }

    // Where Where
    public Where(String columnName, Type operation, Object columnData) {
        // We add this to our private variables.
        this.columnName = columnName.replaceAll(" ", "");
        this.operation = operation;
        this.columnData = columnData;
        this.andConnector = true;
    }

    // Where that will allow the user to state the Connector Statement
    // Used to connect with the previous Where Where.
    public Where(boolean andConnect, String columnName, Type operation, Object columnData) {
        // We add this to our private variables.
        this.columnName = columnName.replaceAll(" ", "");
        this.operation = operation;
        this.columnData = columnData;
        this.andConnector = andConnect;
    }

    // Special Where constructor, only callable from the same package.
    Where() {
        // This is a simple constructor, and should not be used without knowing completly what to do.
    }

    // Setup the Where IN RLS Clause for the Query.
    String setupRLSClause(User userObj, String tableName, C.QueryType queryType) {
        try {
            // Here we will retrieve the RLS From the User Object.
            // Get the Roles and the Group IDs of the User.
            Long[] roleRLSs = getRLS_IDs(userObj.getRoleMap(), queryType);
            Long[] groupRLSs = getRLS_IDs(userObj.getGroupMap(), queryType);
            // Create the RLS Where Clause.
            return getRLSWhereIN(tableName, roleRLSs, groupRLSs);
        } catch (Exception e) {
            // There was an Error.
            return "";
        }
    }

    // Here are the getters of the Class.
    // We get all the data from the object.
    public String getColumnName() {
        return columnName;
    }

    public Object getColumnData() {
        return columnData;
    }

    String getConnector() {
        return andConnector ? " AND " : " OR ";
    }

    void setConnector(boolean andConnect) {
        this.andConnector = andConnect;
    }

    public String operation() {
        try {
            // Generate the right operator to be used in the query.
            switch (operation) {
                case EQUAL:
                    return "=";
                case NOT_EQUAL:
                    return "!=";
                case GREATER:
                    return ">";
                case GREATER_EQUAL:
                    return ">=";
                case LESSER:
                    return "<";
                case LESSER_EQUAL:
                    return "<=";
                default:
                    return null;
            }
        } catch (Exception e) {
            // There was an error.
            return null;
        }
    }

    /*
     * This is a Special Method, and will Create a where in clause.
     * This will be used for the F-RLS of the Sync Users.
     * The Roles and The groups will be provided.
     * This Method, won't follow the Security Norms,
     * Of Prepared Statement, as the Values will be in Integer form.
     * Also Will be Completedly handled Automatically.
     *
     */
    private static String getRLSWhereIN(String tableName, Long roleIDs[], Long groupIDs[]) {
        try {
            // Here we will Create the Where IN Clause of the Sync User.
            StringBuilder rlsBuild = new StringBuilder();
            // Check if there are role ids.
            if (roleIDs.length > 0) {
                // Add the Roles in the Where IN.
                rlsBuild
                                    .append("(")
                                    .append(tableName)
                                    .append(".rls_id_ IN (");
                // Add the Roles ID into the String.
                for (int roleIt = 0; roleIt < roleIDs.length - 1; // This will leave the last Role, which we will add later.
                                    roleIt++) {
                    try {
                        // Lets add the Role ID into the String.
                        rlsBuild.append(roleIDs[roleIt]);
                        rlsBuild.append(',');
                    } catch (Exception e) {
                        // There was an Error.
                    }
                }
                // Now we will add the last role, 
                // This is done so that we don't have to do any additional processing.
                rlsBuild.append(roleIDs[roleIDs.length - 1]);
                rlsBuild
                                    .append(") AND ")
                                    .append(tableName)
                                    .append(".rls_type_ = 1)");
            } else {
                // Add the Role ID PlaceHolder Clause,
                // This is to make sure to extra data is not sent to the user.
                rlsBuild
                                    .append("(")
                                    .append(tableName)
                                    .append(".rls_id_ IN (-1) AND ")
                                    .append(tableName)
                                    .append(".rls_type_ = 1)");
            }

            // Lets add the OR Clause for the connection of the two statements.
            rlsBuild.append(" OR ");

            // Check if there are group ids.
            if (groupIDs.length > 0) {
                // Add the Groups in the Where IN.
                rlsBuild.append("(")
                                    .append(tableName)
                                    .append(".rls_id_ IN (");
                // Add the Roles ID into the String.
                for (int groupIt = 0; groupIt < groupIDs.length - 1; // This will leave the last Role, which we will add later.
                                    groupIt++) {
                    try {
                        // Lets add the Role ID into the String.
                        rlsBuild.append(groupIDs[groupIt]);
                        rlsBuild.append(',');
                    } catch (Exception e) {
                        // There was an Error.
                    }
                }
                // Now we will add the last role, 
                // This is done so that we don't have to do any additional processing.
                rlsBuild.append(groupIDs[groupIDs.length - 1]);
                rlsBuild.append(") AND ")
                                    .append(tableName)
                                    .append(".rls_type_ = 2)");
            } else {
                // Add the Role ID PlaceHolder Clause,
                // This is to make sure to extra data is sent to the user.
                rlsBuild.append("(").append(tableName)
                                    .append(".rls_id_ IN (-1) AND ")
                                    .append(tableName).append(".rls_type_ = 2)");
            }

            return rlsBuild.toString();
        } catch (Exception e) {
            // There was an Error.
            return "";
        }
    }

    private static Long[] getRLS_IDs(HashMap<Long, short[]> rlsMap, C.QueryType queryType) {
        try {
            // Here we get all the IDs and the Related CRUD Level According to  
            // The Query Type.
            // Setup the Query Type.
            short rlsCRUDIndex = -1;
            switch (queryType) {
                case SELECT:
                    rlsCRUDIndex = 0;
                    break;
                case INSERT:
                    rlsCRUDIndex = 1;
                    break;
                case UPDATE:
                    rlsCRUDIndex = 2;
                    break;
                case DELETE:
                    rlsCRUDIndex = 3;
                    break;
            }
            // Get the IDs of the RLS that is to be included.
            ArrayList<Long> rlsIDArr = new ArrayList<>();
            Iterator rlsIter = rlsMap.keySet().iterator();
            // Get the CRUDs from the HashMap and filter.
            while (rlsIter.hasNext()) {
                // Get the Short Array From the Hashmap.
                // Add it to the ArrayList.
                long rlsID = Long.parseLong(rlsIter.next().toString());
                if (rlsMap.get(rlsID)[rlsCRUDIndex] == 1) {
                    // The RLS has the right CRUD Level.
                    // Add the ID into the Array List.
                    rlsIDArr.add(rlsID);
                }
            }
            // Now we have got the RLS IDs extracted.
            return rlsIDArr.toArray(new Long[rlsIDArr.size()]);
        } catch (Exception e) {
            // There was an Error
            return new Long[0];
        }
    }

}
