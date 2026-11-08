package io.wispforest.owo.mixin.braid;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.blaze3d.platform.SDLEventHandler;
import com.mojang.blaze3d.platform.Window;
import io.wispforest.owo.braid.core.BraidWindow;
import org.lwjgl.sdl.SDL_Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SDLEventHandler.class)
public class SDLEventHandlerMixin {
    @Shadow
    private static long getWindowHandle(SDL_Event event) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @ModifyReceiver(method = "pollEvents", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;handleEvent(Lorg/lwjgl/sdl/SDL_Event;)V"))
    private Window redirectToBraidWindow(Window window, SDL_Event event) {
        final var handle = getWindowHandle(event);
        final var braidWindow = BraidWindow.getWindow(handle);

        if (braidWindow == null)
            return window;

        return braidWindow;
    }
}
