package com.vnap.client;

import com.vnap.network.HurtEffectPayload;
import com.vnap.sound.SupplementalSoundCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public final class SupplementalSoundState {
	private SupplementalSoundState() {
	}

	public static void play(HurtEffectPayload payload) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) return;
		Entity entity = ClientEntityLookup.find(minecraft, payload.entityId());
		SoundEvent sound = SupplementalSoundCatalog.byId(payload.effectId());
		if (entity == null || sound == null) return;
		minecraft.getSoundManager().play(new EntityBoundSoundInstance(
			sound, SoundSource.NEUTRAL, 1.0F, 1.0F, entity, entity.getRandom().nextLong()
		));
	}
}
