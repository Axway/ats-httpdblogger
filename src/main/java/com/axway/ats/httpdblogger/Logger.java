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

import java.io.InputStream;
import java.util.Arrays;
import java.util.NoSuchElementException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.axway.ats.core.dbaccess.mssql.DbConnSQLServer;
import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.httpdblogger.exceptions.NoSessionIdException;
import com.axway.ats.httpdblogger.exceptions.UnknownSessionException;
import com.axway.ats.httpdblogger.model.SessionData;
import com.axway.ats.httpdblogger.model.TestResult;
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

	public static final String SESSION_DATA_ATTRIB_NAME = "sessionData";

	@Context
	private ServletContext servletContext;

	@POST
	@Path("startRun")
	@ApiOperation(value = "Start run", notes = "", position = 1)
    @ApiResponses( value = { @ApiResponse( code = 200, message = "Successful run start. Returning the session ID", response = Response.class),
			@ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startRun(@Context HttpServletRequest request,
			@ApiParam(value = "Run details", required = true) StartRunPojo run) {

        // clear provided sessionId, since we want to always start a new session on this REST call
		if (!StringUtils.isNullOrEmpty(run.getSessionId())) {
			logInfo("SessionId, provided by the request, will be ignored.");
			run.setSessionId(null);
		}

        // for backward compatibility, if the db port is not provided, fall-back to the default one for MSSQL
		if (StringUtils.isNullOrEmpty(run.getDbPort())) {
			logInfo("Database port is not provided. We will use the default one for MSSQL ("
					+ DbConnSQLServer.DEFAULT_PORT + ")");
			run.setDbPort(String.valueOf(DbConnSQLServer.DEFAULT_PORT));
		}

		if (!StringUtils.isNullOrEmpty(run.getParentType())) {
			logInfo("Parent type specified in request to start a run. This field will be ignored.");
		}

		if (run.getParentId() != -1) {
			logInfo("Parent ID specified in request to start a run. This field will be ignored.");
		}

		try {

			HttpSession httpSession = getHttpSession(request, run.getSessionId(), true);
			SessionData sd = (SessionData) getSessionData(httpSession, true);

			// start the new RUN
            logInfo(request,
                    "Starting run " + run.getRunName() + " with sessionId='" + httpSession.getId() + "'");

			sd.getDbRequestProcessor().startRun(run);
			sd.setRun(run);
			return Response.ok(new ResponseStartRunPojo(httpSession.getId(), run.getRunId())).build();
		} catch (DatabaseAccessException e) {
			return returnError(e, "Unable to start run");
		}
	}

	@POST
	@Path("startSuite")
	@ApiOperation(value = "Start suite", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful starting suite"),
			@ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startSuite(@Context HttpServletRequest request,
			@ApiParam(value = "Suite details", required = true) StartSuitePojo suite) {

		if (StringUtils.isNullOrEmpty(suite.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to start suite.");
		}

		try {

			HttpSession httpSession = getHttpSession(request, suite.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			if (!StringUtils.isNullOrEmpty(suite.getParentType())) {
				logInfo("Parent type specified in request to start a suite. This field will be ignored.");
			}

			boolean setAsCurrentSuite = suite.getParentId() == -1;

			logInfo(request, "Starting suite " + suite.getSuiteName());

			int suiteId = sd.getDbRequestProcessor().startSuite(sd, suite, setAsCurrentSuite);

            /* set the started suite as a current suite to the current run, so we can ensure, 
             * that if the user does not specify a suite id, 
             * we will use this suite
			 */
			if (setAsCurrentSuite) {
				suite.setSuiteId(suiteId);
				sd.getRun().setSuite(suite);
			}
			// add the suite to already known suites
			sd.addSuiteId(suiteId, suite.getSuiteName());

			return Response.ok(new ResponseStartSuitePojo(suiteId)).build();
		} catch (Exception e) {
			return returnError(e, "Unable to start suite");
		}
	}

	@POST
	@Path("startTestcase")
	@ApiOperation(value = "Start testcase", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful starting testcase"),
			@ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response
            startTestcase( @Context HttpServletRequest request,
			@ApiParam(value = "Testcase details", required = true) StartTestcasePojo testcase) {

		if (StringUtils.isNullOrEmpty(testcase.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to start testcase.");
		}

		if (!StringUtils.isNullOrEmpty(testcase.getParentType())) {
			logInfo("Parent type specified in request to start a testcase. This field will be ignored.");
		}

		try {

			HttpSession httpSession = getHttpSession(request, testcase.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			boolean setAsCurrentTestcase = testcase.getParentId() == -1;

			logInfo(request, "Starting testcase " + testcase.getTestcaseName());

			int testcaseId = sd.getDbRequestProcessor().startTestcase(sd, testcase, setAsCurrentTestcase);

            /* set the started testcase as a current testcase to the current suite, so we can ensure, 
             * that if the user does not specify a suite id, 
             * we will use this suite
			 */
			if (setAsCurrentTestcase) {
				testcase.setTestcaseId(testcaseId);
				sd.getRun().getSuite().setTestcase(testcase);
			}

			// add the testcase id to already known testcaseIds
			sd.addTestcaseId(testcaseId, testcase.getTestcaseName());

			return Response.ok(new ResponseStartTestcasePojo(testcaseId)).build();
		} catch (Exception e) {
			return returnError(e, "Unable to start testcase");
		}
	}

	@POST
	@Path("insertMessage")
	@ApiOperation(value = "Insert Message", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful inserted message"),
			@ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response
            insertMessage( @Context HttpServletRequest request,
			@ApiParam(value = "Message details", required = true) InsertMessagePojo message) {

		if (StringUtils.isNullOrEmpty(message.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to insert message.");
		}

		// escape characters with ASCII code < 32
		message.setMessage(StringUtils.escapeNonPrintableAsciiCharacters(message.getMessage()));

		try {
			HttpSession httpSession = getHttpSession(request, message.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			if (StringUtils.isNullOrEmpty(message.getParentType())) {
                throw new IllegalArgumentException("Parent type does not specified for message with Parent ID = "
                                                   + message.getParentId()
								+ ". Message with missing Parent type could not be logged to database.");
			}

			if (message.getParentType().equalsIgnoreCase("TESTCASE")) {
				logInfo(request, "Inserting message for testcase with id '" + message.getParentId() + "'");
				sd.getDbRequestProcessor().insertMessage(sd, message, message.getParentId() != -1);
			} else if (message.getParentType().equalsIgnoreCase("SUITE")) {
				logInfo(request, "Inserting message for suite with id '" + message.getParentId() + "'");
				sd.getDbRequestProcessor().insertSuiteMessage(sd, message, message.getParentId() != -1);
			} else if (message.getParentType().equalsIgnoreCase("RUN")) {
				logInfo(request, "Inserting message for run with id " + message.getParentId() + "'");
				sd.getDbRequestProcessor().insertRunMessage(sd, message, message.getParentId() != -1);
			} else {
				throw new IllegalArgumentException("'" + message.getParentType()
						+ "' is not a valid Parent type value. Supported values are 'RUN', 'SUITE' and 'TESTCASE'");
			}

			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to insert message");
		}
	}

	@POST
	@Path("insertMessages")
	@ApiOperation(value = "Insert Messages", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful inserted messages"),
			@ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response
            insertMessages( @Context HttpServletRequest request,
			@ApiParam(value = "List of Messages details", required = true) InsertMessagesPojo messages) {

		if (StringUtils.isNullOrEmpty(messages.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to insert messages.");
		}

		try {
			HttpSession httpSession = getHttpSession(request, messages.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			if (StringUtils.isNullOrEmpty(messages.getParentType())) {
                throw new NoSuchElementException("Parent type does not specified for messages with Parent ID = "
                                                 + messages.getParentId()
								+ ". Message with missing Parent type could not be logged to database.");
			}

			boolean skipLifeCycleStateCheck = messages.getParentId() != -1;

			if (messages.getParentType().equalsIgnoreCase("TESTCASE")) {
				sd.getDbRequestProcessor().insertMessages(sd, messages, skipLifeCycleStateCheck);
			} else if (messages.getParentType().equalsIgnoreCase("SUITE")) {
				sd.getDbRequestProcessor().insertSuiteMessages(sd, messages, skipLifeCycleStateCheck);
			} else if (messages.getParentType().equalsIgnoreCase("RUN")) {
				sd.getDbRequestProcessor().insertRunMessages(sd, messages, skipLifeCycleStateCheck);
			} else {
				throw new IllegalArgumentException("'" + messages.getParentType()
						+ "' is not a valid Parent type value. Supported values are 'RUN', 'SUITE' and 'TESTCASE'");
			}

			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to insert message");
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
			@ApiParam(value = "Run metainfo details", required = true) AddRunMetainfoPojo runMetainfo) {

		if (StringUtils.isNullOrEmpty(runMetainfo.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to add run metainfo.");
		}

		if (!StringUtils.isNullOrEmpty(runMetainfo.getParentType())) {
			logInfo("Parent type specified in request to add a run metainfo. This field will be ignored.");
		}

		try {

			HttpSession httpSession = getHttpSession(request, runMetainfo.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			logInfo(request, "Adding run metainfo for run " + sd.getRun().getRunName());

			sd.getDbRequestProcessor().addRunMetainfo(sd.getRun(), runMetainfo);
			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to add run metainfo");
		}
	}

	@POST
	@Path("addScenarioMetainfo")
	@ApiOperation(value = "Add ScenarioMetainfo", notes = "")
    @ApiResponses( { @com.wordnik.swagger.annotations.ApiResponse( code = 200, message = "Successful added scenario metainfo"),
			@com.wordnik.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
    public Response
            addScenarioMetainfo( @Context HttpServletRequest request,
			@ApiParam(value = "Scenario metainfo details", required = true) AddScenarioMetainfoPojo scenarioMetainfo) {

		if (StringUtils.isNullOrEmpty(scenarioMetainfo.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to add scenario metainfo.");
		}

		if (!StringUtils.isNullOrEmpty(scenarioMetainfo.getParentType())) {
			logInfo("Parent type specified in request to add a scenario metainfo. This field will be ignored.");
		}

		try {

			HttpSession httpSession = getHttpSession(request, scenarioMetainfo.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			boolean addScenarioMetaInfoToCurrentTestcase = scenarioMetainfo.getParentId() == -1;

			if (addScenarioMetaInfoToCurrentTestcase) {
				logInfo(request, "Adding scenario metainfo for testcase "
						+ sd.getRun().getSuite().getTestcase().getTestcaseName());
			} else {
                logInfo(request, "Adding scenario metainfo for testcase with id '"
                                 + scenarioMetainfo.getParentId() + "'");
			}

            sd.getDbRequestProcessor().addScenarioMetainfo(sd, scenarioMetainfo,
                                                           addScenarioMetaInfoToCurrentTestcase);
			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to add scenario metainfo");
		}
	}
	
	@POST
    @Path("addTestcaseMetainfo")
    @ApiOperation(value = "Add Testcase Metainfo", notes = "")
    @ApiResponses( { @com.wordnik.swagger.annotations.ApiResponse( code = 200, message = "Successful added testcase metainfo"),
            @com.wordnik.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response
            addTestcaseMetainfo( @Context HttpServletRequest request,
            @ApiParam(value = "Testcase metainfo details", required = true) AddTestcaseMetainfoPojo testcaseMetainfo) {

        if (StringUtils.isNullOrEmpty(testcaseMetainfo.getSessionId())) {
            return returnError(new NoSessionIdException("Session ID not found in the request."),
                    "Unable to add testcase metainfo.");
        }

        if (!StringUtils.isNullOrEmpty(testcaseMetainfo.getParentType())) {
            logInfo("Parent type specified in request to add a testcase metainfo. This field will be ignored.");
        }

        try {

            HttpSession httpSession = getHttpSession(request, testcaseMetainfo.getSessionId(), false);
            SessionData sd = (SessionData) getSessionData(httpSession, false);

            boolean addTestcaseMetaInfoToCurrentTestcase = testcaseMetainfo.getParentId() == -1;

            if (addTestcaseMetaInfoToCurrentTestcase) {
                logInfo(request, "Adding testcase metainfo for testcase "
                        + sd.getRun().getSuite().getTestcase().getTestcaseName());
            } else {
                logInfo(request, "Adding testcase metainfo for testcase with id '"
                                 + testcaseMetainfo.getParentId() + "'");
            }

            sd.getDbRequestProcessor().addTestcaseMetainfo(sd, testcaseMetainfo,
                                                           addTestcaseMetaInfoToCurrentTestcase);
            return Response.ok().build();
        } catch (Exception e) {
            return returnError(e, "Unable to add testcase metainfo");
        }
    }

	@POST
	@Path("updateRun")
	@ApiOperation(value = "Update run", notes = "")
	@ApiResponses({ @com.wordnik.swagger.annotations.ApiResponse(code = 200, message = "Successful updating run"),
			@com.wordnik.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response updateRun(@Context HttpServletRequest request,
			@ApiParam(value = "New Run details", required = true) UpdateRunPojo run) {

		if (StringUtils.isNullOrEmpty(run.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to update run.");
		}

		if (!StringUtils.isNullOrEmpty(run.getParentType())) {
			logInfo("Parent type specified in request to update a run. This field will be ignored.");
		}

		if (run.getParentId() != -1) {
			logInfo("Parent ID specified in request to update a run. This field will be ignored.");
		}

		try {
			HttpSession httpSession = getHttpSession(request, run.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			logInfo(request, "Updating run details for run " + sd.getRun().getRunName());

			sd.getDbRequestProcessor().updateRun(sd.getRun(), run);

			updateSessionDataRunPojo(sd, run);
			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to update run details");
		}
	}

	@POST
	@Path("updateSuite")
	@ApiOperation(value = "Update suite", notes = "")
	@ApiResponses({ @com.wordnik.swagger.annotations.ApiResponse(code = 200, message = "Successful updating suite"),
			@com.wordnik.swagger.annotations.ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
    public Response
            updateSuite( @Context HttpServletRequest request,
			@ApiParam(value = "New Suite details", required = true) UpdateSuitePojo suite) {

		if (StringUtils.isNullOrEmpty(suite.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to update suite.");
		}

		if (!StringUtils.isNullOrEmpty(suite.getParentType())) {
			logInfo("Parent type specified in request to update a suite. This field will be ignored.");
		}

		if (suite.getParentId() != -1) {
			logInfo("Parent ID specified in request to update a sute. This field will be ignored.");
		}

		try {
			HttpSession httpSession = getHttpSession(request, suite.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			boolean updateCurrentSuite = suite.getSuiteId() == -1;

			String suiteName = null;
			int suiteId = -1;
			if (updateCurrentSuite) {
				/*
                 * Since it is possible to update closed/ended suite,
                 * and the current suite needs to be updated,
                 * check if there are current suite indeed
                 * */
				if (sd.getRun().getSuite() == null) {
                    throw new IllegalStateException("Cannot update SUITE" + " as the current state is "
                                                    + sd.getDbRequestProcessor().getState()
                                                    + ", but it is expected to be "
                                                    + LifeCycleState.ATLEAST_SUITE_STARTED);
				}
				suiteId = sd.getRun().getSuite().getSuiteId();
				suiteName = sd.getRun().getSuite().getSuiteName();
			} else {
				suiteId = suite.getSuiteId();
				suiteName = sd.getSuitesMap().get(suite.getSuiteId());
			}

			logInfo("Updating info for suite '" + suiteName + "'");

            // here reverse the value of updateCurrentSuite, because if current suite needs to be updated,
			// life cycle check must not be skipped
			sd.getDbRequestProcessor().updateSuite(sd, suiteId, suite, !updateCurrentSuite);

			sd.getSuitesMap().put(suiteId, suiteName);

			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to update suite details");
		}
	}

	@POST
	@Path("endTestcase")
	@ApiOperation(value = "End testcase", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful ending testcase"),
			@ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response
            endTestcase( @Context HttpServletRequest request,
			@ApiParam(value = "End testcase details", required = true) EndTestcasePojo endTestcasePojo) {

		if (StringUtils.isNullOrEmpty(endTestcasePojo.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to end testcase.");
		}

		if (!StringUtils.isNullOrEmpty(endTestcasePojo.getParentType())) {
			logInfo("Parent type specified in request to end a testcase. This field will be ignored.");
		}

		if (endTestcasePojo.getParentId() != -1) {
			logInfo("Parent ID specified in request to end a testcase. This field will be ignored.");
		}

		try {

			HttpSession httpSession = getHttpSession(request, endTestcasePojo.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			validateTestResult(endTestcasePojo);

			boolean endCurrentTestcase = endTestcasePojo.getTestcaseId() == -1;

			int testcaseId = -1;
			String testcaseName = null;
			if (endCurrentTestcase) {
				testcaseId = sd.getRun().getSuite().getTestcase().getTestcaseId();
				testcaseName = sd.getRun().getSuite().getTestcase().getTestcaseName();
				logInfo(request, "Ending testcase " + testcaseName);
			} else {
				testcaseId = endTestcasePojo.getTestcaseId();
				testcaseName = sd.getTestcasesMap().get(testcaseId);
				logInfo(request, "Ending testcase '" + testcaseName + "'");
			}

			sd.getDbRequestProcessor().endTestcase(sd, testcaseId, endTestcasePojo, endCurrentTestcase);
			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to end testcase");
		}
	}

	@POST
	@Path("endSuite")
	@ApiOperation(value = "End suite", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful ending suite"),
			@ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response
            endSuite( @Context HttpServletRequest request,
			@ApiParam(value = "End suite details", required = true) EndSuitePojo endSuitePojo) {

		if (StringUtils.isNullOrEmpty(endSuitePojo.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to end suite.");
		}

		if (!StringUtils.isNullOrEmpty(endSuitePojo.getParentType())) {
			logInfo("Parent type specified in request to end a suite. This field will be ignored.");
		}

		if (endSuitePojo.getParentId() != -1) {
			logInfo("Parent ID specified in request to end a suite. This field will be ignored.");
		}

		try {

			HttpSession httpSession = getHttpSession(request, endSuitePojo.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			boolean endCurrentSuite = endSuitePojo.getSuiteId() == -1;

			String suiteName = null;
			if (endCurrentSuite) {
				suiteName = sd.getRun().getSuite().getSuiteName();
				logInfo(request, "Ending suite " + suiteName);
			} else {
				suiteName = sd.getSuitesMap().get(endSuitePojo.getSuiteId());
				logInfo(request, "Ending suite " + suiteName);
			}

			sd.getDbRequestProcessor().endSuite(sd, endSuitePojo, endCurrentSuite);
			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to end suite");
		}
	}

	@POST
	@Path("endRun")
	@ApiOperation(value = "End run", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful ending run"),
			@ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response endRun(@Context HttpServletRequest request,
			@ApiParam(value = "End run details", required = true) EndRunPojo endRunPojo) {

		if (StringUtils.isNullOrEmpty(endRunPojo.getSessionId())) {
            return returnError(new NoSessionIdException("Session ID not found in the request."),
                               "Unable to end run.");
		}

		if (!StringUtils.isNullOrEmpty(endRunPojo.getParentType())) {
			logInfo("Parent type specified in request to end a run. This field will be ignored.");
		}

		if (endRunPojo.getParentId() != -1) {
			logInfo("Parent ID specified in request to end a run. This field will be ignored.");
		}

		try {

			HttpSession httpSession = getHttpSession(request, endRunPojo.getSessionId(), false);
			SessionData sd = (SessionData) getSessionData(httpSession, false);

			logInfo(request, "Ending run " + sd.getRun().getRunName());
			sd.getDbRequestProcessor().endRun(sd.getRun(), endRunPojo);
			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to end run.");
		}
	}

	@PUT
	@Path("attachFile")
	@ApiOperation(value = "Attach multipart file", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully attached file"),
			@ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces(MediaType.APPLICATION_JSON)
    public Response
            attachFile( @Context HttpServletRequest request,
			@ApiParam(value = "Attach file details", required = true) @FormDataParam("attach file details") AttachFilePojo attachFilePojo,
			@ApiParam(value = "File Input Stream", required = true) @FormDataParam("stream") InputStream inputStream) {

		if (StringUtils.isNullOrEmpty(attachFilePojo.getSessionId())) {
			return returnError(new NoSessionIdException("Session ID not found in the request."),
					"Unable to attach file.");
		}

		if (StringUtils.isNullOrEmpty(attachFilePojo.getFileName())) {
			return returnError(new NoSuchElementException("File name is empty."), "Unable to attach file");
		}

		if (!StringUtils.isNullOrEmpty(attachFilePojo.getParentType())) {
			logInfo("Parent type specified in request to attach a file. This field will be ignored.");
		}

		attachFilePojo.setInputStream(inputStream);

		String sessionId = attachFilePojo.getSessionId();

		HttpSession httpSession = getHttpSession(request, sessionId, false);
		SessionData sd = (SessionData) getSessionData(httpSession, false);

		boolean attachFileToCurrentTestcase = attachFilePojo.getParentId() == -1;

        int testcaseId = (attachFileToCurrentTestcase)
                                                       ? sd.getRun().getSuite().getTestcase().getTestcaseId()
				: attachFilePojo.getParentId();

		logInfo(request, "Attaching file '" + attachFilePojo.getFileName() + "' to testcase '" + testcaseId + "'");

		try {
			sd.getDbRequestProcessor().attachFile(sd, attachFilePojo, attachFileToCurrentTestcase, testcaseId);
			return Response.ok().build();
		} catch (Exception e) {
			return returnError(e, "Unable to attach file.");
		}
	}

    private HttpSession getHttpSession( HttpServletRequest request, String sessionId,
                                        boolean createNewSession ) {

		HttpSession httpSession = request.getSession(false);

		// check if valid session is obtained
		if (httpSession == null) {

			// try getting httpSession by sessionId
			httpSession = getHttpSessionById(sessionId);

			if (httpSession == null) {
				// no sessions was obtained
				if (createNewSession) {
					// create new session
					httpSession = request.getSession(true);
				}
			}
		}

		if (httpSession == null) {
			throw new UnknownSessionException("Could not obtain session with id '" + sessionId + "'");
		}

		return httpSession;

	}

	private HttpSession getHttpSessionById(String sessionId) {

		if (StringUtils.isNullOrEmpty(sessionId)) {
			return null;
		}

		return (HttpSession) servletContext.getAttribute(sessionId);
	}

	private SessionData getSessionData(HttpSession httpSession, boolean createNew) {

		SessionData sd = (SessionData) httpSession.getAttribute(SESSION_DATA_ATTRIB_NAME);

		// the current session does NOT have a SessionData attribute
		if (sd == null) {
			if (createNew) {
				sd = new SessionData();

				// add the newly created SessionData to the current session
				httpSession.setAttribute(SESSION_DATA_ATTRIB_NAME, sd);
			}
		}

		return sd;

	}

	private void validateTestResult(EndTestcasePojo testcaseResult) {

		TestResult testResult;

		// validate the test result/status
		try {
			testResult = TestResult.valueOf(testcaseResult.getResult().toUpperCase());
		} catch (Exception e) {
			throw new IllegalArgumentException("Ivalid test result \"" + testcaseResult.getResult()
                                               + "\". Must be one of the following "
                                               + Arrays.asList(TestResult.values()));
		}

		testcaseResult.setTestResult(testResult);
	}

	private void updateSessionDataRunPojo(SessionData sessionData, UpdateRunPojo updatedRun) {

		StartRunPojo oldRun = sessionData.getRun();

		if (!StringUtils.isNullOrEmpty(updatedRun.getRunName())) {
			oldRun.setRunName(updatedRun.getRunName());
		}

		if (!StringUtils.isNullOrEmpty(updatedRun.getOsName())) {
			oldRun.setOsName(updatedRun.getOsName());
		}

		if (!StringUtils.isNullOrEmpty(updatedRun.getProductName())) {
			oldRun.setProductName(updatedRun.getProductName());
		}

		if (!StringUtils.isNullOrEmpty(updatedRun.getVersionName())) {
			oldRun.setVersionName(updatedRun.getVersionName());
		}

		if (!StringUtils.isNullOrEmpty(updatedRun.getBuildName())) {
			oldRun.setBuildName(updatedRun.getBuildName());
		}

		if (!StringUtils.isNullOrEmpty(updatedRun.getHostName())) {
			oldRun.setHostName(updatedRun.getHostName());
		}

		if (!StringUtils.isNullOrEmpty(updatedRun.getUserNote())) {
			oldRun.setUserNote(updatedRun.getUserNote());
		}

		sessionData.setRun(oldRun);
	}
}
