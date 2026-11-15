package net.royling.lsp.lofty_safe_protection.experience;



public final class ExperienceMath {
    private ExperienceMath() {}

    public static long atLevel(int level) {
        long l = Math.max(0, level);
        if (l > 1_000_000_000L) return Long.MAX_VALUE;
        if (l <= 16) return l * (l + 6);
        if (l <= 31) return (5 * l * l - 81 * l + 720) / 2;
        return (9 * l * l - 325 * l + 4440) / 2;
    }

    public static long balance(int level, float progress) {
        long base = atLevel(level);
        if (base > Integer.MAX_VALUE) return base;
        long next = atLevel(Math.max(0, level) + 1) - base;
        return base + Math.round(Math.max(0, Math.min(1, (double) progress)) * next);
    }

    public static int levelFor(int points) {
        int low = 0, high = 21864;
        while (low + 1 < high) {
            int middle = (low + high) >>> 1;
            if (atLevel(middle) <= points) low = middle;
            else high = middle;
        }
        return low;
    }

    public static int request(int modifiers) {
        return switch (modifiers) {
            case 0 -> 1;
            case 1 -> 10;
            case 2 -> 50;
            case 3 -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    public static int transfer(boolean deposit, int modifiers, long player, int stored, int capacity) {
        if (player < 0 || player > Integer.MAX_VALUE || stored < 0) return 0;
        long requested = request(modifiers);
        if (deposit) return (int) Math.min(requested, Math.min(player, Math.max(0L, (long) capacity - stored)));
        return -(int) Math.min(requested, Math.min(stored, Integer.MAX_VALUE - player));
    }
}
