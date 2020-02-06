/*
 * Copyright 2017 Axway Software
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
package com.axway.ats.httpdblogger.reporter2.connections.pojo.request;

import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.httpdblogger.reporter2.exceptions.PojoValidationException;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( "Create DB connection request details")
public class CreateDbConnectionPojo {

    @ApiModelProperty( required = true, value = "database host", example = "hostname or IP")
    private String dbHost;

    @ApiModelProperty( required = true, value = "database port", example = "1433 (default for SQL Server), 5432 (default for PostgreSQL)")
    private int    dbPort;

    @ApiModelProperty( required = true, value = "database name", example = "AtsTestDb")
    private String dbName;

    @ApiModelProperty( required = true, value = "database user", example = "AtsUser")
    private String dbUser;

    @ApiModelProperty( required = true, value = "database password", example = "AtsPassword")
    private String dbPassword;

    public CreateDbConnectionPojo() {}

    public CreateDbConnectionPojo( String dbHost, int dbPort, String dbName, String dbUser, String dbPassword ) {

        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public String getDbHost() {

        return dbHost;
    }

    public void setDbHost( String dbHost ) {

        this.dbHost = dbHost;
    }

    public int getDbPort() {

        return dbPort;
    }

    public void setDbPort( int dbPort ) {

        this.dbPort = dbPort;
    }

    public String getDbName() {

        return dbName;
    }

    public void setDbName( String dbName ) {

        this.dbName = dbName;
    }

    public String getDbUser() {

        return dbUser;
    }

    public void setDbUser( String dbUser ) {

        this.dbUser = dbUser;
    }

    public String getDbPassword() {

        return dbPassword;
    }

    public void setDbPassword( String dbPassword ) {

        this.dbPassword = dbPassword;
    }

    public void validate() throws PojoValidationException {

        if (StringUtils.isNullOrEmpty(dbHost)) {
            throw new PojoValidationException("dbHost not provided");
        }

        if (this.dbPort <= 0) {
            throw new PojoValidationException("dbPort must be > 0");
        }

        if (StringUtils.isNullOrEmpty(dbName)) {
            throw new PojoValidationException("dbName not provided");
        }

        if (StringUtils.isNullOrEmpty(dbUser)) {
            throw new PojoValidationException("dbUser not provided");
        }

        if (StringUtils.isNullOrEmpty(dbPassword)) {
            throw new PojoValidationException("dbPassword not provided");
        }

    }

}
