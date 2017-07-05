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

import java.util.HashMap;
import java.util.Map;

import com.axway.ats.httpdblogger.model.pojo.request.StartRunPojo;

/**
 * The session data
 */
public class SessionData {

    // the current RUN
    private StartRunPojo         run;

    // keep track of suites (their IDs and names), that were started/ended by the current session
    private Map<Integer, String> suitesMap;

    // keep track of testcases (their IDs and names), that were started/ended by the current session
    private Map<Integer, String> testcasesMap;

    // a wrapper around the ATS DB Writer
    private DbRequestProcessor   dbRequestProcessor;

    public SessionData() {
        suitesMap = new HashMap<>();
        testcasesMap = new HashMap<>();
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

    public void setRun( StartRunPojo run ) {

        this.run = run;
    }

    public Map<Integer, String> getSuitesMap() {

        return suitesMap;
    }

    public Map<Integer, String> getTestcasesMap() {

        return testcasesMap;
    }

    public void addTestcaseId( int id, String name ) {

        testcasesMap.put( id, name );
    }

    public void addSuiteId( int id, String name ) {

        suitesMap.put( id, name );
    }

    public boolean hasSuiteId( int suiteId ) {

        return suitesMap.containsKey( suiteId );
    }

    public boolean hasTestcaseId( int testcaseId ) {

        return testcasesMap.containsKey( testcaseId );
    }

}
