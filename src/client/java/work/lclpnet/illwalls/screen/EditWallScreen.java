package work.lclpnet.illwalls.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
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
    private Checkbox respawnCheckbox = null;
    private StringWidget cooldownHeader = null;
    private EditBox cooldownInput = null;
    private CycleButton<McTimeUnit> unitButton = null;
    private McTimeUnit prevUnit = null;

    public EditWallScreen(IllusoryWallPlayerSettings settings, @Nullable IllusoryWallEntity entity) {
        super(Component.translatable(entity != null ? "illusory_wall.edit" : "illusory_wall.edit_defaults"));

        this.settings = settings;
        this.entity = entity;
    }

    @Override
    protected void init() {
        boolean shouldRespawn = settings.shouldRespawn();

        respawnCheckbox = Checkbox.builder(Component.translatable("illusory_wall.edit.respawn"), font)
                .pos(0, 0)
                .selected(shouldRespawn)
                .onValueChange((checkbox, checked) -> {
                    if (checked) {
                        addCooldownUi();
                    } else {
                        removeCooldownUi();
                    }
                })
                .build();

        addRenderableWidget(respawnCheckbox);

        uiBuilder.add(respawnCheckbox);
        uiBuilder.resize(width);

        if (shouldRespawn) {
            addCooldownUi();
        }
    }

    private void addCooldownUi() {
        removeCooldownUi();

        cooldownInput = new EditBox(font, 0, 25, 60, 20,
                Component.translatable("illusory_wall.edit.respawn_duration"));

        cooldownHeader = new StringWidget(Component.translatable("illusory_wall.edit.cooldown"), font);

        McTimeUnit cooldownUnit = restoreState(cooldownInput);
        prevUnit = cooldownUnit;

        unitButton = new CycleButton.Builder<>(McTimeUnit::asText)
                .withValues(McTimeUnit.values())
                .withInitialValue(cooldownUnit)
                .create(0, 0, 100, 20, Component.translatable("illusory_wall.edit.unit"),
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
        addRenderableOnly(cooldownHeader);
        addRenderableWidget(cooldownInput);
        addRenderableWidget(unitButton);
    }

    private void removeCooldownUi() {
        if (cooldownHeader != null) removeWidget(cooldownHeader);
        if (cooldownInput != null) removeWidget(cooldownInput);
        if (unitButton != null) removeWidget(unitButton);

        uiBuilder.reset();
    }

    private @NotNull McTimeUnit restoreState(EditBox cooldownInput) {
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

        cooldownInput.setValue(String.valueOf(cooldownValue));

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

            cooldownInput.setValue(String.valueOf(value));
        });
    }

    private OptionalInt getCooldown(EditBox textField) {
        String text = textField.getValue().trim();

        try {
            int value = Integer.parseInt(text);

            return OptionalInt.of(Math.max(0, value));
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        super.resize(client, width, height);

        uiBuilder.resize(width);
    }


    @Override
    public void tick() {
        if (entity != null && entity.isRemoved()) {
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderTransparentBackground(context);
        context.drawCenteredString(this.font, this.title, this.width / 2, OFFSET_Y, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        super.onClose();

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

    private OptionalInt getRespawnDurationTicks(Checkbox respawnCheckbox) {
        if (!respawnCheckbox.selected()) {
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
