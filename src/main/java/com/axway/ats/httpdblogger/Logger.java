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
import com.axway.ats.httpdblogger.model.SessionData;
import com.axway.ats.httpdblogger.model.TestResult;
import com.axway.ats.httpdblogger.model.pojo.BasePojo;
import com.axway.ats.httpdblogger.model.pojo.MessagePojo;
import com.axway.ats.httpdblogger.model.pojo.ResponseStartRunPojo;
import com.axway.ats.httpdblogger.model.pojo.ResponseStartSuitePojo;
import com.axway.ats.httpdblogger.model.pojo.ResponseStartTestcasePojo;
import com.axway.ats.httpdblogger.model.pojo.RunMetainfoPojo;
import com.axway.ats.httpdblogger.model.pojo.RunPojo;
import com.axway.ats.httpdblogger.model.pojo.ScenarioMetainfoPojo;
import com.axway.ats.httpdblogger.model.pojo.SuitePojo;
import com.axway.ats.httpdblogger.model.pojo.TestcasePojo;
import com.axway.ats.httpdblogger.model.pojo.TestcaseResultPojo;
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
                              @ApiParam(value = "Run details", required = true) RunPojo run ) {

        HttpSession httpSession = getHttpSession( request, run.getSessionId() );
        SessionData sd = ( SessionData ) getSessionData( httpSession );

        //start the new RUN
        logInfo( request,
                 "Starting run " + run.getRunName() + " with sessionId='" + httpSession.getId() + "'" );

        try {
            sd.getDbRequestProcessor().startRun( run );
            sd.setRun( run );
            // update httpSession's SessionData
            httpSession.setAttribute( SESSION_DATA_ATTRIB_NAME, sd );
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
                                @ApiParam(value = "Suite details", required = true) SuitePojo suite ) {

        logInfo( request, "Starting suite " + suite.getSuiteName() );

        try {
            HttpSession httpSession = getHttpSession( request, suite.getSessionId() );
            SessionData sd = ( SessionData ) getSessionData( httpSession );

            sd.getDbRequestProcessor().startSuite( sd.getRun(), suite );
            // update httpSession's SessionData
            httpSession.setAttribute( SESSION_DATA_ATTRIB_NAME, sd );
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
                                   @ApiParam(value = "Testcase details", required = true) TestcasePojo testcase ) {

        logInfo( request, "Starting testcase " + testcase.getTestcaseName() );

        try {
            HttpSession httpSession = getHttpSession( request, testcase.getSessionId() );
            SessionData sd = ( SessionData ) getSessionData( httpSession );

            sd.getDbRequestProcessor().startTestcase( sd.getRun(), testcase );
            // update httpSession's SessionData
            httpSession.setAttribute( SESSION_DATA_ATTRIB_NAME, sd );
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
                                   @ApiParam(value = "Message details", required = true) MessagePojo message ) {

        try {
            HttpSession httpSession = getHttpSession( request, message.getSessionId() );
            SessionData sd = ( SessionData ) getSessionData( httpSession );

            if( sd.getDbRequestProcessor().getState() == LifeCycleState.RUN_STARTED ) {

                logInfo( request, "Inserting message for run" + sd.getRun().getRunName() );
                sd.getDbRequestProcessor().insertRunMessage( sd.getRun(), message );

            } else if( sd.getDbRequestProcessor().getState() == LifeCycleState.SUITE_STARTED ) {

                logInfo( request, "Inserting message for suite" + sd.getRun().getSuite().getSuiteName() );
                sd.getDbRequestProcessor().insertSuiteMessage( sd.getRun(), message );

            } else if( sd.getDbRequestProcessor().getState() == LifeCycleState.TEST_CASE_STARTED ) {

                logInfo( request,
                         "Inserting message for testcase"
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
    public Response addRunMetainfo(
                                    @Context HttpServletRequest request,
                                    @ApiParam(value = "Run metainfo details", required = true) RunMetainfoPojo runMetainfo ) {

        try {
            HttpSession httpSession = getHttpSession( request, runMetainfo.getSessionId() );
            SessionData sd = ( SessionData ) getSessionData( httpSession );

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
    public Response addScenarioMetainfo(
                                         @Context HttpServletRequest request,
                                         @ApiParam(value = "Scenario metainfo details", required = true) ScenarioMetainfoPojo scenarioMetainfo ) {

        try {
            HttpSession httpSession = getHttpSession( request, scenarioMetainfo.getSessionId() );
            SessionData sd = ( SessionData ) getSessionData( httpSession );

            logInfo( request,
                     "Adding scenario metainfo for testcase"
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
    public Response updateRun(
                               @Context HttpServletRequest request,
                               @ApiParam(value = "New Run details", required = true) RunPojo run ) {

        try {
            HttpSession httpSession = getHttpSession( request, run.getSessionId() );
            SessionData sd = ( SessionData ) getSessionData( httpSession );

            logInfo( request, "Updating run details for run" + sd.getRun().getRunName() );

            sd.getDbRequestProcessor().updateRun( sd.getRun(), run );

            updateSessionDataRunPojo( sd, run );
            // update httpSession's SessionData
            httpSession.setAttribute( SESSION_DATA_ATTRIB_NAME, sd );
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
                                 @ApiParam(value = "Testcase result", required = true) TestcaseResultPojo testcaseResult ) {

        try {
            validateTestResult( testcaseResult );

            HttpSession httpSession = getHttpSession( request, testcaseResult.getSessionId() );
            SessionData sd = ( SessionData ) getSessionData( httpSession );

            TestcasePojo testcase = sd.getRun().getSuite().getTestcase();
            logInfo( request, "Ending testcase " + testcase.getTestcaseName() );

            sd.getDbRequestProcessor().endTestcase( sd.getRun(), testcase, testcaseResult );
            // update httpSession's SessionData
            httpSession.setAttribute( SESSION_DATA_ATTRIB_NAME, sd );
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
                              @ApiParam(value = "session data", required = true) BasePojo baseData ) {

        try {
            HttpSession httpSession = getHttpSession( request, baseData.getSessionId() );
            SessionData sd = ( SessionData ) getSessionData( httpSession );

            /*  
             * save the provided timestamp in the current suite pojo for this session,
             * so we can use it later when determining the timestamp for logging endSuite event
             */
            sd.getRun().getSuite().setTimestamp( baseData.getTimestamp() );

            logInfo( request, "Ending suite " + sd.getRun().getSuite().getSuiteName() );

            sd.getDbRequestProcessor().endSuite( sd.getRun() );
            // update httpSession's SessionData
            httpSession.setAttribute( SESSION_DATA_ATTRIB_NAME, sd );
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
                            @ApiParam(value = "session data", required = true) BasePojo baseData ) {

        try {
            HttpSession httpSession = getHttpSession( request, baseData.getSessionId() );
            SessionData sd = ( SessionData ) getSessionData( httpSession );

            /* 
             * save the provided timestamp in the current run pojo for this session,
             * so we can use it later when determining the timestamp for logging endRun event
             */
            sd.getRun().setTimestamp( baseData.getTimestamp() );

            logInfo( request, "Ending run " + sd.getRun().getRunName() );
            
            sd.getDbRequestProcessor().endRun( sd.getRun() );
            // remove session here
            return Response.ok().build();
        } catch( Exception e ) {
            return returnError( e, "Unable to end run" );
        }
    }

    private HttpSession getHttpSession(
                                        HttpServletRequest request,
                                        String sessionId ) {

        HttpSession httpSession = request.getSession( false );

        // check if valid session is obtained
        if( httpSession == null ) {

            // try getting httpSession by sessionId
            httpSession = getHttpSessionById( request, sessionId );

            if( httpSession == null ) {

                // no sessions was obtained, so create new one
                httpSession = request.getSession( true );
            }

        }

        return httpSession;

    }

    private HttpSession getHttpSessionById(
                                            HttpServletRequest request,
                                            String sessionId ) {

        // we need the context, so we create a session just to get it
        ServletContext context = request.getSession().getServletContext();

        if( StringUtils.isNullOrEmpty( sessionId ) ) {
            return null;
        }

        return ( HttpSession ) context.getAttribute( sessionId );
    }

    private SessionData getSessionData(
                                        HttpSession httpSession ) {

        SessionData sd = ( SessionData ) httpSession.getAttribute( SESSION_DATA_ATTRIB_NAME );

        if( sd == null ) {

            // the current session does NOT have a SessionData attribute
            sd = new SessionData();
        }

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

    private void updateSessionDataRunPojo(
                                           SessionData sessionData,
                                           RunPojo updatedRun ) {

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
