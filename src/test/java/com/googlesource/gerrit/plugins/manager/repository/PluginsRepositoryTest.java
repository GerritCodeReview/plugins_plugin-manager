// Copyright (C) 2019 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.manager.repository;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class PluginsRepositoryTest {

  private static final ImmutableList<String> GERRIT_CORE_PLUGINS =
      ImmutableList.of(
          "codemirror-editor",
          "commit-message-length-validator",
          "delete-project",
          "download-commands",
          "gitiles",
          "hooks",
          "javamelody",
          "oauth",
          "plugin-manager",
          "replication",
          "replication-api",
          "reviewnotes",
          "singleusergroup",
          "webhooks");

  @Test
  public void corePluginsListShouldMatchDeclaredCorePlugins() throws IOException {
    List<String> plugins = readDeclaredCorePlugins();
    assertThat(plugins.stream().sorted().collect(toList()))
        .containsExactlyElementsIn(GERRIT_CORE_PLUGINS)
        .inOrder();
  }

  @Test
  public void corePluginsListShouldNotBeEmpty() throws IOException {
    assertThat(readDeclaredCorePlugins()).isNotEmpty();
  }

  private static List<String> readDeclaredCorePlugins() throws IOException {
    Path p =
        Path.of(getenv("TEST_SRCDIR"), getenv("TEST_WORKSPACE"), "plugins", "core.plugins.txt");
    assume().that(p.toFile().exists()).isTrue();

    return Files.readAllLines(p, StandardCharsets.UTF_8).stream()
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(toList());
  }

  private static String getenv(String name) {
    String value = System.getenv(name);
    assume().that(value).isNotNull();
    return value;
  }
}
