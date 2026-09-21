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

# press / playing feedback
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
p.write_text(t, encoding="utf-8")
print("patched", p.stat().st_size, "is-on" in t, "againBtn" not in t)
