package com.example.assistmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class AssistMod implements ClientModInitializer {
    public static final String MOD_ID = "assistmod";

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {
        Assist.load();

        openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.assistmod.open_menu",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_RSHIFT,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new AssistScreen());
                }
            }
            if (client.player != null && client.level != null) {
                AssistHandler.tick(client);
            }
        });
    }
}
