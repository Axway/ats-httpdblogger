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

@ApiModel( "Get testcase load queue details")
public class LoadQueuePojo {

    @ApiModelProperty( required = true, value = "id", example = "7709", position = 1)
    public int    id;
    
    @ApiModelProperty( required = true, value = "testcaseId", example = "125", position = 2)
    public int    testcaseId;

    @ApiModelProperty( required = true, value = "name", example = "SFTP Transfers", position = 3)
    public String name;

    @ApiModelProperty( required = true, value = "sequence", example = "2", position = 4)
    public int    sequence;

    @ApiModelProperty( required = true, value = "hostsList", example = "N/A", position = 5)
    public String hostsList;

    @ApiModelProperty( required = true, value = "threadingPattern", example = "N/A", position = 6)
    public String threadingPattern;

    @ApiModelProperty( required = true, value = "numberThreads", example = "20", position = 7)
    public int    numberThreads;

    @ApiModelProperty( required = true, value = "result", example = "N/A", position = 8)
    public int    result;

    @ApiModelProperty(
            required = true,
            value = "state",
            allowableValues = "PASSED,FAILED,SKIPPED,RUNNING",
            example = "PASSED",
            position = 9)
    public String state;

    @ApiModelProperty(
            required = true,
            value = "startTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "1553112000000",
            position = 10)
    long          startTimestamp;

    @ApiModelProperty(
            required = true,
            value = "endTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "1553112000000",
            position = 11)
    long          endTimestamp;

    @ApiModelProperty(
            required = true,
            value = "duration",
            notes = "The format is HH:MM:SS. If you need raw milliseconds, you cat use endTimestamp - startTimestamp",
            example = "32510491200000",
            position = 12)
    String        duration;

    public LoadQueuePojo() {}

    public int getId() {

        return id;
    }

    public void setId( int id ) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName( String name ) {

        this.name = name;
    }

    public int getSequence() {

        return sequence;
    }

    public void setSequence( int sequence ) {

        this.sequence = sequence;
    }

    public String getHostsList() {

        return hostsList;
    }

    public void setHostsList( String hostsList ) {

        this.hostsList = hostsList;
    }

    public String getThreadingPattern() {

        return threadingPattern;
    }

    public void setThreadingPattern( String threadingPattern ) {

        this.threadingPattern = threadingPattern;
    }

    public int getNumberThreads() {

        return numberThreads;
    }

    public void setNumberThreads( int numberThreads ) {

        this.numberThreads = numberThreads;
    }

    public int getResult() {

        return result;
    }

    public void setResult( int result ) {

        this.result = result;
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

}
