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
    the different stable branches of Gerrit.

    Default value: https://gerrit-ci.gerritforge.com

[1]: ../../../Documentation/config-gerrit.html#plugins.allowRemoteAdmin
