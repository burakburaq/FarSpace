package dev.farspace.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class MoonFallDamageMixin {
    @ModifyVariable(method = "calculateFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double farspace$scaleFallDistance(double distance) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!entity.level().dimension().identifier().equals(Identifier.fromNamespaceAndPath("farspace", "moon"))) return distance;
        // Vanilla subtracts SAFE_FALL_DISTANCE next; offset it so the resulting power is (distance - 5) / 3.
        return entity.getAttributeValue(Attributes.SAFE_FALL_DISTANCE) + Math.max(0.0, distance - 5.0) / 3.0;
    }
}
