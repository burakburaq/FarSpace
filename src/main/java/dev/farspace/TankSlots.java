package dev.farspace;

import java.util.HashMap;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class TankSlots implements Container {
    // Keep the old attachment types registered so existing tanks can migrate without loss.
    private static AttachmentType<ItemStack> legacy(String name) {
        return AttachmentRegistry.create(Identifier.fromNamespaceAndPath("farspace", name), b -> b
                .initializer(() -> ItemStack.EMPTY).persistent(ItemStack.OPTIONAL_CODEC)
                .syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.targetOnly()).copyOnDeath());
    }
    public static final AttachmentType<ItemStack> FIRST = legacy("tank_slot_1");
    public static final AttachmentType<ItemStack> SECOND = legacy("tank_slot_2");
    private static final ReferenceQueue<Player> EXPIRED = new ReferenceQueue<>();
    private static final HashMap<PlayerRef, TankSlots> INSTANCES = new HashMap<>();
    private static final class PlayerRef extends WeakReference<Player> {
        private final int hash;
        PlayerRef(Player player, ReferenceQueue<Player> queue) {
            super(player, queue);
            hash = System.identityHashCode(player);
        }
        @Override public int hashCode() { return hash; }
        @Override public boolean equals(Object other) {
            if (this == other) return true;
            return other instanceof PlayerRef ref && get() != null && get() == ref.get();
        }
    }
    public static synchronized TankSlots forPlayer(Player player) {
        PlayerRef expired;
        while ((expired = (PlayerRef) EXPIRED.poll()) != null) INSTANCES.remove(expired);
        PlayerRef lookup = new PlayerRef(player, null);
        TankSlots slots = INSTANCES.get(lookup);
        if (slots == null) {
            slots = new TankSlots(player);
            INSTANCES.put(new PlayerRef(player, EXPIRED), slots);
        }
        return slots;
    }
    public static void initialize() { /* Register legacy attachment types before player data loads. */ }
    private final java.lang.ref.WeakReference<Player> owner;
    private ItemStack chest = ItemStack.EMPTY;
    private ItemContainerContents lastContents;
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private TankSlots(Player player) { owner = new java.lang.ref.WeakReference<>(player); }
    public static boolean hasChest(Player player) { return player.getItemBySlot(EquipmentSlot.CHEST).is(SpaceSuit.CHEST); }
    public static NonNullList<ItemStack> contents(ItemStack chest) {
        var result = NonNullList.withSize(2, ItemStack.EMPTY);
        if (chest.is(SpaceSuit.CHEST)) chest.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(result);
        return result;
    }
    private void refresh() {
        Player player = owner.get();
        ItemStack current = player != null && hasChest(player) ? player.getItemBySlot(EquipmentSlot.CHEST) : ItemStack.EMPTY;
        ItemContainerContents data = current.isEmpty() ? ItemContainerContents.EMPTY : current.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (current != chest || data != lastContents) {
            items.set(0, ItemStack.EMPTY); items.set(1, ItemStack.EMPTY);
            data.copyInto(items); chest = current; lastContents = data;
        }
    }
    public void migrateLegacy() {
        Player player = owner.get();
        if (player == null || player.level().isClientSide() || !hasChest(player)) return;
        refresh();
        for (int i = 0; i < 2; i++) {
            var type = i == 0 ? FIRST : SECOND;
            ItemStack old = player.getAttachedOrElse(type, ItemStack.EMPTY);
            if (items.get(i).isEmpty() && !old.isEmpty()) {
                items.set(i, old.copy()); publish(); player.removeAttached(type);
            }
        }
    }
    private void publish() {
        if (chest.isEmpty()) return;
        lastContents = ItemContainerContents.fromItems(items);
        chest.set(DataComponents.CONTAINER, lastContents);
    }
    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() { return getItem(0).isEmpty() && getItem(1).isEmpty(); }
    @Override public ItemStack getItem(int index) { refresh(); return items.get(index); }
    @Override public void setItem(int index, ItemStack stack) { refresh(); if (!chest.isEmpty()) { items.set(index, stack.copy()); publish(); } }
    @Override public ItemStack removeItem(int index, int count) {
        refresh(); ItemStack result = items.get(index).split(count); publish(); return result;
    }
    @Override public ItemStack removeItemNoUpdate(int index) { refresh(); ItemStack result = items.set(index, ItemStack.EMPTY); publish(); return result; }
    @Override public void setChanged() { refresh(); publish(); }
    @Override public boolean stillValid(Player p) { return p == owner.get(); }
    @Override public void clearContent() { refresh(); items.set(0, ItemStack.EMPTY); items.set(1, ItemStack.EMPTY); publish(); }
}

