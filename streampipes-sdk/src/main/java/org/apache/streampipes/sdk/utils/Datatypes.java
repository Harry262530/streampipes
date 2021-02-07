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

package org.apache.streampipes.sdk.utils;

import org.apache.streampipes.vocabulary.SO;
import org.apache.streampipes.vocabulary.XSD;

import java.net.URI;
import java.util.Arrays;

public enum Datatypes {

    Integer(XSD._integer),
    Long(XSD._long),
    Float(XSD._float),
    Boolean(XSD._boolean),
    String(XSD._string),
    Double(XSD._double),
    Number(URI.create(SO.Number)),
    Sequence(XSD._sequence);

    private URI uri;

    Datatypes(URI uri) {
        this.uri = uri;
    }

    public String toString() {
        return uri.toString();
    }

    public static Datatypes fromDatatypeString(String datatype) {
        return Arrays.stream(Datatypes.values())
                .filter(d -> d.uri.toString().equals(datatype))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find datatype with URI " +datatype));
    }
}
