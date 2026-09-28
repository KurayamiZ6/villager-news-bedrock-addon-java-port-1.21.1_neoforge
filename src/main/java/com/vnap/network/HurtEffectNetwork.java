package com.vnap.network;

import com.vnap.sound.SupplementalSoundCatalog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public final class HurtEffectNetwork {
    private static final double TRACKING_RANGE_SQUARED = 96.0 * 96.0;
    private HurtEffectNetwork() {}

    public static void send(ServerLevel level, LivingEntity entity, boolean baby) {
        HurtEffectPayload payload = new HurtEffectPayload(entity.getUUID(), SupplementalSoundCatalog.chooseHurtEffect(baby));
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(entity) <= TRACKING_RANGE_SQUARED) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
