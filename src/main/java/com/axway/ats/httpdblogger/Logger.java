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
package com.axway.ats.httpdblogger;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.httpdblogger.exceptions.NoSessionIdException;
import com.axway.ats.httpdblogger.exceptions.UnknownSessionException;
import com.axway.ats.httpdblogger.exceptions.UnknownSuiteException;
import com.axway.ats.httpdblogger.model.SessionData;
import com.axway.ats.httpdblogger.model.TestResult;
import com.axway.ats.httpdblogger.model.pojo.request.AddRunMetainfoPojo;
import com.axway.ats.httpdblogger.model.pojo.request.AddScenarioMetainfoPojo;
import com.axway.ats.httpdblogger.model.pojo.request.EndRunPojo;
import com.axway.ats.httpdblogger.model.pojo.request.EndSuitePojo;
import com.axway.ats.httpdblogger.model.pojo.request.EndTestcasePojo;
import com.axway.ats.httpdblogger.model.pojo.request.InsertMessagePojo;
import com.axway.ats.httpdblogger.model.pojo.request.StartRunPojo;
import com.axway.ats.httpdblogger.model.pojo.request.StartSuitePojo;
import com.axway.ats.httpdblogger.model.pojo.request.StartTestcasePojo;
import com.axway.ats.httpdblogger.model.pojo.request.UpdateRunPojo;
import com.axway.ats.httpdblogger.model.pojo.response.ResponseStartRunPojo;
import com.axway.ats.httpdblogger.model.pojo.response.ResponseStartSuitePojo;
import com.axway.ats.httpdblogger.model.pojo.response.ResponseStartTestcasePojo;
import com.axway.ats.log.autodb.LifeCycleState;
import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;
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

    @Context
    private ServletContext     servletContext;

    public static final String SESSION_DATA_ATTRIB_NAME = "sessionData";

    @POST
    @Path("startRun")
    @ApiOperation(value = "Start run", notes = "", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful run start. Returning the session ID", response = Response.class),
                            @ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startRun(
                              @Context HttpServletRequest request,
                              @ApiParam(value = "Run details", required = true) StartRunPojo run ) {

        // clear provided sessionId, since we want to always start a new session on this REST call
        if( !StringUtils.isNullOrEmpty( run.getSessionId() ) ) {
            logInfo( "SessionId, provided by the request, will be ignored." );
            run.setSessionId( null );
        }

        HttpSession httpSession = getHttpSession( request, run.getSessionId(), true );
        SessionData sd = ( SessionData ) getSessionData( httpSession, true );

        //start the new RUN
        logInfo( request,
                 "Starting run " + run.getRunName() + " with sessionId='" + httpSession.getId() + "'" );

        try {
            sd.getDbRequestProcessor().startRun( run );
            sd.setRun( run );
            return Response.ok( new ResponseStartRunPojo( httpSession.getId(), run.getRunId() ) ).build();
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
                                @ApiParam(value = "Suite details", required = true) StartSuitePojo suite ) {

        if( StringUtils.isNullOrEmpty( suite.getSessionId() ) ) {
            return returnError( new NoSessionIdException( "Session ID not found in the request." ),
                                "Unable to start suite." );
        }

        HttpSession httpSession = getHttpSession( request, suite.getSessionId(), false );
        SessionData sd = ( SessionData ) getSessionData( httpSession, false );

        boolean setAsCurrentSuite = suite.getRunId() == -1;

        logInfo( request, "Starting suite " + suite.getSuiteName() );

        try {

            if( httpSession == null ) {
                throw new UnknownSessionException( "Could not obtain session with id '" + suite.getSessionId()
                                                   + "'" );
            }

            int suiteId = sd.getDbRequestProcessor().startSuite( sd, suite, setAsCurrentSuite );

            /* set the started suite as a current suite to the current run, so we can ensure, 
             * that if the user does not specify a suite id, 
             * we will use this suite
            */
            if( setAsCurrentSuite ) {
                suite.setSuiteId( suiteId );
                sd.getRun().setSuite( suite );
            }
            // add the suite id to already known suiteIds
            sd.addSuiteId( suiteId );

            return Response.ok( new ResponseStartSuitePojo( suiteId ) ).build();
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
                                   @ApiParam(value = "Testcase details", required = true) StartTestcasePojo testcase ) {

        if( StringUtils.isNullOrEmpty( testcase.getSessionId() ) ) {
            return returnError( new NoSessionIdException( "Session ID not found in the request." ),
                                "Unable to start testcase." );
        }

        HttpSession httpSession = getHttpSession( request, testcase.getSessionId(), false );
        SessionData sd = ( SessionData ) getSessionData( httpSession, false );

        boolean setAsCurrentTestcase = testcase.getSuiteId() == -1;

        logInfo( request, "Starting testcase " + testcase.getTestcaseName() );

        try {

            if( httpSession == null ) {
                throw new UnknownSessionException( "Could not obtain session with id '"
                                                   + testcase.getSessionId() + "'" );
            }

            int testcaseId = sd.getDbRequestProcessor().startTestcase( sd, testcase, setAsCurrentTestcase );

            /* set the started testcase as a current testcase to the current suite, so we can ensure, 
             * that if the user does not specify a testcase id, 
             * we will use this suite
            */
            if( setAsCurrentTestcase ) {
                testcase.setTestcaseId( testcaseId );
                sd.getRun().getSuite().setTestcase( testcase );
            }

            // add the testcase id to already known testcaseIds
            sd.addTestcaseId( testcaseId );

            return Response.ok( new ResponseStartTestcasePojo( testcaseId ) ).build();
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
                                   @ApiParam(value = "Message details", required = true) InsertMessagePojo message ) {

        if( StringUtils.isNullOrEmpty( message.getSessionId() ) ) {
            return returnError( new NoSessionIdException( "Session ID not found in the request." ),
                                "Unable to insert message." );
        }

        try {
            HttpSession httpSession = getHttpSession( request, message.getSessionId(), false );
            SessionData sd = ( SessionData ) getSessionData( httpSession, false );

            if( message.getTestcaseId() != -1 || message.getSuiteId() != -1 || message.getRunId() != -1 ) {
                // use the provided run/suite/testcase id to choose where to insert the message
                // testcaseId has the highest priority, followed by suiteId and runId
                insertMessageUsingRequestData( request, sd, message );
            } else {
                // use the SessionData's DbEventRequestProcessor's lifecycle state to choose where to insert the message
                insertMessageUsingCurrentSessionState( request, sd, message );
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
    public Response addRunMetainfo(
                                    @Context HttpServletRequest request,
                                    @ApiParam(value = "Run metainfo details", required = true) AddRunMetainfoPojo runMetainfo ) {

        if( StringUtils.isNullOrEmpty( runMetainfo.getSessionId() ) ) {
            return returnError( new NoSessionIdException( "Session ID not found in the request." ),
                                "Unable to add run metainfo." );
        }

        HttpSession httpSession = getHttpSession( request, runMetainfo.getSessionId(), false );
        SessionData sd = ( SessionData ) getSessionData( httpSession, false );

        logInfo( request, "Adding run metainfo for run " + sd.getRun().getRunName() );

        try {
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
    public Response addScenarioMetainfo(
                                         @Context HttpServletRequest request,
                                         @ApiParam(value = "Scenario metainfo details", required = true) AddScenarioMetainfoPojo scenarioMetainfo ) {

        if( StringUtils.isNullOrEmpty( scenarioMetainfo.getSessionId() ) ) {
            return returnError( new NoSessionIdException( "Session ID not found in the request." ),
                                "Unable to add scenario metainfo." );
        }

        HttpSession httpSession = getHttpSession( request, scenarioMetainfo.getSessionId(), false );
        SessionData sd = ( SessionData ) getSessionData( httpSession, false );

        boolean addScenarioMetaInfoToCurrentTestcase = scenarioMetainfo.getTestcaseId() == -1;

        if( addScenarioMetaInfoToCurrentTestcase ) {
            logInfo( request,
                     "Adding scenario metainfo for testcase "
                              + sd.getRun().getSuite().getTestcase().getTestcaseName() );
        } else {
            logInfo( request,
                     "Adding scenario metainfo for testcase with id '" + scenarioMetainfo.getTestcaseId()
                              + "'" );
        }

        try {
            sd.getDbRequestProcessor()
              .addScenarioMetainfo( sd, scenarioMetainfo, addScenarioMetaInfoToCurrentTestcase );
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
    public Response updateRun(
                               @Context HttpServletRequest request,
                               @ApiParam(value = "New Run details", required = true) UpdateRunPojo run ) {

        if( StringUtils.isNullOrEmpty( run.getSessionId() ) ) {
            return returnError( new NoSessionIdException( "Session ID not found in the request." ),
                                "Unable to update run." );
        }

        try {
            HttpSession httpSession = getHttpSession( request, run.getSessionId() , false);
            SessionData sd = ( SessionData ) getSessionData( httpSession , false);

            logInfo( request, "Updating run details for run " + sd.getRun().getRunName() );

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
                                 @ApiParam(value = "End testcase details", required = true) EndTestcasePojo endTestcasePojo ) {

        if( StringUtils.isNullOrEmpty( endTestcasePojo.getSessionId() ) ) {
            return returnError( new NoSessionIdException( "Session ID not found in the request." ),
                                "Unable to end testcase." );
        }

        HttpSession httpSession = getHttpSession( request, endTestcasePojo.getSessionId() , false);
        SessionData sd = ( SessionData ) getSessionData( httpSession , false);

        validateTestResult( endTestcasePojo );

        boolean endCurrentTestcase = endTestcasePojo.getTestcaseId() == -1;

        StartTestcasePojo testcase = sd.getRun().getSuite().getTestcase();
        if( endCurrentTestcase ) {
            logInfo( request, "Ending testcase " + testcase.getTestcaseName() );
        } else {
            logInfo( request, "Ending testcase with id '" + endTestcasePojo.getTestcaseId() + "'" );
        }

        try {
            sd.getDbRequestProcessor().endTestcase( sd,
                                                    testcase,
                                                    endTestcasePojo,
                                                    endCurrentTestcase );
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
                              @ApiParam(value = "End suite details", required = true) EndSuitePojo endSuitePojo ) {

        if( StringUtils.isNullOrEmpty( endSuitePojo.getSessionId() ) ) {
            return returnError( new NoSessionIdException( "Session ID not found in the request." ),
                                "Unable to end suite." );
        }

        HttpSession httpSession = getHttpSession( request, endSuitePojo.getSessionId() , false);
        SessionData sd = ( SessionData ) getSessionData( httpSession , false);

        boolean endCurrentSuite = endSuitePojo.getSuiteId() == -1;

        if( endCurrentSuite ) {
            logInfo( request, "Ending suite " + sd.getRun().getSuite().getSuiteName() );
        } else {
            logInfo( request, "Ending suite with id '" + endSuitePojo.getSuiteId() + "'" );
        }

        try {
            sd.getDbRequestProcessor().endSuite( sd, endSuitePojo, endCurrentSuite );
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
                            @ApiParam(value = "End run details", required = true) EndRunPojo endRunPojo ) {

        if( StringUtils.isNullOrEmpty( endRunPojo.getSessionId() ) ) {
            return returnError( new NoSessionIdException( "Session ID not found in the request." ),
                                "Unable to end run." );
        }

        HttpSession httpSession = getHttpSession( request, endRunPojo.getSessionId() , false);
        SessionData sd = ( SessionData ) getSessionData( httpSession , false);

        try {
            logInfo( request, "Ending run " + sd.getRun().getRunName() );
            sd.getDbRequestProcessor().endRun( sd.getRun(), endRunPojo );
            return Response.ok().build();
        } catch( Exception e ) {
            return returnError( e, "Unable to end run." );
        }
    }

    private HttpSession getHttpSession(
                                        HttpServletRequest request,
                                        String sessionId,
                                        boolean createNewSession ) {

        HttpSession httpSession = request.getSession( false );

        // check if valid session is obtained
        if( httpSession == null ) {

            // try getting httpSession by sessionId
            httpSession = getHttpSessionById( sessionId );

            if( httpSession == null ) {
                // no sessions was obtained
                if( createNewSession ) {
                    // create new session
                    httpSession = request.getSession( true );
                }
            }

        }

        return httpSession;

    }

    private HttpSession getHttpSessionById(
                                            String sessionId ) {

        if( StringUtils.isNullOrEmpty( sessionId ) ) {
            return null;
        }

        return ( HttpSession ) servletContext.getAttribute( sessionId );
    }

    private SessionData getSessionData(
                                        HttpSession httpSession,
                                        boolean createNew ) {

        SessionData sd = ( SessionData ) httpSession.getAttribute( SESSION_DATA_ATTRIB_NAME );

        // the current session does NOT have a SessionData attribute
        if( sd == null ) {
            if( createNew ) {
                sd = new SessionData();

                // add the newly created SessionData to the current session
                httpSession.setAttribute( SESSION_DATA_ATTRIB_NAME, sd );
            }
        }

        return sd;

    }

    private void validateTestResult(
                                     EndTestcasePojo testcaseResult ) {

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

    private void updateSessionDataRunPojo(
                                           SessionData sessionData,
                                           UpdateRunPojo updatedRun ) {

        StartRunPojo oldRun = sessionData.getRun();

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

        if( !StringUtils.isNullOrEmpty( updatedRun.getUserNote() ) ) {
            oldRun.setUserNote( updatedRun.getUserNote() );
        }

        sessionData.setRun( oldRun );
    }

    private void insertMessageUsingCurrentSessionState(
                                                        HttpServletRequest request,
                                                        SessionData sd,
                                                        InsertMessagePojo message ) throws DatabaseAccessException {

        if( sd.getDbRequestProcessor().getState() == LifeCycleState.RUN_STARTED ) {

            logInfo( request, "Inserting message for run " + sd.getRun().getRunName() );
            sd.getDbRequestProcessor().insertRunMessage( sd, message, false );

        } else if( sd.getDbRequestProcessor().getState() == LifeCycleState.SUITE_STARTED ) {

            logInfo( request, "Inserting message for suite " + sd.getRun().getSuite().getSuiteName() );
            sd.getDbRequestProcessor().insertSuiteMessage( sd, message, false );

        } else if( sd.getDbRequestProcessor().getState() == LifeCycleState.TEST_CASE_STARTED ) {

            logInfo( request,
                     "Inserting message for testcase "
                              + sd.getRun().getSuite().getTestcase().getTestcaseName() );
            sd.getDbRequestProcessor().insertMessage( sd, message, false );

        } else {

            throw new IllegalStateException( "Unable to insert message, because no run, suite or testcase has been previously started." );

        }

    }

    private void insertMessageUsingRequestData(
                                                HttpServletRequest request,
                                                SessionData sd,
                                                InsertMessagePojo message ) throws DatabaseAccessException,
                                                                            UnknownSuiteException {

        if( message.getTestcaseId() != -1 ) {
            logInfo( request, "Inserting message for testcase with id '" + message.getTestcaseId() + "'" );
            sd.getDbRequestProcessor().insertMessage( sd, message, true );
        } else if( message.getSuiteId() != -1 ) {
            logInfo( request, "Inserting message for suite with id '" + message.getSuiteId() + "'" );
            sd.getDbRequestProcessor().insertSuiteMessage( sd, message, true );
        } else {
            logInfo( request, "Inserting message for run with id" + message.getRunId() + "'" );
            sd.getDbRequestProcessor().insertRunMessage( sd, message, true );
        }

    }

}
