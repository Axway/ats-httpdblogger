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
package com.axway.ats.httpdblogger.reporter2.suites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.httpdblogger.reporter2.pojo.response.InternalServerErrorPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MessagesPojo;
import com.axway.ats.httpdblogger.reporter2.suites.pojo.response.SuitePojo;
import com.axway.ats.httpdblogger.reporter2.suites.pojo.response.SuitesPojo;
import com.axway.ats.httpdblogger.reporter2.utils.DbConnectionManager;
import com.axway.ats.httpdblogger.reporter2.utils.DbReader;
import com.axway.ats.httpdblogger.reporter2.utils.DbReader.CompareSign;
import com.axway.ats.httpdblogger.reporter2.utils.RequestValidator;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path( "reporter2")
@Api( value = "/reporter2/suite", description = "Retrieve Suite(s) information")
public class SuitesServiceImpl {

    private static final Logger LOG = Logger.getLogger(SuitesServiceImpl.class);

    @GET
    @Path( "/suites")
    @ApiOperation(
            value = "Get suites",
            notes = "Get suites",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained suites",
                    response = SuitesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining suites. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getSuites( @Context HttpServletRequest request,
                               @ApiParam(
                                       required = true,
                                       name = "connectionId") @QueryParam( "connectionId") String connectionId,
                               @ApiParam( required = false, name = "from") @QueryParam( "from") int from,
                               @ApiParam( required = false, name = "to") @QueryParam( "to") int to,
                               @ApiParam(
                                       required = false,
                                       allowMultiple = true,
                                       name = "properties") @QueryParam( "properties") String properties,
                               @ApiParam(
                                       required = false,
                                       name = "whereClause") @QueryParam( "whereClause") String whereClause ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        try {
            RequestValidator.validateQueryParams(request, requiredQueryParams);
            if (!RequestValidator.hasQueryParam(request, "from")) {
                from = 0;
            }
            if (!RequestValidator.hasQueryParam(request, "to")) {
                to = Integer.MAX_VALUE;
            }
            Map<String, Pair<CompareSign, Object>> whereClauseEntries = new HashMap<String, Pair<CompareSign, Object>>();
            if (RequestValidator.hasQueryParam(request, "whereClause")) {

                String[] whereClauseTokens = whereClause.split(",");
                for (String token : whereClauseTokens) {
                    String[] subTokens = token.split(Pattern.quote(" "));
                    if (subTokens.length != 3) {
                        throw new RuntimeException("Incorrect where clause syntax. Expected format for the where clause is <key> <compare sign> <value>");
                    }
                    whereClauseEntries.put(subTokens[0].trim(),
                                           new ImmutablePair<DbReader.CompareSign, Object>(CompareSign.fromString(subTokens[1].trim()),
                                                                                           subTokens[2].trim()));
                }

            }
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getSuites(from, to,
                                                                                                           whereClauseEntries,
                                                                                                           null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getSuites(from, to,
                                                                                                           whereClauseEntries,
                                                                                                           Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get suites";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }

    }

    @GET
    @Path( "/suite/{suiteId: \\d+}")
    @ApiOperation(
            value = "Get suite",
            notes = "Get suite",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained suite",
                    response = SuitePojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining suite. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getRun( @Context HttpServletRequest request,
                            @ApiParam(
                                    required = true,
                                    name = "connectionId") @QueryParam( "connectionId") String connectionId,
                            @ApiParam( required = false, name = "from") @QueryParam( "from") int from,
                            @ApiParam( required = false, name = "to") @QueryParam( "to") int to,
                            @ApiParam(
                                    required = false,
                                    name = "properties") @QueryParam( "properties") String properties,

                            @ApiParam(
                                    required = false,
                                    name = "whereClause") @QueryParam( "whereClause") String whereClause,

                            @ApiParam(
                                    name = "suiteId",
                                    allowMultiple = false,
                                    required = true) @PathParam( "suiteId") int suiteId ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        try {
            RequestValidator.validateQueryParams(request, requiredQueryParams);
            if (!RequestValidator.hasQueryParam(request, "from")) {
                from = 0;
            }
            if (!RequestValidator.hasQueryParam(request, "to")) {
                to = Integer.MAX_VALUE;
            }
            Map<String, Pair<CompareSign, Object>> whereClauseEntries = new HashMap<String, Pair<CompareSign, Object>>();
            whereClauseEntries.put("suiteId",
                                   new ImmutablePair<DbReader.CompareSign, Object>(DbReader.CompareSign.EQUAL,
                                                                                   suiteId));
            if (RequestValidator.hasQueryParam(request, "whereClause")) {

                String[] whereClauseTokens = whereClause.split(",");
                for (String token : whereClauseTokens) {
                    String[] subTokens = token.split(Pattern.quote(" "));
                    if (subTokens.length != 3) {
                        throw new RuntimeException("Incorrect where clause syntax. Expected format for the where clause is <key> <compare sign> <value>");
                    }
                    whereClauseEntries.put(subTokens[0].trim(),
                                           new ImmutablePair<DbReader.CompareSign, Object>(CompareSign.fromString(subTokens[1].trim()),
                                                                                           subTokens[2].trim()));
                }

            }
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getSuite(from, to,
                                                                                                          whereClauseEntries,
                                                                                                          null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getSuite(from, to,
                                                                                                          whereClauseEntries,
                                                                                                          Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get suite";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    @GET
    @Path( "/suite/{suiteId: \\d+}/messages")
    @ApiOperation(
            value = "Get suite messages",
            notes = "Get suite messages",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained suite messages",
                    response = MessagesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining suite messages. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getRunMessages( @Context HttpServletRequest request,
                                    @ApiParam(
                                            required = true,
                                            name = "connectionId") @QueryParam( "connectionId") String connectionId,
                                    @ApiParam( required = false, name = "from") @QueryParam( "from") int from,
                                    @ApiParam( required = false, name = "to") @QueryParam( "to") int to,
                                    @ApiParam(
                                            required = false,
                                            name = "properties") @QueryParam( "properties") String properties,

                                    @ApiParam(
                                            required = false,
                                            name = "whereClause") @QueryParam( "whereClause") String whereClause,

                                    @ApiParam(
                                            name = "suiteId",
                                            allowMultiple = false,
                                            required = true) @PathParam( "suiteId") int suiteId ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        try {
            RequestValidator.validateQueryParams(request, requiredQueryParams);
            if (!RequestValidator.hasQueryParam(request, "from")) {
                from = 0;
            }
            if (!RequestValidator.hasQueryParam(request, "to")) {
                to = Integer.MAX_VALUE;
            }
            Map<String, Pair<CompareSign, Object>> whereClauseEntries = new HashMap<String, Pair<CompareSign, Object>>();
            whereClauseEntries.put("suiteId",
                                   new ImmutablePair<DbReader.CompareSign, Object>(DbReader.CompareSign.EQUAL,
                                                                                   suiteId));
            if (RequestValidator.hasQueryParam(request, "whereClause")) {

                String[] whereClauseTokens = whereClause.split(",");
                for (String token : whereClauseTokens) {
                    String[] subTokens = token.split(Pattern.quote(" "));
                    if (subTokens.length != 3) {
                        throw new RuntimeException("Incorrect where clause syntax. Expected format for the where clause is <key> <compare sign> <value>");
                    }
                    whereClauseEntries.put(subTokens[0].trim(),
                                           new ImmutablePair<DbReader.CompareSign, Object>(CompareSign.fromString(subTokens[1].trim()),
                                                                                           subTokens[2].trim()));
                }

            }
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getSuiteMessages(from,
                                                                                                                  to,
                                                                                                                  whereClauseEntries,
                                                                                                                  null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getSuiteMessages(from,
                                                                                                                  to,
                                                                                                                  whereClauseEntries,
                                                                                                                  Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get suite messages";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    /*@GET
    @Path( "/suites")
    @ApiOperation(
            value = "Get all suite",
            notes = "Get all suite",
            position = 2)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained all suite",
                    response = SuitesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining all suites. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getSuites( @ApiParam( name = "connectionId", required = true) @Validate(
            name = "connectionId",
            type = ValidationType.STRING_NOT_EMPTY) @QueryParam( "connectionId") final String connectionId,
    
                               @ApiParam( name = "runId", required = false) @QueryParam( "runId") final int runId,
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
    
        boolean hasRunId = (request != null && request.getQueryString() != null
                            && request.getQueryString().contains("runId"));
    
        boolean hasFromDate = (request != null && request.getQueryString() != null
                               && request.getQueryString().contains("fromDate"));
    
        boolean hasToDate = (request != null && request.getQueryString() != null
                             && request.getQueryString().contains("toDate"));
        try {
    
            String whereClause = null;
            // validate input
            if (hasRunId) {
                if (runId < 0) {
                    throw new RuntimeException("Run ID must be zero or positive!");
                }
    
                whereClause = "WHERE runId = " + runId;
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
                                      ? readAccess.getSuitesCount(whereClause)
                                      : to;
    
            if (hasFromDate) {
                whereClause += " AND dateStart >= " + "DATEADD(MILLISECOND, " + fromDate
                               + " % 1000, DATEADD(SECOND, " + fromDate + " / 1000, '19700101'))";
            }
    
            if (hasToDate) {
                whereClause += " AND dateStart <= " + "DATEADD(MILLISECOND, " + toDate
                               + " % 1000, DATEADD(SECOND, " + toDate + " / 1000, '19700101'))";
            }
    
            List<Suite> suites = readAccess.getSuites(startRecord, endRecord, whereClause, "suiteId", true, 0);
    
            SuitesPojo pojo = null;
            if (suites == null || suites.isEmpty()) {
                pojo = new SuitesPojo();
                pojo.setSuites(new SuitePojo[]{});
            } else {
                pojo = (SuitesPojo) PojoUtils.logEntityToPojo(suites);
            }
    
            return Response.ok(pojo).build();
    
        } catch (Exception e) {
            String errorMessage = "Could not obtain all suites, using connection ID '" + connectionId + "'";
            if (hasRunId) {
                errorMessage += " and run ID '" + runId + "'";
            }
            // TODO add the from, to, fromDate, toDate in the error message
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }
    
    @GET
    @Path( "/suite/{suiteId: [0-9]*}")
    @ApiOperation(
            value = "Get suite details by providing suiteId",
            notes = "Get suite details by providing suiteId",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained suite details",
                    response = SuitePojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining suite details. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getSuite( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,
    
                      @ApiParam(
                              name = "suiteId",
                              allowMultiple = false,
                              required = true) @PathParam( "suiteId") final int suiteId,
    
                      @ApiParam(
                              name = "properties",
                              allowMultiple = true,
                              value = "If you want to obtain only a subset of the suite's properties, use this parameter",
                              required = false) @QueryParam( "properties") final String properties ) {
    
        try {
            if (suiteId < 0) {
                throw new IllegalArgumentException("Suite ID must be equal or greater than zero");
            }
            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);
            List<Suite> suites = readAccess.getSuites(0, 1, "WHERE suiteId = " + suiteId, "suiteId", false, 0);
            if (suites == null || suites.isEmpty()) {
                throw new NoSuchSuiteException(suiteId, connectionId);
            }
            SuitePojo suitePojo = (SuitePojo) PojoUtils.logEntityToPojo(suites.get(0));
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(suitePojo).build();
            } else {
                String[] propsKeys = properties.replace(" ", "").split(",");
                Object[] propsValues = new Object[propsKeys.length];
                for (int i = 0; i < propsKeys.length; i++) {
                    try {
                        propsValues[i] = ReflectionUtils.getFieldValue(suitePojo, propsKeys[i], false);
                    } catch (Exception e) {
                        if (e.getMessage().contains("Could not obtain field '" + propsKeys[i] + "' from class")) {
                            throw new IllegalArgumentException("" + propsKeys[i] + " is not a valid suite property.");
                        } else {
                            throw e;
                        }
                    }
                }
                String json = JsonUtils.constructJson(propsKeys, propsValues, true);
                return Response.ok(json).build();
            }
    
        } catch (Exception e) {
            String errorMessage = "Could not obtain suite details, by using ID '" + suiteId + "' and connection ID '"
                                  + connectionId;
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }
    
    @GET
    @Path( "/suite/{suiteId: [0-9]*}/messages")
    @ApiOperation(
            value = "Get suite messages by providing suiteId",
            notes = "Get suite messages by providing suiteId",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained suite messages",
                    response = MessagesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining suite messages. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getSuiteMessages( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,
    
                              @ApiParam(
                                      name = "suiteId",
                                      allowMultiple = false,
                                      required = true) @PathParam( "suiteId") final int suiteId,
    
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
    
        try {
    
            String whereClause = null;
            if (suiteId < 0) {
                throw new IllegalArgumentException("Suite ID must be equal or greater than zero");
            }
    
            whereClause = "WHERE suiteId = " + suiteId;
    
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
                                      ? readAccess.getSuiteMessagesCount(whereClause)
                                      : to;
    
            boolean hasFromDate = (request != null && request.getQueryString() != null
                                   && request.getQueryString().contains("fromDate"));
            if (hasFromDate) {
                whereClause += " AND timestamp >= " + "DATEADD(MILLISECOND, " + fromDate
                               + " % 1000, DATEADD(SECOND, " + fromDate + " / 1000, '19700101'))";
            }
    
            boolean hasToDate = (request != null && request.getQueryString() != null
                                 && request.getQueryString().contains("toDate"));
            if (hasToDate) {
                whereClause += " AND timestamp <= " + "DATEADD(MILLISECOND, " + toDate
                               + " % 1000, DATEADD(SECOND, " + toDate + " / 1000, '19700101'))";
            }
    
            List<Message> messages = readAccess.getSuiteMessages(startRecord, endRecord,
                                                                 whereClause,
                                                                 "suiteMessageId", true, 0);
    
            MessagesPojo messagesPojo = null;
            if (messages == null || messages.isEmpty()) {
                messagesPojo = new MessagesPojo();
                messagesPojo.setMessages(new MessagePojo[]{});
            } else {
                messagesPojo = (MessagesPojo) PojoUtils.logEntityToPojo(messages);
            }
    
            return Response.ok(messagesPojo).build();
    
        } catch (Exception e) {
            String errorMessage = "Could not obtain suite messages, by using suite ID '" + suiteId
                                  + "' and connection ID '"
                                  + connectionId + "'";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }*/

}
