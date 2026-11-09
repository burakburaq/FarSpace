package dev.farspace;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

/** Continuous, seed-independent lunar height field sampled in world coordinates. */
public final class MoonHeight implements DensityFunction {
    public static final MoonHeight INSTANCE = new MoonHeight();
    public static final MapCodec<MoonHeight> CODEC = MapCodec.unit(INSTANCE);
    private static final DensitySampler SAMPLER = new DensitySampler() {
        @Override public float sampleValue(SamplerContext context, int x, int y, int z) { return height(x, z); }
        @Override public void sampleVolume(SamplerContext context, DensityBuffer buffer, DensityVolume volume) {
            int index = 0;
            for (int z = 0; z < volume.sizeZ(); z++) {
                for (int x = 0; x < volume.sizeX(); x++) {
                    float value = height(volume.blockX(x), volume.blockZ(z));
                    buffer.setRange(index, volume.sizeY(), value);
                    index += volume.sizeY();
                }
            }
        }
    };

    @Override public DensitySampler compileSampler(CompileContext context) { return SAMPLER; }
    @Override public DensityFunction rewriteChildren(DfRewriteRule rule) { return this; }
    @Override public Interval range() { return Interval.of(-120, 120); }
    @Override public int domainAxes() { return AXIS_X | AXIS_Z; }
    @Override public MapCodec<MoonHeight> codec() { return CODEC; }

    static float height(int x, int z) {
        double warpX = x + 25 * noise(x, z, 120, 11) + 8 * noise(x, z, 42, 12);
        double warpZ = z + 25 * noise(x, z, 120, 13) + 8 * noise(x, z, 42, 14);
        double broad = noise(x, z, 320, 15);
        double h = 38 + 5 * broad + 2.4 * noise(x, z, 61, 16)
                + 1.1 * noise(x, z, 18, 17) + 0.45 * noise(x, z, 6, 18);

        // A warped broad field forms isolated plateaus with steep, irregular shoulders.
        double mesa = noise(warpX, warpZ, 310, 21) + 0.22 * noise(warpX, warpZ, 120, 22);
        double plateau = smooth(0.43, 0.62, mesa);
        h += 38 * plateau + plateau * 1.5 * noise(x, z, 33, 23);

        // Cellular crater centers: warped coordinates break circular rims and overlap smoothly.
        int cellX = floor(warpX / 240), cellZ = floor(warpZ / 240);
        for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
            int cx = cellX + dx, cz = cellZ + dz;
            long bits = hash(cx, cz, 31);
            double px = (cx + 0.16 + 0.68 * unit(bits)) * 240;
            double pz = (cz + 0.16 + 0.68 * unit(bits >>> 16)) * 240;
            double radius = 34 + 43 * unit(bits >>> 32);
            double stretch = 0.82 + 0.36 * unit(bits >>> 48);
            double nx = (warpX - px) / (radius * stretch);
            double nz = (warpZ - pz) * stretch / radius;
            double d = Math.hypot(nx, nz);
            double bowl = 1 - smooth(0.12, 0.96, d);
            double rim = smooth(0.70, 0.94, d) * (1 - smooth(0.96, 1.30, d));
            h += -radius * 0.29 * bowl + radius * 0.095 * rim;
        }

        // Distance to the closest Voronoi point creates scattered rounded mounds.
        double nearest = 2;
        int vx = floor(warpX / 56), vz = floor(warpZ / 56);
        for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
            int cx = vx + dx, cz = vz + dz;
            long bits = hash(cx, cz, 41);
            double px = (cx + 0.12 + 0.76 * unit(bits)) * 56;
            double pz = (cz + 0.12 + 0.76 * unit(bits >>> 24)) * 56;
            nearest = Math.min(nearest, Math.hypot(warpX - px, warpZ - pz) / 56);
        }
        h += 5.5 * (1 - smooth(0.04, 0.42, nearest));

        // Narrow, meandering fractures have near-vertical sides and uneven depth.
        double fracture = Math.abs(noise(x, z, 265, 51) + 0.23 * noise(x, z, 86, 52));
        double rift = 1 - smooth(0.018, 0.065, fracture);
        h -= rift * (34 + 8 * noise(x, z, 97, 53));
        return (float) Math.max(-120, Math.min(120, h));
    }

    private static double noise(double x, double z, double scale, int salt) {
        double px = x / scale, pz = z / scale;
        int ix = floor(px), iz = floor(pz);
        double tx = fade(px - ix), tz = fade(pz - iz);
        double a = signed(hash(ix, iz, salt)), b = signed(hash(ix + 1, iz, salt));
        double c = signed(hash(ix, iz + 1, salt)), d = signed(hash(ix + 1, iz + 1, salt));
        return lerp(lerp(a, b, tx), lerp(c, d, tx), tz);
    }
    private static int floor(double value) { return (int) Math.floor(value); }
    private static double fade(double t) { return t * t * (3 - 2 * t); }
    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    private static double smooth(double a, double b, double value) {
        return fade(Math.max(0, Math.min(1, (value - a) / (b - a))));
    }
    private static double unit(long bits) { return (bits & 0xffffL) / 65535.0; }
    private static double signed(long bits) { return unit(bits) * 2 - 1; }
    private static long hash(int x, int z, int salt) {
        long h = x * 0x9E3779B97F4A7C15L + z * 0xC2B2AE3D27D4EB4FL + salt * 0x165667B19E3779F9L;
        h ^= h >>> 30; h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27; h *= 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }
}

