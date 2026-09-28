package com.vnap.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderSoundMixin {
	@Inject(method = "getAmbientSound()Lnet/minecraft/sounds/SoundEvent;", at = @At("HEAD"), cancellable = true)
	private void vnap$removeVanillaAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
		cir.setReturnValue(SoundEvents.EMPTY);
	}

	@Inject(method = "getHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)Lnet/minecraft/sounds/SoundEvent;", at = @At("HEAD"), cancellable = true)
	private void vnap$removeVanillaHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
		cir.setReturnValue(SoundEvents.EMPTY);
	}

	@Inject(method = "getDeathSound()Lnet/minecraft/sounds/SoundEvent;", at = @At("HEAD"), cancellable = true)
	private void vnap$removeVanillaDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
		cir.setReturnValue(SoundEvents.EMPTY);
	}
}
