package work.lclpnet.illwalls.screen.lib;

import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UiBuilder {

    private final int initialY;
    private final int marginTop;
    private final List<Entry> entries = new ArrayList<>();
    private int width = 0;
    private int currentY;

    public UiBuilder(int initialY, int marginTop) {
        this.initialY = initialY;
        this.marginTop = marginTop;
        reset();
    }

    public void reset() {
        resetY();
        this.entries.clear();
    }

    private void resetY() {
        this.currentY = initialY;
    }

    public void add(LayoutElement widget) {
        add(widget, marginTop);
    }

    public void add(LayoutElement widget, int marginTop) {
        Objects.requireNonNull(widget);

        Entry entry = new Entry(widget, marginTop);

        entries.add(entry);
    }

    public void resize(int width) {
        this.width = width;

        resetY();

        for (Entry entry : entries) {
            entry.update();
        }
    }

    private class Entry {
        private final LayoutElement widget;
        private final int marginTop;

        private Entry(LayoutElement widget, int marginTop) {
            this.widget = widget;
            this.marginTop = marginTop;
        }

        public void update() {
            int widgetWidth = widget.getWidth();
            int widgetHeight = widget.getHeight();

            widget.setX((width - widgetWidth) / 2);
            widget.setY(currentY + marginTop);

            currentY += marginTop + widgetHeight;

            if (widget instanceof Layout layout) {
                layout.arrangeElements();
            }
        }
    }
}
