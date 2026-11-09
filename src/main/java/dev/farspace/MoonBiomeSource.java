package dev.farspace;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public final class MoonBiomeSource extends BiomeSource {
    public static final MapCodec<MoonBiomeSource> CODEC = Biome.CODEC.listOf().fieldOf("biomes")
            .xmap(MoonBiomeSource::new, source -> source.biomes);
    private final List<Holder<Biome>> biomes;
    public MoonBiomeSource(List<Holder<Biome>> biomes) {
        if (biomes.size() != 5) throw new IllegalArgumentException("Moon needs exactly five biomes");
        this.biomes = List.copyOf(biomes);
    }
    @Override protected MapCodec<? extends BiomeSource> codec() { return CODEC; }
    @Override protected Stream<Holder<Biome>> collectPossibleBiomes() { return biomes.stream(); }
    @Override public BiomeResolver createResolver(Climate.Sampler sampler) {
        return (x, y, z) -> biomes.get(MoonRegions.select(QuartPos.toBlock(x), QuartPos.toBlock(z)).first());
    }
}
