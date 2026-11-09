package dev.farspace;

/** Shared biome layout for the biome resolver and terrain sampler, in block coordinates. */
public final class MoonRegions {
    private static final int SIZE = 640;
    public record Selection(int first, int second, double firstWeight) {}
    public static Selection select(int x, int z) {
        int gx = Math.floorDiv(x, SIZE), gz = Math.floorDiv(z, SIZE);
        double nearest = Double.POSITIVE_INFINITY, next = Double.POSITIVE_INFINITY;
        int first = 0, second = 0;
        for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
            int cx = gx + dx, cz = gz + dz;
            long h = hash(cx, cz);
            double px = (cx + 0.25 + 0.5 * ((h & 65535L) / 65535.0)) * SIZE;
            double pz = (cz + 0.25 + 0.5 * (((h >>> 16) & 65535L) / 65535.0)) * SIZE;
            double dist = Math.hypot(x - px, z - pz);
            int biome = Math.floorMod(cx + 2 * cz, 5);
            if (dist < nearest) { next = nearest; second = first; nearest = dist; first = biome; }
            else if (dist < next) { next = dist; second = biome; }
        }
        double t = Math.max(0, Math.min(1, (next - nearest) / 96.0));
        return new Selection(first, second, 0.5 + 0.5 * t * t * (3 - 2 * t));
    }
    private static long hash(int x, int z) {
        long h = x * 0x9E3779B97F4A7C15L + z * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 30; h *= 0xBF58476D1CE4E5B9L; h ^= h >>> 27; h *= 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }
}
