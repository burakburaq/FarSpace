package dev.farspace;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record TetherWarningCondition() implements ConditionalItemModelProperty {
    public static final MapCodec<TetherWarningCondition> CODEC = MapCodec.unit(TetherWarningCondition::new);
    @Override public boolean get(ItemStack stack, ClientLevel level, LivingEntity holder, int seed, ItemDisplayContext context) {
        return TetherOxygenCondition.status(level, holder, context) == 2;
    }
    @Override public MapCodec<? extends ConditionalItemModelProperty> type() { return CODEC; }
}
