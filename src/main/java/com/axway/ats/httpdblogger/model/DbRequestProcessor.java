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

import java.util.List;

import com.axway.ats.core.dbaccess.DbConnection;
import com.axway.ats.core.dbaccess.mssql.DbConnSQLServer;
import com.axway.ats.httpdblogger.model.pojo.BasePojo;
import com.axway.ats.httpdblogger.model.pojo.MessagePojo;
import com.axway.ats.httpdblogger.model.pojo.RunMetainfoPojo;
import com.axway.ats.httpdblogger.model.pojo.RunPojo;
import com.axway.ats.httpdblogger.model.pojo.ScenarioMetainfoPojo;
import com.axway.ats.httpdblogger.model.pojo.SuitePojo;
import com.axway.ats.httpdblogger.model.pojo.TestcasePojo;
import com.axway.ats.httpdblogger.model.pojo.TestcaseResultPojo;
import com.axway.ats.log.autodb.DbReadAccess;
import com.axway.ats.log.autodb.DbWriteAccess;
import com.axway.ats.log.autodb.LifeCycleState;
import com.axway.ats.log.autodb.entities.Run;
import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;

/**
 * A wrapper around the ATS DB Writer.
 * 
 * It checks if the current request can be processed, and if so - it sends it to
 * the DB Writer
 */
public class DbRequestProcessor {

    // the current state
    private LifeCycleState state;

    // the ATS DB Writer
    private DbWriteAccess  dbWriteAccess;

    private int            internalDbVersion;

    public DbRequestProcessor() throws DatabaseAccessException {

        state = LifeCycleState.INITIALIZED;
    }

    public void startRun(
                          RunPojo run ) throws DatabaseAccessException {

        evaluateCurrentState( "start RUN", LifeCycleState.INITIALIZED, LifeCycleState.RUN_STARTED );

        // establish DB connection
        DbConnection dbConnection = new DbConnSQLServer( run.getDbHost(),
                                                         run.getDbName(),
                                                         run.getDbUser(),
                                                         run.getDbPassword() );
        dbWriteAccess = new DbWriteAccess( dbConnection, false );

        // start the RUN
        int runId = dbWriteAccess.startRun( run.getRunName(),
                                            run.getOsName(),
                                            run.getProductName(),
                                            run.getVersionName(),
                                            run.getBuildName(),
                                            getTimestampForTheCurrentEvent( run ),
                                            run.getHostName(),
                                            true );

        try {
            setDbInternalVersion( dbWriteAccess.getDatabaseInternalVersion() );
        } catch( NumberFormatException nfe ) {
            setDbInternalVersion( 0 );
            nfe.printStackTrace();
        }

        run.setRunId( runId );
    }

    public void startSuite(
                            RunPojo run,
                            SuitePojo suite ) throws DatabaseAccessException {

        evaluateCurrentState( "start SUITE", LifeCycleState.RUN_STARTED, LifeCycleState.SUITE_STARTED );

        int suiteId = dbWriteAccess.startSuite( suite.getPackageName(),
                                                suite.getSuiteName(),
                                                getTimestampForTheCurrentEvent( suite ),
                                                run.getRunId(),
                                                true );
        suite.setSuiteId( suiteId );
        run.setSuite( suite );
    }

    public void startTestcase(
                               RunPojo run,
                               TestcasePojo testcase ) throws DatabaseAccessException {

        evaluateCurrentState( "start TESTCASE",
                              LifeCycleState.SUITE_STARTED,
                              LifeCycleState.TEST_CASE_STARTED );

        int testcaseId = dbWriteAccess.startTestCase( run.getSuite().getSuiteName(),
                                                      testcase.getScenarioName(),
                                                      testcase.getScenarioDescription(),
                                                      testcase.getTestcaseName(),
                                                      getTimestampForTheCurrentEvent( testcase ),
                                                      run.getSuite().getSuiteId(),
                                                      true );
        testcase.setTestcaseId( testcaseId );
        run.getSuite().setTestcase( testcase );
    }

    public void insertRunMessage(
                                  RunPojo run,
                                  MessagePojo message ) throws DatabaseAccessException {

        evaluateCurrentState( "insert RUN MESSAGE", LifeCycleState.RUN_STARTED, LifeCycleState.RUN_STARTED );

        dbWriteAccess.insertRunMessage( message.getMessage(),
                                        message.getLogLevel().toInt(),
                                        true,
                                        message.getMachineName(),
                                        message.getThreadName(),
                                        getTimestampForTheCurrentEvent( message ),
                                        run.getRunId(),
                                        true );
    }

    public void insertSuiteMessage(
                                    RunPojo run,
                                    MessagePojo message ) throws DatabaseAccessException {

        evaluateCurrentState( "insert SUITE MESSAGE",
                              LifeCycleState.SUITE_STARTED,
                              LifeCycleState.SUITE_STARTED );

        dbWriteAccess.insertSuiteMessage( message.getMessage(),
                                          message.getLogLevel().toInt(),
                                          true,
                                          message.getMachineName(),
                                          message.getThreadName(),
                                          getTimestampForTheCurrentEvent( message ),
                                          run.getSuite().getSuiteId(),
                                          true );
    }

    public void insertMessage(
                               RunPojo run,
                               MessagePojo message ) throws DatabaseAccessException {

        evaluateCurrentState( "insert MESSAGE",
                              LifeCycleState.TEST_CASE_STARTED,
                              LifeCycleState.TEST_CASE_STARTED );

        dbWriteAccess.insertMessage( message.getMessage(),
                                     message.getLogLevel().toInt(),
                                     true,
                                     message.getMachineName(),
                                     message.getThreadName(),
                                     getTimestampForTheCurrentEvent( message ),
                                     run.getSuite().getTestcase().getTestcaseId(),
                                     true );
    }

    public void addRunMetainfo(
                                RunPojo run,
                                RunMetainfoPojo runMetainfo ) throws DatabaseAccessException {

        evaluateCurrentState( "add RunMetainfo", LifeCycleState.RUN_STARTED, LifeCycleState.RUN_STARTED );

        dbWriteAccess.addRunMetainfo( run.getRunId(),
                                      runMetainfo.getMetaKey(),
                                      runMetainfo.getMetaValue(),
                                      true );
    }

    public void addScenarioMetainfo(
                                     RunPojo run,
                                     ScenarioMetainfoPojo scenarioMetainfo ) throws DatabaseAccessException {

        evaluateCurrentState( "add ScenarioMetainfo",
                              LifeCycleState.TEST_CASE_STARTED,
                              LifeCycleState.TEST_CASE_STARTED );

        dbWriteAccess.addScenarioMetainfo( run.getSuite().getTestcase().getTestcaseId(),
                                           scenarioMetainfo.getMetaKey(),
                                           scenarioMetainfo.getMetaValue(),
                                           true );
    }

    public void updateRun(
                           RunPojo oldRun,
                           RunPojo updatedRun ) throws DatabaseAccessException {

        evaluateCurrentState( "update Run", LifeCycleState.RUN_STARTED, LifeCycleState.RUN_STARTED );

        dbWriteAccess.updateRun( oldRun.getRunId(),
                                 updatedRun.getRunName(),
                                 updatedRun.getOsName(),
                                 updatedRun.getProductName(),
                                 updatedRun.getVersionName(),
                                 updatedRun.getBuildName(),
                                 updatedRun.getUserNote(),
                                 updatedRun.getHostName(),
                                 true );
    }

    public void endTestcase(
                             RunPojo run,
                             TestcasePojo testcase,
                             TestcaseResultPojo testcaseResult ) throws DatabaseAccessException {

        evaluateCurrentState( "end TESTCASE",
                              LifeCycleState.TEST_CASE_STARTED,
                              LifeCycleState.SUITE_STARTED );

        try {
            dbWriteAccess.endTestCase( testcaseResult.getTestResult().toInt(),
                                       getTimestampForTheCurrentEvent( testcaseResult ),
                                       testcase.getTestcaseId(),
                                       true );
        } finally {
            run.getSuite().setTestcase( null );
        }
    }

    public void endSuite(
                          RunPojo run ) throws DatabaseAccessException {

        evaluateCurrentState( "end SUITE", LifeCycleState.SUITE_STARTED, LifeCycleState.RUN_STARTED );

        try {
            dbWriteAccess.endSuite( getTimestampForTheCurrentEvent( run.getSuite() ), run.getSuite().getSuiteId(), true );
        } finally {
            run.setSuite( null );
        }
    }

    public void endRun(
                        RunPojo run ) throws DatabaseAccessException {

        evaluateCurrentState( "end RUN", LifeCycleState.RUN_STARTED, LifeCycleState.INITIALIZED );

        setDbInternalVersion( -1 );

        dbWriteAccess.endRun( getTimestampForTheCurrentEvent( run ), run.getRunId(), true );
    }

    private void evaluateCurrentState(
                                       String message,
                                       LifeCycleState expectedState,
                                       LifeCycleState newState ) {

        if( expectedState == state ) {
            state = newState;
        } else {
            throw new IllegalStateException( "Cannot " + message + " as the current state is " + state
                                             + ", but it is expected to be " + expectedState );
        }
    }

    /**
     * Returns info about runs.
     * Note: this is a session-less operation
     * 
     * @param host
     * @param db
     * @param user
     * @param password
     * @param whereClause
     * @param recordsCount
     * @return
     * @throws DatabaseAccessException
     */
    public List<Run> getRuns(
                              String host,
                              String db,
                              String user,
                              String password,
                              String whereClause,
                              int recordsCount ) throws DatabaseAccessException {

        // establish DB connection
        DbReadAccess dbReadAccess = new DbReadAccess( new DbConnSQLServer( host, db, user, password ) );

        try {
            setDbInternalVersion( dbReadAccess.getDatabaseInternalVersion() );
        } catch( NumberFormatException nfe ) {
            setDbInternalVersion( 0 );
            nfe.printStackTrace();
        }

        // return the matching runs
        return dbReadAccess.getRuns( 0, recordsCount, whereClause, "runId", true );

    }

    public void setDbInternalVersion(
                                      int internalDbVersion ) {

        this.internalDbVersion = internalDbVersion;
    }

    public int getDbInternalVersion() {

        return internalDbVersion;
    }

    public LifeCycleState getState() {

        return state;
    }

    private long getTimestampForTheCurrentEvent(
                                                 BasePojo basePojo ) {

        if( basePojo.getTimestamp() != -1 ) {
            return basePojo.getTimestamp();
        } else {
            return System.currentTimeMillis();
        }

    }
}
