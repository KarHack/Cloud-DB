/* 
 * This Database is for Maintaining all the Tables for the Socket Push Message System. 
 * This system is something similar to Firebase Cloud Messaging.
 * This System will be able to push data effectively to Windows 7 and Up, Web, Android, IOS Devices.
 *
 */

/* Let's Create a Database for the System. */ 
CREATE DATABASE cloud_ozi;

/* Create User Core Admin */
CREATE USER cloud_ozi_user WITH PASSWORD 'w&6@k^b7Vp2CFcW@bvG@SUq+p6y7eH*K';

/* Select the Database that has to be used fo the Following Queries.*/
SET DATABASE = cloud_ozi;

/* Maintain all the Devices Linked to the System. */
CREATE TABLE IF NOT EXISTS devices (
token_id STRING(128) PRIMARY KEY CONSTRAINT valid_token CHECK(LENGTH(token_id) = 128),
device_id STRING NOT NULL,
app_id STRING NOT NULL,
type STRING(10) NOT NULL CONSTRAINT valid_type CHECK(type IN ('windows', 'web', 'android', 'ios', 'server')),
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
UNIQUE (device_id, app_id));

/* Maintain all the Sockets of Every Device Connected to the System. */
CREATE TABLE IF NOT EXISTS device_sockets (
socket_id STRING(24) NOT NULL PRIMARY KEY CONSTRAINT valid_socket CHECK (LENGTH(socket_id) > 18),
token STRING(128) CONSTRAINT valid_token CHECK (LENGTH(token) = 128),
window_id SERIAL NOT NULL,
type STRING(10) NOT NULL CONSTRAINT valid_type CHECK (type IN ('windows', 'web', 'android', 'ios', 'server')),
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
UNIQUE (token, window_id),
INDEX (token),
CONSTRAINT device_fk FOREIGN KEY (token) REFERENCES devices(token_id));

/* Maintain all the APN (Apple Push Notification) and FCM (Firebase Push Notification) Tokens and Details.
   This will be used to send messages to the devices.
   */
CREATE TABLE IF NOT EXISTS apn_fcm_tokens (
token STRING(128) PRIMARY KEY CONSTRAINT valid_token CHECK (LENGTH(token) = 128),
application_id STRING NOT NULL,
server_id STRING NOT NULL,
server_auth STRING NOT NULL,
type STRING(10) NOT NULL CONSTRAINT valid_type CHECK (type IN ('android', 'ios')),
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
CONSTRAINT device_fk FOREIGN KEY (token) REFERENCES devices(token_id));

/* Store all the Messages that have to be delivered to the Devices.
   We will delete the Messages from this database as soon as the Message is Delivered with Integrity Check. */
CREATE TABLE IF NOT EXISTS messages (
message_id UUID PRIMARY KEY DEFAULT uuid_v4()::UUID,
token STRING(128) CONSTRAINT valid_token CHECK (LENGTH(token) = 128),
message STRING NOT NULL,
checksum STRING(32) NOT NULL CONSTRAINT valid_checksum CHECK (LENGTH(checksum) = 32),
time_stamp TIMESTAMP DEFAULT clock_timestamp(),
CONSTRAINT device_fk FOREIGN KEY (token) REFERENCES devices(token_id));

 
/* Grant user priviledges here. */
GRANT SELECT ON TABLE cloud_ozi.* TO cloud_ozi_user;
GRANT INSERT ON TABLE cloud_ozi.* TO cloud_ozi_user;
GRANT UPDATE ON TABLE cloud_ozi.* TO cloud_ozi_user;
GRANT DELETE ON TABLE cloud_ozi.* TO cloud_ozi_user;


/* * Here we will Insert the startup data for this database. * */



