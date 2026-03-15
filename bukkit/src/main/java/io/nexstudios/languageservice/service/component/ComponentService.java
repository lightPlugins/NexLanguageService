package io.nexstudios.languageservice.service.component;

import io.nexstudios.serviceregistry.di.Service;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

public interface ComponentService extends Service {


  /**
   * Creates a new instance of ComponentBuilder for building localized components.
   * This method provides an entry point to customize and build a Component using a specific player's language settings,
   * a localization path, and a default fallback string.
   *
   * @param player the player whose language settings will determine the localization
   * @param path the path used to retrieve the localized string
   * @param def the default string value to use if the path is not resolved
   * @return a ComponentBuilder instance for constructing the localized Component
   * @throws NullPointerException if player, path, or def is null
   */
  ComponentBuilder builder(Player player, String path, String def);

  /**
   * Retrieves a localized Component for the given player and path.
   * The localization process uses the player's language settings to
   * resolve the provided path into a string, which is then parsed into a Component.
   *
   * @param player the player whose language settings will determine the localization
   * @param path the path used to retrieve the localized string
   * @return the localized Component for the given player and path
   * @throws NullPointerException if player or path is null
   */
  Component getComponent(Player player, String path);
  /**
   * Retrieves a localized Component for the given player, path, and default value.
   * The method attempts to resolve a string representation of the Component based on
   * the player's language settings and the provided path. If the path cannot
   * be resolved, the default value is used instead. The resulting string is
   * then parsed into a Component.
   *
   * @param player the player whose language settings will determine the localization
   * @param path the path used to retrieve the localized string
   * @param def the default string value to use if the path is not resolved
   * @return the localized Component for the given player and path, or a Component
   *         based on the default value if the path is not resolved
   * @throws NullPointerException if player or path is null
   */
  Component getComponent(Player player, String path, String def);

  /**
   * Retrieves a localized Component for the given player, path, default value, and TagResolver.
   * The method attempts to resolve a string representation of the Component based on the player's
   * language settings and the provided path. If the path cannot be resolved, the default value
   * is used instead. The resolved string is then parsed into a Component, using the provided
   * TagResolver for tag resolution.
   *
   * @param player the player whose language settings will determine the localization
   * @param path the path used to retrieve the localized string
   * @param def the default string value to use if the path is not resolved
   * @param tagResolver the TagResolver used to handle additional MiniMessage tags during parsing
   * @return the localized Component for the given player and path, or a Component
   *         based on the default value if the path is not resolved
   * @throws NullPointerException if player, path, or tagResolver is null
   */
  Component getComponent(Player player, String path, String def, TagResolver tagResolver);


  /**
   * Retrieves a localized Component for the given player, path, and TagResolver.
   * The method attempts to resolve a string representation of the Component based
   * on the player's language settings and the provided path. The resolved string
   * is then parsed into a Component, using the provided TagResolver for tag resolution.
   *
   * @param player the player whose language settings will determine the localization
   * @param path the path used to retrieve the localized string
   * @param tagResolver the TagResolver used to handle additional MiniMessage tags during parsing
   * @return the localized Component for the given player and path, resolved using the provided TagResolver
   * @throws NullPointerException if player, path, or tagResolver is null
   */
  Component getComponent(Player player, String path, TagResolver tagResolver);

  /**
   * Parses an already resolved raw string into a Component (MiniMessage).
   */
  Component parse(String raw);

  /**
   * Parses an already resolved raw string into a Component (MiniMessage) with an additional TagResolver.
   */
  Component parse(String raw, TagResolver tagResolver);
}