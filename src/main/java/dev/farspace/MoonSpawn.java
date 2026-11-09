package dev.farspace;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.Set;

public final class MoonSpawn {
    private static final AttachmentType<Boolean> ARRIVED = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath("farspace", "initial_moon_spawn"), Codec.BOOL);
    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var key = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("farspace", "moon"));
            var moon = server.getLevel(key);
            if (moon == null) return;
            for (var player : server.getPlayerList().getPlayers()) {
                if (player.getAttachedOrElse(ARRIVED, false)) continue;
                // A full chunk has a populated heightmap; an ungenerated heightmap can return the floor.
                var chunk = moon.getChunk(0, 0);
                int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0) + 1;
                if (player.teleportTo(moon, 0.5, y, 0.5, Set.of(), player.getYRot(), player.getXRot(), false)) {
                    player.setAttached(ARRIVED, true);
                }
            }
        });
    }
}
