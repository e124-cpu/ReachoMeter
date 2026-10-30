package com.example.reachmeter;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ReachSettingsScreen extends Screen {
    private final Screen parent;

    public ReachSettingsScreen(Screen parent) {
        super(Component.literal("Reachometer Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 4;

        // Setting #1: Placement
        this.addRenderableWidget(
            Button.builder(Component.literal("Placement"),
                    b -> this.minecraft.setScreen(new PlacementScreen(this)))
                .bounds(cx - 100, top, 200, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(cx - 100, this.height - 40, 200, 20)
                .build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
