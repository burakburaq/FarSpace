package dev.farspace;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class FirstPersonOxygenCable {
    private record Cable(Vec3 start, Vec3 end, boolean oxygenated) {}
    private static final RenderStateDataKey<Cable> CABLE = RenderStateDataKey.create(() -> "farspace:first_person_oxygen_cable");
    public static void initialize() {
        LevelExtractionEvents.END_EXTRACTION.register(context -> {
            var client = Minecraft.getInstance();
            var player = client.player;
            Cable cable = null;
            if (player != null && client.getCameraEntity() == player && client.options.getCameraType().isFirstPerson()
                    && Oxygen.onMoon(player) && player.isAlive() && !player.isCreative() && !player.isSpectator() && SpaceSuit.complete(player)) {
                var source = OxygenNetwork.connection(player);
                if (source != null) {
                    float partial = context.deltaTracker().getGameTimeDeltaPartialTick(false);
                    Vec3 position = player.getPosition(partial);
                    cable = new Cable(position.add(0, player.getBbHeight() * 0.20, 0), OxygenNetwork.endpoint(player.level(), source), OxygenNetwork.visuallyOxygenated(player.level(), source));
                }
            }
            ((FabricRenderState)context.levelState()).setData(CABLE, cable);
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            Cable cable = ((FabricRenderState)context.levelState()).getData(CABLE);
            if (cable == null) return;
            Vec3 camera = context.levelState().cameraRenderState.pos;
            OxygenCable.submit(context.poseStack(), context.submitNodeCollector(), cable.start.subtract(camera), cable.end.subtract(camera), Vec3.ZERO, cable.oxygenated);
        });
    }
}

