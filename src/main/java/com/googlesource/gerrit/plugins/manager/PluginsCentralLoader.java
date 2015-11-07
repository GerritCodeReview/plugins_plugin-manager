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

package com.googlesource.gerrit.plugins.manager;

import com.google.gerrit.common.Version;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.manager.repository.PluginInfo;
import com.googlesource.gerrit.plugins.manager.repository.PluginsRepository;

import java.io.IOException;
import java.util.Collection;

@Singleton
public class PluginsCentralLoader {

  private static final String GERRIT_VERSION = Version.getVersion();

  private final PluginsRepository repository;

  @Inject
  public PluginsCentralLoader(PluginsRepository repository) {
    this.repository = repository;
  }

  public Collection<PluginInfo> availablePlugins() throws IOException {
    return repository.list(GERRIT_VERSION);
  }
}
