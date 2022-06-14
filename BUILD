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

# The sources are jakarta-canonical (the JGit-style reversed bridge): the
# default ee8 jar is generated through the shared bazlets `to_javax`
# transform and stamps `Gerrit-Flavour: ee8`; the ee11 jar compiles the
# canonical sources directly and self-selects the jakarta configuration.
# Build one or both:
#   bazelisk build //plugins/plugin-manager:plugin-manager \
#       //plugins/plugin-manager:plugin-manager-ee11
gerrit_plugin(
    name = "plugin-manager",
    srcs = SRCS,
    canonical = "jakarta",
    flavour = "ee8",
    manifest_entries = MANIFEST_ENTRIES,
    resources = RESOURCES,
)

# `dir_name = "plugin-manager"` keeps stamping/versioning shared with the default.
gerrit_plugin(
    name = "plugin-manager-ee11",
    srcs = SRCS,
    canonical = "jakarta",
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
