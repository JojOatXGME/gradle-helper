package dev.johanness.gradle_helper.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.tasks.TaskAction;

public class ResolveAndLock extends DefaultTask {
  @TaskAction
  void run() {
    Gradle gradle = getProject().getGradle();

    if (!gradle.getStartParameter().isWriteDependencyLocks()) {
      throw new GradleException(
          "Command line flag '--write-locks' must be present.");
    }

    for (Project project : gradle.getRootProject().getAllprojects()) {
      project.getConfigurations().all(configuration -> {
        if (configuration.isCanBeResolved()) {
          configuration.resolve();
        }
      });
    }
  }
}
