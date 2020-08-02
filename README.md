Gradle Helper
=============

Gradle Plugin for common configurations. I use this plugin for my own
projects but feel free to use it yourself.

Features
--------

### Consistent encoding for Java files (enabled by default)

As mentioned in [Gradle issue #12538], Gradle uses a platform dependent
encoding when compiling Java sources. To enable consistent builds across
different platforms, you have to specify the encoding for every task.

```groovy
tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}
tasks.withType(Javadoc) {
    options.encoding = 'UTF-8'
}
```

This plugin takes the work out of your hands. It uses *UTF-8* by
default. You can specify another encoding in `settings.gradle`.

```groovy
gradleHelper {
    defaultJavaEncoding = 'windows-1252'
}
```

You can disable the feature by setting `defaultJavaEncoding` to `'none'`
or `'default'`.

### Enhanced module resolution (enabled by default)

Using [version ranges] in Gradle is risky because Gradle will select the
latest version, even when it is a snapshot or pre-release. This plugin
resolves such issues by

 *  enabling feature preview `VERSION_ORDERING_V2`, and
 *  rejecting snapshots and pre-releases.

The plugin considers each version with non-numeric non-whitelisted parts
as a pre-release. The following non-numeric parts are currently
whitelisted. The whitelist is case-insensitive.

 *  `release`
 *  `ga`
 *  `final`
 *  `sp`

If you want to use a pre-release, or a dependency does not confirm to
the naming convention, you can whitelist specific modules and versions.

```groovy
gradleHelper {
    dependencyResolution {
        whitelist 'com.example:some-module:*'
    }
}
```

You can disable the feature by setting `dependencyResolution.enabled` to
`false`.

```groovy
gradleHelper {
    dependencyResolution.enabled = false
}
```

### Global configuration for dependency locking

By calling `enableDependencyLocking()`, this plugin allows you to enable
dependency locking for all (sub-)projects at once.

```groovy
gradleHelper {
    enableDependencyLocking()
}
```

This function also enables feature preview `ONE_LOCKFILE_PER_PROJECT`.
The task `resolveAndLock` on the *root project* can be used to update
the dependencies.


[Gradle issue #12538]:
<https://github.com/gradle/gradle/issues/12538>
"Add encoding to JavaPluginExtension · Issue #12538 · gradle/gradle"
[version ranges]:
<https://docs.gradle.org/current/userguide/single_versions.html>
"Declaring Versions and Ranges"
