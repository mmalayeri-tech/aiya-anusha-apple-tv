package app.aiya.tv.server

/** The page the iPhone sees. Self-contained, styled to match the TV's LCARS look. */
object UploadPage {
    fun html(tvName: String): String = """
<!doctype html>
<html lang="en"><head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Send to ${esc(tvName)}</title>
<style>
:root{--o:#FF9900;--p:#CC99CC;--b:#9999FF;--t:#FFCC99;--r:#CC6666}
*{box-sizing:border-box}
body{margin:0;background:#000;color:var(--t);font:600 17px/1.35 "Helvetica Neue",Arial Narrow,sans-serif;letter-spacing:.04em;text-transform:uppercase}
.top{display:flex;height:64px;margin:12px 12px 0}
.elbow{width:96px;background:var(--o);border-top-left-radius:40px}
.bar{flex:1;height:22px;background:var(--o);margin-left:6px}
.cap{width:24px;height:22px;background:var(--b);border-radius:0 11px 11px 0;margin-left:6px}
.wrap{display:flex;margin:0 12px}
.side{width:96px;display:flex;flex-direction:column;gap:5px}
.side div{height:46px;background:var(--p)}.side div:nth-child(2){background:var(--b)}.side div:nth-child(3){background:var(--r);flex:1;min-height:80px}
main{flex:1;padding:4px 0 24px 14px;min-width:0}
h1{margin:0 0 4px;color:var(--o);font-size:28px}
p{margin:4px 0 14px;color:#c9a27a;font-size:14px}
.btn{display:block;width:100%;border:0;border-radius:999px;padding:16px 22px;margin:10px 0;font:inherit;font-size:19px;text-align:right;color:#000;background:var(--o);cursor:pointer}
.btn.b{background:var(--b)}.btn.p{background:var(--p)}
input[type=url]{width:100%;padding:14px 16px;border-radius:12px;border:2px solid var(--b);background:#110d18;color:var(--t);font:inherit;font-size:16px;text-transform:none}
#log{margin-top:10px;font-size:14px}
.row{display:flex;justify-content:space-between;gap:8px;padding:6px 0;border-bottom:1px solid #332}
.row span:first-child{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;text-transform:none}
.ok{color:#9f9}.err{color:#f77}
progress{width:100%;height:14px;accent-color:var(--o)}
</style></head><body>
<div class="top"><div class="elbow"></div><div class="bar"></div><div class="cap"></div></div>
<div class="wrap"><div class="side"><div></div><div></div><div></div></div>
<main>
<h1>Send to TV</h1>
<p>${esc(tvName)} · photos, videos and links appear on the big screen</p>
<input id="pick" type="file" accept="image/*,video/*,audio/*" multiple hidden>
<button class="btn" onclick="pick.click()">Photos &amp; videos</button>
<form id="lf"><input id="url" type="url" placeholder="Paste a link (YouTube, Apple TV, web)" autocomplete="off">
<button class="btn b" type="submit">Open link on TV</button></form>
<progress id="bar" value="0" max="1" hidden></progress>
<div id="log"></div>
</main></div>
<script>
const log=document.getElementById('log'),bar=document.getElementById('bar');
function line(name,msg,cls){const d=document.createElement('div');d.className='row';d.innerHTML='<span></span><span class="'+cls+'"></span>';d.children[0].textContent=name;d.children[1].textContent=msg;log.prepend(d);return d}
function send(file){return new Promise(res=>{const r=line(file.name,'0%','');const x=new XMLHttpRequest();x.open('POST','/upload');x.setRequestHeader('X-Filename',encodeURIComponent(file.name));
x.upload.onprogress=e=>{if(e.lengthComputable){bar.value=e.loaded/e.total;r.children[1].textContent=Math.round(100*e.loaded/e.total)+'%'}};
x.onload=()=>{const ok=x.status==200;r.children[1].textContent=ok?'On TV':'Failed';r.children[1].className=ok?'ok':'err';res()};
x.onerror=()=>{r.children[1].textContent='Failed';r.children[1].className='err';res()};x.send(file)})}
document.getElementById('pick').onchange=async e=>{bar.hidden=false;for(const f of e.target.files){await send(f)}bar.hidden=true;e.target.value=''};
document.getElementById('lf').onsubmit=async e=>{e.preventDefault();const u=document.getElementById('url').value.trim();if(!u)return;
const r=await fetch('/open',{method:'POST',body:u}).catch(()=>null);line(u,r&&r.ok?'Opened':'Failed',r&&r.ok?'ok':'err')};
</script></body></html>
""".trimIndent()

    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
