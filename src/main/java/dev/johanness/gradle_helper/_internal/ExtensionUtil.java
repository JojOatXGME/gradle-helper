package dev.johanness.gradle_helper._internal;

import dev.johanness.gradle_helper.extension.SettingsExtension;
import org.gradle.api.artifacts.dsl.LockMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public final class ExtensionUtil {
  private ExtensionUtil() {} // Cannot be instantiated.

  public static @Nullable String defaultJavaEncoding(@NotNull SettingsExtension extension) {
    String encoding = extension.getDefaultJavaEncoding().getOrElse("UTF-8");
    return encoding.equalsIgnoreCase("none") ||
           encoding.equalsIgnoreCase("default")
        ? null
        : encoding;
  }

  public static boolean enableDependencyLocking(@NotNull SettingsExtension extension) {
    return extension.getDependencyLocking().getEnable().getOrElse(false);
  }

  public static @Nullable LockMode dependencyLockingLockMode(@NotNull SettingsExtension extension) {
    return extension.getDependencyLocking().getLockMode().getOrNull();
  }

  public static boolean rejectPreReleases(@NotNull SettingsExtension extension) {
    return extension.getDependencyResolution().getRejectPreReleases().getOrElse(false);
  }

  public static @NotNull DependencyWhitelist dependencyResolutionWhitelist(@NotNull SettingsExtension extension) {
    return new DependencyWhitelist(
        extension.getDependencyResolution().getWhitelist()
            .getOrElse(Collections.emptyList()));
  }
}
