package com.example.reachmeter;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** Remembers where your hitbox was over the last second, so hits can be lag-compensated. */
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

    /** Your hitbox from the given number of ticks ago (or the fallback if there is no history yet). */
    public static AABB get(int ticksAgo, AABB fallback) {
        if (HISTORY.isEmpty()) return fallback;
        int index = Math.max(0, Math.min(ticksAgo, HISTORY.size() - 1));
        return HISTORY.get(index);
    }
}
