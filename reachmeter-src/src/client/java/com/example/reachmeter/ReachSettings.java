package com.example.reachmeter;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class ReachSettings {
    private static final double DEFAULT_X = 0.01;
    private static final double DEFAULT_Y = 0.93;

    // Position of the counter as a fraction of the screen (0.0 to 1.0)
    public static double xFrac = DEFAULT_X;
    public static double yFrac = DEFAULT_Y;

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
                try {
                    double v = Double.parseDouble(parts[1].trim());
                    if (parts[0].trim().equals("x")) xFrac = clamp01(v);
                    if (parts[0].trim().equals("y")) yFrac = clamp01(v);
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, String.format(Locale.ROOT, "x=%.4f%ny=%.4f%n", xFrac, yFrac));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
