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
p.write_text(t, encoding="utf-8")
print("patched", p.stat().st_size)
