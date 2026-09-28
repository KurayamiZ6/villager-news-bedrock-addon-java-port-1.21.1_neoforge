package com.vnap.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Common-side payload entry points. Client-only handlers are invoked only for play-to-client payloads.
 */
public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    public static void handleDialogueAnimation(DialogueAnimationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            com.vnap.client.DialogueSoundState.start(payload);
            com.vnap.client.DialogueAnimationState.start(payload);
            com.vnap.client.DialogueSubtitleState.start(payload);
        });
    }

    public static void handleHurtEffect(HurtEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.vnap.client.SupplementalSoundState.play(payload));
    }

    public static void handleSettings(VillagerNewsSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.vnap.client.VillagerNewsSettingsState.apply(payload));
    }
}
