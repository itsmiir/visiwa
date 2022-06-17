/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa;

import com.miir.elixir.Elixir;
import com.miir.visiwa.world.gen.atlas.Atlas;
import com.miir.visiwa.world.gen.atlas.AtlasHelper;
import com.miir.visiwa.world.gen.chunk.VisiwaChunkGenerator;
import com.miir.visiwa.world.gen.VisiwaGen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.WorldPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Visiwa implements ModInitializer {
    /*
    todo:
        fix surfacebuilder (one layer of grass generates in the default rule)
        fix the giant patch of stone issue
        create more height variation in terrain (implement in AtlasSubSampler::getSurfaceNoise or Atlas::buildMountains)
        perhaps implement more noise layers for local height? (pixel elevation modulates subpixel biome noise modulates a few more layers modulates the heightmap)
        move away from the heightmap to prepare for 3D biomes and features
        add custom biome tags
        change overwrites to more compat-friendly mixin type (conditional redirect?)
        regional noise variations (mountainous regions are more spiky, plains and beaches are more flat)
        better algorithms for temp, humidity, weirdness
        point-of-interest generation
            -rivers
                -completely redo the river biome
                -regional river heights (watersheds)
                -river vector logic (follows slopes down to a body of water)
                -river delta biomes
                -waterfalls (smooth river y-interpolation will not really look good with minecraft's blocky rivers)
                -advanced river biome surface builders, carvers, and decorators
                    -river bottom material varies depending on local climate, geology (if a cave river, floor should be stone/gravel)
                    -carvers should carve through mountains in certain scenarios
                    -if river is underground, add vines, glowberries, crystals, etc
            -lakes (local sea level)
            -canyon/rift systems
            -cliffs
            -fjords
            -mountain chains
        **probable first release here**
        overhangs, 3D biome distributions (sea cliff overhangs into the sea, underneath is a cove biome or something
        new biomes
            -bush, lake, canyon, deep sea trench, ice sheet, mushroom cave, lava tubes, mossy forest, crater oasis, oasis,
            geothermal pool, mesa, shallow (1 block deep) lake, river valley, tundra (see screenshots), painted mountains, bayou, redwood forest,
            tide pools, alpine slopes, lush desert, pink forest, lava field
            -exotic biomes?
                -amethyst rift, jungle pillars, oceanic sinkhole, infernal leak, haunted woods, underground jungle, frozen cave,
                lush rift, ancient forest, glowing cavern, glassed desert, impact crater, The Highlands, The Lowlands, frozen desert,
                Terraces,
        more large-scale terrain features (river deltas, archipelagos, island chains)
        **1.0**
        new structures?
            -cave river villages
    */

    public static final String ID = "visiwa";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final boolean DEBUG_BIOME = false;


    public static boolean isAtlas = true;
    public static List<RegistryEntry<Biome>> BIOMES;
    public static int SEA_LEVEL;
    public static long SEED = 0L;

    public static ArrayList<RegistryKey<WorldPreset>> NEW_WORLD_TYPES = new ArrayList<>();
    //    public static final Random random = new Random();


    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }
    public static Atlas ATLAS = new Atlas(0, 2, false, VisiwaConfig.WIDTH, VisiwaConfig.HEIGHT);
    @Override
    public void onInitialize() {
        addPreset();
        register();
        Elixir.addPresets(NEW_WORLD_TYPES);
    }

    private void addPreset() {
        NEW_WORLD_TYPES.add(VisiwaGen.ATLAS);
    }

    private void register() {
//        Registry.register(Registry.BIOME_SOURCE, Visiwa.id("atlas"), VisiwaBiomeSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, Visiwa.id("atlas"), VisiwaChunkGenerator.CODEC);
    }

}
