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
package com.axway.ats.restlogger.model.pojo;

import com.axway.ats.core.utils.ExceptionUtils;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "response data")
public class ResponsePojo {

    @ApiModelProperty(required = true, value = "Session ID", example = "f177f4f5-a649-4faa-920e-978ef8d60cb2")
    private String sessionId;

    @ApiModelProperty(required = false, value = "Error message", example = "Invalid session id 'sdfsf4f5-n569-2faa-t20e-978ef8d20cb4'")
    private String error;

    public ResponsePojo() {

        // the no-argument constructor is required for the POJO mapping
    }

    public ResponsePojo( String sessionId) {

        this.sessionId = sessionId;
    }

    public ResponsePojo( String message, Exception exception ) {

        if( exception != null ) {
            this.error = ExceptionUtils.getExceptionMsg( exception, message );
        } else {
            this.error = message;
        }
    }

    public ResponsePojo( Exception exception ) {

        this.error = ExceptionUtils.getExceptionMsg( exception );
    }

    public String getSessionId() {

        return sessionId;
    }

    public void setSessionId( String sessionId ) {

        this.sessionId = sessionId;
    }

    public String getError() {

        return error;
    }

    public void setError( String error ) {

        this.error = error;
    }

}
