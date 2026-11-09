package dev.farspace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public final class TetherBlock extends Block implements EntityBlock {
    public static final BooleanProperty CONNECTABLE = BooleanProperty.create("connectable");
    private static final Identifier ID = Identifier.fromNamespaceAndPath("farspace", "tether");
    private static final ResourceKey<Block> KEY = ResourceKey.create(Registries.BLOCK, ID);
    private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, ID);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(2, 0, 2, 14, 2, 14), Block.box(6.5, 2, 6.5, 9.5, 20, 9.5), Block.box(5.5, 20, 5.5, 10.5, 24, 10.5));
    public static final TetherBlock BLOCK = Registry.register(BuiltInRegistries.BLOCK, KEY,
            new TetherBlock(BlockBehaviour.Properties.of().setId(KEY).strength(1.0f).sound(SoundType.METAL).noOcclusion().lightLevel(s -> s.getValue(CONNECTABLE) ? 8 : 0)));
    public static final Item ITEM = Registry.register(BuiltInRegistries.ITEM, ITEM_KEY,
            new BlockItem(BLOCK, new Item.Properties().setId(ITEM_KEY).useBlockDescriptionPrefix()));
    public static final BlockEntityType<TetherNode> TYPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ID,
            new BlockEntityType<>(TetherNode::new, Set.of(BLOCK)));
    public static void initialize() {}
    private TetherBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CONNECTABLE, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(CONNECTABLE); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TYPE) return null;
        return (world, pos, current, entity) -> {
            if (entity instanceof TetherNode node && world.getGameTime() % 10 == 0) updateConnectable((ServerLevel)world, node);
        };
    }
    private static void updateConnectable(ServerLevel level, TetherNode node) {
        OxygenNetwork.updateIndicators(level, node.getBlockPos());
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TetherNode(pos, state); }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!level.isClientSide() && !old.is(state.getBlock())) level.scheduleTick(pos, this, 1);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof TetherNode node) connect(level, node);
    }
    public static void connect(ServerLevel level, TetherNode node) {
        node.prune(level);
        List<TetherNode> candidates = nearby(level, node.getBlockPos(), 34);
        candidates.sort(Comparator.comparingDouble(other -> other.getBlockPos().distSqr(node.getBlockPos())));
        for (TetherNode other : candidates) {
            if (node.available(level) <= 0) break;
            if (other == node || node.links().contains(other.getBlockPos())) continue;
            other.prune(level);
            if (other.available(level) > 0) { node.add(other.getBlockPos()); other.add(node.getBlockPos()); }
        }
    }
    public static List<TetherNode> nearby(Level level, BlockPos center, int range) {
        List<TetherNode> out = new ArrayList<>();
        int minX = Math.floorDiv(center.getX() - range, 16), maxX = Math.floorDiv(center.getX() + range, 16);
        int minZ = Math.floorDiv(center.getZ() - range, 16), maxZ = Math.floorDiv(center.getZ() + range, 16);
        for (int cx = minX; cx <= maxX; cx++) for (int cz = minZ; cz <= maxZ; cz++) {
            if (!level.hasChunkAt(cx * 16, cz * 16)) continue;
            for (BlockEntity entity : level.getChunk(cx, cz).getBlockEntities().values()) {
                if (entity instanceof TetherNode node && node.getBlockPos().distSqr(center) <= (double)range * range) out.add(node);
            }
        }
        return out;
    }
}
