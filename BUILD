load("@com_googlesource_gerrit_bazlets//tools:junit.bzl", "junit_tests")
load("@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "plugin-manager",
    srcs = glob(["src/main/java/**/*.java"]),
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
    deps = [
        ":plugin-manager__plugin",
        "//java/com/google/gerrit/acceptance:lib",
        "//plugins:plugin-lib",
    ],
)
