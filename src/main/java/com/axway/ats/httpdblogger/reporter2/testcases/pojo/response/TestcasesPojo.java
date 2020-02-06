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

@ApiModel( "Get all testcases response details")
public class TestcasesPojo {

    @ApiModelProperty(
            required = true,
            value = "testcases",
            notes = "A list of TESTCASEs. To see the individual testcase properties, check the 'Get testcase response details' document section",
            example = "{ testcases: [ <TESTCASE_1>, <TESTCASE_2>, ... ] }")
    private TestcasePojo[] testcases;

    public TestcasesPojo() {

    }

    public TestcasePojo[] getTestcases() {

        return testcases;
    }

    public void setTestcases( TestcasePojo[] testcases ) {

        this.testcases = testcases;
    }

}
