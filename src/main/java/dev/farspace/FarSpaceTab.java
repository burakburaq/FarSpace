package dev.farspace;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class FarSpaceTab {
    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("farspace", "items"),
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.farspace.items"))
                        .icon(() -> new ItemStack(SpaceSuit.HELMET))
                        .displayItems((parameters, output) -> {
                            output.accept(SpaceSuit.HELMET);
                            output.accept(SpaceSuit.CHEST);
                            output.accept(SpaceSuit.LEGS);
                            output.accept(SpaceSuit.BOOTS);
                            output.accept(Oxygen.TANK_1);
                            output.accept(Oxygen.TANK_2);
                            output.accept(Oxygen.TANK_3);
                            output.accept(OxygenBlock.ITEM);
                            output.accept(TetherBlock.ITEM);
                        }).build());
    }
}
