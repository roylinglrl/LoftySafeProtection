package net.royling.lsp.lofty_safe_protection.experience;



import com.google.gson.JsonObject;

public record SafeBoxExperienceSettings(boolean enabled, int capacity) {
    public static final SafeBoxExperienceSettings DISABLED = new SafeBoxExperienceSettings(false, 0);

    public SafeBoxExperienceSettings {
        if (capacity < 0 || (enabled && capacity == 0))
            throw new IllegalArgumentException("experience.capacity must be positive when enabled");
    }

    public static SafeBoxExperienceSettings parse(JsonObject definition) {
        if (!definition.has("experience")) return DISABLED;
        JsonObject json = definition.getAsJsonObject("experience");
        boolean enabled = false;
        if (json.has("enabled")) {
            if (!json.get("enabled").isJsonPrimitive() || !json.getAsJsonPrimitive("enabled").isBoolean())
                throw new IllegalArgumentException("experience.enabled must be a boolean");
            enabled = json.get("enabled").getAsBoolean();
        }
        int capacity = json.has("capacity") ? json.get("capacity").getAsBigDecimal().intValueExact() : 0;
        return new SafeBoxExperienceSettings(enabled, capacity);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", enabled);
        json.addProperty("capacity", capacity);
        return json;
    }
}
