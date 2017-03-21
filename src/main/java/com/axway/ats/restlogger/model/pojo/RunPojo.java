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
package com.axway.ats.restlogger.model.pojo;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "run and DB details")
public class RunPojo extends BasePojo {

    @ApiModelProperty(hidden = true)
    private int      runId;

    @ApiModelProperty(required = true, value = "Run name", example = "Acceptance Tests")
    protected String runName;

    @ApiModelProperty(required = true, value = "OS name", example = "linux")
    protected String osName;

    @ApiModelProperty(required = true, value = "Product name", example = "HTTP Client")
    protected String productName;

    @ApiModelProperty(required = true, value = "Version name", example = "1.0.0")
    protected String versionName;

    @ApiModelProperty(required = true, value = "Build name", example = "1234")
    protected String buildName;
    
    @ApiModelProperty(required = true, value = "Host name", example = "localhost")
    protected String hostName;

    @ApiModelProperty(required = true, value = "DB host", example = "localhost")
    private String   dbHost;

    @ApiModelProperty(required = true, value = "DB name", example = "HTTP_TESTS")
    private String   dbName;

    @ApiModelProperty(required = true, value = "DB user", example = "admin")
    private String   dbUser;

    @ApiModelProperty(required = true, value = "DB password", example = "password")
    private String   dbPassword;
    
    @ApiModelProperty(required = false, value = "User note", example = "Acceptance run for HTTP")
    private String    userNote;

    @ApiModelProperty(hidden = true)
    private SuitePojo    suite;

    /*
     * Overriding the session id getter, in order to hide that field from the documentation generation,
     * because it is not needed for Run starting
     */
    @ApiModelProperty(hidden = true)
    public String getSessionId() {

        return sessionId;
    }

    public int getRunId() {

        return runId;
    }

    public void setRunId(
                          int runId ) {

        this.runId = runId;
    }

    public String getRunName() {

        return runName;
    }

    public void setRunName(
                            String runName ) {

        this.runName = runName;
    }

    public String getOsName() {

        return osName;
    }

    public void setOsName(
                           String osName ) {

        this.osName = osName;
    }

    public String getProductName() {

        return productName;
    }

    public void setProductName(
                                String productName ) {

        this.productName = productName;
    }

    public String getVersionName() {

        return versionName;
    }

    public void setVersionName(
                                String versionName ) {

        this.versionName = versionName;
    }

    public String getBuildName() {

        return buildName;
    }

    public void setBuildName(
                              String buildName ) {

        this.buildName = buildName;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String hostName){
        this.hostName = hostName;
    }

    public SuitePojo getSuite() {

        return suite;
    }

    public void setSuite(
                          SuitePojo suite ) {

        this.suite = suite;
    }

    public String getDbHost() {

        return dbHost;
    }

    public void setDbHost(
                           String dbHost ) {

        this.dbHost = dbHost;
    }

    public String getDbName() {

        return dbName;
    }

    public void setDbName(
                           String dbName ) {

        this.dbName = dbName;
    }

    public String getDbUser() {

        return dbUser;
    }

    public void setDbUser(
                           String dbUser ) {

        this.dbUser = dbUser;
    }

    public String getDbPassword() {

        return dbPassword;
    }

    public void setDbPassword(
                               String dbPassword ) {

        this.dbPassword = dbPassword;
    }

    public String getUserNote() {
    
        return userNote;
    }

    public void setUserNote( String userNote ) {
    
        this.userNote = userNote;
    }
    
    
}
