package dev.farspace.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.farspace.OxygenCable;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class OxygenCableSubmitMixin {
    @Inject(method = "submit", at = @At("HEAD"))
    private void farspace$cable(EntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (state instanceof AvatarRenderState avatar) {
            var end = ((FabricRenderState)state).getData(OxygenCable.END);
            if (end != null) OxygenCable.submit(pose, collector, new Vec3(0, avatar.boundingBoxHeight * 0.20, 0), end, camera.pos.subtract(avatar.x, avatar.y, avatar.z), ((FabricRenderState)state).getDataOrDefault(OxygenCable.OXYGENATED, false));
        }
    }
}

