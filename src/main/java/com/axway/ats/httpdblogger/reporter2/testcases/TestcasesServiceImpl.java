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
package com.axway.ats.httpdblogger.reporter2.testcases;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.axway.ats.core.reflect.ReflectionUtils;
import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.httpdblogger.reporter2.pojo.response.InternalServerErrorPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MessagePojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MessagesPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MetaInfoPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MetaInfosPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.exceptions.NoSuchTestcaseException;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.StatisticDescriptionPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.StatisticPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.StatisticsDescriptionsPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.TestcasePojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.TestcasesPojo;
import com.axway.ats.httpdblogger.reporter2.utils.DbConnectionManager;
import com.axway.ats.httpdblogger.reporter2.utils.JsonUtils;
import com.axway.ats.httpdblogger.reporter2.utils.PojoUtils;
import com.axway.ats.log.autodb.entities.Message;
import com.axway.ats.log.autodb.entities.Statistic;
import com.axway.ats.log.autodb.entities.StatisticDescription;
import com.axway.ats.log.autodb.entities.Testcase;
import com.axway.ats.log.autodb.entities.TestcaseMetainfo;
import com.axway.ats.log.autodb.model.IDbReadAccess;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path( "reporter2")
@Api( value = "/reporter2/testcase", description = "Retrieve Testcase(s) information")
public class TestcasesServiceImpl {

    private static final Logger LOG = Logger.getLogger(TestcasesServiceImpl.class);

    @GET
    @Path( "/testcases")
    @ApiOperation(
            value = "Get all testcases",
            notes = "Get all testcases",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained all testcases",
                    response = TestcasesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining all testcases. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getTestcases( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,

                          @ApiParam(
                                  name = "scenarioId",
                                  required = true) @QueryParam( "scenarioId") final int scenarioId,
                          @ApiParam(
                                  name = "from",
                                  required = false) @QueryParam( "from") final int from,
                          @ApiParam(
                                  name = "to",
                                  required = false) @QueryParam( "to") final int to,

                          @ApiParam(
                                  name = "fromDate",
                                  required = false,
                                  allowableValues = "Milliseconds since Epoch. Must be in UTC") @QueryParam( "fromDate") final long fromDate,
                          @ApiParam(
                                  name = "toDate",
                                  required = false,
                                  allowableValues = "Milliseconds since Epoch. Must be in UTC") @QueryParam( "toDate") final long toDate,
                          @Context HttpServletRequest request ) {

        boolean hasScenarioId = (request != null && request.getQueryString() != null
                                 && request.getQueryString().contains("scenarioId"));

        boolean hasFromDate = (request != null && request.getQueryString() != null
                               && request.getQueryString().contains("fromDate"));

        boolean hasToDate = (request != null && request.getQueryString() != null
                             && request.getQueryString().contains("toDate"));

        try {

            String whereClause = null;
            // validate input
            if (hasScenarioId) {
                if (scenarioId < 0) {
                    throw new RuntimeException("Scenario ID must be zero or positive!");
                }

                whereClause = "WHERE scenarioId = " + scenarioId;
            } else {
                whereClause = "WHERE 1=1";
            }

            if (from < 0) {
                throw new RuntimeException("from parameter must be zero or positive!");
            }

            if (to < 0) {
                throw new RuntimeException("to parameter must be zero or positive!");
            }

            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);

            int startRecord = from;
            int endRecord = (to == 0)
                                      ? readAccess.getTestcasesCount(whereClause)
                                      : to;

            if (hasFromDate) {
                whereClause += " AND dateStart >= " + "DATEADD(MILLISECOND, " + fromDate
                               + " % 1000, DATEADD(SECOND, " + fromDate + " / 1000, '19700101'))";
            }

            if (hasToDate) {
                whereClause += " AND dateStart <= " + "DATEADD(MILLISECOND, " + toDate
                               + " % 1000, DATEADD(SECOND, " + toDate + " / 1000, '19700101'))";
            }

            List<Testcase> testcases = readAccess.getTestcases(startRecord, endRecord,
                                                               whereClause, "testcaseId", true,
                                                               0);

            TestcasesPojo pojo = null;
            if (testcases == null || testcases.isEmpty()) {
                pojo = new TestcasesPojo();
                pojo.setTestcases(new TestcasePojo[]{});
            } else {
                pojo = (TestcasesPojo) PojoUtils.logEntityToPojo(testcases);
            }

            return Response.ok(pojo).build();
        } catch (Exception e) {
            String errorMessage = "Could not obtain all testcases, using connection ID '" + connectionId + "'";
            if (hasScenarioId) {
                errorMessage += " and scenario ID '" + scenarioId + "'";
            }
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }

    }

    @GET
    @Path( "/testcase/{testcaseId: [0-9]*}")
    @ApiOperation(
            value = "Get testcase details by providing testcaseId",
            notes = "Get testcase details by providing testcaseId",
            position = 2)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained testcase details",
                    response = TestcasePojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining testcase details. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getTestcase( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,

                         @ApiParam(
                                 name = "testcaseId",
                                 allowMultiple = false,
                                 required = true) @PathParam( "testcaseId") final int testcaseId,

                         @ApiParam(
                                 name = "properties",
                                 allowMultiple = true,
                                 value = "If you want to obtain only a subset of the testcase's properties, use this parameter",
                                 required = false) @QueryParam( "properties") final String properties ) {

        try {
            if (testcaseId < 0) {
                throw new IllegalArgumentException("Testcase ID must be equal or greater than zero");
            }
            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);
            List<Testcase> testcases = readAccess.getTestcases(0, 1, "WHERE testcaseId = " + testcaseId, "testcaseId",
                                                               false,
                                                               0);
            if (testcases == null || testcases.isEmpty()) {
                throw new NoSuchTestcaseException(testcaseId, connectionId);
            }
            TestcasePojo testcasePojo = (TestcasePojo) PojoUtils.logEntityToPojo(testcases.get(0));
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(testcasePojo).build();
            } else {
                String[] propsKeys = properties.replace(" ", "").split(",");
                Object[] propsValues = new Object[propsKeys.length];
                for (int i = 0; i < propsKeys.length; i++) {
                    try {
                        propsValues[i] = ReflectionUtils.getFieldValue(testcasePojo, propsKeys[i], false);
                    } catch (Exception e) {
                        if (e.getMessage().contains("Could not obtain field '" + propsKeys[i] + "' from class")) {
                            throw new IllegalArgumentException("" + propsKeys[i]
                                                               + " is not a valid testcase property.");
                        } else {
                            throw e;
                        }
                    }
                }
                String json = JsonUtils.constructJson(propsKeys, propsValues, true);
                return Response.ok(json).build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not obtain testcase details, by using ID '" + testcaseId
                                  + "' and connection ID '"
                                  + connectionId;
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    @GET
    @Path( "/testcase/{testcaseId: [0-9]*}/messages")
    @ApiOperation(
            value = "Get testcase messages by providing testcaseId",
            notes = "Get testcase messages by providing testcaseId",
            position = 3)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained testcase messages",
                    response = MessagesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining testcase messages. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getTestcaseMessages( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,

                                 @ApiParam(
                                         name = "testcaseId",
                                         allowMultiple = false,
                                         required = true) @PathParam( "testcaseId") final int testcaseId,
                                 @ApiParam(
                                         name = "from",
                                         required = false) @QueryParam( "from") final int from,
                                 @ApiParam(
                                         name = "to",
                                         required = false) @QueryParam( "to") final int to,

                                 @ApiParam(
                                         name = "fromDate",
                                         required = false,
                                         allowableValues = "Milliseconds since Epoch. Must be in UTC") @QueryParam( "fromDate") final long fromDate,
                                 @ApiParam(
                                         name = "toDate",
                                         required = false,
                                         allowableValues = "Milliseconds since Epoch. Must be in UTC") @QueryParam( "toDate") final long toDate,
                                 @Context HttpServletRequest request ) {

        boolean hasTestcaseId = (request != null && request.getQueryString() != null
                                 && request.getQueryString().contains("testcaseId"));

        boolean hasFromDate = (request != null && request.getQueryString() != null
                               && request.getQueryString().contains("fromDate"));

        boolean hasToDate = (request != null && request.getQueryString() != null
                             && request.getQueryString().contains("toDate"));
        try {

            String whereClause = null;
            if (testcaseId < 0) {
                throw new IllegalArgumentException("Testcase ID must be equal or greater than zero");
            }

            if (!hasTestcaseId) {
                throw new RuntimeException("Testcase ID must be provided");
            }

            whereClause = "WHERE testcaseId = " + testcaseId;

            // validate input
            if (from < 0) {
                throw new RuntimeException("from parameter must be zero or positive!");
            }

            if (to < 0) {
                throw new RuntimeException("to parameter must be zero or positive!");
            }

            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);

            int startRecord = from;
            int endRecord = (to == 0)
                                      ? readAccess.getMessagesCount(whereClause)
                                      : to;

            if (hasFromDate) {
                whereClause += " AND timestamp >= " + "DATEADD(MILLISECOND, " + fromDate
                               + " % 1000, DATEADD(SECOND, " + fromDate + " / 1000, '19700101'))";
            }

            if (hasToDate) {
                whereClause += " AND timestamp <= " + "DATEADD(MILLISECOND, " + toDate
                               + " % 1000, DATEADD(SECOND, " + toDate + " / 1000, '19700101'))";
            }

            List<Message> messages = readAccess.getMessages(startRecord, endRecord,
                                                            whereClause,
                                                            "messageId", true, 0);

            MessagesPojo messagesPojo = null;
            if (messages == null || messages.isEmpty()) {
                messagesPojo = new MessagesPojo();
                messagesPojo.setMessages(new MessagePojo[]{});
            } else {
                messagesPojo = (MessagesPojo) PojoUtils.logEntityToPojo(messages);
            }

            return Response.ok(messagesPojo).build();

        } catch (Exception e) {
            String errorMessage = "Could not obtain testcase messages, by using connection ID '"
                                  + connectionId + "'" + " and testcase ID '" + testcaseId + "'";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }

    }

    @GET
    @Path( "/testcase/{testcaseId: [0-9]*}/metainfo")
    @ApiOperation(
            value = "Get testcase metainfo by providing testcaseId",
            notes = "Get testcase metainfo by providing testcaseId",
            position = 4)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained testcase metainfo",
                    response = MetaInfosPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining testcase metainfo. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getTestcaseMetainfo( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,

                                 @ApiParam(
                                         name = "testcaseId",
                                         allowMultiple = false,
                                         required = true) @PathParam( "testcaseId") final int testcaseId ) {

        try {
            if (testcaseId < 0) {
                throw new IllegalArgumentException("Testcase ID must be equal or greater than zero");
            }

            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);
            List<TestcaseMetainfo> metainfos = readAccess.getTestcaseMetainfo(testcaseId);

            MetaInfosPojo metaInfosPojo = null;
            if (metainfos == null || metainfos.isEmpty()) {
                metaInfosPojo = new MetaInfosPojo();
                metaInfosPojo.setMetaInfo(new MetaInfoPojo[]{});
            } else {
                metaInfosPojo = (MetaInfosPojo) PojoUtils.logEntityToPojo(metainfos);
            }

            return Response.ok(metaInfosPojo).build();

        } catch (Exception e) {
            String errorMessage = "Could not obtain testcase metainfo, by using testcase ID '" + testcaseId
                                  + "' and connection ID '"
                                  + connectionId;
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    // TODO export statistics and checkpoints, filtered by
    /*
     * - name - CPU, Memory, Some action name
     * - time interval - 1 hour, 1 day
     * - combined
     * - per loader
     * */

    // not sure what I meant back then. Is this returning statistics descriptions, statistics only or both?!?
    @GET
    @Path( "/testcase/{testcaseId: [0-9]*}/statisticsDescriptions")
    @ApiOperation(
            value = "Get testcase system/user statistics descriptions by providing testcaseId",
            notes = "Get testcase system/user statistics descriptions by providing testcaseId",
            position = 6)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained system/user statistics descriptions",
                    response = StatisticDescriptionPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining testcase system/user statistics descriptions. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getTestcaseStatistics( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,

                                   @ApiParam(
                                           name = "testcaseId",
                                           allowMultiple = false,
                                           required = true) @PathParam( "testcaseId") final int testcaseId ) {

        try {
            if (testcaseId < 0) {
                throw new IllegalArgumentException("Testcase ID must be equal or greater than zero");
            }

            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);
            List<StatisticDescriptionPojo> statisticDescriptionsPojos = new ArrayList<>();

            List<StatisticDescription> statsDescs = readAccess.getSystemStatisticDescriptions(0,
                                                                                              "WHERE tt.testcaseId = "
                                                                                                 + testcaseId,
                                                                                              null, 0, false);
            for (StatisticDescription description : statsDescs) {
                StatisticDescriptionPojo descriptionPojo = (StatisticDescriptionPojo) PojoUtils.logEntityToPojo(description);
                List<StatisticPojo> statisticPojos = new ArrayList<>();
                List<Statistic> stats = readAccess.getSystemStatistics(0, testcaseId + "", description.machineId + "",
                                                                       description.statisticTypeId + "", 0, false);
                for (Statistic statistic : stats) {
                    statisticPojos.add((StatisticPojo) PojoUtils.logEntityToPojo(statistic));
                }
                descriptionPojo.setStatistics((StatisticPojo[]) statisticPojos.toArray(new StatisticPojo[statisticPojos.size()]));
                statisticDescriptionsPojos.add(descriptionPojo);
            }

            StatisticsDescriptionsPojo pojo = new StatisticsDescriptionsPojo();
            pojo.setStatisticsDescriptions((StatisticDescriptionPojo[]) statisticDescriptionsPojos.toArray(
                                                                                                           new StatisticDescriptionPojo[statisticDescriptionsPojos.size()]));

            return Response.ok(pojo).build();

        } catch (Exception e) {
            String errorMessage = "Could not obtain testcase system/user statistics, by using testcase ID '"
                                  + testcaseId
                                  + "' and connection ID '"
                                  + connectionId;
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

}
