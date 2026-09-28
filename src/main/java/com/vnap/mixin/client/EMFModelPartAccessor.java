package com.vnap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import traben.entity_model_features.models.animation.EMFAttachment;
import traben.entity_model_features.models.parts.EMFModelPart;

import java.util.function.Consumer;

/** Exposes EMF's protected attachment-positioner lookup to the sign render layer. */
@Mixin(value = EMFModelPart.class, remap = false)
public interface EMFModelPartAccessor {
	@Invoker("getAttachmentPositioner")
	Consumer<PoseStack> vnap$getAttachmentPositioner(EMFAttachment.Type type);
}
