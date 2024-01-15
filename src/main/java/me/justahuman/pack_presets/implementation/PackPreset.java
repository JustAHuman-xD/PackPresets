package me.justahuman.pack_presets.implementation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class PackPreset {
    private final String name;
    private final Text displayName;
    private final MultilineText description;
    private final List<ResourcePackProfile> packs;
    private final PresetCompatibility compatibility;

    public PackPreset(String name, Text displayName, MultilineText description, List<ResourcePackProfile> packs) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.packs = packs;
        this.compatibility = new PresetCompatibility(packs);
    }

    public void apply(ResourcePackOrganizer organizer) {
        final ResourcePackManager manager = organizer.resourcePackManager;
        final Consumer<ResourcePackManager> applier = organizer.applier;
        manager.setEnabledProfiles(this.packs.stream().map(ResourcePackProfile::getName).collect(ImmutableList.toImmutableList()));
        applier.accept(manager);
    }

    public String getName() {
        return name;
    }

    public Text getDisplayName() {
        return displayName;
    }

    public MultilineText getDescription() {
        return description;
    }

    public List<ResourcePackProfile> getPacks() {
        return packs;
    }

    public PresetCompatibility getCompatibility() {
        return compatibility;
    }

    public List<Identifier> getPackIcons(ResourcePackOrganizer organizer) {
        final List<Identifier> icons = new ArrayList<>();
        for (ResourcePackProfile profile : Lists.reverse(this.packs)) {
            if (icons.size() == 4) {
                break;
            }

            final Identifier icon = organizer.iconIdSupplier.apply(profile);
            if (!icon.equals(PackScreen.UNKNOWN_PACK)) {
                icons.add(icon);
            }
        }

        if (icons.isEmpty()) {
            icons.add(PackScreen.UNKNOWN_PACK);
        }

        return icons;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PackPreset) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.displayName, that.displayName) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.packs, that.packs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, displayName, description, packs);
    }

    @Override
    public String toString() {
        return "PackPreset[" +
                "name=" + name + ", " +
                "displayName=" + displayName + ", " +
                "description=" + description + ", " +
                "packs=" + packs + ']';
    }
}
