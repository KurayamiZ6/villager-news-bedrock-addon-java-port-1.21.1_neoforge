package com.vnap.client;

import com.vnap.VillagerNewsAddonPort;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import traben.entity_model_features.EMFAnimationApi;

import java.io.IOException;
import java.util.function.Supplier;

@EventBusSubscriber(modid = VillagerNewsAddonPort.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class VillagerNewsAddonPortClient {
	private VillagerNewsAddonPortClient() {
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			VillagerNewsClientSettings.load();
			try {
				DialogueAnimationState.load();
				registerFloat("vnap_speaking", DialogueAnimationState::speaking, "Whether the Villager News character is speaking");
				registerFloat("vnap_mouth_open", DialogueAnimationState::mouthOpen, "Current Villager News mouth opening");
				registerFloat("vnap_mouth_width", DialogueAnimationState::mouthWidth, "Current Villager News mouth width");
				registerFloat("vnap_mouth_closed", DialogueAnimationState::mouthClosed, "Current Villager News closed-mouth layer");
				registerFloat("vnap_has_nose", DialogueAnimationState::hasNose, "Villager News nose visibility");
				registerFloat("vnap_cosmetic_mayor_hat", () -> DialogueAnimationState.cosmetic(1), "Villager News mayor hat visibility");
				registerFloat("vnap_cosmetic_helmet", () -> DialogueAnimationState.cosmetic(2), "Villager News helmet visibility");
				registerFloat("vnap_cosmetic_microphone", () -> DialogueAnimationState.cosmetic(3), "Villager News microphone visibility");
				registerFloat("vnap_cosmetic_moustache", () -> DialogueAnimationState.cosmetic(4), "Villager News moustache visibility");
				for (String variable : DialogueAnimationState.animationVariables()) {
					registerFloat(variable, () -> DialogueAnimationState.transform(variable), "Synchronized Villager News dialogue transform");
				}
			} catch (IOException | RuntimeException exception) {
				throw new IllegalStateException("Could not load Villager News animations", exception);
			} catch (Exception exception) {
				throw new IllegalStateException("Could not register Villager News EMF animation variables", exception);
			}
			VillagerNewsAddonPort.LOGGER.info("Registered synchronized EMF facial and dialogue animations");
		});
	}

	@SubscribeEvent
	public static void addPackFinders(AddPackFindersEvent event) {
		if (event.getPackType() != PackType.CLIENT_RESOURCES) {
			return;
		}
		// Two always-active layers are required here. The filter pack is inserted
		// first at TOP, then the Villager News override is inserted at TOP again,
		// leaving the override above the filter. This means the filter blocks Fresh
		// Animations below it without filtering Villager News resources above it.
		event.addPackFinders(
			ResourceLocation.fromNamespaceAndPath(VillagerNewsAddonPort.MOD_ID, "resourcepacks/villager_news_fresh_filter"),
			PackType.CLIENT_RESOURCES,
			Component.literal("Villager News — Fresh Animations villager filter"),
			PackSource.BUILT_IN,
			true,
			Pack.Position.TOP
		);
		event.addPackFinders(
			ResourceLocation.fromNamespaceAndPath(VillagerNewsAddonPort.MOD_ID, "resourcepacks/villager_news_fresh_override"),
			PackType.CLIENT_RESOURCES,
			Component.literal("Villager News — Fresh Animations compatibility override"),
			PackSource.BUILT_IN,
			true,
			Pack.Position.TOP
		);
	}

	@SubscribeEvent
	public static void addLayers(EntityRenderersEvent.AddLayers event) {
		var renderer = event.getRenderer(EntityType.VILLAGER);
		if (renderer instanceof VillagerRenderer villagerRenderer) {
			villagerRenderer.addLayer(new VillagerNewsSignLayer(villagerRenderer));
		}
	}

	private static void registerFloat(String name, Supplier<Float> supplier, String description) throws Exception {
		EMFAnimationApi.registerSingletonAnimationVariable(VillagerNewsAddonPort.RESOURCE_NAMESPACE, name, description, supplier);
	}
}
