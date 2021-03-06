/*
 * Copyright DataStax, Inc.
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
package com.datastax.oss.dsbulk.codecs.text.string.dse;

import static com.datastax.oss.dsbulk.tests.assertions.TestAssertions.assertThat;

import com.datastax.dse.driver.api.core.data.geometry.Point;
import com.datastax.dse.driver.internal.core.data.geometry.DefaultPoint;
import com.datastax.oss.driver.shaded.guava.common.collect.Lists;
import java.util.List;
import org.junit.jupiter.api.Test;

class StringToPointCodecTest {

  private List<String> nullStrings = Lists.newArrayList("NULL");
  private Point point = new DefaultPoint(-1.1, -2.2);

  @Test
  void should_convert_from_valid_external() {
    StringToPointCodec codec = new StringToPointCodec(nullStrings);
    assertThat(codec)
        .convertsFromExternal("'POINT (-1.1 -2.2)'")
        .toInternal(point)
        .convertsFromExternal(" point (-1.1 -2.2) ")
        .toInternal(point)
        .convertsFromExternal("{\"type\":\"Point\",\"coordinates\":[-1.1,-2.2]}")
        .toInternal(point)
        .convertsFromExternal(null)
        .toInternal(null)
        .convertsFromExternal("")
        .toInternal(null)
        .convertsFromExternal("NULL")
        .toInternal(null);
  }

  @Test
  void should_convert_from_valid_internal() {
    StringToPointCodec codec = new StringToPointCodec(nullStrings);
    assertThat(codec).convertsFromInternal(point).toExternal("POINT (-1.1 -2.2)");
  }

  @Test
  void should_not_convert_from_invalid_external() {
    StringToPointCodec codec = new StringToPointCodec(nullStrings);
    assertThat(codec).cannotConvertFromExternal("not a valid point literal");
  }
}
