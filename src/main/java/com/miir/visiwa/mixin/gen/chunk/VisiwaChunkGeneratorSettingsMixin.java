/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.gen.chunk;

import com.miir.visiwa.world.gen.chunk.VisiwaChunkGeneratorSettings;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGeneratorSettings.class)
public class VisiwaChunkGeneratorSettingsMixin {
    @Invoker
    static RegistryEntry<ChunkGeneratorSettings> callRegister(Registry<ChunkGeneratorSettings> registry, RegistryKey<ChunkGeneratorSettings> key, ChunkGeneratorSettings chunkGeneratorSettings) {
        throw new UnsupportedOperationException();
    }
    @Invoker
    static ChunkGeneratorSettings callCreateSurfaceSettings(boolean amplified, boolean largeBiomes) {
        throw new UnsupportedOperationException();
    }

    @Inject(at = @At("HEAD"), method = "initAndGetDefault")
    private static void mixin(Registry<ChunkGeneratorSettings> registry, CallbackInfoReturnable<RegistryEntry<ChunkGeneratorSettings>> cir) {
        callRegister(registry, VisiwaChunkGeneratorSettings.ATLAS, callCreateSurfaceSettings(false, false));
    }

}
