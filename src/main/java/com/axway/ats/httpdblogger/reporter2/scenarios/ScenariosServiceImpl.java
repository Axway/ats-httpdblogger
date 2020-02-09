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
package com.axway.ats.httpdblogger.reporter2.scenarios;

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
import com.axway.ats.httpdblogger.reporter2.pojo.response.MetaInfosPojo;
import com.axway.ats.httpdblogger.reporter2.scenarios.pojo.response.ScenarioPojo;
import com.axway.ats.httpdblogger.reporter2.scenarios.pojo.response.ScenariosPojo;
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
@Api( value = "/reporter2/scenarios", description = "Retrieve Scenario(s) information")
public class ScenariosServiceImpl {

    private static final Logger LOG = Logger.getLogger(ScenariosServiceImpl.class);

    @GET
    @Path( "/scenarios")
    @ApiOperation( value = "Get scenarios", notes = "Get scenarios", position = 2)
    @ApiResponses( value = {
                             @ApiResponse( code = 200, message = "Successfully obtained scenarios", response = ScenariosPojo.class),
                             @ApiResponse( code = 500, message = "Problem obtaining scenarios. The server was not able to process the request", response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getScenarios( @Context HttpServletRequest request,
                                  @ApiParam( required = true, name = "connectionId") @QueryParam( "connectionId") String connectionId,
                                  @ApiParam( required = false, name = "from") @QueryParam( "from") int from,
                                  @ApiParam( required = false, name = "to") @QueryParam( "to") int to,
                                  @ApiParam( required = false, allowMultiple = true, name = "properties") @QueryParam( "properties") String properties,
                                  @ApiParam( required = false, name = "whereClause") @QueryParam( "whereClause") String whereClause ) {

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
                        throw new RuntimeException(
                                                   "Incorrect where clause syntax. Expected format for the where clause is <key> <compare sign> <value>");
                    }
                    whereClauseEntries.put(subTokens[0].trim(), new ImmutablePair<DbReader.CompareSign, Object>(
                                                                                                                CompareSign.fromString(subTokens[1].trim()),
                                                                                                                subTokens[2].trim()));
                }

            }
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getScenarios(from, to,
                                                                                                              whereClauseEntries,
                                                                                                              null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getScenarios(from, to,
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
    @Path( "/scenario/{scenarioId: \\d+}")
    @ApiOperation( value = "Get scenario", notes = "Get scenario", position = 1)
    @ApiResponses( value = {
                             @ApiResponse( code = 200, message = "Successfully obtained scenario", response = ScenarioPojo.class),
                             @ApiResponse( code = 500, message = "Problem obtaining scenario. The server was not able to process the request", response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getScenario( @Context HttpServletRequest request,
                                 @ApiParam( required = true, name = "connectionId") @QueryParam( "connectionId") String connectionId,
                                 @ApiParam( required = false, name = "from") @QueryParam( "from") int from,
                                 @ApiParam( required = false, name = "to") @QueryParam( "to") int to,
                                 @ApiParam( required = false, name = "properties") @QueryParam( "properties") String properties,

                                 @ApiParam( required = false, name = "whereClause") @QueryParam( "whereClause") String whereClause,

                                 @ApiParam( name = "scenarioId", allowMultiple = false, required = true) @PathParam( "scenarioId") int scenarioId ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        requiredQueryParams.add("scenarioId");
        try {
            RequestValidator.validateQueryParams(request, requiredQueryParams);
            if (!RequestValidator.hasQueryParam(request, "from")) {
                from = 0;
            }
            if (!RequestValidator.hasQueryParam(request, "to")) {
                to = Integer.MAX_VALUE;
            }
            Map<String, Pair<CompareSign, Object>> whereClauseEntries = new HashMap<String, Pair<CompareSign, Object>>();
            whereClauseEntries.put("scenarioId",
                                   new ImmutablePair<DbReader.CompareSign, Object>(DbReader.CompareSign.EQUAL,
                                                                                   scenarioId));
            if (RequestValidator.hasQueryParam(request, "whereClause")) {

                String[] whereClauseTokens = whereClause.split(",");
                for (String token : whereClauseTokens) {
                    String[] subTokens = token.split(Pattern.quote(" "));
                    if (subTokens.length != 3) {
                        throw new RuntimeException(
                                                   "Incorrect where clause syntax. Expected format for the where clause is <key> <compare sign> <value>");
                    }
                    whereClauseEntries.put(subTokens[0].trim(), new ImmutablePair<DbReader.CompareSign, Object>(
                                                                                                                CompareSign.fromString(subTokens[1].trim()),
                                                                                                                subTokens[2].trim()));
                }

            }
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getScenario(from, to,
                                                                                                             whereClauseEntries,
                                                                                                             null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getScenario(from, to,
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
    @Path( "/scenario/{scenarioId: \\d+}/metainfo")
    @ApiOperation( value = "Get scenario metainfo", notes = "Get scenario metainfo", position = 1)
    @ApiResponses( value = {
                             @ApiResponse( code = 200, message = "Successfully obtained scenario metainfo", response = MetaInfosPojo.class),
                             @ApiResponse( code = 500, message = "Problem obtaining scenario metainfo. The server was not able to process the request", response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getRunMetainfo( @Context HttpServletRequest request,
                                    @ApiParam( required = true, name = "connectionId") @QueryParam( "connectionId") String connectionId,

                                    @ApiParam( required = false, name = "properties") @QueryParam( "properties") String properties,

                                    @ApiParam( name = "scenarioId", allowMultiple = false, required = true) @PathParam( "scenarioId") int scenarioId ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        requiredQueryParams.add("scenarioId");
        try {

            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId))
                                                                                                .getScenarioMetainfo(scenarioId,
                                                                                                                     null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId))
                                                                                                .getScenarioMetainfo(scenarioId,
                                                                                                                     Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get scenario metainfo";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

}
