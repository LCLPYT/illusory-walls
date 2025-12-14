package work.lclpnet.illwalls.screen.lib;

import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;

import java.util.function.Consumer;

public class RowWidget implements Layout {

    private final int margin;
    private final LayoutElement[] children;
    private int width, height;
    private int x, y;

    public RowWidget(int margin, LayoutElement... children) {
        this.margin = margin;
        this.children = children;

        update();
    }

    public void update() {
        width = 0;
        height = 0;

        for (LayoutElement child : children) {
            width += child.getWidth();
            height = Math.max(height, child.getHeight());
        }

        if (children.length > 0) {
            width += margin * (children.length - 1);
        }
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        for (LayoutElement child : children) {
            consumer.accept(child);
        }
    }

    @Override
    public void arrangeElements() {
        Layout.super.arrangeElements();

        int x = this.x;

        for (LayoutElement child : children) {
            int childWidth = child.getWidth();
            int childHeight = child.getHeight();

            child.setX(x);
            child.setY(this.y + (height - childHeight) / 2);

            x += childWidth + margin;
        }
    }
}
