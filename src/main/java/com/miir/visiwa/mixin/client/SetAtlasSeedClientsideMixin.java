/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.client;

import com.miir.visiwa.Visiwa;
import com.miir.visiwa.world.gen.Atlas;
import com.miir.visiwa.world.gen.AtlasHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class SetAtlasSeedClientsideMixin {
    @Inject(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;disconnect()V",
                    shift = At.Shift.AFTER))
    private void mixin(String levelName, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, CallbackInfo ci) throws IOException {
        long seed = saveLoader.saveProperties().getGeneratorOptions().getSeed();
        Visiwa.LOGGER.info("set seed");
        Visiwa.SEED = seed;
        Visiwa.ATLAS = new Atlas(seed);
        Visiwa.ATLAS.draw();
        Visiwa.ATLAS.printAll("output");
    }
}
