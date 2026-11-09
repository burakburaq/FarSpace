package dev.farspace;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class SpaceSuit {
    private static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("farspace", "space_suit"));
    public static final Item HELMET = register("space_helmet", EquipmentSlot.HEAD);
    public static final Item CHEST = register("space_chestplate", EquipmentSlot.CHEST);
    public static final Item LEGS = register("space_leggings", EquipmentSlot.LEGS);
    public static final Item BOOTS = register("space_boots", EquipmentSlot.FEET);
    private static Item register(String name, EquipmentSlot slot) {
        var key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("farspace", name));
        var equipment = Equippable.builder(slot).setAsset(ASSET).setDamageOnHurt(false).build();
        return Registry.register(BuiltInRegistries.ITEM, key,
                new Item(new Item.Properties().setId(key).stacksTo(1).component(DataComponents.EQUIPPABLE, equipment)));
    }
    public static boolean complete(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(HELMET)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(CHEST)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(LEGS)
                && player.getItemBySlot(EquipmentSlot.FEET).is(BOOTS);
    }
    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(HELMET); entries.accept(CHEST); entries.accept(LEGS); entries.accept(BOOTS);
        });
    }
}
