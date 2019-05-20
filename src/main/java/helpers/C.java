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
package helpers;

/**
 *
 * @author karan This class will hold all the static data. It will hold all data
 * that are repeatable but do no change and will be used Throughout the
 * application.
 *
 */
public class C {

    public static final String now = "now()";

    // The DB Cluster Related Values.
    public static class Clust {

	// IP Addresses of the whole cluster.
	//public static final String DB_CLUSTER_IP = "10.150.0.2:26257";
        public static final String DB_CLUSTER_IP = "172.31.4.89:26257";
	//public static final String DB_CLUSTER_IP = "localhost:26257";
	public static final String CLOUD_PUSH_IP = "10.150.0.4:8082";
	public static final String CLOUD_DB_IP = "127.0.0.1:8080";
	public static final String CLOUD_DB = "CloudDB/";
	// The main core database.
	public static final String CORE_DB = "cloud_core";
        // The Key Value Store Database.
        public static final String CLOUD_KV = "cloud_kv";
	// Core database Credentials
	public static final String CLOUD_USER = "cloud_core_user";
	public static final String CLOUD_PASSWORD = "S$xSW*Wq2k-Br-dAE8cyg-h@#5G2FW?Q";
	// Key Value database Credentials
	public static final String CLOUD_KV_USER = "cloud_kv_user";
	public static final String CLOUD_KV_PASSWORD = "mqyHzyb4YbxfNr25D2BVA2Lj5xNkvLa3";
	// Additional Common features for the whole cluster.
	public static final String SSL_STATUS = "disable";
	// App ID in Cloud Push.
	public static final String CLOUD_PUSH_APP_ID = "2998917646713asds125";
        // API ID of Cloud KV for Cloud DB.
        public static final String CLOUD_KV_API_KEY = "IusrG4a047lcBTON4h2iZLANEC04YHFsjSQH6mCu4WO6KkuSlcND6TgrGhPHQW4Ca4F8H4TcLRr7fuwSWKYJwDcFPxwzIPnKjreQqLZo2WG16bR11V5Dozzaq94DXeZI";
    }

    // Params class will hold the common params.
    public static class Params {

	// The Params.
	public static final String TOKEN = "token";
	public static final String VERSION_NO = "v";
	public static final String TABLE_NAME = "table";
	public static final String SERVER_ID = "server_id";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String CREATE_TIME_MILLI = "create_time_milli";
	public static final String UPDATE_TIME_MILLI = "update_time_milli";

    }

    // Tables Class will hold the names of the tables and the columns of the tables.
    public static class Tables {

	// The Tables.
	public static final String SUPER_ADMINS = "super_admins";
	public static final String SUPER_ADMINS_QUERIES = "super_admins_queries";
	public static final String COMPANIES = "companies";
	public static final String COMPANY_ADDRESSES = "company_addresses";
	public static final String ACCOUNTS = "accounts";
	public static final String PROJECTS = "projects";
	public static final String PROJECT_LOGS = "project_logs";
	public static final String DATABASES = "databases";
	public static final String TENANTS = "tenants";
	public static final String TABLES = "tables";
	public static final String APP_SERVERS = "app_servers";
	public static final String ROLES = "roles";
	public static final String GROUPS = "groups";
	public static final String SERVER_USERS = "server_users";
	public static final String SERVER_USER_QUERIES = "server_user_queries";
	public static final String USERS = "users";
	public static final String DEVICES = "devices";
	public static final String USER_QUERIES = "user_queries";
	public static final String SERVER_USER_AUTH = "server_user_auth";
	public static final String USER_AUTH = "user_auth";
	public static final String API_LOGS = "api_logs";
	public static final String TABLE_ROLE_GRANTS = "table_role_grants";
	public static final String ROLE_SYNC = "role_sync";

	// Super Admin Table Column Names.
	public static class SuperUser {

	    // The Columns.
	    public static final String ID = "id";
	    public static final String FIRST_NAME = "first_name";
	    public static final String LAST_NAME = "last_name";
	    public static final String USERNAME = "username";
	    public static final String PASSWORD = "password";
	    public static final String AUTH_KEY = "auth_key";
	    public static final String PREV_AUTH_KEY = "prev_auth_key";
	    public static final String ROLE = "role";
	    public static final String PARENT_ROLE = "parent_role";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Super Admin Query Table Column Names.
	public static class SuperUserQueries {

	    // The Columns.
	    public static final String ID = "id";
	    public static final String SUPER_ADMIN_ID = "super_admin_id";
	    public static final String QUERY = "query";
	    public static final String RESULT = "result";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Companies Table Column Names.
	public static class Companies {

	    // The Columns.
	    public static final String ID = "id";
	    public static final String NAME = "name";
	    public static final String ALIAS = "alias";
	    public static final String INDUSTRY = "industry";
	    public static final String EMAIL_ID = "email_id";
	    public static final String PHONE_NUMBER = "phone_number";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Companies Addresses Table Column Names.
	public static class CompaniesAddresses {

	    // The Columns.
	    public static final String ID = "id";
	    public static final String COMPANY_ID = "company_id";
	    public static final String ADDRESS_LINE1 = "address_line1";
	    public static final String ADDRESS_LINE2 = "address_line2";
	    public static final String ADDRESS_LINE3 = "address_line3";
	    public static final String CITY = "city";
	    public static final String STATE = "state";
	    public static final String LT = "lt";
	    public static final String LN = "ln";
	    public static final String IS_HQ = "is_hq";
	    public static final String IS_BILLING = "is_billing";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Accounts Table Column Names.
	public static class Accounts {

	    // The Types.
	    public static class Type {

		public static String COMPANY = "company";
		public static String INDIVIDUAL = "individual";
	    }

	    // The Columns.
	    public static final String ID = "id";
	    public static final String FIRST_NAME = "first_name";
	    public static final String LAST_NAME = "last_name";
	    public static final String COMPANY_ID = "company_id";
	    public static final String TYPE = "type";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Projects Table Column Names.
	public static class Projects {

	    // The Columns.
	    public static final String ID = "id";
	    public static final String ACCOUNT_ID = "account_id";
	    public static final String NAME = "name";
	    public static final String CODE_NAME = "code_name";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Projects Logs Table Column Names.
	public static class ProjectLogs {

	    // The Columns.
	    public static final String ID = "id";
	    public static final String PROJECT_ID = "project_id";
	    public static final String LOG_STACK = "log_stack";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Databases Table Column Names.
	public static class Databases {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String PROJECT_ID = "project_id";
	    public static final String NAME = "name";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Tenants Table Column Names.
	public static class Tenants {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String DATABASE_ID = "database_id";
	    public static final String NAME = "name";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Table Table Column Names.
	public static class Table {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String DATABASE_ID = "database_id";
	    public static final String NAME = "name";
	    public static final String SYNCABLE = "syncable";
	    public static final String MULTI_TENANT = "multi_tenant";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// AppServers Table Column Names.
	public static class AppServers {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String PROJECT_ID = "project_id";
	    public static final String NAME = "name";
	    public static final String APP_LANGUAGE = "app_language";
	    public static final String AUTH_KEY = "auth_key";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Roles Table Column Names.
	public static class Roles {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String DATABASE_ID = "database_id";
	    public static final String TENANT_ID = "tenant_id";
	    public static final String PARENT_ID = "parent_id";
	    public static final String NAME = "name";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// ServerUsers Table Column Names.
	public static class ServerUsers {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String DATABASE_ID = "database_id";
	    public static final String USERNAME = "username";
	    public static final String PASSWORD = "password";
	    public static final String AUTH_KEY = "auth_key";
	    public static final String PREV_AUTH_KEY = "prev_auth_key";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// ServerUsersQueries Table Column Names.
	public static class ServerUsersQueries {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String SERVER_USER_ID = "server_user_id";
	    public static final String QUERY = "query";
	    public static final String APP_SERVER_ID = "app_server_id";
	    public static final String RESULT = "result";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Users Table Column Names.
	public static class Users {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String DATABASE_ID = "database_id";
	    public static final String TENANT_ID = "tenant_id";
	    public static final String ROLE_ID = "role_id";
	    public static final String USERNAME = "username";
	    public static final String PASSWORD = "password";
	    public static final String AUTH_KEY = "auth_key";
	    public static final String PREV_AUTH_KEY = "prev_auth_key";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// UserQueries Table Column Names.
	public static class UserQueries {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String USER_ID = "user_id";
	    public static final String TENANT_ID = "tenant_id";
	    public static final String DEVICE_ID = "device_id";
	    public static final String QUERY = "query";
	    public static final String RESULT = "result";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// Devices Table Column Names.
	public static class Devices {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String USER_ID = "user_id";
	    public static final String TYPE = "type";
	    public static final String SERVER_ID = "server_id";
	    public static final String TOKEN = "token";
	    public static final String SERVER_AUTH = "server_auth";
	    public static final String DEVICE_ID = "device_id";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// ServerUserAuth Table Column Names.
	public static class ServerUserAuth {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String DATABASE_ID = "database_id";
	    public static final String PROJECT_ID = "project_id";
	    public static final String AUTH_KEY = "auth_key";
	    public static final String PREV_AUTH_KEY = "prev_auth_key";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// UserAuth Table Column Names.
	public static class UserAuth {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String DATABASE_ID = "database_id";
	    public static final String TENANT_ID = "tenant_id";
	    public static final String ROLE_ID = "role_id";
	    public static final String AUTH_KEY = "auth_key";
	    public static final String PREV_AUTH_KEY = "prev_auth_key";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// ApiLogs Table Column Names.
	public static class ApiLogs {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String USER_ID = "user_id";
	    public static final String USER_TYPE = "user_type";
	    public static final String API = "api";
	    public static final String PARAMS = "params";
	    public static final String VERSION_NO = "version_no";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// TableRoleGrants Table Column Names.
	public static class TableRoleGrants {

	    // The Column Names
	    public static final String ROLE_ID = "role_id";
	    public static final String TABLE_ID = "table_id";
	    public static final String READ = "read";
	    public static final String WRITE = "write";
	    public static final String EDIT = "edit";
	    public static final String REMOVE = "remove";
	    public static final String SYNCABLE = "syncable";
	    public static final String TIME_STAMP = "time_stamp";

	}

	// RoleSync Table Column Names.
	public static class RoleSync {

	    // The Column Names
	    public static final String ID = "id";
	    public static final String ROLE_ID = "role_id";
	    public static final String SYNC_DATA = "sync_data";
	    public static final String TIME_STAMP = "time_stamp";

	}

    }

    // These columns are present in all the tables to help maintain integrity of the system.
    public static class CommonColumns {

	// The columns that are common to all the tables.
	public static final String UPDATE_TIME = "update_time_";

    }

    //Here We will Define all the Query Types,
    // Like SELECT, INSERT, UPDATE and DELETE.
    public enum QueryType {
	SELECT, INSERT, UPDATE, DELETE
    }

    public static class RLS {

	public static final short NO_RLS = 0;
	public static final short ROW_RLS = 1;
	public static final short GROUP_RLS = 2;

    }

}
