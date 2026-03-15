package io.nexstudios.languageservice.service.language;

import io.nexstudios.configservice.config.ConfigurationSection;
import io.nexstudios.configservice.config.FileConfiguration;
import io.nexstudios.configservice.service.multireader.MultiFileReaderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class DefaultLanguageService implements LanguageService {

  private static final Path LANG_DIR = Path.of("languages");

  private final Plugin plugin;
  private final MultiFileReaderService multiFileReaderService;

  @Getter
  private final String defaultLanguage = "en";

  private final NamespacedKey pdcKey = new NamespacedKey(plugin, "language");

  private final Map<String, FileConfiguration> languagesById = new ConcurrentHashMap<>();

  @Override
  public void reload() {
    Map<Path, FileConfiguration> loaded = multiFileReaderService.loadAll(LANG_DIR);

    Map<String, FileConfiguration> next = new LinkedHashMap<>();
    for (Map.Entry<Path, FileConfiguration> e : loaded.entrySet()) {
      String id = toLanguageId(e.getKey());
      if (id == null || id.isBlank()) continue;
      next.put(id, e.getValue());
    }

    languagesById.clear();
    languagesById.putAll(next);

    if (!languagesById.containsKey(defaultLanguage) && !languagesById.isEmpty()) {
      plugin.getLogger().warning(
          "Default language '" + defaultLanguage + "' was not found in " + LANG_DIR + " (available: " + languagesById.keySet() + ")"
      );
    }
  }

  @Override
  public Set<String> getRegisteredLanguages() {
    return Collections.unmodifiableSet(new LinkedHashSet<>(languagesById.keySet()));
  }

  @Override
  public String getLanguage(Player player) {
    Objects.requireNonNull(player, "player");

    PersistentDataContainer pdc = player.getPersistentDataContainer();
    String stored = pdc.get(pdcKey, PersistentDataType.STRING);

    // Not set -> set default immediately
    if (stored == null || stored.isBlank()) {
      pdc.set(pdcKey, PersistentDataType.STRING, defaultLanguage);
      return defaultLanguage;
    }

    String normalized = stored.trim().toLowerCase(Locale.ROOT);

    // Invalid -> fall back to default (and fix stored value)
    if (!isRegistered(normalized) && isRegistered(defaultLanguage)) {
      pdc.set(pdcKey, PersistentDataType.STRING, defaultLanguage);
      return defaultLanguage;
    }

    return normalized;
  }

  @Override
  public void setLanguage(Player player, String languageId) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(languageId, "languageId");

    String normalized = languageId.trim().toLowerCase(Locale.ROOT);
    if (!isRegistered(normalized)) {
      throw new IllegalArgumentException(
          "Language '" + normalized + "' is not registered. Available: " + getRegisteredLanguages()
      );
    }

    player.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, normalized);
  }

  @Override
  public boolean isRegistered(String languageId) {
    if (languageId == null) return false;
    return languagesById.containsKey(languageId.trim().toLowerCase(Locale.ROOT));
  }

  @Override
  public Optional<FileConfiguration> getLanguageConfig(String languageId) {
    if (languageId == null) return Optional.empty();
    return Optional.ofNullable(languagesById.get(languageId.trim().toLowerCase(Locale.ROOT)));
  }

  @Override
  public String translate(Player player, String key, String def) {
    Objects.requireNonNull(key, "key");

    FileConfiguration cfg = resolveConfig(getLanguage(player));
    if (cfg == null) return def;

    return cfg.getString(key, def);
  }

  @Override
  public ConfigurationSection section(Player player) {
    FileConfiguration cfg = resolveConfig(getLanguage(player));
    if (cfg == null) {
      return new ConfigurationSection(CommentedConfigurationNode.root());
    }
    return cfg;
  }

  private FileConfiguration resolveConfig(String languageId) {
    FileConfiguration cfg = languagesById.get(languageId);
    if (cfg != null) return cfg;
    return languagesById.get(defaultLanguage);
  }

  private static String toLanguageId(Path path) {
    if (path == null) return null;
    Path file = path.getFileName();
    if (file == null) return null;

    String name = file.toString(); // e.g. "en.yml"
    int dot = name.lastIndexOf('.');
    if (dot > 0) name = name.substring(0, dot);

    return name.trim().toLowerCase(Locale.ROOT);
  }
}