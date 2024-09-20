package me.justahuman.pack_presets.mixins;

import me.justahuman.pack_presets.PackPresets;
import me.justahuman.pack_presets.screen.CreatePresetScreen;
import me.justahuman.pack_presets.screen.PackPresetsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PackScreen.class)
public abstract class PackScreenMixin extends Screen {
    @Unique private static final Text OPEN_PRESETS = Text.translatable("pack_presets.screen.pack.button.open_presets");
    @Unique private static final Text OPEN_PRESETS_TOOLTIP = Text.translatable("pack_presets.screen.pack.button.open_presets.tooltip");
    @Unique private static final Text CREATE_PRESET = Text.translatable("pack_presets.screen.pack.button.create_preset");
    @Unique private static final Text CREATE_PRESET_TOOLTIP = Text.translatable("pack_presets.screen.pack.button.create_preset.tooltip");
    @Shadow private PackListWidget selectedPackList;

    protected PackScreenMixin(Text title) {
        super(title);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;addFooter(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;"), method = "init")
    public <T extends Widget> T addWidgets(ThreePartsLayoutWidget instance, T widget) {
        if (widget instanceof DirectionalLayoutWidget footer) {
            footer.add(ButtonWidget.builder(OPEN_PRESETS, button -> {
                this.close();
                this.client.setScreen(new PackPresetsScreen(self(), PackPresets.getPresetsDir()));
            }).tooltip(Tooltip.of(OPEN_PRESETS_TOOLTIP)).build());
        }

        this.addDrawableChild(ButtonWidget.builder(CREATE_PRESET, button -> {
            this.close();
            this.client.setScreen(new CreatePresetScreen(self()));
        }).tooltip(Tooltip.of(CREATE_PRESET_TOOLTIP)).dimensions(
            selectedPackList.getX() + selectedPackList.getWidth() + 4,
            selectedPackList.getY(),
            75, 20
        ).build());

        return instance.addFooter(widget);
    }

    @Unique
    public PackScreen self() {
        return (PackScreen) (Object) this;
    }

    @Shadow public abstract void close();
}
