package io.wispforest.owo.ui.renderstate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import io.wispforest.owo.mixin.PictureInPictureRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public record CubeMapElementRenderState(
    CubeMap cubeMap,
    boolean rotate,
    ScreenRectangle bounds,
    ScreenRectangle scissorArea
) implements PictureInPictureRenderState {
    public static final ScopedValue<OutputOverride> OUTPUT_OVERRIDE = ScopedValue.newInstance();

    @Override
    public int x0() {
        return this.bounds.left();
    }

    @Override
    public int x1() {
        return this.bounds.right();
    }

    @Override
    public int y0() {
        return this.bounds.top();
    }

    @Override
    public int y1() {
        return this.bounds.bottom();
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return this.scissorArea != null ? this.scissorArea.intersection(this.bounds) : this.bounds;
    }

    public static class Renderer extends PictureInPictureRenderer<CubeMapElementRenderState> {
        private float spin;

        @Override
        public Class<CubeMapElementRenderState> getRenderStateClass() {
            return CubeMapElementRenderState.class;
        }

        @Override
        protected void renderToTexture(CubeMapElementRenderState state, PoseStack matrices, SubmitNodeCollector collector) {

            final var accessor = (PictureInPictureRendererAccessor) this;

            // TODO: we should probably investigate syncing this to the actual panorama
            //  rotation at some point

            Minecraft minecraft = Minecraft.getInstance();
            if (state.rotate()) {
                float a = minecraft.getDeltaTracker().getRealtimeDeltaTicks();
                float delta = (float) (a * minecraft.gameRenderer.gameRenderState().optionsRenderState.panoramaSpeed);
                this.spin = Mth.wrapDegrees(this.spin + delta * 0.1F);
            }

            ScopedValue.where(OUTPUT_OVERRIDE, new OutputOverride(
                    accessor.getTextureView(),
                    accessor.getDepthTextureView(),
                    new Vector4f(1, 0, 0, 0)
            )).run(() -> state.cubeMap.render(10.0F, this.spin));
        }

        @Override
        protected String getTextureLabel() {
            return "owo-ui_cubemap";
        }
    }

    public record OutputOverride(GpuTextureView color, GpuTextureView depth, Vector4fc resetColor) {}
}
