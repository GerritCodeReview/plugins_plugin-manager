load("@gerrit_api_version//:version.bzl", "GERRIT_API_VERSION")
load("@com_googlesource_gerrit_bazlets//tools:junit.bzl", "junit_tests")
load("@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl", "PLUGIN_TEST_DEPS", "gerrit_plugin")

gerrit_plugin(
    name = "plugin-manager",
    srcs = glob(["src/main/java/**/*.java"]),
    gerrit_api_version = GERRIT_API_VERSION,
    manifest_entries = [
        "Gerrit-PluginName: plugin-manager",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.manager.WebModule",
        "Gerrit-Module: com.googlesource.gerrit.plugins.manager.PluginModule",
        "Gerrit-ReloadMode: restart",
        "Implementation-Title: Plugin manager",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/plugin-manager",
    ],
    resources = glob(["src/main/resources/**/*"]),
)

junit_tests(
    name = "plugin_manager_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    data = ["//:release.war"],
    visibility = ["//visibility:public"],
    deps = PLUGIN_TEST_DEPS + [
        ":plugin-manager__plugin",
    ],
)
