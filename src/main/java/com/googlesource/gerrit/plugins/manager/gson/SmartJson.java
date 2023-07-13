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

import com.google.common.base.Predicates;
import com.google.gson.JsonElement;
import java.util.Optional;

public class SmartJson {

  private final JsonElement jsonElem;

  private SmartJson(JsonElement elem) {
    this.jsonElem = elem;
  }

  public static SmartJson of(JsonElement fromJson) {
    return new SmartJson(fromJson);
  }

  public Optional<String> getOptionalString(String fieldName) {
    return getOptional(fieldName)
        .filter(elem -> !elem.jsonElem.isJsonNull())
        .map(
            (elem) -> {
              if (!elem.jsonElem.isJsonPrimitive()) {
                throw new IllegalArgumentException(
                    "cannot convert " + elem.jsonElem + " into a String");
              }
              return elem.jsonElem.getAsString();
            })
        .filter(Predicates.notNull());
  }

  public String getString(String fieldName) {
    return getOptionalString(fieldName).orElse("");
  }

  public Optional<SmartJson> getOptional(String fieldName) {
    if (jsonElem != null
        && !jsonElem.isJsonNull()
        && jsonElem.getAsJsonObject().get(fieldName) != null) {
      return Optional.of(SmartJson.of(jsonElem.getAsJsonObject().get(fieldName)));
    }
    return Optional.empty();
  }

  public SmartJson get(String fieldName) {
    return getOptional(fieldName).get();
  }

  public JsonElement get() {
    return jsonElem;
  }
}
