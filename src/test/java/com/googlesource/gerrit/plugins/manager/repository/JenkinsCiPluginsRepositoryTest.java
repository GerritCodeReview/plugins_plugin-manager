// Copyright (C) 2026 The Android Open Source Project
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.util.Providers;
import com.googlesource.gerrit.plugins.manager.PluginManagerConfig;
import com.googlesource.gerrit.plugins.manager.gson.SmartGson;
import com.googlesource.gerrit.plugins.manager.gson.SmartJson;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsCiPluginsRepositoryTest {
  private static final String TEST_GERRIT_VERSION = "3.0";
  private static final String TEST_JENKINS_URL = "http://jenkins.test.localdomain";
  private static final String TEST_JENKINS_JOB_URL = TEST_JENKINS_URL + "/job/my-test-job";
  private static final String JENKINS_JSON_API = "/api/json";
  private static final String JENKINS_JOB_SUCCESS_COLOR = "blue";
  private static final String TEST_JENKINS_JOB_BUILD_URL =
      TEST_JENKINS_JOB_URL + "/lastSuccessfulBuild";
  private static final String TEST_PLUGIN_NAME = "test-plugin";
  private static final String TEST_JENKINS_JOB_BUILD_SHA1 =
      "59e8a0d32c1252f7df949d9377ceab0b39d6339f";
  private static final String APACHE_LICENSE_2_0_TEXT =
      """
                                    Apache License
                              Version 2.0, January 2004
                           http://www.apache.org/licenses/

      TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

      1. Definitions.

         "License" shall mean the terms and conditions for use, reproduction,
         and distribution as defined by Sections 1 through 9 of this document.

         "Licensor" shall mean the copyright owner or entity authorized by
         the copyright owner that is granting the License.
      """;
  private static final String APACHE_LICENSE_2_0_TEXT_FIRST_TWO_LINES =
      APACHE_LICENSE_2_0_TEXT.lines().limit(2).collect(Collectors.joining("\n"));
  private static final String TEST_PLUGIN_VERSION_1_0 = "v1.0";
  private static final String TEST_PLUGIN_DESCRIPTION = "Test plugin";

  @Mock SmartGson gsonMock;

  @Mock PluginManagerConfig configMock;

  private SmartJson testBuildDetailJson;
  private Gson gson;

  @Before
  public void setUp() throws IOException {
    gson = new Gson();

    JenkinsCiPluginsRepository.Job testJob = new JenkinsCiPluginsRepository.Job();
    testJob.color = JENKINS_JOB_SUCCESS_COLOR;
    testJob.url = TEST_JENKINS_JOB_URL;
    JenkinsCiPluginsRepository.View testView = new JenkinsCiPluginsRepository.View();
    testView.jobs = new JenkinsCiPluginsRepository.Job[] {testJob};

    when(gsonMock.get(any(String.class), eq(JenkinsCiPluginsRepository.View.class)))
        .thenReturn(testView);

    when(gsonMock.get(eq(TEST_JENKINS_JOB_URL + JENKINS_JSON_API)))
        .thenReturn(
            SmartJson.of(Map.of("lastSuccessfulBuild", Map.of("url", TEST_JENKINS_JOB_BUILD_URL))));

    testBuildDetailJson =
        SmartJson.of(
            Map.of(
                "artifacts",
                List.of(),
                "url",
                TEST_JENKINS_JOB_BUILD_URL,
                "actions",
                List.of(Map.of("lastBuiltRevision", Map.of("SHA1", TEST_JENKINS_JOB_BUILD_SHA1)))));
    when(gsonMock.get(eq(TEST_JENKINS_JOB_BUILD_URL + JENKINS_JSON_API)))
        .thenReturn(testBuildDetailJson);

    when(configMock.getJenkinsUrl()).thenReturn(TEST_JENKINS_URL);
  }

  @Test
  public void noArtifactsPublished_listOfPluginShouldBeEmpty() throws IOException {
    JenkinsCiPluginsRepository pluginRepo =
        new JenkinsCiPluginsRepository(Providers.of(gsonMock), configMock);
    assertThat(pluginRepo.list(TEST_GERRIT_VERSION)).isEmpty();
  }

  @Test
  public void artifactPublishedWithLicense_listOfPluginsShouldHaveOnePluginWihLicense()
      throws IOException {
    testBuildDetailJson.add(
        "artifacts",
        gson.toJsonTree(
                List.of(
                    jsonArtifact(TEST_PLUGIN_NAME + ".jar"),
                    jsonArtifact(TEST_PLUGIN_NAME + ".jar-version"),
                    jsonArtifact(TEST_PLUGIN_NAME + ".json"),
                    jsonArtifact("LICENSE")),
                List.class)
            .getAsJsonArray());
    when(gsonMock.getReader(getArtifactUrl(TEST_PLUGIN_NAME + ".jar-version")))
        .thenReturn(inputStreamOf(TEST_PLUGIN_VERSION_1_0));
    when(gsonMock.getReader(getArtifactUrl("LICENSE")))
        .thenReturn(inputStreamOf(APACHE_LICENSE_2_0_TEXT));
    when(gsonMock.get(getArtifactUrl(TEST_PLUGIN_NAME + ".json")))
        .thenReturn(SmartJson.of(Map.of("description", TEST_PLUGIN_DESCRIPTION)));

    JenkinsCiPluginsRepository pluginRepo =
        new JenkinsCiPluginsRepository(Providers.of(gsonMock), configMock);

    List<PluginInfo> pluginsList = pluginRepo.list(TEST_GERRIT_VERSION);
    assertThat(pluginsList).hasSize(1);

    PluginInfo pluginInfo = pluginsList.getFirst();
    assertThat(pluginInfo.name).isEqualTo(TEST_PLUGIN_NAME);
    assertThat(pluginInfo.version).isEqualTo(TEST_PLUGIN_VERSION_1_0);
    assertThat(pluginInfo.description).isEqualTo(TEST_PLUGIN_DESCRIPTION);
    assertThat(pluginInfo.license).isEqualTo(APACHE_LICENSE_2_0_TEXT_FIRST_TWO_LINES);
  }

  private static @NonNull String getArtifactUrl(String artifactPath) {
    return TEST_JENKINS_JOB_BUILD_URL + "/artifact/" + TEST_PLUGIN_NAME + "/" + artifactPath;
  }

  private InputStreamReader inputStreamOf(String streamContent) {
    return new InputStreamReader(
        new ByteArrayInputStream(streamContent.getBytes(StandardCharsets.UTF_8)),
        StandardCharsets.UTF_8);
  }

  private JsonObject jsonArtifact(String artifactName) {
    JsonObject artifactJson = new JsonObject();
    artifactJson.addProperty("displayPath", artifactName);
    artifactJson.addProperty("fileName", artifactName);
    artifactJson.addProperty("relativePath", TEST_PLUGIN_NAME + "/" + artifactName);
    return artifactJson;
  }
}
