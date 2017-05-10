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

import com.axway.ats.httpdblogger.model.MessageLevel;
import com.axway.ats.httpdblogger.model.pojo.BasePojo;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "message details")
public class InsertMessagePojo extends BasePojo {

    @ApiModelProperty(required = true, value = "Log level", example="TRACE|DEBUG|INFO|WARN|ERROR|FATAL")
    private String level;
    @ApiModelProperty(required = true, value = "Host message is logged from", example="127.0.0.1")
    private String machineName;
    @ApiModelProperty(required = true, value = "Thread name", example="main")
    private String threadName;
    @ApiModelProperty(required = true, value = "Message", example="Beginning http tests")
    private String message;
    @ApiModelProperty(required = false, value = "Run ID", example="323")
    private int    runId      = -1;
    @ApiModelProperty(required = false, value = "Suite ID", example="100")
    private int    suiteId    = -1;
    @ApiModelProperty(required = false, value = "Testcase ID", example="2")
    private int    testcaseId = -1;

    public String getLevel() {

        return level;
    }

    public void setLevel(
                          String level ) {

        this.level = level;
    }

    public String getMachineName() {

        return machineName;
    }

    public void setMachineName(
                                String machineName ) {

        this.machineName = machineName;
    }

    public String getThreadName() {

        return threadName;
    }

    public void setThreadName(
                               String threadName ) {

        this.threadName = threadName;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(
                            String message ) {

        this.message = message;
    }

    public MessageLevel getLogLevel() {

        return MessageLevel.valueOf( level );
    }

    public int getRunId() {

        return runId;
    }

    public void setRunId(
                          int runId ) {

        this.runId = runId;
    }

    public int getSuiteId() {

        return suiteId;
    }

    public void setSuiteId(
                            int suiteId ) {

        this.suiteId = suiteId;
    }

    public int getTestcaseId() {

        return testcaseId;
    }

    public void setTestcaseId(
                               int testcaseId ) {

        this.testcaseId = testcaseId;
    }

}
