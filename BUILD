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

# The default (javax.servlet) jar. With the ee11 twin below, the plugin is
# explicitly flavour-managed: `flavour = "ee8"` stamps `Gerrit-Flavour: ee8`
# (loader-equivalent to the unmarked jar) and guards the target to the ee8
# configuration, so the ee11 wildcard pass skips the javax side instead of
# failing -- both wildcard passes stay green at every stage:
#   bazelisk test plugins/plugin-manager/...
#   bazelisk test --@com_googlesource_gerrit_bazlets//flags:flavour=ee11 \
#       plugins/plugin-manager/...
gerrit_plugin(
    name = "plugin-manager",
    srcs = SRCS,
    flavour = "ee8",
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

# EE8 tests, guarded to the ee8 configuration (the default). The WAR under
# test follows the flavour: the default tests inspect release.war.
gerrit_plugin_tests(
    name = "plugin_manager_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    data = ["//:release.war"],
    flavour = "ee8",
    visibility = ["//visibility:public"],
    deps = [
        ":plugin-manager__plugin",
    ],
)

# EE11 test twin: the same canonical test sources through the shared
# `to_jakarta` transform, executed against :plugin-manager-ee11__plugin and
# inspecting the matching flavour's WAR (release-ee11.war; the test reads
# the runfile name from PLUGIN_MANAGER_TEST_WAR). This also proves the
# core-plugins listing derives names from the Gerrit-PluginName manifest
# entry, not from the -ee11-suffixed jar file names inside the EE11 WAR.
# A test target cannot self-transition the flavour, so the twin is guarded
# to the ee11 configuration and runs under the flag pass.
gerrit_plugin_tests(
    name = "plugin_manager_tests-ee11",
    srcs = glob(["src/test/java/**/*.java"]),
    data = ["//:release-ee11"],
    env = {"PLUGIN_MANAGER_TEST_WAR": "release-ee11.war"},
    flavour = "ee11",
    visibility = ["//visibility:public"],
    deps = [
        ":plugin-manager-ee11__plugin",
    ],
)
