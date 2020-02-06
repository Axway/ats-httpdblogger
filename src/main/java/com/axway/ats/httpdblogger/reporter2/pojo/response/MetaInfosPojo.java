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

@ApiModel( "Get list of metainfo response details")
public class MetaInfosPojo {

    @ApiModelProperty(
            required = true,
            value = "metainfo",
            notes = "A list of METAINFOs",
            example = "{ metainfo: [ <METAINFO_1>, <METAINFO_2>, ... ] }")
    private MetaInfoPojo[] metaInfo;

    public MetaInfosPojo() {}

    public MetaInfoPojo[] getMetaInfo() {

        return metaInfo;
    }

    public void setMetaInfo( MetaInfoPojo[] metaInfo ) {

        this.metaInfo = metaInfo;
    }

}
