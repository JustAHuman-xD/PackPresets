package me.justahuman.pack_presets.mixins;

import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntryListWidget.class)
public interface EntryWidgetAccessor {
    @Accessor
    int getTop();
}
