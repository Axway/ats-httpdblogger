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

@ApiModel( "Get testcase load queue single checkpoint details")
public class CheckpointPojo {

    @ApiModelProperty( required = true, value = "id", example = "7709", position = 1)
    public int    id;

    @ApiModelProperty( required = true, value = "checkpointSummaryId", example = "7709", position = 2)
    public int    checkpointSummaryId;

    @ApiModelProperty(
            required = true,
            value = "name",
            example = "some action name",
            notes = " The action name. In Java this is the name of the Java method, annotated with @Action",
            position = 3)
    public String name;

    @ApiModelProperty( required = true, value = "responseTime", example = "1005", position = 4)
    public int    responseTime;

    @ApiModelProperty( required = true, value = "transferRate", example = "1002", position = 5)
    public float  transferRate;

    @ApiModelProperty( required = true, value = "transferRateUnit", example = "something per second", position = 6)
    public String transferRateUnit;

    @ApiModelProperty(
            required = true,
            allowableValues = "FAILED(0), PASSED(1), RUNNING(4)",
            value = "result",
            example = "1",
            position = 7)
    public int    result;

    @ApiModelProperty(
            required = true,
            value = "endTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "1553112000000",
            position = 8)
    public long   endTimestamp;

    public CheckpointPojo() {}

    public int getId() {

        return id;
    }

    public void setId( int checkpointId ) {

        this.id = checkpointId;
    }

    public int getCheckpointSummaryId() {

        return checkpointSummaryId;
    }

    public void setCheckpointSummaryId( int checkpointSummaryId ) {

        this.checkpointSummaryId = checkpointSummaryId;
    }

    public String getName() {

        return name;
    }

    public void setName( String name ) {

        this.name = name;
    }

    public int getResponseTime() {

        return responseTime;
    }

    public void setResponseTime( int responseTime ) {

        this.responseTime = responseTime;
    }

    public float getTransferRate() {

        return transferRate;
    }

    public void setTransferRate( float transferRate ) {

        this.transferRate = transferRate;
    }

    public String getTransferRateUnit() {

        return transferRateUnit;
    }

    public void setTransferRateUnit( String transferRateUnit ) {

        this.transferRateUnit = transferRateUnit;
    }

    public int getResult() {

        return result;
    }

    public void setResult( int result ) {

        this.result = result;
    }

    public long getEndTimestamp() {

        return endTimestamp;
    }

    public void setEndTimestamp( long endTimestamp ) {

        this.endTimestamp = endTimestamp;
    }

}
