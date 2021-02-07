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
package org.apache.streampipes.model.staticproperty;

import org.apache.streampipes.vocabulary.StreamPipes;
import io.fogsy.empire.annotations.RdfProperty;
import io.fogsy.empire.annotations.RdfsClass;

import javax.persistence.Entity;

@RdfsClass(StreamPipes.COLOR_PICKER_STATIC_PROPERTY)
@Entity
public class ColorPickerStaticProperty extends StaticProperty {

  @RdfProperty(StreamPipes.SELECTED_COLOR)
  private String selectedColor;

  public ColorPickerStaticProperty() {
    super(StaticPropertyType.ColorPickerStaticProperty);
  }

  public ColorPickerStaticProperty(ColorPickerStaticProperty other) {
    super(other);
    this.selectedColor = other.getSelectedColor();
  }

  public ColorPickerStaticProperty(String internalName, String label, String description) {
    super(StaticPropertyType.ColorPickerStaticProperty, internalName, label, description);
  }

  public String getSelectedColor() {
    return selectedColor;
  }

  public void setSelectedColor(String selectedColor) {
    this.selectedColor = selectedColor;
  }

  @Override
  public void accept(StaticPropertyVisitor visitor) {
    visitor.visit(this);
  }
}
