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

var app = angular.module('PluginManager', []).controller(
    'LoadInstalledPlugins',
    function($scope, $http) {
      var plugins = this;

      plugins.list = {};

      plugins.available = {};

      $scope.refreshInstalled = function(hideAvailable) {
        $http.get('/plugins/?all', plugins.httpConfig).then(
            function successCallback(response) {
              plugins.list = response.data;
              if(hideAvailable) {
                $.each( plugins.list, function(id, props) {
                  var escapedVersion = props.version.replace(/\./g, "\\.");
                  $("tr#" + id + "-" + escapedVersion).addClass("hidden");
                });
              }
            }, function errorCallback(response) {
            });
      }

      $scope.install = function(id, url) {
        var pluginInstallData = { "url" : url };
        $("button#" + id).addClass("hidden");
        $("span#installing-" + id).removeClass("hidden");
        $http.put('/plugins/' + id + ".jar",
            pluginInstallData).then(
                function successCallback(response) {
                  $("span#installing-" + id).addClass("hidden");
                  $("span#installed-" + id).removeClass("hidden");
                  $scope.refreshInstalled(false);
            }, function errorCallback(response) {
              $("span#installing-" + id).addClass("hidden");
              $("span#failed-" + id).removeClass("hidden");
            });
      }
      
      $http.get('/plugins/plugin-manager/available', plugins.httpConfig).then(
          function successCallback(response) {
            plugins.available = response.data;
            $scope.refreshInstalled(true);
          }, function errorCallback(response) {
          });
      
    });

app.config(function($httpProvider) {
  $httpProvider.defaults.headers.common = {
    'X-Gerrit-Auth' : '@X-Gerrit-Auth'
  };
});