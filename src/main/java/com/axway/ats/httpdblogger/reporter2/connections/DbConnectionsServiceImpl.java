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
package com.axway.ats.httpdblogger.reporter2.connections;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.axway.ats.core.validation.Validate;
import com.axway.ats.core.validation.ValidationType;
import com.axway.ats.core.validation.Validator;
import com.axway.ats.httpdblogger.reporter2.connections.pojo.response.CreateDbConnectionPojo;
import com.axway.ats.httpdblogger.reporter2.connections.pojo.response.DeleteDbConnectionPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.InternalServerErrorPojo;
import com.axway.ats.httpdblogger.reporter2.utils.DbConnectionManager;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path( "reporter2")
@Api( value = "/reporter2/connection", description = "Manage DB connection details")
public class DbConnectionsServiceImpl {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DbConnectionsServiceImpl.class);

    @PUT
    @Path( "/connection")
    @ApiOperation(
            value = "Create DB connection",
            notes = "Create DB connection by providing db connection parameters",
            position = 1)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully created DB connection",
                    response = CreateDbConnectionPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem creating DB connection. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Consumes( MediaType.APPLICATION_JSON)
    @Produces( MediaType.APPLICATION_JSON)
    public Response
            createConnection( @ApiParam(
                    name = "DB connection details",
                    required = true) com.axway.ats.httpdblogger.reporter2.connections.pojo.request.CreateDbConnectionPojo connectionPojo ) {

        try {
            if (connectionPojo == null) {
                throw new RuntimeException("POJO mapping error. POJO is null");
            }
            // validate POJO
            connectionPojo.validate();

            // create new DB connection
            String uuid = DbConnectionManager.createDbConnection(connectionPojo);

            return Response.ok(new CreateDbConnectionPojo(uuid)).build();

        } catch (Exception e) {
            String errorMessage = "Could not create DB connection.";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

    @DELETE
    @Path( "/connection/{connectionId: .*}")
    @ApiOperation(
            value = "Delete DB connection",
            notes = "Delete DB connection by providing the DB connection ID",
            position = 2)
    @ApiResponses(
            value = { @ApiResponse(
                    code = 200,
                    message = "Successfully deleted DB connection",
                    response = DeleteDbConnectionPojo.class),
                      @ApiResponse(
                              code = 500,
                              message = "Problem deleting DB connection. The server was not able to process the request",
                              response = InternalServerErrorPojo.class)
            })
    @Produces( MediaType.APPLICATION_JSON)
    public Response deleteSession(
                                   @ApiParam( name = "DB connectionId", required = true) @Validate(
                                           name = "DB connectionId",
                                           type = ValidationType.STRING_NOT_EMPTY) @PathParam( "connectionId") final String connectionId ) {

        try {
            Validator validator = new Validator();
            validator.validateMethodParameters(new Object[]{ connectionId });

            DbConnectionManager.removeDbConnection(connectionId);
            return Response.ok(new DeleteDbConnectionPojo(connectionId)).build();
        } catch (Exception e) {
            String errorMessage = "Could not delete DB connection";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new InternalServerErrorPojo(errorMessage, e))
                           .build();
        }
    }

}
