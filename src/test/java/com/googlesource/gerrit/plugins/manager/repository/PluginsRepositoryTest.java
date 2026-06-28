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
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.common.Version;
import com.google.gerrit.server.config.SitePaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
          "replication-api",
          "reviewnotes",
          "singleusergroup",
          "webhooks");

  @Test
  public void corePluginsRepositoryShouldReturnCorePluginsFromReleaseWar() throws IOException {
    SitePaths site = prepareSiteDirWithReleaseWar();

    PluginsRepository pluginRepo = new CorePluginsRepository(site, new CorePluginsDescriptions());

    Collection<PluginInfo> plugins = pluginRepo.list(Version.getVersion());
    assertThat(plugins.stream().map(p -> p.name).sorted().collect(toList()))
        .containsExactlyElementsIn(GERRIT_CORE_PLUGINS)
        .inOrder();
  }

  @Test
  public void corePluginsListFromReleaseWarShouldNotBeEmptyOnWindows() throws IOException {
    char windowsSeparator = '\\';
    SitePaths site = prepareSiteDirWithReleaseWar();

    PluginsRepository pluginRepo =
        new CorePluginsRepository(
            site.gerrit_war,
            site.gerrit_war.toString().replace('/', windowsSeparator),
            new CorePluginsDescriptions());

    assertThat(pluginRepo.list(Version.getVersion())).isNotEmpty();
  }

  private SitePaths prepareSiteDirWithReleaseWar() throws IOException {
    SitePaths site = new SitePaths(random());
    Path pathToReleaseWar =
        Path.of(
            requiredEnv("TEST_SRCDIR"),
            requiredEnv("TEST_WORKSPACE"),
            warName());
    assertWithMessage("WAR runfile must exist: %s", pathToReleaseWar)
        .that(Files.exists(pathToReleaseWar))
        .isTrue();
    Files.createDirectories(site.bin_dir);
    Files.createSymbolicLink(site.gerrit_war, pathToReleaseWar);
    return site;
  }

  /**
   * The WAR under test follows the servlet flavour: the default test target inspects {@code
   * release.war}; the flavoured test twin passes the matching WAR's runfile name via {@code
   * PLUGIN_MANAGER_TEST_WAR}.
   */
  private static String warName() {
    String war = System.getenv("PLUGIN_MANAGER_TEST_WAR");
    return war != null ? war : "release.war";
  }

  private static String requiredEnv(String name) {
    String value = System.getenv(name);
    assertWithMessage("Missing required env var: %s", name)
        .that(value)
        .isNotNull();
    return value;
  }

  private static Path random() throws IOException {
    Path tmp = Files.createTempFile("gerrit_", "_site");
    Files.deleteIfExists(tmp);
    return tmp;
  }
}
