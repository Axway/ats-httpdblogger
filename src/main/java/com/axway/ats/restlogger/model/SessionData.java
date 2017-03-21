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
package com.axway.ats.restlogger.model;

import java.util.Date;

import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;
import com.axway.ats.restlogger.model.pojo.RunPojo;

/**
 * The session data
 */
public class SessionData {

    // the current RUN
    private RunPojo                run;
    
    // a wrapper around the ATS DB Writer
    private DbRequestProcessor dbRequestProcessor;

    // The last time this session was called.
    // This flag is used to discover not used sessions.
    private long               lastUsed;

    public SessionData() {

        updateLastUsedFlag();
    }

    public DbRequestProcessor getDbRequestProcessor() throws DatabaseAccessException {

        if( dbRequestProcessor == null ) {
            dbRequestProcessor = new DbRequestProcessor();
        }
        return dbRequestProcessor;
    }

    public RunPojo getRun() {

        return this.run;
    }

    public void setRun(
                        RunPojo run ) {

        this.run = run;
    }

    public void updateLastUsedFlag() {

        lastUsed = new Date().getTime();
    }

    public long getLastUsedFlag() {

        return lastUsed;
    }
}
