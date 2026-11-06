package io.wispforest.owo.ui.parsing;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.util.CommandOpenedScreen;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.system.MemoryUtil;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ConfigureHotReloadScreen extends BaseUIModelScreen<FlowLayout> implements CommandOpenedScreen {

    private final @Nullable Screen parent;

    private final Identifier modelId;
    private @Nullable Path reloadLocation;

    private LabelComponent fileNameLabel;

    public ConfigureHotReloadScreen(Identifier modelId, @Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(Owo.id("configure_hot_reload")));
        this.parent = parent;

        this.modelId = modelId;
        this.reloadLocation = UIModelLoader.getHotReloadPath(this.modelId);
    }

    private CompletableFuture<@Nullable Path> openFileDialog() {
        var future = new CompletableFuture<@Nullable Path>();
        SDLDialog.SDL_ShowOpenFileDialog(
                (_, filelist, _) -> {
                    if (filelist == MemoryUtil.NULL) {
                        future.completeExceptionally(new RuntimeException("Failed to open file dialog: " + SDLError.SDL_GetError()));
                        return;
                    }

                    var pathAddress = MemoryUtil.memGetAddress(filelist);
                    if (pathAddress == MemoryUtil.NULL) {
                        future.complete(null);
                        return;
                    }

                    future.complete(Path.of(MemoryUtil.memUTF8(pathAddress)));
                },
                0L,
                0L,
                null,
                (CharSequence) null,
                false
        );
        return future;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(LabelComponent.class, "ui-model-label").text(Component.translatable("text.owo.configure_hot_reload.model", this.modelId));
        this.fileNameLabel = rootComponent.childById(LabelComponent.class, "file-name-label");
        this.updateFileNameLabel();

        rootComponent.childById(ButtonComponent.class, "choose-button").onPress(button -> {
            openFileDialog()
                .thenAccept(path -> {
                    if (path != null) this.reloadLocation = path;
                })
                .whenComplete((_, e) -> {
                    if (e != null)
                        Owo.LOGGER.error("Failed to choose file", e);

                    this.updateFileNameLabel();
                });
        });

        rootComponent.childById(ButtonComponent.class, "save-button").onPress(button -> {
            UIModelLoader.setHotReloadPath(this.modelId, this.reloadLocation);
            this.onClose();
        });

        rootComponent.childById(LabelComponent.class, "close-label").mouseDown().subscribe((click, doubled) -> {
            UISounds.playInteractionSound();
            this.onClose();
            return true;
        });
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.parent);
    }

    private void updateFileNameLabel() {
        this.fileNameLabel.text(Component.translatable(
                "text.owo.configure_hot_reload.reload_from",
                this.reloadLocation == null ? Component.translatable("text.owo.configure_hot_reload.reload_from.unset") : this.reloadLocation
        ));
    }
}
