/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.server;

import com.miir.visiwa.Visiwa;
import com.miir.visiwa.world.gen.atlas.Atlas;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Mixin(MinecraftServer.class)
public class GetSeedMixin {
    @Inject(method = "startServer", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static <S extends MinecraftServer> void mixin(Function<Thread, S> serverFactory, CallbackInfoReturnable<S> cir, AtomicReference ref, Thread thread, MinecraftServer server) throws IOException {
        GeneratorOptions options = server.getSaveProperties().getGeneratorOptions();
        long seed = options.getSeed();
        Visiwa.LOGGER.info("setting atlas for current world...");
        Visiwa.SEED = seed;
        Visiwa.ATLAS = new Atlas(seed);
        Visiwa.ATLAS.draw();
        if (Visiwa.DEV_ENV) {
            Visiwa.ATLAS.printAll("output");
        }
        Visiwa.BIOMES = options.getChunkGenerator().getBiomeSource().getBiomes().stream().toList();
        Visiwa.SEA_LEVEL = options.getChunkGenerator().getSeaLevel();
        Visiwa.LOGGER.info("done!");
    }
}
