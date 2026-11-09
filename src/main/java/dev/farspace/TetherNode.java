package dev.farspace;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class TetherNode extends BlockEntity {
    private final List<BlockPos> links = new ArrayList<>(2);
    public TetherNode(BlockPos pos, BlockState state) { super(TetherBlock.TYPE, pos, state); }
    public List<BlockPos> links() { return List.copyOf(links); }
    public BlockPos oxygenSource() {
        if (level == null || links.size() >= 2) return null;
        if (level instanceof ServerLevel server) {
            int minX = Math.floorDiv(worldPosition.getX() - 5, 16), maxX = Math.floorDiv(worldPosition.getX() + 5, 16);
            int minZ = Math.floorDiv(worldPosition.getZ() - 5, 16), maxZ = Math.floorDiv(worldPosition.getZ() + 5, 16);
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) server.getChunk(x, z);
        }
        return OxygenBlock.find(level, worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5);
    }
    public int available(ServerLevel world) { return Math.max(0, 2 - links.size() - (oxygenSource() != null ? 1 : 0)); }
    public void add(BlockPos pos) { if (!links.contains(pos) && links.size() < 2) { links.add(pos.immutable()); changed(); } }
    public void remove(BlockPos pos) { if (links.remove(pos)) changed(); }
    public void prune(ServerLevel world) {
        boolean removed = links.removeIf(pos -> {
            world.getChunk(Math.floorDiv(pos.getX(),16), Math.floorDiv(pos.getZ(),16));
            return !(world.getBlockEntity(pos) instanceof TetherNode other) || !other.links.contains(worldPosition);
        });
        if (removed) changed();
    }
    private void changed() {
        setChanged();
        if (level instanceof ServerLevel server) server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel server) for (BlockPos neighbor : List.copyOf(links)) {
            server.getChunk(Math.floorDiv(neighbor.getX(),16), Math.floorDiv(neighbor.getZ(),16));
            if (server.getBlockEntity(neighbor) instanceof TetherNode other) { other.remove(worldPosition); server.scheduleTick(neighbor, TetherBlock.BLOCK, 1); }
        }
        super.preRemoveSideEffects(pos, state);
    }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.store("links", BlockPos.CODEC.listOf(), links); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); links.clear(); links.addAll(in.read("links", BlockPos.CODEC.listOf()).orElse(List.of())); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup) { return saveWithoutMetadata(lookup); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
