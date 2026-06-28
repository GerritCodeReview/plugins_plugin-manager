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

gerrit_plugin(
    name = "plugin-manager",
    srcs = SRCS,
    manifest_entries = MANIFEST_ENTRIES,
    resources = RESOURCES,
)

# EE11 (jakarta.servlet) flavour. The shared bazlets `flavour = "ee11"` rewrites
# the plugin's javax.servlet imports to jakarta.servlet, injects
# `Gerrit-Flavour: ee11`, compiles against the jakarta plugin API (in-tree:
# //plugins:plugin-lib-neverlink), and wraps the target in a flavour=ee11
# transition -- so building :plugin-manager-ee11 self-selects the jakarta config
# with no command-line flag. plugin-manager has no external servlet deps, so the
# transition alone flips its classpath. Build with:
#   bazelisk build //plugins/plugin-manager:plugin-manager-ee11
# (or both flavours at once:
#  `bazelisk build //plugins/plugin-manager:plugin-manager \
#      //plugins/plugin-manager:plugin-manager-ee11`).
# `dir_name = "plugin-manager"` keeps stamping/versioning shared with the default.
gerrit_plugin(
    name = "plugin-manager-ee11",
    srcs = SRCS,
    dir_name = "plugin-manager",
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
