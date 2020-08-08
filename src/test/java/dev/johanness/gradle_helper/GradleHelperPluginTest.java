package dev.johanness.gradle_helper;

import dev.johanness._testutils.MavenRepositoryGenerator;
import dev.johanness._testutils.ProjectGenerator;
import groovy.json.StringEscapeUtils;
import org.gradle.testkit.runner.GradleRunner;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

final class GradleHelperPluginTest {
  @Nested
  final class DefaultJavaEncoding {
    @Test
    void sets_encoding(@TempDir Path tempDir) throws IOException {
      ProjectGenerator.generateGradleProject(tempDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "include 'subproject'\n")
          .rootProject(
              "apply plugin: 'java'\n" +
              "\n" +
              "assert compileJava.options.encoding == 'UTF-8'\n" +
              "assert compileTestJava.options.encoding == 'UTF-8'\n" +
              "assert javadoc.options.encoding == 'UTF-8'\n")
          .subProject("subproject",
              "apply plugin: 'java'\n" +
              "\n" +
              "assert compileJava.options.encoding == 'UTF-8'\n" +
              "assert compileTestJava.options.encoding == 'UTF-8'\n" +
              "assert javadoc.options.encoding == 'UTF-8'\n");

      GradleRunner.create()
          .withProjectDir(tempDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();
    }

    @Test
    void sets_custom_encoding(@TempDir Path tempDir) throws IOException {
      ProjectGenerator.generateGradleProject(tempDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  defaultJavaEncoding = 'windows-1252'" +
              "}\n" +
              "\n" +
              "include 'subproject'\n")
          .rootProject(
              "apply plugin: 'java'\n" +
              "\n" +
              "assert compileJava.options.encoding == 'windows-1252'\n" +
              "assert compileTestJava.options.encoding == 'windows-1252'\n" +
              "assert javadoc.options.encoding == 'windows-1252'\n")
          .subProject("subproject",
              "apply plugin: 'java'\n" +
              "\n" +
              "assert compileJava.options.encoding == 'windows-1252'\n" +
              "assert compileTestJava.options.encoding == 'windows-1252'\n" +
              "assert javadoc.options.encoding == 'windows-1252'\n");

      GradleRunner.create()
          .withProjectDir(tempDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"None", "DeFaUlT"})
    void disable_feature_for(String encoding, @TempDir Path tempDir) throws IOException {
      ProjectGenerator.generateGradleProject(tempDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  defaultJavaEncoding = '" + encoding + "'" +
              "}\n" +
              "\n" +
              "include 'subproject'\n")
          .rootProject(
              "apply plugin: 'java'\n" +
              "\n" +
              "assert compileJava.options.encoding == null\n" +
              "assert compileTestJava.options.encoding == null\n" +
              "assert javadoc.options.encoding == null\n")
          .subProject("subproject",
              "apply plugin: 'java'\n" +
              "\n" +
              "assert compileJava.options.encoding == null\n" +
              "assert compileTestJava.options.encoding == null\n" +
              "assert javadoc.options.encoding == null\n");

      GradleRunner.create()
          .withProjectDir(tempDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();
    }
  }

  @Nested
  final class UseVersionRangesWithDependencyLocking {
    @Test
    void enables_version_ordering_v2(@TempDir Path tempDir) throws IOException {
      URI mavenRepository = MavenRepositoryGenerator.generateMavenRepository(tempDir.resolve("maven"))
          .module("group", "module", "4.0-Final")
          .generate();

      Path projectDir = tempDir.resolve("gradle");
      ProjectGenerator.generateGradleProject(projectDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  useVersionRangesWithDependencyLocking()\n" +
              "}\n")
          .rootProject(
              projectRequestingModule(mavenRepository, "group:module:[3.0, 4.0["));

      GradleRunner.create()
          .withArguments("resolve")
          .withProjectDir(projectDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .buildAndFail();
    }

    @Test
    void enables_one_lockfile_per_project(@TempDir Path tempDir) throws IOException {
      ProjectGenerator.generateGradleProject(tempDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  useVersionRangesWithDependencyLocking()\n" +
              "}\n")
          .rootProject(
              "apply plugin: 'java'\n");

      GradleRunner.create()
          .withArguments(":dependencies", "--write-locks")
          .withProjectDir(tempDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();

      Assertions.assertTrue(Files.exists(tempDir.resolve("gradle.lockfile")),
          "Lockfile of root project was not generated");
    }
  }

  @Nested
  final class DependencyResolution {
    @Test
    void keep_disabled_by_default(@TempDir Path tempDir) throws IOException {
      URI mavenRepository = MavenRepositoryGenerator.generateMavenRepository(tempDir.resolve("maven"))
          .module("group", "module", "4.0-Final")
          .module("group", "module", "4.1-dev")
          .generate();

      Path projectDir = tempDir.resolve("gradle");
      ProjectGenerator.generateGradleProject(projectDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "include 'wouldFailWithRejectPreReleases'\n" +
              "include 'wouldFailWithVersionOrderingV2'\n")
          .subProject("wouldFailWithRejectPreReleases",
              projectRequestingModule(mavenRepository, "group:module:4.1-dev"))
          .subProject("wouldFailWithVersionOrderingV2",
              projectRequestingModule(mavenRepository, "group:module:[3.0, 4.0)"));

      GradleRunner.create()
          .withArguments("resolve")
          .withProjectDir(projectDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.9_+_1", "1.0-ReLeAsE", "GA", "1.Final", "2.2sp4"})
    void accepts_version(String version, @TempDir Path tempDir) throws IOException {
      URI mavenRepository = MavenRepositoryGenerator.generateMavenRepository(tempDir.resolve("maven"))
          .module("group", "module", version)
          .generate();

      Path projectDir = tempDir.resolve("gradle");
      ProjectGenerator.generateGradleProject(projectDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  dependencyResolution {\n" +
              "    rejectPreReleases = true\n" +
              "  }\n" +
              "}\n")
          .rootProject(
              projectRequestingModule(mavenRepository, "group:module:" + version));

      GradleRunner.create()
          .withArguments("resolve")
          .withProjectDir(projectDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0-SNAPSHOT", "1.0dev1", "5.1-M1", "x"})
    void rejects_version(String version, @TempDir Path tempDir) throws IOException {
      URI mavenRepository = MavenRepositoryGenerator.generateMavenRepository(tempDir.resolve("maven"))
          .module("group", "module", version)
          .generate();

      Path projectDir = tempDir.resolve("gradle");
      ProjectGenerator.generateGradleProject(projectDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  dependencyResolution {\n" +
              "    rejectPreReleases = true\n" +
              "  }\n" +
              "}\n")
          .rootProject(
              projectRequestingModule(mavenRepository, "group:module:" + version));

      GradleRunner.create()
          .forwardOutput()
          .withArguments("resolve")
          .withProjectDir(projectDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .buildAndFail();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0-SNAPSHOT", "1.0dev1", "5.1-M1", "x"})
    void rejects_version_on_subproject(String version, @TempDir Path tempDir) throws IOException {
      URI mavenRepository = MavenRepositoryGenerator.generateMavenRepository(tempDir.resolve("maven"))
          .module("group", "module", version)
          .generate();

      Path projectDir = tempDir.resolve("gradle");
      ProjectGenerator.generateGradleProject(projectDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  dependencyResolution {\n" +
              "    rejectPreReleases = true\n" +
              "  }\n" +
              "}\n" +
              "\n" +
              "include 'subproject'\n")
          .subProject(
              "subproject",
              projectRequestingModule(mavenRepository, "group:module:" + version));

      GradleRunner.create()
          .withArguments("resolve")
          .withProjectDir(projectDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .buildAndFail();
    }

    @Test
    void accepts_whitelisted_version(@TempDir Path tempDir) throws IOException {
      URI mavenRepository = MavenRepositoryGenerator.generateMavenRepository(tempDir.resolve("maven"))
          .module("group", "module", "1.0-dev")
          .generate();

      Path projectDir = tempDir.resolve("gradle");
      ProjectGenerator.generateGradleProject(projectDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  dependencyResolution {\n" +
              "    rejectPreReleases = true\n" +
              "    whitelist 'group:module:*-dev'\n" +
              "  }\n" +
              "}\n")
          .rootProject(
              projectRequestingModule(mavenRepository, "group:module:1.0-dev"));

      GradleRunner.create()
          .withArguments("resolve")
          .withProjectDir(projectDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();
    }
  }

  @Nested
  final class DependencyLocking {
    @Test
    void keep_disabled_by_default(@TempDir Path tempDir) throws IOException {
      ProjectGenerator.generateGradleProject(tempDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "include 'subproject'\n")
          .rootProject(
              "apply plugin: 'java'\n" +
              "\n" +
              "assert tasks.findByName('updateDependencies') == null\n")
          .subProject("subproject",
              "apply plugin: 'java'\n" +
              "\n" +
              "assert tasks.findByName('updateDependencies') == null\n");

      GradleRunner.create()
          .withArguments("dependencies", "--write-locks")
          .withProjectDir(tempDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();

      Assertions.assertFalse(Files.exists(tempDir.resolve("gradle.lockfile")) ||
                             Files.exists(tempDir.resolve("gradle/dependency-locks")),
          "Lockfile of root project must not be generated");
      Assertions.assertFalse(Files.exists(tempDir.resolve("subproject/gradle.lockfile")) ||
                             Files.exists(tempDir.resolve("gradle/dependency-locks")),
          "Lockfile of subproject must not be generated");
    }

    @Test
    void enables_dependency_locking_when_requested(@TempDir Path tempDir) throws IOException {
      ProjectGenerator.generateGradleProject(tempDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  dependencyLocking.enable = true\n" +
              "}\n" +
              "\n" +
              "include 'subproject'\n")
          .rootProject(
              "apply plugin: 'java'\n" +
              "\n" +
              "assert tasks.findByName('updateDependencies') != null\n")
          .subProject("subproject",
              "apply plugin: 'java'\n" +
              "\n" +
              "assert tasks.findByName('updateDependencies') == null\n");

      GradleRunner.create()
          .withArguments(":dependencies", ":subproject:dependencies", "--write-locks")
          .withProjectDir(tempDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();

      Assertions.assertTrue(Files.exists(tempDir.resolve("gradle.lockfile")) ||
                            Files.exists(tempDir.resolve("gradle/dependency-locks")),
          "Lockfile of root project must be generated");
      Assertions.assertTrue(Files.exists(tempDir.resolve("subproject/gradle.lockfile")) ||
                            Files.exists(tempDir.resolve("gradle/dependency-locks")),
          "Lockfile of subproject must be generated");
    }

    @Test
    void updates_dependencies_for_all_projects_when_task_is_executed(@TempDir Path tempDir) throws IOException {
      ProjectGenerator.generateGradleProject(tempDir)
          .settings(
              "plugins {\n" +
              "  id 'dev.johanness.gradle-helper'\n" +
              "}\n" +
              "\n" +
              "gradleHelper {\n" +
              "  dependencyLocking.enable = true\n" +
              "}\n" +
              "\n" +
              "include 'subproject'\n")
          .rootProject(
              "apply plugin: 'java'\n")
          .subProject("subproject",
              "apply plugin: 'java'\n");

      GradleRunner.create()
          .withArguments("updateDependencies")
          .withProjectDir(tempDir.toFile())
          .withPluginClasspath()
          .withDebug(true)
          .build();

      Assertions.assertTrue(Files.exists(tempDir.resolve("gradle.lockfile")) ||
                            Files.exists(tempDir.resolve("gradle/dependency-locks")),
          "Lockfile of root project must be generated");
      Assertions.assertTrue(Files.exists(tempDir.resolve("subproject/gradle.lockfile")) ||
                            Files.exists(tempDir.resolve("gradle/dependency-locks")),
          "Lockfile of subproject must be generated");
    }
  }

  @Language("groovy")
  private @NotNull String projectRequestingModule(
      @NotNull URI mavenRepository,
      @Language(value = "groovy", prefix = "'", suffix = "'") @NotNull String moduleSpecifier)
  {
    return
        "apply plugin: 'java'\n" +
        "\n" +
        "repositories {\n" +
        "  maven {\n" +
        "    url = '" + StringEscapeUtils.escapeJava(mavenRepository.toString()) + "'\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "dependencies {\n" +
        "  implementation '" + moduleSpecifier + "'\n" +
        "}\n" +
        "\n" +
        "task resolve {\n" +
        "  doLast {\n" +
        "    logger.warn('Resolved: ' + configurations.compileClasspath.resolvedConfiguration.files)\n" +
        "  }\n" +
        "}\n";
  }
}
