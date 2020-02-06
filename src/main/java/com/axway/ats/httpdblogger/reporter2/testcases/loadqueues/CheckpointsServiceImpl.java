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
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.axway.ats.httpdblogger.reporter2.pojo.response.InternalServerErrorPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointSummaryPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointsSummariesPojo;
import com.axway.ats.httpdblogger.reporter2.utils.DbConnectionManager;
import com.axway.ats.httpdblogger.reporter2.utils.PojoUtils;
import com.axway.ats.log.autodb.entities.CheckpointSummary;
import com.axway.ats.log.autodb.model.IDbReadAccess;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path( "reporter2")
@Api( value = "/reporter2/checkpoints", description = "Retrieve CheckpointSummary(s) and/or Checkpoint(s) information")
public class CheckpointsServiceImpl {

    private static final Logger LOG = Logger.getLogger(CheckpointsServiceImpl.class);

    @GET
    @Path( "/checkpointsSummaries")
    @ApiOperation(
            value = "Get all checkpoint summaries",
            notes = "Get all checkpoint summaries",
            position = 5)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained all checkpoint summaries",
                    response = CheckpointsSummariesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining all checkpoints summaries. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getTestcaseLoadQueueCheckpoints( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId ) {

        try {

            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);

            CheckpointsSummariesPojo checkpointsPojo = new CheckpointsSummariesPojo();
            List<CheckpointSummary> checkpointSummaries = readAccess.getCheckpointsSummary("1=1",
                                                                                           "checkpointSummaryId",
                                                                                           true);
            List<CheckpointSummaryPojo> pojos = new ArrayList<CheckpointSummaryPojo>();
            for (CheckpointSummary checkSummary : checkpointSummaries) {
                pojos.add((CheckpointSummaryPojo) PojoUtils.logEntityToPojo(checkSummary));
            }
            checkpointsPojo.setCheckpointsSummaries(pojos.toArray(new CheckpointSummaryPojo[pojos.size()]));

            return Response.ok(checkpointsPojo).build();

        } catch (Exception e) {
            String errorMessage = "Could not obtain all checkpoints summaries, by using connection ID '"
                                  + connectionId + "'";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    @GET
    @Path( "/checkpointSummary/{checkpointSummaryId: [0-9]*}")
    @ApiOperation(
            value = "Get checkpoint summary by providing checkpointSummaryId",
            notes = "Get checkpoint summary by providing checkpointSummaryId",
            position = 5)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained checkpoint summary by providing checkpointSummaryId",
                    response = CheckpointSummaryPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining checkpoint summary by providing checkpointSummaryId. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getCheckpointsSummaries( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,

                                     @ApiParam(
                                             name = "checkpointSummaryId",
                                             allowMultiple = false,
                                             required = true) @PathParam( "checkpointSummaryId") final int checkpointSummaryId ) {

        try {

            if (checkpointSummaryId < 0) {
                throw new IllegalArgumentException("CheckpointSummary ID must be equal or greater than zero");
            }

            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);

            List<CheckpointSummary> checkpointSummaries = readAccess.getCheckpointsSummary("checkpointSummaryId = "
                                                                                           + checkpointSummaryId,
                                                                                           "checkpointSummaryId",
                                                                                           true);

            CheckpointSummaryPojo pojo = null;
            if (checkpointSummaries == null || checkpointSummaries.size() == 0) {
                pojo = new CheckpointSummaryPojo();
            } else {
                CheckpointSummary summary = checkpointSummaries.get(0);
                pojo = (CheckpointSummaryPojo) PojoUtils.logEntityToPojo(summary);
            }

            return Response.ok(pojo).build();

        } catch (Exception e) {
            String errorMessage = "Could not obtain checkpoint summary, by using checkpointSummary ID "
                                  + checkpointSummaryId + " and connection ID '"
                                  + connectionId + "'";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }
    
    

}
