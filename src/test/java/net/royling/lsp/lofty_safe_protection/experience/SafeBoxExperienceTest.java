package net.royling.lsp.lofty_safe_protection.experience;

import net.royling.lsp.lofty_safe_protection.client.gui.SafeBoxPanelLayout;


import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafeBoxExperienceTest {
    @Test void configIsOptionalAndValidated() {
        assertEquals(SafeBoxExperienceSettings.DISABLED, config("{}"));
        assertEquals(new SafeBoxExperienceSettings(true, 1234), config("{\"experience\":{\"enabled\":true,\"capacity\":1234}}"));
        assertEquals(new SafeBoxExperienceSettings(false, 500), config("{\"experience\":{\"enabled\":false,\"capacity\":500}}"));
        for (String json : new String[]{"{\"enabled\":true}","{\"enabled\":true,\"capacity\":0}","{\"capacity\":-1}","{\"enabled\":\"true\"}","{\"enabled\":true,\"capacity\":1.5}","{\"capacity\":2147483648}"})
            assertThrows(RuntimeException.class, () -> config("{\"experience\":" + json + "}"));
    }

    @Test void curveMatchesVanillaLevelCosts() {
        long points = 0;
        for (int level = 0; level < 22000; level++) {
            assertEquals(points, ExperienceMath.atLevel(level), "level " + level);
            points += level >= 30 ? 112L + (level - 30) * 9L : level >= 15 ? 37L + (level - 15) * 5L : 7L + level * 2L;
        }
        assertTrue(ExperienceMath.atLevel(Integer.MAX_VALUE) > Integer.MAX_VALUE);
    }

    @Test void integerPointsSurviveFloatBarsAndLevelBoundaries() {
        for (int points = 0; points < 200000; points++) roundTrip(points);
        for (int points : new int[]{1_000_000, 123_456_789, Integer.MAX_VALUE - 1, Integer.MAX_VALUE}) roundTrip(points);
    }

    private void roundTrip(int points) {
        int level = ExperienceMath.levelFor(points);
        long base = ExperienceMath.atLevel(level), next = ExperienceMath.atLevel(level + 1) - base;
        assertTrue(base <= points && base + next > points);
        assertEquals(points, ExperienceMath.balance(level, (float) (points - base) / next));
    }

    @Test void modifierAmountsAndBounds() {
        int[] requests = {1, 10, 50, 100};
        for (int mode = 0; mode < 4; mode++) {
            assertEquals(requests[mode], ExperienceMath.transfer(true, mode, 1000, 0, 100));
            assertEquals(-requests[mode], ExperienceMath.transfer(false, mode, 1000, 100, 100));
        }
        assertEquals(7, ExperienceMath.transfer(true, 3, 7, 0, 500));
        assertEquals(7, ExperienceMath.transfer(true, 2, 100, 93, 100));
        assertEquals(0, ExperienceMath.transfer(true, 3, 0, 0, 500));
        assertEquals(0, ExperienceMath.transfer(true, 3, 100, 100, 100));
        assertEquals(0, ExperienceMath.transfer(true, 255, 100, 0, 500));
        assertEquals(0, ExperienceMath.transfer(false, -1, 100, 500, 500));
        assertEquals(-1, ExperienceMath.transfer(false, 3, Integer.MAX_VALUE - 1, 500, 500));
        assertEquals(0, ExperienceMath.transfer(false, 3, Integer.MAX_VALUE, 500, 500));
    }

    @Test void disabledOrReducedCapacityKeepsWithdrawalsAvailable() {
        assertEquals(0, ExperienceMath.transfer(true, 3, 100, 300, 0));
        assertEquals(-300, ExperienceMath.transfer(false, 3, 100, 300, 0));
        assertEquals(0, ExperienceMath.transfer(true, 3, 100, 300, 100));
        assertEquals(-300, ExperienceMath.transfer(false, 3, 100, 300, 100));
    }

    @Test void transfersConservePointsAcrossRepeatedDeposits() {
        int player = 12345, bank = 0;
        for (int i = 0; i < 10000; i++) {
            int delta = ExperienceMath.transfer(i % 3 != 0, i % 4, player, bank, 500);
            player -= delta; bank += delta;
            assertEquals(12345, player + bank);
            assertTrue(bank >= 0 && bank <= 500 && player >= 0);
        }
    }

    @Test void experienceHeaderGrowsUpWithoutMovingEquipmentOrCargo() {
        for (int columns = 1; columns <= 9; columns++) for (int rows = 1; rows <= 6; rows++) {
            var plain = SafeBoxPanelLayout.frame(50, 20, columns, rows, 1);
            var xp = SafeBoxPanelLayout.frame(50, 20, columns, rows, true, 1);
            assertEquals(plain.y() - 24, xp.y());
            assertEquals(plain.y() + plain.height(), xp.y() + xp.height());
            assertTrue(xp.width() >= 80);
            assertTrue(xp.contains(SafeBoxPanelLayout.equipmentX(50), SafeBoxPanelLayout.equipmentY(20)));
            assertTrue(xp.contains(SafeBoxPanelLayout.cargoX(50, columns - 1) + 15, SafeBoxPanelLayout.cargoY(20, rows, 0)));
            assertEquals(22, SafeBoxPanelLayout.frame(50, 20, columns, rows, true, 0).height());
        }
    }

    private SafeBoxExperienceSettings config(String json) { return SafeBoxExperienceSettings.parse(JsonParser.parseString(json).getAsJsonObject()); }
}
