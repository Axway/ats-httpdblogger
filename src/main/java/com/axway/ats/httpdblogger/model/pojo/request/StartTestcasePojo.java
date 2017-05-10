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

@ApiModel(value = "start testcase details")
public class StartTestcasePojo extends BasePojo {

    @ApiModelProperty(hidden = true)
    private int    testcaseId;

    @ApiModelProperty(required = true, value = "Testcase name", example = "testGetHomePathUnix")
    private String testcaseName;

    @ApiModelProperty(required = true, value = "Scenario name", example = "testGetHomePath")
    private String scenarioName;

    @ApiModelProperty(required = true, value = "Scenario description", example = "testGetHomePath")
    private String scenarioDescription;

    @ApiModelProperty(required = false, value = "Suite ID", example = "939")
    private int    suiteId = -1;

    public int getTestcaseId() {

        return testcaseId;
    }

    public void setTestcaseId(
                               int testcaseId ) {

        this.testcaseId = testcaseId;
    }

    public String getTestcaseName() {

        return testcaseName;
    }

    public void setTestcaseName(
                                 String testcaseName ) {

        this.testcaseName = testcaseName;
    }

    public String getScenarioName() {

        return scenarioName;
    }

    public void setScenarioName(
                                 String scenarioName ) {

        this.scenarioName = scenarioName;
    }

    public String getScenarioDescription() {

        return scenarioDescription;
    }

    public void setScenarioDescription(
                                        String scenarioDescription ) {

        this.scenarioDescription = scenarioDescription;
    }

    public int getSuiteId() {

        return suiteId;
    }

    public void setSuiteId(
                            int suiteId ) {

        this.suiteId = suiteId;
    }

}
