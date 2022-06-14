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

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.httpd.WebLoginListener;
import com.google.inject.jakarta.ServletModule;

public class WebModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(AvailablePluginsCollection.class);
    DynamicSet.bind(binder(), WebLoginListener.class).to(FirstWebLoginListener.class);

    serve("/available*").with(PluginManagerRestApiServlet.class);

    filterRegex(".*plugin-manager\\.js").through(XAuthFilter.class);
  }
}
