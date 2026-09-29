from pathlib import Path

p = Path("index.html")
t = p.read_text(encoding="utf-8")
needle = "    if(btn.dataset.tab==='groups')renderGroups();\n  };\n});"
insert = (
    "    if(btn.dataset.tab==='groups')renderGroups();\n"
    "    const sb=document.getElementById('speakBtn');\n"
    "    if(sb) sb.style.visibility=btn.dataset.tab==='home'?'visible':'hidden';\n"
    "  };\n});"
)
if needle in t:
    t = t.replace(needle, insert, 1)
t = t.replace(
    'accept=".xlsx,.xls" hidden/>',
    'accept=".xlsx,.xls,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel" style="position:fixed;left:-9999px;width:1px;height:1px;opacity:0"/>',
    1,
)
t = t.replace(
    ".actions{display:grid;grid-template-columns:1fr 1.15fr 1fr;gap:10px;margin-top:14px}",
    ".actions{display:grid;grid-template-columns:1fr 1.15fr;gap:10px;margin-top:14px}",
    1,
)
t = t.replace(
    '    <button class="btn ghost" id="againBtn">\u518d\u51fa\u4e00\u8a5e</button>\n',
    "",
    1,
)
t = t.replace("document.getElementById('againBtn').onclick=pickWord;\n", "")

if ".icon-btn.is-on" not in t:
    t = t.replace(
        ".icon-btn{background:var(--paper);border:1px solid var(--line);color:var(--ink);width:40px;height:40px;border-radius:14px;box-shadow:0 1px 0 rgba(255,255,255,.55)}",
        ".icon-btn{background:var(--paper);border:1px solid var(--line);color:var(--ink);width:40px;height:40px;border-radius:14px;box-shadow:0 1px 0 rgba(255,255,255,.55);transition:transform .12s ease,background .12s ease,color .12s ease,border-color .12s ease}\n.icon-btn:active,.icon-btn.is-on{background:#2a1b12;border-color:#2a1b12;color:#f7f0e0;transform:scale(.88);box-shadow:none}\n.btn{transition:transform .12s ease,background .12s ease,filter .12s ease}\n.btn.primary:active,.btn.primary.is-on{background:#9a4310;transform:scale(.96)}\n.btn.ghost:active,.btn.ghost.is-on{background:#e8d7b8;transform:scale(.96)}",
        1,
    )
t = t.replace(
    '<button class="icon-btn" id="speakBtn" aria-label="\u6717\u8b80">\u266a</button>',
    '<button class="icon-btn" id="speakBtn" aria-label="\u6717\u8b80"><span id="speakGlyph">\u266a</span></button>',
    1,
)
if "function bump(" not in t:
    t = t.replace(
        "function speak(){\n  const w=state.current;if(!w||!speechSynthesis)return;\n  speechSynthesis.cancel();\n  const u=new SpeechSynthesisUtterance(w.speech||w.kana);u.lang='ja-JP';u.rate=.9;speechSynthesis.speak(u);\n}",
        "function bump(el,ms,on,off){\n  if(!el)return;\n  el.classList.add('is-on');\n  if(on){const g=el.querySelector('#speakGlyph')||el;g.textContent=on;}\n  clearTimeout(el._bump);\n  el._bump=setTimeout(()=>{el.classList.remove('is-on');if(off){const g=el.querySelector('#speakGlyph')||el;g.textContent=off;}},ms||200);\n}\nfunction speak(){\n  const w=state.current;if(!w)return;\n  const btn=document.getElementById('speakBtn');\n  bump(btn,700,'\u266b','\u266a');\n  const text=w.speech||w.kana||w.kanji||'';\n  const a=android();\n  if(a&&a.speak){try{a.speak(text);return}catch(e){}}\n  if(!speechSynthesis)return;\n  speechSynthesis.cancel();\n  const u=new SpeechSynthesisUtterance(text);u.lang='ja-JP';u.rate=.9;speechSynthesis.speak(u);\n}",
        1,
    )
t = t.replace(
    "document.getElementById('nextBtn').onclick=pickWord;",
    "document.getElementById('nextBtn').onclick=()=>{bump(document.getElementById('nextBtn'),180);pickWord()};",
    1,
)

t = t.replace(
    '<p class="hint" id="permBattery" style="padding:0">\u7565\u904e\u96fb\u6c60\u512a\u5316\uff1a\u2026</p>',
    '<p class="hint" id="permBattery" style="display:none"></p>',
    1,
)
if "if(batteryEl) batteryEl.style.display='none'" not in t:
    t = t.replace(
        "if(batteryEl) batteryEl.textContent='\u7565\u904e\u96fb\u6c60\u512a\u5316\uff1a'+(p.battery?'\u5df2\u5141\u8a31':'\u5c1a\u672a\u5141\u8a31');",
        "if(batteryEl) batteryEl.style.display='none';",
        1,
    )

if '.guide{' not in t:
    t = t.replace(
        ".list{min-height:0;flex:1;overflow-y:auto}",
        ".list{min-height:0;flex:1;overflow-y:auto}\n.guide{background:var(--paper);border:1px solid var(--line);border-radius:16px;padding:14px 16px;margin:12px 0 16px}\n.guide-title{font-weight:600;color:var(--ink);margin:0 0 8px;font-size:15px}\n.guide p{margin:0 0 6px;font-size:13px;line-height:1.55;color:var(--muted)}\n.guide p:last-child{margin:0}\n.word-row{display:block;width:100%;border:0;border-bottom:1px solid var(--line);background:transparent;text-align:left;padding:14px 8px;font-family:\"Noto Sans TC\",sans-serif;color:inherit}\n.back-row{display:flex;align-items:center;gap:8px;margin:0 0 8px}\n.back-row .btn{padding:10px 12px;min-height:40px;flex-shrink:0}\n.detail-pos{font-family:\"Noto Sans TC\",sans-serif;font-size:13px;color:var(--muted);margin-top:8px}",
        1,
    )

guide_path = Path("scripts/tts_guide.html")
if guide_path.exists() and 'id="ttsGuide"' not in t:
    guide = guide_path.read_text(encoding="utf-8")
    inserted = False
    for needle_html in (
        '<p class="hint" id="permBattery" style="display:none"></p>',
        '<p class="hint" id="permOverlay" style="padding:0">\u986f\u793a\u5728\u5176\u4ed6\u61c9\u7528\u7a0b\u5f0f\u4e0a\u5c64\uff1a\u2026</p>',
        '<div class="row">\n    <span>\u89e3\u9396\u6642\u986f\u793a\u55ae\u8a5e</span>',
    ):
        if needle_html in t:
            if needle_html.startswith('<div class="row">'):
                t = t.replace(needle_html, guide + needle_html, 1)
            else:
                t = t.replace(needle_html, needle_html + "\n" + guide, 1)
            inserted = True
            break
    if not inserted:
        print("WARN: tts guide needle not found")

# --- group word list + word detail ---
if 'id="groupWords"' not in t:
    extra_html = '''
<section id="groupWords" class="screen">
  <div class="back-row">
    <button type="button" class="btn ghost" id="backGroups">返回</button>
    <p class="hint" id="groupWordsTitle" style="padding:0;margin:0"></p>
  </div>
  <div class="list" id="wordList"></div>
</section>
<section id="wordDetail" class="screen">
  <div class="back-row">
    <button type="button" class="btn ghost" id="backWords">返回</button>
    <button type="button" class="icon-btn" id="detailSpeak" aria-label="朗讀" style="margin-left:auto">♪</button>
  </div>
  <div class="card" id="detailCard">
    <div class="corner"><div class="mark" id="dMark"></div><div class="trans" id="dTrans"></div></div>
    <div class="group-tag" id="dMeta"></div>
    <p class="kana" id="dKana"></p>
    <p class="kanji" id="dKanji"></p>
    <p class="zh" id="dZh"></p>
    <p class="detail-pos" id="dPos"></p>
    <div class="ex" id="dEx"></div>
  </div>
</section>
'''
    t = t.replace('</section>\n\n<section id="import"', '</section>\n' + extra_html + '<section id="import"', 1)

t = t.replace(
    '''      <button type="button" class="group-main">
        <span><span style="display:block;font-weight:500">${label}</span>
        <span class="hint" style="padding:0;font-size:12px">${sub}</span></span>
        <span class="check ${g.active?'on':''}">✓</span>
      </button>''',
    '''      <button type="button" class="group-main">
        <span><span style="display:block;font-weight:500">${label}</span>
        <span class="hint" style="padding:0;font-size:12px">${sub}</span></span>
      </button>
      <button type="button" class="check ${g.active?'on':''}" aria-label="勾選">✓</button>''',
    1,
)
t = t.replace(
    '''    el.querySelector('.group-main').onclick=()=>{
      const g=state.data.groups.find(x=>x.id===id);g.active=!g.active;save();renderGroups();
    };''',
    '''    el.querySelector('.group-main').onclick=()=>openGroupWords(id);
    el.querySelector('.check').onclick=e=>{
      e.stopPropagation();
      const g=state.data.groups.find(x=>x.id===id);g.active=!g.active;save();renderGroups();
    };''',
    1,
)

if 'function openGroupWords' not in t:
    t = t.replace(
        'boot();\n</script>',
        r'''function esc(s){return String(s||'').replace(/[&<>"']/g,c=>({'&':'&','<':'<','>':'>','"':'"',"'":'&#39;'}[c]))}
function showBrowse(id){
  document.querySelectorAll('.screen').forEach(s=>s.classList.toggle('on',s.id===id));
  document.querySelectorAll('nav button').forEach(b=>b.classList.toggle('on',b.dataset.tab==='groups'));
  const sb=document.getElementById('speakBtn');
  if(sb) sb.style.visibility='hidden';
}
function openGroupWords(id){
  const g=state.data.groups.find(x=>x.id===id); if(!g) return;
  state.browseGroupId=id;
  const title=document.getElementById('groupWordsTitle');
  if(title) title.textContent=displayGroupName(g.name)+' · '+(g.words||[]).length+' 個單詞';
  const box=document.getElementById('wordList');
  if(!box) return;
  const words=g.words||[];
  if(!words.length){box.innerHTML='<p class="hint">此課沒有單詞</p>';showBrowse('groupWords');return;}
  box.innerHTML=words.map((w,i)=>{
    const kana=cleanForm(w.kana)||w.kana||'';
    const kanji=cleanForm(w.kanji);
    const head=kanji&&kanji!==kana&&kanji!=='-'?kanji:kana;
    const sub=[kana&&kana!==head?kana:'', w.zh].filter(Boolean).join(' · ');
    return `<button type="button" class="word-row" data-i="${i}"><span style="display:block;font-weight:500">${esc(head||'（無）')}</span><span class="hint" style="padding:0;font-size:12px">${esc(sub)}</span></button>`;
  }).join('');
  box.querySelectorAll('.word-row').forEach(el=>{ el.onclick=()=>openWordDetail(+el.dataset.i); });
  showBrowse('groupWords');
}
function openWordDetail(i){
  const g=state.data.groups.find(x=>x.id===state.browseGroupId); if(!g) return;
  const w=(g.words||[])[i]; if(!w) return;
  state.browseWordIndex=i;
  const kana=cleanForm(w.kana)||w.kana||'';
  const kanji=cleanForm(w.kanji);
  const showKanji=kanji&&kanji!==kana&&kanji!=='-';
  const set=(id,v)=>{const el=document.getElementById(id); if(el) el.textContent=v||'';};
  set('dMark',w.mark||'');
  set('dTrans',w.trans||'');
  set('dMeta',displayGroupName(g.name));
  set('dKana',kana);
  set('dKanji',showKanji?kanji:'');
  set('dZh',w.zh||'');
  set('dPos',w.pos||'');
  const ex=document.getElementById('dEx');
  if(ex){
    if(w.exampleJp||w.exampleZh){
      ex.innerHTML=`${w.exampleJp?`<div>${esc(w.exampleJp)}</div>`:''}${w.exampleZh?`<div class="ex-zh">${esc(w.exampleZh)}</div>`:''}`;
    }else ex.innerHTML='';
  }
  showBrowse('wordDetail');
}
function speakBrowse(){
  const g=state.data.groups.find(x=>x.id===state.browseGroupId); if(!g) return;
  const w=(g.words||[])[state.browseWordIndex]; if(!w) return;
  const btn=document.getElementById('detailSpeak');
  if(btn) bump(btn,700);
  const text=w.speech||w.kana||w.kanji||'';
  const a=android();
  if(a&&a.speak){try{a.speak(text);return}catch(e){}}
  if(!speechSynthesis)return;
  speechSynthesis.cancel();
  const u=new SpeechSynthesisUtterance(text);u.lang='ja-JP';u.rate=.9;speechSynthesis.speak(u);
}
const backG=document.getElementById('backGroups');
const backW=document.getElementById('backWords');
const dSpeak=document.getElementById('detailSpeak');
if(backG) backG.onclick=()=>{state.browseGroupId=null; if(typeof setTab==='function') setTab('groups'); else {document.querySelectorAll('.screen').forEach(s=>s.classList.toggle('on',s.id==='groups'));renderGroups();}};
if(backW) backW.onclick=()=>openGroupWords(state.browseGroupId);
if(dSpeak) dSpeak.onclick=speakBrowse;
boot();
</script>'''
    )

p.write_text(t, encoding="utf-8")
print("patched", p.stat().st_size, "openGroupWords" in t, "ttsGuide" in t)
