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
package com.axway.ats.httpdblogger.reporter2.pojo.response;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel( "Get message response details")
public class MessagePojo {

    @ApiModelProperty(
            required = true,
            value = "id",
            example = "2047",
            position = 1)
    public int    id;

    @ApiModelProperty(
            required = true,
            value = "parentMessageId",
            notes = "If the message's content was more than 3950 characters, ATS will split the message on chunks, acting like linked list. "
                    + "So you can use this parameter to obtain the entire message content. Note that this can be -1, which will mean that the message was not split.",
            example = "1018",
            position = 2)
    public int    parentMessageId;

    @ApiModelProperty(
            required = true,
            value = "messageType",
            notes = "The log level of the message",
            allowableValues = "FATAL, ERROR, WARN, INFO, DEBUG, TRACE",
            position = 3)
    public String messageType;

    @ApiModelProperty(
            required = true,
            value = "machineName",
            notes = "The machine name that this message originated from",
            example = "jenkins slave on 127.0.0.1",
            position = 4)
    public String machineName;

    @ApiModelProperty(
            required = true,
            value = "threadName",
            notes = "The thread name that this message originated from",
            example = "main",
            position = 5)
    public String threadName;

    @ApiModelProperty(
            required = true,
            value = "insertTimestamp",
            notes = "The milliseconds are in UTC, so you will have to additionally convert them to the appropriate time zone",
            example = "1553112000000",
            position = 6)
    public long   insertTimestamp;

    @ApiModelProperty(
            required = true,
            value = "messageContent",
            example = "Begin initial file system snapshot",
            position = 7)
    public String messageContent;

    public MessagePojo() {}

    public int getId() {

        return id;
    }

    public void setId( int id ) {

        this.id = id;
    }

    public int getParentMessageId() {

        return parentMessageId;
    }

    public void setParentMessageId( int parentMessageId ) {

        this.parentMessageId = parentMessageId;
    }

    public String getMessageContent() {

        return messageContent;
    }

    public void setMessageContent( String messageContent ) {

        this.messageContent = messageContent;
    }

    public String getMessageType() {

        return messageType;
    }

    public void setMessageType( String messageType ) {

        this.messageType = messageType;
    }

    public String getMachineName() {

        return machineName;
    }

    public void setMachineName( String machineName ) {

        this.machineName = machineName;
    }

    public String getThreadName() {

        return threadName;
    }

    public void setThreadName( String threadName ) {

        this.threadName = threadName;
    }

    public long getInsertTimestamp() {

        return insertTimestamp;
    }

    public void setInsertTimestamp( long insertTimestamp ) {

        this.insertTimestamp = insertTimestamp;
    }

}
