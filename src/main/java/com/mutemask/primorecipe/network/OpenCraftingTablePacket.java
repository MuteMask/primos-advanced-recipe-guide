package com.mutemask.primorecipe.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record OpenCraftingTablePacket(List<ItemStack> ingredients) implements CustomPacketPayload {
    public static final Type<OpenCraftingTablePacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath("primorecipe", "open_crafting_table")
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCraftingTablePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC),
        OpenCraftingTablePacket::ingredients,
        OpenCraftingTablePacket::new
    );

    public OpenCraftingTablePacket(List<ItemStack> ingredients) {
        this.ingredients = new ArrayList<>(ingredients);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
