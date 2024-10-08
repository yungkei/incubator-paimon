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

package org.apache.paimon.flink.action.cdc;

import org.apache.paimon.types.DataType;
import org.apache.paimon.types.DataTypes;
import org.apache.paimon.utils.StringUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/** A {@link NoFiledExpression} is a function that computes a value from a set of input fields. */
public interface NoFiledExpression extends Serializable {

    List<String> SUPPORTED_EXPRESSION =
            Arrays.asList(
                    "PROV",
                    "SYSTEM_OP_TS",
                    "SYSTEM_PHYSICAL_TABLE",
                    "SYSTEM_PHYSICAL_DB",
                    "SYSTEM_OP_ID");

    /** Return {@link DataType} of computed value. */
    DataType outputType();

    /** Compute value from given input. Input and output are serialized to string. */
    String eval();

    String defaultValue();

    static NoFiledExpression create(String exprName, String defaultValue, String... literals) {
        switch (exprName.toUpperCase()) {
            case "PROV":
                return prov(defaultValue);
            case "SYSTEM_OP_TS":
                return systemOpTs(defaultValue);
            case "SYSTEM_OP_ID":
                return systemOPTraceidComputer(defaultValue);
            case "SYSTEM_PHYSICAL_TABLE":
                return systemOPTableComputer(defaultValue);
            case "SYSTEM_PHYSICAL_DB":
                return systemOPDBComputer(defaultValue);
            default:
                throw new UnsupportedOperationException(
                        String.format(
                                "Unsupported expression: %s. Supported expressions are: %s",
                                exprName, String.join(",", SUPPORTED_EXPRESSION)));
        }
    }

    static NoFiledExpression prov(String defaultValue) {
        return new ProvComputer(defaultValue);
    }

    static NoFiledExpression systemOPTableComputer(String defaultValue) {
        return new SystemOPTableComputer(defaultValue);
    }

    static NoFiledExpression systemOPDBComputer(String defaultValue) {
        return new SystemOPDBComputer(defaultValue);
    }

    static NoFiledExpression systemOPTraceidComputer(String defaultValue) {
        return new SystemOPTraceidComputer(defaultValue);
    }

    static NoFiledExpression systemOpTs(String defaultValue) {
        return new SystemOpTsComputer(defaultValue);
    }

    /** Compute year from a time input. */
    final class ProvComputer implements NoFiledExpression {

        private static final long serialVersionUID = 1L;
        private String defaultValue;

        public ProvComputer(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public DataType outputType() {
            return DataTypes.STRING();
        }

        @Override
        public String defaultValue() {
            return defaultValue;
        }

        @Override
        public String eval() {
            return defaultValue;
        }
    }

    /** Expression to handle real table. */
    final class SystemOPTableComputer implements NoFiledExpression {

        private static final long serialVersionUID = 1L;
        private String defaultValue;

        public SystemOPTableComputer(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public DataType outputType() {
            return DataTypes.STRING();
        }

        @Override
        public String defaultValue() {
            return defaultValue;
        }

        @Override
        public String eval() {
            return defaultValue;
        }
    }

    /** Expression to handle real db. */
    final class SystemOPDBComputer implements NoFiledExpression {

        private static final long serialVersionUID = 1L;
        private String defaultValue;

        public SystemOPDBComputer(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public DataType outputType() {
            return DataTypes.STRING();
        }

        @Override
        public String defaultValue() {
            return defaultValue;
        }

        @Override
        public String eval() {
            return defaultValue;
        }
    }

    /** Expression to handle real db. */
    final class SystemOPTraceidComputer implements NoFiledExpression {

        private static final long serialVersionUID = 1L;
        private String defaultValue;

        public SystemOPTraceidComputer(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public DataType outputType() {
            return DataTypes.STRING();
        }

        @Override
        public String defaultValue() {
            return defaultValue;
        }

        @Override
        public String eval() {
            return defaultValue;
        }
    }

    /** Expression to handle temporal value. */
    final class SystemOpTsComputer implements NoFiledExpression {

        private static final long serialVersionUID = 1L;
        private String defaultValue;
        private int precision = 0;

        public SystemOpTsComputer(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public DataType outputType() {
            return DataTypes.TIMESTAMP(precision);
        }

        @Override
        public String defaultValue() {
            return defaultValue;
        }

        @Override
        public String eval() {
            if (StringUtils.isEmpty(defaultValue)) {
                LocalDate today = LocalDate.now();

                LocalDateTime todayStart = today.atStartOfDay();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDate = todayStart.format(formatter);
                return String.format("%s", formattedDate);
            } else {
                if (defaultValue.contains(".")) {
                    String[] split = defaultValue.split("\\.");
                    precision = split[1].length();
                }
                return String.format("%s", defaultValue);
            }
        }
    }
}
