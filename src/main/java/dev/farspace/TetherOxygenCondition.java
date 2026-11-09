package dev.farspace;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** A held tether previews whether a newly placed tether could receive oxygen. */
public record TetherOxygenCondition() implements ConditionalItemModelProperty {
    public static final MapCodec<TetherOxygenCondition> CODEC = MapCodec.unit(TetherOxygenCondition::new);
    @Override public boolean get(ItemStack stack, ClientLevel level, LivingEntity holder, int seed, ItemDisplayContext context) {
        return status(level, holder, context) == 1;
    }
    static int status(ClientLevel level, LivingEntity holder, ItemDisplayContext context) {
        boolean inHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (!inHand || level == null || holder == null || holder.level() != level) return 0;
        if (OxygenBlock.find(level, holder.getX(), holder.getY(), holder.getZ()) != null) return 1;
        double nearestDistanceSq = Double.POSITIVE_INFINITY;
        for (TetherNode node : TetherBlock.nearby(level, holder.blockPosition(), 34)) {
            if (node.getBlockState().getValue(TetherBlock.CONNECTABLE)
                    && node.links().size() + (node.oxygenSource() != null ? 1 : 0) < 2)
                nearestDistanceSq = Math.min(nearestDistanceSq, node.getBlockPos().distSqr(holder.blockPosition()));
        }
        if (nearestDistanceSq < 30 * 30) return 1;
        return nearestDistanceSq <= 34 * 34 ? 2 : 0;
    }
    @Override public MapCodec<? extends ConditionalItemModelProperty> type() { return CODEC; }
}
