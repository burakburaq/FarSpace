package dev.farspace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class OxygenBlock {
    public static final double RANGE = 5.0;
    private static final Identifier ID = Identifier.fromNamespaceAndPath("farspace", "oxygen_block");
    private static final ResourceKey<Block> KEY = ResourceKey.create(Registries.BLOCK, ID);
    private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, ID);
    public static final Block BLOCK = Registry.register(BuiltInRegistries.BLOCK, KEY,
            new Block(BlockBehaviour.Properties.of().setId(KEY).strength(3.0f).sound(SoundType.METAL)));
    public static final Item ITEM = Registry.register(BuiltInRegistries.ITEM, ITEM_KEY,
            new BlockItem(BLOCK, new Item.Properties().setId(ITEM_KEY).useBlockDescriptionPrefix()));
    public static void initialize() {}
    /** Nearest loaded source within five blocks, measured from player feet to block center. */
    public static BlockPos find(Level level, double x, double y, double z) {
        double nearest = RANGE * RANGE;
        BlockPos result = null;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int bx = Mth.floor(x - RANGE); bx <= Mth.floor(x + RANGE); bx++)
            for (int bz = Mth.floor(z - RANGE); bz <= Mth.floor(z + RANGE); bz++) {
                if (!level.hasChunkAt(bx, bz)) continue;
                for (int by = Mth.floor(y - RANGE); by <= Mth.floor(y + RANGE); by++) {
                    double dx = bx + 0.5 - x, dy = by + 0.5 - y, dz = bz + 0.5 - z;
                    double dist = dx * dx + dy * dy + dz * dz;
                    if (dist > nearest) continue;
                    pos.set(bx, by, bz);
                    if (level.getBlockState(pos).is(BLOCK)) { result = pos.immutable(); nearest = dist; }
                }
            }
        return result;
    }
}

