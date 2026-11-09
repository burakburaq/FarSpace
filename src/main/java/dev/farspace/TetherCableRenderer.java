package dev.farspace;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class TetherCableRenderer {
    private record Cable(Vec3 start, Vec3 end, boolean oxygenated) {}
    private static final RenderStateDataKey<List<Cable>> LINES = RenderStateDataKey.create(() -> "farspace:tether_lines");
    public static void initialize() {
        LevelExtractionEvents.END_EXTRACTION.register(context -> {
            var player = Minecraft.getInstance().player;
            List<Cable> lines = new ArrayList<>();
            if (player != null && player.level() == context.level()) {
                for (TetherNode node : TetherBlock.nearby(context.level(), player.blockPosition(), 96)) {
                    var pos = node.getBlockPos();
                    Vec3 start = OxygenNetwork.endpoint(context.level(), pos);
                    boolean oxygenated = node.getBlockState().getValue(TetherBlock.CONNECTABLE);
                    for (var end : node.links()) {
                        if (pos.asLong() < end.asLong()) lines.add(new Cable(start, new Vec3(end.getX()+0.5, end.getY()+1.3, end.getZ()+0.5), oxygenated));
                    }
                    var source = node.oxygenSource();
                    if (source != null) lines.add(new Cable(start, Vec3.atCenterOf(source), oxygenated));
                }
            }
            ((FabricRenderState)context.levelState()).setData(LINES, List.copyOf(lines));
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            List<Cable> lines = ((FabricRenderState)context.levelState()).getData(LINES);
            if (lines == null) return;
            Vec3 camera = context.levelState().cameraRenderState.pos;
            for (Cable line : lines) OxygenCable.submit(context.poseStack(), context.submitNodeCollector(), line.start.subtract(camera), line.end.subtract(camera), Vec3.ZERO, line.oxygenated);
        });
    }
}
