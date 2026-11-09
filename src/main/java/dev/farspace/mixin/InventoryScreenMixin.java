package dev.farspace.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import dev.farspace.TankSlots;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> {
    protected InventoryScreenMixin(InventoryMenu menu, RecipeBookComponent<?> book, Inventory inventory, Component title) { super(menu, book, inventory, title); }


    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void farspace$drawTankSlots(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null || !TankSlots.hasChest(Minecraft.getInstance().player)) return;
        for (int i = 0; i < 2; i++) {
            int x = leftPos + 76, y = topPos + 7 + i * 18;
            g.fill(x, y, x + 18, y + 18, 0xFF555555);
            g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
            g.fill(x + 2, y + 2, x + 16, y + 16, 0xFF373737);
        }
    }
}



