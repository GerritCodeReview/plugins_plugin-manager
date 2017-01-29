load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "plugin-manager",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/**/*"]),
    manifest_entries = [
        "Gerrit-PluginName: plugin-manager",
        "Gerrit-ApiType: plugin",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.manager.WebModule",
        "Gerrit-Module: com.googlesource.gerrit.plugins.manager.Module",
        "Gerrit-ReloadMode: restart",
        "Implementation-Title: Plugin manager",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/plugin-manager",
    ],
    provided_deps = [
        "//lib:gson",
        "//lib/log:log4j",
    ],
)
