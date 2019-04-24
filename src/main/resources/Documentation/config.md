Enablement
----------

The plugin-manager requires the ability to administer the plugins in Gerrit,
using the [Gerrit's `plugins.allowRemoteAdmin = true`][1] setting.

Configuration
-------------

The other plugin-specific settings are defined in the `[plugin-manager]` section
in the gerrit.config.

jenkinsUrl
:   URL of the Jenkins CI responsible for building and validating the plugins for
    the current stable branch of Gerrit.
    Default value: https://gerrit-ci.gerritforge.com


Plugin discovery
----------------

The compatible plugins are retrieved from a site of build artifacts that are
following the view setup of the gerrit-ci-scripts project. There is one view
per Gerrit stable branch (e.g. `Plugins-stable-3.0` contains all the artifacts
of the plugins built against Gerrit stable-3.0 branch.

Only the plugins with a job in the corresponding view and having at least one
successful build will be shown in the list and be discoverable.

It is possible to control the list of plugins discoverable by editing the
corresponding view.

[1]: ../../../Documentation/config-gerrit.html#plugins.allowRemoteAdmin
[2]: https://gerrit.googlesource.com/gerrit-ci-scripts