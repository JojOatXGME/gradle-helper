package dev.johanness.gradle_helper._internal;

import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DependencyWhitelist {
  private static final @NotNull Pattern SPECIAL_SEQUENCE_PATTERN =
      Pattern.compile("\\*|[\\\\]{2}|\\\\\\*");

  private final @NotNull List<Pattern> whitelistPatterns;

  public DependencyWhitelist(@NotNull List<String> whitelist) {
    whitelistPatterns = whitelist.stream()
        .map(DependencyWhitelist::toPattern)
        .collect(Collectors.toList());
  }

  public boolean contains(@NotNull ModuleComponentIdentifier candidate) {
    String stringRepresentation = candidate.getDisplayName();
    for (Pattern pattern : whitelistPatterns) {
      if (pattern.matcher(stringRepresentation).matches()) {
        return true;
      }
    }
    return false;
  }

  private static @NotNull Pattern toPattern(@NotNull String globExpression) {
    StringBuilder pattern = new StringBuilder();
    StringBuilder mustBeQuoted = new StringBuilder();
    Matcher matcher = SPECIAL_SEQUENCE_PATTERN.matcher(globExpression);
    while (matcher.find()) {
      matcher.appendReplacement(mustBeQuoted, "");
      switch (matcher.group()) {
        case "*":
          pattern.append(Pattern.quote(remove(mustBeQuoted)));
          pattern.append("[^:]*");
          break;
        case "\\\\":
          mustBeQuoted.append("\\");
          break;
        case "\\*":
          mustBeQuoted.append('*');
          break;
        default:
          throw new AssertionError("Must not match regex");
      }
    }
    matcher.appendTail(mustBeQuoted);
    pattern.append(Pattern.quote(remove(mustBeQuoted)));
    return Pattern.compile(pattern.toString());
  }

  private static @NotNull String remove(@NotNull StringBuilder stringBuilder) {
    String str = stringBuilder.toString();
    stringBuilder.setLength(0);
    return str;
  }
}
