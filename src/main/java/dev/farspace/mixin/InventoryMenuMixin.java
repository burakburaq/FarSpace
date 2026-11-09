package dev.farspace.mixin;

import dev.farspace.Oxygen;
import dev.farspace.TankSlots;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu {
    protected InventoryMenuMixin(MenuType<?> type, int id) { super(type, id); }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void farspace$slots(Inventory inventory, boolean active, Player player, CallbackInfo ci) {
        TankSlots tanks = TankSlots.forPlayer(player);
        for (int i = 0; i < 2; i++) addSlot(new Slot(tanks, i, 77, 8 + i * 18) {
            @Override public boolean mayPlace(ItemStack stack) { return TankSlots.hasChest(player) && Oxygen.capacity(stack) > 0; }
            @Override public boolean isActive() { return TankSlots.hasChest(player); }
            @Override public int getMaxStackSize() { return 1; }
        });
    }
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void farspace$quickMove(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        int first = slots.size() - 2;
        if (index < 0 || index >= slots.size()) return;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return;
        ItemStack stack = slot.getItem();
        if (index >= first || (index >= 9 && index < 45 && Oxygen.capacity(stack) > 0)) {
            ItemStack original = stack.copy();
            boolean moved = index >= first ? moveItemStackTo(stack, 9, 45, false) : moveItemStackTo(stack, first, first + 2, false);
            if (!moved) { if (index >= first) cir.setReturnValue(ItemStack.EMPTY); return; }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
            slot.onTake(player, stack);
            cir.setReturnValue(original);
        }
    }
}

