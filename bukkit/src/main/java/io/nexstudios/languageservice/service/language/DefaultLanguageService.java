package io.nexstudios.languageservice.service.language;

import io.nexstudios.configservice.config.ConfigurationSection;
import io.nexstudios.configservice.config.FileConfiguration;
import io.nexstudios.configservice.service.multireader.MultiFileReaderService;
import io.nexstudios.configservice.service.singlereader.FileReaderService;
import io.nexstudios.serviceregistry.di.Dependencies;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Dependencies({
    MultiFileReaderService.class,
    FileReaderService.class
})
public class DefaultLanguageService implements LanguageService {

  private static final String LANG_RESOURCE_PREFIX = "languages/";
  private static final String LANG_DIR_NAME = "languages";

  private final Plugin plugin;
  private final MultiFileReaderService multiFileReaderService;
  private final FileReaderService fileReaderService;

  @Getter
  private final String defaultLanguage = "en";

  private final NamespacedKey pdcKey;

  private final Map<String, FileConfiguration> languagesById = new ConcurrentHashMap<>();

  public DefaultLanguageService(Plugin plugin, MultiFileReaderService multiFileReaderService, FileReaderService fileReaderService) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.multiFileReaderService = Objects.requireNonNull(multiFileReaderService, "multiFileReaderService");
    this.fileReaderService = Objects.requireNonNull(fileReaderService, "fileReaderService");
    this.pdcKey = new NamespacedKey(this.plugin, "language");
  }

  @Override
  public void reload() {
    Path langDir = plugin.getDataFolder().toPath().resolve(LANG_DIR_NAME);

    plugin.getLogger().info("Ensuring bundled language defaults (create + autoupdate) ...");
    ensureBundledLanguagesPresentAndUpdated();

    plugin.getLogger().info("Loading languages from " + langDir + " ...");

    Path relativeLangDir = Path.of(LANG_DIR_NAME);
    Map<Path, FileConfiguration> loaded = multiFileReaderService.loadAll(relativeLangDir);

    plugin.getLogger().info("Loaded " + loaded.size() + " languages from " + langDir);

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
          "Default language '" + defaultLanguage + "' was not found in " + langDir + " (available: " + languagesById.keySet() + ")"
      );
    }
  }

  private void ensureBundledLanguagesPresentAndUpdated() {
    URI jarUri;
    try {
      jarUri = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
    } catch (Exception e) {
      plugin.getLogger().warning("Could not resolve plugin jar location (" + e.getMessage() + ")");
      return;
    }

    Path jarPath;
    try {
      jarPath = Path.of(jarUri);
    } catch (Exception e) {
      plugin.getLogger().warning("Could not convert plugin jar URI to path: " + jarUri + " (" + e.getMessage() + ")");
      return;
    }

    int processed = 0;

    try (JarFile jar = new JarFile(jarPath.toFile())) {
      Enumeration<JarEntry> entries = jar.entries();

      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.isDirectory()) continue;

        String resourcePath = entry.getName(); // e.g. languages/en.yml
        if (!resourcePath.startsWith(LANG_RESOURCE_PREFIX)) continue;
        if (!resourcePath.endsWith(".yml")) continue;

        String fileName = resourcePath.substring(LANG_RESOURCE_PREFIX.length()); // e.g. en.yml
        if (fileName.isBlank()) continue;

        // always directly under languages/ (no subfolders)
        if (fileName.contains("/")) continue;

        Path relativeTarget = Path.of(LANG_DIR_NAME).resolve(fileName); // languages/en.yml

        try {
          fileReaderService.load(relativeTarget, resourcePath, true);
          processed++;
        } catch (Exception ex) {
          plugin.getLogger().warning("Failed to ensure language default '" + resourcePath + "': " + ex.getMessage());
        }
      }
    } catch (Exception e) {
      plugin.getLogger().warning("Could not scan plugin jar for bundled language files (" + e.getMessage() + ")");
      return;
    }

    if (processed > 0) {
      plugin.getLogger().info("Processed " + processed + " bundled language file(s) via FileReaderService (create + autoupdate).");
    }
  }

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