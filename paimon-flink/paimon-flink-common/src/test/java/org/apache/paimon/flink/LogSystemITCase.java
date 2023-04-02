/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.flink;

import org.apache.paimon.flink.kafka.KafkaTableTestBase;
import org.apache.paimon.utils.BlockingIterator;

import org.apache.flink.table.api.TableResult;
import org.apache.flink.types.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** ITCase for table with log system. */
public class LogSystemITCase extends KafkaTableTestBase {

    @BeforeEach
    public void before() throws IOException {
        tEnv.executeSql(
                String.format(
                        "CREATE CATALOG PAIMON WITH (" + "'type'='paimon', 'warehouse'='%s')",
                        getTempDirPath()));
        tEnv.useCatalog("PAIMON");
    }

    @Test
    public void testAppendOnlyWithEventual() throws Exception {
        createTopicIfNotExists("T", 1);
        // disable checkpointing to test eventual
        env.getCheckpointConfig().disableCheckpointing();
        env.setParallelism(1);
        tEnv.executeSql(
                String.format(
                        "CREATE TABLE T (i INT, j INT) WITH ("
                                + "'log.system'='kafka', "
                                + "'write-mode'='append-only', "
                                + "'log.consistency'='eventual', "
                                + "'kafka.bootstrap.servers'='%s', "
                                + "'kafka.topic'='T')",
                        getBootstrapServers()));
        tEnv.executeSql("CREATE TEMPORARY TABLE gen (i INT, j INT) WITH ('connector'='datagen')");
        TableResult write = tEnv.executeSql("INSERT INTO T SELECT * FROM gen");
        BlockingIterator<Row, Row> read =
                BlockingIterator.of(tEnv.executeSql("SELECT * FROM T").collect());
        List<Row> collect = read.collect(10);
        assertThat(collect).hasSize(10);
        write.getJobClient().get().cancel();
        read.close();
    }

    @Test
    public void testReadFromFile() throws Exception {
        createTopicIfNotExists("test-double-sink", 1);
        // disable checkpointing to test eventual
        env.getCheckpointConfig().disableCheckpointing();
        env.setParallelism(1);
        tEnv.executeSql(
                String.format(
                        "CREATE TABLE kafka_file_double_sink (\n"
                                + " word STRING ,\n"
                                + "    cnt BIGINT,\n"
                                + "      PRIMARY KEY (word) NOT ENFORCED\n"
                                + ")\n"
                                + "WITH (\n"
                                + " 'merge-engine' = 'aggregation',\n"
                                + "  'changelog-producer' = 'full-compaction',\n"
                                + "    'log.consistency' = 'eventual',\n"
                                + "    'log.system' = 'kafka',\n"
                                + "    'streaming-read-from'='file-store',\n"
                                + "    'fields.cnt.aggregate-function' = 'sum',\n"
                                + "    'kafka.bootstrap.servers' = '%s',\n"
                                + "    'kafka.topic' = 'test-double-sink',\n"
                                + "    'kafka.transaction.timeout.ms'='30000'\n"
                                + "\n"
                                + ");",
                        getBootstrapServers()));
        tEnv.executeSql(
                "CREATE TEMPORARY TABLE word_table (\n"
                        + "    word STRING\n"
                        + ") WITH (\n"
                        + "    'connector' = 'datagen',\n"
                        + "    'fields.word.length' = '1'\n"
                        + ");");
        TableResult write =
                tEnv.executeSql(
                        "INSERT INTO kafka_file_double_sink SELECT word, COUNT(*) FROM word_table GROUP BY word;");
        BlockingIterator<Row, Row> read =
                BlockingIterator.of(
                        tEnv.executeSql("SELECT * FROM kafka_file_double_sink").collect());
        List<Row> collect = read.collect(10);
        assertThat(collect).hasSize(10);
        write.getJobClient().get().cancel();

        read.close();
    }

    @Test
    public void testReadFromLog() throws Exception {
        createTopicIfNotExists("test-single-sink", 1);
        // disable checkpointing to test eventual
        env.getCheckpointConfig().disableCheckpointing();
        env.setParallelism(1);
        // 'fields.cnt.aggregate-function' = 'sum' is miss will throw
        // java.lang.UnsupportedOperationException: Aggregate function 'last_non_null_value' does
        // not support retraction
        // data will only be written to kafka
        tEnv.executeSql(
                String.format(
                        "CREATE TABLE kafka_file_single_sink (\n"
                                + " word STRING ,\n"
                                + "    cnt BIGINT,\n"
                                + "      PRIMARY KEY (word) NOT ENFORCED\n"
                                + ")\n"
                                + "WITH (\n"
                                + " 'merge-engine' = 'aggregation',\n"
                                + "    'changelog-producer' = 'full-compaction',\n"
                                + "    'log.consistency' = 'eventual',\n"
                                + "    'log.system' = 'kafka',\n"
                                + "    'streaming-read-from'='log-store',\n"
                                + "    'kafka.bootstrap.servers' = '%s',\n"
                                + "    'kafka.topic' = 'test-single-sink',\n"
                                + "    'kafka.transaction.timeout.ms'='30000'\n"
                                + "\n"
                                + ");",
                        getBootstrapServers()));
        tEnv.executeSql(
                "CREATE TEMPORARY TABLE word_table (\n"
                        + "    word STRING\n"
                        + ") WITH (\n"
                        + "    'connector' = 'datagen',\n"
                        + "    'fields.word.length' = '1'\n"
                        + ");");
        TableResult write =
                tEnv.executeSql(
                        "INSERT INTO kafka_file_single_sink SELECT word, COUNT(*) FROM word_table GROUP BY word;");
        BlockingIterator<Row, Row> read =
                BlockingIterator.of(
                        tEnv.executeSql("SELECT * FROM kafka_file_single_sink").collect());
        List<Row> collect = read.collect(10);
        assertThat(collect).hasSize(10);
        write.getJobClient().get().cancel();

        read.close();
    }
}
