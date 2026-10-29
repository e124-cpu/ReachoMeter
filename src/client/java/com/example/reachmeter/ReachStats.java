package com.example.reachmeter;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.Locale;

public class ReachStats {
    public static double last = 0;
    public static double max = 0;
    public static double sum = 0;
    public static int count = 0;

    private static final Path LOG =
        FabricLoader.getInstance().getConfigDir().resolve("reachmeter-log.txt");

    public static void record(String attackerName, double distance) {
        last = distance;
        sum += distance;
        count++;
        if (distance > max) max = distance;

        String line = String.format(Locale.ROOT, "%s | %s | %.2f blocks%n",
            LocalTime.now().withNano(0), attackerName, distance);
        try {
            Files.writeString(LOG, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double average() {
        return count == 0 ? 0 : sum / count;
    }

    public static void reset() {
        last = 0;
        max = 0;
        sum = 0;
        count = 0;
    }
}
