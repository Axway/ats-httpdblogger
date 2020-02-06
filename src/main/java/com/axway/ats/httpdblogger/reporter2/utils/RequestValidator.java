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
package com.axway.ats.httpdblogger.reporter2.utils;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.axway.ats.core.utils.StringUtils;

/**
 * Validates that the HTTP request has all of the mandatory information
 * */
public class RequestValidator {

    public static void validateQueryParams( HttpServletRequest request, List<String> requiredQueryParameters ) {

        if (request == null) {
            throw new RuntimeException("Request is null!");
        }

        if (StringUtils.isNullOrEmpty(request.getQueryString())) {
            throw new RuntimeException("Request's query string is null/empty!");
        }

        if (requiredQueryParameters != null && !requiredQueryParameters.isEmpty()) {

            for (String queryParam : requiredQueryParameters) {

                if (!RequestValidator.hasQueryParam(request, queryParam)) {
                    throw new RuntimeException("Required query parameter '" + queryParam
                                               + "' not found in the request!");
                }
            }
        }
    }

    public static boolean hasQueryParam( HttpServletRequest request, String queryParam ) {

        if (request != null) {
            return request.getParameterMap().containsKey(queryParam);
        }

        return false;
    }

    public static String getQueryParam( HttpServletRequest request, String queryParam ) {

        if (request != null) {
            return request.getParameter(queryParam);
        } else {
            return null;
        }

    }

}
