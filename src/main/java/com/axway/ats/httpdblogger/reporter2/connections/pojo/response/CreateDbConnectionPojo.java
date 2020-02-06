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
package com.axway.ats.httpdblogger.reporter2.connections.pojo.response;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( "Create DB connection response details")
public class CreateDbConnectionPojo {

    @ApiModelProperty( required = true, value = "connection ID", example = "f177f4f5-a649-4faa-920e-978ef8d60cb2")
    private String connectionId;

    public CreateDbConnectionPojo() {}

    public CreateDbConnectionPojo( String connectionId ) {

        this.connectionId = connectionId;
    }

    public String getConnectionId() {

        return connectionId;
    }

    public void setConnectionId( String connectionId ) {

        this.connectionId = connectionId;
    }

}
