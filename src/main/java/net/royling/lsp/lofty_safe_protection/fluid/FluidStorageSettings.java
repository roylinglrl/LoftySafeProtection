package net.royling.lsp.lofty_safe_protection.fluid;

import com.google.gson.JsonObject;

public record FluidStorageSettings(int slots, int capacity) {
    public static final FluidStorageSettings DISABLED = new FluidStorageSettings(0, 0);
    public static final int MAX_SLOTS = 9;

    public FluidStorageSettings {
        if (slots < 0 || slots > MAX_SLOTS || capacity < 0 || (slots == 0) != (capacity == 0))
            throw new IllegalArgumentException("Fluid storage requires 1..9 slots and positive capacity in mB");
    }

    public boolean enabled() { return slots > 0; }

    public static FluidStorageSettings parse(JsonObject definition) {
        if (!definition.has("fluids")) return DISABLED;
        var json = definition.getAsJsonObject("fluids");
        int slots = json.get("slots").getAsBigDecimal().intValueExact();
        int capacity = json.get("capacity").getAsBigDecimal().intValueExact();
        if (slots == 0 || capacity == 0) throw new IllegalArgumentException("Fluid slots and capacity must be positive");
        return new FluidStorageSettings(slots, capacity);
    }

    public JsonObject toJson() {
        var json = new JsonObject();
        json.addProperty("slots", slots);
        json.addProperty("capacity", capacity);
        return json;
    }
}
