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
# Each flavour's targets are guarded to their configuration, so both
# wildcard passes stay green:
#   bazelisk test plugins/plugin-manager/...
#   bazelisk test --@com_googlesource_gerrit_bazlets//flags:flavour=ee11 \
#       plugins/plugin-manager/...
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

# EE8 tests, guarded to the ee8 configuration (the default). The WAR under
# test follows the flavour: the default tests inspect release.war.
gerrit_plugin_tests(
    name = "plugin_manager_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    canonical = "jakarta",
    data = ["//:release.war"],
    flavour = "ee8",
    visibility = ["//visibility:public"],
    deps = [
        ":plugin-manager__plugin",
    ],
)

# EE11 test twin: the canonical test sources compiled directly (no
# transform -- since the jakarta-canonical migration the EE11 side IS the
# canonical side), executed against :plugin-manager-ee11__plugin and
# inspecting the matching flavour's WAR (release-ee11.war; the test reads
# the runfile name from PLUGIN_MANAGER_TEST_WAR). This also proves the
# core-plugins listing derives names from the Gerrit-PluginName manifest
# entry, not from the -ee11-suffixed jar file names inside the EE11 WAR.
# A test target cannot self-transition the flavour, so the twin is guarded
# to the ee11 configuration and runs under the flag pass.
gerrit_plugin_tests(
    name = "plugin_manager_tests-ee11",
    srcs = glob(["src/test/java/**/*.java"]),
    canonical = "jakarta",
    data = ["//:release-ee11"],
    env = {"PLUGIN_MANAGER_TEST_WAR": "release-ee11.war"},
    flavour = "ee11",
    visibility = ["//visibility:public"],
    deps = [
        ":plugin-manager-ee11__plugin",
    ],
)
