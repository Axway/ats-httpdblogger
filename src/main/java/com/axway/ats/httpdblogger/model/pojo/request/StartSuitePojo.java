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

@ApiModel(value = "suite details")
public class StartSuitePojo extends BasePojo {

    @ApiModelProperty(hidden = true)
    private int               suiteId;

    @ApiModelProperty(required = true, value = "Suite name", example = "Test_Http")
    private String            suiteName;

    @ApiModelProperty(required = true, value = "Suite package name", example = "com.foo.bar.http")
    private String            packageName;

    @ApiModelProperty(hidden = true)
    private StartTestcasePojo testcase;

    @ApiModelProperty(required = false, value = "Run ID", example = "8920")
    private int               runId = -1;

    public int getSuiteId() {

        return suiteId;
    }

    public void setSuiteId(
                            int suiteId ) {

        this.suiteId = suiteId;
    }

    public String getSuiteName() {

        return suiteName;
    }

    public void setSuiteName(
                              String suiteName ) {

        this.suiteName = suiteName;
    }

    public String getPackageName() {

        return packageName;
    }

    public void setPackageName(
                                String packageName ) {

        this.packageName = packageName;
    }

    public StartTestcasePojo getTestcase() {

        return testcase;
    }

    public void setTestcase(
                             StartTestcasePojo testcase ) {

        this.testcase = testcase;
    }

    public int getRunId() {

        return runId;
    }

    public void setRunId(
                          int runId ) {

        this.runId = runId;
    }

}
