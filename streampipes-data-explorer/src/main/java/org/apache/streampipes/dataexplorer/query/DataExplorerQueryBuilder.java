/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.streampipes.dataexplorer.query;

import org.influxdb.dto.Query;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class DataExplorerQueryBuilder {

  private StringJoiner queryParts;
  private String databaseName;
  private TimeUnit timeUnit;

  private DataExplorerQueryBuilder(String databaseName) {
    this.queryParts = new StringJoiner(" ");
    this.databaseName = databaseName;
  }

  public static DataExplorerQueryBuilder create(String databaseName) {
    return new DataExplorerQueryBuilder(databaseName);
  }

  public DataExplorerQueryBuilder add(String queryPart) {
    this.queryParts.add(queryPart);
    return this;
  }

  public DataExplorerQueryBuilder withTimeUnit(TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
    return this;
  }

  public boolean hasTimeUnit() {
    return this.timeUnit != null;
  }

  public TimeUnit getTimeUnit() {
    return this.timeUnit;
  }

  public Query toQuery() {
    return new Query(queryParts.toString(), databaseName);
  }
}
