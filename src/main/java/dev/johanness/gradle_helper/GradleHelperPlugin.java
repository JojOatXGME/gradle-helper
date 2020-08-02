package dev.johanness.gradle_helper;

import dev.johanness.gradle_helper._internal.DependencyWhitelist;
import dev.johanness.gradle_helper._internal.ExtensionUtil;
import dev.johanness.gradle_helper.extension.SettingsExtension;
import dev.johanness.gradle_helper.task.ResolveAndLock;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.LockMode;
import org.gradle.api.initialization.Settings;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class GradleHelperPlugin implements Plugin<Settings> {
  private static final @NotNull String EXTENSION_NAME = "gradleHelper";
  private static final @NotNull String RESOLVE_AND_LOCK_TASK_NAME = "resolveAndLock";

  private static final @NotNull Pattern RESOLVE_AND_LOCK_TASK_PATTERN =
      Pattern.compile(":?" + Pattern.quote(RESOLVE_AND_LOCK_TASK_NAME), Pattern.CASE_INSENSITIVE);

  // The pattern is based on https://docs.gradle.org/current/userguide/single_versions.html#version_ordering
  private static final @NotNull Pattern RELEASED_VERSION_PATTERN =
      Pattern.compile("(\\d|release|ga|final|sp|[-._+])+", Pattern.CASE_INSENSITIVE);

  @Override
  public void apply(@NotNull Settings settings) {
    SettingsExtension extension = settings.getExtensions().create(EXTENSION_NAME, SettingsExtension.class);

    if (settings.getStartParameter().getTaskNames().stream()
        .anyMatch(RESOLVE_AND_LOCK_TASK_PATTERN.asMatchPredicate())) {
      settings.getStartParameter().setWriteDependencyLocks(true);
      settings.getStartParameter().setRefreshDependencies(true);
    }

    settings.getGradle().settingsEvaluated(s -> afterEvaluation(s, extension));
    settings.getGradle().rootProject(p -> rootProject(p, extension));
    settings.getGradle().beforeProject(p -> allProjects(p, extension));
  }

  private static void afterEvaluation(@NotNull Settings settings, @NotNull SettingsExtension extension) {
    if (ExtensionUtil.isDependencyLockingEnabled(extension)) {
      settings.enableFeaturePreview("ONE_LOCKFILE_PER_PROJECT");
    }

    if (ExtensionUtil.isDependencyResolutionEnabled(extension)) {
      settings.enableFeaturePreview("VERSION_ORDERING_V2");
    }
  }

  private static void rootProject(@NotNull Project project, @NotNull SettingsExtension extension) {
    if (ExtensionUtil.isDependencyLockingEnabled(extension)) {
      project.getTasks().register(RESOLVE_AND_LOCK_TASK_NAME, ResolveAndLock.class, task -> {
        task.setDescription("Resolves and locks the dependencies of all projects");
      });
    }
  }

  private static void allProjects(@NotNull Project project, @NotNull SettingsExtension extension) {
    String defaultJavaEncoding = ExtensionUtil.getDefaultJavaEncoding(extension);
    if (defaultJavaEncoding != null) {
      project.getTasks().withType(JavaCompile.class,
          task -> task.getOptions().setEncoding(defaultJavaEncoding));
      project.getTasks().withType(Javadoc.class,
          task -> task.getOptions().setEncoding(defaultJavaEncoding));
    }

    if (ExtensionUtil.isDependencyLockingEnabled(extension)) {
      project.getDependencyLocking().lockAllConfigurations();
      LockMode lockMode = ExtensionUtil.getDependencyLockingLockMode(extension);
      if (lockMode != null) {
        project.getDependencyLocking().getLockMode().set(lockMode);
      }
    }

    if (ExtensionUtil.isDependencyResolutionEnabled(extension)) {
      DependencyWhitelist whitelist = ExtensionUtil.getDependencyResolutionWhitelist(extension);
      project.getConfigurations().all(configuration -> {
        configuration.getResolutionStrategy().getComponentSelection().all(selection -> {
          if (looksLikePreRelease(selection.getCandidate().getVersion()) &&
              !whitelist.contains(selection.getCandidate())) {
            selection.reject("gradle-helper: Looks like a pre-release");
          }
        });
      });
    }
  }

  private static boolean looksLikePreRelease(@NotNull String version) {
    return !RELEASED_VERSION_PATTERN.matcher(version).matches();
  }
}
