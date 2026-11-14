package net.royling.lsp.lofty_safe_protection.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import net.royling.lsp.lofty_safe_protection.data.DefinitionScriptHooks;

public final class SafeKubePlugin implements KubeJSPlugin {
    private static final EventGroup GROUP = EventGroup.of("SafeEvents");
    private static final EventHandler DEFINITIONS = GROUP.server("definitions", () -> SafeDefinitionEvent.class);

    @Override
    public void registerEvents(EventGroupRegistry registry) { registry.register(GROUP); }

    @Override
    public void init() {
        DefinitionScriptHooks.apply = (boxes, containers) -> DEFINITIONS.post(new SafeDefinitionEvent(boxes, containers));
    }
}
