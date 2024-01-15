package me.justahuman.pack_presets.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import me.justahuman.pack_presets.PackPresets;
import me.justahuman.pack_presets.screen.CreatePresetScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.Text;
import net.minecraft.util.PathUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PackPresetProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path directory;
    private final ResourcePackManager manager;

    public PackPresetProvider(Path directory, ResourcePackManager manager) {
        this.directory = directory;
        this.manager = manager;
    }

    public List<PackPreset> getPresets() {
        final List<PackPreset> presets = new ArrayList<>();

        try {
            PathUtil.createDirectories(this.directory);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.directory)) {
                for (Path path : stream) {
                    final String fileName = path.getFileName().toString();
                    if (!fileName.endsWith(".json")) {
                        LOGGER.warn("Found non-json file in presets directory: {}", fileName);
                    }

                    final JsonObject json = PackPresets.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class);
                    final PackPreset preset = this.getPreset(path, json);
                    if (preset != null) {
                        presets.add(preset);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to list presets in {}", this.directory, e);
        }

        return presets;
    }

    private PackPreset getPreset(Path path, JsonObject json) {
        try {
            final String name = json.get("name").getAsString();
            if (!CreatePresetScreen.validName(name)) {
                LOGGER.warn("Invalid preset name '{}' of '{}', ignoring", name, path);
                return null;
            }

            final String displayName = json.get("display_name").getAsString();
            if (!CreatePresetScreen.validDisplayName(displayName)) {
                LOGGER.warn("Invalid preset display name '{}' of '{}', ignoring", displayName, path);
                return null;
            }

            final String description = json.get("description").getAsString();
            if (!CreatePresetScreen.validDescription(description)) {
                LOGGER.warn("Invalid preset description '{}' of '{}', ignoring", description, path);
                return null;
            }

            final List<String> packs = json.get("packs").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();
            if (packs.isEmpty()) {
                LOGGER.warn("Preset '{}' of '{}' has no packs, ignoring", name, path);
                return null;
            }

            return new PackPreset(
                    name,
                    Text.literal(displayName),
                    MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.literal(description), 257, 2),
                    packs.stream().map(manager::getProfile).filter(Objects::nonNull).toList()
            );
        } catch (Exception e) {
            LOGGER.warn("Failed to load preset '{}', ignoring", path, e);
            return null;
        }
    }
}
