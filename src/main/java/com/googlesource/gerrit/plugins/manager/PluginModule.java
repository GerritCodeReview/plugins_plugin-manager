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

import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.extensions.webui.TopMenu;
import com.google.inject.AbstractModule;
import com.google.inject.internal.UniqueAnnotations;
import com.googlesource.gerrit.plugins.manager.repository.CorePluginsRepository;
import com.googlesource.gerrit.plugins.manager.repository.JenkinsCiPluginsRepository;
import com.googlesource.gerrit.plugins.manager.repository.PluginsRepository;

public class PluginModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(String.class)
        .annotatedWith(PluginCanonicalWebUrlPath.class)
        .toProvider(PluginCanonicalWebUrlPathProvider.class);

    DynamicSet.bind(binder(), TopMenu.class).to(PluginManagerTopMenu.class);

    DynamicSet.setOf(binder(), PluginsRepository.class);
    DynamicSet.bind(binder(), PluginsRepository.class).to(JenkinsCiPluginsRepository.class);
    DynamicSet.bind(binder(), PluginsRepository.class).to(CorePluginsRepository.class);

    install(PluginsCentralCache.module());

    bind(LifecycleListener.class).annotatedWith(UniqueAnnotations.create()).to(OnStartStop.class);
  }
}
