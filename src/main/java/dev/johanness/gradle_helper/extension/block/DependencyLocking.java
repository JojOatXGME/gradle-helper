package dev.johanness.gradle_helper.extension.block;

import org.gradle.api.artifacts.dsl.LockMode;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class DependencyLocking {
  private final @NotNull Property<Boolean> enable;
  private final @NotNull Property<LockMode> lockMode;

  @Inject
  public DependencyLocking(@NotNull ObjectFactory objectFactory) {
    enable = objectFactory.property(Boolean.class);
    lockMode = objectFactory.property(LockMode.class);
  }

  public @NotNull Property<Boolean> getEnable() {
    return enable;
  }

  public @NotNull Property<LockMode> getLockMode() {
    return lockMode;
  }
}
