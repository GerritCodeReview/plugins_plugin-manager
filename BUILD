load("@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl", "gerrit_plugin", "gerrit_plugin_tests")

SRCS = glob(["src/main/java/**/*.java"])

RESOURCES = glob(["src/main/resources/**/*"])

MANIFEST_ENTRIES = [
    "Gerrit-PluginName: plugin-manager",
    "Gerrit-HttpModule: com.googlesource.gerrit.plugins.manager.WebModule",
    "Gerrit-Module: com.googlesource.gerrit.plugins.manager.PluginModule",
    "Gerrit-ReloadMode: restart",
    "Implementation-Title: Plugin manager",
    "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/plugin-manager",
]

# The sources are jakarta (Servlet API 6.1); the jar stamps
# `Gerrit-Flavour: ee11`. The EE8 flavour is gone with Gerrit's EE8
# retirement; javax consumers stay on the last -ee8 release.
gerrit_plugin(
    name = "plugin-manager",
    srcs = SRCS,
    canonical = "jakarta",
    flavour = "ee11",
    manifest_entries = MANIFEST_ENTRIES,
    resources = RESOURCES,
)


gerrit_plugin_tests(
    name = "plugin_manager_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    data = ["//:release.war"],
    visibility = ["//visibility:public"],
    deps = [
        ":plugin-manager__plugin",
    ],
)
