[![Build Travis](https://img.shields.io/travis/jenkinsci/build-name-setter-plugin/master.svg)](https://travis-ci.org/jenkinsci/build-name-setter-plugin)
[![Build status](https://ci.appveyor.com/api/projects/status/niut5mwbxdnht3pt/branch/master?svg=true)](https://ci.appveyor.com/project/damianszczepanik/build-name-setter-plugin/branch/master)
# Build name setter plugin for Jenkins

This plugin sets the display name of a build to something other than #1, #2, #3, ... so that you can use an identifier
that makes more sense in your context. When you install this plugin, your job configuration page gets additional setting
that lets you specify a build name for each new build. Also this plugin updates environment build variable `BUILD_DISPLAY_NAME` so it's value can be used in other build steps.

This plugin can be used in two ways:

* Set build name at the begining and at the end of the build (both by default, it also can be ajusted)

![alt tag](./.README/Screenshot_build_env.png)

* Set build name between two build steps (as a separate build step)

![alt tag](./.README/Screenshot_build_step.png)

As the result you can obtain something like this:

![alt tag](./.README/Screenshot_build_name.png)

The power of this plugin is based on [Macro Token](https://wiki.jenkins.io/display/JENKINS/Token+Macro+Plugin) so take a look what features you can use.
