package dev.johanness._testutils;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectGenerator {
  private final @NotNull Path projectRoot;

  private ProjectGenerator(@NotNull Path projectRoot) {
    this.projectRoot = projectRoot;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull ProjectGenerator generateGradleProject(@NotNull Path projectRoot) {
    return new ProjectGenerator(projectRoot);
  }

  @Contract("_ -> this")
  public @NotNull ProjectGenerator settings(
      @Language("groovy") @NotNull String sources)
      throws IOException
  {
    Files.createDirectories(projectRoot);
    Files.writeString(projectRoot.resolve("settings.gradle"), sources, StandardCharsets.UTF_8);
    return this;
  }

  @Contract("_ -> this")
  public @NotNull ProjectGenerator rootProject(
      @Language("groovy") @NotNull String sources)
      throws IOException
  {
    Files.createDirectories(projectRoot);
    Files.writeString(projectRoot.resolve("build.gradle"), sources, StandardCharsets.UTF_8);
    return this;
  }

  @Contract("_, _ -> this")
  public @NotNull ProjectGenerator subProject(
      @NotNull String subproject,
      @Language("groovy") @NotNull String sources)
      throws IOException
  {
    Path dir = projectRoot.resolve(Path.of("", subproject.split("[:/]")));
    Files.createDirectories(dir);
    Files.writeString(dir.resolve("build.gradle"), sources, StandardCharsets.UTF_8);
    return this;
  }
}
