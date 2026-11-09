package dev.farspace;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class OxygenTankItem extends Item {
    private final int capacity;
    public OxygenTankItem(Properties properties, int capacity) { super(properties); this.capacity = capacity; }
    private int remaining(ItemStack stack) { return Math.clamp(stack.getOrDefault(Oxygen.REMAINING, 0), 0, capacity); }
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13.0f * remaining(stack) / capacity); }
    @Override public int getBarColor(ItemStack stack) { return 0x42BFFF; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal(remaining(stack) + "/" + capacity + " o2"));
    }
}
