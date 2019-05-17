load("//tools/bzl:junit.bzl", "junit_tests")
load("//tools/bzl:plugin.bzl", "PLUGIN_DEPS", "PLUGIN_TEST_DEPS", "gerrit_plugin")

gerrit_plugin(
    name = "plugin-manager",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: plugin-manager",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.manager.WebModule",
        "Gerrit-Module: com.googlesource.gerrit.plugins.manager.Module",
        "Gerrit-ReloadMode: restart",
        "Implementation-Title: Plugin manager",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/plugin-manager",
    ],
    resources = glob(["src/main/resources/**/*"]),
)

junit_tests(
    name = "plugin_manager_tests",
    srcs = glob(
        include = ["src/test/java/**/*.java"],
        exclude = ["src/test/java/com/googlesource/gerrit/plugins/manager/repository/PluginsRepositoryIT.java"],
    ),
    visibility = ["//visibility:public"],
    deps = PLUGIN_TEST_DEPS + [
        ":plugin-manager__plugin",
    ],
)

# This test can be run optimized by consuming gerrit.war from local Maven repository,
# or, alternatively download the published gerrit.war from Maven Central. Anyway, mark
# this change as manual, so that it should be invoked explicitly:
# $ bazel test plugins/plugin-manager:plugin_manager_plugins_repository_test
junit_tests(
    name = "plugin_manager_plugins_repository_test",
    srcs = glob(["src/test/java/com/googlesource/gerrit/plugins/manager/repository/PluginsRepositoryIT.java"]),
    tags = [
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = PLUGIN_TEST_DEPS + [
        ":plugin-manager__plugin",
        "//lib/commons:io",
    ],
)
