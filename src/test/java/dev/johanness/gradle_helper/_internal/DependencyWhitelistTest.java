package dev.johanness.gradle_helper._internal;

import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DependencyWhitelistTest {
  @Test
  void wildcard_does_not_match_colon() {
    DependencyWhitelist whitelist = new DependencyWhitelist(List.of("*"));
    assertNotContains(whitelist, candidate("group", "module", "version"));
  }

  @Test
  void wildcard_can_be_escaped() {
    DependencyWhitelist whitelist = new DependencyWhitelist(List.of("g:m:\\*"));
    assertContains(whitelist, candidate("g", "m", "*"));
    assertNotContains(whitelist, candidate("g", "m", "test"));
    assertNotContains(whitelist, candidate("g", "m", "\\*"));
  }

  @Test
  void escape_sequence_can_be_escaped() {
    DependencyWhitelist whitelist = new DependencyWhitelist(List.of("g:m:\\\\*"));
    assertContains(whitelist, candidate("g", "m", "\\test"));
    assertNotContains(whitelist, candidate("g", "m", "*"));
  }

  @Test
  void expressions_can_have_multiple_wildcards() {
    DependencyWhitelist whitelist = new DependencyWhitelist(List.of("g:*:v*-*"));
    assertContains(whitelist, candidate("g", "m1", "v1-1"));
    assertContains(whitelist, candidate("g", "m2", "v-"));
    assertNotContains(whitelist, candidate("g", "m", "1-1"));
    assertNotContains(whitelist, candidate("g", "m", "v11"));
  }

  @Test
  void multiple_expressions_can_be_listed() {
    DependencyWhitelist whitelist = new DependencyWhitelist(
        List.of("g:m1:*", "g:m2:*", "g:m3:*"));
    assertContains(whitelist, candidate("g", "m1", ""));
    assertContains(whitelist, candidate("g", "m2", ""));
    assertContains(whitelist, candidate("g", "m3", ""));
    assertNotContains(whitelist, candidate("g", "m4", ""));
  }

  private static @NotNull ModuleComponentIdentifier candidate(
      @NotNull String group,
      @NotNull String module,
      @NotNull String version)
  {
    return new DefaultModuleComponentIdentifier(DefaultModuleIdentifier.newId(group, module), version);
  }

  private static void assertContains(
      @NotNull DependencyWhitelist whitelist,
      @NotNull ModuleComponentIdentifier candidate)
  {
    assertTrue(whitelist.contains(candidate), "contains(" + candidate + ")");
  }

  private static void assertNotContains(
      @NotNull DependencyWhitelist whitelist,
      @NotNull ModuleComponentIdentifier candidate)
  {
    assertFalse(whitelist.contains(candidate), "!contains(" + candidate + ")");
  }
}
