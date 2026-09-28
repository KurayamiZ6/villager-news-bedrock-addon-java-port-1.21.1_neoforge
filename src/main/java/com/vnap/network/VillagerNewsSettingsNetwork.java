package com.vnap.network;

import com.vnap.config.VillagerNewsSettings;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class VillagerNewsSettingsNetwork {
    private VillagerNewsSettingsNetwork() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(VillagerNewsSettingsNetwork::onPlayerLogin);
    }

    public static void handleServerPayload(VillagerNewsSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!canEdit(player)) {
                send(player);
                return;
            }
            VillagerNewsSettings.update(payload.chattiness(), payload.rareVoicelines(), payload.spawnSpecialVillagers());
            send(player);
        });
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) send(player);
    }

    public static void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new VillagerNewsSettingsPayload(
            VillagerNewsSettings.chattiness(),
            VillagerNewsSettings.rareVoicelines(),
            VillagerNewsSettings.spawnSpecialVillagers(),
            canEdit(player)
        ));
    }

    private static boolean canEdit(ServerPlayer player) {
        return player.getServer() != null && player.getServer().isSingleplayerOwner(player.getGameProfile())
            || player.hasPermissions(2);
    }
}
