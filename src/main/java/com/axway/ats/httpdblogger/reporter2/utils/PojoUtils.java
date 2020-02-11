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

import java.lang.reflect.Array;
import java.util.List;

import com.axway.ats.core.reflect.ReflectionUtils;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MessagePojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MessagesPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MetaInfoPojo;
import com.axway.ats.httpdblogger.reporter2.pojo.response.MetaInfosPojo;
import com.axway.ats.httpdblogger.reporter2.runs.pojo.response.RunPojo;
import com.axway.ats.httpdblogger.reporter2.runs.pojo.response.RunsPojo;
import com.axway.ats.httpdblogger.reporter2.scenarios.pojo.response.ScenarioPojo;
import com.axway.ats.httpdblogger.reporter2.scenarios.pojo.response.ScenariosPojo;
import com.axway.ats.httpdblogger.reporter2.suites.pojo.response.SuitePojo;
import com.axway.ats.httpdblogger.reporter2.suites.pojo.response.SuitesPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointSummaryPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointsPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.CheckpointsSummariesPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.LoadQueuePojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.LoadQueuesPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.StatisticDescriptionPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.StatisticPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.StatisticsDescriptionsPojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.TestcasePojo;
import com.axway.ats.httpdblogger.reporter2.testcases.pojo.response.TestcasesPojo;
import com.axway.ats.log.autodb.entities.Checkpoint;
import com.axway.ats.log.autodb.entities.CheckpointSummary;
import com.axway.ats.log.autodb.entities.DbEntity;
import com.axway.ats.log.autodb.entities.LoadQueue;
import com.axway.ats.log.autodb.entities.Message;
import com.axway.ats.log.autodb.entities.Run;
import com.axway.ats.log.autodb.entities.RunMetaInfo;
import com.axway.ats.log.autodb.entities.Scenario;
import com.axway.ats.log.autodb.entities.ScenarioMetaInfo;
import com.axway.ats.log.autodb.entities.Statistic;
import com.axway.ats.log.autodb.entities.StatisticDescription;
import com.axway.ats.log.autodb.entities.Suite;
import com.axway.ats.log.autodb.entities.Testcase;
import com.axway.ats.log.autodb.entities.TestcaseMetainfo;

public class PojoUtils {

    public static Object logEntityToPojo( Object entity ) {

        boolean isList = false;
        boolean isArray = false;

        if (entity == null) {
            throw new IllegalArgumentException("Entity could not be null");
        }

        if (entity instanceof List) {
            isList = true;
            if ( ((List<?>) entity).isEmpty()) {
                throw new IllegalArgumentException("Entity could not be empty List");
            }
        }

        if (entity.getClass().isArray()) {
            if (Array.getLength(entity) <= 0) {
                throw new IllegalArgumentException("Entity could not be empty array");
            } else {
                isArray = true;
                // TODO convert array to list?
            }
        }

        if (entity instanceof Run) {

            return runToPojo((Run) entity);
        } else if (isList && ((List<?>) entity).get(0) instanceof Run) {

            return runListToPojo((List<Run>) entity);

        } else if (entity instanceof Suite) {

            return suiteToPojo((Suite) entity);
        } else if (isList && ((List<?>) entity).get(0) instanceof Suite) {

            return suiteListToPojo((List<Suite>) entity);
        } else if (entity instanceof Scenario) {
            return scenarioToPojo((Scenario) entity);
        } else if (isList
                   && ((List<?>) entity).get(0) instanceof Scenario) {

            return scenarioListToPojo((List<Scenario>) entity);
        } else if (entity instanceof Testcase) {

            return testcaseToPojo((Testcase) entity);
        } else if (isList
                   && ((List<?>) entity).get(0) instanceof Testcase) {

            return testcaseListToPojo((List<Testcase>) entity);
        } else if (entity instanceof Message) {

            return messageToPojo((Message) entity);
        } else if (isList
                   && ((List<?>) entity).get(0) instanceof Message) {

            return messageListToPojo((List<Message>) entity);
        } else if (entity instanceof RunMetaInfo || entity instanceof ScenarioMetaInfo
                   || entity instanceof TestcaseMetainfo) {

            return metainfoToPojo(entity);

        } else if (isList && ( ((List<?>) entity).get(0) instanceof RunMetaInfo
                               || ((List<?>) entity).get(0) instanceof ScenarioMetaInfo
                               || ((List<?>) entity).get(0) instanceof TestcaseMetainfo)) {

            return metainfoListToPojo(entity);

        } else if (entity instanceof StatisticDescription) {

            return statisticDescriptionToPojo((StatisticDescription) entity);

        } else if (isList && ((List<?>) entity).get(0) instanceof StatisticDescription) {

            return statisticDescriptionListToPojo((List<StatisticDescription>) entity);
        } else if (entity instanceof Statistic) {

            return statisticToPojo((Statistic) entity);
        } else if (isList && ((List<?>) entity).get(0) instanceof Statistic) {

            return statisticListToPojo((List<Statistic>) entity);

        } else if (entity instanceof LoadQueue) {

            return loadQueueToPojo((LoadQueue) entity);
        } else if (isList && ((List<?>) entity).get(0) instanceof LoadQueue) {

            return loadQueueListToPojo((List<LoadQueue>) entity);
        } else if (entity instanceof CheckpointSummary) {
            return checkpointSummaryToPojo((CheckpointSummary) entity);
        } else if (isList && ((List<?>) entity).get(0) instanceof CheckpointSummary) {
            return checkpointSummaryListToPojo((List<CheckpointSummary>) entity);
        } else if (entity instanceof Checkpoint) {
            return checkpointToPojo((Checkpoint) entity);
        } else if (isList && ((List<?>) entity).get(0) instanceof Checkpoint) {
            return checkpointListToPojo((List<Checkpoint>) entity);
        }

        throw new UnsupportedOperationException("Entity '" + entity.getClass().getName()
                                                + "' does not have associated POJO");

    }

    private static CheckpointsPojo checkpointListToPojo( List<Checkpoint> checkpoints ) {

        CheckpointsPojo checkpointsPojo = new CheckpointsPojo();
        CheckpointPojo[] pojos = new CheckpointPojo[checkpoints.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (CheckpointPojo) logEntityToPojo(checkpoints.get(i));
        }

        checkpointsPojo.setCheckpoints(pojos);

        return checkpointsPojo;
    }

    private static CheckpointPojo checkpointToPojo( Checkpoint checkpoint ) {

        CheckpointPojo pojo = new CheckpointPojo();
        pojo.id = checkpoint.checkpointId;
        pojo.checkpointSummaryId = checkpoint.checkpointSummaryId;
        pojo.name = checkpoint.name;
        pojo.responseTime = checkpoint.responseTime;
        pojo.transferRate = checkpoint.transferRate;
        pojo.transferRateUnit = checkpoint.transferRateUnit;
        pojo.result = checkpoint.result;
        pojo.insertTimestamp = checkpoint.getEndTimestamp();
        return pojo;
    }

    private static CheckpointsSummariesPojo

            checkpointSummaryListToPojo( List<CheckpointSummary> checkpointsSummaries ) {

        CheckpointsSummariesPojo checkpointsSummariesPojo = new CheckpointsSummariesPojo();
        CheckpointSummaryPojo[] pojos = new CheckpointSummaryPojo[checkpointsSummaries.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (CheckpointSummaryPojo) logEntityToPojo(checkpointsSummaries.get(i));
        }

        checkpointsSummariesPojo.setCheckpointsSummaries(pojos);

        return checkpointsSummariesPojo;
    }

    private static CheckpointSummaryPojo checkpointSummaryToPojo( CheckpointSummary checkpointSummary ) {

        CheckpointSummaryPojo pojo = new CheckpointSummaryPojo();
        pojo.id = checkpointSummary.checkpointSummaryId;
        pojo.loadQueueId = checkpointSummary.loadQueueId;
        pojo.name = checkpointSummary.name;
        pojo.numRunning = checkpointSummary.numRunning;
        pojo.numPassed = checkpointSummary.numPassed;
        pojo.numFailed = checkpointSummary.numFailed;
        pojo.numTotal = checkpointSummary.numTotal;
        pojo.minResponseTime = checkpointSummary.minResponseTime;
        pojo.avgResponseTime = checkpointSummary.avgResponseTime;
        pojo.maxResponseTime = checkpointSummary.maxResponseTime;
        pojo.minTransferRate = checkpointSummary.minTransferRate;
        pojo.avgTransferRate = checkpointSummary.avgTransferRate;
        pojo.maxTransferRate = checkpointSummary.maxTransferRate;
        pojo.transferRateUnit = checkpointSummary.transferRateUnit;
        return pojo;
    }

    private static LoadQueuesPojo loadQueueListToPojo( List<LoadQueue> loadQueues ) {

        LoadQueuesPojo loadQueuesPojo = new LoadQueuesPojo();
        LoadQueuePojo[] pojos = new LoadQueuePojo[loadQueues.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (LoadQueuePojo) logEntityToPojo(loadQueues.get(i));
        }

        loadQueuesPojo.setLoadQueues(pojos);

        return loadQueuesPojo;
    }

    private static LoadQueuePojo loadQueueToPojo( LoadQueue loadQueue ) {

        LoadQueuePojo loadQueuePojo = new LoadQueuePojo();

        loadQueuePojo.hostsList = loadQueue.hostsList;
        loadQueuePojo.name = loadQueue.name;
        loadQueuePojo.id = loadQueue.loadQueueId;
        loadQueuePojo.state = loadQueue.state;
        loadQueuePojo.threadingPattern = loadQueue.threadingPattern;
        loadQueuePojo.numberThreads = loadQueue.numberThreads;
        loadQueuePojo.result = loadQueue.result;
        loadQueuePojo.sequence = loadQueue.sequence;
        loadQueuePojo.setStartTimestamp(loadQueue.getStartTimestamp());
        loadQueuePojo.setEndTimestamp(loadQueue.getEndTimestamp());
        loadQueuePojo.setDuration(loadQueue.getDurationAsString(loadQueue.getEndTimestamp()));

        return loadQueuePojo;
    }

    // why array and not array pojo wrapper like the rest?!?
    private static StatisticPojo[] statisticListToPojo( List<Statistic> statistics ) {

        StatisticPojo[] statisticsPojo = new StatisticPojo[statistics.size()];
        int size = statisticsPojo.length;
        for (int i = 0; i < size; i++) {
            Statistic statistic = statistics.get(i);
            statisticsPojo[i] = (StatisticPojo) PojoUtils.logEntityToPojo(statistic);
        }

        return statisticsPojo;
    }

    private static StatisticPojo statisticToPojo( Statistic statistic ) {

        StatisticPojo statisticPojo = new StatisticPojo();

        statisticPojo.setValue(statistic.value);
        statisticPojo.setTransferSize(statistic.transferSize);
        statisticPojo.setTimestamp(statistic.getStartTimestamp());

        return statisticPojo;
    }

    private static StatisticsDescriptionsPojo statisticDescriptionListToPojo( List<StatisticDescription> statsDescs ) {

        StatisticsDescriptionsPojo statisticsDescriptionsPojo = new StatisticsDescriptionsPojo();
        StatisticDescriptionPojo[] pojos = new StatisticDescriptionPojo[statsDescs.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (StatisticDescriptionPojo) logEntityToPojo(statsDescs.get(i));
        }

        statisticsDescriptionsPojo.setStatisticsDescriptions(pojos);

        return statisticsDescriptionsPojo;
    }

    private static StatisticDescriptionPojo statisticDescriptionToPojo( StatisticDescription statisticDescription ) {

        StatisticDescriptionPojo pojo = new StatisticDescriptionPojo();

        pojo.setAvgValue(statisticDescription.avgValue);
        pojo.setMachineId(statisticDescription.machineId);
        pojo.setMachineName(statisticDescription.machineName);
        pojo.setMaxValue(statisticDescription.maxValue);
        pojo.setMinValue(statisticDescription.minValue);
        pojo.setNumberMeasurements(statisticDescription.numberMeasurements);
        pojo.setParams(statisticDescription.params);
        pojo.setParent(statisticDescription.parent);
        pojo.setQueueName(statisticDescription.queueName);
        pojo.setStatisticName(statisticDescription.statisticName);
        pojo.setStatisticTypeId(statisticDescription.statisticTypeId);
        pojo.setUnit(statisticDescription.unit);
        pojo.setInternalName(statisticDescription.internalName);

        return pojo;
    }

    private static MetaInfosPojo metainfoListToPojo( Object entity ) {

        List<DbEntity> metaInfos = (List<DbEntity>) entity;
        MetaInfosPojo metaInfosPojo = new MetaInfosPojo();
        MetaInfoPojo[] pojos = new MetaInfoPojo[metaInfos.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (MetaInfoPojo) logEntityToPojo(metaInfos.get(i));
        }

        metaInfosPojo.setMetaInfo(pojos);

        return metaInfosPojo;
    }

    private static MetaInfoPojo metainfoToPojo( Object metainfo ) {

        MetaInfoPojo metaInfoPojo = new MetaInfoPojo();
        metaInfoPojo.setId((int) ReflectionUtils.getFieldValue(metainfo, "metaInfoId", false));
        if (metainfo instanceof RunMetaInfo) {
            metaInfoPojo.setParentId( ((RunMetaInfo) metainfo).runId);
        }
        if (metainfo instanceof ScenarioMetaInfo) {
            metaInfoPojo.setParentId( ((ScenarioMetaInfo) metainfo).scenarioId);
        }
        if (metainfo instanceof TestcaseMetainfo) {
            metaInfoPojo.setParentId( ((TestcaseMetainfo) metainfo).testcaseId);
        }
        metaInfoPojo.setName((String) ReflectionUtils.getFieldValue(metainfo, "name", false));
        metaInfoPojo.setValue((String) ReflectionUtils.getFieldValue(metainfo, "value", false));

        return metaInfoPojo;

    }

    private static MessagesPojo messageListToPojo( List<Message> messages ) {

        MessagesPojo messagesPojo = new MessagesPojo();
        MessagePojo[] pojos = new MessagePojo[messages.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (MessagePojo) logEntityToPojo(messages.get(i));
        }

        messagesPojo.setMessages(pojos);

        return messagesPojo;
    }

    private static TestcasesPojo testcaseListToPojo( List<Testcase> testcases ) {

        TestcasesPojo testcasesPojo = new TestcasesPojo();
        TestcasePojo[] pojos = new TestcasePojo[testcases.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (TestcasePojo) logEntityToPojo(testcases.get(i));
        }

        testcasesPojo.setTestcases(pojos);

        return testcasesPojo;
    }

    private static ScenariosPojo scenarioListToPojo( List<Scenario> scenarios ) {

        ScenariosPojo scenariosPojo = new ScenariosPojo();
        ScenarioPojo[] pojos = new ScenarioPojo[scenarios.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (ScenarioPojo) logEntityToPojo(scenarios.get(i));
        }

        scenariosPojo.setScenarios(pojos);

        return scenariosPojo;
    }

    private static SuitesPojo suiteListToPojo( List<Suite> suites ) {

        SuitesPojo suitesPojo = new SuitesPojo();
        SuitePojo[] pojos = new SuitePojo[suites.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (SuitePojo) logEntityToPojo(suites.get(i));
        }

        suitesPojo.setSuites(pojos);

        return suitesPojo;
    }

    private static RunsPojo runListToPojo( List<Run> runs ) {

        RunsPojo runsPojo = new RunsPojo();
        RunPojo[] pojos = new RunPojo[runs.size()];

        for (int i = 0; i < pojos.length; i++) {
            pojos[i] = (RunPojo) logEntityToPojo(runs.get(i));
        }

        runsPojo.setRuns(pojos);
        return runsPojo;
    }

    private static MessagePojo messageToPojo( Message message ) {

        MessagePojo messagePojo = new MessagePojo();
        messagePojo.setId(message.messageId);
        messagePojo.setParentMessageId(message.parentMessageId);
        messagePojo.setMessageType(message.messageType);
        messagePojo.setMachineName(message.machineName);
        messagePojo.setThreadName(message.threadName);
        messagePojo.setInsertTimestamp(message.getStartTimestamp());
        messagePojo.setMessageContent(message.messageContent);

        return messagePojo;
    }

    private static TestcasePojo testcaseToPojo( Testcase testcase ) {

        TestcasePojo testcasePojo = new TestcasePojo();
        testcasePojo.setId(Integer.parseInt(testcase.testcaseId));
        testcasePojo.setScenarioId(Integer.parseInt(testcase.scenarioId));
        testcasePojo.setSuiteId(Integer.parseInt(testcase.suiteId));
        testcasePojo.setName(testcase.name);
        testcasePojo.setState(testcase.state);
        testcasePojo.setStartTimestamp(testcase.getStartTimestamp());
        testcasePojo.setEndTimestamp(testcase.getEndTimestamp());
        testcasePojo.setDuration(testcase.getDurationAsString(0));
        testcasePojo.setUserNote(testcase.userNote);
        return testcasePojo;
    }

    private static ScenarioPojo scenarioToPojo( Scenario scenario ) {

        ScenarioPojo scenarioPojo = new ScenarioPojo();
        scenarioPojo.setId(Integer.parseInt(scenario.scenarioId));
        scenarioPojo.setSuiteId(Integer.parseInt(scenario.suiteId));
        scenarioPojo.setName(scenario.name);
        scenarioPojo.setDescription(scenario.description);
        scenarioPojo.setUserNote(scenario.userNote);
        scenarioPojo.setState(scenario.state);
        scenarioPojo.setTestcasesTotal(scenario.testcasesTotal);
        scenarioPojo.setTestcasesFailed(scenario.testcasesFailed);
        scenarioPojo.setTestcasesPassedPercent(scenario.testcasesPassedPercent);

        float testcasesPassedPercentage = (Integer.parseInt(scenario.testcasesPassedPercent.replace("%", ""))
                                           / 100.0f);
        int testcasesPassed = (int) Math.ceil(scenario.testcasesTotal * testcasesPassedPercentage);

        int testcasesSkipped = (scenarioPojo.getTestcasesTotal() - (testcasesPassed + scenario.testcasesFailed));
        while (testcasesSkipped < 0) {
            testcasesSkipped = (scenarioPojo.getTestcasesTotal() - (testcasesPassed + scenario.testcasesFailed));
            testcasesPassed--;
        }
        scenarioPojo.setTestcasesSkipped(testcasesSkipped);
        scenarioPojo.setTestcasesPassed(testcasesPassed);

        scenarioPojo.setStartTimestamp(scenario.getStartTimestamp());
        scenarioPojo.setEndTimestamp(scenario.getEndTimestamp());
        scenarioPojo.setDuration(scenario.getDurationAsString(0));

        return scenarioPojo;
    }

    private static SuitePojo suiteToPojo( Suite suite ) {

        SuitePojo suitePojo = new SuitePojo();
        suitePojo.setId(Integer.parseInt(suite.suiteId));
        suitePojo.setRunId(Integer.parseInt(suite.runId));
        suitePojo.setName(suite.name);
        suitePojo.setUserNote(suite.userNote);
        suitePojo.setStartTimestamp(suite.getStartTimestamp());
        suitePojo.setEndTimestamp(suite.getEndTimestamp());
        suitePojo.setDuration(suite.getDurationAsString(0));
        suitePojo.setTestcasesTotal(suite.testcasesTotal);
        suitePojo.setTestcasesPassed(suite.testcasesTotal - (suite.testcasesFailed + suite.testcasesSkipped));
        suitePojo.setTestcasesFailed(suite.testcasesFailed);
        suitePojo.setTestcasesSkipped(suite.testcasesSkipped);

        suitePojo.setScenariosTotal(suite.scenariosTotal);
        suitePojo.setScenariosPassed(suite.scenariosTotal - (suite.scenariosFailed + suite.scenariosSkipped));
        suitePojo.setScenariosFailed(suite.scenariosFailed);
        suitePojo.setScenariosSkipped(suite.scenariosSkipped);
        suitePojo.setTestcasesPassedPercent(suite.testcasesPassedPercent);
        suitePojo.setPackageName(suite.packageName);

        return suitePojo;
    }

    private static RunPojo runToPojo( Run run ) {

        RunPojo runPojo = new RunPojo();
        runPojo.setId(Integer.parseInt(run.runId));
        runPojo.setName(run.runName);
        runPojo.setBuild(run.buildName);
        runPojo.setVersion(run.versionName);
        runPojo.setOs(run.os);
        runPojo.setProduct(run.productName);
        runPojo.setUserNote(run.userNote);
        runPojo.setStartTimestamp(run.getStartTimestamp());
        runPojo.setEndTimestamp(run.getEndTimestamp());
        runPojo.setDuration(run.getDurationAsString(0));
        runPojo.setTestcasesTotal(run.testcasesTotal);
        runPojo.setTestcasesPassed(run.testcasesTotal - (run.testcasesFailed + run.testcasesSkipped));
        runPojo.setTestcasesFailed(run.testcasesFailed);
        runPojo.setTestcasesSkipped(run.testcasesSkipped);

        runPojo.setScenariosTotal(run.scenariosTotal);
        runPojo.setScenariosPassed(run.scenariosTotal - (run.scenariosFailed + run.scenariosSkipped));
        runPojo.setScenariosFailed(run.scenariosFailed);
        runPojo.setScenariosSkipped(run.scenariosSkipped);
        runPojo.setTestcasesPassedPercent(run.testcasesPassedPercent);
        runPojo.setHost(run.hostName);

        return runPojo;
    }

}
