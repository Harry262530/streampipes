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

package org.apache.streampipes.container.extensions.function;

import org.apache.streampipes.container.declarer.IStreamPipesFunctionDeclarer;
import org.apache.streampipes.container.init.DeclarersSingleton;
import org.apache.streampipes.model.function.FunctionDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum StreamPipesFunctionHandler {

  INSTANCE;

  private final Map<String, IStreamPipesFunctionDeclarer> runningInstances;

  StreamPipesFunctionHandler() {
    this.runningInstances = new HashMap<>();
  }

  public void initializeFunctions() {
    initializeFunctions(DeclarersSingleton.getInstance().getFunctions().values());
  }

  public void initializeFunctions(Collection<IStreamPipesFunctionDeclarer> functions) {
    functions.forEach(function -> {
      Runnable r = function::invokeRuntime;
      new Thread(r).start();
      runningInstances.put(function.getFunctionId().getId(), function);
    });
    if (!(runningInstances.isEmpty())) {
      new Thread(new FunctionRegistrationHandler(getFunctionDefinitions())).start();
    }
  }

  public void cleanupFunctions() {
    this.runningInstances.forEach((key, value) -> {
      value.discardRuntime();
    });
    new Thread(new FunctionDeregistrationHandler(getFunctionDefinitions())).start();
  }

  private List<FunctionDefinition> getFunctionDefinitions() {
    return this.runningInstances
        .values()
        .stream()
        .map(function -> new FunctionDefinition(function.getFunctionId(), function.requiredStreamIds()))
        .collect(Collectors.toList());
  }

}
