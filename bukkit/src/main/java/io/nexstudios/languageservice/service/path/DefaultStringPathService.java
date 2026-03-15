package io.nexstudios.languageservice.service.path;

import io.nexstudios.configservice.config.FileConfiguration;
import io.nexstudios.languageservice.service.language.LanguageService;
import io.nexstudios.serviceregistry.di.Dependencies;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Objects;

@RequiredArgsConstructor
@Dependencies({
    LanguageService.class
})
public class DefaultStringPathService implements StringPathService {

  private final LanguageService languageService;

  @Override
  public String getTranslation(Player player, String path, String def) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(path, "path");
    return languageService.translate(player, path, def);
  }

  @Override
  public String getTranslation(String languageId, String path, String def) {
    Objects.requireNonNull(languageId, "languageId");
    Objects.requireNonNull(path, "path");

    String normalized = languageId.trim().toLowerCase(Locale.ROOT);

    FileConfiguration cfg = languageService.getLanguageConfig(normalized)
        .orElseGet(() -> languageService.getLanguageConfig(languageService.getDefaultLanguage()).orElse(null));

    if (cfg == null) return def;
    return cfg.getString(path, def);
  }
}