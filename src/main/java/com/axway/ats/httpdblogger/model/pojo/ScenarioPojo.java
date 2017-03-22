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
package com.axway.ats.httpdblogger.model.pojo;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "scenario details")
public class ScenarioPojo extends BasePojo {

    @ApiModelProperty(hidden = true)
    private int      scenarioId;

    @ApiModelProperty(required = true, value = "Scenario name")
    private String   scenarioName;

    @ApiModelProperty(required = true, value = "Scenario description")
    private String   description;

    @ApiModelProperty(hidden = true)
    private TestcasePojo testcase;

    public int getScenarioId() {

        return scenarioId;
    }

    public void setScenarioId(
                               int scenarioId ) {

        this.scenarioId = scenarioId;
    }

    public String getScenarioName() {

        return scenarioName;
    }

    public void setScenarioName(
                                 String scenarioName ) {

        this.scenarioName = scenarioName;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(
                                String description ) {

        this.description = description;
    }

    public TestcasePojo getTestcase() {

        return testcase;
    }

    public void setTestcase(
                             TestcasePojo testcase ) {

        this.testcase = testcase;
    }
}
