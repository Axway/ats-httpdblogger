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

import com.axway.ats.httpdblogger.model.TestResult;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "testcase result")
public class TestcaseResultPojo extends BasePojo {

    @ApiModelProperty(required = true, value = "Testcase result", example = "FAILED|PASSED|SKIPPED|RUNNING")
    private String     result;

    @ApiModelProperty(hidden = true)
    private TestResult testResult = TestResult.RUNNING;

    public String getResult() {

        return result;
    }

    public void setResult(
                           String result ) {

        this.result = result;
    }

    public TestResult getTestResult() {

        return testResult;
    }

    public void setTestResult(
                               TestResult testResult ) {

        this.testResult = testResult;
    }
}
