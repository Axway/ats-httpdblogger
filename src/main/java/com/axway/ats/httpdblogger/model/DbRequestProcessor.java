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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.axway.ats.core.dbaccess.DbUtils;
import com.axway.ats.core.dbaccess.mssql.DbConnSQLServer;
import com.axway.ats.core.dbaccess.postgresql.DbConnPostgreSQL;
import com.axway.ats.core.utils.IoUtils;
import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.httpdblogger.exceptions.UnknownRunException;
import com.axway.ats.httpdblogger.exceptions.UnknownSuiteException;
import com.axway.ats.httpdblogger.exceptions.UnknownTestcaseException;
import com.axway.ats.httpdblogger.model.pojo.BasePojo;
import com.axway.ats.httpdblogger.model.pojo.request.AddRunMetainfoPojo;
import com.axway.ats.httpdblogger.model.pojo.request.AddScenarioMetainfoPojo;
import com.axway.ats.httpdblogger.model.pojo.request.AddTestcaseMetainfoPojo;
import com.axway.ats.httpdblogger.model.pojo.request.AttachFilePojo;
import com.axway.ats.httpdblogger.model.pojo.request.EndRunPojo;
import com.axway.ats.httpdblogger.model.pojo.request.EndSuitePojo;
import com.axway.ats.httpdblogger.model.pojo.request.EndTestcasePojo;
import com.axway.ats.httpdblogger.model.pojo.request.InsertMessagePojo;
import com.axway.ats.httpdblogger.model.pojo.request.InsertMessagesPojo;
import com.axway.ats.httpdblogger.model.pojo.request.StartRunPojo;
import com.axway.ats.httpdblogger.model.pojo.request.StartSuitePojo;
import com.axway.ats.httpdblogger.model.pojo.request.StartTestcasePojo;
import com.axway.ats.httpdblogger.model.pojo.request.UpdateRunPojo;
import com.axway.ats.httpdblogger.model.pojo.request.UpdateSuitePojo;
import com.axway.ats.log.autodb.logqueue.LifeCycleState;
import com.axway.ats.log.autodb.io.PGDbReadAccess;
import com.axway.ats.log.autodb.io.SQLServerDbReadAccess;
import com.axway.ats.log.autodb.entities.Run;
import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;

/**
 * A wrapper around the ATS DB Writer.
 * 
 * It checks if the current request can be processed, and if so - it sends it to
 * the DB Writer
 */
public class DbRequestProcessor {

    private static final String      ATTACHED_FILES_PROPERTY = "ats.attached.files.dir";

    private final long               MAX_FILE_SIZE           = 10 * 1024 * 1024;                          // 10MB

    private static final Logger      log                     = Logger.getLogger(DbRequestProcessor.class);

    // the current state
    private LifeCycleState           state;

    // the ATS DB Writer
    private IHttpDbLoggerWriteAccess dbWriteAccess;

    private int                      internalDbVersion;

    public DbRequestProcessor() {

        state = LifeCycleState.INITIALIZED;
    }

    public void startRun( StartRunPojo run ) throws DatabaseAccessException {

        evaluateCurrentState("start RUN", LifeCycleState.INITIALIZED, LifeCycleState.RUN_STARTED);

        int dbPort = -1;
        try {
            dbPort = Integer.parseInt(run.getDbPort());
        } catch (Exception e) {
            throw new DatabaseAccessException("Provided db port is not a valid INTEGER number", e);
        }

        Exception mssqlException = DbUtils.isMSSQLDatabaseAvailable(run.getDbHost(), dbPort, run.getDbName(),
                                                                    run.getDbUser(),
                                                                    run.getDbPassword());
        if (mssqlException == null) {
            dbWriteAccess = new HttpDbLoggerSQLServerWriteAccess(new DbConnSQLServer(run.getDbHost(),
                                                                                     run.getDbName(),
                                                                                     run.getDbUser(),
                                                                                     run.getDbPassword()));
        } else {
            Exception pgsqlException = DbUtils.isPostgreSQLDatabaseAvailable(run.getDbHost(), dbPort, run.getDbName(),
                                                                             run.getDbUser(),
                                                                             run.getDbPassword());
            if (pgsqlException == null) {
                dbWriteAccess = new HttpDbLoggerPGWriteAccess(new DbConnPostgreSQL(run.getDbHost(),
                                                                                   run.getDbName(),
                                                                                   run.getDbUser(),
                                                                                   run.getDbPassword()));
            } else {

                String errMsg = "Neither MSSQL, nor PostgreSQL server at '" + run.getDbHost() + ":"
                                + dbPort +
                                "' has database with name '" + run.getDbName()
                                + "'. Exception for MSSQL is : \n\t" + mssqlException
                                + "\n\nException for PostgreSQL is: \n\t"
                                + pgsqlException;
                throw new DatabaseAccessException(errMsg);
            }
        }

        // start the RUN
        int runId = dbWriteAccess.startRun(run.getRunName(), run.getOsName(), run.getProductName(),
                                           run.getVersionName(), run.getBuildName(),
                                           getTimestampForTheCurrentEvent(run), run.getHostName(), true);

        try {
            setDbInternalVersion(dbWriteAccess.getDatabaseInternalVersion());
        } catch (NumberFormatException nfe) {
            setDbInternalVersion(0);
            nfe.printStackTrace();
        }

        run.setRunId(runId);
    }

    public int startSuite( SessionData sd, StartSuitePojo suite,
                           boolean setAsCurrentSuite ) throws DatabaseAccessException {

        // no parent id was provided by the request, so set the suite as a current one
        if (setAsCurrentSuite) {

            evaluateCurrentState("start SUITE", LifeCycleState.RUN_STARTED, LifeCycleState.SUITE_STARTED);

            return dbWriteAccess.startSuite(suite.getPackageName(), suite.getSuiteName(),
                                            getTimestampForTheCurrentEvent(suite), sd.getRun().getRunId(), // use current runId
                                            true);
        } else {// parent id was provided by the request, so try to add it to the specified run
            validateRunId(sd.getRun().getRunId(), suite.getParentId(), suite.getSessionId());

            return dbWriteAccess.startSuite(suite.getPackageName(), suite.getSuiteName(),
                                            getTimestampForTheCurrentEvent(suite), suite.getParentId(), // use suite's runId (as provided by the REST request)
                                            true);
        }
    }

    public int startTestcase( SessionData sd, StartTestcasePojo testcase,
                              boolean setAsCurrentTestcase ) throws DatabaseAccessException {

        if (setAsCurrentTestcase) {

            evaluateCurrentState("start TESTCASE", LifeCycleState.SUITE_STARTED,
                                 LifeCycleState.TEST_CASE_STARTED);

            return dbWriteAccess.startTestCase(sd.getRun().getSuite().getSuiteName(),
                                               testcase.getScenarioName(), testcase.getScenarioDescription(),
                                               testcase.getTestcaseName(),
                                               getTimestampForTheCurrentEvent(testcase),
                                               sd.getRun().getSuite().getSuiteId(), true);

        } else {// parent id was provided by the request, so try to add it to the specified suite 

            validateSuiteId(sd, testcase.getParentId(), testcase.getSessionId());

            return dbWriteAccess.startTestCase(sd.getSuitesMap().get(testcase.getParentId()),
                                               testcase.getScenarioName(), testcase.getScenarioDescription(),
                                               testcase.getTestcaseName(),
                                               getTimestampForTheCurrentEvent(testcase),
                                               testcase.getParentId(), true);
        }

    }

    public void insertRunMessage( SessionData sd, InsertMessagePojo message,
                                  boolean skipLifeCycleStateCheck ) throws DatabaseAccessException {

        if (skipLifeCycleStateCheck) {
            // we will skip the lifecycle check and add the run message, to the run, referred by the message's parentId
            dbWriteAccess.insertRunMessage(message.getMessage(), message.getLogLevel().toInt(), true,
                                           message.getMachineName(), message.getThreadName(),
                                           getTimestampForTheCurrentEvent(message), message.getParentId(),
                                           true);

        } else {
            evaluateCurrentState("insert RUN MESSAGE", LifeCycleState.RUN_STARTED,
                                 LifeCycleState.RUN_STARTED);

            dbWriteAccess.insertRunMessage(message.getMessage(), message.getLogLevel().toInt(), true,
                                           message.getMachineName(), message.getThreadName(),
                                           getTimestampForTheCurrentEvent(message), sd.getRun().getRunId(),
                                           true);

        }

    }

    public void insertRunMessages( SessionData sd, InsertMessagesPojo messages,
                                   boolean skipLifeCycleStateCheck ) throws DatabaseAccessException {

        // if the timestamp is -1, use the local one (System.currentTimeMillis())
        messages.setTimestamp(getTimestampForTheCurrentEvent(messages));

        if (skipLifeCycleStateCheck) {
            // we will skip the lifecycle check and add the run message, to the run, referred by the message's parentId

            dbWriteAccess.insertRunMessages(messages);

        } else {
            evaluateCurrentState("insert RUN MESSAGES", LifeCycleState.RUN_STARTED,
                                 LifeCycleState.RUN_STARTED);

            // the request does not provide a runId, so use the one of the current Run
            messages.setParentId(sd.getRun().getRunId());

            dbWriteAccess.insertRunMessages(messages);

        }

    }

    public void insertSuiteMessage( SessionData sd, InsertMessagePojo message,
                                    boolean skipLifeCycleStateCheck ) throws DatabaseAccessException {

        if (skipLifeCycleStateCheck) {
            // we will skip the lifecycle check and add the suite message, to the suite, referred by the message's parentId

            validateSuiteId(sd, message.getParentId(), message.getSessionId());

            dbWriteAccess.insertSuiteMessage(message.getMessage(), message.getLogLevel().toInt(), true,
                                             message.getMachineName(), message.getThreadName(),
                                             getTimestampForTheCurrentEvent(message),
                                             message.getParentId(), true);
        } else {
            evaluateCurrentState("insert SUITE MESSAGE", LifeCycleState.SUITE_STARTED,
                                 LifeCycleState.SUITE_STARTED);

            dbWriteAccess.insertSuiteMessage(message.getMessage(), message.getLogLevel().toInt(), true,
                                             message.getMachineName(), message.getThreadName(),
                                             getTimestampForTheCurrentEvent(message),
                                             sd.getRun().getSuite().getSuiteId(), true);

        }

    }

    public void insertSuiteMessages( SessionData sd, InsertMessagesPojo messages,
                                     boolean skipLifeCycleStateCheck ) throws DatabaseAccessException {

        // if the timestamp is -1, use the local one (System.currentTimeMillis())
        messages.setTimestamp(getTimestampForTheCurrentEvent(messages));

        if (skipLifeCycleStateCheck) {
            // we will skip the lifecycle check and add the suite message, to the suite, referred by the message's parentId

            validateSuiteId(sd, messages.getParentId(), messages.getSessionId());

            dbWriteAccess.insertSuiteMessages(messages);
        } else {
            evaluateCurrentState("insert SUITE MESSAGES", LifeCycleState.SUITE_STARTED,
                                 LifeCycleState.SUITE_STARTED);

            // the request does not provide a suiteId, so use the one of the current Suite
            messages.setParentId(sd.getRun().getSuite().getSuiteId());

            dbWriteAccess.insertSuiteMessages(messages);

        }

    }

    public void insertMessage( SessionData sd, InsertMessagePojo message,
                               boolean skipLifeCycleStateCheck ) throws DatabaseAccessException {

        if (skipLifeCycleStateCheck) {
            // we will skip the lifecycle check and add the testcase message, to the testcase, referred by the message's parentId

            validateTestcaseId(sd, message.getParentId(), message.getSessionId());

            dbWriteAccess.insertMessage(message.getMessage(), message.getLogLevel().toInt(), true,
                                        message.getMachineName(), message.getThreadName(),
                                        getTimestampForTheCurrentEvent(message), message.getParentId(),
                                        true);

        } else {
            evaluateCurrentState("insert MESSAGE", LifeCycleState.TEST_CASE_STARTED,
                                 LifeCycleState.TEST_CASE_STARTED);

            dbWriteAccess.insertMessage(message.getMessage(), message.getLogLevel().toInt(), true,
                                        message.getMachineName(), message.getThreadName(),
                                        getTimestampForTheCurrentEvent(message),
                                        sd.getRun().getSuite().getTestcase().getTestcaseId(), true);
        }

    }

    public void insertMessages( SessionData sd, InsertMessagesPojo messages,
                                boolean skipLifeCycleStateCheck ) throws DatabaseAccessException {

        // if the timestamp is -1, use the local one (System.currentTimeMillis())
        messages.setTimestamp(getTimestampForTheCurrentEvent(messages));

        if (skipLifeCycleStateCheck) {

            validateTestcaseId(sd, messages.getParentId(), messages.getSessionId());

            dbWriteAccess.insertMessages(messages);

        } else {

            evaluateCurrentState("insert MESSAGES", LifeCycleState.TEST_CASE_STARTED,
                                 LifeCycleState.TEST_CASE_STARTED);

            // the request does not provide a testcaseId, so use the one of the current Testcase
            messages.setParentId(sd.getRun().getSuite().getTestcase().getTestcaseId());

            dbWriteAccess.insertMessages(messages);
        }

    }

    public void addRunMetainfo( StartRunPojo run,
                                AddRunMetainfoPojo runMetainfo ) throws DatabaseAccessException {

        /* We did not check the state as in startRun, startTestcase,
         * because we want to always be able to add run metainfo, as long as a run was started
         */

        dbWriteAccess.addRunMetainfo(run.getRunId(), runMetainfo.getMetaKey(), runMetainfo.getMetaValue(),
                                     true);

    }

    public void
            addScenarioMetainfo( SessionData sd, AddScenarioMetainfoPojo scenarioMetainfo,
                                 boolean addScenarioMetaInfoToCurrentTestcase ) throws DatabaseAccessException {

        if (addScenarioMetaInfoToCurrentTestcase) {
            evaluateCurrentState("add ScenarioMetainfo", LifeCycleState.TEST_CASE_STARTED,
                                 LifeCycleState.TEST_CASE_STARTED);

            dbWriteAccess.addScenarioMetainfo(sd.getRun().getSuite().getTestcase().getTestcaseId(),
                                              scenarioMetainfo.getMetaKey(), scenarioMetainfo.getMetaValue(),
                                              true);
        } else {

            validateTestcaseId(sd, scenarioMetainfo.getParentId(), scenarioMetainfo.getSessionId());

            dbWriteAccess.addScenarioMetainfo(scenarioMetainfo.getParentId(), scenarioMetainfo.getMetaKey(),
                                              scenarioMetainfo.getMetaValue(), true);
        }

    }

    public void
            addTestcaseMetainfo( SessionData sd, AddTestcaseMetainfoPojo testcaseMetainfo,
                                 boolean addTestcaseMetaInfoToCurrentTestcase ) throws DatabaseAccessException {

        if (addTestcaseMetaInfoToCurrentTestcase) {
            evaluateCurrentState("add TestcaseMetainfo", LifeCycleState.TEST_CASE_STARTED,
                                 LifeCycleState.TEST_CASE_STARTED);

            dbWriteAccess.addTestcaseMetainfo(sd.getRun().getSuite().getTestcase().getTestcaseId(),
                                              testcaseMetainfo.getMetaKey(), testcaseMetainfo.getMetaValue(),
                                              true);
        } else {

            validateTestcaseId(sd, testcaseMetainfo.getParentId(), testcaseMetainfo.getSessionId());

            dbWriteAccess.addTestcaseMetainfo(testcaseMetainfo.getParentId(), testcaseMetainfo.getMetaKey(),
                                              testcaseMetainfo.getMetaValue(), true);
        }

    }

    public void attachFile( SessionData sd, AttachFilePojo attachFilePojo,
                            boolean attachFileToCurrentTestcase, int testcaseId ) throws Exception {

        if (attachFileToCurrentTestcase) {
            evaluateCurrentState("attach file", LifeCycleState.TEST_CASE_STARTED,
                                 LifeCycleState.TEST_CASE_STARTED);
        } else {
            validateTestcaseId(sd, attachFilePojo.getParentId(), attachFilePojo.getSessionId());
        }

        String attachmentsDir = System.getProperty(ATTACHED_FILES_PROPERTY);
        if (StringUtils.isNullOrEmpty(attachmentsDir)) {
            attachmentsDir = System.getenv(ATTACHED_FILES_PROPERTY);
        }
        if (StringUtils.isNullOrEmpty(attachmentsDir)) {
            attachmentsDir = getProperties().getProperty(ATTACHED_FILES_PROPERTY);
        }
        if (StringUtils.isNullOrEmpty(attachmentsDir)) {
            attachmentsDir = System.getenv("CATALINA_BASE");
        }
        if (StringUtils.isNullOrEmpty(attachmentsDir)) {
            attachmentsDir = System.getenv("CATALINA_HOME");
        }

        if (StringUtils.isNullOrEmpty(attachmentsDir)) {
            String errorMessage = "No directory for attached files was configured. "
                                  + "You can set such directory in one of the following ways: " + "key '"
                                  + ATTACHED_FILES_PROPERTY
                                  + "' as a system variable, environment variable or property in the WEB-INF/classes/ats.config.properties configuration file in the HTTP DB Logger war file. "
                                  + "Last option is to set 'CATALINA_BASE' or 'CATALINA_HOME' when running on Tomcat.";
            throw new RuntimeException(errorMessage);
        }

        attachmentsDir = attachmentsDir.replace("\\", "/");

        int runId = sd.getRun().getRunId();
        int suiteId = sd.getRun().getSuite().getSuiteId();

        String dbName = sd.getRun().getDbName();

        String attachedFilesDir = attachmentsDir + "/ats-attached-files" + "/" + dbName;
        attachedFilesDir = attachedFilesDir + "/" + runId + "/" + suiteId + "/" + testcaseId;
        attachedFilesDir = IoUtils.normalizeFilePath(attachedFilesDir);

        File attachedDirFile = new File(attachedFilesDir);
        if (!attachedDirFile.exists()) {
            attachedDirFile.mkdirs();
        }

        String filePath = attachedFilesDir + "/" + attachFilePojo.getFileName();
        File newFile = new File(filePath);
        try {
            IoUtils.copyStream(attachFilePojo.getInputStream(),
                               new BufferedOutputStream(new FileOutputStream(newFile)),
                               true, true, MAX_FILE_SIZE);
        } catch (IOException e) {

            if (e.getMessage().contains("Max size of " + MAX_FILE_SIZE + " bytes reached.")) {
                newFile.delete();
            }

            throw e;
        }

    }

    public void updateRun( StartRunPojo oldRun, UpdateRunPojo updatedRun ) throws DatabaseAccessException {

        /*
         * Updating a run is always permitted, as long as the run being updated exists and is started by the current session
         * */

        dbWriteAccess.updateRun(oldRun.getRunId(), updatedRun.getRunName(), updatedRun.getOsName(),
                                updatedRun.getProductName(), updatedRun.getVersionName(),
                                updatedRun.getBuildName(), updatedRun.getUserNote(),
                                updatedRun.getHostName(), true);
    }

    public void updateSuite( SessionData sd, int suiteId, UpdateSuitePojo suite,
                             boolean skipLifeCycleStateCheck ) throws DatabaseAccessException {

        if (skipLifeCycleStateCheck) {
            // check if the suite, referred by the provided suiteId is started by the current session
            validateSuiteId(sd, suite.getSuiteId(), suite.getSessionId());

        } else {
            evaluateCurrentState("update SUITE", LifeCycleState.ATLEAST_SUITE_STARTED,
                                 sd.getDbRequestProcessor().getState());
        }

        dbWriteAccess.updateSuite(suiteId, suite.getSuiteName(), suite.getUserNote(), true);

    }

    public void endTestcase( SessionData sd, int testcaseId, EndTestcasePojo endTestcasePojo,
                             boolean endCurrentTestcase ) throws DatabaseAccessException {

        if (endCurrentTestcase) {
            evaluateCurrentState("end TESTCASE", LifeCycleState.TEST_CASE_STARTED,
                                 LifeCycleState.SUITE_STARTED);

            try {
                dbWriteAccess.endTestCase(endTestcasePojo.getTestResult().toInt(),
                                          getTimestampForTheCurrentEvent(endTestcasePojo), testcaseId,
                                          true);
            } finally {
                sd.getRun().getSuite().setTestcase(null);
            }

        } else {

            validateTestcaseId(sd, testcaseId, endTestcasePojo.getSessionId());

            dbWriteAccess.endTestCase(endTestcasePojo.getTestResult().toInt(),
                                      getTimestampForTheCurrentEvent(endTestcasePojo), testcaseId, true);
        }

    }

    public void endSuite( SessionData sd, EndSuitePojo endSuitePojo,
                          boolean endCurrentSuite ) throws DatabaseAccessException {

        if (endCurrentSuite) {
            evaluateCurrentState("end SUITE", LifeCycleState.SUITE_STARTED, LifeCycleState.RUN_STARTED);

            try {
                dbWriteAccess.endSuite(getTimestampForTheCurrentEvent(endSuitePojo),
                                       sd.getRun().getSuite().getSuiteId(), true);
            } finally {
                sd.getRun().setSuite(null);
            }
        } else {

            validateSuiteId(sd, endSuitePojo.getSuiteId(), endSuitePojo.getSessionId());

            dbWriteAccess.endSuite(getTimestampForTheCurrentEvent(endSuitePojo), endSuitePojo.getSuiteId(),
                                   true);

            // check if the provided suiteId is the same as the current suite' id
            if (sd.getRun().getSuite() != null
                && endSuitePojo.getSuiteId() == sd.getRun().getSuite().getSuiteId()) {
                // close the current suite
                this.state = LifeCycleState.RUN_STARTED;
                sd.getRun().setSuite(null);
            }
        }

    }

    public void endRun( StartRunPojo run, EndRunPojo endRunPojo ) throws DatabaseAccessException {

        evaluateCurrentState("end RUN", LifeCycleState.RUN_STARTED, LifeCycleState.INITIALIZED);

        setDbInternalVersion(-1);

        dbWriteAccess.endRun(getTimestampForTheCurrentEvent(endRunPojo), run.getRunId(), true);
    }

    private void evaluateCurrentState( String message, LifeCycleState expectedState,
                                       LifeCycleState newState ) {

        if (expectedState == LifeCycleState.ATLEAST_SUITE_STARTED) {

            if (state == LifeCycleState.INITIALIZED) {
                throw new IllegalStateException("Cannot " + message + " as the current state is " + state
                                                + ", but it is expected to be " + expectedState);
            }

        } else {
            if (expectedState == state) {
                state = newState;
            } else {
                throw new IllegalStateException("Cannot " + message + " as the current state is " + state
                                                + ", but it is expected to be " + expectedState);
            }
        }

    }

    /**
     * Returns info about runs.
     * Note: this is a session-less operation
     * 
     * @param host DB host name
     * @param port DB port to use to connect
     * @param db database name
     * @param user username for DB connection
     * @param password password  for DB connection
     * @param whereClause filtering clause
     * @param recordsCount
     * @param timeOffset local time offset relativeto UTC to be applied
     * @return
     * @throws DatabaseAccessException
     */
    public List<Run> getRuns( String host, int port, String db, String user, String password, String whereClause,
                              int recordsCount, String timeOffset ) throws DatabaseAccessException {

        SQLServerDbReadAccess dbReadAccess = null;
        // establish DB connection
        Exception mssqlException = DbUtils.isMSSQLDatabaseAvailable(host, port, db, user, password);
        if (mssqlException == null) {
            dbReadAccess = new SQLServerDbReadAccess(new DbConnSQLServer(host, port, db, user, password, null));
        } else {
            Exception pgsqlException = DbUtils.isPostgreSQLDatabaseAvailable(host, port, db, user, password);

            if (pgsqlException == null) {
                dbReadAccess = new PGDbReadAccess(new DbConnPostgreSQL(host, port, db, user, password, null));
            } else {
                String errMsg = "Neither MSSQL, nor PostgreSQL server at '" + host + ":"
                                + port +
                                "' has database with name '" + db
                                + "'. Exception for MSSQL is : \n\t" + mssqlException
                                + "\n\nException for PostgreSQL is: \n\t"
                                + pgsqlException;
                throw new DatabaseAccessException(errMsg);
            }
        }

        try {
            setDbInternalVersion(dbReadAccess.getDatabaseInternalVersion());
        } catch (NumberFormatException nfe) {
            setDbInternalVersion(0);
            nfe.printStackTrace();
        }

        // return the matching runs
        return dbReadAccess.getRuns(0, recordsCount, whereClause, "runId", true,
                                    Integer.parseInt(timeOffset));

    }

    public void setDbInternalVersion( int internalDbVersion ) {

        this.internalDbVersion = internalDbVersion;
    }

    public int getDbInternalVersion() {

        return internalDbVersion;
    }

    public LifeCycleState getState() {

        return state;
    }

    private long getTimestampForTheCurrentEvent( BasePojo basePojo ) {

        if (basePojo.getTimestamp() != -1) {
            return basePojo.getTimestamp();
        } else {
            return System.currentTimeMillis();
        }

    }

    private void validateRunId( int expectedRunId, int providedRunId, String sessionId ) {

        if (expectedRunId != providedRunId) {
            throw new UnknownRunException("The provided run id does not match the run id from this session. Session id was '"
                                          + sessionId + "', provided run id was '" + providedRunId
                                          + "' and expected run id was '" + expectedRunId + "'.");
        }
    }

    private void validateSuiteId( SessionData sd, int providedSuiteId, String sessionId ) {

        boolean isKnownSuiteId = sd.hasSuiteId(providedSuiteId);

        if (!isKnownSuiteId) {
            throw new UnknownSuiteException("The provided suite id does not match any suite id from this session. Session id was '"
                                            + sessionId + "'and provided suite id was '" + providedSuiteId
                                            + "'.");
        }
    }

    private void validateTestcaseId( SessionData sd, int providedTestcaseId, String sessionId ) {

        boolean isKnownTestcaseId = sd.hasTestcaseId(providedTestcaseId);

        if (!isKnownTestcaseId) {
            throw new UnknownTestcaseException("The provided testcase id does not match any testcase id from this session. Session id was '"
                                               + sessionId + "'and provided testcase id was '"
                                               + providedTestcaseId + "'.");
        }

    }

    private Properties getProperties() {

        Properties configProperties = new Properties();
        try {
            configProperties.load(this.getClass()
                                      .getClassLoader()
                                      .getResourceAsStream("ats.config.properties"));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Can't load ats.config.properties file");
            }
        }
        return configProperties;
    }

}
