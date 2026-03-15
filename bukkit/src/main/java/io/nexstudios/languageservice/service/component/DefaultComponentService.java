package io.nexstudios.languageservice.service.component;

import io.nexstudios.languageservice.service.path.StringPathService;
import io.nexstudios.serviceregistry.di.Dependencies;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.Objects;

@RequiredArgsConstructor
@Dependencies({
    StringPathService.class
})
public class DefaultComponentService implements ComponentService {

  private final StringPathService stringPathService;

  /**
   * You can swap this for a customized MiniMessage builder if you want to restrict tags later.
   */
  private final MiniMessage miniMessage = MiniMessage.miniMessage();

  @Override
  public Component getComponent(Player player, String path) {
    return getComponent(player, path, path);
  }

  @Override
  public Component getComponent(Player player, String path, String def) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(path, "path");

    String raw = stringPathService.getTranslation(player, path, def);
    return parse(raw);
  }

  @Override
  public Component getComponent(Player player, String path, String def, TagResolver tagResolver) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(tagResolver, "tagResolver");

    String raw = stringPathService.getTranslation(player, path, def);
    return parse(raw, tagResolver);
  }

  @Override
  public Component getComponent(Player player, String path, TagResolver tagResolver) {
    return getComponent(player, path, path, tagResolver);
  }

  @Override
  public Component parse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Component.empty();
    }
    return miniMessage.deserialize(raw);
  }

  @Override
  public Component parse(String raw, TagResolver tagResolver) {
    Objects.requireNonNull(tagResolver, "tagResolver");
    if (raw == null || raw.isBlank()) {
      return Component.empty();
    }
    return miniMessage.deserialize(raw, tagResolver);
  }
}