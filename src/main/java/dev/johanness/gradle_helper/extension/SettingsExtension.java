package dev.johanness.gradle_helper.extension;

import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.LockMode;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class SettingsExtension {
  private final @NotNull Property<String> defaultJavaEncoding;
  private final @NotNull DependencyLocking dependencyLocking;
  private final @NotNull DependencyResolution dependencyResolution;

  @Inject
  public SettingsExtension(@NotNull ObjectFactory objectFactory) {
    defaultJavaEncoding = objectFactory.property(String.class);
    dependencyLocking = objectFactory.newInstance(DependencyLocking.class);
    dependencyResolution = objectFactory.newInstance(DependencyResolution.class);
  }

  public void enableDependencyLocking() {
    dependencyLocking.getEnabled().set(true);
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

  public static class DependencyLocking {
    private final @NotNull Property<Boolean> enabled;
    private final @NotNull Property<LockMode> lockMode;

    @Inject
    public DependencyLocking(@NotNull ObjectFactory objectFactory) {
      enabled = objectFactory.property(Boolean.class);
      lockMode = objectFactory.property(LockMode.class);
    }

    public @NotNull Property<Boolean> getEnabled() {
      return enabled;
    }

    public @NotNull Property<LockMode> getLockMode() {
      return lockMode;
    }
  }

  public static class DependencyResolution {
    private final @NotNull Property<Boolean> enabled;
    private final @NotNull ListProperty<String> whitelist;

    @Inject
    public DependencyResolution(@NotNull ObjectFactory objectFactory) {
      enabled = objectFactory.property(Boolean.class);
      whitelist = objectFactory.listProperty(String.class).empty();
    }

    public @NotNull Property<Boolean> getEnabled() {
      return enabled;
    }

    public @NotNull ListProperty<String> getWhitelist() {
      return whitelist;
    }

    public void whitelist(@NotNull String pattern) {
      whitelist.add(pattern);
    }
  }
}
