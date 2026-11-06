package io.wispforest.owo.mixin.braid;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.renderpearl.api.device.GpuBackend;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.BraidWindow;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.OptionalInt;

@Mixin(Window.class)
public class WindowMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @WrapMethod(method = "createWindow")
    private long makeWindowShared(GpuBackend backend, int width, int height, String title, Operation<Long> original) {
        if(!BraidWindow.SHARE_NEXT_WINDOW_INSTANCE.get())
            return original.call(backend, width, height, title);

        BraidWindow.SHARE_NEXT_WINDOW_INSTANCE.set(false);

        OptionalInt oldValue = getAttribute();

        setAttribute(1);
        final var handle = original.call(backend, width, height, title);
        LOGGER.warn("overrode value");
        oldValue.ifPresent(WindowMixin::setAttribute);
        return handle;
    }

    @Unique
    private static OptionalInt getAttribute() {
        try (final var stack = MemoryStack.stackPush()) {
            final var outBuf = stack.mallocInt(1);
            if(!SDLVideo.SDL_GL_GetAttribute(SDLVideo.SDL_GL_SHARE_WITH_CURRENT_CONTEXT, outBuf)) {
                Owo.LOGGER.warn("Failed to get GL share attribute: {}", SDLError.SDL_GetError());
                return OptionalInt.empty();
            }
            return OptionalInt.of(outBuf.get());
        }
    }

    @Unique
    private static void setAttribute(int value) {
        if(!SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_SHARE_WITH_CURRENT_CONTEXT, value)) {
            Owo.LOGGER.warn("Failed to set GL share attribute: {}", SDLError.SDL_GetError());
        }
    }
}
