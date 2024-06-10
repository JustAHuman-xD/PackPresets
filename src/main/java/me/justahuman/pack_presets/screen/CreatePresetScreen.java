package me.justahuman.pack_presets.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import me.justahuman.pack_presets.PackPresets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreatePresetScreen extends Screen {
    private final PackScreen parent;

    private TextFieldWidget nameField;
    private TextFieldWidget displayNameField;
    private TextFieldWidget descriptionField;
    private ButtonWidget createButton;

    public CreatePresetScreen(PackScreen parent) {
        super(Text.translatable("pack_presets.screen.create_preset.title"));

        this.parent = parent;
    }

    @Override
    protected void init() {
        this.nameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 66, 200, 20, Text.translatable("pack_presets.screen.create_preset.enter_name"));
        this.nameField.setTextPredicate(string -> string.isEmpty() || validName(string));
        this.nameField.setSuggestion("example_preset_name");
        this.nameField.setMaxLength(32);
        this.nameField.setChangedListener(name -> {
            this.nameField.setSuggestion(name.isEmpty() ? "example_preset_name" : "");
            this.updateCreateButton();
        });
        this.addSelectableChild(this.nameField);

        this.displayNameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 106, 200, 20, Text.translatable("pack_presets.screen.create_preset.enter_display_name"));
        this.displayNameField.setTextPredicate(string -> string.isEmpty() || validDisplayName(string));
        this.displayNameField.setSuggestion("Example Preset Name");
        this.displayNameField.setMaxLength(32);
        this.displayNameField.setChangedListener(name -> {
            this.displayNameField.setSuggestion(name.isEmpty() ? "Example Preset Name" : "");
            this.updateCreateButton();
        });
        this.addSelectableChild(this.displayNameField);

        this.descriptionField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 146, 200, 20, Text.translatable("pack_presets.screen.create_preset.enter_description"));
        this.descriptionField.setTextPredicate(string -> string.isEmpty() || validDescription(string));
        this.descriptionField.setSuggestion("This is an example preset description.");
        this.descriptionField.setMaxLength(256);
        this.descriptionField.setChangedListener(name -> {
            this.descriptionField.setSuggestion(name.isEmpty() ? "This is an example preset description." : "");
            this.updateCreateButton();
        });
        this.addSelectableChild(this.descriptionField);

        this.createButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("pack_presets.screen.create_preset.create"), button -> {
            this.createAndClose();
        }).dimensions(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> {
            close();
        }).dimensions(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20).build());

        this.setInitialFocus(this.nameField);
        this.updateCreateButton();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        final String name = this.nameField.getText();
        final String displayName = this.displayNameField.getText();
        final String description = this.descriptionField.getText();

        this.init(client, width, height);

        this.nameField.setText(name);
        this.displayNameField.setText(displayName);
        this.descriptionField.setText(description);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 17, 16777215);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("pack_presets.screen.create_preset.enter_name"), this.width / 2 - 100 + 1, 53, 10526880);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("pack_presets.screen.create_preset.enter_display_name"), this.width / 2 - 100 + 1, 94, 10526880);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("pack_presets.screen.create_preset.enter_description"), this.width / 2 - 100 + 1, 134, 10526880);
        this.nameField.render(context, mouseX, mouseY, delta);
        this.displayNameField.render(context, mouseX, mouseY, delta);
        this.descriptionField.render(context, mouseX, mouseY, delta);
    }

    public void updateCreateButton() {
        this.createButton.active =
                validName(this.nameField.getText())
                && validDisplayName(this.displayNameField.getText())
                && validDescription(this.descriptionField.getText());
    }

    public void createAndClose() {
        final JsonObject preset = new JsonObject();
        final JsonArray packs = new JsonArray();

        this.parent.organizer.resourcePackManager.getEnabledIds().forEach(packs::add);

        preset.addProperty("name", this.nameField.getText());
        preset.addProperty("display_name", this.displayNameField.getText());
        preset.addProperty("description", this.descriptionField.getText());
        preset.add("packs", packs);

        final File presetFile = PackPresets.getPresetsDir().resolve(this.nameField.getText() + ".json").toFile();
        try {
            presetFile.getParentFile().mkdirs();
            presetFile.createNewFile();
        } catch (IOException e) {
            LogUtils.getLogger().warn("Failed to create preset to {}", presetFile.getPath(), e);
            close();
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(presetFile))) {
            PackPresets.GSON.toJson(preset, writer);
            writer.flush();
        } catch (JsonIOException | IOException e) {
            LogUtils.getLogger().warn("Failed to create preset to {}", presetFile.getPath(), e);
        }

        close();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    public static boolean validName(String string) {
        return !string.isBlank() && string.matches("^[a-z0-9_]+$");
    }

    public static boolean validDisplayName(String string) {
        return !string.isBlank() && string.matches("^.+$");
    }

    public static boolean validDescription(String string) {
        return !string.isBlank() && string.matches("^.+$");
    }
}
