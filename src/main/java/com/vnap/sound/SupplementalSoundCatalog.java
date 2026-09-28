package com.vnap.sound;

import com.vnap.VillagerNewsAddonPort;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class SupplementalSoundCatalog {
	private static final List<String> ADULT_HURT_EFFECTS = List.of("a", "d", "g", "j", "l", "n", "r", "t", "v");
	private static final List<String> BABY_HURT_EFFECTS = List.of("b", "e", "h", "k", "m", "o", "s", "u");
	private static final List<String> EFFECTS = List.of(
		"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v"
	);
	private static final Map<String, SoundEvent> REGISTERED = new LinkedHashMap<>();
	private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, VillagerNewsAddonPort.RESOURCE_NAMESPACE);

	private SupplementalSoundCatalog() {
	}

	public static void register(IEventBus modEventBus) {
		for (String effect : EFFECTS) {
			ResourceLocation id = VillagerNewsAddonPort.id("effect." + effect);
			SoundEvent sound = SoundEvent.createVariableRangeEvent(id);
			REGISTERED.put(effect, sound);
			SOUNDS.register(id.getPath(), () -> sound);
		}
		SOUNDS.register(modEventBus);
	}

	public static String chooseHurtEffect(boolean baby) {
		List<String> effects = baby ? BABY_HURT_EFFECTS : ADULT_HURT_EFFECTS;
		return effects.get(ThreadLocalRandom.current().nextInt(effects.size()));
	}

	public static SoundEvent byId(String effect) {
		return REGISTERED.get(effect);
	}
}
