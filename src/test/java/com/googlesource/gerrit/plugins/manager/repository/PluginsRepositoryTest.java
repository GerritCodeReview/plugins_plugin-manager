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
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.common.Version;
import com.google.gerrit.server.config.SitePaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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
          "plugin-manager",
          "replication",
          "reviewnotes",
          "singleusergroup",
          "webhooks");

  @Test
  public void corePluginsRepositoryShouldReturnCorePluginsFromReleaseWar() throws IOException {
    SitePaths site = new SitePaths(random());
    PluginsRepository pluginRepo = new CorePluginsRepository(site, new CorePluginsDescriptions());

    Path pathToReleaseWar =
        Paths.get(System.getenv("TEST_SRCDIR"), System.getenv("TEST_WORKSPACE"), "release.war");
    if (!pathToReleaseWar.toFile().exists()) {
      throw new IllegalStateException("Cannot find release.war");
    }
    Files.createDirectories(site.bin_dir);
    Files.createSymbolicLink(site.gerrit_war, pathToReleaseWar);

    Collection<PluginInfo> plugins = pluginRepo.list(Version.getVersion());
    assertThat(plugins).hasSize(GERRIT_CORE_PLUGINS.size());

    assertThat(plugins.stream().map(p -> p.name).sorted().collect(toList()))
        .containsExactlyElementsIn(GERRIT_CORE_PLUGINS)
        .inOrder();
  }

  private static Path random() throws IOException {
    Path tmp = Files.createTempFile("gerrit_", "_site");
    Files.deleteIfExists(tmp);
    return tmp;
  }
}