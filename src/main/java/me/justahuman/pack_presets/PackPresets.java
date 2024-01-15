package me.justahuman.pack_presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.justahuman.pack_presets.implementation.PackPresetProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Path;

public class PackPresets implements ClientModInitializer {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PackPresetProvider provider;

    @Override
    public void onInitializeClient() {
        provider = new PackPresetProvider(getPresetsDir(), MinecraftClient.getInstance().getResourcePackManager());
    }

    public static Path getPresetsDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("pack_presets");
    }

    public static PackPresetProvider getProvider() {
        return provider;
    }
}
