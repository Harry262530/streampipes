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

package org.apache.streampipes.connect.adapter;

import org.apache.streampipes.config.backend.BackendConfig;
import org.apache.streampipes.connect.adapter.model.pipeline.AdapterPipeline;
import org.apache.streampipes.connect.adapter.preprocessing.elements.*;
import org.apache.streampipes.connect.adapter.preprocessing.transform.stream.DuplicateFilterPipelineElement;
import org.apache.streampipes.connect.api.IAdapterPipelineElement;
import org.apache.streampipes.model.connect.adapter.AdapterDescription;
import org.apache.streampipes.model.connect.rules.TransformationRuleDescription;
import org.apache.streampipes.model.connect.rules.schema.SchemaTransformationRuleDescription;
import org.apache.streampipes.model.connect.rules.stream.EventRateTransformationRuleDescription;
import org.apache.streampipes.model.connect.rules.stream.RemoveDuplicatesTransformationRuleDescription;
import org.apache.streampipes.model.connect.rules.value.*;
import org.apache.streampipes.model.grounding.JmsTransportProtocol;
import org.apache.streampipes.model.grounding.KafkaTransportProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdapterPipelineGenerator {

  public AdapterPipeline generatePipeline(AdapterDescription adapterDescription) {

    var pipelineElements = makeAdapterPipelineElements(adapterDescription.getRules());

    var duplicatesTransformationRuleDescription = getRemoveDuplicateRule(adapterDescription.getRules());
    if (duplicatesTransformationRuleDescription != null) {
      pipelineElements.add(new DuplicateFilterPipelineElement(duplicatesTransformationRuleDescription.getFilterTimeWindow()));
    }

    var transformStreamAdapterElement = new TransformStreamAdapterElement();
    var eventRateTransformationRuleDescription = getEventRateTransformationRule(adapterDescription.getRules());
    if (eventRateTransformationRuleDescription != null) {
      transformStreamAdapterElement.addStreamTransformationRuleDescription(eventRateTransformationRuleDescription);
    }
    pipelineElements.add(transformStreamAdapterElement);

    // TODO decide what was meant with this comment
    // Needed when adapter is (
    if (adapterDescription.getEventGrounding() != null && adapterDescription.getEventGrounding().getTransportProtocol() != null
      && adapterDescription.getEventGrounding().getTransportProtocol().getBrokerHostname() != null) {
      return new AdapterPipeline(pipelineElements, getAdapterSink(adapterDescription));
    }

    return new AdapterPipeline(pipelineElements);
  }

  public List<IAdapterPipelineElement> makeAdapterPipelineElements(List<TransformationRuleDescription> rules) {
    List<IAdapterPipelineElement> pipelineElements = new ArrayList<>();

    // Must be before the schema transformations to ensure that user can move this event property
    var timestampTransformationRuleDescription = getTimestampRule(rules);
    if (timestampTransformationRuleDescription != null) {
      pipelineElements.add(new AddTimestampPipelineElement(
        timestampTransformationRuleDescription.getRuntimeKey()));
    }

    var valueTransformationRuleDescription = getAddValueRule(rules);
    if (valueTransformationRuleDescription != null) {
      pipelineElements.add(new AddValuePipelineElement(
        valueTransformationRuleDescription.getRuntimeKey(),
        valueTransformationRuleDescription.getStaticValue()));
    }

    // first transform schema before transforming vales
    // value rules should use unique keys for of new schema
    pipelineElements.add(new TransformSchemaAdapterPipelineElement(getSchemaRules(rules)));
    pipelineElements.add(new TransformValueAdapterPipelineElement(getValueRules(rules)));

    return pipelineElements;
  }

  private SendToBrokerAdapterSink<?> getAdapterSink(AdapterDescription adapterDescription) {
    var prioritizedProtocol =
      BackendConfig.INSTANCE.getMessagingSettings().getPrioritizedProtocols().get(0);

    if (GroundingService.isPrioritized(prioritizedProtocol, JmsTransportProtocol.class)) {
      return new SendToJmsAdapterSink(adapterDescription);
    }
    else if (GroundingService.isPrioritized(prioritizedProtocol, KafkaTransportProtocol.class)) {
      return new SendToKafkaAdapterSink(adapterDescription);
    }
    else {
      return new SendToMqttAdapterSink(adapterDescription);
    }
  }

  private RemoveDuplicatesTransformationRuleDescription getRemoveDuplicateRule(List<TransformationRuleDescription> rules) {
    return getRule(rules, RemoveDuplicatesTransformationRuleDescription.class);
  }

  private EventRateTransformationRuleDescription getEventRateTransformationRule(List<TransformationRuleDescription> rules) {
    return getRule(rules, EventRateTransformationRuleDescription.class);
  }

  private AddTimestampRuleDescription getTimestampRule(List<TransformationRuleDescription> rules) {
    return getRule(rules, AddTimestampRuleDescription.class);
  }

  private AddValueTransformationRuleDescription getAddValueRule(List<TransformationRuleDescription> rules) {
    return getRule(rules, AddValueTransformationRuleDescription.class);
  }

  private <G extends TransformationRuleDescription> G getRule(List<TransformationRuleDescription> rules,
                                                              Class<G> type) {

    if (rules != null) {
      for (TransformationRuleDescription tr : rules) {
        if (type.isInstance(tr)) {
          return type.cast(tr);
        }
      }
    }

    return null;
  }

  private List<TransformationRuleDescription> getValueRules(List<TransformationRuleDescription> rules) {
    return rules
      .stream()
      .filter(r -> r instanceof ValueTransformationRuleDescription && !(r instanceof AddTimestampRuleDescription))
      .collect(Collectors.toList());
  }

  private List<TransformationRuleDescription> getSchemaRules(List<TransformationRuleDescription> rules) {
    return rules
      .stream()
      .filter(r -> r instanceof SchemaTransformationRuleDescription)
      .collect(Collectors.toList());
  }
}
