package com.example.reachmeter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public class ReachMeterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // The little counter in the bottom-left corner
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("reachmeter", "reach_counter"),
            (graphics, deltaTracker) -> render(graphics)
        );

        // /reachreset clears the stats
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("reachreset").executes(ctx -> {
                ReachStats.reset();
                ctx.getSource().sendFeedback(Component.literal("Reach stats reset."));
                return 1;
            }))
        );
    }

    private static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();

        String line1;
        int color;
        if (ReachStats.count == 0) {
            line1 = "Enemies reach: --";
            color = 0xFFFFFFFF;
        } else {
            line1 = String.format(Locale.ROOT, "Enemies reach: %.2f", ReachStats.last);
            if (ReachStats.last <= 3.05) {
                color = 0xFF55FF55;      // green: normal
            } else if (ReachStats.last <= 3.5) {
                color = 0xFFFFFF55;      // yellow: borderline
            } else {
                color = 0xFFFF5555;      // red: suspicious
            }
        }
        int bottom = graphics.guiHeight();

        if (ReachStats.count > 0) {
            String line2 = String.format(Locale.ROOT, "Avg %.2f | Max %.2f | Hits %d",
                ReachStats.average(), ReachStats.max, ReachStats.count);
            graphics.drawString(mc.font, line2, 6, bottom - 12, 0xFFAAAAAA, true);
            graphics.drawString(mc.font, line1, 6, bottom - 24, color, true);
        } else {
            graphics.drawString(mc.font, line1, 6, bottom - 12, color, true);
        }
    }
}
