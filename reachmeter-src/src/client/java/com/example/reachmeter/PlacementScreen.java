package com.example.reachmeter;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class PlacementScreen extends Screen {
    private final Screen parent;
    private boolean dragging = false;
    private int grabDx = 0;
    private int grabDy = 0;

    public PlacementScreen(Screen parent) {
        super(Component.literal("Placement"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        this.addRenderableWidget(
            Button.builder(Component.literal("Reset position"), b -> ReachSettings.resetPosition())
                .bounds(cx - 102, this.height - 40, 100, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(cx + 2, this.height - 40, 100, 20)
                .build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, "Drag the counter where you want it",
            this.width / 2, 20, 0xFFFFFFFF);

        int x = ReachHud.x(this.width, this.font);
        int y = ReachHud.y(this.height);
        int w = ReachHud.width(this.font);
        int h = ReachHud.height();

        // Box around the counter so you can see what you are dragging
        int border = this.dragging ? 0xFFFFFF55 : 0xFFFFFFFF;
        graphics.fill(x - 3, y - 3, x + w + 3, y + h + 3, 0x66000000);
        graphics.fill(x - 3, y - 3, x + w + 3, y - 2, border);
        graphics.fill(x - 3, y + h + 2, x + w + 3, y + h + 3, border);
        graphics.fill(x - 3, y - 3, x - 2, y + h + 3, border);
        graphics.fill(x + w + 2, y - 3, x + w + 3, y + h + 3, border);

        ReachHud.draw(graphics, this.font, x, y);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        int x = ReachHud.x(this.width, this.font);
        int y = ReachHud.y(this.height);
        int w = ReachHud.width(this.font);
        int h = ReachHud.height();
        double mx = event.x();
        double my = event.y();

        if (mx >= x - 3 && mx <= x + w + 3 && my >= y - 3 && my <= y + h + 3) {
            this.dragging = true;
            this.grabDx = (int) (mx - x);
            this.grabDy = (int) (my - y);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.dragging) {
            ReachSettings.xFrac = ReachSettings.clamp01((event.x() - this.grabDx) / (double) this.width);
            ReachSettings.yFrac = ReachSettings.clamp01((event.y() - this.grabDy) / (double) this.height);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragging) {
            this.dragging = false;
            ReachSettings.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        ReachSettings.save();
        this.minecraft.setScreen(this.parent);
    }
}
