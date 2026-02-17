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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.common.Version;
import com.google.gson.JsonSyntaxException;
import com.googlesource.gerrit.plugins.manager.gson.SmartGson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PluginsRepositoryTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

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

  @Test
  public void corePluginsShouldIncludeLicenseForNativeAndWindowsPaths() throws IOException {
    // Exercise repository loading with a generated fixture instead of building release.war.
    Path war = writeTestWar();
    // Use the same physical file while also exercising Windows separator normalization.
    for (String warPath : List.of(war.toString(), war.toString().replace('/', '\\'))) {
      ImmutableList<PluginInfo> plugins =
          new CorePluginsRepository(war, warPath, new CorePluginsDescriptions())
              .list(Version.getVersion());
      assertThat(plugins).hasSize(1);
      assertThat(plugins.get(0).name).isEqualTo("test-plugin");
      // The repository assigns this license to core plugins; it does not read a LICENSE file.
      assertThat(plugins.get(0).license).isEqualTo(CorePluginsRepository.GERRIT_LICENSE);
    }
  }

  @Test
  public void malformedPluginMetadataShouldBeIgnored() throws IOException {
    String metadataUrl = "https://example.com/plugin.json";
    SmartGson gson = mock(SmartGson.class);
    when(gson.get(metadataUrl)).thenThrow(new JsonSyntaxException("Expected a JSON object"));
    JenkinsCiPluginsRepository pluginRepo = new JenkinsCiPluginsRepository(() -> gson, null);

    assertThat(pluginRepo.tryGetJson(metadataUrl)).isEmpty();
  }

  private Path writeTestWar() throws IOException {
    // A valid JAR without a manifest exercises the fallback that derives the name from
    // test-plugin.jar.
    ByteArrayOutputStream pluginBytes = new ByteArrayOutputStream();
    try (JarOutputStream plugin = new JarOutputStream(pluginBytes)) {
      plugin.putNextEntry(new JarEntry("plugin.txt"));
      plugin.write(0);
      plugin.closeEntry();
    }
    // Reproduce only the WAR layout consumed by CorePluginsRepository.
    Path war = temporaryFolder.newFile("gerrit.war").toPath();
    try (JarOutputStream archive = new JarOutputStream(Files.newOutputStream(war))) {
      archive.putNextEntry(new JarEntry("WEB-INF/plugins/test-plugin.jar"));
      archive.write(pluginBytes.toByteArray());
      archive.closeEntry();
    }
    return war;
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
