package com.vnap.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.models.parts.EMFModelPart;

/**
 * Wooly fix (1.21.1).
 *
 * On 26.x every EntityModel renders its root ModelPart, so the custom Wooly model attached to
 * "part": "root" in sheep2.jem / sheep3.jem is drawn. On 1.21.1 the sheep model is an
 * AgeableListModel: it only renders headParts() and bodyParts() (head, body, legs) and never the
 * root itself. Wooly's jem hides those vanilla parts and hangs its whole model on the root, so
 * nothing was drawn at all. Villagers are HierarchicalModels (they render the root), which is why
 * they were not affected.
 *
 * After the vanilla parts are drawn we render the custom parts EMF attached to the root ourselves.
 */
@Mixin(AgeableListModel.class)
public abstract class AgeableListModelMixin {
	@Shadow protected abstract Iterable<ModelPart> bodyParts();

	@Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("TAIL"))
	private void vnap$renderRootAttachedEmfParts(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color, CallbackInfo ci) {
		if (!((Object) this instanceof SheepModel<?>)) return;
		for (ModelPart part : this.bodyParts()) {
			if (part instanceof EMFModelPart emfPart) {
				// getAllEMFCustomChildren() only returns custom (jem) parts, never the vanilla ones,
				// so nothing is drawn twice.
				for (ModelPart custom : emfPart.getRoot().getAllEMFCustomChildren()) {
					custom.render(poseStack, buffer, light, overlay, color);
				}
				return;
			}
		}
	}
}
