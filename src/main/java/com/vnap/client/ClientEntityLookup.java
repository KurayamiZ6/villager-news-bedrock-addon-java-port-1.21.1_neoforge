package com.vnap.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

/**
 * Resolves an entity from the UUID carried by a network payload.
 * Minecraft 1.21.1 exposes Level#getEntity(int), not the UUID overload that
 * was available to the newer port. The client only needs nearby entities,
 * so a bounded entity query is both compatible and sufficient for the
 * 96-block server tracking range used by Villager News.
 */
final class ClientEntityLookup {
    private static final double SEARCH_RADIUS = 128.0;

    private ClientEntityLookup() {
    }

    static Entity find(Minecraft minecraft, UUID uuid) {
        if (minecraft.level == null || minecraft.player == null) return null;
        return find(minecraft.level, minecraft.player.getBoundingBox().inflate(SEARCH_RADIUS), uuid);
    }

    static Entity find(Level level, AABB area, UUID uuid) {
        return level.getEntitiesOfClass(Entity.class, area, entity -> uuid.equals(entity.getUUID()))
            .stream()
            .findFirst()
            .orElse(null);
    }
}
