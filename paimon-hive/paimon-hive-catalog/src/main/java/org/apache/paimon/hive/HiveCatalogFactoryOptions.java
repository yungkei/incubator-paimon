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

package org.apache.paimon.hive;

import org.apache.paimon.options.ConfigOption;
import org.apache.paimon.options.ConfigOptions;

/** Options for hive catalog. */
public final class HiveCatalogFactoryOptions {

    public static final String IDENTIFIER = "hive";

    public static final ConfigOption<String> HIVE_CONF_DIR =
            ConfigOptions.key("hive-conf-dir")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "File directory of the hive-site.xml , used to create HiveMetastoreClient and security authentication, such as Kerberos, LDAP, Ranger and so on");

    public static final ConfigOption<String> HADOOP_CONF_DIR =
            ConfigOptions.key("hadoop-conf-dir")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "File directory of the core-site.xml、hdfs-site.xml、yarn-site.xml、mapred-site.xml. Currently, only local file system paths are supported. We recommend using the HADOOP_CONF_DIR environment variable to specify the Hadoop configuration. So consider using this parameter only if the environment variables don't meet your needs, such as when you want to set up the Hadoop configuration individually for each HiveCatalog");

    private HiveCatalogFactoryOptions() {}
}
