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
# Each flavour's targets are guarded to their configuration, so both
# wildcard passes stay green:
#   bazelisk test plugins/plugin-manager/...
#   bazelisk test --@com_googlesource_gerrit_bazlets//flags:flavour=ee8 \
#       plugins/plugin-manager/...
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

# Tests for the unsuffixed (EE11 default) jar, guarded to the ee11
# configuration. The WAR under test follows the flavour: since the flip,
# release.war IS the jakarta WAR.
gerrit_plugin_tests(
    name = "plugin_manager_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    canonical = "jakarta",
    data = ["//:release.war"],
    flavour = "ee11",
    visibility = ["//visibility:public"],
    deps = [
        ":plugin-manager__plugin",
    ],
)

# EE8 legacy test twin: executed against the generated
# :plugin-manager-ee8__plugin and inspecting the legacy WAR
# (release-ee8.war; the test reads the runfile name from
# PLUGIN_MANAGER_TEST_WAR). A test target cannot self-transition the
# flavour, so the twin is guarded to the ee8 configuration and runs under
# the ee8 flag pass.
gerrit_plugin_tests(
    name = "plugin_manager_tests-ee8",
    srcs = glob(["src/test/java/**/*.java"]),
    canonical = "jakarta",
    data = ["//:release-ee8"],
    env = {"PLUGIN_MANAGER_TEST_WAR": "release-ee8.war"},
    flavour = "ee8",
    visibility = ["//visibility:public"],
    deps = [
        ":plugin-manager-ee8__plugin",
    ],
)
