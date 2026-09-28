import { existsSync, readFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const resources = join(root, 'src', 'main', 'resources');
const modAssets = join(resources, 'assets', 'villager-news-addon-port');

function readJson(path) {
  return JSON.parse(readFileSync(path, 'utf8'));
}

function check(condition, message) {
  if (!condition) throw new Error(message);
}

const dialogues = readJson(join(modAssets, 'dialogues.json'));
const sounds = readJson(join(modAssets, 'sounds.json'));
const animations = readJson(join(modAssets, 'dialogue_animations.json'));
const handbook = readJson(join(modAssets, 'handbook.json'));
const language = readJson(join(modAssets, 'lang', 'en_us.json'));
const behaviorSource = readFileSync(join(root, 'src/main/java/com/vnap/dialogue/ContextualDialogueController.java'), 'utf8');
const clientSource = readFileSync(join(root, 'src/main/java/com/vnap/client/VillagerNewsAddonPortClient.java'), 'utf8');
const clientEventsSource = readFileSync(join(root, 'src/main/java/com/vnap/client/VillagerNewsClientEvents.java'), 'utf8');
const subtitleSource = readFileSync(join(root, 'src/main/java/com/vnap/client/DialogueSubtitleState.java'), 'utf8');
const animationSource = readFileSync(join(root, 'src/main/java/com/vnap/client/DialogueAnimationState.java'), 'utf8');
const emfMixin = readFileSync(join(root, 'src/main/java/com/vnap/mixin/client/EMFModelPartMixin.java'), 'utf8');
const signLayer = readFileSync(join(root, 'src/main/java/com/vnap/client/VillagerNewsSignLayer.java'), 'utf8');
const buildSource = readFileSync(join(root, 'build.gradle'), 'utf8');
const gradleProperties = readFileSync(join(root, 'gradle.properties'), 'utf8');
const mixinClient = readFileSync(join(resources, 'villager-news-addon-port.client.mixins.json'), 'utf8');
const toml = readFileSync(join(resources, 'META-INF/neoforge.mods.toml'), 'utf8');
const itemsSource = readFileSync(join(root, 'src/main/java/com/vnap/item/VillagerNewsItems.java'), 'utf8');
const dialogueCatalogSource = readFileSync(join(root, 'src/main/java/com/vnap/dialogue/DialogueCatalog.java'), 'utf8');
const soundCatalogSource = readFileSync(join(root, 'src/main/java/com/vnap/sound/SupplementalSoundCatalog.java'), 'utf8');

const javaFiles = [];
async function collectJava(dir) {
  const entries = await (await import('node:fs/promises')).readdir(dir, { withFileTypes: true });
  for (const entry of entries) {
    const full = join(dir, entry.name);
    if (entry.isDirectory()) await collectJava(full);
    else if (entry.name.endsWith('.java')) javaFiles.push(full);
  }
}
await collectJava(join(root, 'src/main/java'));
const javaText = javaFiles.map((path) => readFileSync(path, 'utf8')).join('\n');

const groups = Object.entries(dialogues.groups);
check(groups.length === 523, `Expected 523 dialogue groups, found ${groups.length}`);
let variants = 0;
let subtitles = 0;
for (const [id, group] of groups) {
  check(Array.isArray(group.variants) && group.variants.length > 0, `Dialogue ${id} has no variants`);
  check(Array.isArray(animations.groups?.[id]) && animations.groups[id].length === group.variants.length,
    `Dialogue ${id} has mismatched animation variants`);
  for (const variant of group.variants) {
    const event = sounds[`dialogue.${id}.${variant.index}`];
    check(event?.sounds?.length === 1, `Dialogue ${id}.${variant.index} must have one sound`);
    const sound = event.sounds[0];
    const name = typeof sound === 'string' ? sound : sound?.name;
    check(typeof name === 'string' && (sound?.stream === true || typeof sound === 'string'),
      `Dialogue ${id}.${variant.index} is missing streamed audio metadata`);
    const relative = name.replace('villager-news-addon-port:', '');
    check(existsSync(join(modAssets, 'sounds', `${relative}.ogg`)), `Missing audio file for ${name}`);
    check(Array.isArray(variant.subtitles) && variant.subtitles.length > 0, `Dialogue ${id}.${variant.index} has no timed subtitles`);
    check(variant.subtitles.every((entry, i) => typeof entry.key === 'string'
      && typeof language[entry.key] === 'string' && language[entry.key].length > 0
      && Number.isFinite(entry.time) && (i === 0 || entry.time >= variant.subtitles[i - 1].time)),
      `Dialogue ${id}.${variant.index} has an invalid subtitle timeline`);
    variants++;
    subtitles += variant.subtitles.length;
  }
}
check(variants === 2212, `Expected 2212 synchronized variants, found ${variants}`);
check(subtitles === 3741, `Expected 3741 timed subtitles, found ${subtitles}`);
check(animations.gestures?.length === 46, 'Expected 46 dialogue gestures');
check(animations.idles?.length === 6, 'Expected 6 idle animations');
check(animations.locomotion?.duration === 0.4375, 'Walking animation duration changed');
check(animations.runLocomotion?.duration === 0.4375, 'Running animation duration changed');

check(javaText.includes('net.minecraft.world.entity.npc.Villager'), 'Villager 1.21.1 package is not present');
check(!javaText.includes('import net.fabricmc.'), 'Fabric API import remains in Java source');
check(!javaText.includes('import com.terraformersmc.'), 'Fabric Mod Menu import remains in Java source');
for (const obsolete of ['GuiGraphicsExtractor', 'SubmitNodeCollector', 'VillagerRenderState',
  'net.minecraft.client.renderer.entity.state', 'net.minecraft.client.model.npc']) {
  check(!javaText.includes(obsolete), `Obsolete 26.x client API remains: ${obsolete}`);
}

check(gradleProperties.includes('mod_id=villager_news_addon_port'), 'NeoForge mod id is not the legal NeoForge identifier');
check(toml.includes('modId="neoforge"') && toml.includes('modId="minecraft"'), 'NeoForge/Minecraft dependencies missing');
check(buildSource.includes('net.neoforged.moddev') && buildSource.includes('net.neoforged:neoforge:${neo_version}'), 'NeoForge Gradle setup missing');
check(buildSource.includes('maven.modrinth:4I1XuqiY:gEOHNhtN'), 'EMF Version ID is not pinned');
check(buildSource.includes('maven.modrinth:BVzZfTc1:uGBt1h06'), 'ETF Version ID is not pinned');
check(buildSource.includes('curse.maven:entity-sound-features-1060324:7933712'), 'ESF CurseMaven artifact is not pinned');
check(buildSource.includes("name = 'CurseMaven'"), 'CurseMaven repository is missing');
check(itemsSource.includes('DeferredRegister.createItems') && itemsSource.includes('ITEMS.register(modEventBus)'), 'Items are not using NeoForge DeferredRegister');
check(itemsSource.includes('DeferredRegister<CreativeModeTab>') && itemsSource.includes('CREATIVE_TABS.register(modEventBus)'), 'Creative tab is not using DeferredRegister');
check(dialogueCatalogSource.includes('DeferredRegister<SoundEvent>') && !dialogueCatalogSource.includes('Registry.register('), 'Dialogue sounds still use direct registry mutation');
check(soundCatalogSource.includes('DeferredRegister<SoundEvent>') && !soundCatalogSource.includes('Registry.register('), 'Supplemental sounds still use direct registry mutation');
check(/-Xmaxerrs.+2000/.test(buildSource) && /-Xmaxwarns.+2000/.test(buildSource), 'javac error/warning limits are not 2000');

check(clientSource.includes('FMLClientSetupEvent') && clientSource.includes('EntityRenderersEvent.AddLayers'), 'NeoForge client bootstrap/layer registration missing');
check(subtitleSource.includes('GuiGraphics') && !subtitleSource.includes('GuiGraphicsExtractor'), 'Subtitle HUD was not ported to GuiGraphics');
check(signLayer.includes('RenderLayer<Villager, VillagerModel<Villager>>')
  && signLayer.includes('MultiBufferSource')
  && signLayer.includes('EMFAttachment.Type.VILLAGER'), 'Villager News sign render layer port is incomplete');
check(animationSource.includes('EMFAnimationApi.getCurrentEntity') && clientSource.includes('registerFloat'), 'EMF animation integration is missing');
check(mixinClient.includes('EMFModelPartMixin') && mixinClient.includes('EMFModelPartAccessor') && mixinClient.includes('VillagerProfessionLayerMixin') && emfMixin.includes('villager_news_base_fgk6'), 'EMF/client mixins are not fully active');
check(signLayer.includes('EMFModelPartAccessor') && signLayer.includes('vnap$getAttachmentPositioner') && emfMixin.includes('ResourceLocation textureOverride')
  && emfMixin.includes('render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V')
  && emfMixin.includes('compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V'),
  'EMF mixin does not target the verified 3.3.9 render/compile signatures');
check(javaText.includes('ContextualDialogueController') && javaText.includes('HurtEffectPayload'), 'Core dialogue/network classes are missing');
check(handbook.contexts && Object.keys(handbook.contexts).length === 491, 'Handbook context catalog is incomplete');
check(existsSync(join(modAssets, 'textures', 'entity', 'sign_text.png')), '87-message sign atlas is missing');
check(!existsSync(join(modAssets, 'items')), '26.x item display-definition directory remains active');
const recipe = readJson(join(resources, 'data', 'villager-news-addon-port', 'recipes', 'handbook.json'));
check(recipe.result?.item === 'villager-news-addon-port:handbook' && recipe.result?.count === 1, 'Handbook recipe is not in the 1.21.1 result format');
check(Array.isArray(recipe.ingredients) && recipe.ingredients.length === 3 && recipe.ingredients.every(entry => entry?.item === 'minecraft:paper'), 'Handbook recipe ingredients changed');
check(javaText.includes('VillagerNewsSignSchema') && javaText.includes('bamboo_sign') && signLayer.includes('minecraft("bamboo")') && !javaText.includes('pale_oak_sign'), '1.21.1 sign schema/palette adaptation is missing');
check(javaText.includes('.getSpawnType()') && !javaText.includes('VillagerSpawnReasonTracker') && !javaText.includes('VillagerSpawnReasonMixin'), 'Spawn reason bridge is not finalized for 1.21.1');
check(!javaText.includes('SULFUR_CUBE_HOT'), '26.3-only SULFUR_CUBE_HOT damage type remains');

console.log(`OK: ${groups.length} dialogue groups, ${variants} variants, ${subtitles} timed subtitles, ${javaFiles.length} Java files.`);
console.log('OK: Fabric/26.x client imports absent, NeoForge build pins present, EMF rainbow mixin verified structurally.');
