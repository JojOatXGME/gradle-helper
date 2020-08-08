Gradle Helper
=============

Gradle Plugin for common configurations in my projects. Although I
design this plugin around my personal projects, I try to keep it
universal. It is open for everyone who wants to use it.

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

### Shorthand for version ranges with dependency locking

If you want to use [version ranges], there are some pitfalls in Gradle.
you can use the following configuration for this plugin to avoid at
least some of them.

```groovy
gradleHelper {
    useVersionRangesWithDependencyLocking()
}
```

The function `useVersionRangesWithDependencyLocking()` makes a few
adjustments to the configuration.

 *  The function enables feature
    [**Rejecting pre-releases for version ranges**]. The feature rejects
    pre-releases like `1.4-SNAPSHOT` to prevent their usage in ranges
    like `[1.0, 2.0[`.

 *  The function enables [dependency locking] using the feature
    [**Global configuration for dependency locking**]. Dependency
    locking is an important concept to achieve reproducible builds with
    version ranges.

 *  The function enables feature preview `VERSION_ORDERING_V2` of
    Gradle. It is expected to become default with Gradle 7. It rejects
    versions like `2.0-Final` for ranges like `[1.0, 2.0[`. See
    [version ranges] for more details.

 *  The function enables feature preview `ONE_LOCKFILE_PER_PROJECT` of
    Gradle. It is expected to become default with Gradle 7. See [single
    lock file per project] for more details.

### Rejecting pre-releases for version ranges

_:warning: This feature will not prevent Gradle from using `2.0-Final`
in ranges like `[1.0, 2.0[` since `2.0-Final` is not considered a
pre-release. Use Gradle's `enableFeaturePreview("VERSION_ORDERING_V2")`
to prevent such problems._

Using [version ranges] in Gradle is risky because Gradle will select the
latest version, even when it is a snapshot or pre-release. This plugin
can reject snapshots and pre-releases from module resolution.

```groovy
gradleHelper {
    dependencyResolution {
        rejectPreReleases = true
        whitelist 'com.example:some-module:*'
    }
}
```

The plugin considers each version with non-numeric parts as a
pre-release. There are a few exceptions for non-numeric parts that are
tolerated. The following list is a complete case-insensitive set of
tolerated parts.

 *  `release`
 *  `ga`
 *  `final`
 *  `sp`

Therefore, the feature accepts versions like `1.4`, `2.0.Final` and
`4.2sp1`, but rejects `1.4-SNAPSHOT` and `5.7.0-M1`. If you want to use
a pre-release, or a dependency does not confirm to the version schema,
you can use `whitelist` to whitelist specific modules and versions.

### Global configuration for dependency locking

This feature allows you to enable dependency locking for all
(sub-)projects at once.

```groovy
gradleHelper {
    dependencyLocking.enable = true
}
```

If you enable the feature, the plugin also creates a task named
`updateDependencies` on the *root project*. The task can be used to
update all dependencies and write new lockfiles.

```console
$ ./gradlew updateDependencies
```


[Gradle issue #12538]:
<https://github.com/gradle/gradle/issues/12538>
"Add encoding to JavaPluginExtension · Issue #12538 · gradle/gradle"
[version ranges]:
<https://docs.gradle.org/current/userguide/single_versions.html>
"Declaring Versions and Ranges"
[dependency locking]:
<https://docs.gradle.org/current/userguide/dependency_locking.html>
"Locking dependency versions"
[single lock file per project]:
<https://docs.gradle.org/current/userguide/dependency_locking.html#single_lock_file_per_project>
"Locking dependency versions – Single lock file per project"

[**Rejecting pre-releases for version ranges**]:
<#rejecting-pre-releases-for-version-ranges>
[**Global configuration for dependency locking**]:
<#global-configuration-for-dependency-locking>
