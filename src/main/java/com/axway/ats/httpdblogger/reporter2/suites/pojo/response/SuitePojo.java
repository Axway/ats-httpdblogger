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
package com.axway.ats.httpdblogger.reporter2.suites.pojo.response;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( "Get suite response details")
public class SuitePojo {

    @ApiModelProperty( required = true, value = "id", example = "7709", position = 1)
    private int    id;

    @ApiModelProperty( required = true, value = "runId", example = "7709", position = 2)
    private int    runId;

    @ApiModelProperty(
            required = true,
            notes = "The name is the same as the name of your JAVA class",
            value = "name",
            example = "Test_SecureTransfer",
            position = 3)
    private String name;

    @ApiModelProperty( required = true, value = "testcasesTotal", example = "100", position = 4)
    private int    testcasesTotal;

    @ApiModelProperty( required = true, value = "testcasesPassed", example = "90", position = 5)
    private int    testcasesPassed;

    @ApiModelProperty( required = true, value = "testcasesFailed", example = "8", position = 6)
    private int    testcasesFailed;

    @ApiModelProperty( required = true, value = "testcasesSkipped", example = "2", position = 7)
    private int    testcasesSkipped;

    @ApiModelProperty( required = true, value = "scenariosTotal", example = "100", position = 8)
    private int    scenariosTotal;

    @ApiModelProperty( required = true, value = "scenariosPassed", example = "90", position = 9)
    private int    scenariosPassed;

    @ApiModelProperty( required = true, value = "scenariosFailed", example = "8", position = 10)
    private int    scenariosFailed;

    @ApiModelProperty( required = true, value = "scenariosSkipped", example = "2", position = 11)
    private int    scenariosSkipped;

    @ApiModelProperty( required = true, value = "testcasesPassedPercent", example = "81%", position = 12)
    private String testcasesPassedPercent;

    @ApiModelProperty(
            required = true,
            value = "startTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "1553112000000",
            position = 13)
    private long   startTimestamp;

    @ApiModelProperty(
            required = true,
            value = "endTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "32510491200000",
            position = 14)
    private long   endTimestamp;

    @ApiModelProperty(
            required = true,
            value = "duration",
            notes = "The format is HH:MM:SS. If you need raw milliseconds, you cat use endTimestamp - startTimestamp",
            example = "32510491200000",
            position = 15)
    private String duration;

    @ApiModelProperty( required = true, value = "packageName", example = "filetransfer", position = 16)
    private String packageName;

    @ApiModelProperty(
            required = true,
            value = "userNote",
            example = "The tests are using my local changes",
            position = 17)
    private String userNote;

    public SuitePojo() {}

    public int getId() {

        return id;
    }

    public void setId( int id ) {

        this.id = id;
    }

    public int getRunId() {

        return runId;
    }

    public void setRunId( int runId ) {

        this.runId = runId;
    }

    public String getName() {

        return name;
    }

    public void setName( String name ) {

        this.name = name;
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

    public int getScenariosTotal() {

        return scenariosTotal;
    }

    public void setScenariosTotal( int scenariosTotal ) {

        this.scenariosTotal = scenariosTotal;
    }

    public int getScenariosPassed() {

        return scenariosPassed;
    }

    public void setScenariosPassed( int scenariosPassed ) {

        this.scenariosPassed = scenariosPassed;
    }

    public int getScenariosFailed() {

        return scenariosFailed;
    }

    public void setScenariosFailed( int scenariosFailed ) {

        this.scenariosFailed = scenariosFailed;
    }

    public int getScenariosSkipped() {

        return scenariosSkipped;
    }

    public void setScenariosSkipped( int scenariosSkipped ) {

        this.scenariosSkipped = scenariosSkipped;
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

    public String getPackageName() {

        return packageName;
    }

    public void setPackageName( String packageName ) {

        this.packageName = packageName;
    }

    public String getTestcasesPassedPercent() {

        return testcasesPassedPercent;
    }

    public void setTestcasesPassedPercent( String testcasesPassedPercent ) {

        this.testcasesPassedPercent = testcasesPassedPercent;
    }

}
