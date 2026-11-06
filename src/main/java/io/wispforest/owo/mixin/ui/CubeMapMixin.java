package io.wispforest.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import io.wispforest.owo.ui.renderstate.CubeMapElementRenderState;
import net.minecraft.client.renderer.CubeMap;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

@Mixin(CubeMap.class)
public class CubeMapMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/renderpearl/api/commands/RenderPass;"))
    private RenderPass injectOutputTextures(CommandEncoder instance, Supplier<String> label, GpuTextureView colorTexture, Optional<Vector4fc> clearColor, @Nullable GpuTextureView depthTexture, OptionalDouble clearDepth, Operation<RenderPass> original) {
        if (!CubeMapElementRenderState.OUTPUT_OVERRIDE.isBound())
            return original.call(instance, label, colorTexture, clearColor, depthTexture, clearDepth);


        final var override = CubeMapElementRenderState.OUTPUT_OVERRIDE.get();

        return original.call(
                instance,
                label,
                override.color(),
                Optional.of(override.resetColor()),
                override.depth(),
                clearDepth
        );
    }

}
