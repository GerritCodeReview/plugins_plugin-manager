// Copyright (C) 2016 The Android Open Source Project
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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.comparing;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.common.Version;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorePluginsRepository implements PluginsRepository {
  private static final Logger log = LoggerFactory.getLogger(CorePluginsRepository.class);
  private static final String GERRIT_VERSION = Version.getVersion();
  private static final char WINDOWS_FILE_SEPARATOR = '\\';
  private static final char UNIX_FILE_SEPARATOR = '/';

  private final CorePluginsDescriptions pluginsDescriptions;
  private final String gerritWarFilenameOnUnix;
  private final File gerritWarFile;

  @Inject
  public CorePluginsRepository(SitePaths site, CorePluginsDescriptions pd) {
    this(site.gerrit_war.toFile(), site.gerrit_war.toString(), pd);
  }

  @VisibleForTesting
  public CorePluginsRepository(File gerritWarFile, String gerritWar, CorePluginsDescriptions pd) {
    this.pluginsDescriptions = pd;
    this.gerritWarFilenameOnUnix = gerritWar.replace(WINDOWS_FILE_SEPARATOR, UNIX_FILE_SEPARATOR);
    this.gerritWarFile = gerritWarFile;
  }

  @Nullable
  private PluginInfo extractPluginInfoFromJarEntry(JarEntry entry) {
    try {
      Path entryName = Paths.get(entry.getName());
      URI pluginUrl = new URI("jar:file:" + gerritWarFilenameOnUnix + "!/" + entry.getName());
      try (JarInputStream pluginJar = new JarInputStream(pluginUrl.toURL().openStream())) {
        return getManifestEntry(pluginJar)
            .map(
                m -> {
                  Attributes pluginAttributes = m.getMainAttributes();
                  String pluginName = pluginAttributes.getValue("Gerrit-PluginName");
                  return new PluginInfo(
                      pluginName,
                      pluginsDescriptions.get(pluginName).orElse(""),
                      pluginAttributes.getValue("Implementation-Version"),
                      "",
                      pluginUrl.toString());
                })
            .orElse(
                new PluginInfo(
                    dropSuffix(entryName.getFileName().toString(), ".jar"),
                    "",
                    "",
                    "",
                    pluginUrl.toString()));
      } catch (IOException e) {
        log.error("Unable to open plugin " + pluginUrl, e);
        return null;
      }
    } catch (URISyntaxException e) {
      log.error("Invalid plugin filename", e);
      return null;
    }
  }

  private String dropSuffix(String string, String suffix) {
    return string.endsWith(suffix)
        ? string.substring(0, string.length() - suffix.length())
        : string;
  }

  @Nullable
  private static Optional<Manifest> getManifestEntry(JarInputStream pluginJar) throws IOException {
    for (JarEntry entry = pluginJar.getNextJarEntry();
        entry != null;
        entry = pluginJar.getNextJarEntry()) {
      if (entry.getName().equals(JarFile.MANIFEST_NAME)) {
        return Optional.of(new Manifest(pluginJar));
      }
    }
    return Optional.empty();
  }

  @Override
  public ImmutableList<PluginInfo> list(String gerritVersion) throws IOException {
    if (!gerritVersion.equals(GERRIT_VERSION)) {
      log.warn(
          "No core plugins available for version {} which is different than "
              + "the current running Gerrit",
          gerritVersion);
      return ImmutableList.of();
    }

    if (gerritWarFile == null) {
      log.warn("Core plugins not available on non-war Gerrit distributions");
      return ImmutableList.of();
    }

    try (JarFile gerritWar = new JarFile(gerritWarFile)) {
      return gerritWar.stream()
          .filter(e -> e.getName().startsWith("WEB-INF/plugins") && e.getName().endsWith(".jar"))
          .map(this::extractPluginInfoFromJarEntry)
          .filter(Objects::nonNull)
          .sorted(comparing(p -> p.name))
          .collect(toImmutableList());
    }
  }
}
