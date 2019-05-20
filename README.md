# Cloud DB
Offline Syncing Database Wrapper with Row Level Security &amp; Multi-tenancy capabilities.  Allows applications to work in Low to No Internet Connectivity mode.  Automatically synchronizes data when the device was offline.
This is a thin wrapper which allows the user to interact with the database & enables the following functionalities.

# Functionality
1. REST HTTP Endpoint to Retrieve data from an SQL Database.
2. JOINs & ACID Properties of the Underlying SQL Database.
3. GraphQL Type Functionality of the Client being able to ask for only the data that the client requires.
4. Multi-Tenancy
5. Ability to work in Low & No Internet conditions as Well
6. Automatically Synchronizes data to & from the server when the device connects to the network.
7. Automatically converts specific values to prepared statements & has multiple measures to safe guard against SQL Injection & Other Hacking Mechanisms.
8. APIs & Functions for Math Operations over the database.

# Limitations
1. Currently only Supports Postgres SQL driver.
2. Offline Syncing Support only on WPF (Windows)

# Roadmap
1. Client Libraries for all Major Platforms & Languages.
2. HTTP API Triggers on a CRUD operation over a table.
3. Support for MySQL & Other SQL Database Drivers
4. Ability to Interact with a Caching Database for Hyper Fast Data Retrievals.
5. Ability to Interact with Elastic Search to Assist in full text searches without having the over head of maintaining multiple databases.

# Syncing Technology Advantages
1. Only Syncs the Delta changes. So it is very fast & low on bandwidth
2. Ability to Sync with Operations. For Eg. In an Inventory Management system, if the stock is 5 & 10 new items are being added, then we don't need to update the database with '15' the sum, we just need to update with +10.  This will automatically increment it accordingly. This ensures that data is always consistent over all the devices in all conditions.

# Description
We have built this wrapper mainly over CockroachDB, which allows us to have a highly scalable database at the Core.

# Advantage to Other Products
We allow the developer to perform Joins & take advantage of ACID properties to a certain extent.  Most other products do not allow these functionalities.

# Changelog
1. Initial Commit. This has the stable version of V0.1 of Cloud DB.  It syncs data according to the RLS & works with multiple tenants. 
2. Small Bug Fixes.
