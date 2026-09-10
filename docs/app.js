let DATA = null;
const $ = (s) => document.querySelector(s);

async function load() {
  const res = await fetch('journey.json');
  DATA = await res.json();
  $('#repoLink').href = DATA.meta.repo;
  renderStrip(); renderArch(); renderTimeline(); wireModal(); routeHash();
}
window.addEventListener('hashchange', routeHash);

function renderStrip() {
  const items = DATA.phases.map(p => `<span><b>${p.id.replace('phase-','P')}</b> ${p.title.split('—')[1] || p.title} →</span>`).join('');
  $('#flowTrack').innerHTML = items + items; // loop seamlessly
}

const COLS = [70, 360, 650, 940], ROWS = [70, 230, 390, 540], W = 220, H = 74;

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

function wireModal() {
  $('#modalX').onclick = closeModal;
  $('#modalBackdrop').addEventListener('click', e => { if (e.target.id==='modalBackdrop') closeModal(); });
  document.addEventListener('keydown', e => { if (e.key==='Escape') closeModal(); });
}
function openModal({kicker,title,tag,body,recall,files,howto}) {
  $('#mKicker').textContent = kicker; $('#mTitle').textContent = title;
  $('#mTag').textContent = tag || ''; $('#mBody').textContent = body || '';
  $('#mRecall').innerHTML = recall ? `🧠 <strong>Recall:</strong> ${recall}` : '';
  $('#mFiles').innerHTML = (files||[]).map(f=>`<li><code>${f}</code></li>`).join('');
  const hw = $('#mHowto'); const wrap = $('#mHowtoWrap');
  if (howto && howto.length) { wrap.style.display=''; hw.innerHTML = howto.map(h=>`<li>${h}</li>`).join(''); }
  else wrap.style.display='none';
  $('#modalBackdrop').hidden = false;
}
function closeModal(){ $('#modalBackdrop').hidden = true; history.replaceState(null,'',location.pathname+location.search); }
function openPhase(id) {
  const p = DATA.phases.find(x=>x.id===id); if(!p) return;
  openModal({kicker:`${p.date} · ${p.commit} · ${p.stats}`, title:p.title, tag:p.tagline,
    body:'', recall:p.recall, files:p.files, howto:p.howto});
  location.hash = '/' + id;
}
function openNode(id) {
  const n = nodeById(id); if(!n) return;
  const ph = DATA.phases.find(x=>x.id===n.phase);
  document.querySelectorAll('.anode').forEach(g=>g.classList.remove('active'));
  document.querySelector(`.anode[data-node="${id}"]`)?.classList.add('active');
  openModal({kicker:`System map · belongs to ${ph?ph.title:'—'}`, title:n.label+' — '+n.sub,
    tag:'', body:n.body, recall:n.recall, files:(n.files||[]).map(f=>'src/main/java/com/example/razorpay/'+f), howto:ph?.howto});
}
function routeHash() {
  const h = location.hash.replace('#/','');
  if (!h || h==='map' || h==='timeline') return;
  if (DATA.phases.some(p=>p.id===h)) {
    document.getElementById(h)?.scrollIntoView({behavior:'smooth',block:'center'});
    openPhase(h);
  }
}
load();
