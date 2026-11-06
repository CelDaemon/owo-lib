package io.wispforest.owo.mixin.braid;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {
    @Accessor
    static GpuBackend getBACKEND() {
        throw new AssertionError("Implemented via mixin");
    }
}
