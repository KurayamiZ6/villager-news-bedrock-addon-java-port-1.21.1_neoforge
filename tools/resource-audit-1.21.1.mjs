import fs from 'node:fs';
import path from 'node:path';

const root = process.env.VNAP_PROJECT_ROOT || process.cwd();
const resources = path.join(root, 'src', 'main', 'resources');
const asset = path.join(resources, 'assets', 'villager-news-addon-port');

function walk(dir, predicate = () => true) {
  const out=[];
  if (!fs.existsSync(dir)) return out;
  for (const e of fs.readdirSync(dir,{withFileTypes:true})) {
    const p=path.join(dir,e.name);
    if (e.isDirectory()) out.push(...walk(p,predicate));
    else if (predicate(p)) out.push(p);
  }
  return out;
}
function readJson(p){return JSON.parse(fs.readFileSync(p,'utf8'));}

const jsonFiles=walk(resources,f=>f.endsWith('.json'));
const jsonErrors=[];
for (const f of jsonFiles) { try { readJson(f); } catch(e) { jsonErrors.push(`${path.relative(root,f)}: ${e.message}`); } }

const soundsPath=path.join(asset,'sounds.json');
const sounds=readJson(soundsPath);
const oggFiles=walk(path.join(asset,'sounds'),f=>f.endsWith('.ogg'));
const oggRel=new Set(oggFiles.map(f=>path.relative(path.join(asset,'sounds'),f).replaceAll(path.sep,'/').replace(/\.ogg$/,'')));
const soundRefs=[];
for (const [id,data] of Object.entries(sounds)) {
  const entries=Array.isArray(data?.sounds)?data.sounds:[];
  for (const entry of entries) {
    const raw=typeof entry==='string'?entry:entry?.name;
    if (!raw) continue;
    // SoundEvent JSON may use an explicit namespace (e.g.
    // villager-news-addon-port:voice/foo), while the file tree is relative
    // to this mod's sounds directory (voice/foo.ogg).
    soundRefs.push(raw.includes(':') ? raw.slice(raw.indexOf(':') + 1) : raw);
  }
}
const soundMissing=[...new Set(soundRefs)].filter(name=>!oggRel.has(name));
const referencedOggs=new Set(soundRefs);
const unreferenced=[...oggRel].filter(name=>!referencedOggs.has(name));

const dialogues=readJson(path.join(asset,'dialogues.json'));
const groups=Object.entries(dialogues.groups ?? {});
const variants=groups.flatMap(([,g])=>g.variants ?? []);
const dialogueSoundMissing=[];
for (const [groupId,g] of groups) {
  for (const v of (g.variants ?? [])) {
    const key=`dialogue.${groupId}.${v.index}`;
    if (!(key in sounds)) dialogueSoundMissing.push(key);
  }
}

const anim=readJson(path.join(asset,'dialogue_animations.json'));
const subtitleCount=variants.reduce((n,v)=>n+(v.subtitles?.length??0),0);
const gestureCount=(anim.gestures??[]).length;
const idleCount=(anim.idles??[]).length;
const pngCount=walk(resources,f=>f.endsWith('.png')).length;
const jemCount=walk(resources,f=>f.endsWith('.jem')).length;

const checks=[
  ['JSON files parse',jsonErrors.length===0,`${jsonErrors.length} parse errors`],
  ['Dialogue groups',groups.length===523,`found ${groups.length}`],
  ['Dialogue variants',variants.length===2212,`found ${variants.length}`],
  ['Timed subtitle frames',subtitleCount===3741,`found ${subtitleCount}`],
  ['Gesture definitions',gestureCount===46,`found ${gestureCount}`],
  ['Idle animations',idleCount===6,`found ${idleCount}`],
  ['OGG files',oggFiles.length===2235,`found ${oggFiles.length}`],
  ['PNG files',pngCount===68,`found ${pngCount}`],
  ['JEM files',jemCount===13,`found ${jemCount}`],
  ['Referenced OGG files exist',soundMissing.length===0,`${soundMissing.length} missing`],
  ['Dialogue variant sound entries exist',dialogueSoundMissing.length===0,`${dialogueSoundMissing.length} missing`],
  ['Handbook recipe layout',fs.existsSync(path.join(resources,'data','villager-news-addon-port','recipes','handbook.json')), 'missing recipes/handbook.json'],
  ['Pack metadata',fs.existsSync(path.join(resources,'pack.mcmeta')), 'missing pack.mcmeta'],
];
console.log(`resource-audit-1.21.1: ${checks.filter(c=>c[1]).length}/${checks.length} passed`);
for(const [name,ok,detail] of checks) console.log(`${ok?'PASS':'FAIL'} ${name}${!ok?' — '+detail:''}`);
console.log(`sounds.json entries: ${Object.keys(sounds).length}`);
console.log(`referenced OGGs: ${referencedOggs.size}; unreferenced OGGs: ${unreferenced.length}`);
if (unreferenced.length) console.log(`unreferenced: ${unreferenced.join(', ')}`);
if (jsonErrors.length) { console.log('\nJSON errors:'); console.log(jsonErrors.join('\n')); }
if (soundMissing.length) { console.log('\nMissing sound files:'); console.log(soundMissing.join('\n')); }
if (dialogueSoundMissing.length) { console.log('\nMissing dialogue sounds:'); console.log(dialogueSoundMissing.join('\n')); }
process.exit(checks.every(c=>c[1])?0:1);
