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

import com.google.common.collect.Iterables;
import com.google.gerrit.common.Nullable;
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
import org.apache.commons.lang3.ArrayUtils;
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
  private static final String JENKINS_JSON_API = "/api/json";
  private static final String JENKINS_JOB_SUCCESS_COLOR = "blue";

  private static final String TEST_JAR_PLUGIN_NAME = "test-jar-plugin";
  private static final String TEST_JS_PLUGIN_NAME = "test-js-plugin";
  private static final String TEST_JENKINS_JOB_BUILD_SHA1 =
      "59e8a0d32c1252f7df949d9377ceab0b39d6339f";
  private static final String FOO_LICENSE =
      """
      Foo License 2.0
      April 2005
      This is foo
      """;
  private static final String BAR_LICENSE =
      """
      Bar License 2.0
      April 2006
      This is foo
      """;
  private static final String FOO_LICENSE_FIRST_TWO_LINES =
      FOO_LICENSE.lines().limit(2).collect(Collectors.joining("\n"));
  private static final String BAR_LICENSE_FIRST_TWO_LINES =
      BAR_LICENSE.lines().limit(2).collect(Collectors.joining("\n"));
  private static final String TEST_PLUGIN_VERSION_1_0 = "v1.0";
  private static final String TEST_PLUGIN_DESCRIPTION = "Test plugin";
  public static final String PLUGIN_LICENSE_RESOURCE_PATH = "LICENSE";

  @Mock SmartGson gsonMock;

  @Mock PluginManagerConfig configMock;

  private JenkinsCiPluginsRepository.View testView;

  @Before
  public void setUp() throws IOException {
    testView = new JenkinsCiPluginsRepository.View();
    testView.jobs = new JenkinsCiPluginsRepository.Job[0];
    when(gsonMock.get(any(String.class), eq(JenkinsCiPluginsRepository.View.class)))
        .thenReturn(testView);
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
    addPluginToJenkinsCiMock(
        TEST_JAR_PLUGIN_NAME, "jar", TEST_PLUGIN_VERSION_1_0, TEST_PLUGIN_DESCRIPTION, FOO_LICENSE);
    addPluginToJenkinsCiMock(
        TEST_JS_PLUGIN_NAME, "js", TEST_PLUGIN_VERSION_1_0, TEST_PLUGIN_DESCRIPTION, BAR_LICENSE);

    JenkinsCiPluginsRepository pluginRepo =
        new JenkinsCiPluginsRepository(Providers.of(gsonMock), configMock);

    List<PluginInfo> pluginsList = pluginRepo.list(TEST_GERRIT_VERSION);
    assertThat(pluginsList).hasSize(2);

    PluginInfo jarPluginInfo = getPluginWithName(pluginsList, TEST_JAR_PLUGIN_NAME);
    assertThat(jarPluginInfo).isNotNull();
    assertPluginInfoDetails(
        jarPluginInfo,
        TEST_PLUGIN_VERSION_1_0,
        TEST_PLUGIN_DESCRIPTION,
        FOO_LICENSE_FIRST_TWO_LINES);

    PluginInfo jsPluginInfo = getPluginWithName(pluginsList, TEST_JS_PLUGIN_NAME);
    assertThat(jsPluginInfo).isNotNull();
    assertPluginInfoDetails(
        jsPluginInfo,
        TEST_PLUGIN_VERSION_1_0,
        TEST_PLUGIN_DESCRIPTION,
        BAR_LICENSE_FIRST_TWO_LINES);
  }

  private static @Nullable PluginInfo getPluginWithName(
      List<PluginInfo> pluginsList, String pluginName) {
    return Iterables.tryFind(pluginsList, pi -> pi.name.equals(pluginName)).orNull();
  }

  private static void assertPluginInfoDetails(
      PluginInfo pluginInfo, String pluginVersion, String pluginDescription, String pluginLicense) {
    assertThat(pluginInfo.version).isEqualTo(pluginVersion);
    assertThat(pluginInfo.description).isEqualTo(pluginDescription);
    assertThat(pluginInfo.license).isEqualTo(pluginLicense);
  }

  private void addPluginToJenkinsCiMock(
      String pluginName,
      String pluginFileExtension,
      String pluginVersion,
      String pluginDescription,
      String pluginLicense)
      throws IOException {
    JenkinsCiPluginsRepository.Job testPluginJob = new JenkinsCiPluginsRepository.Job();
    testPluginJob.color = JENKINS_JOB_SUCCESS_COLOR;
    testPluginJob.name = pluginName + "-job";
    testPluginJob.url = TEST_JENKINS_URL + "/job/" + testPluginJob.name;

    testView.jobs = ArrayUtils.addAll(testView.jobs, testPluginJob);

    String testPluginJobLastSuccessfulBuild = testPluginJob.url + "/lastSuccessfulBuild";

    when(gsonMock.get(eq(testPluginJob.url + JENKINS_JSON_API)))
        .thenReturn(
            SmartJson.of(
                Map.of("lastSuccessfulBuild", Map.of("url", testPluginJobLastSuccessfulBuild))));

    String pluginVersionResourcePath = pluginName + "." + pluginFileExtension + "-version";
    String pluginJsonResourcePath = pluginName + ".json";
    SmartJson testBuildDetailJson =
        SmartJson.of(
            Map.of(
                "artifacts",
                List.of(
                    jsonArtifact(pluginName, pluginName + "." + pluginFileExtension),
                    jsonArtifact(pluginName, pluginVersionResourcePath),
                    jsonArtifact(pluginName, pluginJsonResourcePath),
                    jsonArtifact(pluginName, PLUGIN_LICENSE_RESOURCE_PATH)),
                "url",
                testPluginJobLastSuccessfulBuild,
                "actions",
                List.of(Map.of("lastBuiltRevision", Map.of("SHA1", TEST_JENKINS_JOB_BUILD_SHA1)))));
    when(gsonMock.get(eq(testPluginJobLastSuccessfulBuild + JENKINS_JSON_API)))
        .thenReturn(testBuildDetailJson);

    when(gsonMock.getReader(
            getArtifactUrl(
                testPluginJobLastSuccessfulBuild, pluginName, pluginVersionResourcePath)))
        .thenReturn(inputStreamOf(pluginVersion));
    when(gsonMock.getReader(
            getArtifactUrl(
                testPluginJobLastSuccessfulBuild, pluginName, PLUGIN_LICENSE_RESOURCE_PATH)))
        .thenReturn(inputStreamOf(pluginLicense));
    when(gsonMock.get(
            getArtifactUrl(testPluginJobLastSuccessfulBuild, pluginName, pluginJsonResourcePath)))
        .thenReturn(SmartJson.of(Map.of("description", pluginDescription)));
  }

  private static @NonNull String getArtifactUrl(
      String lastSuccessfulBuildUrl, String pluginName, String artifactPath) {
    return lastSuccessfulBuildUrl + "/artifact/" + pluginName + "/" + artifactPath;
  }

  private InputStreamReader inputStreamOf(String streamContent) {
    return new InputStreamReader(
        new ByteArrayInputStream(streamContent.getBytes(StandardCharsets.UTF_8)),
        StandardCharsets.UTF_8);
  }

  private JsonObject jsonArtifact(String pluginName, String artifactName) {
    JsonObject artifactJson = new JsonObject();
    artifactJson.addProperty("displayPath", artifactName);
    artifactJson.addProperty("fileName", artifactName);
    artifactJson.addProperty("relativePath", pluginName + "/" + artifactName);
    return artifactJson;
  }
}
