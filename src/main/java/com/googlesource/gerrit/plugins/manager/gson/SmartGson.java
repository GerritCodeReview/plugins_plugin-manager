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

package com.googlesource.gerrit.plugins.manager.gson;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gerrit.json.OutputFormat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class SmartGson {

  private final Gson gson;

  public SmartGson() {
    this.gson = OutputFormat.JSON.newGson();
  }

  public SmartJson get(String url) throws IOException {
    return SmartJson.of(gson.fromJson(getReader(url), JsonObject.class));
  }

  public SmartJson of(String jsonText) {
    return SmartJson.of(gson.fromJson(jsonText, JsonObject.class));
  }

  public <T> T get(String url, Class<T> classOfT) throws IOException {
    try (Reader reader = getReader(url)) {
      return gson.fromJson(reader, classOfT);
    }
  }

  private InputStreamReader getReader(String url) throws IOException {
    URL ciUrl;
    try {
      ciUrl = URI.create(url).toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Internal error: Gerrit CI URL seems to be malformed", e);
    }
    return new InputStreamReader(ciUrl.openStream(), UTF_8);
  }
}
