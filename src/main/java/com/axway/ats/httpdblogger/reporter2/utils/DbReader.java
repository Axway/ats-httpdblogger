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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.axway.ats.core.reflect.ReflectionUtils;
import com.axway.ats.core.utils.StringUtils;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MessagePojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MessagesPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MetaInfoPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MetaInfosPojo;
import com.axway.ats.httpdblogger.reporter2.runs.pojo.response.RunPojo;
import com.axway.ats.httpdblogger.reporter2.runs.pojo.response.RunsPojo;
import com.axway.ats.httpdblogger.reporter2.suites.pojo.response.SuitePojo;
import com.axway.ats.httpdblogger.reporter2.suites.pojo.response.SuitesPojo;
import com.axway.ats.log.autodb.entities.Message;
import com.axway.ats.log.autodb.entities.Run;
import com.axway.ats.log.autodb.entities.RunMetaInfo;
import com.axway.ats.log.autodb.entities.Suite;
import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;
import com.axway.ats.log.autodb.model.IDbReadAccess;

/**
 * Obtain data from ATS Log DB
 * */
public class DbReader {

    public static enum CompareSign {
        EQUAL, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL;

        public static CompareSign fromString( String value ) {

            if (value.equals("=")) {
                return EQUAL;
            }

            if (value.equals("!=")) {
                return NOT_EQUAL;
            }

            if (value.equals("<")) {
                return LESS_THAN;
            }

            if (value.equals("<=")) {
                return LESS_THAN_OR_EQUAL;
            }

            if (value.equals(">")) {
                return GREATER_THAN;
            }

            if (value.equals(">=")) {
                return GREATER_THAN_OR_EQUAL;
            }

            throw new UnsupportedOperationException("Wrong value " + value);

        }

        public String toString() {

            if (this == EQUAL) {
                return "=";
            }
            if (this == NOT_EQUAL) {
                return "!=";
            }
            if (this == LESS_THAN) {
                return "<";
            }
            if (this == LESS_THAN_OR_EQUAL) {
                return "<=";
            }
            if (this == GREATER_THAN) {
                return ">";
            }
            if (this == GREATER_THAN_OR_EQUAL) {
                return ">=";
            }

            throw new UnsupportedOperationException("Wrong value " + this);

        }
    }

    private IDbReadAccess readAccess;

    public DbReader( IDbReadAccess readAccess ) {

        this.readAccess = readAccess;
    }

    /**
     * @return either {@link RunsPojo} or JSON string
     * */
    public Object getRuns( int from, int to, Map<String, Pair<CompareSign, Object>> whereClauseEntries,
                           List<String> properties ) throws DatabaseAccessException {

        try {
            String whereClause = constructWhereClause(RunPojo.class, whereClauseEntries);
            RunsPojo pojo = null;
            List<Run> runs = readAccess.getRuns(from, to, whereClause, "runId", false, 0);

            if (runs != null && !runs.isEmpty()) {

                pojo = (RunsPojo) PojoUtils.logEntityToPojo(runs);
                if (properties != null && !properties.isEmpty()) {

                    return getSubsetOfPropertiesFromPojos(Arrays.asList(pojo.getRuns()), properties, "runs");
                }

            } else {
                // no data available
                pojo = new RunsPojo();
                pojo.setRuns(new RunPojo[]{});
                return pojo;
            }

            return pojo;

        } catch (DatabaseAccessException e) {
            String errorMessage = String.format("Could not obtain runs from DB. Item range was from %d to %d, where clause was: %s",
                                                from, to, whereClauseEntries);
            throw new DatabaseAccessException(errorMessage, e);
        }
    }

    public Object getRun( int from, int to, Map<String, Pair<CompareSign, Object>> whereClauseEntries,
                          List<String> properties ) throws DatabaseAccessException {

        try {
            String whereClause = constructWhereClause(RunPojo.class, whereClauseEntries);
            RunPojo pojo = null;
            List<Run> runs = readAccess.getRuns(from, to, whereClause, "runId", false, 0);

            if (runs != null && !runs.isEmpty()) {

                pojo = (RunPojo) PojoUtils.logEntityToPojo(runs.get(0));
                if (properties != null && !properties.isEmpty()) {

                    return getSubsetOfPropertiesFromPojos(Collections.singletonList(pojo), properties, "run");
                }

            } else {
                // no data available
                pojo = new RunPojo();
                return pojo;
            }

            return pojo;

        } catch (DatabaseAccessException e) {
            String errorMessage = String.format("Could not obtain run from DB. Item range was from %d to %d, where clause was: %s",
                                                from, to, whereClauseEntries);
            throw new DatabaseAccessException(errorMessage, e);
        }
    }

    public Object getRunMessages( int from, int to, Map<String, Pair<CompareSign, Object>> whereClauseEntries,
                                  List<String> properties ) throws DatabaseAccessException {

        try {
            String whereClause = constructWhereClause(RunPojo.class, whereClauseEntries);
            MessagesPojo pojo = null;
            List<Message> messages = readAccess.getRunMessages(from, to, whereClause, "runId", false, 0);

            if (messages != null && !messages.isEmpty()) {

                pojo = (MessagesPojo) PojoUtils.logEntityToPojo(messages);
                if (properties != null && !properties.isEmpty()) {

                    return getSubsetOfPropertiesFromPojos(Arrays.asList(pojo.getMessages()), properties, "messages");
                }

            } else {
                // no data available
                pojo = new MessagesPojo();
                pojo.setMessages(new MessagePojo[]{});
                return pojo;
            }

            return pojo;

        } catch (DatabaseAccessException e) {
            String errorMessage = String.format("Could not obtain run messages from DB. Item range was from %d to %d, where clause was: %s",
                                                from, to, whereClauseEntries);
            throw new DatabaseAccessException(errorMessage, e);
        }
    }

    public Object getRunMetainfo( int runId,
                                  List<String> properties ) throws DatabaseAccessException {

        try {
            //String whereClause = constructWhereClause(RunPojo.class, whereClauseEntries);
            MetaInfosPojo pojo = null;
            List<RunMetaInfo> messages = readAccess.getRunMetaInfo(runId);

            if (messages != null && !messages.isEmpty()) {

                pojo = (MetaInfosPojo) PojoUtils.logEntityToPojo(messages);
                if (properties != null && !properties.isEmpty()) {

                    return getSubsetOfPropertiesFromPojos(Arrays.asList(pojo.getMetaInfo()), properties, "metainfo");
                }

            } else {
                // no data available
                pojo = new MetaInfosPojo();
                pojo.setMetaInfo(new MetaInfoPojo[]{});
                return pojo;
            }

            return pojo;

        } catch (DatabaseAccessException e) {
            String errorMessage = String.format("Could not obtain run metainfo from DB. run ID was: %d",
                                                runId);
            throw new DatabaseAccessException(errorMessage, e);
        }
    }

    public Object getSuites( int from, int to, Map<String, Pair<CompareSign, Object>> whereClauseEntries,
                             List<String> properties ) throws DatabaseAccessException {

        try {
            String whereClause = constructWhereClause(RunPojo.class, whereClauseEntries);
            SuitesPojo pojo = null;
            List<Suite> entities = readAccess.getSuites(from, to, whereClause, "suiteId", false, 0);

            if (entities != null && !entities.isEmpty()) {

                pojo = (SuitesPojo) PojoUtils.logEntityToPojo(entities);
                if (properties != null && !properties.isEmpty()) {

                    return getSubsetOfPropertiesFromPojos(Arrays.asList(pojo.getSuites()), properties, "suites");
                }

            } else {
                // no data available
                pojo = new SuitesPojo();
                pojo.setSuites(new SuitePojo[]{});
                return pojo;
            }

            return pojo;

        } catch (DatabaseAccessException e) {
            String errorMessage = String.format("Could not obtain suites from DB. Item range was from %d to %d, where clause was: %s",
                                                from, to, whereClauseEntries);
            throw new DatabaseAccessException(errorMessage, e);
        }
    }

    public Object getSuite( int from, int to, Map<String, Pair<CompareSign, Object>> whereClauseEntries,
                            List<String> properties ) throws DatabaseAccessException {

        try {
            String whereClause = constructWhereClause(RunPojo.class, whereClauseEntries);
            SuitePojo pojo = null;
            List<Suite> entities = readAccess.getSuites(from, to, whereClause, "suiteId", false, 0);

            if (entities != null && !entities.isEmpty()) {

                pojo = (SuitePojo) PojoUtils.logEntityToPojo(entities.get(0));
                if (properties != null && !properties.isEmpty()) {

                    return getSubsetOfPropertiesFromPojos(Collections.singletonList(pojo), properties, "suite");
                }

            } else {
                // no data available
                pojo = new SuitePojo();
                return pojo;
            }

            return pojo;

        } catch (DatabaseAccessException e) {
            String errorMessage = String.format("Could not obtain suite from DB. Item range was from %d to %d, where clause was: %s",
                                                from, to, whereClauseEntries);
            throw new DatabaseAccessException(errorMessage, e);
        }
    }

    public Object getSuiteMessages( int from, int to, Map<String, Pair<CompareSign, Object>> whereClauseEntries,
                                    List<String> properties ) throws DatabaseAccessException {

        try {
            String whereClause = constructWhereClause(SuitePojo.class, whereClauseEntries);
            MessagesPojo pojo = null;
            List<Message> messages = readAccess.getSuiteMessages(from, to, whereClause, "suiteId", false, 0);

            if (messages != null && !messages.isEmpty()) {

                pojo = (MessagesPojo) PojoUtils.logEntityToPojo(messages);
                if (properties != null && !properties.isEmpty()) {

                    return getSubsetOfPropertiesFromPojos(Arrays.asList(pojo.getMessages()), properties, "messages");
                }

            } else {
                // no data available
                pojo = new MessagesPojo();
                pojo.setMessages(new MessagePojo[]{});
                return pojo;
            }

            return pojo;

        } catch (DatabaseAccessException e) {
            String errorMessage = String.format("Could not obtain suite messages from DB. Item range was from %d to %d, where clause was: %s",
                                                from, to, whereClauseEntries);
            throw new DatabaseAccessException(errorMessage, e);
        }
    }

    private String getSubsetOfPropertiesFromPojos( List<?> pojos, List<String> properties, String jsonKey ) {

        // get subset of the runs' properties
        List<Map<String, Object>> jsons = new ArrayList<>();

        for (Object pojo : pojos) {
            Map<String, Object> json = new HashMap<String, Object>();
            for (String property : properties) {
                try {
                    if (StringUtils.isNullOrEmpty(property)) {
                        throw new RuntimeException("Pojo property cound not be null/empty!");
                    }
                    property = property.trim();
                    String key = property;
                    Object value = ReflectionUtils.getFieldValue(pojo, property, false);
                    json.put(key, value);
                } catch (Exception e) {
                    if (e.getMessage()
                         .contains("Could not obtain field '" + property + "' from class")) {
                        throw new IllegalArgumentException("" + property
                                                           + " is not a valid " + pojos.get(0).getClass().getName()
                                                           + " property.");
                    } else {
                        throw e;
                    }
                }
            }

            jsons.add(json);
        }
        String json = JsonUtils.constructJson(new String[]{ jsonKey }, new Object[]{ jsons }, true);
        return json;

    }

    private String constructWhereClause( Class<?> pojoClass,
                                         Map<String, Pair<CompareSign, Object>> whereClauseEntries ) {

        if (pojoClass == null) {
            throw new RuntimeException("Pojo class is null!");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("WHERE 1=1");
        if (whereClauseEntries != null && !whereClauseEntries.isEmpty()) {
            for (Map.Entry<String, Pair<CompareSign, Object>> entry : whereClauseEntries.entrySet()) {
                String key = entry.getKey();
                Pair<CompareSign, Object> value = entry.getValue();
                CompareSign cmpSign = value.getLeft();
                Object theValue = value.getRight();
                /*Object object = null;
                try {
                    object = pojoClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("No such pojo '" + pojoClass.getName() + "'");
                }*/

                // will throw a RuntimeException is error occurred
                //ReflectionUtils.getField(object, key, true);
                if (theValue instanceof String) {
                    sb.append(" AND " + key + cmpSign.toString() + "'" + theValue + "'");
                } else {
                    sb.append(" AND " + key + cmpSign.toString() + theValue);
                }
            }
        }

        return sb.toString();
    }

}
