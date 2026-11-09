package dev.farspace;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public final class OxygenCable {
    public static final RenderStateDataKey<Vec3> END = RenderStateDataKey.create(() -> "farspace:oxygen_cable_end");
    public static final RenderStateDataKey<Boolean> OXYGENATED = RenderStateDataKey.create(() -> "farspace:oxygen_cable_active");
    public static void submit(PoseStack pose, SubmitNodeCollector collector, Vec3 start, Vec3 end, Vec3 camera, boolean oxygenated) {
        Vec3 delta = end.subtract(start);
        collector.submitCustomGeometry(pose, RenderTypes.leash(), (matrix, vertices) -> {
            Matrix4fc transform = matrix.pose();
            for (int i = 0; i <= 24; i++) {
                double t = i / 24.0;
                double yy = delta.y > 0 ? delta.y * t * t : delta.y - delta.y * (1 - t) * (1 - t);
                Vec3 point = start.add(delta.x * t, yy - 0.64 * t * (1 - t), delta.z * t);
                double slope = delta.y > 0 ? 2 * delta.y * t : 2 * delta.y * (1 - t);
                Vec3 tangent = new Vec3(delta.x, slope - 0.64 + 1.28 * t, delta.z);
                Vec3 across = tangent.cross(camera.subtract(point));
                if (across.lengthSqr() < 1.0e-10) across = tangent.cross(new Vec3(0, 1, 0));
                if (across.lengthSqr() < 1.0e-10) across = new Vec3(1, 0, 0);
                across = across.normalize().scale(0.025);
                vertex(vertices, transform, point.subtract(across), i, oxygenated);
                vertex(vertices, transform, point.add(across), i, oxygenated);
            }
        });
    }
    private static void vertex(VertexConsumer out, Matrix4fc matrix, Vec3 point, int segment, boolean oxygenated) {
        int shade = segment % 2 == 0 ? 255 : 210;
        int gray = segment % 2 == 0 ? 65 : 50;
        out.addVertex(matrix, (float)point.x, (float)point.y, (float)point.z)
                .setColor(oxygenated ? 15 : gray, oxygenated ? shade : gray, oxygenated ? 255 : gray, 255)
                .setLight(oxygenated ? 0x00F000F0 : 0x00800080);
    }
}

