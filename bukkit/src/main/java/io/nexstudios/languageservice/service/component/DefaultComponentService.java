package io.nexstudios.languageservice.service.component;

import io.nexstudios.languageservice.service.path.StringPathService;
import io.nexstudios.serviceregistry.di.Dependencies;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Dependencies({
    StringPathService.class
})
public class DefaultComponentService implements ComponentService {

  private static final Pattern LEGACY_HEX = Pattern.compile("(?i)[&§]#([0-9a-f]{6})");

  private final StringPathService stringPathService;

  /**
   * You can swap this for a customized MiniMessage builder if you want to restrict tags later.
   */
  private final MiniMessage miniMessage = MiniMessage.miniMessage();

  @Override
  public ComponentBuilder builder(Player player, String path, String def, boolean withPrefix) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(path, "path");

    String raw = stringPathService.getTranslation(player, path, def);
    raw = withPrefix ? applyOptionalPrefix(player, raw) : raw;

    return new ComponentBuilder(this, raw);
  }

  @Override
  public Component getComponent(Player player, String path, boolean withPrefix) {
    return getComponent(player, path, path, withPrefix);
  }

  @Override
  public Component getComponent(Player player, String path, String def, boolean withPrefix) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(path, "path");

    String raw = stringPathService.getTranslation(player, path, def);
    raw = withPrefix ? applyOptionalPrefix(player, raw) : raw;

    return parse(raw);
  }

  @Override
  public Component getComponent(Player player, String path, String def, TagResolver tagResolver, boolean withPrefix) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(tagResolver, "tagResolver");

    String raw = stringPathService.getTranslation(player, path, def);
    raw = withPrefix ? applyOptionalPrefix(player, raw) : raw;

    return parse(raw, tagResolver);
  }

  @Override
  public Component getComponent(Player player, String path, TagResolver tagResolver, boolean withPrefix) {
    return getComponent(player, path, path, tagResolver, withPrefix);
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

  private String applyOptionalPrefix(Player player, String rawMessage) {
    String prefix = resolveRawPrefix(player);
    if (prefix.isBlank()) {
      return rawMessage;
    }
    if (rawMessage == null || rawMessage.isBlank()) {
      return prefix;
    }
    return prefix + " " + rawMessage;
  }

  private String resolveRawPrefix(Player player) {
    String prefix = stringPathService.getTranslation(player, "general.prefix", "");
    return prefix == null ? "" : prefix.trim();
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