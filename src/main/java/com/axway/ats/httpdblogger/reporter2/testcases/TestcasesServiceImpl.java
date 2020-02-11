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
import com.axway.ats.httpdblogger.reporter2.pojo.response.MessagesPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MetaInfosPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.TestcasePojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.TestcasesPojo;
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
@Api( value = "/reporter2/testcases", description = "Retrieve Testcase(s) information")
public class TestcasesServiceImpl extends BaseReporterServiceImpl {

    private static final Logger LOG = Logger.getLogger(TestcasesServiceImpl.class);

    @GET
    @Path( "/testcases")
    @ApiOperation( value = "Get testcases", notes = "Get testcases", position = 1)
    @ApiResponses(
            value = {
                      @ApiResponse(
                              code = 200,
                              message = "Successfully obtained testcases",
                              response = TestcasesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining testcases. The server was not able to process the request",
                              response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getTestcases( @Context HttpServletRequest request,
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
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getTestcases(from, to,
                                                                                                              whereClauseEntries,
                                                                                                              null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getTestcases(from, to,
                                                                                                              whereClauseEntries,
                                                                                                              Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get testcases";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }

    }

    @GET
    @Path( "/testcase/{testcaseId: \\d+}")
    @ApiOperation( value = "Get testcase", notes = "Get testcase", position = 1)
    @ApiResponses(
            value = {
                      @ApiResponse(
                              code = 200,
                              message = "Successfully obtained testcase",
                              response = TestcasePojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining testcase. The server was not able to process the request",
                              response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getTestcase( @Context HttpServletRequest request,
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
                                         name = "testcaseId",
                                         allowMultiple = false,
                                         required = true) @PathParam( "testcaseId") int testcaseId ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        requiredQueryParams.add("testcaseId");
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
                                                                                                      testcaseId);
            if (whereClauseEntries.containsKey("testcaseId")) {
                whereClauseEntries.get("testcaseId")
                                  .add(pair);
            } else {
                List<Pair<CompareSign, Object>> array = new ArrayList<Pair<CompareSign, Object>>();
                array.add(pair);
                whereClauseEntries.put("testcaseId", array);
            }
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getTestcase(from, to,
                                                                                                             whereClauseEntries,
                                                                                                             null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId)).getTestcase(from, to,
                                                                                                             whereClauseEntries,
                                                                                                             Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get testcase";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    @GET
    @Path( "/testcase/{testcaseId: \\d+}/messages")
    @ApiOperation( value = "Get testcase messages", notes = "Get testcase messages", position = 1)
    @ApiResponses(
            value = {
                      @ApiResponse(
                              code = 200,
                              message = "Successfully obtained testcase messages",
                              response = MessagesPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining testcase messages. The server was not able to process the request",
                              response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getTestcaseMessages( @Context HttpServletRequest request,
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
                                                 name = "testcaseId",
                                                 allowMultiple = false,
                                                 required = true) @PathParam( "testcaseId") int testcaseId ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        requiredQueryParams.add("testcaseId");
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
                                                                                                      testcaseId);
            if (whereClauseEntries.containsKey("testcaseId")) {
                whereClauseEntries.get("testcaseId")
                                  .add(pair);
            } else {
                List<Pair<CompareSign, Object>> array = new ArrayList<Pair<CompareSign, Object>>();
                array.add(pair);
                whereClauseEntries.put("testcaseId", array);
            }
            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId))
                                                                                                .getTestcaseMessages(from,
                                                                                                                     to,
                                                                                                                     whereClauseEntries,
                                                                                                                     null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId))
                                                                                                .getTestcaseMessages(from,
                                                                                                                     to,
                                                                                                                     whereClauseEntries,
                                                                                                                     Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get testcase messages";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    @GET
    @Path( "/testcase/{testcaseId: \\d+}/metainfo")
    @ApiOperation( value = "Get testcase metainfo", notes = "Get testcase metainfo", position = 1)
    @ApiResponses(
            value = {
                      @ApiResponse(
                              code = 200,
                              message = "Successfully obtained testcase metainfo",
                              response = MetaInfosPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem obtaining testcase metainfo. The server was not able to process the request",
                              response = InternalServerErrorPojo.class) })
    @Produces( MediaType.APPLICATION_JSON)
    public Response getRunMetainfo( @Context HttpServletRequest request,
                                    @ApiParam(
                                            required = true,
                                            name = "connectionId") @QueryParam( "connectionId") String connectionId,

                                    @ApiParam(
                                            required = false,
                                            name = "properties") @QueryParam( "properties") String properties,

                                    @ApiParam(
                                            name = "testcaseId",
                                            allowMultiple = false,
                                            required = true) @PathParam( "testcaseId") int testcaseId ) {

        List<String> requiredQueryParams = new ArrayList<String>();
        requiredQueryParams.add("connectionId");
        requiredQueryParams.add("testcaseId");
        try {

            if (StringUtils.isNullOrEmpty(properties)) {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId))
                                                                                                .getTestcaseMetainfo(testcaseId,
                                                                                                                     null))
                               .build();
            } else {
                return Response.ok(new DbReader(DbConnectionManager.getReadAccess(connectionId))
                                                                                                .getTestcaseMetainfo(testcaseId,
                                                                                                                     Arrays.asList(properties.split(","))))
                               .build();
            }

        } catch (Exception e) {
            String errorMessage = "Could not get testcase metainfo";
            LOG.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    /*
     * // TODO export statistics and checkpoints, filtered by
     * 
     * - name - CPU, Memory, Some action name - time interval - 1 hour, 1 day -
     * combined - per loader
     * 
     * 
     * // not sure what I meant back then. Is this returning statistics
     * descriptions, statistics only or both?!?
     * 
     * @GET
     * 
     * @Path( "/testcase/{testcaseId: [0-9]*}/statisticsDescriptions")
     * 
     * @ApiOperation( value =
     * "Get testcase system/user statistics descriptions by providing testcaseId",
     * notes =
     * "Get testcase system/user statistics descriptions by providing testcaseId",
     * position = 6)
     * 
     * @ApiResponses( value = { @ApiResponse( code = 200, message =
     * "Successfully obtained system/user statistics descriptions", response =
     * StatisticDescriptionPojo.class),
     * 
     * @ApiResponse( code = 500, message =
     * "Problem obtaining testcase system/user statistics descriptions. The server was not able to process the request"
     * , response = InternalServerErrorPojo.class) })
     * 
     * @Produces( MediaType.APPLICATION_JSON) public Response
     * getTestcaseStatistics( @ApiParam( name = "connectionId", required =
     * true) @QueryParam( "connectionId") final String connectionId,
     * 
     * @ApiParam( name = "testcaseId", allowMultiple = false, required =
     * true) @PathParam( "testcaseId") final int testcaseId ) {
     * 
     * try { if (testcaseId < 0) { throw new
     * IllegalArgumentException("Testcase ID must be equal or greater than zero"); }
     * 
     * IDbReadAccess readAccess = DbConnectionManager.getReadAccess(connectionId);
     * List<StatisticDescriptionPojo> statisticDescriptionsPojos = new
     * ArrayList<>();
     * 
     * List<StatisticDescription> statsDescs =
     * readAccess.getSystemStatisticDescriptions(0, "WHERE tt.testcaseId = " +
     * testcaseId, null, 0, false); for (StatisticDescription description :
     * statsDescs) { StatisticDescriptionPojo descriptionPojo =
     * (StatisticDescriptionPojo) PojoUtils.logEntityToPojo(description);
     * List<StatisticPojo> statisticPojos = new ArrayList<>(); List<Statistic> stats
     * = readAccess.getSystemStatistics(0, testcaseId + "", description.machineId +
     * "", description.statisticTypeId + "", 0, false); for (Statistic statistic :
     * stats) { statisticPojos.add((StatisticPojo)
     * PojoUtils.logEntityToPojo(statistic)); }
     * descriptionPojo.setStatistics((StatisticPojo[]) statisticPojos.toArray(new
     * StatisticPojo[statisticPojos.size()]));
     * statisticDescriptionsPojos.add(descriptionPojo); }
     * 
     * StatisticsDescriptionsPojo pojo = new StatisticsDescriptionsPojo();
     * pojo.setStatisticsDescriptions((StatisticDescriptionPojo[])
     * statisticDescriptionsPojos.toArray( new
     * StatisticDescriptionPojo[statisticDescriptionsPojos.size()]));
     * 
     * return Response.ok(pojo).build();
     * 
     * } catch (Exception e) { String errorMessage =
     * "Could not obtain testcase system/user statistics, by using testcase ID '" +
     * testcaseId + "' and connection ID '" + connectionId; LOG.error(errorMessage,
     * e); return Response.status(Response.Status.INTERNAL_SERVER_ERROR) .entity(new
     * InternalServerErrorPojo(errorMessage, e)) .build(); } }
     */

}
