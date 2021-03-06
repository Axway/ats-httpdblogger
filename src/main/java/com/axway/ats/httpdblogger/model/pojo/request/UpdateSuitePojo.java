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
package com.axway.ats.httpdblogger.model.pojo.request;

import com.axway.ats.httpdblogger.model.pojo.BasePojo;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( value = "update suite details")
public class UpdateSuitePojo extends BasePojo {

    @ApiModelProperty( required = false, value = "Suite ID", example = "1321")
    protected int    suiteId;

    @ApiModelProperty( required = false, value = "Suite name", example = "Tests_OracleConnection")
    protected String suiteName;

    @ApiModelProperty( required = false, value = "User note", example = "Acceptanse test for Oracle DB connection")
    private String   userNote;

    public int getSuiteId() {

        return suiteId;
    }

    public void setSuiteId( int suiteId ) {

        this.suiteId = suiteId;
    }

    public String getSuiteName() {

        return suiteName;
    }

    public void setSuiteName( String suiteName ) {

        this.suiteName = suiteName;
    }

    public String getUserNote() {

        return userNote;
    }

    public void setUserNote( String userNote ) {

        this.userNote = userNote;
    }

}
