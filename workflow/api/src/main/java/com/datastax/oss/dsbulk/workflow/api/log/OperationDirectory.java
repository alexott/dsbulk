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
package com.datastax.oss.dsbulk.workflow.api.log;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class OperationDirectory {

  private static final String OPERATION_DIRECTORY_KEY =
      "com.datastax.oss.dsbulk.OPERATION_DIRECTORY";

  public static void setCurrentOperationDirectory(@NonNull Path operationDirectory) {
    System.setProperty(OPERATION_DIRECTORY_KEY, operationDirectory.toFile().getAbsolutePath());
  }

  @NonNull
  public static Optional<Path> getCurrentOperationDirectory() {
    String path = System.getProperty(OPERATION_DIRECTORY_KEY);
    if (path == null) {
      return Optional.empty();
    }
    return Optional.of(Paths.get(path));
  }
}