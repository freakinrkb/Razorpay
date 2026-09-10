let DATA = null;
const $ = (s) => document.querySelector(s);
let annFilter = 'All';

async function load() {
  const res = await fetch('journey.json');
  DATA = await res.json();
  $('#repoLink').href = DATA.meta.repo;
  renderStrip(); renderArch(); renderTimeline();
  renderTech(); renderAnnFilters(); renderAnn(); renderPat();
  wireModal(); observeCards(); routeHash();
}
window.addEventListener('hashchange', routeHash);

function renderStrip() {
  const items = DATA.phases.map(p => `<span><b>${p.id.replace('phase-','P')}</b> ${p.title.split('—')[1] || p.title} →</span>`).join('');
  $('#flowTrack').innerHTML = items + items; // loop seamlessly
}

const COLS = [70, 360, 650, 940], ROWS = [70, 230, 390, 540, 700], W = 220, H = 74;

function nodeById(id){ return DATA.nodes.find(n=>n.id===id); }

function renderArch() {
  const svg = $('#archSvg');
  const edges = [
    ['auth','ratelimit',''], ['ratelimit','idempotency',''],
    ['idempotency','order',''], ['customer','order',''],
    ['order','payment-gw',''], ['payment-gw','simulator','slow'],
    ['payment-gw','outbox',''], ['vault','payment-gw','slow'],
    ['outbox','kafka',''], ['kafka','webhookdlv',''],
    ['webhookcfg','webhookdlv','slow'], ['webhookdlv','dlq',''],
    ['payment-gw','refundsettle','backlog'], ['order','refundsettle','backlog'],
    ['auth','infracore','slow'], ['outbox','infracore','slow'],
    ['apigw','auth',''], ['discovery','apigw','slow'], ['configsvr','discovery','slow'],
    ['payment-gw','settle','backlog'], ['settle','kafka',''],
  ];
  let defs = `<defs><linearGradient id="edgeGrad" x1="0" y1="0" x2="1" y2="0">
    <stop offset="0" stop-color="#7c8cff"/><stop offset="1" stop-color="#38e1c6"/></linearGradient></defs>`;
  let eSvg = edges.map(([a,b,cls]) => {
    const A = nodeById(a), B = nodeById(b);
    const x1 = COLS[A.col]+W/2, y1 = ROWS[A.row]+H/2, x2 = COLS[B.col]+W/2, y2 = ROWS[B.row]+H/2;
    const mx = (x1+x2)/2;
    return `<path class="edge ${cls}" d="M${x1},${y1} C${mx},${y1} ${mx},${y2} ${x2},${y2}"/>`;
  }).join('');
  let nSvg = DATA.nodes.map(n => {
    const x = COLS[n.col], y = ROWS[n.row];
    const cls = n.phase==='phase-4' ? 'wip' : (n.phase==='phase-5' ? 'next' : '');
    const warn = (n.id==='simulator'||n.id==='order') ? ' warn' : '';
    return `<g class="anode ${cls}${warn}" data-node="${n.id}" transform="translate(${x},${y})">
      <rect width="${W}" height="${H}" rx="14"></rect>
      <text x="16" y="30">${n.label}</text>
      <text class="sub" x="16" y="52">${n.sub}</text>
    </g>`;
  }).join('');
  svg.innerHTML = defs + eSvg + nSvg;
  svg.querySelectorAll('.anode').forEach(g => g.addEventListener('click', () => openNode(g.dataset.node)));
  $('#archCards').innerHTML = DATA.nodes.map(n =>
    `<button data-node="${n.id}">▸ ${n.label}</button>`).join('');
  document.querySelectorAll('#archCards button').forEach(b =>
    b.addEventListener('click', () => openNode(b.dataset.node)));
}

function renderTimeline() {
  const el = $('#timelineEl');
  el.innerHTML = DATA.phases.map(p => `
    <button class="tcard" id="${p.id}" data-status="${p.status}" data-phase="${p.id}">
      <div class="meta"><span class="pill ${p.status}">${p.status==='wip'?'● YOU ARE HERE':p.status}</span><span>${p.date}</span><span>${p.commit}</span><span>${p.stats}</span></div>
      <h3>${p.title}</h3>
      <p class="tag">${p.tagline}</p>
      <p>${p.recall}</p>
      <span class="go">Click for files + how to replay →</span>
    </button>`).join('');
  el.querySelectorAll('.tcard').forEach(c => c.addEventListener('click', () => openPhase(c.dataset.phase)));
  const io = new IntersectionObserver(es => es.forEach(e => {
    if (e.isIntersecting) { e.target.classList.add('visible'); io.unobserve(e.target); }
  }), {threshold:.12});
  el.querySelectorAll('.tcard').forEach(c => io.observe(c));
}

/* ---- v2: tech / annotations / patterns ---- */

function phaseTitle(pid){
  const p = (DATA.phases||[]).find(x=>x.id===pid);
  return p ? p.title : pid;
}

function renderTech() {
  $('#techGrid').innerHTML = (DATA.tech||[]).map(t => `
    <button class="gcard" id="${t.id}" data-open="tech:${t.id}">
      <h3>${t.name}</h3>
      <p class="why">${t.why}</p>
      <span class="ref">${t.ref}</span>
    </button>`).join('');
}

function renderAnnFilters() {
  const groups = ['All', ...new Set((DATA.annotations||[]).map(a=>a.group))];
  $('#annFilters').innerHTML = groups.map(g =>
    `<button data-f="${g}" class="${g===annFilter?'on':''}">${g}</button>`).join('');
  document.querySelectorAll('#annFilters button').forEach(b =>
    b.addEventListener('click', () => {
      annFilter = b.dataset.f;
      document.querySelectorAll('#annFilters button').forEach(x=>x.classList.toggle('on', x===b));
      renderAnn(); observeCards();
    }));
}

function renderAnn() {
  const list = (DATA.annotations||[]).filter(a => annFilter==='All' || a.group===annFilter);
  $('#annGrid').innerHTML = list.map(a => `
    <button class="gcard" id="${a.id}" data-open="ann:${a.id}">
      <span class="grp">${a.group}</span>
      <h3>${a.title}</h3>
      <p class="why">${a.body}</p>
      <span class="ref">${(a.examples||[])[0]||''}</span>
    </button>`).join('');
}

function renderPat() {
  $('#patGrid').innerHTML = (DATA.patterns||[]).map(p => `
    <button class="gcard" id="${p.id}" data-open="pat:${p.id}">
      <span class="grp">${phaseTitle(p.phase)}</span>
      <h3>${p.title}</h3>
      <p class="why">${p.problem}</p>
    </button>`).join('');
  document.querySelectorAll('[data-open]').forEach(el =>
    el.addEventListener('click', () => {
      const [kind, id] = el.dataset.open.split(':');
      if (kind==='tech') openTech(id);
      if (kind==='ann') openAnn(id);
      if (kind==='pat') openPat(id);
    }));
}

function observeCards(){
  const io = new IntersectionObserver(es => es.forEach(e => {
    if (e.isIntersecting) { e.target.classList.add('visible'); io.unobserve(e.target); }
  }), {threshold:.08});
  document.querySelectorAll('.gcard:not(.visible)').forEach(c => io.observe(c));
}

/* ---- modal with tabs ---- */

function wireModal() {
  $('#modalX').onclick = closeModal;
  $('#modalBackdrop').addEventListener('click', e => { if (e.target.id==='modalBackdrop') closeModal(); });
  document.addEventListener('keydown', e => { if (e.key==='Escape') closeModal(); });
}

function openModal({kicker,title,tag,tabs,hash}) {
  $('#mKicker').textContent = kicker; $('#mTitle').textContent = title;
  $('#mTag').textContent = tag || '';
  const body = tabs.map((t,i) => `
    <div class="tabpane" data-pane="${i}" ${i>0?'hidden':''}>${t.html}</div>`).join('');
  const tabBtns = tabs.map((t,i) =>
    `<button data-tab="${i}" class="${i===0?'on':''}">${t.label}</button>`).join('');
  $('#mBody').innerHTML = `<div class="tabs">${tabBtns}</div>${body}`;
  $('#mRecall').innerHTML = '';
  $('#mFiles').innerHTML = '';
  $('#mHowtoWrap').style.display = 'none';
  document.querySelectorAll('#mBody [data-tab]').forEach(b =>
    b.addEventListener('click', () => {
      document.querySelectorAll('#mBody [data-tab]').forEach(x=>x.classList.toggle('on', x===b));
      document.querySelectorAll('#mBody [data-pane]').forEach(p=>{ p.hidden = p.dataset.pane !== b.dataset.tab; });
    }));
  $('#modalBackdrop').hidden = false;
  if (hash) location.hash = '/' + hash;
}
function closeModal(){ $('#modalBackdrop').hidden = true; history.replaceState(null,'',location.pathname+location.search); }

function recallHtml(r){ return r ? `🧠 <strong>Recall:</strong> ${r}` : ''; }
function filesHtml(fs){ return (fs||[]).map(f=>`<li><code>${f}</code></li>`).join(''); }
function howtoHtml(h){ return (h||[]).map(x=>`<li>${x}</li>`).join(''); }

function openPhase(id) {
  const p = DATA.phases.find(x=>x.id===id); if(!p) return;
  const relT = (DATA.tech||[]).filter(t => (p.nodes||[]).some(n => (t.why+t.ref).includes(n)) ).slice(0,4);
  openModal({kicker:`${p.date} · ${p.commit} · ${p.stats}`, title:p.title, tag:p.tagline, hash:id, tabs:[
    {label:'Explanation', html:`<p>${p.recall}</p>${(p.checklist||[]).length?`<h4>Sunday checklist</h4><ul class="checklist">${p.checklist.map(c=>`<li>${c}</li>`).join('')}</ul>`:''}${relT.length?`<h4>Key tech in this phase</h4><ul>${relT.map(t=>`<li><strong>${t.name}</strong> — ${t.why}</li>`).join('')}</ul>`:''}`},
    {label:'Files', html:`<ul class="file-list">${filesHtml(p.files)}</ul>`},
    {label:'Replay', html:`<ol class="howto">${howtoHtml(p.howto)}</ol>`},
  ]});
}
function openNode(id) {
  const n = nodeById(id); if(!n) return;
  const ph = DATA.phases.find(x=>x.id===n.phase);
  document.querySelectorAll('.anode').forEach(g=>g.classList.remove('active'));
  document.querySelector(`.anode[data-node="${id}"]`)?.classList.add('active');
  openModal({kicker:`System map · belongs to ${ph?ph.title:'—'}`, title:n.label+' — '+n.sub, tag:'', tabs:[
    {label:'Explanation', html:`<p>${n.body}</p><div class="modal-recall">${recallHtml(n.recall)}</div>`},
    {label:'Files', html:`<ul class="file-list">${filesHtml((n.files||[]).map(f=>'src/main/java/com/example/razorpay/'+f))}</ul>`},
    ...(ph?.howto?.length ? [{label:'Replay', html:`<ol class="howto">${howtoHtml(ph.howto)}</ol>`}] : []),
  ]});
}
function openTech(id) {
  const t = (DATA.tech||[]).find(x=>x.id===id); if(!t) return;
  openModal({kicker:'Tech stack · what · why · where', title:t.name, tag:t.witty||'', hash:id, tabs:[
    {label:'Explanation', html:`<p><strong>Why here:</strong> ${t.why}</p>`},
    {label:'Where', html:`<p><code>${t.ref}</code></p>`},
  ]});
}
function openAnn(id) {
  const a = (DATA.annotations||[]).find(x=>x.id===id); if(!a) return;
  openModal({kicker:`Annotation · ${a.group}`, title:a.title, tag:'', hash:id, tabs:[
    {label:'Explanation', html:`<p>${a.body}</p><p><strong>Why in this project:</strong> ${a.why}</p>${a.pitfall?`<div class="pit">⚠️ ${a.pitfall}</div>`:''}`},
    {label:'Proof', html:`<ul class="file-list">${filesHtml(a.examples)}</ul>`},
  ]});
}
function openPat(id) {
  const p = (DATA.patterns||[]).find(x=>x.id===id); if(!p) return;
  openModal({kicker:`Design pattern · ${phaseTitle(p.phase)}`, title:p.title, tag:'', hash:id, tabs:[
    {label:'Explanation', html:`<p><strong>Problem it kills here:</strong> ${p.problem}</p>`},
    {label:'Files', html:`<ul class="file-list">${filesHtml(p.files)}</ul>`},
  ]});
}
function routeHash() {
  const h = location.hash.replace('#/','');
  if (!h || ['map','timeline','tech','annotations','patterns','extend'].includes(h)) return;
  if (DATA.phases.some(p=>p.id===h)) {
    document.getElementById(h)?.scrollIntoView({behavior:'smooth',block:'center'});
    openPhase(h); return;
  }
  if ((DATA.tech||[]).some(t=>t.id===h)) {
    document.getElementById(h)?.scrollIntoView({behavior:'smooth',block:'center'});
    openTech(h); return;
  }
  if ((DATA.annotations||[]).some(a=>a.id===h)) {
    document.getElementById(h)?.scrollIntoView({behavior:'smooth',block:'center'});
    openAnn(h); return;
  }
  if ((DATA.patterns||[]).some(p=>p.id===h)) {
    document.getElementById(h)?.scrollIntoView({behavior:'smooth',block:'center'});
    openPat(h); return;
  }
}
load();
