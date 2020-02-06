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

@ApiModel( "Get testcase single statistic description response details")
public class StatisticDescriptionPojo {

    @ApiModelProperty( required = true, value = "machineId", example = "7709", position = 1)
    public int      machineId;

    @ApiModelProperty( required = true, value = "machineName", example = "loader_8080", position = 2)
    public String   machineName;

    @ApiModelProperty( required = true, value = "queueName", example = "7709", position = 3)
    public String   queueName;

    @ApiModelProperty( required = true, value = "statisticTypeId", example = "7709", position = 4)
    public int      statisticTypeId;

    @ApiModelProperty( required = true, value = "statisticName", example = "JVM Memory", position = 5)
    public String   statisticName;

    @ApiModelProperty( required = true, value = "unit", example = "mb/s", position = 6)
    public String   unit;
    @ApiModelProperty( required = true, value = "params", example = "", position = 7)
    public String   params;

    @ApiModelProperty( required = true, value = "parent", example = "N/A", position = 8)
    public String   parent;

    @ApiModelProperty( required = true, value = "internalName", example = "[process] <some process name>", position = 9)
    public String   internalName;

    @ApiModelProperty( required = true, value = "numberMeasurements", example = "1022", position = 10)
    public int      numberMeasurements;

    @ApiModelProperty( required = true, value = "minValue", example = "100", position = 11)
    public float    minValue;

    @ApiModelProperty( required = true, value = "maxValue", example = "500", position = 12)
    public float    maxValue;

    @ApiModelProperty( required = true, value = "avgValue", example = "300.6", position = 13)
    public float    avgValue;

    @ApiModelProperty( required = true, value = "statistics", example = "N/A", position = 14)
    StatisticPojo[] statistics;

    public StatisticDescriptionPojo() {}

    public int getMachineId() {

        return machineId;
    }

    public void setMachineId( int machineId ) {

        this.machineId = machineId;
    }

    public String getMachineName() {

        return machineName;
    }

    public void setMachineName( String machineName ) {

        this.machineName = machineName;
    }

    public String getQueueName() {

        return queueName;
    }

    public void setQueueName( String queueName ) {

        this.queueName = queueName;
    }

    public int getStatisticTypeId() {

        return statisticTypeId;
    }

    public void setStatisticTypeId( int statisticTypeId ) {

        this.statisticTypeId = statisticTypeId;
    }

    public String getStatisticName() {

        return statisticName;
    }

    public void setStatisticName( String statisticName ) {

        this.statisticName = statisticName;
    }

    public String getUnit() {

        return unit;
    }

    public void setUnit( String unit ) {

        this.unit = unit;
    }

    public String getParams() {

        return params;
    }

    public void setParams( String params ) {

        this.params = params;
    }

    public String getParent() {

        return parent;
    }

    public void setParent( String parent ) {

        this.parent = parent;
    }

    public int getNumberMeasurements() {

        return numberMeasurements;
    }

    public void setNumberMeasurements( int numberMeasurements ) {

        this.numberMeasurements = numberMeasurements;
    }

    public float getMinValue() {

        return minValue;
    }

    public void setMinValue( float minValue ) {

        this.minValue = minValue;
    }

    public float getMaxValue() {

        return maxValue;
    }

    public void setMaxValue( float maxValue ) {

        this.maxValue = maxValue;
    }

    public float getAvgValue() {

        return avgValue;
    }

    public void setAvgValue( float avgValue ) {

        this.avgValue = avgValue;
    }

    public String getInternalName() {

        return internalName;
    }

    public void setInternalName( String internalName ) {

        this.internalName = internalName;
    }

    public StatisticPojo[] getStatistics() {

        return statistics;
    }

    public void setStatistics( StatisticPojo[] statistics ) {

        this.statistics = statistics;
    }

}
