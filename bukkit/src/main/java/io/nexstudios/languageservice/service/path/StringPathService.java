package io.nexstudios.languageservice.service.path;

import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.entity.Player;

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
}