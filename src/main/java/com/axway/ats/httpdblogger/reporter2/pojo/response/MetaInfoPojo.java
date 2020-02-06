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

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( "Get metainfo response details")
public class MetaInfoPojo {

    @ApiModelProperty( required = true, value = "id", example = "1023", position = 1)
    private int    id;

    @ApiModelProperty( required = true, value = "parentId", example = "1023", position = 2)
    private int    parentId;

    @ApiModelProperty( required = true, value = "name", example = "java_version", position = 3)
    private String name;

    @ApiModelProperty( required = true, value = "value", example = "1.8", position = 4)
    private String value;

    public MetaInfoPojo() {}

    public int getId() {

        return id;
    }

    public void setId( int id ) {

        this.id = id;
    }

    public int getParentId() {

        return parentId;
    }

    public void setParentId( int parentId ) {

        this.parentId = parentId;
    }

    public String getName() {

        return name;
    }

    public void setName( String name ) {

        this.name = name;
    }

    public String getValue() {

        return value;
    }

    public void setValue( String value ) {

        this.value = value;
    }

}
