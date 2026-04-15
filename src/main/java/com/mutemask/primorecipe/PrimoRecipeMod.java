package com.mutemask.primorecipe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mutemask.primorecipe.network.OpenCraftingTablePacket;

public class PrimoRecipeMod implements ModInitializer {
    public static final String MOD_ID = "primorecipe";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Primo's Advanced Recipe Guide");
        
        PayloadTypeRegistry.playC2S().register(
            OpenCraftingTablePacket.TYPE,
            OpenCraftingTablePacket.STREAM_CODEC
        );
        
        ServerPlayNetworking.registerGlobalReceiver(OpenCraftingTablePacket.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                if (player.containerMenu instanceof CraftingMenu craftingMenu) {
                    for (int i = 0; i < payload.ingredients().size() && i < 9; i++) {
                        craftingMenu.slots.get(i + 1).set(payload.ingredients().get(i));
                    }
                    craftingMenu.broadcastChanges();
                }
            });
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
