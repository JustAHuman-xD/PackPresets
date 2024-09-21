package me.justahuman.pack_presets.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.justahuman.pack_presets.PackPresets;
import me.justahuman.pack_presets.implementation.PackPreset;
import me.justahuman.pack_presets.implementation.PresetCompatibility;
import me.justahuman.pack_presets.screen.PackPresetsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackListWidget.ResourcePackEntry;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public class PresetListWidget extends AlwaysSelectedEntryListWidget<PresetListWidget.PresetEntry> {
    private final PackPresetsScreen screen;

    public PresetListWidget(MinecraftClient client, PackPresetsScreen screen, int width, int height) {
        super(client, width, height, height - 83, 32, 36);

        this.screen = screen;
        this.centerListVertically = false;
        Objects.requireNonNull(client.textRenderer);
        this.setRenderHeader(false, 0);
    }

    public void refresh() {
        children().clear();
        final PresetEntry entry = getSelectedOrNull();
        final String name = entry == null ? "" : entry.getPreset().getName();
        setSelected(null);

        for (PackPreset preset : PackPresets.getProvider().getPresets()) {
            System.out.println("preset: " + preset.getName());
            PresetEntry presetEntry = new PresetEntry(this.client, this, preset);
            children().add(presetEntry);
            if (preset.getName().equals(name)) {
                setSelected(presetEntry);
            }
        }
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.getRowRight() - 6;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        final PresetEntry entry = this.getSelectedOrNull();
        if (entry != null && keyCode == 257) {
            entry.apply();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static class PresetEntry extends AlwaysSelectedEntryListWidget.Entry<PresetEntry> {
        protected final MinecraftClient client;
        private final PresetListWidget widget;
        private final PackPreset preset;

        private final OrderedText displayName;
        private final MultilineText description;
        private final OrderedText incompatibleText;
        private final MultilineText compatibilityNotificationText;

        public PresetEntry(MinecraftClient client, PresetListWidget widget, PackPreset preset) {
            this.client = client;
            this.widget = widget;
            this.preset = preset;

            this.displayName = ResourcePackEntry.trimTextToWidth(client, preset.getDisplayName());
            this.description = preset.getDescription();
            this.incompatibleText = ResourcePackEntry.trimTextToWidth(client, PackListWidget.INCOMPATIBLE);
            this.compatibilityNotificationText = preset.getCompatibility().getNotification();
        }

        @Override
        public Text getNarration() {
            return Text.translatable("narrator.select", this.preset.getDisplayName());
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            final PresetCompatibility compatibility = this.preset.getCompatibility();
            if (!compatibility.isCompatible()) {
                DrawableHelper.fill(matrices, x - 1, y - 1, x + entryWidth - 3, y + entryHeight +1, -8978432);
            }

            final List<Identifier> icons = this.preset.getPackIcons(this.widget.screen.getOrganizer());
            final int width = 32 / icons.size();
            for (int i = 0; i < icons.size(); i++) {
                final Identifier icon = icons.get(i);
                RenderSystem.setShaderTexture(0, icon);
                DrawableHelper.drawTexture(matrices, x + (i * width), y, 0.0F, 0.0F, width, 32, 32, 32);
            }

            OrderedText nameText = this.displayName;
            MultilineText descriptionText = this.description;
            if (Boolean.TRUE.equals(this.client.options.getTouchscreen().getValue()) || hovered || this.widget.getSelectedOrNull() == this && this.widget.isFocused()) {
                RenderSystem.setShaderTexture(0, PackListWidget.RESOURCE_PACKS_TEXTURE);
                DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
                if (!compatibility.isCompatible()) {
                    nameText = this.incompatibleText;
                    descriptionText = this.compatibilityNotificationText;
                }

                int relativeX = mouseX - x;
                DrawableHelper.drawTexture(matrices, x, y, 0.0F, relativeX < 32 ? 32.0F : 0.0F, 32, 32, 256, 256);
            }

            this.client.textRenderer.drawWithShadow(matrices, nameText, x + 32 + 2, y + 1, 16777215);
            descriptionText.drawWithShadow(matrices, x + 32 + 2, y + 12, 10, -8355712);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            double relativeX = mouseX - this.widget.getRowLeft();
            if (relativeX <= 32.0) {
                this.widget.setSelected(null);
                this.apply();
                return true;
            }

            return false;
        }

        public void apply() {
            final PresetCompatibility compatibility = this.preset.getCompatibility();
            if (compatibility.isCompatible()) {
                this.preset.apply(this.widget.screen.getOrganizer());
                return;
            }

            this.client.setScreen(new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    this.preset.apply(this.widget.screen.getOrganizer());
                }
            }, Text.translatable("pack_presets.preset.incompatible.confirm.title"), compatibility.getConfirmMessage()));
        }

        public PackPreset getPreset() {
            return this.preset;
        }
    }
}
