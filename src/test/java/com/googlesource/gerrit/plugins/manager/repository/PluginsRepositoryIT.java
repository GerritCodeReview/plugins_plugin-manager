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

import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gerrit.acceptance.UseLocalDisk;
import com.google.gerrit.common.Version;
import java.io.IOException;
import java.util.Collection;
import org.junit.Test;

@UseLocalDisk
@TestPlugin(
    name = "repository-manager",
    sysModule = "com.googlesource.gerrit.plugins.manager.Module")
public class PluginsRepositoryIT extends LightweightPluginDaemonTest {

  @Test
  public void corePluginsRepositoryShouldReturnOnePlugin() throws IOException {
    PluginsRepository pluginRepo = plugin.getSysInjector().getInstance(CorePluginsRepository.class);
    Collection<PluginInfo> plugins = pluginRepo.list(Version.getVersion());
    assertThat(plugins).isNotEmpty();
  }
}
