package com.example.reachmeter;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class ReachHistory {
    private static final int MAX_TICKS = 20;
    private static final List<AABB> HISTORY = new ArrayList<>();

    public static void tick(Minecraft mc) {
        if (mc.player == null) {
            HISTORY.clear();
            return;
        }
        HISTORY.add(0, mc.player.getBoundingBox());
        while (HISTORY.size() > MAX_TICKS) {
            HISTORY.remove(HISTORY.size() - 1);
        }
    }

    public static AABB get(int ticksAgo, AABB fallback) {
        if (HISTORY.isEmpty()) return fallback;
        int index = Math.max(0, Math.min(ticksAgo, HISTORY.size() - 1));
        return HISTORY.get(index);
    }
}
