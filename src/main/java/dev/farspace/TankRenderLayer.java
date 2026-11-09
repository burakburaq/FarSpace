package dev.farspace;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;

public final class TankRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("farspace", "textures/entity/oxygen_tank.png");
    private final ModelPart[] models = {createTank(6), createTank(9), createTank(12)};
    public TankRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }
    public static void initialize() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (type == EntityTypes.PLAYER && renderer instanceof AvatarRenderer<?> avatar) helper.register(new TankRenderLayer(avatar));
        });
    }
    private static ModelPart createTank(int height) {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("tank", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5f, 1, 2.5f, 3, height, 3)
                .texOffs(16, 0).addBox(-1.65f, 2, 2.35f, 3.3f, 1.5f, 3.3f)
                .texOffs(16, 0).addBox(-1.65f, height - 0.5f, 2.35f, 3.3f, 1.5f, 3.3f)
                .texOffs(32, 0).addBox(-0.65f, 0, 3.2f, 1.3f, 1, 1.3f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }
    @Override public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float a, float b) {
        if (!state.chestEquipment.is(SpaceSuit.CHEST) || state.isInvisible) return;
        var tanks = TankSlots.contents(state.chestEquipment);
        pose.pushPose();
        getParentModel().body.translateAndRotate(pose);
        for (int i = 0; i < 2; i++) {
            ItemStack stack = tanks.get(i);
            int capacity = Oxygen.capacity(stack);
            int variant = capacity == 720 ? 0 : capacity == 1440 ? 1 : capacity == 2880 ? 2 : -1;
            if (variant < 0) continue;
            pose.pushPose();
            pose.translate(i == 0 ? 0.14 : -0.14, 0, 0);
            collector.submitModelPart(models[variant], pose, RenderTypes.entityCutout(TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
            pose.popPose();
        }
        pose.popPose();
    }
}
