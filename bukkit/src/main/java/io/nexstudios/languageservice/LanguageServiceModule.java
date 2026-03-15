package io.nexstudios.languageservice;

import io.nexstudios.configservice.service.multireader.MultiFileReaderService;
import io.nexstudios.languageservice.service.component.ComponentService;
import io.nexstudios.languageservice.service.component.DefaultComponentService;
import io.nexstudios.languageservice.service.language.DefaultLanguageService;
import io.nexstudios.languageservice.service.language.LanguageService;
import io.nexstudios.languageservice.service.path.DefaultStringPathService;
import io.nexstudios.languageservice.service.path.StringPathService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;
import org.bukkit.plugin.Plugin;


@Dependencies({
    MultiFileReaderService.class
})
public class LanguageServiceModule implements ServiceModule {

  private final Plugin plugin;

  public LanguageServiceModule(Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void install(ServiceAccessor serviceAccessor) {

    MultiFileReaderService multiFileReaderService = serviceAccessor.getService(MultiFileReaderService.class);

    LanguageService languageService = new DefaultLanguageService(plugin, multiFileReaderService);
    languageService.reload();

    serviceAccessor.register(LanguageService.class, languageService);

    StringPathService stringPathService = new DefaultStringPathService(languageService);
    serviceAccessor.register(StringPathService.class, stringPathService);

    ComponentService componentService = new DefaultComponentService(stringPathService);
    serviceAccessor.register(ComponentService.class, componentService);

  }
}
