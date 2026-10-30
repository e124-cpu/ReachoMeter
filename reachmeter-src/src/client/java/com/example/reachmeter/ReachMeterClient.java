package com.example.reachmeter;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ReachMeterClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("reachmeter", "settings")
    );

    private static KeyMapping openSettingsKey;

    @Override
    public void onInitializeClient() {
        // Page Up opens the settings screen
        openSettingsKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.reachmeter.open_settings",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_PAGEUP,
                CATEGORY
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ReachHistory.tick(client);
            while (openSettingsKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ReachSettingsScreen(null));
                }
            }
        });

        // The counter itself
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("reachmeter", "reach_counter"),
            (graphics, deltaTracker) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof PlacementScreen) return; // the screen draws its own preview
                int x = ReachHud.x(graphics.guiWidth(), mc.font);
                int y = ReachHud.y(graphics.guiHeight());
                ReachHud.draw(graphics, mc.font, x, y);
            }
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
}
