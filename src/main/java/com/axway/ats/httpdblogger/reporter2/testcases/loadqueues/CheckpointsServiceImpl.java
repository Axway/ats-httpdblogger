/*
 * Copyright 2020 Axway Software
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
package com.axway.ats.httpdblogger.reporter2.testcases.loadqueues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import com.axway.ats.httpdblogger.reporter2.BaseReporterServiceImpl;
import com.axway.ats.httpdblogger.reporter2.pojo.response.InternalServerErrorPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointSummaryPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointsPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointsSummariesPojo;
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
@Api( value = "/reporter2/checkpoints", description = "Retrieve CheckpointSummary(s) and/or Checkpoint(s) information")
public class CheckpointsServiceImpl extends BaseReporterServiceImpl {

    private static final Logger LOG = Logger.getLogger(CheckpointsServiceImpl.class);

    @GET
    @Path( "/checkpointsSummaries")
    @ApiOperation( value = "Get checkpoints summaries", notes = "Get checkpoint summaries", position = 1)
    @ApiResponses(
            value = {
                      @ApiResponse(
                              code = 200,
                              message = "Successfully obtained checkpoint summaries",
                              response = CheckpointsSummariesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining checkpoint summaries. The server was not able to process the request",
                              response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getCheckpointsSummaries( @Context HttpServletRequest request,
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
                                                     name = "whereClauseKeys") @QueryParam( "whereClauseKeys") String whereClauseKeys,
                                             @ApiParam(
                                                     required = false,
                                                     name = "whereClauseCmpSigns") @QueryParam( "whereClauseCmpSigns") String whereClauseCmpSigns,
                                             @ApiParam(
                                                     required = false,
                                                     name = "whereClauseValues") @QueryParam( "whereClauseValues") String whereClauseValues ) {

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
            Map<String, List<Pair<CompareSign, Object>>> whereClauseEntries = constructWhereClauseMap(whereClauseKeys,
                                                                                                      whereClauseCmpSigns,
                                                                                                      whereClauseValues);
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getCheckpointsSummaries(from,
                                                                                                                         to,
                                                                                                                         whereClauseEntries,
                                                                                                                         null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getCheckpointsSummaries(from,
                                                                                                                         to,
                                                                                                                         whereClauseEntries,
                                                                                                                         Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get checkpoints summaries";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }

    }

    @GET
    @Path( "/checkpointSummary/{checkpointSummaryId: \\d+}")
    @ApiOperation( value = "Get checkpoint summary", notes = "Get checkpoint summary", position = 2)
    @ApiResponses(
            value = {
                      @ApiResponse(
                              code = 200,
                              message = "Successfully obtained checkpoint summary",
                              response = CheckpointSummaryPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining checkpoint summary. The server was not able to process the request",
                              response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getCheckpointSummary( @Context HttpServletRequest request,
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
                                                  name = "whereClauseKeys") @QueryParam( "whereClauseKeys") String whereClauseKeys,
                                          @ApiParam(
                                                  required = false,
                                                  name = "whereClauseCmpSigns") @QueryParam( "whereClauseCmpSigns") String whereClauseCmpSigns,
                                          @ApiParam(
                                                  required = false,
                                                  name = "whereClauseValues") @QueryParam( "whereClauseValues") String whereClauseValues,

                                          @ApiParam(
                                                  name = "checkpointSummaryId",
                                                  allowMultiple = false,
                                                  required = true) @PathParam( "checkpointSummaryId") int checkpointSummaryId ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        requiredQueryParams.add("checkpointSummaryId");
        try {
            RequestValidator.validateQueryParams(request, requiredQueryParams);
            if (!RequestValidator.hasQueryParam(request, "from")) {
                from = 0;
            }
            if (!RequestValidator.hasQueryParam(request, "to")) {
                to = Integer.MAX_VALUE;
            }
            Map<String, List<Pair<CompareSign, Object>>> whereClauseEntries = constructWhereClauseMap(whereClauseKeys,
                                                                                                      whereClauseCmpSigns,
                                                                                                      whereClauseValues);
            Pair<DbReader.CompareSign, Object> pair = new ImmutablePair<DbReader.CompareSign, Object>(CompareSign.EQUAL,
                                                                                                      checkpointSummaryId);
            if (whereClauseEntries.containsKey("checkpointSummaryId")) {
                whereClauseEntries.get("checkpointSummaryId")
                                  .add(pair);
            } else {
                List<Pair<CompareSign, Object>> array = new ArrayList<Pair<CompareSign, Object>>();
                array.add(pair);
                whereClauseEntries.put("checkpointSummaryId", array);
            }
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getCheckpointSummary(from,
                                                                                                                      to,
                                                                                                                      whereClauseEntries,
                                                                                                                      null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getCheckpointSummary(from,
                                                                                                                      to,
                                                                                                                      whereClauseEntries,
                                                                                                                      Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get checkpoint summary";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    @GET
    @Path( "/checkpoints")
    @ApiOperation( value = "Get checkpoints", notes = "Get checkpoint", position = 2)
    @ApiResponses(
            value = {
                      @ApiResponse(
                              code = 200,
                              message = "Successfully obtained checkpoint",
                              response = CheckpointsPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining checkpoint. The server was not able to process the request",
                              response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getCheckpoints( @Context HttpServletRequest request,
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
                                            name = "whereClauseKeys") @QueryParam( "whereClauseKeys") String whereClauseKeys,
                                    @ApiParam(
                                            required = false,
                                            name = "whereClauseCmpSigns") @QueryParam( "whereClauseCmpSigns") String whereClauseCmpSigns,
                                    @ApiParam(
                                            required = false,
                                            name = "whereClauseValues") @QueryParam( "whereClauseValues") String whereClauseValues ) {

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
            Map<String, List<Pair<CompareSign, Object>>> whereClauseEntries = constructWhereClauseMap(whereClauseKeys,
                                                                                                      whereClauseCmpSigns,
                                                                                                      whereClauseValues);
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getCheckpoints(from,
                                                                                                                to,
                                                                                                                whereClauseEntries,
                                                                                                                null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getCheckpoints(from,
                                                                                                                to,
                                                                                                                whereClauseEntries,
                                                                                                                Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get checkpoints";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }

    }

    @GET
    @Path( "/checkpoint/{checkpointId: \\d+}")
    @ApiOperation( value = "Get checkpoint ", notes = "Get checkpoint", position = 2)
    @ApiResponses(
            value = {
                      @ApiResponse(
                              code = 200,
                              message = "Successfully obtained checkpoint",
                              response = CheckpointPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining checkpoint. The server was not able to process the request",
                              response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getCheckpoint( @Context HttpServletRequest request,
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
                                           name = "whereClauseKeys") @QueryParam( "whereClauseKeys") String whereClauseKeys,
                                   @ApiParam(
                                           required = false,
                                           name = "whereClauseCmpSigns") @QueryParam( "whereClauseCmpSigns") String whereClauseCmpSigns,
                                   @ApiParam(
                                           required = false,
                                           name = "whereClauseValues") @QueryParam( "whereClauseValues") String whereClauseValues,

                                   @ApiParam(
                                           name = "checkpointId",
                                           allowMultiple = false,
                                           required = true) @PathParam( "checkpointId") int checkpointId ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        requiredQueryParams.add("checkpointId");
        try {
            RequestValidator.validateQueryParams(request, requiredQueryParams);
            if (!RequestValidator.hasQueryParam(request, "from")) {
                from = 0;
            }
            if (!RequestValidator.hasQueryParam(request, "to")) {
                to = Integer.MAX_VALUE;
            }
            Map<String, List<Pair<CompareSign, Object>>> whereClauseEntries = constructWhereClauseMap(whereClauseKeys,
                                                                                                      whereClauseCmpSigns,
                                                                                                      whereClauseValues);
            Pair<DbReader.CompareSign, Object> pair = new ImmutablePair<DbReader.CompareSign, Object>(CompareSign.EQUAL,
                                                                                                      checkpointId);
            if (whereClauseEntries.containsKey("checkpointId")) {
                whereClauseEntries.get("checkpointId")
                                  .add(pair);
            } else {
                List<Pair<CompareSign, Object>> array = new ArrayList<Pair<CompareSign, Object>>();
                array.add(pair);
                whereClauseEntries.put("checkpointId", array);
            }
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getCheckpoint(from,
                                                                                                               to,
                                                                                                               whereClauseEntries,
                                                                                                               null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getCheckpoint(from,
                                                                                                               to,
                                                                                                               whereClauseEntries,
                                                                                                               Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get checkpoint";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

}
