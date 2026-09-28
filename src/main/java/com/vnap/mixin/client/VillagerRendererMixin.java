package com.vnap.mixin.client;

import com.vnap.client.DialogueAnimationState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerRenderer.class)
public abstract class VillagerRendererMixin {
	@Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/npc/Villager;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"))
	private void vnap$trackBodyRotation(Villager villager, CallbackInfoReturnable<ResourceLocation> cir) {
		Minecraft minecraft = Minecraft.getInstance();
		float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
		float bodyRotation = Mth.rotLerp(partialTick, villager.yBodyRotO, villager.yBodyRot);
		DialogueAnimationState.trackBodyRotation(villager, bodyRotation, villager.tickCount + partialTick);
	}
}
