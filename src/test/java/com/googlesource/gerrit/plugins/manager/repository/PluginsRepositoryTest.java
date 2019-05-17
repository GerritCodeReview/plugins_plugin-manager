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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PluginsRepositoryTest {

  private static final String GERRIT_WAR_MAVEN_LOCAL =
      "~/.m2/repository/com/google/gerrit/gerrit-war/3.1.0-SNAPSHOT/gerrit-war-3.1.0-SNAPSHOT.war";
  private static final String GERRIT_WAR_MAVEN_CENTRAL =
      "https://repo1.maven.org/maven2/com/google/gerrit/gerrit-war/3.0.2/gerrit-war-3.0.2.war";
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

  private Path root;

  @Before
  public void setUp() throws IOException {
    root = random();
  }

  @After
  public void tearDown() throws IOException {
    FileUtils.deleteDirectory(root.toFile());
  }

  @Test
  public void corePluginsRepositoryShouldReturnCorePluginsFromReleaseWar() throws IOException {
    SitePaths site = new SitePaths(root);

    PluginsRepository pluginRepo = new CorePluginsRepository(site, new CorePluginsDescriptions());

    Path localMavenPath =
        Paths.get(GERRIT_WAR_MAVEN_LOCAL.replaceFirst("^~", System.getProperty("user.home")));
    if (localMavenPath.toFile().exists()) {
      // Create symbolic link from local maven directory.
      // The pre-requisite is installing war with this command:
      // VERBOSE=1 tools/maven/api.sh war_install
      Files.createDirectories(site.bin_dir);
      Files.createSymbolicLink(site.gerrit_war, localMavenPath);
    } else {
      // Download release.war from Maven Central
      FileUtils.copyURLToFile(new URL(GERRIT_WAR_MAVEN_CENTRAL), site.gerrit_war.toFile());
    }

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
