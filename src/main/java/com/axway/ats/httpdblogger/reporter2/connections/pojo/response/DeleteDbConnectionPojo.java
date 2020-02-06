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

@ApiModel( "Delete DB connection response details")
public class DeleteDbConnectionPojo {

    @ApiModelProperty( required = true, value = "result message", example = "Successfully deleted DB connection '<Connection ID>'")
    private String message;

    public DeleteDbConnectionPojo() {}

    public DeleteDbConnectionPojo( String connectionId ) {

        this.message = "Successfully deleted DB connection '" + connectionId + "'";
    }

    public String getMessage() {

        return message;
    }

    public void setMessage( String message ) {

        this.message = message;
    }

}
