package dev.johanness.gradle_helper;

import dev.johanness.gradle_helper._internal.DependencyWhitelist;
import dev.johanness.gradle_helper._internal.ExtensionUtil;
import dev.johanness.gradle_helper.extension.SettingsExtension;
import dev.johanness.gradle_helper.task.UpdateDependencies;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.LockMode;
import org.gradle.api.initialization.Settings;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public final class GradleHelperPlugin implements Plugin<Settings> {
  private static final @NotNull String EXTENSION_NAME = "gradleHelper";
  private static final @NotNull String UPDATE_DEPENDENCIES_TASK_NAME = "updateDependencies";

  private static final @NotNull Pattern UPDATE_DEPENDENCIES_TASK_PATTERN =
      Pattern.compile(":?" + Pattern.quote(UPDATE_DEPENDENCIES_TASK_NAME), Pattern.CASE_INSENSITIVE);

  // The pattern is based on https://docs.gradle.org/current/userguide/single_versions.html#version_ordering
  private static final @NotNull Pattern RELEASED_VERSION_PATTERN =
      Pattern.compile("(\\d|release|ga|final|sp|[-._+])+", Pattern.CASE_INSENSITIVE);

  @Override
  public void apply(@NotNull Settings settings) {
    SettingsExtension extension = settings.getExtensions().create(
        EXTENSION_NAME,
        SettingsExtension.class,
        settings);

    settings.getGradle().settingsEvaluated(s -> {
      if (ExtensionUtil.enableDependencyLocking(extension)) {
        List<String> taskNames = settings.getStartParameter().getTaskNames();
        if (taskNames.stream().anyMatch(UPDATE_DEPENDENCIES_TASK_PATTERN.asMatchPredicate())) {
          // Unfortunately, I guess Gradle does not actually support this use of the API.
          // It works for now. There are tests that fail when it stops working.
          settings.getStartParameter().setWriteDependencyLocks(true);
          settings.getStartParameter().setRefreshDependencies(true);
        }
      }
    });

    settings.getGradle().rootProject(p -> rootProject(p, extension));
    settings.getGradle().beforeProject(p -> allProjects(p, extension));
  }

  private static void rootProject(@NotNull Project project, @NotNull SettingsExtension extension) {
    if (ExtensionUtil.enableDependencyLocking(extension)) {
      project.getTasks().register(UPDATE_DEPENDENCIES_TASK_NAME, UpdateDependencies.class, task -> {
        task.setDescription("Resolves and locks the dependencies of all projects in this build");
      });
    }
  }

  private static void allProjects(@NotNull Project project, @NotNull SettingsExtension extension) {
    String defaultJavaEncoding = ExtensionUtil.defaultJavaEncoding(extension);
    if (defaultJavaEncoding != null) {
      project.getTasks().withType(JavaCompile.class,
          task -> task.getOptions().setEncoding(defaultJavaEncoding));
      project.getTasks().withType(Javadoc.class,
          task -> task.getOptions().setEncoding(defaultJavaEncoding));
    }

    if (ExtensionUtil.enableDependencyLocking(extension)) {
      project.getDependencyLocking().lockAllConfigurations();
      LockMode lockMode = ExtensionUtil.dependencyLockingLockMode(extension);
      if (lockMode != null) {
        project.getDependencyLocking().getLockMode().set(lockMode);
      }
    }

    if (ExtensionUtil.rejectPreReleases(extension)) {
      DependencyWhitelist whitelist = ExtensionUtil.dependencyResolutionWhitelist(extension);
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
