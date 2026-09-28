import fs from 'node:fs';
import path from 'node:path';

const root = process.env.VNAP_PROJECT_ROOT || process.cwd();
const src = path.join(root, 'src', 'main');
const javaRoot = path.join(src, 'java');
const resourcesRoot = path.join(src, 'resources');

function walk(dir, predicate = () => true) {
  const out = [];
  if (!fs.existsSync(dir)) return out;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const file = path.join(dir, entry.name);
    if (entry.isDirectory()) out.push(...walk(file, predicate));
    else if (predicate(file)) out.push(file);
  }
  return out;
}

const javaFiles = walk(javaRoot, f => f.endsWith('.java'));
const java = javaFiles.map(file => fs.readFileSync(file, 'utf8')).join('\n');
const clientMixins = JSON.parse(fs.readFileSync(path.join(resourcesRoot, 'villager-news-addon-port.client.mixins.json'), 'utf8'));
const serverMixins = JSON.parse(fs.readFileSync(path.join(resourcesRoot, 'villager-news-addon-port.mixins.json'), 'utf8'));

const failures = [];
const checks = [];
function check(label, ok, failureDetail='') {
  checks.push({ label, ok, failureDetail });
  if (!ok) failures.push(`${label}: ${failureDetail || 'failed'}`);
}

check('No Fabric imports in active Java', !java.includes('net.fabricmc.'), 'found net.fabricmc namespace');
check('No 26.x render-state APIs in active Java', !/VillagerRenderState|GuiGraphicsExtractor|SubmitNodeCollector/.test(java), 'found obsolete 26.x renderer APIs');
check('Villager package uses 1.21.1 namespace', !java.includes('world.entity.npc.villager.'), 'found old villager subpackages');
check('ResourceLocation is used instead of Identifier', !/net\.minecraft\.resources\.Identifier/.test(java), 'found Identifier import');
check('No Fabric Mod Menu API', !java.includes('com.terraformersmc.modmenu'), 'found Mod Menu Fabric API');
check('Profession-layer alignment mixin is active', clientMixins.client.includes('VillagerProfessionLayerMixin'), 'profession-layer mixin missing');
check('EMF mixin remains active', clientMixins.client.includes('EMFModelPartMixin'), 'EMFModelPartMixin missing');
check('Villager renderer mixin remains active', clientMixins.client.includes('VillagerRendererMixin'), 'VillagerRendererMixin missing');
check('No obsolete spawn-reason tracker mixin', !serverMixins.mixins.includes('VillagerSpawnReasonMixin') && !java.includes('VillagerSpawnReasonTracker'), 'obsolete spawn tracker remains');
check('Mob#getSpawnType is used for 1.21.1 spawn reason', java.includes('.getSpawnType()'), 'spawn reason is not read from Mob#getSpawnType');
check('1.21.1 spawn-egg reason is used', java.includes('MobSpawnType.SPAWN_EGG'), 'spawn egg branch not mapped to SPAWN_EGG');
check('No obsolete 26.x SPAWN_ITEM_USE reason', !java.includes('SPAWN_ITEM_USE'), 'found 26.x spawn reason');
check('Recipe is in 1.21.1 plural recipes directory', fs.existsSync(path.join(resourcesRoot, 'data', 'villager-news-addon-port', 'recipes', 'handbook.json')) && !fs.existsSync(path.join(resourcesRoot, 'data', 'villager-news-addon-port', 'recipe')), 'recipe layout mismatch');
check('pack.mcmeta exists', fs.existsSync(path.join(resourcesRoot, 'pack.mcmeta')), 'missing pack.mcmeta');
check('No active Pale Oak sign reference', !/PALE_OAK|pale_oak/.test(java), 'found pale oak reference');
check('No removed sulfur damage type', !java.includes('SULFUR_CUBE_HOT'), 'found removed damage type');
check('No stale 26.3 profession accessor', !java.includes('getVillagerData().profession()'), 'found profession().value() pattern');
check('VillagerData level getter is 1.21.1-compatible', java.includes('getVillagerData().getLevel()'), 'found newer record-style level() access');
check('No stale 26.3 screen API', !java.includes('setScreenAndShow'), 'found setScreenAndShow');
check('No stale respawn API', !java.includes('getRespawnData'), 'found getRespawnData');
check('ServerPlayer uses serverLevel when ServerLevel is required', !/ServerLevel\s+\w+\s*=\s*\w+\.level\(\)/.test(java), 'found ServerLevel = player.level()');
check('No stale overlay-message API', !java.includes('sendOverlayMessage'), 'found sendOverlayMessage');
check('No stale permanent-invulnerable API', !java.includes('setPermanentlyInvulnerable'), 'found setPermanentlyInvulnerable');
check('No stale DeltaTracker client API', !java.includes('getDeltaTracker'), 'found getDeltaTracker');
check('No client Level UUID lookup overload', !/minecraft\.level\.getEntity\([^)]*(?:UUID|payload\.entityId|entry\.getKey|\bid\b)[^)]*\)/.test(java), 'found client getEntity(uuid)');
check('No newer MerchantOffers CODEC dependency', !java.includes('MerchantOffers.CODEC'), 'found newer MerchantOffers CODEC');
check('VillagerNewsSignLayer uses vanilla entity sign textures', java.includes('textures/entity/signs/') && !java.includes('textures/block/'), 'sign layer still references block-sign textures');
check('EMF attachment lookup uses 1.21 accessor bridge', java.includes('EMFModelPartAccessor') && java.includes('vnap$getAttachmentPositioner'), 'EMF attachment accessor bridge missing');
check('Build properties placeholder is expanded', fs.readFileSync(path.join(resourcesRoot, 'villager-news-addon-port-build.properties'), 'utf8').includes('${dialogue_test_command}') && java.includes('dialogueTestCommand'), 'dialogue_test_command expansion is not configured');
check('Deferred registration is used for custom items', java.includes('DeferredRegister.Items'), 'custom items are not clearly deferred-registered');
check('Deferred registration is used for custom sounds', java.includes('DeferredRegister<SoundEvent>'), 'custom sounds are not clearly deferred-registered');
check('Common Java has no client-only imports', !javaFiles.filter(file => !file.includes(`${path.sep}client${path.sep}`)).some(file => {
  const text = fs.readFileSync(file, 'utf8');
  return /import\s+com\.vnap\.client\.|import\s+net\.minecraft\.client\./.test(text);
}), 'common source imports a client-only package');
check('Client payload handler keeps client references behind DistExecutor', !/^import\s+com\.vnap\.client\./m.test(fs.readFileSync(path.join(javaRoot, 'com', 'vnap', 'network', 'ClientPayloadHandlers.java'), 'utf8')), 'client handler imports client-only classes directly');
check('No obvious duplicate return statements', !/return;\s*return;/.test(java), 'duplicate return statements detected');

console.log(`final-api-audit-1.21.1: ${checks.filter(c => c.ok).length}/${checks.length} passed`);
for (const c of checks) console.log(`${c.ok ? 'PASS' : 'FAIL'} ${c.label}${!c.ok && c.failureDetail ? ` — ${c.failureDetail}` : ''}`);
if (failures.length) {
  console.error(`\n${failures.length} checks failed.`);
  process.exit(1);
}
