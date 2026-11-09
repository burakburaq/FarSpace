package dev.farspace;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.Set;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class OxygenNetwork {
    private static final AttachmentType<BlockPos> CONNECTION = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("farspace", "oxygen_connection"),
            b -> b.syncWith(BlockPos.STREAM_CODEC, AttachmentSyncPredicate.all()));
    public static void initialize() {}
    public static BlockPos connection(Player player) { return player.getAttached(CONNECTION); }
    public static boolean visuallyOxygenated(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(OxygenBlock.BLOCK) || (state.is(TetherBlock.BLOCK) && state.getValue(TetherBlock.CONNECTABLE));
    }
    public static Vec3 endpoint(Level level, BlockPos pos) {
        return new Vec3(pos.getX()+0.5, pos.getY() + (level.getBlockState(pos).is(TetherBlock.BLOCK) ? 1.3 : 0.5), pos.getZ()+0.5);
    }
    public static boolean update(ServerPlayer p, boolean suit) {
        if (!Oxygen.onMoon(p) || !p.isAlive() || p.isCreative() || p.isSpectator() || !suit) {
            if (connection(p) != null) p.removeAttached(CONNECTION);
            return false;
        }
        ServerLevel level = p.level();
        BlockPos prior = connection(p), selected = null;
        if (prior != null && level.hasChunkAt(prior) && level.getBlockState(prior).is(TetherBlock.BLOCK)
                && distanceSq(p, prior) <= 18 * 18) selected = prior;
        if (selected == null) {
            BlockPos direct = OxygenBlock.find(level, p.getX(), p.getY(), p.getZ());
            if (direct != null) selected = direct;
            else {
                double closest = 18 * 18;
                for (TetherNode node : TetherBlock.nearby(level, p.blockPosition(), 19)) {
                    double d = distanceSq(p, node.getBlockPos());
                    if (d <= closest) { closest = d; selected = node.getBlockPos(); }
                }
            }
        }
        if (selected == null) { if (prior != null) p.removeAttached(CONNECTION); return false; }
        if (!selected.equals(prior)) p.setAttached(CONNECTION, selected);
        return level.getBlockState(selected).is(OxygenBlock.BLOCK) || powered(level, selected);
    }
    private static double distanceSq(Player p, BlockPos pos) {
        return p.distanceToSqr(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
    }
    private static final WeakHashMap<ServerLevel, IndicatorBatch> INDICATORS = new WeakHashMap<>();
    private static final class IndicatorBatch { long tick = Long.MIN_VALUE; final Set<BlockPos> processed = new HashSet<>(); }
    /** Update every tether in a connected component once per indicator tick. */
    public static void updateIndicators(ServerLevel level, BlockPos start) {
        IndicatorBatch batch = INDICATORS.computeIfAbsent(level, ignored -> new IndicatorBatch());
        if (batch.tick != level.getGameTime()) { batch.tick = level.getGameTime(); batch.processed.clear(); }
        if (batch.processed.contains(start)) return;
        var pending = new ArrayDeque<BlockPos>();
        var component = new ArrayList<TetherNode>();
        boolean oxygen = false;
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            if (!batch.processed.add(pos)) continue;
            level.getChunk(Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16));
            if (!(level.getBlockEntity(pos) instanceof TetherNode node)) continue;
            component.add(node);
            oxygen |= node.oxygenSource() != null;
            for (BlockPos next : node.links()) if (!batch.processed.contains(next)) pending.add(next);
        }
        for (TetherNode node : component) {
            var state = node.getBlockState();
            if (state.getValue(TetherBlock.CONNECTABLE) != oxygen)
                level.setBlockAndUpdate(node.getBlockPos(), state.setValue(TetherBlock.CONNECTABLE, oxygen));
        }
    }
    public static boolean powered(ServerLevel level, BlockPos start) {
        var pending = new ArrayDeque<BlockPos>(); var visited = new HashSet<BlockPos>();
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            if (!visited.add(pos)) continue;
            level.getChunk(Math.floorDiv(pos.getX(),16), Math.floorDiv(pos.getZ(),16));
            if (!(level.getBlockEntity(pos) instanceof TetherNode node)) continue;
            if (node.oxygenSource() != null) return true;
            for (BlockPos next : node.links()) if (!visited.contains(next)) pending.add(next);
        }
        return false;
    }
}
