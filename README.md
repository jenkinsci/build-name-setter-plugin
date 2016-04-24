# Jenkins-build-name-setter-plugin

This plugin sets the display name of a build to something other than #1, #2, #3, ... so that you can use an identifier
that makes more sense in your context. When you install this plugin, your job configuration page gets additional setting
that lets you specify a build name for each new build. Also this plugin updates environment build variable BUILD_DISPLAY_NAME so it's value can be used in other build steps.

This plugin can be used in two ways:

1) Set build name at the begining and at the end of the build (both by default, it also can be ajusted)
![alt tag](./Screenshot_build_env.png)

2) Set build name between two build steps (as a separate build step)
![alt tag](./Screenshot_build_step.png)

As the result you can obtain something like this:
![alt tag](./Screenshot_build_name.png)

Memo:
token-macro is not optional from version:1.6.0
