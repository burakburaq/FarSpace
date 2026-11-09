package dev.farspace.mixin;

import dev.farspace.Oxygen;
import dev.farspace.SpaceSuit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MoonFreezingMixin {
    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    private void farspace$freezeWithoutSuit(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof Player player && Oxygen.onMoon(player) && !SpaceSuit.complete(player)
                && !player.isCreative() && !player.isSpectator()) cir.setReturnValue(true);
    }
}
