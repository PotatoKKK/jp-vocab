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
p.write_text(t, encoding="utf-8")
print("patched", p.stat().st_size, "againBtn" not in t)
