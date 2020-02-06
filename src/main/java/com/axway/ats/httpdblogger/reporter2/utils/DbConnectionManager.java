/*
 * Copyright 2019 Axway Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axway.ats.httpdblogger.reporter2.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.axway.ats.core.dbaccess.DbConnection;
import com.axway.ats.core.dbaccess.DbUtils;
import com.axway.ats.core.dbaccess.mssql.DbConnSQLServer;
import com.axway.ats.core.dbaccess.postgresql.DbConnPostgreSQL;
import com.axway.ats.httpdblogger.reporter2.connections.exceptions.NoSuchDbConnectionException;
import com.axway.ats.httpdblogger.reporter2.exceptions.PojoValidationException;
import com.axway.ats.log.autodb.io.PGDbReadAccess;
import com.axway.ats.log.autodb.io.SQLServerDbReadAccess;
import com.axway.ats.log.autodb.model.IDbReadAccess;

public class DbConnectionManager {

    // DbConnection ID -> DB connect data
    private static Map<String, DbConnectionData> dbConnections = java.util.Collections.synchronizedMap(new HashMap<String, DbConnectionData>());

    static {
        
    }

    public static boolean doesDbConnectionExists( String dbConnectionId ) {

        return dbConnections.containsKey(dbConnectionId);
    }

    public static IDbReadAccess getReadAccess( String dbConnectionId ) throws NoSuchDbConnectionException {

        if (!doesDbConnectionExists(dbConnectionId)) {
            throw new NoSuchDbConnectionException(dbConnectionId);
        }

        return dbConnections.get(dbConnectionId).getReadAccess();

    }

    public static DbConnection getDbConnection( String dbConnectionId ) throws NoSuchDbConnectionException {

        if (!doesDbConnectionExists(dbConnectionId)) {
            throw new NoSuchDbConnectionException(dbConnectionId);
        }

        return dbConnections.get(dbConnectionId).getDbConnection();
    }

    public static String
            createDbConnection( com.axway.ats.httpdblogger.reporter2.connections.pojo.request.CreateDbConnectionPojo dbConnectionPojo ) throws PojoValidationException {

        dbConnectionPojo.validate();

        DbConnectionData connectionData = new DbConnectionData(dbConnectionPojo.getDbHost(),
                                                               dbConnectionPojo.getDbPort(),
                                                               dbConnectionPojo.getDbName(),
                                                               dbConnectionPojo.getDbUser(),
                                                               dbConnectionPojo.getDbPassword());

        String uuid = UUID.randomUUID().toString().trim();
        while (dbConnections.containsKey(uuid)) {
            uuid = UUID.randomUUID().toString().trim();
        }
        dbConnections.put(uuid, connectionData);

        return uuid;
    }

    public static void removeDbConnection( String dbConnectionId ) throws NoSuchDbConnectionException {

        if (!doesDbConnectionExists(dbConnectionId)) {
            throw new NoSuchDbConnectionException(dbConnectionId);
        }

        dbConnections.remove(dbConnectionId);

    }

    static class DbConnectionData {

        private String        dbHost;
        private int           dbPort;
        private String        dbName;
        private String        dbUser;
        private String        dbPassword;
        private DbConnection  dbConnection;
        private IDbReadAccess readAccess;

        DbConnectionData( String dbHost,
                          int dbPort,
                          String dbName,
                          String dbUser,
                          String dbPassword ) {

            this.dbHost = dbHost;
            this.dbPort = dbPort;
            this.dbName = dbName;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
            this.dbConnection = createConnection();
            this.readAccess = createDbReadAccess();

        }

        private IDbReadAccess createDbReadAccess() {

            if (this.dbConnection == null) {
                this.dbConnection = createConnection();
            }
            if (this.dbConnection instanceof DbConnSQLServer) {
                return new SQLServerDbReadAccess(dbConnection);
            } else if (this.dbConnection instanceof DbConnPostgreSQL) {
                return new PGDbReadAccess(dbConnection);
            } else {
                throw new UnsupportedOperationException("DB server " + this.dbConnection.getDbType()
                                                        + " is not supported one for ATS Log DB");
            }
        }

        private DbConnection createConnection() {

            DbConnection dbConnection = null;
            Exception mssqlException = null;
            Exception postgreException = null;

            mssqlException = DbUtils.isMSSQLDatabaseAvailable(dbHost, dbPort, dbName, dbUser, dbPassword);
            if (mssqlException == null) {
                dbConnection = new DbConnSQLServer(dbHost, dbPort, dbName, dbUser, dbPassword, null);
            } else {
                postgreException = DbUtils.isPostgreSQLDatabaseAvailable(dbHost, dbPort, dbName, dbUser, dbPassword);
                if (postgreException == null) {
                    dbConnection = new DbConnPostgreSQL(dbHost, dbPort, dbName, dbUser, dbPassword, null);
                } else {
                    String errMsg = "Neither MSSQL, nor PostgreSQL server at '" + dbHost + ":"
                                    + dbPort +
                                    "' has database with name '" + dbName
                                    + "'. Exception for MSSQL is : \n\t" + mssqlException
                                    + "\n\nException for PostgreSQL is: \n\t"
                                    + postgreException;
                    throw new RuntimeException(errMsg);
                }

            }
            return dbConnection;
        }

        public DbConnection getDbConnection() {

            return dbConnection;
        }

        public IDbReadAccess getReadAccess() {

            if (this.readAccess == null) {
                this.readAccess = createDbReadAccess();
            }

            return this.readAccess;

        }

        public boolean equals( DbConnectionData connectionData ) {

            return this.toString()
                       .equals(connectionData.toString());
        }

        public String toString() {

            return this.dbHost + ":" + this.dbPort + "/" + this.dbName + "?user=" + this.dbUser + "&password"
                   + this.dbPassword;
        }

        public int hashCode() {

            return toString().hashCode();
        }
    }

}
