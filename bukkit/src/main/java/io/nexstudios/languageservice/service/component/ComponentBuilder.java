package io.nexstudios.languageservice.service.component;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
public class ComponentBuilder {

  private final DefaultComponentService componentService;
  private final String rawDefault;

  private Function<String, String> transformer = Function.identity();
  private TagResolver resolver = TagResolver.empty();

  /**
   * Applies a string transformer on the resolved raw string.
   * You can call this multiple times; transformers are applied in order.
   */
  public ComponentBuilder string(Function<String, String> transformer) {
    Objects.requireNonNull(transformer, "transformer");
    this.transformer = this.transformer.andThen(transformer);
    return this;
  }

  /**
   * Adds a MiniMessage TagResolver (placeholders/tags) for parsing.
   * You can call this multiple times; resolvers are combined.
   */
  public ComponentBuilder resolver(TagResolver resolver) {
    Objects.requireNonNull(resolver, "resolver");
    this.resolver = TagResolver.builder().resolver(this.resolver).resolver(resolver).build();
    return this;
  }

  /**
   * Builds the final component (MiniMessage + legacy color support).
   */
  public Component build() {
    String raw = transformer.apply(rawDefault);

    if (resolver == TagResolver.empty()) {
      return componentService.parse(raw);
    }
    return componentService.parse(raw, resolver);
  }
}