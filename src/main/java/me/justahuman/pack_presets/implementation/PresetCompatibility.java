package me.justahuman.pack_presets.implementation;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.Text;

import java.util.List;

public class PresetCompatibility {
    private final MultilineText notification;
    private final Text confirmMessage;
    private final boolean compatible;

    public PresetCompatibility(List<ResourcePackProfile> packs) {
        int oldCount = 0;
        int newCount = 0;

        for (ResourcePackProfile pack : packs) {
            if (pack.getSource() == ModResourcePackCreator.RESOURCE_PACK_SOURCE) {
                continue;
            }

            ResourcePackCompatibility compatibility = pack.getCompatibility();
            switch (compatibility) {
                case TOO_OLD -> oldCount++;
                case TOO_NEW -> newCount++;
                default -> {}
            }
        }

        final TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        if (oldCount > 0 && newCount > 0) {
            this.notification = MultilineText.create(renderer, Text.translatable("pack_presets.preset.incompatible.both", oldCount + newCount), 257, 2);
        } else if (oldCount > 0) {
            this.notification = MultilineText.create(renderer, Text.translatable("pack_presets.preset.incompatible.old", oldCount), 257, 2);
        } else if (newCount > 0) {
            this.notification = MultilineText.create(renderer, Text.translatable("pack_presets.preset.incompatible.new", newCount), 257, 2);
        } else {
            this.notification = null;
        }

        this.confirmMessage = Text.translatable("pack_presets.preset.incompatible.confirm");
        this.compatible = oldCount == 0 && newCount == 0;
    }

    public boolean isCompatible() {
        return this.compatible;
    }

    public MultilineText getNotification() {
        return this.notification;
    }

    public Text getConfirmMessage() {
        return this.confirmMessage;
    }
}
