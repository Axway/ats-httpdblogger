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
package com.axway.ats.restlogger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.log.autodb.LifeCycleState;
import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;
import com.axway.ats.restlogger.exceptions.InvalidSessionException;
import com.axway.ats.restlogger.model.SessionData;
import com.axway.ats.restlogger.model.TestResult;
import com.axway.ats.restlogger.model.pojo.BasePojo;
import com.axway.ats.restlogger.model.pojo.MessagePojo;
import com.axway.ats.restlogger.model.pojo.ResponseStartRunPojo;
import com.axway.ats.restlogger.model.pojo.ResponseStartSuitePojo;
import com.axway.ats.restlogger.model.pojo.ResponseStartTestcasePojo;
import com.axway.ats.restlogger.model.pojo.RunMetainfoPojo;
import com.axway.ats.restlogger.model.pojo.RunPojo;
import com.axway.ats.restlogger.model.pojo.ScenarioMetainfoPojo;
import com.axway.ats.restlogger.model.pojo.SuitePojo;
import com.axway.ats.restlogger.model.pojo.TestcasePojo;
import com.axway.ats.restlogger.model.pojo.TestcaseResultPojo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * The entry point for this REST application. All available public methods are
 * listed in this class.
 */
@Path("logger")
@Api(value = "/logger")
public class Logger extends BaseEntry {

    // Map the session UID with the particular session.
    // A new session is create on starting a RUN and is discarded on ending a RUN or when it has expired.
    private static Map<String, SessionData> sessions = Collections.synchronizedMap( new HashMap<String, SessionData>() );

    // A session will expire and will be discarded if not used for the specified idle time - currently 24 hours
    private static final long SESSION_MAX_IDLE_TIME = 24 * 60 * 60 * 1000;

    @POST
    @Path("startRun")
    @ApiOperation(value = "Start run", notes = "", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful run start. Returning the session ID", response = Response.class),
                            @ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startRun(
                              @Context HttpServletRequest request,
                              @ApiParam(value = "Run details", required = true ) RunPojo run) {

        // we do some cleanup here, it is not be very resource consuming
        cleanupExpiredSessions();

        // create a new session for the new RUN
        SessionData sd = new SessionData();
        String sessionId = UUID.randomUUID().toString();
        sessions.put( sessionId, sd );

        // start the new RUN
        logInfo( request, "Starting run " + run.getRunName() + " with sessionId='" + sessionId + "'" );

        try {
            sd.getDbRequestProcessor().startRun( run );
            sd.setRun( run );
            return Response.ok( new ResponseStartRunPojo( sessionId, run.getRunId() ) ).build();
        } catch( DatabaseAccessException e ) {
            return returnError( e, "Unable to start run" );
        }
    }

    @POST
    @Path("startSuite")
    @ApiOperation(value = "Start suite", notes = "")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful starting suite"),
                            @ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startSuite(
                                @Context HttpServletRequest request,
                                @ApiParam(value = "Suite details", required = true ) SuitePojo suite) {

        logInfo( request, "Starting suite " + suite.getSuiteName() );

        try {
            SessionData sd = getSessionData( suite.getSessionId() );

            sd.getDbRequestProcessor().startSuite( sd.getRun(), suite );
            return Response.ok( new ResponseStartSuitePojo( suite.getSuiteId() ) ).build();
        } catch( Exception e ) {
            return returnError( e, "Unable to start suite" );
        }
    }

    @POST
    @Path("startTestcase")
    @ApiOperation(value = "Start testcase", notes = "")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful starting testcase"),
                            @ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startTestcase(
                                   @Context HttpServletRequest request,
                                   @ApiParam(value = "Testcase details", required = true ) TestcasePojo testcase) {

        logInfo( request, "Starting testcase " + testcase.getTestcaseName() );

        try {
            SessionData sd = getSessionData( testcase.getSessionId() );

            sd.getDbRequestProcessor().startTestcase( sd.getRun(), testcase );
            return Response.ok( new ResponseStartTestcasePojo( testcase.getTestcaseId() ) ).build();
        } catch( Exception e ) {
            return returnError( e, "Unable to start testcase" );
        }
    }

    @POST
    @Path("insertMessage")
    @ApiOperation(value = "Insert Message", notes = "")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful inserted message"),
                            @ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertMessage(
                                   @Context HttpServletRequest request,
                                   @ApiParam(value = "Message details", required = true ) MessagePojo message) {

        try {
            SessionData sd = getSessionData( message.getSessionId() );
            if( sd.getDbRequestProcessor().getState() == LifeCycleState.RUN_STARTED ) {
                logInfo( request, "Inserting message for run" + sd.getRun().getRunName() );

                sd.getDbRequestProcessor().insertRunMessage( sd.getRun(), message );
            } else if( sd.getDbRequestProcessor().getState() == LifeCycleState.SUITE_STARTED ) {
                logInfo( request, "Inserting message for suite" + sd.getRun().getSuite().getSuiteName() );

                sd.getDbRequestProcessor().insertSuiteMessage( sd.getRun(), message );
            } else if( sd.getDbRequestProcessor().getState() == LifeCycleState.TEST_CASE_STARTED ) {
                logInfo( request, "Inserting message for testcase"
                                  + sd.getRun().getSuite().getTestcase().getTestcaseName() );

                sd.getDbRequestProcessor().insertMessage( sd.getRun(), message );
            } else {
                throw new IllegalStateException( "Unable to insert message, because no run, suite or testcase has been previously started." );
            }

            return Response.ok().build();
        } catch( Exception e ) {
            return returnError( e, "Unable to insert message" );
        }
    }
    
    @POST
    @Path("addRunMetainfo")
    @ApiOperation(value = "Add RunMetainfo", notes = "")
    @ApiResponses({ @com.wordnik.swagger.annotations.ApiResponse(code = 200, message = "Successful added run metainfo"),
                    @com.wordnik.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response
            addRunMetainfo( @Context HttpServletRequest request,
                            @ApiParam(value = "Run metainfo details", required = true) RunMetainfoPojo runMetainfo ) {

        try {
            SessionData sd = getSessionData( runMetainfo.getSessionId() );

            logInfo( request, "Adding run metainfo for run" + sd.getRun().getRunName() );

            sd.getDbRequestProcessor().addRunMetainfo( sd.getRun(), runMetainfo );
            return Response.ok().build();
        } catch( Exception e ) {
            return returnError( e, "Unable to add run metainfo" );
        }
    }

    @POST
    @Path("addScenarioMetainfo")
    @ApiOperation(value = "Add ScenarioMetainfo", notes = "")
    @ApiResponses({ @com.wordnik.swagger.annotations.ApiResponse(code = 200, message = "Successful added scenario metainfo"),
                    @com.wordnik.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response
            addScenarioMetainfo( @Context HttpServletRequest request,
                                 @ApiParam(value = "Scenario metainfo details", required = true) ScenarioMetainfoPojo scenarioMetainfo ) {

        try {
            SessionData sd = getSessionData( scenarioMetainfo.getSessionId() );

            logInfo( request, "Adding scenario metainfo for testcase"
                              + sd.getRun().getSuite().getTestcase().getTestcaseName() );

            sd.getDbRequestProcessor().addScenarioMetainfo( sd.getRun(), scenarioMetainfo );
            return Response.ok().build();
        } catch( Exception e ) {
            return returnError( e, "Unable to add scenario metainfo" );
        }
    }

    @POST
    @Path("updateRun")
    @ApiOperation(value = "Update run", notes = "")
    @ApiResponses({ @com.wordnik.swagger.annotations.ApiResponse(code = 200, message = "Successful updating run"),
                    @com.wordnik.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response updateRun( @Context HttpServletRequest request,
                               @ApiParam(value = "New Run details", required = true) RunPojo run ) {

        try {
            SessionData sd = getSessionData( run.getSessionId() );

            logInfo( request, "Updating run details for run" + sd.getRun().getRunName() );

            sd.getDbRequestProcessor().updateRun( sd.getRun(), run );

            updateSessionDataRunPojo( sd, run );

            return Response.ok().build();
        } catch( Exception e ) {
            return returnError( e, "Unable to update run details" );
        }
    }

    @POST
    @Path("endTestcase")
    @ApiOperation(value = "End testcase", notes = "")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful ending testcase"),
                            @ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response endTestcase(
                                 @Context HttpServletRequest request,
                                 @ApiParam(value = "Testcase result", required = true ) TestcaseResultPojo testcaseResult) {

        try {
            validateTestResult( testcaseResult );

            SessionData sd = getSessionData( testcaseResult.getSessionId() );

            TestcasePojo testcase = sd.getRun().getSuite().getTestcase();
            logInfo( request, "Ending testcase " + testcase.getTestcaseName() );

            sd.getDbRequestProcessor().endTestcase( sd.getRun(), testcase, testcaseResult );
            return Response.ok().build();
        } catch( Exception e ) {
            return returnError( e, "Unable to end testcase" );
        }
    }


    @POST
    @Path("endSuite")
    @ApiOperation(value = "End suite", notes = "")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful ending suite"),
                            @ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response endSuite(
                              @Context HttpServletRequest request,
                              @ApiParam(value = "session data", required = true ) BasePojo baseData) {

        try {
            SessionData sd = getSessionData( baseData.getSessionId() );

            logInfo( request, "Ending suite " + sd.getRun().getSuite().getSuiteName() );

            sd.getDbRequestProcessor().endSuite( sd.getRun() );
            return Response.ok().build();
        } catch( Exception e ) {
            return returnError( e, "Unable to end suite" );
        }
    }

    @POST
    @Path("endRun")
    @ApiOperation(value = "End run", notes = "")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful ending run"),
                            @ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response endRun(
                            @Context HttpServletRequest request,
                            @ApiParam(value = "session data", required = true ) BasePojo baseData) {

        try {
            SessionData sd = getSessionData( baseData.getSessionId() );

            logInfo( request, "Ending run " + sd.getRun().getRunName() );

            sd.getDbRequestProcessor().endRun( sd.getRun() );

            // discard the session for the this RUN
            sessions.remove( baseData.getSessionId() );
            return Response.ok().build();
        } catch( Exception e ) {
            return returnError( e, "Unable to end run" );
        }
    }

    private SessionData getSessionData(
                                        String sessionId ) throws InvalidSessionException {

        if( !sessions.containsKey( sessionId ) ) {
            throw new InvalidSessionException( "Invalid Session ID '" + sessionId + "'" );
        }

        SessionData sd = sessions.get( sessionId );
        sd.updateLastUsedFlag();
        return sd;
    }

    private void validateTestResult(
                                     TestcaseResultPojo testcaseResult ) {

        TestResult testResult;

        // validate the test result/status
        try {
            testResult = TestResult.valueOf( testcaseResult.getResult().toUpperCase() );
        } catch( Exception e ) {
            throw new IllegalArgumentException( "Ivalid test result \"" + testcaseResult.getResult()
                                                + "\". Must be one of the following "
                                                + Arrays.asList( TestResult.values() ) );
        }

        testcaseResult.setTestResult( testResult );
    }

    private void cleanupExpiredSessions() {

        for( String sessionId : sessions.keySet() ) {
            SessionData sd = sessions.get( sessionId );
            long lastTimeUsed = sessions.get( sessionId ).getLastUsedFlag();
            if( new Date().getTime() - lastTimeUsed > SESSION_MAX_IDLE_TIME ) {
                StringBuilder sb = new StringBuilder();
                sb.append( "The following session is discarded as it has not been used for the last " );
                sb.append( SESSION_MAX_IDLE_TIME / 1000 );
                sb.append( " seconds:" );
                sb.append( "\nSession unique ID: " );
                sb.append( sessionId );
                sb.append( "\nRun DB ID: " );
                sb.append( sd.getRun().getRunId() );
                sb.append( "\nRun name: " );
                sb.append( sd.getRun().getRunName() );
                log.warn( sb.toString() );
            }
        }
    }
    
    private void updateSessionDataRunPojo( SessionData sessionData, RunPojo updatedRun ) {

        RunPojo oldRun = sessionData.getRun();

        if( !StringUtils.isNullOrEmpty( updatedRun.getRunName() ) ) {
            oldRun.setRunName( updatedRun.getRunName() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getOsName() ) ) {
            oldRun.setOsName( updatedRun.getOsName() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getProductName() ) ) {
            oldRun.setProductName( updatedRun.getProductName() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getVersionName() ) ) {
            oldRun.setVersionName( updatedRun.getVersionName() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getBuildName() ) ) {
            oldRun.setBuildName( updatedRun.getBuildName() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getHostName() ) ) {
            oldRun.setHostName( updatedRun.getHostName() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getDbHost() ) ) {
            oldRun.setDbHost( updatedRun.getDbHost() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getDbName() ) ) {
            oldRun.setDbName( updatedRun.getDbName() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getDbUser() ) ) {
            oldRun.setDbUser( updatedRun.getDbUser() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getDbPassword() ) ) {
            oldRun.setDbPassword( updatedRun.getDbPassword() );
        }

        if( !StringUtils.isNullOrEmpty( updatedRun.getUserNote() ) ) {
            oldRun.setUserNote( updatedRun.getUserNote() );
        }

        sessionData.setRun( oldRun );
    }
}
