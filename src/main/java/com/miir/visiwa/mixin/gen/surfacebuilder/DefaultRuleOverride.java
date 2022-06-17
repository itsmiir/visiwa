/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.gen.surfacebuilder;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.VanillaSurfaceRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VanillaSurfaceRules.class)
public class DefaultRuleOverride {
    @Shadow @Final private static MaterialRules.MaterialRule GRASS_BLOCK;
    @Shadow @Final private static MaterialRules.MaterialRule DIRT;
    @Shadow @Final private static MaterialRules.MaterialRule SANDSTONE;
    @Shadow @Final private static MaterialRules.MaterialRule SAND;
    @Shadow @Final private static MaterialRules.MaterialRule STONE;
    @Shadow @Final private static MaterialRules.MaterialRule GRAVEL;
    @Shadow @Final private static MaterialRules.MaterialRule CALCITE;
    @Shadow @Final private static MaterialRules.MaterialRule POWDER_SNOW;
    @Shadow @Final private static MaterialRules.MaterialRule PACKED_ICE;
    @Shadow @Final private static MaterialRules.MaterialRule SNOW_BLOCK;
    @Shadow @Final private static MaterialRules.MaterialRule ICE;
    @Shadow @Final private static MaterialRules.MaterialRule MUD;
    @Shadow @Final private static MaterialRules.MaterialRule COARSE_DIRT;
    @Shadow @Final private static MaterialRules.MaterialRule PODZOL;
    @Shadow @Final private static MaterialRules.MaterialRule MYCELIUM;
    @Shadow @Final private static MaterialRules.MaterialRule WATER;
    @Shadow @Final private static MaterialRules.MaterialRule ORANGE_TERRACOTTA;
    @Shadow @Final private static MaterialRules.MaterialRule TERRACOTTA;
    @Shadow @Final private static MaterialRules.MaterialRule RED_SANDSTONE;
    @Shadow @Final private static MaterialRules.MaterialRule RED_SAND;
    @Shadow @Final private static MaterialRules.MaterialRule WHITE_TERRACOTTA;
    @Shadow @Final private static MaterialRules.MaterialRule AIR;
    @Shadow @Final private static MaterialRules.MaterialRule BEDROCK;
    @Shadow @Final private static MaterialRules.MaterialRule DEEPSLATE;

    /**
     * @author miir
     * @reason for testing
     */
    @Overwrite
    public static MaterialRules.MaterialRule createDefaultRule(boolean surface, boolean bedrockRoof, boolean bedrockFloor) {
        MaterialRules.MaterialCondition aboveY97 = MaterialRules.aboveY(YOffset.fixed(97), 2);
        MaterialRules.MaterialCondition aboveY256 = MaterialRules.aboveY(YOffset.fixed(256), 0);
        MaterialRules.MaterialCondition aboveY63PlusStoneDepth = MaterialRules.aboveYWithStoneDepth(YOffset.fixed(63), -1);
        MaterialRules.MaterialCondition aboveY74PlusStoneDepth = MaterialRules.aboveYWithStoneDepth(YOffset.fixed(74), 1);
        MaterialRules.MaterialCondition aboveY60 = MaterialRules.aboveY(YOffset.fixed(60), 0);
        MaterialRules.MaterialCondition aboveY62 = MaterialRules.aboveY(YOffset.fixed(62), 0);
        MaterialRules.MaterialCondition aboveY63 = MaterialRules.aboveY(YOffset.fixed(63), 0);
        MaterialRules.MaterialCondition atOrAboveSeaLevel = MaterialRules.water(-1, 0);
        MaterialRules.MaterialCondition aboveSealLevel = MaterialRules.water(0, 0);
        MaterialRules.MaterialCondition aboveSeaLevelMinus5PlusStoneDepth = MaterialRules.waterWithStoneDepth(-6, -1); // return true if surface >= water height - 6
        MaterialRules.MaterialCondition negativeRunDepth = MaterialRules.hole();
        MaterialRules.MaterialCondition isFrozenOcean = MaterialRules.biome(BiomeKeys.FROZEN_OCEAN, BiomeKeys.DEEP_FROZEN_OCEAN);
        MaterialRules.MaterialCondition isSteepSlope = MaterialRules.steepSlope();
        MaterialRules.MaterialCondition isWarmOceanBeachOrSnowyBeach = MaterialRules.biome(BiomeKeys.WARM_OCEAN, BiomeKeys.BEACH, BiomeKeys.SNOWY_BEACH);
        MaterialRules.MaterialCondition isDesert = MaterialRules.biome(BiomeKeys.DESERT);
        MaterialRules.MaterialCondition lowSurfaceNoise = MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, -0.909, -0.5454);
        MaterialRules.MaterialCondition midSurfaceNoise = MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, -0.1818, 0.1818);
        MaterialRules.MaterialCondition highSurfaceNoise = MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, 0.5454, 0.909);
        MaterialRules.MaterialRule GRASS_DIRT = MaterialRules.sequence(
                MaterialRules.condition(aboveSealLevel, GRASS_BLOCK), DIRT);
        MaterialRules.MaterialRule SANDSTONE_SAND = MaterialRules.sequence(
                MaterialRules.condition(
                        MaterialRules.STONE_DEPTH_CEILING, SANDSTONE), SAND);
        MaterialRules.MaterialRule STONE_GRAVEL = MaterialRules.sequence(
                MaterialRules.condition(
                        MaterialRules.STONE_DEPTH_CEILING, STONE), GRAVEL);

        MaterialRules.MaterialRule stonyPeaksStonyShoreWindsweptHillsBeachWarmOceanDripstoneCaves = MaterialRules.sequence(
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.STONY_PEAKS),
                        MaterialRules.sequence(
                                MaterialRules.condition(
                                        MaterialRules.noiseThreshold(NoiseParametersKeys.CALCITE,
                                -0.0125, 0.0125), CALCITE), STONE)),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.STONY_SHORE),
                        MaterialRules.sequence(
                                MaterialRules.condition(
                                        MaterialRules.noiseThreshold(NoiseParametersKeys.GRAVEL,
                                -0.05, 0.05), STONE_GRAVEL), STONE)),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.WINDSWEPT_HILLS),
                        MaterialRules.condition(surfaceNoiseThreshold(1.0), STONE)),
                MaterialRules.condition(isWarmOceanBeachOrSnowyBeach, SANDSTONE_SAND),
                MaterialRules.condition(isDesert, SANDSTONE_SAND),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.DRIPSTONE_CAVES), STONE));

        MaterialRules.MaterialRule powderSnowNarrowNoise = MaterialRules.condition(
                MaterialRules.noiseThreshold(NoiseParametersKeys.POWDER_SNOW, 0.45, 0.58),
                MaterialRules.condition(aboveSealLevel, POWDER_SNOW));

        MaterialRules.MaterialRule powderSnowWideNoise = MaterialRules.condition(
                MaterialRules.noiseThreshold(NoiseParametersKeys.POWDER_SNOW, 0.35, 0.6),
                MaterialRules.condition(aboveSealLevel, POWDER_SNOW));

        MaterialRules.MaterialRule rockyBiomesAndMangroveRule = MaterialRules.sequence(
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.FROZEN_PEAKS),
                        MaterialRules.sequence(
                                MaterialRules.condition(isSteepSlope, PACKED_ICE),
                                MaterialRules.condition(
                                        MaterialRules.noiseThreshold(NoiseParametersKeys.PACKED_ICE, -0.5, 0.2), PACKED_ICE),
                                MaterialRules.condition(
                                        MaterialRules.noiseThreshold(NoiseParametersKeys.ICE, -0.0625, 0.025), ICE),
                                MaterialRules.condition(aboveSealLevel, SNOW_BLOCK))),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.SNOWY_SLOPES),
                        MaterialRules.sequence(
                                MaterialRules.condition(isSteepSlope, STONE),
                                powderSnowNarrowNoise, MaterialRules.condition(aboveSealLevel, SNOW_BLOCK))),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.JAGGED_PEAKS), STONE),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.GROVE),
                        MaterialRules.sequence(
                                powderSnowNarrowNoise,
                                DIRT)),
                stonyPeaksStonyShoreWindsweptHillsBeachWarmOceanDripstoneCaves,
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.WINDSWEPT_SAVANNA),
                        MaterialRules.condition(surfaceNoiseThreshold(1.75), STONE)),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS),
                        MaterialRules.sequence(
                                MaterialRules.condition(surfaceNoiseThreshold(2.0), STONE_GRAVEL),
                                MaterialRules.condition(surfaceNoiseThreshold(1.0), STONE),
                                MaterialRules.condition(surfaceNoiseThreshold(-1.0), DIRT), STONE_GRAVEL)),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.MANGROVE_SWAMP), MUD), DIRT);

        MaterialRules.MaterialRule materialRule8 = MaterialRules.sequence(
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.FROZEN_PEAKS),
                        MaterialRules.sequence(
                                MaterialRules.condition(
//                                        if steep slope, then packed ice
                                        isSteepSlope,
                                        PACKED_ICE),
//                                else if packed ice noise threshold between 0 and .2, then packed ice
                                MaterialRules.condition(
                                        MaterialRules.noiseThreshold(NoiseParametersKeys.PACKED_ICE, 0.0, 0.2), PACKED_ICE),
//                                else if ice noise between 0 and .025, then ice
                                MaterialRules.condition(
                                        MaterialRules.noiseThreshold(NoiseParametersKeys.ICE, 0.0, 0.025), ICE),
//                                else if above sea level, then snow block
                                MaterialRules.condition(aboveSealLevel, SNOW_BLOCK))),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.SNOWY_SLOPES),
                        MaterialRules.sequence(
                                MaterialRules.condition(isSteepSlope, STONE),
                                powderSnowWideNoise, MaterialRules.condition(aboveSealLevel, SNOW_BLOCK))),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.JAGGED_PEAKS),
                        MaterialRules.sequence(
                                MaterialRules.condition(isSteepSlope, STONE),
                                MaterialRules.condition(aboveSealLevel, SNOW_BLOCK))),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.GROVE),
                        MaterialRules.sequence(
                                powderSnowWideNoise,
                                MaterialRules.condition(aboveSealLevel, SNOW_BLOCK))),
                stonyPeaksStonyShoreWindsweptHillsBeachWarmOceanDripstoneCaves, MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.WINDSWEPT_SAVANNA),
                        MaterialRules.sequence(
                                MaterialRules.condition(surfaceNoiseThreshold(1.75), STONE),
                                MaterialRules.condition(surfaceNoiseThreshold(-0.5), COARSE_DIRT))),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS),
                        MaterialRules.sequence(
                                MaterialRules.condition(surfaceNoiseThreshold(2.0), STONE_GRAVEL),
                                MaterialRules.condition(surfaceNoiseThreshold(1.0), STONE),
                                MaterialRules.condition(surfaceNoiseThreshold(-1.0), GRASS_DIRT), STONE_GRAVEL)),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.OLD_GROWTH_PINE_TAIGA, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA),
                        MaterialRules.sequence(
                                MaterialRules.condition(surfaceNoiseThreshold(1.75), COARSE_DIRT),
                                MaterialRules.condition(surfaceNoiseThreshold(-0.95), PODZOL))),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.ICE_SPIKES), MaterialRules.condition(aboveSealLevel, SNOW_BLOCK)),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.MANGROVE_SWAMP), MUD),
                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.MUSHROOM_FIELDS), MYCELIUM), GRASS_DIRT);


        MaterialRules.MaterialRule materialRule9 = MaterialRules.sequence(
                MaterialRules.condition(
//                        if above the deepslate layer
                        MaterialRules.STONE_DEPTH_FLOOR,
                        MaterialRules.sequence(
                                MaterialRules.condition(
//                                        if wooded badlands biome
                                        MaterialRules.biome(BiomeKeys.WOODED_BADLANDS),
                                        MaterialRules.condition(
//                                                if above Y = 97
                                                aboveY97,
                                                MaterialRules.sequence(
//                                                      if surface noise value is low
                                                        MaterialRules.condition(
                                                                lowSurfaceNoise,
//                                                                then place coarse dirt
                                                                COARSE_DIRT),
//                                                        elif surface noise is mid
                                                        MaterialRules.condition(
                                                                midSurfaceNoise,
//                                                                then place coarse dirt
                                                                COARSE_DIRT),
//                                                        elif surface noise is high
                                                        MaterialRules.condition(
                                                                highSurfaceNoise,
//                                                                then place coarse dirt
                                                                COARSE_DIRT),
//                                                        else use grass dirt surface builder (extreme values of surface noise)
                                                        GRASS_DIRT))),

                                MaterialRules.condition(
//                                        if swamp biome
                                        MaterialRules.biome(BiomeKeys.SWAMP),
//                                        then
                                        MaterialRules.condition(
//                                                if above y=62
                                                aboveY62,
//                                                then
                                                MaterialRules.condition(
//                                                        if not above y=63
                                                        MaterialRules.not(aboveY63),
//                                                        then
                                                        MaterialRules.condition(
//                                                                if swamp noise is positive
                                                                MaterialRules.noiseThreshold(
//                                                                        presumably place grass?
                                                                        NoiseParametersKeys.SURFACE_SWAMP, 0.0),
//                                                                else place water
                                                                WATER)))),

                                MaterialRules.condition(
//                                        if mangrove swamp
                                        MaterialRules.biome(BiomeKeys.MANGROVE_SWAMP),
                                        MaterialRules.condition(
                                                aboveY60,
                                                MaterialRules.condition(
                                                    MaterialRules.not(aboveY63),
                                                    MaterialRules.condition(
                                                            MaterialRules.noiseThreshold(
                                                                    NoiseParametersKeys.SURFACE_SWAMP, 0.0),
                                                            WATER)))))),

                MaterialRules.condition(
                        MaterialRules.biome(BiomeKeys.BADLANDS, BiomeKeys.ERODED_BADLANDS, BiomeKeys.WOODED_BADLANDS),
                        MaterialRules.sequence(
                                MaterialRules.condition(
                                        MaterialRules.STONE_DEPTH_FLOOR,
                                        MaterialRules.sequence(
                                                MaterialRules.condition(aboveY256, ORANGE_TERRACOTTA),
                                                MaterialRules.condition(aboveY74PlusStoneDepth,
                                                        MaterialRules.sequence(
                                                                MaterialRules.condition(lowSurfaceNoise, TERRACOTTA),
                                                                MaterialRules.condition(
                                                                        midSurfaceNoise, TERRACOTTA),
                                                                MaterialRules.condition(highSurfaceNoise, TERRACOTTA),
                                                                MaterialRules.terracottaBands())),
                                                MaterialRules.condition(atOrAboveSeaLevel,
                                                        MaterialRules.sequence(
                                                                MaterialRules.condition(
                                                                        MaterialRules.STONE_DEPTH_CEILING, RED_SANDSTONE),
                                                                RED_SAND)),
                                                MaterialRules.condition(
                                                        MaterialRules.not(negativeRunDepth), ORANGE_TERRACOTTA),
                                                MaterialRules.condition(aboveSeaLevelMinus5PlusStoneDepth, WHITE_TERRACOTTA), STONE_GRAVEL)),
                                MaterialRules.condition(aboveY63PlusStoneDepth,
                                        MaterialRules.sequence(
                                                MaterialRules.condition(aboveY63, MaterialRules.condition(
                                                        MaterialRules.not(aboveY74PlusStoneDepth), ORANGE_TERRACOTTA)),
                                                MaterialRules.terracottaBands())),
                                MaterialRules.condition(
                                        MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH,
                                        MaterialRules.condition(aboveSeaLevelMinus5PlusStoneDepth, WHITE_TERRACOTTA)))),
                MaterialRules.condition(
                        MaterialRules.STONE_DEPTH_FLOOR, MaterialRules.condition(
                        atOrAboveSeaLevel, MaterialRules.sequence(
                                MaterialRules.condition(
                                        isFrozenOcean,
                                        MaterialRules.condition(
                                                negativeRunDepth,
                                                MaterialRules.sequence(
                                                        MaterialRules.condition(aboveSealLevel, AIR),
                                                        MaterialRules.condition(
                                                                MaterialRules.temperature(), ICE), WATER))),
                                materialRule8))),
                MaterialRules.condition(
                        aboveSeaLevelMinus5PlusStoneDepth,
                        MaterialRules.sequence(
                                MaterialRules.condition(
                                        MaterialRules.STONE_DEPTH_FLOOR,
                                        MaterialRules.condition(
                                                isFrozenOcean,
                                                MaterialRules.condition(negativeRunDepth, WATER))),
                                MaterialRules.condition(
                                        MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH,
                                        rockyBiomesAndMangroveRule),
                                MaterialRules.condition(
                                        isWarmOceanBeachOrSnowyBeach,
                                        MaterialRules.condition(
                                                MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_6,
                                                SANDSTONE)),
                                MaterialRules.condition(
                                        isDesert,
                                        MaterialRules.condition(
                                                MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH_RANGE_30,
                                                SANDSTONE)))),
                MaterialRules.condition(
                        MaterialRules.STONE_DEPTH_FLOOR,
                        MaterialRules.sequence(
                                MaterialRules.condition(
                                        MaterialRules.biome(BiomeKeys.FROZEN_PEAKS, BiomeKeys.JAGGED_PEAKS),
                                        STONE),
                                MaterialRules.condition(
                                        MaterialRules.biome(BiomeKeys.WARM_OCEAN, BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN),
                                        SANDSTONE_SAND), STONE_GRAVEL)));

        ImmutableList.Builder builder = ImmutableList.builder();
        if (bedrockRoof) {
            builder.add(MaterialRules.condition(
                    MaterialRules.not(MaterialRules.verticalGradient("bedrock_roof", YOffset.belowTop(5), YOffset.getTop())), BEDROCK));
        }
        if (bedrockFloor) {
            builder.add(MaterialRules.condition(
                    MaterialRules.verticalGradient("bedrock_floor", YOffset.getBottom(), YOffset.aboveBottom(5)), BEDROCK));
        }
        MaterialRules.MaterialRule materialRule10 = MaterialRules.condition(
                MaterialRules.surface(), materialRule9);
        builder.add(surface ? materialRule10 : materialRule9);
        builder.add(MaterialRules.condition(
                MaterialRules.verticalGradient("deepslate", YOffset.fixed(0), YOffset.fixed(8)), DEEPSLATE));
        return MaterialRules.sequence((MaterialRules.MaterialRule[])builder.build().toArray(MaterialRules.MaterialRule[]::new));
    }
    private static MaterialRules.MaterialCondition surfaceNoiseThreshold(double min) {
        return MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, min / 8.25, Double.MAX_VALUE);
    }
}
