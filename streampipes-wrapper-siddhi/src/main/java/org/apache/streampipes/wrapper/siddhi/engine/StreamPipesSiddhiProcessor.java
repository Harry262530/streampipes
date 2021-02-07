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
package org.apache.streampipes.wrapper.siddhi.engine;

import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.siddhi.engine.callback.SiddhiDebugCallback;
import org.apache.streampipes.wrapper.siddhi.engine.generator.SiddhiInvocationConfigGenerator;
import org.apache.streampipes.wrapper.standalone.ProcessorParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataProcessor;

public abstract class StreamPipesSiddhiProcessor extends StreamPipesDataProcessor implements SiddhiStatementGenerator<ProcessorParams> {

  private SiddhiEngine siddhiEngine;

  public StreamPipesSiddhiProcessor() {
    this.siddhiEngine = new SiddhiEngine();
  }

  public StreamPipesSiddhiProcessor(SiddhiDebugCallback debugCallback) {
    this.siddhiEngine = new SiddhiEngine(debugCallback);
  }

  @Override
  public void onInvocation(ProcessorParams parameters, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext runtimeContext) throws SpRuntimeException {
    SiddhiInvocationConfigGenerator<ProcessorParams> siddhiConfigGenerator = new SiddhiInvocationConfigGenerator<>(parameters,
            this::makeStatements);
    this.siddhiEngine.initializeEngine(siddhiConfigGenerator, spOutputCollector, runtimeContext);
  }

  @Override
  public void onEvent(Event event, SpOutputCollector collector) throws SpRuntimeException {
    this.siddhiEngine.processEvent(event);
  }

  @Override
  public void onDetach() throws SpRuntimeException {
    this.siddhiEngine.shutdownEngine();
  }


}
