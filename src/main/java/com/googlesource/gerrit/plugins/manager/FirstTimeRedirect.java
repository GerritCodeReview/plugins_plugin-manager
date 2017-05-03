// Copyright (C) 2017 The Android Open Source Project
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

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FirstTimeRedirect implements Filter {
  private static Logger log = LoggerFactory.getLogger(FirstTimeRedirect.class);

  private final SitePaths sitePaths;
  private final String redirectLocation;

  private boolean firstTime = true;

  @Inject
  FirstTimeRedirect(@GerritServerConfig Config config, SitePaths sitePaths) {
    this.sitePaths = sitePaths;
    this.redirectLocation = config.getString("httpd", null, "firstTimeRedirectUrl");
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (firstTime && isFirstTime()) {
      ((HttpServletResponse) response).sendRedirect(redirectLocation);
      firstTimeDone();
    } else {
      chain.doFilter(request, response);
    }
  }

  private void firstTimeDone() {
    try {
      firstTimeFile().createNewFile();
    } catch (IOException e) {
      log.error("Unable to mark first time redirect flag file", e);
      firstTime = false;
    }
  }

  private boolean isFirstTime() {
    if (!firstTime) {
      return false;
    }

    return (firstTime = redirectLocation != null && !firstTimeFile().exists());
  }

  private File firstTimeFile() {
    return sitePaths.resolve(".firstTimeRedirect").toFile();
  }

  @Override
  public void destroy() {
  }

}
