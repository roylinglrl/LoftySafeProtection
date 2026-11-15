package net.royling.lsp.lofty_safe_protection.fluid;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.resources.ResourceLocation;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.client.gui.InventoryContainerPanelLayout;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FluidStorageDefinitionTest {
    @Test void defaultFluidContainerHasTwoFourBucketTanksAndNoItemSlots() throws Exception {
        try (var stream = getClass().getResourceAsStream("/data/lofty_safe_protection/inventory_containers/fluid_container.json")) {
            assertNotNull(stream);
            var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            var id = ResourceLocation.parse("lofty_safe_protection:fluid_container");
            var definition = InventoryContainerDefinition.parse(id, json);
            assertEquals(0, definition.columns());
            assertEquals(0, definition.rows());
            assertEquals(new FluidStorageSettings(2, 4000), definition.fluids());
            assertEquals(definition, InventoryContainerDefinition.parse(id, definition.toJson()));
        }
    }

    @Test void invalidFluidSettingsAndHalfEmptyItemGridsAreRejected() {
        var id = ResourceLocation.parse("test:fluid");
        for (String settings : new String[]{"{\"slots\":0,\"capacity\":4000}","{\"slots\":10,\"capacity\":4000}",
                "{\"slots\":2,\"capacity\":0}","{\"slots\":2,\"capacity\":-1}","{\"slots\":2.5,\"capacity\":4000}","{\"slots\":2,\"capacity\":2147483648}"}) {
            var json = JsonParser.parseString("{\"id\":\"test:fluid\",\"translation_key\":\"test.fluid\",\"fluids\":" + settings + "}").getAsJsonObject();
            assertThrows(RuntimeException.class, () -> InventoryContainerDefinition.parse(id, json));
        }
    }

    @Test void fixedSizeFluidSlotsFitEveryItemGridAndWidenNarrowWindows() {
        for (int columns = 0; columns <= 9; columns++) for (int rows = 0; rows <= 6; rows++) for (int slots = 1; slots <= 9; slots++) {
            var bounds = InventoryContainerPanelLayout.place(10, 10, columns, rows, slots, 500, 500);
            int last = slots - 1;
            assertTrue(bounds.fluidX(last) + 16 <= bounds.x() + bounds.width() - 4);
            assertTrue(bounds.fluidY(rows) + 64 <= bounds.y() + bounds.height() - 4);
            assertTrue(bounds.fluidContains(last, rows, bounds.fluidX(last) + 15, bounds.fluidY(rows) + 63));
            assertFalse(bounds.fluidContains(last, rows, bounds.fluidX(last) + 16, bounds.fluidY(rows) + 64));
        }
        assertTrue(InventoryContainerPanelLayout.place(0, 0, 1, 1, 9, 500, 500).width() >= 186);
    }
}
