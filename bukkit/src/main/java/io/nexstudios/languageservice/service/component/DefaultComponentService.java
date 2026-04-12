package io.nexstudios.languageservice.service.component;

import io.nexstudios.languageservice.service.path.StringPathService;
import io.nexstudios.serviceregistry.di.Dependencies;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Dependencies({
    StringPathService.class
})
public class DefaultComponentService implements ComponentService {

  private static final String DEFAULT_CONSOLE_LANGUAGE = "en";

  private static final Pattern LEGACY_HEX = Pattern.compile("(?i)[&§]#([0-9a-f]{6})");

  private final StringPathService stringPathService;

  /**
   * You can swap this for a customized MiniMessage builder if you want to restrict tags later.
   */
  private final MiniMessage miniMessage = MiniMessage.miniMessage();

  @Override
  public ComponentBuilder builder(Player player, String path, String def, boolean withPrefix) {
    return builder((CommandSender) player, path, def, withPrefix);
  }

  @Override
  public ComponentBuilder builder(CommandSender sender, String path, String def, boolean withPrefix) {
    Objects.requireNonNull(sender, "sender");
    Objects.requireNonNull(path, "path");

    String raw = resolveRawTranslation(sender, path, def);
    raw = withPrefix ? applyOptionalPrefix(sender, raw) : raw;

    return new ComponentBuilder(this, raw);
  }

  @Override
  public Component getComponent(Player player, String path, boolean withPrefix) {
    return getComponent(player, path, path, withPrefix);
  }

  @Override
  public Component getComponent(CommandSender sender, String path, boolean withPrefix) {
    return getComponent(sender, path, path, withPrefix);
  }

  @Override
  public Component getComponent(Player player, String path, String def, boolean withPrefix) {
    return getComponent((CommandSender) player, path, def, withPrefix);
  }

  @Override
  public Component getComponent(CommandSender sender, String path, String def, boolean withPrefix) {
    Objects.requireNonNull(sender, "sender");
    Objects.requireNonNull(path, "path");

    String raw = resolveRawTranslation(sender, path, def);
    raw = withPrefix ? applyOptionalPrefix(sender, raw) : raw;

    return parse(raw);
  }

  @Override
  public Component getComponent(Player player, String path, String def, TagResolver tagResolver, boolean withPrefix) {
    return getComponent((CommandSender) player, path, def, tagResolver, withPrefix);
  }

  @Override
  public Component getComponent(CommandSender sender, String path, String def, TagResolver tagResolver, boolean withPrefix) {
    Objects.requireNonNull(sender, "sender");
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(tagResolver, "tagResolver");

    String raw = resolveRawTranslation(sender, path, def);
    raw = withPrefix ? applyOptionalPrefix(sender, raw) : raw;

    return parse(raw, tagResolver);
  }

  @Override
  public Component getComponent(Player player, String path, TagResolver tagResolver, boolean withPrefix) {
    return getComponent(player, path, path, tagResolver, withPrefix);
  }

  @Override
  public Component getComponent(CommandSender sender, String path, TagResolver tagResolver, boolean withPrefix) {
    return getComponent(sender, path, path, tagResolver, withPrefix);
  }

  @Override
  public List<Component> getComponents(Player player, String path, String def, boolean withPrefix) {
    return getComponents(player, path, def, TagResolver.empty(), withPrefix);
  }

  @Override
  public List<Component> getComponents(CommandSender sender, String path, String def, boolean withPrefix) {
    return getComponents(sender, path, def, TagResolver.empty(), withPrefix);
  }

  @Override
  public List<Component> getComponents(Player player, String path, String def, TagResolver tagResolver, boolean withPrefix) {
    return getComponents((CommandSender) player, path, def, tagResolver, withPrefix);
  }

  @Override
  public List<Component> getComponents(CommandSender sender, String path, String def, TagResolver tagResolver, boolean withPrefix) {
    Objects.requireNonNull(sender, "sender");
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(tagResolver, "tagResolver");

    List<String> lines = resolveRawTranslations(sender, path, List.of(def));
    List<Component> out = new ArrayList<>(Math.max(1, lines.size()));

    for (String rawLine : lines) {
      String raw = withPrefix ? applyOptionalPrefix(sender, rawLine) : rawLine;
      out.add(tagResolver == TagResolver.empty() ? parse(raw) : parse(raw, tagResolver));
    }

    return out;
  }

  @Override
  public Component parse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Component.empty();
    }
    String mm = legacyToMiniMessage(raw);
    return miniMessage.deserialize(mm);
  }

  @Override
  public Component parse(String raw, TagResolver tagResolver) {
    Objects.requireNonNull(tagResolver, "tagResolver");
    if (raw == null || raw.isBlank()) {
      return Component.empty();
    }

    String mm = legacyToMiniMessage(raw);
    return miniMessage.deserialize(mm, tagResolver);
  }

  private String applyOptionalPrefix(CommandSender sender, String rawMessage) {
    String prefix = resolveRawPrefix(sender);
    if (prefix.isBlank()) {
      return rawMessage;
    }
    if (rawMessage == null || rawMessage.isBlank()) {
      return prefix;
    }
    return prefix + " " + rawMessage;
  }

  private String resolveRawPrefix(CommandSender sender) {
    String prefix = resolveRawTranslation(sender, "general.prefix", "");
    return prefix == null ? "" : prefix.trim();
  }

  private String resolveRawTranslation(CommandSender sender, String path, String def) {
    if (sender instanceof Player player) {
      return stringPathService.getTranslation(player, path, def);
    }
    return stringPathService.getTranslation(DEFAULT_CONSOLE_LANGUAGE, path, def);
  }

  private List<String> resolveRawTranslations(CommandSender sender, String path, List<String> def) {
    if (sender instanceof Player player) {
      return stringPathService.getTranslationLines(player, path, def);
    }
    return stringPathService.getTranslationLines(DEFAULT_CONSOLE_LANGUAGE, path, def);
  }

  private static String legacyToMiniMessage(String in) {
    // Convert legacy hex: &#ffdc73 or §#ffdc73 -> <#ffdc73>
    String out = replaceHex(in);

    // Convert standard legacy codes (& / §)
    // Colors
    out = out
        .replace("§0", "<black>").replace("&0", "<black>")
        .replace("§1", "<dark_blue>").replace("&1", "<dark_blue>")
        .replace("§2", "<dark_green>").replace("&2", "<dark_green>")
        .replace("§3", "<dark_aqua>").replace("&3", "<dark_aqua>")
        .replace("§4", "<dark_red>").replace("&4", "<dark_red>")
        .replace("§5", "<dark_purple>").replace("&5", "<dark_purple>")
        .replace("§6", "<gold>").replace("&6", "<gold>")
        .replace("§7", "<gray>").replace("&7", "<gray>")
        .replace("§8", "<dark_gray>").replace("&8", "<dark_gray>")
        .replace("§9", "<blue>").replace("&9", "<blue>")
        .replace("§a", "<green>").replace("&a", "<green>")
        .replace("§b", "<aqua>").replace("&b", "<aqua>")
        .replace("§c", "<red>").replace("&c", "<red>")
        .replace("§d", "<light_purple>").replace("&d", "<light_purple>")
        .replace("§e", "<yellow>").replace("&e", "<yellow>")
        .replace("§f", "<white>").replace("&f", "<white>");

    // Formatting
    out = out
        .replace("§k", "<obfuscated>").replace("&k", "<obfuscated>")
        .replace("§l", "<bold>").replace("&l", "<bold>")
        .replace("§m", "<strikethrough>").replace("&m", "<strikethrough>")
        .replace("§n", "<underlined>").replace("&n", "<underlined>")
        .replace("§o", "<italic>").replace("&o", "<italic>")
        .replace("§r", "<reset>").replace("&r", "<reset>");

    // Also support uppercase variants
    out = out
        .replace("§A", "<green>").replace("&A", "<green>")
        .replace("§B", "<aqua>").replace("&B", "<aqua>")
        .replace("§C", "<red>").replace("&C", "<red>")
        .replace("§D", "<light_purple>").replace("&D", "<light_purple>")
        .replace("§E", "<yellow>").replace("&E", "<yellow>")
        .replace("§F", "<white>").replace("&F", "<white>")
        .replace("§K", "<obfuscated>").replace("&K", "<obfuscated>")
        .replace("§L", "<bold>").replace("&L", "<bold>")
        .replace("§M", "<strikethrough>").replace("&M", "<strikethrough>")
        .replace("§N", "<underlined>").replace("&N", "<underlined>")
        .replace("§O", "<italic>").replace("&O", "<italic>")
        .replace("§R", "<reset>").replace("&R", "<reset>");

    return out;
  }

  private static String replaceHex(String in) {
    Matcher m = LEGACY_HEX.matcher(in);
    StringBuilder sb = new StringBuilder(in.length());
    while (m.find()) {
      String hex = m.group(1).toLowerCase();
      m.appendReplacement(sb, "<#" + hex + ">");
    }
    m.appendTail(sb);
    return sb.toString();
  }
}