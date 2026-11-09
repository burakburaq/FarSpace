package dev.farspace.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MoonGravityMixin {
    @Inject(method = "getGravity", at = @At("RETURN"), cancellable = true)
    private void farspace$moonGravity(CallbackInfoReturnable<Double> cir) {
        Entity entity = (Entity)(Object)this;
        if (entity.level().dimension().identifier().equals(Identifier.fromNamespaceAndPath("farspace", "moon"))) {
            cir.setReturnValue(cir.getReturnValue() / 3.0);
        }
    }
}
