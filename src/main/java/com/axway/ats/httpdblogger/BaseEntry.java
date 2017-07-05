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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.axway.ats.httpdblogger.model.pojo.response.ResponsePojo;

public class BaseEntry {

    protected org.apache.log4j.Logger log;

    public BaseEntry() {
        log = org.apache.log4j.Logger.getLogger( this.getClass() );
    }

    protected void logInfo(
                            String message ) {

        if( log.isDebugEnabled() ) {
            log.debug( message );
        }
    }

    protected void logInfo(
                            HttpServletRequest request,
                            String message ) {

        if( log.isDebugEnabled() ) {
            log.debug( "[" + request.getRemoteAddr() /* + ":" + request.getRemotePort() */ + "] " + message );
        }
    }

    protected Response returnError(
                                    String message ) {

        log.error( message );
        return Response.serverError().entity( new ResponsePojo( message, null ) ).build();
    }

    protected Response returnError(
                                    HttpServletRequest request,
                                    String message ) {

        message = "[" + request.getRemoteAddr() + "] " + message;

        log.error( message );
        return Response.serverError().entity( new ResponsePojo( message, null ) ).build();
    }

    protected Response returnError(
                                    Exception e,
                                    String message ) {

        log.error( message, e );
        return Response.serverError().entity( new ResponsePojo( message, e ) ).build();
    }

    protected Response returnError(
                                    HttpServletRequest request,
                                    Exception e,
                                    String message ) {

        message = "[" + request.getRemoteAddr() + "] " + message;

        log.error( message, e );
        return Response.serverError().entity( new ResponsePojo( message, e ) ).build();
    }
}
