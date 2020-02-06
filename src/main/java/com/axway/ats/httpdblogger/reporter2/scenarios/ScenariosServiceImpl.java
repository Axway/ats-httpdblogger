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

import javax.ws.rs.Path;

import org.apache.log4j.Logger;

import com.wordnik.swagger.annotations.Api;

@Path( "reporter2")
@Api( value = "/reporter2/scenario", description = "Retrieve Scenario(s) information")
public class ScenariosServiceImpl {

    private static final Logger LOG = Logger.getLogger(ScenariosServiceImpl.class);

    /*@GET
    @Path( "/scenarios")
    @ApiOperation(
            value = "Get all scenarios",
            notes = "Get all scenarios",
            position = 2)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained all scenarios",
                    response = ScenariosPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining all scenarios. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getScenarios( @ApiParam( name = "connectionId", required = true) @Validate(
            name = "connectionId",
            type = ValidationType.STRING_NOT_EMPTY) @QueryParam( "connectionId") final String connectionId,
                                  @ApiParam(
                                          name = "suiteId",
                                          required = false) @QueryParam( "suiteId") final int suiteId,
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
    
        boolean hasSuiteId = (request != null && request.getQueryString() != null
                              && request.getQueryString().contains("suiteId"));
    
        boolean hasFromDate = (request != null && request.getQueryString() != null
                               && request.getQueryString().contains("fromDate"));
    
        boolean hasToDate = (request != null && request.getQueryString() != null
                             && request.getQueryString().contains("toDate"));
        try {
    
            String whereClause = null;
            // validate input
            if (hasSuiteId) {
                if (suiteId < 0) {
                    throw new RuntimeException("Suite ID must be zero or positive!");
                }
    
                whereClause = "WHERE suiteId = " + suiteId;
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
                                      ? readAccess.getScenariosCount(whereClause)
                                      : to;
    
            if (hasFromDate) {
                whereClause += " AND dateStart >= " + "DATEADD(MILLISECOND, " + fromDate
                               + " % 1000, DATEADD(SECOND, " + fromDate + " / 1000, '19700101'))";
            }
    
            if (hasToDate) {
                whereClause += " AND dateStart <= " + "DATEADD(MILLISECOND, " + toDate
                               + " % 1000, DATEADD(SECOND, " + toDate + " / 1000, '19700101'))";
            }
    
            List<Scenario> scenarios = readAccess.getScenarios(startRecord, endRecord,
                                                               whereClause, "scenarioId", true,
                                                               0);
    
            ScenariosPojo pojo = null;
            if (scenarios == null || scenarios.isEmpty()) {
                pojo = new ScenariosPojo();
                pojo.setScenarios(new ScenarioPojo[]{});
            } else {
                pojo = (ScenariosPojo) PojoUtils.logEntityToPojo(scenarios);
            }
    
            return Response.ok(pojo).build();
        } catch (Exception e) {
            String errorMessage = "Could not obtain all scenarios, using connection ID '" + connectionId + "'";
            if (hasSuiteId) {
                errorMessage += " and suite ID '" + suiteId + "'";
            }
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    
    }
    
    @GET
    @Path( "/scenario/{scenarioId: [0-9]*}")
    @ApiOperation(
            value = "Get scenario details by providing scenarioId",
            notes = "Get scenario details by providing scenarioId",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained scenario details",
                    response = ScenarioPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining scenario details. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getScenario( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,
    
                         @ApiParam(
                                 name = "scenarioId",
                                 allowMultiple = false,
                                 required = true) @PathParam( "scenarioId") final int scenarioId,
    
                         @ApiParam(
                                 name = "properties",
                                 allowMultiple = true,
                                 value = "If you want to obtain only a subset of the scenario's properties, use this parameter",
                                 required = false) @QueryParam( "properties") final String properties ) {
    
        try {
            if (scenarioId < 0) {
                throw new IllegalArgumentException("Scenario ID must be equal or greater than zero");
            }
            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);
            List<Scenario> scenarios = readAccess.getScenarios(0, 1, "WHERE scenarioId = " + scenarioId, "scenarioId",
                                                               false,
                                                               0);
            if (scenarios == null || scenarios.isEmpty()) {
                // It is possible that the scenario exists, but all of the testcases, related to that scenario were deleted
                // In that case you will receive the exception below as well, which can be confusing for the end user
                throw new NoSuchScenarioException(scenarioId, connectionId);
            }
            ScenarioPojo scenarioPojo = (ScenarioPojo) PojoUtils.logEntityToPojo(scenarios.get(0));
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(scenarioPojo).build();
            } else {
                String[] propsKeys = properties.replace(" ", "").split(",");
                Object[] propsValues = new Object[propsKeys.length];
                for (int i = 0; i < propsKeys.length; i++) {
                    try {
                        propsValues[i] = ReflectionUtils.getFieldValue(scenarioPojo, propsKeys[i], false);
                    } catch (Exception e) {
                        if (e.getMessage().contains("Could not obtain field '" + propsKeys[i] + "' from class")) {
                            throw new IllegalArgumentException("" + propsKeys[i]
                                                               + " is not a valid scenario property.");
                        } else {
                            throw e;
                        }
                    }
                }
                String json = JsonUtils.constructJson(propsKeys, propsValues, true);
                return Response.ok(json).build();
            }
    
        } catch (Exception e) {
            String errorMessage = "Could not obtain scenario details, by using ID '" + scenarioId
                                  + "' and connection ID '"
                                  + connectionId;
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }
    
    @GET
    @Path( "/scenario/{scenarioId: [0-9]*}/metainfo")
    @ApiOperation(
            value = "Get scenario metainfo by providing scenarioId",
            notes = "Get scenario metainfo by providing scenarioId",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully obtained scenario metainfo",
                    response = MetaInfosPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining scenario metainfo. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            getScenarioMetainfo( @ApiParam(
                    name = "connectionId",
                    required = true) @QueryParam( "connectionId") final String connectionId,
    
                                 @ApiParam(
                                         name = "scenarioId",
                                         allowMultiple = false,
                                         required = true) @PathParam( "scenarioId") final int scenarioId ) {
    
        try {
            if (scenarioId < 0) {
                throw new IllegalArgumentException("Scenario ID must be equal or greater than zero");
            }
    
            IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);
            List<ScenarioMetaInfo> metainfos = readAccess.getScenarioMetaInfo(scenarioId);
    
            MetaInfosPojo metaInfosPojo = null;
            if (metainfos == null || metainfos.isEmpty()) {
                metaInfosPojo = new MetaInfosPojo();
                metaInfosPojo.setMetaInfo(new MetaInfoPojo[]{});
            } else {
                metaInfosPojo = (MetaInfosPojo) PojoUtils.logEntityToPojo(metainfos);
            }
    
            return Response.ok(metaInfosPojo).build();
    
        } catch (Exception e) {
            String errorMessage = "Could not obtain scenario metainfo, by using scenario ID '" + scenarioId
                                  + "' and connection ID '"
                                  + connectionId;
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }*/

}
