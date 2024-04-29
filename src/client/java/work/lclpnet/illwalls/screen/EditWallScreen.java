package work.lclpnet.illwalls.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import work.lclpnet.illwalls.entity.IllusoryWallEntity;
import work.lclpnet.illwalls.network.ApplyWallSettingsC2SPacket;
import work.lclpnet.illwalls.screen.lib.RowWidget;
import work.lclpnet.illwalls.screen.lib.UiBuilder;
import work.lclpnet.illwalls.util.McTimeUnit;
import work.lclpnet.illwalls.wall.IllusoryWallPlayerSettings;
import work.lclpnet.illwalls.wall.IllusoryWallProperties;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public class EditWallScreen extends Screen {

    private final int OFFSET_Y = 20;
    private final UiBuilder uiBuilder = new UiBuilder(OFFSET_Y + 30, 4);
    private final IllusoryWallPlayerSettings settings;
    @Nullable
    private final IllusoryWallEntity entity;
    private CheckboxWidget respawnCheckbox = null;
    private TextWidget cooldownHeader = null;
    private TextFieldWidget cooldownInput = null;
    private CyclingButtonWidget<McTimeUnit> unitButton = null;
    private McTimeUnit prevUnit = null;

    public EditWallScreen(IllusoryWallPlayerSettings settings, @Nullable IllusoryWallEntity entity) {
        super(Text.translatable(entity != null ? "illusory_wall.edit" : "illusory_wall.edit_defaults"));

        this.settings = settings;
        this.entity = entity;
    }

    @Override
    protected void init() {
        boolean shouldRespawn = settings.shouldRespawn();

        respawnCheckbox = CheckboxWidget.builder(Text.translatable("illusory_wall.edit.respawn"), textRenderer)
                .pos(0, 0)
                .checked(shouldRespawn)
                .callback((checkbox, checked) -> {
                    if (checked) {
                        addCooldownUi();
                    } else {
                        removeCooldownUi();
                    }
                })
                .build();

        addDrawableChild(respawnCheckbox);

        uiBuilder.add(respawnCheckbox);
        uiBuilder.resize(width);

        if (shouldRespawn) {
            addCooldownUi();
        }
    }

    private void addCooldownUi() {
        removeCooldownUi();

        cooldownInput = new TextFieldWidget(textRenderer, 0, 25, 60, 20,
                Text.translatable("illusory_wall.edit.respawn_duration"));

        cooldownHeader = new TextWidget(Text.translatable("illusory_wall.edit.cooldown"), textRenderer);

        McTimeUnit cooldownUnit = restoreState(cooldownInput);
        prevUnit = cooldownUnit;

        unitButton = new CyclingButtonWidget.Builder<>(McTimeUnit::asText)
                .values(McTimeUnit.values())
                .initially(cooldownUnit)
                .build(0, 0, 100, 20, Text.translatable("illusory_wall.edit.unit"),
                        (btn, unit) -> changeUnit(unit));

        RowWidget cooldownRow = new RowWidget(4, cooldownInput, unitButton);

        // add widgets to the builder
        uiBuilder.reset();
        uiBuilder.add(respawnCheckbox);
        uiBuilder.add(cooldownHeader, 14);
        uiBuilder.add(cooldownRow);

        // update the widget positions
        uiBuilder.resize(width);

        // actually add the widgets to the screen
        addDrawable(cooldownHeader);
        addDrawableChild(cooldownInput);
        addDrawableChild(unitButton);
    }

    private void removeCooldownUi() {
        if (cooldownHeader != null) remove(cooldownHeader);
        if (cooldownInput != null) remove(cooldownInput);
        if (unitButton != null) remove(unitButton);

        uiBuilder.reset();
    }

    private @NotNull McTimeUnit restoreState(TextFieldWidget cooldownInput) {
        int cooldownValue;
        McTimeUnit cooldownUnit;

        int ticks = settings.getRespawnDuration();

        if (ticks < 0) {
            cooldownValue = 60;
            cooldownUnit = McTimeUnit.SECONDS;
        } else {
            cooldownUnit = McTimeUnit.getBiggestFittingTimeUnit(ticks);
            cooldownValue = cooldownUnit.fromTicks(ticks);
        }

        cooldownInput.setText(String.valueOf(cooldownValue));

        return cooldownUnit;
    }

    private void changeUnit(McTimeUnit newUnit) {
        McTimeUnit prevUnit = this.prevUnit;
        this.prevUnit = newUnit;

        if (cooldownInput == null) return;

        getCooldown(cooldownInput).ifPresent(value -> {
            boolean wasZero = value == 0;

            value = newUnit.fromTicks(prevUnit.toTicks(value));

            if (!wasZero) {
                value = Math.max(value, 1);
            }

            cooldownInput.setText(String.valueOf(value));
        });
    }

    private OptionalInt getCooldown(TextFieldWidget textField) {
        String text = textField.getText().trim();

        try {
            int value = Integer.parseInt(text);

            return OptionalInt.of(Math.max(0, value));
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);

        uiBuilder.resize(width);
    }


    @Override
    public void tick() {
        if (entity != null && entity.isRemoved()) {
            this.close();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, OFFSET_Y, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        super.close();

        applySettings();
    }

    private void applySettings() {
        if (respawnCheckbox == null) return;

        OptionalInt respawnDuration = getRespawnDurationTicks(respawnCheckbox);

        if (respawnDuration.isEmpty()) return;

        settings.setRespawnDuration(respawnDuration.getAsInt());

        var packet = new ApplyWallSettingsC2SPacket(settings, entity);
        ClientPlayNetworking.send(packet);
    }

    private OptionalInt getRespawnDurationTicks(CheckboxWidget respawnCheckbox) {
        if (!respawnCheckbox.isChecked()) {
            return OptionalInt.of(IllusoryWallProperties.NO_RESPAWN);
        }

        if (cooldownInput == null || unitButton == null) {
            return OptionalInt.empty();
        }

        OptionalInt cooldown = getCooldown(cooldownInput);

        if (cooldown.isEmpty()) {
            return OptionalInt.empty();
        }

        McTimeUnit unit = unitButton.getValue();

        return OptionalInt.of(unit.toTicks(cooldown.getAsInt()));

    }
}
