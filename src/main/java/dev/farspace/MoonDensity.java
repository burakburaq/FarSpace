package dev.farspace;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

/** Lunar ground plus three-dimensional stone arches, pillars and leaning shelves in biome B. */
public final class MoonDensity implements DensityFunction {
    public static final MoonDensity INSTANCE = new MoonDensity();
    public static final MapCodec<MoonDensity> CODEC = MapCodec.unit(INSTANCE);
    private record Column(double ground, double bWeight, double x, double z, double direction) {}
    private static Column column(int x, int z) {
        var r = MoonRegions.select(x, z);
        double w = r.firstWeight();
        double aw = (r.first() == 0 ? w : 0) + (r.second() == 0 ? 1 - w : 0);
        double bw = (r.first() == 1 ? w : 0) + (r.second() == 1 ? 1 - w : 0);
        double a = aw > 0 ? MoonHeight.height(x, z) : 38;
        double b = 38 + 7 * Math.sin(x / 82.0) * Math.cos(z / 91.0) + 2 * Math.sin((x + z) / 19.0);
        double ground = 38 + aw * (a - 38) + bw * (b - 38);
        int cx = Math.floorDiv(x, 192), cz = Math.floorDiv(z, 192);
        long h = (cx * 0x9E3779B97F4A7C15L) ^ (cz * 0xC2B2AE3D27D4EB4FL);
        h ^= h >>> 29; h *= 0xBF58476D1CE4E5B9L; h ^= h >>> 31;
        double localX = x - (cx * 192.0 + 96 + ((h & 31) - 15));
        double localZ = z - (cz * 192.0 + 96 + (((h >>> 8) & 31) - 15));
        return new Column(ground, bw, localX, localZ, ((h >>> 16) & 1) == 0 ? 1 : -1);
    }
    public static float surface(int x, int z) {
        Column c = column(x, z);
        return (float) (c.bWeight > 0.01 ? Math.max(c.ground, 90) : c.ground);
    }
    private static float density(Column c, int y) {
        double ground = c.ground - y;
        if (c.bWeight < 0.01 || y < 26 || y > 104) return (float)ground;
        double x = c.x * c.direction, z = c.z;
        // A thick vertical stone ring creates a genuine open passage through the arch.
        double ring = 7 - Math.hypot(Math.hypot(x, y - 58.0) - 23, z * 1.25);
        // A slanted pillar supports a broad, thin, asymmetrical overhang.
        double shiftedX = x + 29 - (y - 38) * 0.32;
        double pillar = Math.min(8 - Math.hypot(shiftedX, z + 2), Math.min(y - 32.0, 85.0 - y));
        double shelf = 1 - Math.sqrt(Math.pow((x - 4) / 42.0, 2) + Math.pow((z + 3) / 26.0, 2) + Math.pow((y - 83) / 6.0, 2));
        double structure = Math.max(ring, Math.max(pillar, shelf * 8));
        // Fade structures before a biome border rather than cutting them off abruptly.
        return (float)Math.max(ground, structure - (1 - c.bWeight) * 28);
    }
    private static final DensitySampler SAMPLER = new DensitySampler() {
        @Override public float sampleValue(SamplerContext context, int x, int y, int z) { return density(column(x, z), y); }
        @Override public void sampleVolume(SamplerContext context, DensityBuffer buffer, DensityVolume volume) {
            int index = 0;
            for (int z = 0; z < volume.sizeZ(); z++) for (int x = 0; x < volume.sizeX(); x++) {
                Column c = column(volume.blockX(x), volume.blockZ(z));
                for (int y = 0; y < volume.sizeY(); y++) buffer.set(index++, density(c, volume.blockY(y)));
            }
        }
    };
    @Override public DensitySampler compileSampler(CompileContext context) { return SAMPLER; }
    @Override public DensityFunction rewriteChildren(DfRewriteRule rule) { return this; }
    @Override public Interval range() { return Interval.INFINITE; }
    @Override public int domainAxes() { return ALL_AXES; }
    @Override public MapCodec<MoonDensity> codec() { return CODEC; }
}
