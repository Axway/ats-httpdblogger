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

@ApiModel(value = "session data")
public class BasePojo {

    @ApiModelProperty(required = true, value = "Session ID", example = "f177f4f5-a649-4faa-920e-978ef8d60cb2")
    protected String sessionId;
    @ApiModelProperty(required = false, value = "timestamp", example = "1493709734")
    protected long   timestamp = -1;

    public String getSessionId() {

        return sessionId;
    }

    public void setSessionId(
                              String sessionId ) {

        this.sessionId = sessionId;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(
                              long timestamp ) {

        this.timestamp = timestamp;
    }

}
