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
    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        // Page Up opens the settings screen (rebindable in Controls)
        openSettingsKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.reachmeter.open_settings",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_PAGEUP,
                CATEGORY
            )
        );

        // Turns the counter on/off. Unbound by default (-1), pick a key in Controls.
        toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.reachmeter.toggle",
                InputConstants.Type.KEYSYM,
                -1,
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

            while (toggleKey.consumeClick()) {
                ReachSettings.enabled = !ReachSettings.enabled;
                ReachSettings.save();
                if (client.player != null) {
                    client.player.displayClientMessage(
                        Component.literal("Reachometer: " + (ReachSettings.enabled ? "ON" : "OFF")),
                        true
                    );
                }
            }
        });

        // The counter itself (hidden when switched off)
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("reachmeter", "reach_counter"),
            (graphics, deltaTracker) -> {
                if (!ReachSettings.enabled) return;
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof PlacementScreen) return;
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
