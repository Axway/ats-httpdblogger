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

@ApiModel( "Get single testcases system/user statistics details")
public class StatisticPojo {

    @ApiModelProperty(
            required = true,
            value = "value",
            example = "CPU average or for checkpoints the name and arguments of a Java @Action-annotated method",
            position = 1)
    public float value;

    @ApiModelProperty( required = true, value = "transferSize", example = "1024.45", position = 2)
    public float transferSize;

    @ApiModelProperty( required = true, value = "timestamp", example = "1553112000000", position = 3)
    private long timestamp;

    public StatisticPojo() {}

    public float getValue() {

        return value;
    }

    public void setValue( float value ) {

        this.value = value;
    }

    public float getTransferSize() {

        return transferSize;
    }

    public void setTransferSize( float transferSize ) {

        this.transferSize = transferSize;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp( long timestamp ) {

        this.timestamp = timestamp;
    }

}
