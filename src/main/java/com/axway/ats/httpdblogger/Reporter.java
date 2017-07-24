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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.httpdblogger.model.DbRequestProcessor;
import com.axway.ats.log.autodb.entities.Run;
import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * The REPORTER entry point for this HTTP application. 
 * All available public methods are listed in this class.
 */
@Path("reporter")
@Api(value = "/reporter")
public class Reporter extends BaseEntry {

    // all columns as user enters them, case not sensitive
    private static final Set<String> ALL_RUN_COLUMNS;

    private int                      internalDbVersion;

    static {
        ALL_RUN_COLUMNS = new HashSet<String>();
        ALL_RUN_COLUMNS.add( "run" );
        ALL_RUN_COLUMNS.add( "product" );
        ALL_RUN_COLUMNS.add( "version" );
        ALL_RUN_COLUMNS.add( "build" );
        ALL_RUN_COLUMNS.add( "os" );
        ALL_RUN_COLUMNS.add( "scenariostotal" );
        ALL_RUN_COLUMNS.add( "testcasestotal" );
        ALL_RUN_COLUMNS.add( "scenariosfailed" );
        ALL_RUN_COLUMNS.add( "testcasesfailed" );
        ALL_RUN_COLUMNS.add( "testcasesskipped" );
        ALL_RUN_COLUMNS.add( "datestart" );
        ALL_RUN_COLUMNS.add( "dateend" );
        ALL_RUN_COLUMNS.add( "duration" );
        ALL_RUN_COLUMNS.add( "usernote" );
        ALL_RUN_COLUMNS.add( "hostname" );
    }

    @GET
    @Path("getRuns")
    @ApiOperation(value = "Get runs", notes = "Returns a list of RUNs that matched a certain creteria", position = 1)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully got runs info", response = Response.class),
                            @ApiResponse(code = 500, message = "Internal server error", response = Response.class) })
    @Produces(MediaType.APPLICATION_JSON)
    public Response
            getRuns( @Context HttpServletRequest request,
                     @ApiParam(value = "DB name", required = true) @QueryParam("dbName") String dbName,
                     @ApiParam(value = "DB host. Default is 127.0.0.1", required = false) @DefaultValue("127.0.0.1") @QueryParam("dbHost") String dbHost,
                     @ApiParam(value = "DB user. Default is AutoUser", required = false) @DefaultValue("AutoUser") @QueryParam("dbUser") String dbUser,
                     @ApiParam(value = "DB password. Default is AtsPassword", required = false) @DefaultValue("AtsPassword") @QueryParam("dbPassword") String dbPassword,
                     @ApiParam(value = "Columns to show from the result set as they show up in Test Explorer UI. "
                                       + "Possible values: name, product, version, build, os, "
                                       + "scenariosTotal, scenariosFailed, testcasesTotal, testcasesfailed, testcasesSkipped, "
                                       + "dateStart, dateEnd, duration, userNote, hostName. "
                                       + "If not specified - all columns are returned", required = false) @QueryParam("columns") String columns,
                     @ApiParam(value = "Max number of runs to return. Default is 1000", required = false) @DefaultValue("1000") @QueryParam("top") int top,
                     @ApiParam(value = "Token contained in the run name.", required = false) @QueryParam("name") String name,
                     @ApiParam(value = "Token contained in the product name.", required = false) @QueryParam("product") String product,
                     @ApiParam(value = "Token contained in the version name.", required = false) @QueryParam("version") String version,
                     @ApiParam(value = "Token contained in the build name.", required = false) @QueryParam("build") String build,
                     @ApiParam(value = "Token contained in the os name.", required = false) @QueryParam("os") String os,
                     @ApiParam(value = "Token containing the time offset (in milliseconds) from UTC of the caller.", required = false) @QueryParam("timeOffset") String timeOffset,
                     @ApiParam(value = "Token containing the current request time (in milliseconds).", required = false) @QueryParam("currentTimestamp") String currentTimestamp ) {

        if( dbName == null ) {
            // database name is mandatory
            return returnError( request, "No 'dbName' parameter provided" );
        }
        
        //
        if(StringUtils.isNullOrEmpty( timeOffset )){
            timeOffset = "0";
        }
        
        if(StringUtils.isNullOrEmpty( currentTimestamp )){
            currentTimestamp = System.currentTimeMillis()+"";
        }

        if( top < 1 ) {
            // the number to runs to fetch must be positive
            top = 1000;
        }

        String whereClause = constructWhereClause( name, product, version, build, os );

        DbRequestProcessor dbRequestProcessor = null;

        try {
            dbRequestProcessor = new DbRequestProcessor();
            List<Run> runs = dbRequestProcessor.getRuns( dbHost, dbName, dbUser, dbPassword, whereClause,
                                                         top, timeOffset );

            internalDbVersion = dbRequestProcessor.getDbInternalVersion();

            String runsInfo = toJsonFormat( runs, columns, currentTimestamp );
            logInfo( request, "Found the folloing runs:\n" + runsInfo );

            return Response.ok( runsInfo ).build();
        } catch( DatabaseAccessException e ) {
            return returnError( e, "Unable to get run " + whereClause );
        }
    }

    private String constructWhereClause( String name, String product, String version, String build,
                                         String os ) {

        StringBuilder where = new StringBuilder();

        if( !StringUtils.isNullOrEmpty( name ) ) {
            where.append( " AND runName LIKE '%" + escapeSqlSearchValue( name ) + "%' escape '\\'" );
        }
        if( !StringUtils.isNullOrEmpty( product ) ) {
            where.append( " AND productName LIKE '%" + escapeSqlSearchValue( product ) + "%' escape '\\'" );
        }
        if( !StringUtils.isNullOrEmpty( version ) ) {
            where.append( " AND versionName LIKE '%" + escapeSqlSearchValue( version ) + "%' escape '\\'" );
        }
        if( !StringUtils.isNullOrEmpty( build ) ) {
            where.append( " AND buildName LIKE '%" + escapeSqlSearchValue( build ) + "%' escape '\\'" );
        }
        if( !StringUtils.isNullOrEmpty( os ) ) {
            where.append( " AND OS LIKE '%" + escapeSqlSearchValue( os ) + "%' escape '\\'" );
        }

        if( where.length() > 0 ) {
            where.delete( 0, " AND".length() );
            where.insert( 0, "WHERE" );
        } else {
            // match all
            where.append( "WHERE 1=1" );
        }
        return where.toString();
    }

    private String escapeSqlSearchValue( String searchValue ) {

        // replacing the '\' and '%'
        searchValue = searchValue.replace( "\\", "\\\\" ).replace( "%", "\\%" );
        // replace '*' with '%' to support wild card search and then escape the special characters
        searchValue = searchValue.replace( '*', '%' )
                                 .replace( "'", "''" )
                                 .replace( "\"", "\\\"" )
                                 .replace( "-", "\\-" )
                                 .replace( "!", "\\!" )
                                 .replace( "&", "\\&" )
                                 .replace( "$", "\\$" )
                                 .replace( "?", "\\?" )
                                 .replace( "[", "\\[" )
                                 .replace( "]", "\\]" );
        return searchValue;
    }

    /**
     * Format the runs info into JSON format.
     * Currently we do not use a third party library for this conversion as it is very simple for now.
     * 
     * @param runs
     * @param columnsToSelectParameter
     * @return
     */
    private String toJsonFormat( List<Run> runs, String columnsToSelectParameter, String currentTimestamp ) {

        // all columns are pulled from the DB, 
        // user may desire to narrow the list of columns displayed
        Set<String> columns = new HashSet<String>();
        if( columnsToSelectParameter != null ) {
            for( String column : columnsToSelectParameter.split( "," ) ) {
                columns.add( column.trim().toLowerCase() );
            }
        }
        if( columns.size() == 0 ) {
            columns = ALL_RUN_COLUMNS;
        }

        /**@BackwardCompatibility**/
        if( internalDbVersion < 1 ) {
            columns.remove( new String( "hostname" ) );

        } else if( internalDbVersion >= 1 ) {
            columns.add( new String( "hostname" ) );
        }

        StringBuilder sb = new StringBuilder();
        sb.append( "{\"runs\":[" );
        for( Run run : runs ) {
            sb.append( "\n{" );

            if( columns.contains( "run" ) ) {
                sb.append( "\"Run\":\"" );
                sb.append( run.runName );
                sb.append( "\", " );
            }

            if( columns.contains( "id" ) ) {
                sb.append( "Id\":\"" );
                sb.append( run.runId );
                sb.append( "\", " );
            }

            if( columns.contains( "product" ) ) {
                sb.append( "\"Product\":\"" );
                sb.append( run.productName );
                sb.append( "\", " );
            }

            if( columns.contains( "version" ) ) {
                sb.append( "\"Version\":\"" );
                sb.append( run.versionName );
                sb.append( "\", " );
            }

            if( columns.contains( "build" ) ) {
                sb.append( "\"Build\":\"" );
                sb.append( run.buildName );
                sb.append( "\", " );
            }

            if( columns.contains( "os" ) ) {
                sb.append( "\"OS\":\"" );
                sb.append( run.os );
                sb.append( "\", " );
            }

            if( columns.contains( "scenariosTotal".toLowerCase() ) ) {
                sb.append( "\"Total test scenarios\":\"" );
                sb.append( run.scenariosTotal );
                sb.append( "\", " );
            }

            if( columns.contains( "testcasesTotal".toLowerCase() ) ) {
                sb.append( "\"Total test cases\":\"" );
                sb.append( run.testcasesTotal );
                sb.append( "\", " );
            }

            if( columns.contains( "scenariosFailed".toLowerCase() ) ) {
                sb.append( "\"Failed test scenarios\":\"" );
                sb.append( run.scenariosFailed );
                sb.append( "\", " );
            }

            if( columns.contains( "testcasesFailed".toLowerCase() ) ) {
                sb.append( "\"Failed test cases\":\"" );
                sb.append( run.testcasesFailed );
                sb.append( "\", " );
            }

            if( columns.contains( "testcasesSkipped".toLowerCase() ) ) {
                sb.append( "\"Skipped test cases\":\"" );
                sb.append( run.testcasesSkipped );
                sb.append( "\", " );
            }

            if( columns.contains( "dateStart".toLowerCase() ) ) {
                sb.append( "\"Start\":\"" );
                sb.append( run.getDateStart() );
                sb.append( "\", " );
            }

            if( columns.contains( "dateEnd".toLowerCase() ) ) {
                sb.append( "\"End\":\"" );
                sb.append( run.getDateEnd() );
                sb.append( "\", " );
            }

            if( columns.contains( "duration" ) ) {
                sb.append( "\"Duration\":\"" );
                sb.append( run.getDuration( Long.parseLong( currentTimestamp ) ) );
                sb.append( "\", " );
            }

            if( columns.contains( "userNote".toLowerCase() ) ) {
                sb.append( "\"User note\":\"" );
                sb.append( run.userNote );
                sb.append( "\", " );
            }

            if( columns.contains( "hostName".toLowerCase() ) ) {
                sb.append( "\"Executor host\":\"" );
                sb.append( run.hostName );
                sb.append( "\", " );
            }

            sb.delete( sb.length() - 3, sb.length() );
            sb.append( "\"}" );
        }
        sb.append( "\n]}" );

        return sb.toString();
    }
}
