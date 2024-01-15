package me.justahuman.pack_presets.mixins;

import me.justahuman.pack_presets.PackPresets;
import me.justahuman.pack_presets.screen.CreatePresetScreen;
import me.justahuman.pack_presets.screen.PackPresetsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackScreen.class)
public abstract class PackScreenMixin extends Screen {
    @Shadow private ButtonWidget doneButton;
    @Shadow private PackListWidget selectedPackList;

    protected PackScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void addWidgets(CallbackInfo ci) {
        final int x = this.width / 2;
        final int y = this.height - 48;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("pack_presets.screen.pack.button.open_presets"), button -> {
            this.close();
            this.client.setScreen(new PackPresetsScreen(self(), PackPresets.getPresetsDir()));
        }).tooltip(Tooltip.of(Text.translatable("pack_presets.screen.pack.button.open_presets.tooltip")))
        .dimensions(x - 100 - 2 - 50, y, 100, 20).build());

        if (this.drawables.get(2) instanceof ButtonWidget folderButton) {
            folderButton.setDimensionsAndPosition(100, 20, x - 50, y);
        }

        doneButton.setDimensionsAndPosition(100, 20, x + 50 + 2, y);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("pack_presets.screen.pack.button.create_preset"), button -> {
            this.close();
            this.client.setScreen(new CreatePresetScreen(self()));
        }).tooltip(Tooltip.of(Text.translatable("pack_presets.screen.pack.button.create_preset.tooltip")))
        .dimensions(selectedPackList.getX() + selectedPackList.getWidth() + 4, selectedPackList.getY(), 75, 20)
        .build());
    }

    @Unique
    public PackScreen self() {
        return (PackScreen) (Object) this;
    }

    @Shadow public abstract void close();
}
