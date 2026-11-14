package net.royling.lsp.lofty_safe_protection.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class WindowStack<T> {
    private final List<T> windows = new ArrayList<>();

    public void raise(T window) { windows.remove(window); windows.add(window); }
    public List<T> backToFront() { return List.copyOf(windows); }
    public void clear() { windows.clear(); }

    public T top(Predicate<T> matches) {
        for (int index = windows.size() - 1; index >= 0; index--)
            if (matches.test(windows.get(index))) return windows.get(index);
        return null;
    }
}
