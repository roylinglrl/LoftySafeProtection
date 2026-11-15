package net.royling.lsp.lofty_safe_protection.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;

import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.royling.lsp.lofty_safe_protection.data.DefinitionScriptHooks;

public final class SafeKubePlugin extends KubeJSPlugin {
    private static final EventGroup GROUP = EventGroup.of("SafeEvents");
    private static final EventHandler DEFINITIONS = GROUP.server("definitions", () -> SafeDefinitionEvent.class);

    @Override
    public void registerEvents() { GROUP.register(); }

    @Override
    public void init() {
        DefinitionScriptHooks.apply = (boxes, containers) -> DEFINITIONS.post(new SafeDefinitionEvent(boxes, containers));
    }
}
