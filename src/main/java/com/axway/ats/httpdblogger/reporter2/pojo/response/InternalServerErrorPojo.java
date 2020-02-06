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
package com.axway.ats.httpdblogger.reporter2.pojo.response;

import com.axway.ats.core.utils.StringUtils;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( "Internal server error response details")
public class InternalServerErrorPojo {

    @ApiModelProperty(
            required = true,
            value = "exceptionMessage",
            example = "Could not <Some action>. Caused by: <Cause 1>. Caused by: <Cause 2>, etc")
    private String exceptionMessage;

    public InternalServerErrorPojo() {

        this(null, null);
    }

    public InternalServerErrorPojo( String message ) {

        this(message, null);

    }

    public InternalServerErrorPojo( String message, Throwable throwable ) {

        if (!StringUtils.isNullOrEmpty(message)) {
            this.exceptionMessage = message;
        }

        if (throwable != null) {
            this.exceptionMessage += "." + getCauses(throwable);
        }

    }

    public String getExceptionMessage() {

        return exceptionMessage;
    }

    public void setExceptionMessage( String exceptionMessage ) {

        this.exceptionMessage = exceptionMessage;
    }

    private String getCauses( Throwable throwable ) {

        StringBuilder sb = new StringBuilder();
        sb.append("Caused by: " + throwable.getMessage() + "\n");
        Throwable t = throwable.getCause();
        while (t != null) {
            sb.append("Caused by: " + t.getMessage() + "\n");
            t = t.getCause();
        }
        return sb.toString();
    }

}
