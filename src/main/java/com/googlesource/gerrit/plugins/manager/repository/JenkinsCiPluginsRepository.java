// Copyright (C) 2015 The Android Open Source Project
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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.flogger.FluentLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.manager.GerritVersionBranch;
import com.googlesource.gerrit.plugins.manager.PluginManagerConfig;
import com.googlesource.gerrit.plugins.manager.gson.SmartGson;
import com.googlesource.gerrit.plugins.manager.gson.SmartJson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class JenkinsCiPluginsRepository implements PluginsRepository {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final PluginManagerConfig config;

  private HashMap<String, List<PluginInfo>> cache = new HashMap<>();

  static class View {
    String name;
    Job[] jobs;
  }

  static class Job {
    String name;
    String url;
    String color;
  }

  private final Provider<SmartGson> gsonProvider;

  @Inject
  public JenkinsCiPluginsRepository(Provider<SmartGson> gsonProvider, PluginManagerConfig config) {
    this.gsonProvider = gsonProvider;
    this.config = config;
  }

  @Override
  public List<PluginInfo> list(String gerritVersion) throws IOException {
    List<PluginInfo> list = cache.get(gerritVersion);
    if (list == null) {
      list = getList(gerritVersion);
      cache.put(gerritVersion, list);
    }
    return list;
  }

  private List<PluginInfo> getList(String gerritVersion) throws IOException {
    SmartGson gson = gsonProvider.get();
    String viewName = "Plugins-" + GerritVersionBranch.getBranch(gerritVersion);
    List<PluginInfo> plugins = new ArrayList<>();

    try {
      Job[] jobs =
          gson.get(config.getJenkinsUrl() + "/view/" + viewName + "/api/json", View.class).jobs;

      for (Job job : jobs) {
        if (job.color.equals("blue")) {
          Optional<PluginInfo> pluginInfo = getPluginInfo(gson, job.url);
          if (pluginInfo.isPresent()) {
            plugins.add(pluginInfo.get());
          }
        }
      }
    } catch (FileNotFoundException e) {
      logger.atWarning().withCause(e).log(
          "No plugins available for Gerrit version %s", gerritVersion);
    }

    return plugins;
  }

  private Optional<PluginInfo> getPluginInfo(final SmartGson gson, String url) throws IOException {
    SmartJson jobDetails = gson.get(url + "/api/json");
    Optional<SmartJson> lastSuccessfulBuild = jobDetails.getOptional("lastSuccessfulBuild");

    return lastSuccessfulBuild.flatMap(
        new Function<SmartJson, Optional<PluginInfo>>() {
          @Override
          public Optional<PluginInfo> apply(SmartJson build) {
            String buildUrl = build.getString("url");
            return getPluginArtifactInfo(buildUrl);
          }
        });
  }

  private Optional<PluginInfo> getPluginArtifactInfo(String url) {
    Optional<SmartJson> buildExecution = tryGetJson(url + "/api/json");
    Optional<JsonArray> artifacts =
        buildExecution.map(json -> json.get("artifacts").get().getAsJsonArray());
    if (artifacts.orElseGet(() -> new JsonArray()).size() == 0) {
      return Optional.empty();
    }

    Optional<SmartJson> artifactJavaJson = artifacts.flatMap(a -> findArtifact(a, ".jar"));
    if (artifactJavaJson.isPresent()) {
      Optional<PluginInfo> javaArtifact =
          getJavaPluginArtifactInfo(buildExecution, artifacts, artifactJavaJson);
      if (javaArtifact.isPresent()) {
        return javaArtifact;
      }
    }

    Optional<SmartJson> artifactJsJson = artifacts.flatMap(a -> findArtifact(a, ".js"));
    if (artifactJsJson.isPresent()) {
      Optional<PluginInfo> jsArtifact =
          getJsPluginArtifactInfo(buildExecution, artifacts, artifactJsJson);
      if (jsArtifact.isPresent()) {
        return jsArtifact;
      }
    }

    return Optional.empty();
  }

  private Optional<PluginInfo> getJavaPluginArtifactInfo(
      Optional<SmartJson> buildExecution,
      Optional<JsonArray> artifacts,
      Optional<SmartJson> artifactJson) {
    String pluginPath = artifactJson.get().getString("relativePath");

    String[] pluginPathParts = pluginPath.split("/");
    String pluginName =
        isMavenBuild(pluginPathParts)
            ? fixPluginNameForMavenBuilds(pluginPathParts)
            : pluginNameOfJar(pluginPathParts);

    String pluginUrl =
        String.format("%s/artifact/%s", buildExecution.get().getString("url"), pluginPath);

    Optional<String> pluginVersion =
        fetchArtifact(buildExecution.get(), artifacts.get(), ".jar-version");
    Optional<String> pluginDescription =
        fetchArtifactJson(buildExecution.get(), artifacts.get(), ".json")
            .flatMap(json -> json.getOptionalString("description"));

    for (JsonElement elem : buildExecution.get().get("actions").get().getAsJsonArray()) {
      SmartJson elemJson = SmartJson.of(elem);
      Optional<SmartJson> lastBuildRevision = elemJson.getOptional("lastBuiltRevision");

      if (lastBuildRevision.isPresent()) {
        String sha1 = lastBuildRevision.get().getString("SHA1").substring(0, 8);
        return pluginVersion.map(
            version ->
                new PluginInfo(pluginName, pluginDescription.orElse(""), version, sha1, pluginUrl));
      }
    }

    return Optional.empty();
  }

  private Optional<PluginInfo> getJsPluginArtifactInfo(
      Optional<SmartJson> buildExecution,
      Optional<JsonArray> artifacts,
      Optional<SmartJson> artifactJson) {
    String pluginPath = artifactJson.get().getString("relativePath");

    String[] pluginPathParts = pluginPath.split("/");
    String pluginName = pluginNameOfJs(pluginPathParts);

    String pluginUrl =
        String.format("%s/artifact/%s", buildExecution.get().getString("url"), pluginPath);

    Optional<String> pluginVersion =
        fetchArtifact(buildExecution.get(), artifacts.get(), ".js-version");
    Optional<String> pluginDescription =
        fetchArtifactJson(buildExecution.get(), artifacts.get(), ".json")
            .flatMap(json -> json.getOptionalString("description"));

    for (JsonElement elem : buildExecution.get().get("actions").get().getAsJsonArray()) {
      SmartJson elemJson = SmartJson.of(elem);
      Optional<SmartJson> lastBuildRevision = elemJson.getOptional("lastBuiltRevision");

      if (lastBuildRevision.isPresent()) {
        String sha1 = lastBuildRevision.get().getString("SHA1").substring(0, 8);
        return pluginVersion.map(
            version ->
                new PluginInfo(pluginName, pluginDescription.orElse(""), version, sha1, pluginUrl));
      }
    }

    return Optional.empty();
  }

  private Optional<String> fetchArtifact(
      SmartJson buildExecution, JsonArray artifacts, String artifactSuffix) {
    StringBuilder artifactBody = new StringBuilder();
    Optional<SmartJson> verArtifactJson = findArtifact(artifacts, artifactSuffix);
    if (verArtifactJson.isPresent()) {
      String versionUrl =
          String.format(
              "%s/artifact/%s",
              buildExecution.getString("url"), verArtifactJson.get().getString("relativePath"));
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(URI.create(versionUrl).toURL().openStream(), UTF_8), 4096)) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (artifactBody.length() > 0) {
            artifactBody.append("\n");
          }
          artifactBody.append(line);
        }
      } catch (Exception e) {
        logger.atSevere().log("Unable to fetch artifact from %s", versionUrl);
        return Optional.empty();
      }
    }
    return Optional.of(artifactBody.toString());
  }

  private Optional<SmartJson> fetchArtifactJson(
      SmartJson buildExecution, JsonArray artifacts, String artifactSuffix) {
    Optional<SmartJson> jsonArtifact = findArtifact(artifacts, artifactSuffix);
    return jsonArtifact.flatMap(
        artifactJson ->
            tryGetJson(
                String.format(
                    "%s/artifact/%s",
                    buildExecution.getString("url"),
                    jsonArtifact.get().getString("relativePath"))));
  }

  private Optional<SmartJson> tryGetJson(String url) {
    try {
      return Optional.of(gsonProvider.get().get(url));
    } catch (IOException e) {
      logger.atSevere().withCause(e).log("Cannot get JSON from %s", url);
      return Optional.empty();
    }
  }

  @VisibleForTesting
  static String fixPluginNameForMavenBuilds(String[] pluginPathParts) {
    String mavenPluginFilename =
        StringUtils.substringBeforeLast(pluginPathParts[pluginPathParts.length - 1], ".");
    int versionDelim = mavenPluginFilename.indexOf('-');
    return versionDelim > 0 ? mavenPluginFilename.substring(0, versionDelim) : mavenPluginFilename;
  }

  private String pluginNameOfJar(String[] pluginJarParts) {
    int filePos = pluginJarParts.length - 1;
    int pathPos = filePos - 1;

    if (pluginJarParts[filePos].startsWith(pluginJarParts[pathPos])) {
      return pluginJarParts[pathPos];
    }

    int jarExtPos = pluginJarParts[filePos].indexOf(".jar");
    return pluginJarParts[filePos].substring(0, jarExtPos);
  }

  private String pluginNameOfJs(String[] pluginJsParts) {
    int filePos = pluginJsParts.length - 1;
    int pathPos = filePos - 1;

    if (pluginJsParts[filePos].startsWith(pluginJsParts[pathPos])) {
      return pluginJsParts[pathPos];
    }

    int jsExtPos = pluginJsParts[filePos].indexOf(".js");
    return pluginJsParts[filePos].substring(0, jsExtPos);
  }

  @VisibleForTesting
  static boolean isMavenBuild(String[] pluginPathParts) {
    return pluginPathParts[pluginPathParts.length - 2].equals("target");
  }

  private Optional<SmartJson> findArtifact(JsonArray artifacts, String string) {
    for (int i = 0; i < artifacts.size(); i++) {
      SmartJson artifact = SmartJson.of(artifacts.get(i));
      String path = artifact.getString("relativePath");
      if (path.endsWith(string) && !path.endsWith("-static" + string)) {
        return Optional.of(artifact);
      }
    }

    return Optional.empty();
  }
}
