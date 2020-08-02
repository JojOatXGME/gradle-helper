package dev.johanness._testutils;

import groovy.json.StringEscapeUtils;
import org.gradle.testkit.runner.GradleRunner;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MavenRepositoryGenerator {
  private static final @NotNull String GRADLE_PROJECT_DIR = ".gradle";

  private final @NotNull URI repositoryUri;
  private final @NotNull Path projectRoot;
  private final @NotNull ProjectGenerator projectGenerator;
  private final @NotNull List<String> projectNames = new ArrayList<>();

  private MavenRepositoryGenerator(@NotNull Path repositoryRoot) {
    repositoryUri = repositoryRoot.toUri();
    projectRoot = repositoryRoot.resolve(GRADLE_PROJECT_DIR);
    projectGenerator = ProjectGenerator.generateGradleProject(projectRoot);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MavenRepositoryGenerator generateMavenRepository(@NotNull Path repositoryRoot) {
    return new MavenRepositoryGenerator(repositoryRoot);
  }

  @Contract("_, _, _ -> this")
  public @NotNull MavenRepositoryGenerator module(
      @NotNull String group,
      @NotNull String module,
      @NotNull String version)
      throws IOException
  {
    String projectName = projectName(group, module, version);
    projectNames.add(projectName);
    projectGenerator.subProject(projectName,
        "apply plugin: 'java-library'\n" +
        "apply plugin: 'maven-publish'\n" +
        "\n" +
        "publishing {\n" +
        "  repositories {\n" +
        "    maven {\n" +
        "      url = '" + StringEscapeUtils.escapeJava(repositoryUri.toString()) + "'\n" +
        "    }\n" +
        "  }\n" +
        "  publications {\n" +
        "    maven(MavenPublication) {\n" +
        "      groupId = '" + group + "'\n" +
        "      artifactId = '" + module + "'\n" +
        "      version = '" + version + "'\n" +
        "      from components.java\n" +
        "    }\n" +
        "  }\n" +
        "}\n");
    return this;
  }

  public @NotNull URI generate() throws IOException {
    projectGenerator.settings(
        "rootProject.name = 'maven-repo'\n" +
        projectNames.stream()
            .map(name -> "include '" + name + "'\n")
            .collect(Collectors.joining()));

    GradleRunner.create()
        .withArguments("publishAllPublicationsToMavenRepository")
        .withProjectDir(projectRoot.toFile())
        .build();

    return repositoryUri;
  }

  private static @NotNull String projectName(
      @NotNull String group,
      @NotNull String module,
      @NotNull String version)
  {
    return group.replace('.', '_') +
           "__" + module +
           "__" + version.replaceAll("[-._+]", "_");
  }
}
