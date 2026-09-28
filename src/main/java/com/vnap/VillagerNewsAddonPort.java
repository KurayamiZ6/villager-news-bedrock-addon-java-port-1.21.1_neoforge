package com.vnap;

import com.vnap.command.DialogueTestCommand;
import com.vnap.config.VillagerNewsBuildSettings;
import com.vnap.config.VillagerNewsSettings;
import com.vnap.dialogue.ContextualDialogueController;
import com.vnap.dialogue.DialogueCatalog;
import com.vnap.item.VillagerNewsItems;
import com.vnap.network.ClientPayloadHandlers;
import com.vnap.network.DialogueAnimationPayload;
import com.vnap.network.HurtEffectPayload;
import com.vnap.network.VillagerNewsSettingsNetwork;
import com.vnap.network.VillagerNewsSettingsPayload;
import com.vnap.sound.SupplementalSoundCatalog;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(VillagerNewsAddonPort.MOD_ID)
public final class VillagerNewsAddonPort {
    public static final String MOD_ID = "villager_news_addon_port";
    /** Resource namespace retained for compatibility with the existing assets/data and registered IDs. */
    public static final String RESOURCE_NAMESPACE = "villager-news-addon-port";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public VillagerNewsAddonPort(IEventBus modEventBus, ModContainer modContainer) {
        VillagerNewsItems.register(modEventBus);
        VillagerNewsSettings.load();
        SupplementalSoundCatalog.register(modEventBus);
        DialogueCatalog.register(modEventBus);

        modEventBus.addListener(VillagerNewsAddonPort::registerPayloads);
        VillagerNewsSettingsNetwork.register();
        ContextualDialogueController.register();
        if (VillagerNewsBuildSettings.dialogueTestCommand()) {
            DialogueTestCommand.register();
        }
        LOGGER.info("Villager News models, textures, and contextual dialogue are ready for NeoForge 1.21.1.");
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(DialogueAnimationPayload.TYPE, DialogueAnimationPayload.CODEC, ClientPayloadHandlers::handleDialogueAnimation);
        registrar.playToClient(HurtEffectPayload.TYPE, HurtEffectPayload.CODEC, ClientPayloadHandlers::handleHurtEffect);
        registrar.playBidirectional(
                VillagerNewsSettingsPayload.TYPE,
                VillagerNewsSettingsPayload.CODEC,
                new DirectionalPayloadHandler<>(
                        ClientPayloadHandlers::handleSettings,
                        VillagerNewsSettingsNetwork::handleServerPayload
                )
        );
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RESOURCE_NAMESPACE, path);
    }
}
