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

# The sources are jakarta-canonical (the JGit-style reversed bridge). The
# unsuffixed jar is the EE11 default: it compiles the canonical sources
# directly, stamps `Gerrit-Flavour: ee11` and self-selects the jakarta
# configuration. The -ee8 jar is the legacy flavour, generated through the
# shared bazlets `to_javax` transform and stamping `Gerrit-Flavour: ee8`.
# Build one or both:
#   bazelisk build //plugins/plugin-manager:plugin-manager \
#       //plugins/plugin-manager:plugin-manager-ee8
gerrit_plugin(
    name = "plugin-manager",
    srcs = SRCS,
    canonical = "jakarta",
    flavour = "ee11",
    manifest_entries = MANIFEST_ENTRIES,
    resources = RESOURCES,
)

# `dir_name = "plugin-manager"` keeps stamping/versioning shared with the default.
gerrit_plugin(
    name = "plugin-manager-ee8",
    srcs = SRCS,
    canonical = "jakarta",
    dir_name = "plugin-manager",
    flavour = "ee8",
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
