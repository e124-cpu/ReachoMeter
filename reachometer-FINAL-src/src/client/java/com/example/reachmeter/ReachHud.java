package com.example.reachmeter;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;

/** Draws the counter. Used by both the real HUD and the placement screen. */
public class ReachHud {

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
            color = 0xFFFFFFFF;          // white: nothing recent
        } else if (shown <= 3.5) {
            color = 0xFF55FF55;          // green: normal
        } else if (shown <= 4.0) {
            color = 0xFFFFFF55;          // yellow: could be lag / movement
        } else {
            color = 0xFFFF5555;          // red: worth a closer look
        }
        graphics.drawString(font, line1(), x, y, color, true);
        graphics.drawString(font, line2(), x, y + 11, 0xFFAAAAAA, true);
    }
}
