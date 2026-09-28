package com.vnap.item;

import com.mojang.serialization.JsonOps;
import com.vnap.VillagerNewsAddonPort;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class VillagerNewsItems {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VillagerNewsAddonPort.RESOURCE_NAMESPACE);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(
		Registries.CREATIVE_MODE_TAB, VillagerNewsAddonPort.RESOURCE_NAMESPACE);

	public static final DeferredItem<Item> HANDBOOK = ITEMS.registerItem("handbook", properties -> new Item(properties.stacksTo(1)));
	public static final DeferredItem<Item> MAYOR_HAT = ITEMS.registerItem("mayor_hat", properties -> new Item(properties.stacksTo(1)));
	public static final DeferredItem<Item> MICROPHONE = ITEMS.registerItem("microphone", properties -> new Item(properties.stacksTo(1)));
	public static final DeferredItem<Item> MOUSTACHE = ITEMS.registerItem("moustache", properties -> new Item(properties.stacksTo(1)));
	public static final DeferredItem<Item> TESTIFICATE_MAN_HELMET = ITEMS.registerItem("testificate_man_helmet", properties -> new Item(properties.stacksTo(1)));
	public static final DeferredItem<Item> VILLAGER_NOSE = ITEMS.registerItem("villager_nose", properties -> new Item(properties.stacksTo(1)));
	public static final DeferredItem<Item> MAYOR_VILLAGER_SPAWN_EGG = registerSpawnEgg("mayor_villager_spawn_egg", EntityType.VILLAGER, "Mayor Villager");
	public static final DeferredItem<Item> TESTIFICATE_MAN_SPAWN_EGG = registerSpawnEgg("testificate_man_spawn_egg", EntityType.VILLAGER, "Testificate Man");
	public static final DeferredItem<Item> VILLAGER_5_SPAWN_EGG = registerSpawnEgg("villager_5_spawn_egg", EntityType.VILLAGER, "Villager #5");
	public static final DeferredItem<Item> VILLAGER_9_SPAWN_EGG = registerSpawnEgg("villager_9_spawn_egg", EntityType.VILLAGER, "Villager #9");
	public static final DeferredItem<Item> UNTOUCHABLE_VILLAGER_SPAWN_EGG = registerSpawnEgg("untouchable_villager_spawn_egg", EntityType.VILLAGER, "Villager Unreachable");
	public static final DeferredItem<Item> WOOLY_SPAWN_EGG = registerSpawnEgg("wooly_spawn_egg", EntityType.SHEEP, "Wooly The Sheep");

	private static final Map<DeferredItem<Item>, Integer> COSMETICS = new LinkedHashMap<>();
	public static final Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register("items", () -> CreativeModeTab.builder()
		.title(Component.translatable("itemGroup.villager-news-addon-port.items"))
		.icon(() -> new ItemStack(HANDBOOK.get()))
		.displayItems((parameters, output) -> {
			output.accept(HANDBOOK.get());
			output.accept(MAYOR_HAT.get());
			output.accept(TESTIFICATE_MAN_HELMET.get());
			output.accept(MICROPHONE.get());
			output.accept(MOUSTACHE.get());
			output.accept(VILLAGER_NOSE.get());
			output.accept(MAYOR_VILLAGER_SPAWN_EGG.get());
			output.accept(TESTIFICATE_MAN_SPAWN_EGG.get());
			output.accept(VILLAGER_5_SPAWN_EGG.get());
			output.accept(VILLAGER_9_SPAWN_EGG.get());
			output.accept(UNTOUCHABLE_VILLAGER_SPAWN_EGG.get());
			output.accept(WOOLY_SPAWN_EGG.get());
		})
		.build());

	static {
		COSMETICS.put(MAYOR_HAT, 1);
		COSMETICS.put(TESTIFICATE_MAN_HELMET, 2);
		COSMETICS.put(MICROPHONE, 3);
		COSMETICS.put(MOUSTACHE, 4);
	}

	private VillagerNewsItems() {
	}

	public static void register(IEventBus modEventBus) {
		ITEMS.register(modEventBus);
		CREATIVE_TABS.register(modEventBus);
	}

	public static int cosmetic(Item item) {
		for (Map.Entry<DeferredItem<Item>, Integer> entry : COSMETICS.entrySet()) {
			if (entry.getKey().get() == item) return entry.getValue();
		}
		return 0;
	}

	public static Item cosmeticItem(int cosmetic) {
		return COSMETICS.entrySet().stream()
			.filter(entry -> entry.getValue() == cosmetic)
			.map(entry -> entry.getKey().get())
			.findFirst().orElse(null);
	}

	private static DeferredItem<Item> registerSpawnEgg(String path, EntityType<? extends Mob> type, String entityName) {
		CompoundTag tag = new CompoundTag();
		tag.putString("id", EntityType.getKey(type).toString());
		tag.putString("CustomName", ComponentSerialization.CODEC
			.encodeStart(JsonOps.INSTANCE, Component.literal(entityName))
			.getOrThrow()
			.toString());
		tag.putBoolean("PersistenceRequired", true);
		return ITEMS.registerItem(path, properties -> new SpawnEggItem(type,
			type == EntityType.SHEEP ? 15198183 : 5651507,
			type == EntityType.SHEEP ? 16758197 : 12422002,
			properties.component(DataComponents.ENTITY_DATA, CustomData.of(tag))
		));
	}
}
