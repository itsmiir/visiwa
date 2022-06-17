/*
 * Copyright (c) 2022 miir
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.miir.visiwa.mixin.gen;

import com.miir.visiwa.Visiwa;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.tag.WorldPresetTags;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(MoreOptionsDialog.class)
public abstract class AddAtlasMixin {
    @Shadow public abstract GeneratorOptionsHolder getGeneratorOptionsHolder();
    @Inject(method = "init", at = @At("HEAD"))
    private void mixin(CallbackInfo ci) {
        List<RegistryEntry<WorldPreset>> normal = new ArrayList<>(List.copyOf(this.getGeneratorOptionsHolder().dynamicRegistryManager().get(Registry.WORLD_PRESET_KEY).getEntryList(WorldPresetTags.NORMAL).get().stream().toList()));
        List<RegistryEntry<WorldPreset>> extended = new ArrayList<>(List.copyOf(this.getGeneratorOptionsHolder().dynamicRegistryManager().get(Registry.WORLD_PRESET_KEY).getEntryList(WorldPresetTags.EXTENDED).get().stream().toList()));
        for (RegistryKey<WorldPreset> key :
                Visiwa.NEW_WORLD_TYPES) {
            RegistryEntry<WorldPreset> preset = this.getGeneratorOptionsHolder().dynamicRegistryManager().get(Registry.WORLD_PRESET_KEY).entryOf(key);
            normal.add(preset);
            extended.add(preset);
        }
        this.getGeneratorOptionsHolder().dynamicRegistryManager().get(Registry.WORLD_PRESET_KEY).populateTags(Map.of(WorldPresetTags.NORMAL, normal));
        this.getGeneratorOptionsHolder().dynamicRegistryManager().get(Registry.WORLD_PRESET_KEY).populateTags(Map.of(WorldPresetTags.EXTENDED, extended));
    }
}
