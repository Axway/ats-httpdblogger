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
package com.axway.ats.httpdblogger.reporter2.testcases.pojo.response;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( "Get testcase response details")
public class TestcasePojo {

    @ApiModelProperty( required = true, value = "id", example = "7709", position = 1)
    private int id;

    @ApiModelProperty( required = true, value = "scenarioId", example = "321", position = 2)
    private int scenarioId;

    @ApiModelProperty( required = true, value = "suiteId", example = "126", position = 3)
    private int suiteId;

    @ApiModelProperty(
            required = true,
            notes = "The name is the same as the name of your JAVA method plus all of the java arguments' values",
            value = "name",
            example = "test_sftp( AtsUser, AtsPassword, localhost, 11122 )",
            position = 4)
    String      name;

    @ApiModelProperty(
            required = true,
            allowableValues = "PASSED, FAILED, SKIPPED, RUNNING",
            value = "state",
            example = "PASSED",
            position = 5)
    String      state;

    @ApiModelProperty(
            required = true,
            value = "startTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "1553112000000",
            position = 6)
    long        startTimestamp;

    @ApiModelProperty(
            required = true,
            value = "endTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "1553112000000",
            position = 7)
    long        endTimestamp;

    @ApiModelProperty(
            required = true,
            value = "duration",
            notes = "The format is HH:MM:SS. If you need raw milliseconds, you cat use endTimestamp - startTimestamp",
            example = "32510491200000",
            position = 8)
    String      duration;

    @ApiModelProperty(
            required = true,
            value = "userNote",
            example = "The tests are using my local changes",
            position = 9)
    String      userNote;

    public TestcasePojo() {}

    public int getId() {

        return id;
    }

    public void setId( int id ) {

        this.id = id;
    }

    public int getScenarioId() {

        return scenarioId;
    }

    public void setScenarioId( int scenarioId ) {

        this.scenarioId = scenarioId;
    }

    public int getSuiteId() {

        return suiteId;
    }

    public void setSuiteId( int suiteId ) {

        this.suiteId = suiteId;
    }

    public String getName() {

        return name;
    }

    public void setName( String name ) {

        this.name = name;
    }

    public String getState() {

        return state;
    }

    public void setState( String state ) {

        this.state = state;
    }

    public long getStartTimestamp() {

        return startTimestamp;
    }

    public void setStartTimestamp( long startTimestamp ) {

        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {

        return endTimestamp;
    }

    public void setEndTimestamp( long endTimestamp ) {

        this.endTimestamp = endTimestamp;
    }

    public String getDuration() {

        return duration;
    }

    public void setDuration( String duration ) {

        this.duration = duration;
    }

    public String getUserNote() {

        return userNote;
    }

    public void setUserNote( String userNote ) {

        this.userNote = userNote;
    }

}
