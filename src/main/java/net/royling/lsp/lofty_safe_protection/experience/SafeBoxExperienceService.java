package net.royling.lsp.lofty_safe_protection.experience;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxMenuAccess;
import net.royling.lsp.lofty_safe_protection.storage.SafeBoxSavedData;


import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;

public final class SafeBoxExperienceService {
    private SafeBoxExperienceService() {}

    public static int capacity(SafeBoxSavedData.PlayerStorage state) {
        var definition = SafeBoxManager.definition(state.getItem(0));
        return definition != null && definition.experience().enabled() ? definition.experience().capacity() : 0;
    }

    public static boolean visible(SafeBoxSavedData.PlayerStorage state) {
        return capacity(state) > 0 || state.experience() > 0;
    }

    public static void transfer(ServerPlayer player, boolean deposit, int modifiers) {
        if (!player.isAlive() || player.isSpectator() || player.containerMenu != player.inventoryMenu
                || !(player.inventoryMenu instanceof SafeBoxMenuAccess access)) return;
        var box = access.lsp$storage();
        if (!box.expanded || !box.interactive) return;
        var state = box.state();
        long before = ExperienceMath.balance(player.experienceLevel, player.experienceProgress);
        int delta = ExperienceMath.transfer(deposit, modifiers, before, state.experience(), capacity(state));
        if (delta == 0) return;
        int remaining = (int) (before - delta);
        int level = ExperienceMath.levelFor(remaining);
        player.totalExperience = (int) Math.clamp((long) player.totalExperience - delta, 0, Integer.MAX_VALUE);
        player.setExperienceLevels(level);
        player.setExperiencePoints((int) (remaining - ExperienceMath.atLevel(level)));
        state.setExperience(state.experience() + delta);
        player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        player.inventoryMenu.broadcastChanges();
    }
}
