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
package com.axway.ats.httpdblogger.reporter2.scenarios.pojo.response;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( "Get scenario response details")
public class ScenarioPojo {

    @ApiModelProperty( required = true, value = "id", example = "7709", position = 1)
    private int    id;

    @ApiModelProperty( required = true, value = "suiteId", example = "7709", position = 2)
    private int    suiteId;

    @ApiModelProperty(
            required = true,
            notes = "The name is the same as the name of your JAVA method",
            value = "name",
            example = "test_sftp",
            position = 3)
    String         name;

    @ApiModelProperty(
            required = true,
            value = "description",
            example = "SFTP file transfer is a flacky one",
            position = 4)
    String         description;

    @ApiModelProperty(
            required = true,
            allowableValues = "PASSED, FAILED, SKIPPED, RUNNING",
            value = "state",
            example = "PASSED",
            position = 5)
    String         state;

    @ApiModelProperty( required = true, value = "testcasesPassed", example = "90", position = 6)
    int            testcasesTotal;

    @ApiModelProperty( required = true, value = "testcasesPassed", example = "79", position = 7)
    int            testcasesPassed;

    @ApiModelProperty( required = true, value = "testcasesFailed", example = "10", position = 8)
    int            testcasesFailed;

    @ApiModelProperty( required = true, value = "testcasesSkipped", example = "1", position = 9)
    int            testcasesSkipped;

    @ApiModelProperty( required = true, value = "testcasesPassedPercent", example = "81%", position = 10)
    private String testcasesPassedPercent;

    @ApiModelProperty(
            required = true,
            value = "startTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "1553112000000",
            position = 11)
    long           startTimestamp;

    @ApiModelProperty(
            required = true,
            value = "endTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "1553112000000",
            position = 12)
    long           endTimestamp;

    @ApiModelProperty(
            required = true,
            value = "duration",
            notes = "The format is HH:MM:SS. If you need raw milliseconds, you cat use endTimestamp - startTimestamp",
            example = "32510491200000",
            position = 13)
    String         duration;

    @ApiModelProperty(
            required = true,
            value = "userNote",
            example = "The tests are using my local changes",
            position = 14)
    String         userNote;

    public ScenarioPojo() {}

    public int getId() {

        return id;
    }

    public void setId( int id ) {

        this.id = id;
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

    public String getDescription() {

        return description;
    }

    public void setDescription( String description ) {

        this.description = description;
    }

    public String getState() {

        return state;
    }

    public void setState( String state ) {

        this.state = state;
    }

    public int getTestcasesTotal() {

        return testcasesTotal;
    }

    public void setTestcasesTotal( int testcasesTotal ) {

        this.testcasesTotal = testcasesTotal;
    }

    public int getTestcasesPassed() {

        return testcasesPassed;
    }

    public void setTestcasesPassed( int testcasesPassed ) {

        this.testcasesPassed = testcasesPassed;
    }

    public int getTestcasesFailed() {

        return testcasesFailed;
    }

    public void setTestcasesFailed( int testcasesFailed ) {

        this.testcasesFailed = testcasesFailed;
    }

    public int getTestcasesSkipped() {

        return testcasesSkipped;
    }

    public void setTestcasesSkipped( int testcasesSkipped ) {

        this.testcasesSkipped = testcasesSkipped;
    }

    public String getTestcasesPassedPercent() {

        return testcasesPassedPercent;
    }

    public void setTestcasesPassedPercent( String testcasesPassedPercent ) {

        this.testcasesPassedPercent = testcasesPassedPercent;
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
