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
package com.axway.ats.httpdblogger.model;

import java.sql.CallableStatement;
import com.axway.ats.core.dbaccess.DbUtils;
import com.axway.ats.core.dbaccess.mssql.DbConnSQLServer;
import com.axway.ats.httpdblogger.model.pojo.request.InsertMessagePojo;
import com.axway.ats.httpdblogger.model.pojo.request.InsertMessagesPojo;
import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;
import com.axway.ats.log.autodb.io.SQLServerDbWriteAccess;

public class HttpDbLoggerSQLServerWriteAccess extends SQLServerDbWriteAccess implements IHttpDbLoggerWriteAccess {

    public HttpDbLoggerSQLServerWriteAccess( DbConnSQLServer dbConnection ) throws DatabaseAccessException {
        super(dbConnection, false);
    }

    public void insertMessages( InsertMessagesPojo messages ) throws DatabaseAccessException {

        DbEventsCache dbEventsCache = null;

        try {

            dbEventsCache = new DbEventsCache(this);

            int testcaseId = messages.getParentId();

            InsertEventStatementsFactory eventStatementsFactory = new InsertEventStatementsFactory(true);

            for (InsertMessagePojo message : messages.getMessages()) {

                // if no time stamp is provided with this message, use the one from the InsertMessagesPojo
                if (message.getTimestamp() == -1) {
                    message.setTimestamp(messages.getTimestamp());
                }

                message.setTimestamp(inUTC(message.getTimestamp()));

                CallableStatement insertMessageStatement = eventStatementsFactory.getInsertTestcaseMessageStatement(dbEventsCache.getConnection(),
                                                                                                                    message.getMessage(),
                                                                                                                    message.getLogLevel()
                                                                                                                           .toInt(),
                                                                                                                    false,
                                                                                                                    message.getMachineName(),
                                                                                                                    message.getThreadName(),
                                                                                                                    message.getTimestamp(),
                                                                                                                    testcaseId);
                dbEventsCache.addInsertTestcaseMessageEventToBatch(insertMessageStatement);

            }

            dbEventsCache.flushCache();
        } finally {
            DbUtils.closeConnection(dbEventsCache.getConnection());
        }

    }

    public void insertSuiteMessages( InsertMessagesPojo messages ) throws DatabaseAccessException {

        DbEventsCache dbEventsCache = null;

        try {

            int suiteId = messages.getParentId();

            dbEventsCache = new DbEventsCache(this);
            InsertEventStatementsFactory eventStatementsFactory = new InsertEventStatementsFactory(true);

            for (InsertMessagePojo message : messages.getMessages()) {

                // if no time stamp is provided with this message, use the one from the InsertMessagesPojo
                if (message.getTimestamp() == -1) {
                    message.setTimestamp(messages.getTimestamp());
                }

                message.setTimestamp(inUTC(message.getTimestamp()));

                CallableStatement insertMessageStatement = eventStatementsFactory.getInsertSuiteMessageStatement(dbEventsCache.getConnection(),
                                                                                                                 message.getMessage(),
                                                                                                                 message.getLogLevel()
                                                                                                                        .toInt(),
                                                                                                                 false,
                                                                                                                 message.getMachineName(),
                                                                                                                 message.getThreadName(),
                                                                                                                 message.getTimestamp(),
                                                                                                                 suiteId);
                dbEventsCache.addInsertSuiteMessageEventToBatch(insertMessageStatement);

            }

            dbEventsCache.flushCache();

        } finally {
            DbUtils.closeConnection(dbEventsCache.getConnection());
        }

    }

    public void insertRunMessages( InsertMessagesPojo messages ) throws DatabaseAccessException {

        DbEventsCache dbEventsCache = null;

        try {

            int runId = messages.getParentId();

            dbEventsCache = new DbEventsCache(this);
            InsertEventStatementsFactory eventStatementsFactory = new InsertEventStatementsFactory(true);

            for (InsertMessagePojo message : messages.getMessages()) {

                CallableStatement insertMessageStatement = eventStatementsFactory.getInsertRunMessageStatement(dbEventsCache.getConnection(),
                                                                                                               message.getMessage(),
                                                                                                               message.getLogLevel()
                                                                                                                      .toInt(),
                                                                                                               false,
                                                                                                               message.getMachineName(),
                                                                                                               message.getThreadName(),
                                                                                                               message.getTimestamp(),
                                                                                                               runId);
                dbEventsCache.addInsertRunMessageEventToBatch(insertMessageStatement);

            }

            dbEventsCache.flushCache();

        } finally {
            DbUtils.closeConnection(dbEventsCache.getConnection());
        }

    }

}
