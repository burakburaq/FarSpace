package dev.farspace.mixin;

import dev.farspace.Oxygen;
import dev.farspace.OxygenNetwork;
import dev.farspace.OxygenCable;
import dev.farspace.SpaceSuit;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class OxygenCableExtractMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void farspace$cable(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        Vec3 relative = null;
        boolean oxygenated = false;
        if (avatar instanceof Player p && Oxygen.onMoon(p) && !p.isCreative() && !p.isSpectator() && p.isAlive() && SpaceSuit.complete(p)) {
            var source = OxygenNetwork.connection(p);
            if (source != null) {
                relative = OxygenNetwork.endpoint(p.level(), source).subtract(state.x, state.y, state.z);
                oxygenated = OxygenNetwork.visuallyOxygenated(p.level(), source);
            }
        }
        ((FabricRenderState)state).setData(OxygenCable.END, relative);
        ((FabricRenderState)state).setData(OxygenCable.OXYGENATED, oxygenated);
    }
}
