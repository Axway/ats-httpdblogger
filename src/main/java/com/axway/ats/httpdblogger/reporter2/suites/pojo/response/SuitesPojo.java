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
package com.axway.ats.httpdblogger.reporter2.suites.pojo.response;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( "Get all suites response details")
public class SuitesPojo {

    @ApiModelProperty(
            required = true,
            value = "suites",
            notes = "A list of SUITEs. To see the individual suite properties, check the 'Get suite response details' document section",
            example = "{ suites: [ <SUITE_1>, <SUITE_2>, ... ] }")
    private SuitePojo[] suites;

    public SuitesPojo() {

    }

    public SuitePojo[] getSuites() {

        return suites;
    }

    public void setSuites( SuitePojo[] suites ) {

        this.suites = suites;
    }

}
