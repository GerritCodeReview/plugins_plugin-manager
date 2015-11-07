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
import com.google.gerrit.server.cache.CacheModule;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.UniqueAnnotations;
import com.google.inject.servlet.ServletModule;

import com.googlesource.gerrit.plugins.manager.PluginsCentralLoader.ListKey;
import com.googlesource.gerrit.plugins.manager.repository.JenkinsCiPluginsRepository;
import com.googlesource.gerrit.plugins.manager.repository.PluginInfo;
import com.googlesource.gerrit.plugins.manager.repository.PluginsRepository;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class WebModule extends ServletModule {

  @Override
  protected void configureServlets() {
    install(new CacheModule() {
      @Override
      protected void configure() {
        cache(PluginsCentralCache.PLUGINS_LIST_CACHE_NAME, ListKey.class,
            new TypeLiteral<Collection<PluginInfo>>() {}).expireAfterWrite(1,
            TimeUnit.HOURS).loader(PluginsCentralLoader.class);
      }
    });
    bind(PluginsCentralCache.class);

    bind(PluginsRepository.class).to(JenkinsCiPluginsRepository.class);
    bind(LifecycleListener.class).annotatedWith(UniqueAnnotations.create()).to(
        OnStartStop.class);

    bind(AvailablePluginsCollection.class);

    serve("/available*").with(PluginManagerRestApiServlet.class);

    filterRegex(".*\\.js").through(XAuthFilter.class);
  }
}
