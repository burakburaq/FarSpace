package dev.farspace;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class FarSpace implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE,
                Identifier.fromNamespaceAndPath("farspace", "moon_height"), MoonHeight.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE,
                Identifier.fromNamespaceAndPath("farspace", "moon_density"), MoonDensity.CODEC);
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE,
                Identifier.fromNamespaceAndPath("farspace", "moon_surface"), MoonSurface.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE,
                Identifier.fromNamespaceAndPath("farspace", "moon_biomes"), MoonBiomeSource.CODEC);
        TankSlots.initialize();
        SpaceSuit.initialize();
        Oxygen.initialize();
        OxygenBlock.initialize();
        TetherBlock.initialize();
        OxygenNetwork.initialize();
        FarSpaceTab.initialize();
        MoonSpawn.initialize();
    }
}



