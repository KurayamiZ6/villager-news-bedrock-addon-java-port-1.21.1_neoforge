package com.vnap.client;

import com.vnap.VillagerNewsAddonPort;
import com.vnap.item.VillagerNewsItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = VillagerNewsAddonPort.MOD_ID, value = Dist.CLIENT)
public final class VillagerNewsClientEvents {
	private VillagerNewsClientEvents() {
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		DialogueSoundState.tick(minecraft);
		DialogueAnimationState.tick(minecraft);
		DialogueSubtitleState.tick(minecraft);
	}

	@SubscribeEvent
	public static void onRenderGui(RenderGuiEvent.Post event) {
		DialogueSubtitleState.render(event.getGuiGraphics(), event.getPartialTick());
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (!event.getLevel().isClientSide()) return;
		if (!event.getItemStack().is(VillagerNewsItems.HANDBOOK.get())) return;
		Minecraft.getInstance().setScreen(new HandbookScreen());
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		Minecraft minecraft = Minecraft.getInstance();
		DialogueSoundState.clear(minecraft);
		DialogueAnimationState.clear();
		DialogueSubtitleState.clear();
		VillagerNewsSettingsState.reset();
	}
}
