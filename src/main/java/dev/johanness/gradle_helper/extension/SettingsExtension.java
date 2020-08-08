package dev.johanness.gradle_helper.extension;

import dev.johanness.gradle_helper.extension.block.DependencyLocking;
import dev.johanness.gradle_helper.extension.block.DependencyResolution;
import org.gradle.api.Action;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class SettingsExtension {
  private final @NotNull Settings settings;
  private final @NotNull Property<String> defaultJavaEncoding;
  private final @NotNull DependencyLocking dependencyLocking;
  private final @NotNull DependencyResolution dependencyResolution;

  @Inject
  public SettingsExtension(@NotNull Settings settings, @NotNull ObjectFactory objectFactory) {
    this.settings = settings;
    defaultJavaEncoding = objectFactory.property(String.class);
    dependencyLocking = objectFactory.newInstance(DependencyLocking.class);
    dependencyResolution = objectFactory.newInstance(DependencyResolution.class);
  }

  public void useVersionRangesWithDependencyLocking() {
    dependencyLocking.getEnable().set(true);
    dependencyResolution.getRejectPreReleases().set(true);
    settings.enableFeaturePreview("ONE_LOCKFILE_PER_PROJECT");
    settings.enableFeaturePreview("VERSION_ORDERING_V2");
  }

  public @NotNull Property<String> getDefaultJavaEncoding() {
    return defaultJavaEncoding;
  }

  public @NotNull DependencyLocking getDependencyLocking() {
    return dependencyLocking;
  }

  public void dependencyLocking(@NotNull Action<DependencyLocking> action) {
    action.execute(dependencyLocking);
  }

  public @NotNull DependencyResolution getDependencyResolution() {
    return dependencyResolution;
  }

  public void dependencyResolution(@NotNull Action<DependencyResolution> action) {
    action.execute(dependencyResolution);
  }

}
