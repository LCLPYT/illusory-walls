package work.lclpnet.illwalls.screen.lib;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class WideCheckboxWidget extends CheckboxWidget {

    private Consumer<WideCheckboxWidget> onClick = null;

    public WideCheckboxWidget(int x, int y, int width, int height, Text message, boolean checked) {
        super(x, y, width, height, message, checked);
    }

    @Override
    public int getWidth() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return 24 + textRenderer.getWidth(getMessage());
    }

    @Override
    public void onPress() {
        super.onPress();

        if (onClick != null) {
            onClick.accept(this);
        }
    }

    public void setOnClick(Consumer<WideCheckboxWidget> onClick) {
        this.onClick = onClick;
    }
}
