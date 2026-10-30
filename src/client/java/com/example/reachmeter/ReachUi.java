package com.example.reachmeter;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

class ReachSettings {
    private static final double DEFAULT_X = 0.01;
    private static final double DEFAULT_Y = 0.93;

    public static double xFrac = DEFAULT_X;
    public static double yFrac = DEFAULT_Y;
    public static boolean enabled = true;

    private static final Path FILE =
        FabricLoader.getInstance().getConfigDir().resolve("reachmeter-settings.txt");

    static {
        load();
    }

    public static void resetPosition() {
        xFrac = DEFAULT_X;
        yFrac = DEFAULT_Y;
        save();
    }

    private static void load() {
        try {
            if (!Files.exists(FILE)) return;
            List<String> lines = Files.readAllLines(FILE);
            for (String line : lines) {
                String[] parts = line.trim().split("=");
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                String value = parts[1].trim();
                if (key.equals("enabled")) {
                    enabled = Boolean.parseBoolean(value);
                    continue;
                }
                try {
                    double v = Double.parseDouble(value);
                    if (key.equals("x")) xFrac = clamp01(v);
                    if (key.equals("y")) yFrac = clamp01(v);
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, String.format(Locale.ROOT,
                "x=%.4f%ny=%.4f%nenabled=%s%n", xFrac, yFrac, enabled));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}

class ReachHud {

    public static String line1() {
        return String.format(Locale.ROOT, "Enemies reach: %.2f", ReachStats.displayed());
    }

    public static String line2() {
        return String.format(Locale.ROOT, "Avg %.2f | Max %.2f | Hits %d",
            ReachStats.average(), ReachStats.max, ReachStats.count);
    }

    public static int width(Font font) {
        return Math.max(font.width(line1()), font.width(line2()));
    }

    public static int height() {
        return 20;
    }

    public static int x(int guiWidth, Font font) {
        int x = (int) (ReachSettings.xFrac * guiWidth);
        return Math.max(0, Math.min(x, Math.max(0, guiWidth - width(font))));
    }

    public static int y(int guiHeight) {
        int y = (int) (ReachSettings.yFrac * guiHeight);
        return Math.max(0, Math.min(y, Math.max(0, guiHeight - height())));
    }

    public static void draw(GuiGraphics graphics, Font font, int x, int y) {
        double shown = ReachStats.displayed();
        int color;
        if (shown <= 0) {
            color = 0xFFFFFFFF;
        } else if (shown <= 3.5) {
            color = 0xFF55FF55;
        } else if (shown <= 4.0) {
            color = 0xFFFFFF55;
        } else {
            color = 0xFFFF5555;
        }
        graphics.drawString(font, line1(), x, y, color, true);
        graphics.drawString(font, line2(), x, y + 11, 0xFFAAAAAA, true);
    }
}

class ReachSettingsScreen extends Screen {
    private final Screen parent;

    public ReachSettingsScreen(Screen parent) {
        super(Component.literal("Reachometer Settings"));
        this.parent = parent;
    }

    private static Component toggleLabel() {
        return Component.literal("Counter: " + (ReachSettings.enabled ? "ON" : "OFF"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 4;

        // Setting 1: Placement
        this.addRenderableWidget(
            Button.builder(Component.literal("Placement"),
                    b -> this.minecraft.setScreen(new PlacementScreen(this)))
                .bounds(cx - 100, top, 200, 20)
                .build()
        );

        // Setting 2: turn the counter on or off
        this.addRenderableWidget(
            Button.builder(toggleLabel(), b -> {
                    ReachSettings.enabled = !ReachSettings.enabled;
                    ReachSettings.save();
                    b.setMessage(toggleLabel());
                })
                .bounds(cx - 100, top + 24, 200, 20)
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

class PlacementScreen extends Screen {
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
