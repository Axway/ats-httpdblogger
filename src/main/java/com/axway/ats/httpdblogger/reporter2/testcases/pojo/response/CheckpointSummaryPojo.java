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

@ApiModel( "Get testcase load queue single checkpoint summary details")
public class CheckpointSummaryPojo {

    @ApiModelProperty( required = true, value = "id", example = "7709", position = 1)
    public int    id;

    @ApiModelProperty( required = true, value = "loadQueueId", example = "125", position = 2)
    public int    loadQueueId;

    @ApiModelProperty(
            required = true,
            value = "name",
            example = "some action name",
            notes = " The action name. In Java this is the name of the Java method, annotated with @Action",
            position = 3)
    public String name;

    @ApiModelProperty(
            required = true,
            value = "numRunning",
            example = "100",
            notes = " Show the number of currently running actions with that name",
            position = 4)
    public int    numRunning;

    @ApiModelProperty( required = true, value = "numPassed", example = "10", position = 5)
    public int    numPassed;

    @ApiModelProperty( required = true, value = "numFailed", example = "5", position = 6)
    public int    numFailed;

    @ApiModelProperty( required = true, value = "numTotal", example = "15", position = 7)
    public int    numTotal;

    @ApiModelProperty( required = true, value = "minResponseTime", example = "1000", position = 8)
    public int    minResponseTime;

    @ApiModelProperty( required = true, value = "avgResponseTime", example = "1005", position = 9)
    public float  avgResponseTime;

    @ApiModelProperty( required = true, value = "maxResponseTime", example = "1010", position = 10)
    public int  maxResponseTime;

    @ApiModelProperty( required = true, value = "minTransferRate", example = "1010.54", position = 11)
    public float  minTransferRate;

    @ApiModelProperty( required = true, value = "avgTransferRate", example = "1010.55", position = 12)
    public float  avgTransferRate;

    @ApiModelProperty( required = true, value = "maxTransferRate", example = "1010.80", position = 13)
    public float  maxTransferRate;

    @ApiModelProperty( required = true, value = "transferRateUnit", example = "something per second", position = 14)
    public String transferRateUnit;

    public CheckpointSummaryPojo() {

    }

    public int getId() {

        return id;
    }

    public void setId( int id ) {

        this.id = id;
    }

    public int getLoadQueueId() {

        return loadQueueId;
    }

    public void setLoadQueueId( int loadQueueId ) {

        this.loadQueueId = loadQueueId;
    }

    public String getName() {

        return name;
    }

    public void setName( String name ) {

        this.name = name;
    }

    public int getNumRunning() {

        return numRunning;
    }

    public void setNumRunning( int numRunning ) {

        this.numRunning = numRunning;
    }

    public int getNumPassed() {

        return numPassed;
    }

    public void setNumPassed( int numPassed ) {

        this.numPassed = numPassed;
    }

    public int getNumFailed() {

        return numFailed;
    }

    public void setNumFailed( int numFailed ) {

        this.numFailed = numFailed;
    }

    public int getNumTotal() {

        return numTotal;
    }

    public void setNumTotal( int numTotal ) {

        this.numTotal = numTotal;
    }

    public int getMinResponseTime() {

        return minResponseTime;
    }

    public void setMinResponseTime( int minResponseTime ) {

        this.minResponseTime = minResponseTime;
    }

    public float getAvgResponseTime() {

        return avgResponseTime;
    }

    public void setAvgResponseTime( float avgResponseTime ) {

        this.avgResponseTime = avgResponseTime;
    }

    public int getMaxResponseTime() {

        return maxResponseTime;
    }

    public void setMaxResponseTime( int maxResponseTime ) {

        this.maxResponseTime = maxResponseTime;
    }

    public float getMinTransferRate() {

        return minTransferRate;
    }

    public void setMinTransferRate( float minTransferRate ) {

        this.minTransferRate = minTransferRate;
    }

    public float getAvgTransferRate() {

        return avgTransferRate;
    }

    public void setAvgTransferRate( float avgTransferRate ) {

        this.avgTransferRate = avgTransferRate;
    }

    public float getMaxTransferRate() {

        return maxTransferRate;
    }

    public void setMaxTransferRate( float maxTransferRate ) {

        this.maxTransferRate = maxTransferRate;
    }

    public String getTransferRateUnit() {

        return transferRateUnit;
    }

    public void setTransferRateUnit( String transferRateUnit ) {

        this.transferRateUnit = transferRateUnit;
    }

}
