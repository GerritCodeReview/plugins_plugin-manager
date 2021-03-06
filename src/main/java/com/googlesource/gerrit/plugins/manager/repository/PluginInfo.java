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

import com.google.gerrit.extensions.restapi.Url;

public class PluginInfo {
  public final String id;
  public final String name;
  public final String description;
  public final String version;
  public final String sha1;
  public final String url;

  public PluginInfo(String name, String description, String version, String sha1, String url) {
    this.id = Url.encode(name);
    this.name = name;
    this.description = description;
    this.version = version;
    this.sha1 = sha1;
    this.url = url;
  }
}
