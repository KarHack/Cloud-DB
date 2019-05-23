/* 
 * This is the main database, thats going to handle authentication and sync.
 */
/* Create Database. */
CREATE DATABASE cloud_core;

/* Create User Core Admin */
CREATE USER cloud_core_user WITH PASSWORD 'S$xSW*Wq2k-Br-dAE8cyg-h@#5G2FW?Q';

/* Set the Database */
SET DATABASE = cloud_core;

/* Create the Super Admins Table. */
CREATE TABLE IF NOT EXISTS super_admins (
id SERIAL PRIMARY KEY,
first_name STRING(50) NOT NULL, 
last_name STRING(50) NOT NULL, 
username STRING(50) UNIQUE NOT NULL, 
password STRING(32) NOT NULL CONSTRAINT valid_password CHECK (LENGTH(password) = 32),
role STRING(50) DEFAULT NULL, 
parent_role STRING(50) DEFAULT NULL, 
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (first_name, last_name));

/* Create table to Maintain all the Companies. */
CREATE TABLE IF NOT EXISTS companies (
id SERIAL PRIMARY KEY,
name STRING(100) NOT NULL,
alias STRING(50) DEFAULT NULL,
industry STRING(50) DEFAULT NULL,
email_id STRING(50) NOT NULL CONSTRAINT valid_email_id CHECK (email_id LIKE '%_@__%.__%' AND email_id = LOWER(email_id)),
phone_number BIGINT NOT NULL CONSTRAINT valid_phone_number CHECK (LENGTH(phone_number::STRING) BETWEEN 8 AND 12),
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (name),
UNIQUE (email_id),
UNIQUE (phone_number));

/* Create table to Maintain all the Addresses related to the Company */
CREATE TABLE IF NOT EXISTS company_addresses (
id UUID PRIMARY KEY DEFAULT uuid_v4()::UUID,
company_id INT NOT NULL,
address_line1 STRING(100) DEFAULT NULL,
address_line2 STRING(100) DEFAULT NULL,
address_line3 STRING(100) DEFAULT NULL,
city STRING(50) NOT NULL,
state STRING(50) NOT NULL,
lt DECIMAL DEFAULT 0.0,
ln DECIMAL DEFAULT 0.0,
is_hq BOOLEAN DEFAULT false CONSTRAINT valid_is_hq CHECK (is_hq IN (true, false)),
is_billing BOOLEAN DEFAULT false CONSTRAINT valid_is_billing CHECK (is_billing IN (true, false)),
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (company_id),
CONSTRAINT company_fk FOREIGN KEY (company_id) REFERENCES companies(id));

/* Create table to Maintain all the Accounts linked to their 
   respective companies in the system. */
CREATE TABLE IF NOT EXISTS accounts (
id SERIAL PRIMARY KEY,
first_name STRING(50) NOT NULL, 
last_name STRING(50) NOT NULL, 
company_id INT NOT NULL,
type STRING(10) NOT NULL CHECK(type IN ('company', 'individual')),
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (company_id),
CONSTRAINT company_fk FOREIGN KEY (company_id) REFERENCES companies(id));

/* Create table to Maintain all the Projects linked to each Account. */
CREATE TABLE IF NOT EXISTS projects (
id SERIAL PRIMARY KEY,
account_id INT NOT NULL,
name STRING(24) NOT NULL CONSTRAINT valid_project_name CHECK(LENGTH(name) > 5),
code_name STRING(16) NOT NULL CONSTRAINT valid_project_code CHECK(code_name NOT LIKE '% %' AND LENGTH(code_name) BETWEEN 6 AND 16),
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (account_id),
CONSTRAINT account_fk FOREIGN KEY (account_id) REFERENCES accounts(id));

/* Create table to Maintain all the Project Logs of each project */
CREATE TABLE IF NOT EXISTS project_logs (
id UUID PRIMARY KEY DEFAULT uuid_v4()::UUID,
project_id INT NOT NULL,
log_stack STRING DEFAULT NULL,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (project_id),
CONSTRAINT project_fk FOREIGN KEY (project_id) REFERENCES projects(id));

/* Create table to Maintain all the Databases within the Project.
Here the Database ID will be used as the actual database name in the system. 
*/
CREATE TABLE IF NOT EXISTS databases (
id STRING(10) PRIMARY KEY NOT NULL CONSTRAINT valid_database_id CHECK (id NOT LIKE '% %'),
project_id INT NOT NULL,
name STRING(16) UNIQUE NOT NULL,
username STRING(32) UNIQUE NOT NULL,
password STRING(32) NOT NULL CONSTRAINT valid_password CHECK (LENGTH(password) = 32),
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (project_id),
CONSTRAINT project_fk FOREIGN KEY (project_id) REFERENCES projects(id));

/* Create table to Maintain the tenants within the Each Database */
CREATE TABLE IF NOT EXISTS tenants (
id SERIAL PRIMARY KEY,
database_id STRING(10) NOT NULL,
name STRING(16) NOT NULL,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (database_id),
UNIQUE (database_id, name),
CONSTRAINT database_fk FOREIGN KEY (database_id) REFERENCES databases(id));


/* Create table to Maintain all the Tables in Each Database.
Here the Table ID will be used as the Actual Table Name in the database.
We will also be able to set which table should be scyned with the users, and thus be accessable to normal users.
We can also state the ACL Type of a table, that the table will use Role Based PRIVILEGES or Group Based.
This will work with our role based and group based ACL to provide flexible row based security.
00 - No ACL, or F-RLS (Flexible Row Level Security).
10 - Role Based F-RLS.
11 - Group Based F-RLS.
 */
 
CREATE TABLE IF NOT EXISTS tables (
id STRING(50) PRIMARY KEY,
database_id STRING(10) NOT NULL,
name STRING(50) NOT NULL,
multi_tenant BOOL NOT NULL DEFAULT false,
syncable BOOL NOT NULL DEFAULT false,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (database_id),
UNIQUE (database_id, name),
CONSTRAINT database_fk FOREIGN KEY (database_id) REFERENCES databases(id));

/* Create table to maintain the Servers running
and given access for each app */
CREATE TABLE IF NOT EXISTS app_servers (
server_id SERIAL PRIMARY KEY,
token STRING(96) NOT NULL UNIQUE CONSTRAINT valid_token CHECK (LENGTH(token) = 96),
project_id INT NOT NULL,
name STRING(16) NOT NULL,
app_language STRING(10) NOT NULL,
ip_address STRING(15) NOT NULL DEFAULT '0.0.0.0',
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (project_id),
UNIQUE (project_id, name),
CONSTRAINT project_fk FOREIGN KEY (project_id) REFERENCES projects(id));

/* Create table to Maintain the Server Users */
CREATE TABLE IF NOT EXISTS server_users (
user_id UUID PRIMARY KEY DEFAULT uuid_v4()::UUID,
token STRING(160) NOT NULL UNIQUE CONSTRAINT valid_token CHECK (LENGTH(token) = 160),
database_id STRING(10) NOT NULL,
username STRING(32) NOT NULL,
password STRING(32) NOT NULL,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (database_id),
CONSTRAINT database_fk FOREIGN KEY (database_id) REFERENCES databases(id));

/* Create table to Maintain the Server Users Authentication */
CREATE TABLE IF NOT EXISTS user_server_auth (
token STRING(256) PRIMARY KEY CONSTRAINT valid_token CHECK (LENGTH(token) = 256),
user_token STRING(160) UNIQUE NOT NULL CONSTRAINT valid_user_token CHECK (LENGTH(user_token) = 160),
server_token STRING(96) NOT NULL CONSTRAINT valid_server_token CHECK (LENGTH(server_token) = 96),
user_id UUID NOT NULL,
server_id INT NOT NULL,
database_id STRING(10) NOT NULL,
username STRING(50) NOT NULL,
password STRING(50) NOT NULL,
ip_address STRING(15) NOT NULL DEFAULT '0.0.0.0',
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (database_id),
CONSTRAINT server_user_fk FOREIGN KEY (user_token) REFERENCES server_users(token),
CONSTRAINT app_server_fk FOREIGN KEY (server_token) REFERENCES app_servers(token),
CONSTRAINT server_user_id_fk FOREIGN KEY (user_id) REFERENCES server_users(user_id),
CONSTRAINT app_server_id_fk FOREIGN KEY (server_id) REFERENCES app_servers(server_id),
CONSTRAINT database_fk FOREIGN KEY (database_id) REFERENCES databases(id));

/* Create table to Maintain the Roles of each Tenant in the Databases */
CREATE TABLE IF NOT EXISTS roles (
id SERIAL PRIMARY KEY,
tenant_id INT NOT NULL,
database_id STRING(10) NOT NULL,
name STRING(16) NOT NULL,
parent_id INT DEFAULT 0,
branch STRING NOT NULL,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (database_id),
INDEX (tenant_id),
INDEX (parent_id),
UNIQUE (tenant_id, name),
CONSTRAINT database_fk FOREIGN KEY (database_id) REFERENCES databases(id),
CONSTRAINT tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id));

/* Create table to Maintain the Groups of each Tenant in the Database */
CREATE TABLE IF NOT EXISTS groups (
id SERIAL PRIMARY KEY NOT NULL,
tenant_id INT NOT NULL,
database_id STRING(10) NOT NULL,
name STRING(16) NOT NULL,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (database_id),
INDEX (tenant_id),
UNIQUE (tenant_id, name),
CONSTRAINT database_fk FOREIGN KEY (database_id) REFERENCES databases(id),
CONSTRAINT tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id));

/* Create table to Maintain the Sync Users.
 * These users are normal users and will be running on remote devices.
 */
CREATE TABLE IF NOT EXISTS users (
user_id UUID PRIMARY KEY DEFAULT uuid_v4()::UUID,
token STRING(160) NOT NULL UNIQUE CONSTRAINT valid_token CHECK (LENGTH(token) = 160),
tenant_id INT NOT NULL,
database_id STRING(10) NOT NULL,
username STRING(32) NOT NULL,
password STRING(32) NOT NULL,
is_active BOOL NOT NULL DEFAULT false,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (tenant_id),
CONSTRAINT tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id),
CONSTRAINT database_fk FOREIGN KEY (database_id) REFERENCES databases(id));

/* Create table to Maintain the Table and User Security Map. 
 * Used for the Table Level ACL of the User.
 */
CREATE TABLE IF NOT EXISTS table_user_map (
table_id STRING(50) NOT NULL,
user_id UUID NOT NULL,
read BIT DEFAULT 0,
write BIT DEFAULT 0,
edit BIT DEFAULT 0,
remove BIT DEFAULT 0,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
PRIMARY KEY (table_id, user_id),
INDEX (user_id),
CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(user_id),
CONSTRAINT table_fk FOREIGN KEY (table_id) REFERENCES tables(id));

/* Create table to Maintain the Mappings of the Roles to Every User and the User's Priviledge in that role. */
CREATE TABLE IF NOT EXISTS user_role_map (
user_id UUID NOT NULL,
role_id INT NOT NULL,
read BIT DEFAULT 0,
write BIT DEFAULT 0,
edit BIT DEFAULT 0,
remove BIT DEFAULT 0,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
PRIMARY KEY (user_id, role_id),
INDEX (user_id),
CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(user_id),
CONSTRAINT role_fk FOREIGN KEY (role_id) REFERENCES roles(id));

/* Create table to Maintain the Mappings of the Roles to Every User and the User's Priviledge in that Group. */
CREATE TABLE IF NOT EXISTS user_group_map (
user_id UUID NOT NULL,
group_id INT NOT NULL,
read BIT DEFAULT 0,
write BIT DEFAULT 0,
edit BIT DEFAULT 0,
remove BIT DEFAULT 0,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
PRIMARY KEY (user_id, group_id),
INDEX (user_id),
CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(user_id),
CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES groups(id));

/* Create table to Maintain the Devices linked to each user.
 * And method to push data to them.
 */
CREATE TABLE IF NOT EXISTS devices (
device_uid UUID PRIMARY KEY DEFAULT uuid_v4()::UUID,
token STRING(128) NOT NULL UNIQUE CONSTRAINT valid_token CHECK (LENGTH(token) = 128),
user_id UUID NOT NULL,
type STRING(10) NOT NULL CONSTRAINT valid_type CHECK(type IN ('windows', 'web', 'android', 'ios')),
device_id STRING NOT NULL,
database_id STRING(10) NOT NULL,
push_token STRING NOT NULL,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (user_id),
INDEX (device_id),
UNIQUE (device_id, database_id),
CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(user_id),
CONSTRAINT database_fk FOREIGN KEY (database_id) REFERENCES databases(id));
 
/* Create table to Maintain the User to Device Authentication Tokens. */
CREATE TABLE IF NOT EXISTS user_device_auth (
token STRING(256) PRIMARY KEY CONSTRAINT valid_token CHECK (LENGTH(token) = 256),
user_token STRING(160) NOT NULL CONSTRAINT valid_user_token CHECK (LENGTH(user_token) = 160),
device_token STRING(128) NOT NULL UNIQUE CONSTRAINT valid_device_token CHECK (LENGTH(device_token) = 128),
user_id UUID NOT NULL,
device_uid UUID NOT NULL,
push_token STRING NOT NULL,
database_id STRING(10) NOT NULL,
tenant_id INT NOT NULL,
username STRING(50) NOT NULL,
password STRING(50) NOT NULL,
is_active BOOL NOT NULL DEFAULT false,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (database_id),
INDEX (tenant_id),
CONSTRAINT user_fk FOREIGN KEY (user_token) REFERENCES users(token),
CONSTRAINT device_fk FOREIGN KEY (device_token) REFERENCES devices(token),
CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES users(user_id),
CONSTRAINT device_uid_fk FOREIGN KEY (device_uid) REFERENCES devices(device_uid),
CONSTRAINT database_fk FOREIGN KEY (database_id) REFERENCES databases(id),
CONSTRAINT tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id));

/* Create table to Store all the Database Changes so that 
 * users trying to sync will have one source of truth. 
 * This will run according to each role.
 */
CREATE TABLE IF NOT EXISTS role_sync (
id UUID PRIMARY KEY DEFAULT uuid_v4()::UUID,
role_id INT NOT NULL,
sync_table_name STRING(32) NOT NULL,
sync_data STRING NOT NULL,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (role_id),
CONSTRAINT role_fk FOREIGN KEY (role_id) REFERENCES roles(id));

/* Create table to Store all the Database Changes so that 
 * users trying to sync will have one source of truth. 
 * This will run according to each group.
 */
CREATE TABLE IF NOT EXISTS group_sync (
id UUID PRIMARY KEY DEFAULT uuid_v4()::UUID,
group_id INT NOT NULL,
sync_table_name STRING(32) NOT NULL,
sync_data STRING NOT NULL,
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
INDEX (group_id),
CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES groups(id));


/* Grant user priviledges here. */
GRANT SELECT ON TABLE cloud_core.* TO cloud_core_user;
GRANT INSERT ON TABLE cloud_core.* TO cloud_core_user;
GRANT UPDATE ON TABLE cloud_core.* TO cloud_core_user;
GRANT DELETE ON TABLE cloud_core.* TO cloud_core_user;

/* * Here we will Insert the startup data for this database. * */
INSERT INTO companies (id, name, alias, industry, email_id, phone_number) VALUES (
319813811384090625, '36th Element Technologies Pvt. Ltd.', '36th Element', 
'SAAS', 'info@36thelement.com', 9172430741);

INSERT INTO company_addresses (id, company_id, address_line1, address_line2, address_line3,
city, state, lt, ln, is_hq, is_billing) VALUES (
'2f83749c-5c6d-423a-968a-e707cf00e6e4', 319813811384090625, 
'Dharmendra Apts.', 
'Holy Cross, IC Colony', 
'Borivali West', 'Mumbai', 
'Maharashtra', '19.252', '72.850', true, true);

INSERT INTO accounts (id, first_name, last_name, company_id, type) VALUES (
319814699588911105, 'Karan', 'Soi', 319813811384090625, 'company');

INSERT INTO projects (id, account_id, name, code_name) VALUES (
319834699588911106, 319814699588911105, 'Cloud Push', 'cloud_ozi');

INSERT INTO project_logs (id, project_id, log_stack) VALUES (
'7f341b9c-1e85-4d0e-92b7-6b8dd1de619b', 319834699588911106, 'Project Created');

INSERT INTO projects (id, account_id, name, code_name) VALUES (
319834699588911107, 319814699588911105, '36E Business', 'a36e_biz');

INSERT INTO app_servers (server_id, token, project_id, name, app_language, ip_address) VALUES (
319831292588911106, 'UvxqU44AeYAB9CT8KWLUrYcWKFbkdjBNPxxeCP5qjzNZWqvB8X3z9yssAHzRwVXsUWWCf3vw5kn7pgjS6CBVqjNM8DHA2FuB', 319834699588911106, 'Cloud Push - 1', 'Node.js', '0.0.0.0');

INSERT INTO app_servers (server_id, token, project_id, name, app_language, ip_address) VALUES (
319831292588911107, 'UNdFSxYyCZYgGVfzQyGb6k9hhRQ9fLcTsdpWRmXNEAAJr8r2b9KXCE8fJD3usU5Cp9QR4227BLX8hHcRdrs4JuRQPZBMPZbf', 319834699588911107, '36E Business - 1', 'Java', '0.0.0.0');

/* Start inserts for Cloud Push */
INSERT INTO databases (id, project_id, name, username, password) VALUES (
'cloud_ozi', 319834699588911106, 'Cloud Push', 'cloud_ozi_user', 'w&6@k^b7Vp2CFcW@bvG@SUq+p6y7eH*K');

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'devices', 'cloud_ozi', 'devices', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'device_sockets', 'cloud_ozi', 'device_sockets', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'apn_fcm_tokens', 'cloud_ozi', 'apn_fcm_tokens', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'messages', 'cloud_ozi', 'messages', false, false);

/* Start inserts for 36E Business */
INSERT INTO databases (id, project_id, name, username, password) VALUES (
'a36e_biz', 319834699588911107, '36E Business', 'a36e_biz_user', 'Ht2R?=cs8@WunR&M3m93swx%xGFehRWp');


INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'industries', 'a36e_biz', 'industries', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'countries', 'a36e_biz', 'countries', false, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'states', 'a36e_biz', 'states', false, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'account_managers', 'a36e_biz', 'account_managers', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'customers', 'a36e_biz', 'customers', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'enquiries', 'a36e_biz', 'enquiries', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'account_manager_targets', 'a36e_biz', 'account_manager_targets', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'interactions', 'a36e_biz', 'interactions', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'tickets', 'a36e_biz', 'tickets', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'feedback', 'a36e_biz', 'feedback', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'accounts', 'a36e_biz', 'accounts', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'demos', 'a36e_biz', 'demos', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'am_feedback', 'a36e_biz', 'am_feedback', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'email_subscribers', 'a36e_biz', 'email_subscribers', false, false);



INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'companies', 'a36e_biz', 'companies', false, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'company_billing_address', 'a36e_biz', 'company_billing_address', false, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'office_addresses', 'a36e_biz', 'office_addresses', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'users', 'a36e_biz', 'users', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'features', 'a36e_biz', 'features', false, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'features_table_map', 'a36e_biz', 'features_table_map', false, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'feature_role_map', 'a36e_biz', 'feature_user_rls_map', true, true);


/* The Actual Functionality of the Software. */
/* Give Meta data About the Tables. */
/* Consumer Tables. */
INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'consumers', 'a36e_biz', 'consumers', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'consumer_billing', 'a36e_biz', 'consumer_billing', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'consumer_addresses', 'a36e_biz', 'consumer_addresses', true, true);


/* Tables Related to Products. */
INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_categories', 'a36e_biz', 'product_categories', false, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'similar_product_categories', 'a36e_biz', 'similar_product_categories', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'uom', 'a36e_biz', 'uom', false, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_master', 'a36e_biz', 'product_master', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_master_images', 'a36e_biz', 'product_master_images', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_variations', 'a36e_biz', 'product_variations', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_variation_images', 'a36e_biz', 'product_variation_images', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_additions', 'a36e_biz', 'product_additions', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_attributes', 'a36e_biz', 'product_attributes', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_brands', 'a36e_biz', 'product_brands', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_warranties', 'a36e_biz', 'product_warranties', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_kit', 'a36e_biz', 'product_kit', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_kit_products', 'a36e_biz', 'product_kit_products', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_wishlists', 'a36e_biz', 'product_wishlists', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'recently_viewed_products', 'a36e_biz', 'recently_viewed_products', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'viewed_products', 'a36e_biz', 'viewed_products', false, false);


/* Tables Related to Stores */
INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'stores', 'a36e_biz', 'stores', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'store_addresses', 'a36e_biz', 'store_addresses', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'store_timings', 'a36e_biz', 'store_timings', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'store_staff', 'a36e_biz', 'store_staff', true, true);


/* Tables Related to Warehouse */
INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'warehouses', 'a36e_biz', 'warehouses', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'warehouse_addresses', 'a36e_biz', 'warehouse_addresses', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'warehouse_staff', 'a36e_biz', 'warehouse_staff', true, true);


/* Tables Related to Transporter */
INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'transporters', 'a36e_biz', 'transporters', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'transporter_vehicals', 'a36e_biz', 'transporter_vehicals', true, true);


/* Tables Related to Inventory. */
INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'store_inventory_ledger', 'a36e_biz', 'store_inventory_ledger', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'warehouse_inventory_ledger', 'a36e_biz', 'warehouse_inventory_ledger', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'transport_inventory_ledger', 'a36e_biz', 'transport_inventory_ledger', true, true);


/* Tables Related to Sales. */
INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'sales', 'a36e_biz', 'sales', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'sale_addresses', 'a36e_biz', 'sale_addresses', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'sale_saleperson', 'a36e_biz', 'sale_saleperson', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'sale_transporter', 'a36e_biz', 'sale_transporter', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'sale_customer', 'a36e_biz', 'sale_customer', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'sale_products', 'a36e_biz', 'sale_products', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_groups', 'a36e_biz', 'product_groups', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'prod_group_products', 'a36e_biz', 'prod_group_products', true, true);

/*  */



/* Handle the Relation between the Tables & Users */
INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'buyer_addresses', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'buyers', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'companies', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'consumer_addresses', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'consumer_billing', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'consumers', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'countries', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 0, 0, 0);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'states', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 0, 0, 0);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'feature_table_map', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'feature_user_rls_map', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'features', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'office_addresses', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'users', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'company_billing_address', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'sales', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'sale_customer', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'sale_addresses', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'sale_saleperson', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'sale_transporter', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);



INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_categories', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'similar_product_categories', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'uom', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_master', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_master_images', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_variations', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_variation_images', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_additions', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_attributes', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_brands', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_warranties', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_kit', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_kit_products', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_wishlists', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'recently_viewed_products', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'stores', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'store_addresses', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'store_timings', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'store_staff', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'store_inventory_ledger', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'warehouses', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'warehouse_addresses', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'warehouse_staff', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'warehouse_inventory_ledger', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'transporters', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'transporter_vehicals', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'transport_inventory_ledger', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'sale_products', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'product_groups', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);

INSERT INTO table_user_map (table_id, user_id, read, write, edit, remove) VALUES (
'prod_group_products', 'd72f2c93-6e2d-43e8-9f5e-6df1206a838c', 1, 1, 1, 1);






/*
INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'account_activity', 'a36e_biz', 'account_activity', false, false);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'outlets', 'a36e_biz', 'outlets', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'outlet_address', 'a36e_biz', 'outlet_address', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'warehouses', 'a36e_biz', 'warehouses', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'warehouse_address', 'a36e_biz', 'warehouse_address', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'factories', 'a36e_biz', 'factories', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'factory_address', 'a36e_biz', 'factory_address', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'vendors', 'a36e_biz', 'vendors', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'vendor_addresses', 'a36e_biz', 'vendor_addresses', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'vendor_bank_details', 'a36e_biz', 'vendor_bank_details', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'buyers_consignors', 'a36e_biz', 'buyers_consignors', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'buyer_consignor_addresses', 'a36e_biz', 'buyer_consignor_addresses', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'buyer_consignor_bank_details', 'a36e_biz', 'buyer_consignor_bank_details', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_categories', 'a36e_biz', 'product_categories', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_groups', 'a36e_biz', 'product_groups', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'brand_manufacturers', 'a36e_biz', 'brand_manufacturers', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'brands', 'a36e_biz', 'brands', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'generic_products', 'a36e_biz', 'generic_products', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'uom', 'a36e_biz', 'uom', false, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_master', 'a36e_biz', 'product_master', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'product_attributes', 'a36e_biz', 'product_attributes', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'outlet_product_instances', 'a36e_biz', 'outlet_product_instances', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'warehouse_product_instances', 'a36e_biz', 'warehouse_product_instances', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'factory_product_instances', 'a36e_biz', 'factory_product_instances', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'outlet_product_inventory', 'a36e_biz', 'outlet_product_inventory', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'warehouse_product_inventory', 'a36e_biz', 'warehouse_product_inventory', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'factory_product_inventory', 'a36e_biz', 'factory_product_inventory', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'branch_transfers', 'a36e_biz', 'branch_transfers', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'branch_transfers_products', 'a36e_biz', 'branch_transfers_products', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'purchases', 'a36e_biz', 'purchases', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'purchase_vouchers', 'a36e_biz', 'purchase_vouchers', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'pv_products', 'a36e_biz', 'pv_products', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'pv_additional_charges', 'a36e_biz', 'pv_additional_charges', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'buyer_purchase_order', 'a36e_biz', 'buyer_purchase_order', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'po_buyer_product', 'a36e_biz', 'po_buyer_product', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'po_buyer_additional_charges', 'a36e_biz', 'po_buyer_additional_charges', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'sales', 'a36e_biz', 'sales', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'seller_purchase_order', 'a36e_biz', 'seller_purchase_order', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'po_seller_product', 'a36e_biz', 'po_seller_product', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'po_seller_additional_charges', 'a36e_biz', 'po_seller_additional_charges', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'performa_invoices', 'a36e_biz', 'performa_invoices', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'pi_products', 'a36e_biz', 'pi_products', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'pi_additional_charges', 'a36e_biz', 'pi_additional_charges', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'delivery_challans', 'a36e_biz', 'delivery_challans', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'dc_products', 'a36e_biz', 'dc_products', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'dc_purchase_order_links', 'a36e_biz', 'dc_purchase_order_links', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'invoices', 'a36e_biz', 'invoices', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'po_invoice_links', 'a36e_biz', 'po_invoice_links', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'dc_invoice_links', 'a36e_biz', 'dc_invoice_links', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'invoice_products', 'a36e_biz', 'invoice_products', true, true);

INSERT INTO tables (id, database_id, name, multi_tenant, syncable) VALUES (
'invoice_additional_charges', 'a36e_biz', 'invoice_additional_charges', true, true);
*/

/* Insert the Applications into the Table. */
INSERT INTO applications (database_id, project_id, name) VALUES (
'cloud_ozi', 319834699588911106, 'Node js - Cloud Push');

INSERT INTO applications (database_id, project_id, name) VALUES (
'a36e_biz', 319834699588911107, 'Java - 36E Business');

/* Insert the Required Server Users */
INSERT INTO server_users (token, database_id, username, password) VALUES (
'GxgwHsMq3htj4WNQfEHxRxzwKV78YNUZGbp7vnn8JfaZswb66wQhhdBzTUQ9PN37SvK23pcfnrG4mQ4Q5e3YHw3cuRNceFZWVC5BJNmmUug9bPsDnTd2aCPYhvuvM7wFMQCzjYc35B2kbEeb9KJZ4Kz9cUzpKp2S', 'cloud_ozi', 'cloud_ozi_user', 'w&6@k^b7Vp2CFcW@bvG@SUq+p6y7eH*K');

INSERT INTO server_users (token, database_id, username, password) VALUES (
'9xjqjJUgMAVPLzyxYnKdwchHLCpqKLTqEzPDYFmhMAD5Wvzhz5huBFNxuep63TjRHZXE8NMZ2z88hBrYCZrbW84nxJRkxyx9vLHHS5WqtMvcjmecjYurdT2fTSCz3rSuuzRXDj7E63gTYzFnJWggA5cy4tabd4aS', 'a36e_biz', 'a36e_biz_user', 'Ht2R?=cs8@WunR&M3m93swx%xGFehRWp');

/* Insert the Server Users into the User Server Authentication Table. */
INSERT INTO user_server_auth (token, user_token, server_token, user_id, 
server_id, database_id, username, password, ip_address) VALUES (
'GqBXWzv8yvu3q4s9UAxUsYBHx4gwBeAHsMqAR3Chtj4WzN9QfEHx8RxzwVKV7T8YNUwZNGbp7vnnW8JZfaZswbK66wQshhdBzTUQ29XPN37SvK2F3pcUfnrG4mQ4QL5ej3YHw3cuRNzceFZWVC5BWJNmmUugH9bPUsDnTdA52aCPYhvuqvYM7wFMQCzjYCr8c35B2kPbDEeb9xKJZ4KzeN9PcUzpBMxKpd2qNbKjckjFBWSWf6VvCj3CpnS5g7kw', 'GxgwHsMq3htj4WNQfEHxRxzwKV78YNUZGbp7vnn8JfaZswb66wQhhdBzTUQ9PN37SvK23pcfnrG4mQ4Q5e3YHw3cuRNceFZWVC5BJNmmUug9bPsDnTd2aCPYhvuvM7wFMQCzjYc35B2kbEeb9KJZ4Kz9cUzpKp2S', 'UvxqU44AeYAB9CT8KWLUrYcWKFbkdjBNPxxeCP5qjzNZWqvB8X3z9yssAHzRwVXsUWWCf3vw5kn7pgjS6CBVqjNM8DHA2FuB', 'b3b4581a-950d-4e57-8466-f4646050946c', 319831292588911106, 'cloud_ozi', 'cloud_ozi_user', 'w&6@k^b7Vp2CFcW@bvG@SUq+p6y7eH*K', '0.0.0.0');

INSERT INTO user_server_auth (token, user_token, server_token, user_id, 
server_id, database_id, username, password, ip_address) VALUES (
'9829rXrbENbKFxfCUydS8ZfDxYjqgCJjJUgYuMVAVPLz3yGxYnKdzwchHULCpfqKLTsqAEzPDYFmyhMJAD5WvzQhz5hCuBFNxuepP653TjRHZXEZ8NMbZ2z88hBrYGCZErbW84nxJRAkxyx9vLHH9S5WqtMvBcjmpecjYuMXrdT2fTSCNzk3rSuuzRXDjR6P7E63gTmYZzFnJpWggA5cWRys4tabcQdd4faJTQhL99uRshSQ4d42Rc2rhXRBH8L7', '9xjqjJUgMAVPLzyxYnKdwchHLCpqKLTqEzPDYFmhMAD5Wvzhz5huBFNxuep63TjRHZXE8NMZ2z88hBrYCZrbW84nxJRkxyx9vLHHS5WqtMvcjmecjYurdT2fTSCz3rSuuzRXDj7E63gTYzFnJWggA5cy4tabd4aS', 'UNdFSxYyCZYgGVfzQyGb6k9hhRQ9fLcTsdpWRmXNEAAJr8r2b9KXCE8fJD3usU5Cp9QR4227BLX8hHcRdrs4JuRQPZBMPZbf', 'd45ac9bd-e29e-4aea-a643-dc2a91bbf0b6', 319831292588911107, 'a36e_biz', 'a36e_biz_user', 'Ht2R?=cs8@WunR&M3m93swx%xGFehRWp', '0.0.0.0');

