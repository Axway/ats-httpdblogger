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
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.LoadQueuePojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.LoadQueuesPojo;
import com.axway.ats.httpdblogger.reporter2.utils.DbConnectionManager;
import com.axway.ats.httpdblogger.reporter2.utils.JsonUtils;
import com.axway.ats.httpdblogger.reporter2.utils.PojoUtils;
import com.axway.ats.log.autodb.entities.LoadQueue;
import com.axway.ats.log.autodb.model.IDbReadAccess;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path( "reporter2")
@Api( value = "/reporter2/loadQueues", description = "Retrieve LoadQueue(s) information")
public class LoadQueuesServiceImpl {

    private static final Logger LOG = Logger.getLogger(LoadQueuesServiceImpl.class);

    /*@GET
    @Path( "/loadQueues")
    @ApiOperation(
            value = "Get all loadQueues",
            notes = "Get all loadQueues",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained all loadQueues",
                    response = LoadQueuesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining all loadQueues. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getLoadQueues( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,
    
                           @ApiParam(
                                   name = "testcaseId",
                                   required = false) @QueryParam( "testcaseId") final int testcaseId,
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
            // validate input
            if (hasTestcaseId) {
                if (testcaseId < 0) {
                    throw new RuntimeException("Testcase ID must be zero or positive!");
                }
    
                whereClause = "testcaseId = " + testcaseId;
            } else {
                whereClause = "1=1";
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
                                      ? Integer.MAX_VALUE // hopefully there will be no more than that load queues
                                      : to;
    
            if (hasFromDate) {
                whereClause += " AND dateStart >= " + "DATEADD(MILLISECOND, " + fromDate
                               + " % 1000, DATEADD(SECOND, " + fromDate + " / 1000, '19700101'))";
            }
    
            if (hasToDate) {
                whereClause += " AND dateStart <= " + "DATEADD(MILLISECOND, " + toDate
                               + " % 1000, DATEADD(SECOND, " + toDate + " / 1000, '19700101'))";
            }
    
            List<LoadQueue> loadQueues = readAccess.getLoadQueues(whereClause, "loadQueueId",
                                                                  false, 0);
    
            return null;
        } catch (Exception e) {
            String errorMessage = "Could not obtain all loadQueues, by using connection ID '"
                                  + connectionId + "'";
            if (hasTestcaseId) {
                errorMessage += " and testcaseId '" + testcaseId + "'";
            }
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    
        try {
        
            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);
            List<LoadQueue> loadQueues = readAccess.getLoadQueues("1 = 1", "loadQueueId",
                                                                  true, 0);
        
            LoadQueuesPojo pojo = null;
            if (loadQueues.size() == 0) {
                pojo = new LoadQueuesPojo();
            } else {
                pojo = (LoadQueuesPojo) PojoUtils.logEntityToPojo(loadQueues);
        
            }
        
            return Response.ok(pojo).build();
        
        } catch (Exception e) {
            String errorMessage = "Could not obtain all loadQueues, by using connection ID '"
                                  + connectionId + "'";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }
    */

    @GET
    @Path( "/loadQueue/{loadQueueId: [0-9]*}")
    @ApiOperation(
            value = "Get loadQueue by providing loadQueueId",
            notes = "Get loadQueue by providing loadQueueId",
            position = 5)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained loadQueue",
                    response = LoadQueuePojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining loadQueue. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getLoadQueue( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,

                          @ApiParam(
                                  name = "loadQueueId",
                                  allowMultiple = false,
                                  required = true) @PathParam( "loadQueueId") final int loadQueueId,

                          @ApiParam(
                                  name = "properties",
                                  allowMultiple = true,
                                  value = "If you want to obtain only a subset of the loadQueue's properties, use this parameter",
                                  required = false) @QueryParam( "properties") final String properties ) {

        try {

            if (loadQueueId < 0) {
                throw new IllegalArgumentException("LoadQueue ID must be equal or greater than zero");
            }

            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);
            List<LoadQueue> loadQueues = readAccess.getLoadQueues("loadQueueId = "
                                                                  + loadQueueId, "loadQueueId",
                                                                  true, 0);

            LoadQueuePojo pojo = null;
            if (loadQueues.size() == 0) {
                pojo = new LoadQueuePojo();
            } else {
                pojo = (LoadQueuePojo) PojoUtils.logEntityToPojo(loadQueues.get(0));
            }

            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(pojo).build();
            } else {
                String[] propsKeys = properties.replace(" ", "").split(",");
                Object[] propsValues = new Object[propsKeys.length];
                for (int i = 0; i < propsKeys.length; i++) {
                    try {
                        propsValues[i] = ReflectionUtils.getFieldValue(pojo, propsKeys[i], false);
                    } catch (Exception e) {
                        if (e.getMessage().contains("Could not obtain field '" + propsKeys[i] + "' from class")) {
                            throw new IllegalArgumentException("" + propsKeys[i] + " is not a valid run property.");
                        } else {
                            throw e;
                        }
                    }
                }
                String json = JsonUtils.constructJson(propsKeys, propsValues, true);
                return Response.ok(json).build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not obtain loadQueue, by using loadQueue ID '" + loadQueueId
                                  + "' and connection ID '"
                                  + connectionId + "'";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

}
