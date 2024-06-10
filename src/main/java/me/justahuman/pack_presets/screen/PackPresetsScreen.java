package me.justahuman.pack_presets.screen;

import com.mojang.logging.LogUtils;
import me.justahuman.pack_presets.screen.widget.PresetListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class PackPresetsScreen extends Screen {
    private static final Text TITLE = Text.translatable("pack_presets.screen.presets.title");
    private static final Text SUB_TITLE = Text.translatable("pack_presets.screen.presets.subtitle").formatted(Formatting.GRAY);
    private final PackScreen parent;
    private final ResourcePackOrganizer organizer;
    private final Path presetsDirectory;

    @Nullable
    private PackScreen.DirectoryWatcher directoryWatcher;
    private long refreshTimeout;

    private PresetListWidget presetList;

    public PackPresetsScreen(PackScreen parent, Path presetsDirectory) {
        super(TITLE);

        this.parent = parent;
        this.organizer = parent.organizer;
        this.presetsDirectory = presetsDirectory;

        this.directoryWatcher = PackScreen.DirectoryWatcher.create(presetsDirectory);
    }

    @Override
    protected void init() {
        this.presetList = this.addDrawableChild(new PresetListWidget(this.client, this, 300, this.height));
        this.presetList.setX(this.width / 2 - 150);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("pack_presets.screen.presets.folder"), button -> {
            Util.getOperatingSystem().open(this.presetsDirectory.toUri());
        }).dimensions(this.width / 2 - 125 - 1, this.height - 48, 125, 20).tooltip(Tooltip.of(Text.translatable("pack_presets.screen.presets.folder.tooltip"))).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> {
            close();
        }).dimensions(this.width / 2 + 1, this.height - 48, 125, 20).build());

        this.refresh();
    }

    @Override
    public void tick() {
        if (this.directoryWatcher != null) {
            try {
                if (this.directoryWatcher.pollForChange()) {
                    this.refreshTimeout = 20L;
                }
            } catch (IOException e) {
                LogUtils.getLogger().warn("Failed to poll for directory {} changes, stopping", this.presetsDirectory);
                this.closeDirectoryWatcher();
            }
        }

        if (this.refreshTimeout > 0L && --this.refreshTimeout == 0L) {
            this.refresh();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, TITLE, this.width / 2, 8, 16777215);
        context.drawCenteredTextWithShadow(this.textRenderer, SUB_TITLE, this.width / 2, 20, 0xFFFFFF);
    }

    public void refresh() {
        this.organizer.refresh();
        this.presetList.refresh();
        this.refreshTimeout = 0L;
    }

    @Override
    public void close() {
        this.closeDirectoryWatcher();
        this.client.setScreen(this.parent);
    }

    public void closeDirectoryWatcher() {
        if (this.directoryWatcher != null) {
            try {
                this.directoryWatcher.close();
                this.directoryWatcher = null;
            } catch (Exception ignored) {}
        }
    }

    public ResourcePackOrganizer getOrganizer() {
        return organizer;
    }
}
