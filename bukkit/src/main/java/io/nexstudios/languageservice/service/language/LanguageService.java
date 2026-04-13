package io.nexstudios.languageservice.service.language;

import io.nexstudios.configservice.config.ConfigurationSection;
import io.nexstudios.configservice.config.FileConfiguration;
import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This interface represents a service for managing and handling multiple languages in a system.
 * It provides methods to reload language files, query and update player-specific language settings,
 * validate registered languages, and retrieve configuration details or translations for a specific language.
 */
public interface LanguageService extends Service {

  /**
   * Reload all language files from the languages/ directory.
   */
  void reload();

  /**
   * @return all registered language ids (e.g. "en", "de")
   */
  Set<String> getRegisteredLanguages();

  /**
   * @return default language id (by design: "en")
   */
  String getDefaultLanguage();

  /**
   * Returns the player's language (from PDC). If missing, sets and returns the default language.
   */
  String getLanguage(Player player);

  /**
   * Sets the player's language (stored in PDC).
   *
   * @throws IllegalArgumentException if the language id is not registered
   */
  void setLanguage(Player player, String languageId);

  /**
   * @return true if the given language id is registered
   */
  boolean isRegistered(String languageId);

  /**
   * @return the config for a language id, if present
   */
  Optional<FileConfiguration> getLanguageConfig(String languageId);

  /**
   * Translate a key using the player's language. If the key is missing, returns def.
   */
  String translate(Player player, String key, String def);

  /**
   * Returns a translation list for a key using the player's language.
   * If the value at the key is not a list, an empty list is returned.
   */
  List<String> getTranslation(Player player, String key);

  /**
   * @return the root section for the player's language (for more advanced access).
   */
  ConfigurationSection section(Player player);
}