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

@ApiModel(value = "update run details")
public class UpdateRunPojo extends BasePojo {

    @ApiModelProperty(required = false, value = "Run name", example = "Acceptance Tests")
    protected String runName;

    @ApiModelProperty(required = false, value = "OS name", example = "linux")
    protected String osName;

    @ApiModelProperty(required = false, value = "Product name", example = "HTTP Client")
    protected String productName;

    @ApiModelProperty(required = false, value = "Version name", example = "1.0.0")
    protected String versionName;

    @ApiModelProperty(required = false, value = "Build name", example = "1234")
    protected String buildName;

    @ApiModelProperty(required = false, value = "Host name", example = "localhost")
    protected String hostName;

    @ApiModelProperty(required = false, value = "User note", example = "Acceptance run for HTTP")
    private String   userNote;

    public String getRunName() {

        return runName;
    }

    public void setRunName(
                            String runName ) {

        this.runName = runName;
    }

    public String getOsName() {

        return osName;
    }

    public void setOsName(
                           String osName ) {

        this.osName = osName;
    }

    public String getProductName() {

        return productName;
    }

    public void setProductName(
                                String productName ) {

        this.productName = productName;
    }

    public String getVersionName() {

        return versionName;
    }

    public void setVersionName(
                                String versionName ) {

        this.versionName = versionName;
    }

    public String getBuildName() {

        return buildName;
    }

    public void setBuildName(
                              String buildName ) {

        this.buildName = buildName;
    }

    public String getHostName() {

        return hostName;
    }

    public void setHostName(
                             String hostName ) {

        this.hostName = hostName;
    }

    public String getUserNote() {

        return userNote;
    }

    public void setUserNote(
                             String userNote ) {

        this.userNote = userNote;
    }

}
