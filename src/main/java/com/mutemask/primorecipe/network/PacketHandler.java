package com.mutemask.primorecipe.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class PacketHandler {
    
    public static void register() {
        // Register packets
        PayloadTypeRegistry.playC2S().register(
            OpenCraftingTablePacket.TYPE,
            OpenCraftingTablePacket.STREAM_CODEC
        );
        
        ServerPlayNetworking.registerGlobalReceiver(OpenCraftingTablePacket.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                // Handle packet on server
                if (player.containerMenu instanceof net.minecraft.world.inventory.CraftingMenu craftingMenu) {
                    // Pre-fill crafting grid
                    for (int i = 0; i < payload.ingredients().size() && i < 9; i++) {
                        craftingMenu.slots.get(i + 1).set(payload.ingredients().get(i));
                    }
                    craftingMenu.broadcastChanges();
                }
            });
        });
    }
}
