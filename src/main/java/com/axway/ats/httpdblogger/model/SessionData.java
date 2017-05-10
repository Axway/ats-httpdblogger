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
package com.axway.ats.httpdblogger.model;

import java.util.HashSet;
import java.util.Set;

import com.axway.ats.httpdblogger.model.pojo.request.StartRunPojo;

/**
 * The session data
 */
public class SessionData {

    // the current RUN
    private StartRunPojo       run;

    // keep track of suites (only their IDs), that were started/ended by the current session
    private Set<Integer>       suitesIds;

    // keep track of testcases (only their IDs), that were started/ended by the current session
    private Set<Integer>       testcasesIds;

    // a wrapper around the ATS DB Writer
    private DbRequestProcessor dbRequestProcessor;

    public SessionData() {
        suitesIds = new HashSet<>();
        testcasesIds = new HashSet<>();
    }

    public DbRequestProcessor getDbRequestProcessor() {

        if( dbRequestProcessor == null ) {
            dbRequestProcessor = new DbRequestProcessor();
        }
        return dbRequestProcessor;
    }

    public StartRunPojo getRun() {

        return this.run;
    }

    public void setRun(
                        StartRunPojo run ) {

        this.run = run;
    }

    public Set<Integer> getSuitesIds() {

        return suitesIds;
    }

    public Set<Integer> getTestcasesIds() {

        return testcasesIds;
    }

    public void addTestcaseId(
                               int id ) {

        testcasesIds.add( id );
    }

    public void addSuiteId(
                            int id ) {

        suitesIds.add( id );
    }
    
    public boolean hasSuiteId(int suiteId){
        return suitesIds.contains( suiteId );
    }
    
    public boolean hasTestcaseId(int testcaseId){
        return testcasesIds.contains( testcaseId );
    }

}
