package dev.farspace;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

/** Conservative surface estimate for chunk generation, including biome B's shelves. */
public final class MoonSurface implements DensityFunction {
    public static final MoonSurface INSTANCE = new MoonSurface();
    public static final MapCodec<MoonSurface> CODEC = MapCodec.unit(INSTANCE);
    private static final DensitySampler SAMPLER = new DensitySampler() {
        @Override public float sampleValue(SamplerContext context, int x, int y, int z) { return MoonDensity.surface(x, z); }
        @Override public void sampleVolume(SamplerContext context, DensityBuffer buffer, DensityVolume volume) {
            int index = 0;
            for (int z = 0; z < volume.sizeZ(); z++) for (int x = 0; x < volume.sizeX(); x++) {
                float value = MoonDensity.surface(volume.blockX(x), volume.blockZ(z));
                buffer.setRange(index, volume.sizeY(), value);
                index += volume.sizeY();
            }
        }
    };
    @Override public DensitySampler compileSampler(CompileContext context) { return SAMPLER; }
    @Override public DensityFunction rewriteChildren(DfRewriteRule rule) { return this; }
    @Override public Interval range() { return Interval.of(-120, 120); }
    @Override public int domainAxes() { return AXIS_X | AXIS_Z; }
    @Override public MapCodec<MoonSurface> codec() { return CODEC; }
}
