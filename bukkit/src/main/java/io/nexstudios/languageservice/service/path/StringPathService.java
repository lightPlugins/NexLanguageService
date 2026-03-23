package io.nexstudios.languageservice.service.path;

import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.entity.Player;

import java.util.List;

public interface StringPathService extends Service {

  /**
   * Resolves a string by a dotted path (e.g. "general.prefix") for the player's language.
   * Returns def if the path is missing.
   */
  String getTranslation(Player player, String path, String def);

  /**
   * Resolves a string by a dotted path for a specific language id.
   * Returns def if the path is missing or the language is unknown.
   */
  String getTranslation(String languageId, String path, String def);

  /**
   * Resolves multiple lines by a dotted path.
   * If the underlying YAML value is a list, the list is returned.
   * If it's a single string, a singleton list is returned.
   */
  List<String> getTranslationLines(Player player, String path, List<String> def);

  /**
   * Same as getTranslationLines(Player, ...), but for a specific language id.
   */
  List<String> getTranslationLines(String languageId, String path, List<String> def);
}