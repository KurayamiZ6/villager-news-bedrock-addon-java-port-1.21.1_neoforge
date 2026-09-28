package com.vnap.mixin;

import com.vnap.dialogue.ContextualDialogueController;
import com.vnap.entity.VillagerNewsData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.nbt.NbtOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerDataMixin implements VillagerNewsData {
	@Unique
	private static final EntityDataAccessor<Boolean> VNAP_HAS_NOSE = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.BOOLEAN);
	@Unique
	private static final EntityDataAccessor<Integer> VNAP_COSMETIC = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.INT);
	@Unique
	private static final EntityDataAccessor<Integer> VNAP_SIGN_MESSAGE = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.INT);
	@Unique
	private static final EntityDataAccessor<Integer> VNAP_SIGN_TYPE = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.INT);
	@Unique
	private VillagerData vnap$originalVillagerData;
	@Unique
	private MerchantOffers vnap$originalVillagerOffers;

	@Inject(method = "defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V", at = @At("TAIL"))
	private void vnap$defineData(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(VNAP_HAS_NOSE, true);
		builder.define(VNAP_COSMETIC, 0);
		builder.define(VNAP_SIGN_MESSAGE, -1);
		builder.define(VNAP_SIGN_TYPE, -1);
	}

	@Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void vnap$saveData(net.minecraft.nbt.CompoundTag output, CallbackInfo ci) {
		output.putBoolean("VillagerNewsHasNose", vnap$hasNose());
		output.putInt("VillagerNewsCosmetic", vnap$cosmetic());
		output.putInt("VillagerNewsSignMessage", vnap$signMessage());
		output.putInt("VillagerNewsSignType", vnap$signType());
		output.putInt("VillagerNewsSignSchema", 1);
		if (vnap$originalVillagerData != null && vnap$originalVillagerOffers != null) {
			VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, vnap$originalVillagerData).result()
				.ifPresent(tag -> output.put("VillagerNewsOriginalData", tag));
			MerchantOffers.CODEC.encodeStart(NbtOps.INSTANCE, vnap$originalVillagerOffers).result()
                .ifPresent(tag -> output.put("VillagerNewsOriginalOffers", tag));
		}
	}

	@Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void vnap$loadData(net.minecraft.nbt.CompoundTag input, CallbackInfo ci) {
		vnap$setHasNose(input.contains("VillagerNewsHasNose") ? input.getBoolean("VillagerNewsHasNose") : true);
		vnap$setCosmetic(input.contains("VillagerNewsCosmetic") ? input.getInt("VillagerNewsCosmetic") : 0);
		int signMessage = input.contains("VillagerNewsSignMessage") ? input.getInt("VillagerNewsSignMessage") : -1;
		vnap$setSignMessage(signMessage);
		int equippedSign = ContextualDialogueController.signType(((Villager) (Object) this).getMainHandItem());
		int savedSignType = input.contains("VillagerNewsSignType") ? input.getInt("VillagerNewsSignType") : -1;
		if (!input.contains("VillagerNewsSignSchema") && savedSignType >= 8) {
			// Minecraft 26.3 had Pale Oak inserted at index 8. In 1.21.1 it does not exist.
			savedSignType = savedSignType == 8 ? -1 : savedSignType - 1;
		}
		vnap$setSignType(savedSignType >= 0 ? savedSignType : equippedSign >= 0 ? equippedSign : signMessage >= 0 ? 0 : -1);
		if (equippedSign >= 0) ((Villager) (Object) this).setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		vnap$originalVillagerData = input.contains("VillagerNewsOriginalData")
			? VillagerData.CODEC.parse(NbtOps.INSTANCE, input.get("VillagerNewsOriginalData")).result().orElse(null) : null;
		vnap$originalVillagerOffers = input.contains("VillagerNewsOriginalOffers")
			? readOffers(input.getCompound("VillagerNewsOriginalOffers")) : null;
		if (vnap$originalVillagerData == null || vnap$originalVillagerOffers == null) {
			vnap$originalVillagerData = null;
			vnap$originalVillagerOffers = null;
		}
	}

	@Unique
	private static MerchantOffers readOffers(net.minecraft.nbt.CompoundTag tag) {
		return MerchantOffers.CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(null);
	}

	@Redirect(
		method = "customServerAiStep()V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;stopTrading()V")
	)
	private void vnap$keepSpecialTradeOpen(Villager villager) {
		if (!ContextualDialogueController.isSpecialTrader(villager)) {
			villager.setTradingPlayer(null);
		}
	}

	@ModifyVariable(method = "setVillagerData(Lnet/minecraft/world/entity/npc/VillagerData;)V", at = @At("HEAD"), argsOnly = true)
	private VillagerData vnap$preventSpecialProfession(VillagerData value) {
		Villager villager = (Villager) (Object) this;
		return ContextualDialogueController.isSpecialTrader(villager)
			? value.setProfession(VillagerProfession.NONE).setLevel(1)
			: value;
	}

	@Override
	public boolean vnap$hasOriginalVillagerState() {
		return vnap$originalVillagerData != null && vnap$originalVillagerOffers != null;
	}

	@Override
	public void vnap$captureOriginalVillagerState() {
		if (vnap$hasOriginalVillagerState()) return;
		Villager villager = (Villager) (Object) this;
		vnap$originalVillagerData = villager.getVillagerData();
		vnap$originalVillagerOffers = villager.getOffers().copy();
	}

	@Override
	public void vnap$restoreOriginalVillagerState() {
		if (!vnap$hasOriginalVillagerState()) return;
		Villager villager = (Villager) (Object) this;
		VillagerData originalData = vnap$originalVillagerData;
		MerchantOffers originalOffers = vnap$originalVillagerOffers.copy();
		vnap$originalVillagerData = null;
		vnap$originalVillagerOffers = null;
		villager.setVillagerData(originalData);
		villager.getOffers().clear();
		villager.getOffers().addAll(originalOffers);
	}

	@Override
	public boolean vnap$hasNose() {
		return ((Villager) (Object) this).getEntityData().get(VNAP_HAS_NOSE);
	}

	@Override
	public void vnap$setHasNose(boolean value) {
		((Villager) (Object) this).getEntityData().set(VNAP_HAS_NOSE, value);
	}

	@Override
	public int vnap$cosmetic() {
		return ((Villager) (Object) this).getEntityData().get(VNAP_COSMETIC);
	}

	@Override
	public void vnap$setCosmetic(int value) {
		((Villager) (Object) this).getEntityData().set(VNAP_COSMETIC, Math.max(0, Math.min(4, value)));
	}

	@Override
	public int vnap$signMessage() {
		return ((Villager) (Object) this).getEntityData().get(VNAP_SIGN_MESSAGE);
	}

	@Override
	public void vnap$setSignMessage(int value) {
		((Villager) (Object) this).getEntityData().set(VNAP_SIGN_MESSAGE, Math.max(-1, Math.min(86, value)));
	}

	@Override
	public int vnap$signType() {
		return ((Villager) (Object) this).getEntityData().get(VNAP_SIGN_TYPE);
	}

	@Override
	public void vnap$setSignType(int value) {
		((Villager) (Object) this).getEntityData().set(VNAP_SIGN_TYPE, Math.max(-1, Math.min(10, value)));
	}
}
