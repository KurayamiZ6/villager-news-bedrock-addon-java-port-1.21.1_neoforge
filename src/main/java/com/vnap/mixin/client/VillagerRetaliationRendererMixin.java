package com.vnap.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Villager Retaliation selects its active model inside render() by calling its
 * private currentRenderMode() method. FIX6 attempted to change the model field
 * before that selection, so the original method immediately overwrote the
 * change.
 *
 * This version redirects the actual mode lookup used by render() to
 * PACK_NATIVE. PACK_NATIVE is Villager Retaliation's own vanilla-model adapter
 * path. With Villager News installed, that is the same model path used by the
 * vanilla villager renderer and therefore remains compatible with EMF/CEM
 * without allowing the Retaliation Fresh-compatible combat/humanoid models to
 * become the parent of the profession layer.
 *
 * The target mod remains optional: reflection and @Coerce avoid a hard
 * compile-time dependency on Villager Retaliation.
 */
@Pseudo
@Mixin(
        targets = "com.jvn.villagerretaliation.client.renderer.AbstractVillagerRetaliationVillagerRenderer",
        remap = false
)
public abstract class VillagerRetaliationRendererMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/jvn/villagerretaliation/client/renderer/AbstractVillagerRetaliationVillagerRenderer;currentRenderMode()Lcom/jvn/villagerretaliation/config/VillagerRenderMode;"
            )
    )
    private @Coerce Object vnap$forcePackNativeMode(@Coerce Object renderer) {
        try {
            Class<?> modeClass = Class.forName(
                    "com.jvn.villagerretaliation.config.VillagerRenderMode",
                    false,
                    renderer.getClass().getClassLoader()
            );
            @SuppressWarnings({"rawtypes", "unchecked"})
            Enum<?> mode = Enum.valueOf((Class) modeClass, "PACK_NATIVE");
            return mode;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            throw new IllegalStateException(
                    "Villager News Fresh compatibility could not select Villager Retaliation PACK_NATIVE mode",
                    exception
            );
        }
    }
}
