package dev.johanness.gradle_helper.extension.block;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class DependencyResolution {
  private final @NotNull Property<Boolean> rejectPreReleases;
  private final @NotNull ListProperty<String> whitelist;

  @Inject
  public DependencyResolution(@NotNull ObjectFactory objectFactory) {
    rejectPreReleases = objectFactory.property(Boolean.class);
    whitelist = objectFactory.listProperty(String.class).empty();
  }

  public @NotNull Property<Boolean> getRejectPreReleases() {
    return rejectPreReleases;
  }

  public @NotNull ListProperty<String> getWhitelist() {
    return whitelist;
  }

  public void whitelist(@NotNull String pattern) {
    whitelist.add(pattern);
  }
}
