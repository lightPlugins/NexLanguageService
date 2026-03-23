package io.nexstudios.languageservice.service.component;

import io.nexstudios.serviceregistry.di.Service;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.List;

public interface ComponentService extends Service {

  /**
   * Creates a new {@link ComponentBuilder} instance for constructing components with
   * optional input transformation, placeholders, and tag resolvers.
   *
   * @param player the player associated with the component, used for placeholder resolution
   * @param path the configuration key path to fetch the raw component string
   * @param def the default value to fallback to if no value is found at the specified path
   * @param withPrefix whether to include a prefix in the constructed component
   * @return a {@link ComponentBuilder} instance to further customize and build the component
   */
  ComponentBuilder builder(Player player, String path, String def, boolean withPrefix);

  /**
   * Retrieves a {@link Component} based on the specified player and configuration path.
   * The method optionally includes a prefix based on the provided {@code withPrefix} parameter.
   *
   * @param player the player associated with the component, used for placeholder resolution
   * @param path the configuration key or path where the component is located
   * @param withPrefix a boolean flag specifying whether a prefix should be included in the component
   * @return the resolved {@link Component} based on the provided parameters
   */
  Component getComponent(Player player, String path, boolean withPrefix);

  /**
   * Retrieves a {@link Component} based on the specified player and configuration path,
   * with an optional default value and prefix inclusion.
   *
   * @param player the player associated with the component, used for placeholder resolution
   * @param path the configuration key path to fetch the raw component string
   * @param def the default value to fallback to if no value is found at the specified path
   * @param withPrefix whether to include a prefix in the constructed component
   * @return a {@link Component} instance resolved and optionally prefixed
   */
  Component getComponent(Player player, String path, String def, boolean withPrefix);

  /**
   * Retrieves a {@link Component} based on the specified player, configuration path, and additional parameters.
   * The method supports placeholder resolution using a {@link TagResolver} and optionally includes a prefix based
   * on the provided {@code withPrefix} parameter. If no value is found at the specified path, the method uses the
   * provided default value {@code def}.
   *
   * @param player the player associated with the component, used for placeholder resolution
   * @param path the configuration key or path where the component is located
   * @param def the default value to fallback to if no value is found at the specified path
   * @param tagResolver a {@link TagResolver} used for resolving placeholders within the component
   * @param withPrefix a boolean flag specifying whether a prefix should be included in the component
   * @return the resolved {@link Component} based on the specified input parameters
   */
  Component getComponent(Player player, String path, String def, TagResolver tagResolver, boolean withPrefix);

  /**
   * Retrieves a {@link Component} based on the specified player, configuration path, and tag resolver.
   * Optionally includes a prefix based on the provided {@code withPrefix} parameter.
   *
   * @param player the player associated with the component, used for placeholder resolution
   * @param path the configuration key or path where the component is located
   * @param tagResolver the {@link TagResolver} to process placeholders or tags in the component
   * @param withPrefix a boolean flag specifying whether a prefix should be included in the component
   * @return the resolved {@link Component} based on the provided parameters
   */
  Component getComponent(Player player, String path, TagResolver tagResolver, boolean withPrefix);

  /**
   * Retrieves a list of {@link Component} instances based on the specified player,
   * configuration path, default value, and additional parameters. This method supports
   * placeholder resolution using a {@link TagResolver} and optionally includes a prefix
   * in the resolved components based on the {@code withPrefix} parameter.
   *
   * @param player the player associated with the components, used for placeholder resolution
   * @param path the configuration key path to fetch the raw component strings
   * @param def the default value to fallback to if no value is found at the specified path
   * @param tagResolver a {@link TagResolver} used for placeholder and tag resolution
   * @param withPrefix a boolean flag specifying whether a prefix should be included in the components
   * @return a list of {@link Component} instances resolved with the specified parameters
   */
  List<Component> getComponents(Player player, String path, String def, TagResolver tagResolver, boolean withPrefix);

  /**
   * Retrieves a list of {@link Component} objects based on the specified player and configuration path.
   * The method optionally includes a default value and a prefix based on the provided parameters.
   *
   * @param player the player associated with the components, used for placeholder resolution
   * @param path the configuration key path to fetch the raw component strings
   * @param def the default value to fallback to if no value is found at the specified path
   * @param withPrefix whether to include a prefix in the constructed components
   * @return a list of {@link Component} objects constructed and optionally prefixed
   */
  List<Component> getComponents(Player player, String path, String def, boolean withPrefix);

  /**
   * Parses the provided raw string into a {@link Component}.
   *
   * @param raw the raw string to be parsed into a {@link Component}
   * @return the parsed {@link Component} created from the provided raw string
   */
  Component parse(String raw);

  /**
   * Parses the provided raw string into a {@link Component} with optional tag resolution.
   *
   * @param raw the raw string to be parsed into a {@link Component}
   * @param tagResolver the {@link TagResolver} used to resolve placeholders or tags within the component
   * @return the parsed {@link Component} created from the provided raw string with any resolved tags
   */
  Component parse(String raw, TagResolver tagResolver);
}