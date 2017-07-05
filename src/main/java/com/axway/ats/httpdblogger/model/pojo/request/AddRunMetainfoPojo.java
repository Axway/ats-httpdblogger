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

@ApiModel("Run metainfo details")
public class AddRunMetainfoPojo extends BasePojo {
    @ApiModelProperty(required = true, value = "meta key", example="type")
    private String metaKey;
    @ApiModelProperty(required = true, value = "meta value", example="Regression")
    private String metaValue;

    public AddRunMetainfoPojo() {}

    public String getMetaKey() {

        return metaKey;
    }

    public void setMetaKey(
                            String metaKey ) {

        this.metaKey = metaKey;
    }

    public String getMetaValue() {

        return metaValue;
    }

    public void setMetaValue(
                              String metaValue ) {

        this.metaValue = metaValue;
    }

}