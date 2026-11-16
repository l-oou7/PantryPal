/* PantryPal illustrations: chef mascot, kawaii food characters, dish art and pixel icons.
   Everything is inline SVG so it scales crisply and needs no image files. */
(function () {
  const C = {
    mint: '#B8E3D3', sage: '#80BFA5', cream: '#FFF4D6', coral: '#FF9B8E',
    butter: '#FFD65A', lav: '#BDA7E8', forest: '#245C45', white: '#FFFFFF', blush: '#FF9B8E'
  };
  const OUT = C.forest;

  /* ---------- tiny face shared by all kawaii foods ---------- */
  function face(cx, cy, s = 1, mood = 'happy') {
    const ex = 7 * s, ey = cy, r = 2.2 * s;
    let eyes;
    if (mood === 'sleepy') {
      eyes = `<path d="M${cx - ex - 2.5 * s} ${ey} q${2.5 * s} ${2 * s} ${5 * s} 0 M${cx + ex - 2.5 * s} ${ey} q${2.5 * s} ${2 * s} ${5 * s} 0" stroke="${OUT}" stroke-width="${1.6 * s}" fill="none" stroke-linecap="round"/>`;
    } else if (mood === 'worried') {
      eyes = `<circle cx="${cx - ex}" cy="${ey}" r="${r}" fill="${OUT}"/><circle cx="${cx + ex}" cy="${ey}" r="${r}" fill="${OUT}"/>
              <path d="M${cx - ex - 3 * s} ${ey - 5 * s} l${5 * s} ${1.5 * s} M${cx + ex + 3 * s} ${ey - 5 * s} l${-5 * s} ${1.5 * s}" stroke="${OUT}" stroke-width="${1.3 * s}" stroke-linecap="round"/>`;
    } else {
      eyes = `<circle cx="${cx - ex}" cy="${ey}" r="${r}" fill="${OUT}"/><circle cx="${cx + ex}" cy="${ey}" r="${r}" fill="${OUT}"/>
              <circle cx="${cx - ex + 0.8 * s}" cy="${ey - 0.8 * s}" r="${0.7 * s}" fill="#fff"/><circle cx="${cx + ex + 0.8 * s}" cy="${ey - 0.8 * s}" r="${0.7 * s}" fill="#fff"/>`;
    }
    const mouth = mood === 'worried'
      ? `<path d="M${cx - 2.5 * s} ${cy + 6 * s} q${2.5 * s} ${-2 * s} ${5 * s} 0" stroke="${OUT}" stroke-width="${1.5 * s}" fill="none" stroke-linecap="round"/>`
      : `<path d="M${cx - 3 * s} ${cy + 4 * s} q${3 * s} ${3.5 * s} ${6 * s} 0" stroke="${OUT}" stroke-width="${1.5 * s}" fill="none" stroke-linecap="round"/>`;
    const cheeks = `<ellipse cx="${cx - ex - 3.5 * s}" cy="${cy + 3.5 * s}" rx="${2.6 * s}" ry="${1.6 * s}" fill="${C.blush}" opacity=".75"/>
                    <ellipse cx="${cx + ex + 3.5 * s}" cy="${cy + 3.5 * s}" rx="${2.6 * s}" ry="${1.6 * s}" fill="${C.blush}" opacity=".75"/>`;
    return eyes + cheeks + mouth;
  }
  const sw = 'stroke="' + OUT + '" stroke-width="2.5" stroke-linejoin="round"';

  /* ---------- kawaii food characters (viewBox 0 0 80 80) ---------- */
  const foods = {
    strawberry: (m) => `<path d="M40 70 C18 60 12 36 22 26 C30 20 50 20 58 26 C68 36 62 60 40 70Z" fill="#FF6F6F" ${sw}/>
      <g fill="${C.cream}">${[[28, 34], [40, 30], [52, 34], [33, 52], [47, 52], [40, 61]].map(([x, y]) => `<ellipse cx="${x}" cy="${y}" rx="1.4" ry="2"/>`).join('')}</g>
      <path d="M40 24 L30 14 L38 18 L40 9 L42 18 L50 14Z" fill="${C.sage}" ${sw}/>${face(40, 42, 1, m)}`,
    carrot: (m) => `<path d="M24 26 Q40 18 56 26 L44 72 Q40 76 36 72Z" fill="#FF9A3C" ${sw}/>
      <path d="M33 40h6M42 54h5M36 63h4" stroke="${OUT}" stroke-width="2" stroke-linecap="round"/>
      <path d="M40 22 C34 10 30 8 28 12 M40 22 C40 8 42 5 45 8 M40 22 C46 12 52 10 52 15" stroke="${C.sage}" stroke-width="5" fill="none" stroke-linecap="round"/>${face(40, 33, .9, m)}`,
    tomato: (m) => `<circle cx="40" cy="45" r="26" fill="#FF5A4E" ${sw}/>
      <path d="M40 22 L32 16 L38 19 L40 12 L42 19 L48 16Z" fill="${C.sage}" ${sw}/>
      <ellipse cx="30" cy="34" rx="5" ry="3" fill="#fff" opacity=".45"/>${face(40, 46, 1.1, m)}`,
    broccoli: (m) => `<path d="M33 46 L31 72 L49 72 L47 46Z" fill="#A8D672" ${sw}/>
      <g fill="#5BAA4A" ${sw}><circle cx="26" cy="36" r="13"/><circle cx="54" cy="36" r="13"/><circle cx="40" cy="26" r="15"/></g>
      ${face(40, 58, .8, m)}`,
    egg: (m) => `<path d="M40 10 C22 10 14 44 18 56 C22 70 58 70 62 56 C66 44 58 10 40 10Z" fill="${C.white}" ${sw}/>${face(40, 46, 1, m)}`,
    friedEgg: (m) => `<path d="M14 44 C10 28 30 18 42 22 C58 16 72 30 66 46 C70 62 48 68 38 64 C22 70 10 58 14 44Z" fill="#fff" ${sw}/>
      <circle cx="40" cy="43" r="13" fill="${C.butter}" ${sw}/>${face(40, 43, .8, m)}`,
    mushroom: (m) => `<path d="M32 46 L30 70 Q40 74 50 70 L48 46Z" fill="${C.cream}" ${sw}/>
      <path d="M10 46 C10 20 70 20 70 46Z" fill="#C97B5A" ${sw}/><g fill="${C.cream}"><circle cx="28" cy="34" r="4"/><circle cx="48" cy="30" r="3"/><circle cx="58" cy="40" r="3"/></g>
      ${face(40, 58, .8, m)}`,
    avocado: (m) => `<path d="M40 8 C26 8 16 36 16 50 C16 66 28 74 40 74 C52 74 64 66 64 50 C64 36 54 8 40 8Z" fill="#5E8C3A" ${sw}/>
      <path d="M40 16 C30 16 23 38 23 50 C23 62 31 67 40 67 C49 67 57 62 57 50 C57 38 50 16 40 16Z" fill="#D7EFA0"/>
      <circle cx="40" cy="52" r="9" fill="#9C6644" ${sw}/>${face(40, 34, .8, m)}`,
    bread: (m) => `<path d="M14 36 C10 18 70 18 66 36 L62 70 L18 70Z" fill="#E9B872" ${sw}/>
      <path d="M22 40 L58 40 L56 66 L24 66Z" fill="#FFE8C2"/>${face(40, 50, 1, m)}`,
    milk: (m) => `<path d="M24 26 L40 14 L56 26 L56 72 L24 72Z" fill="#fff" ${sw}/><path d="M24 26 L56 26" ${sw}/>
      <rect x="24" y="36" width="32" height="14" fill="${C.mint}"/>${face(40, 58, .9, m)}`,
    cheese: (m) => `<path d="M10 56 L66 30 L70 58 L10 66Z" fill="${C.butter}" ${sw}/><circle cx="54" cy="46" r="4" fill="#E8B830"/><circle cx="26" cy="58" r="3" fill="#E8B830"/>${face(42, 52, .8, m)}`,
    drumstick: (m) => `<path d="M30 20 C46 8 70 22 58 42 C52 52 40 52 34 48 L22 60 Z" fill="#D9874A" ${sw}/>
      <path d="M34 48 L20 62" stroke="${C.cream}" stroke-width="7" stroke-linecap="round"/><circle cx="16" cy="64" r="5" fill="${C.cream}" ${sw}/><circle cx="20" cy="70" r="5" fill="${C.cream}" ${sw}/>${face(44, 32, .8, m)}`,
    fish: (m) => `<path d="M12 40 C24 18 54 18 62 40 C54 62 24 62 12 40Z" fill="${C.coral}" ${sw}/><path d="M62 40 L74 28 L74 52Z" fill="${C.coral}" ${sw}/>${face(36, 40, .85, m)}`,
    riceBag: (m) => `<path d="M20 24 Q40 12 60 24 L64 70 L16 70Z" fill="${C.cream}" ${sw}/><path d="M24 24 Q40 30 56 24" stroke="${OUT}" stroke-width="2" fill="none"/>${face(40, 48, 1, m)}`,
    jar: (m) => `<rect x="22" y="22" width="36" height="50" rx="8" fill="${C.lav}" ${sw}/><rect x="26" y="12" width="28" height="12" rx="3" fill="${C.coral}" ${sw}/>${face(40, 48, 1, m)}`,
    can: (m) => `<rect x="20" y="16" width="40" height="56" rx="6" fill="#9FC9E8" ${sw}/><rect x="20" y="30" width="40" height="24" fill="${C.butter}"/><path d="M20 30h40M20 54h40" ${sw}/>${face(40, 42, .9, m)}`,
    banana: (m) => `<path d="M14 24 C14 56 40 70 66 58 C58 54 36 54 24 22Z" fill="${C.butter}" ${sw}/><path d="M14 24 l-2 -6" ${sw}/>${face(34, 48, .75, m)}`,
    apple: (m) => `<path d="M40 24 C24 14 10 30 16 50 C20 66 32 72 40 66 C48 72 60 66 64 50 C70 30 56 14 40 24Z" fill="#8FD16A" ${sw}/><path d="M40 24 L42 12" ${sw}/><path d="M42 14 C50 8 56 12 54 16 C48 18 44 16 42 14Z" fill="${C.sage}" ${sw}/>${face(40, 44, 1, m)}`,
    lemon: (m) => `<ellipse cx="40" cy="42" rx="28" ry="22" fill="${C.butter}" ${sw}/>${face(40, 42, 1, m)}`,
    onion: (m) => `<path d="M40 14 C30 30 14 36 18 54 C22 70 58 70 62 54 C66 36 50 30 40 14Z" fill="#E7C6E8" ${sw}/><path d="M40 14 L38 6 M40 14 L44 7" ${sw}/>${face(40, 50, .9, m)}`,
    garlic: (m) => `<path d="M40 14 C26 30 12 40 18 56 C24 70 56 70 62 56 C68 40 54 30 40 14Z" fill="#FFF8EC" ${sw}/><path d="M40 20 C36 40 36 56 40 66 M40 20 C44 40 44 56 40 66" stroke="${OUT}" stroke-width="1.5" fill="none" opacity=".5"/>${face(40, 50, .85, m)}`,
    leaf: (m) => `<path d="M14 66 C14 30 40 12 68 12 C68 44 50 66 14 66Z" fill="#6CC070" ${sw}/><path d="M18 62 L56 24" stroke="${OUT}" stroke-width="2"/>${face(44, 44, .8, m)}`,
    pepper: (m) => `<path d="M22 30 C16 50 22 70 40 70 C58 70 64 50 58 30 C50 24 30 24 22 30Z" fill="#FF6B5E" ${sw}/><path d="M40 26 L40 14 L48 12" ${sw} fill="none"/>${face(40, 46, .95, m)}`,
    potato: (m) => `<ellipse cx="40" cy="44" rx="28" ry="22" fill="#D9A867" ${sw}/><circle cx="26" cy="36" r="1.6" fill="${OUT}"/><circle cx="56" cy="52" r="1.6" fill="${OUT}"/>${face(40, 44, 1, m)}`,
    bottle: (m) => `<path d="M32 10 h16 v12 c10 4 12 12 12 20 v28 h-40 v-28 c0 -8 2 -16 12 -20Z" fill="#C9E28A" ${sw}/>${face(40, 52, .9, m)}`
  };

  /* name keyword -> character, then category fallback */
  const byName = [
    [/strawberr|berr|raspberr|cherr/, 'strawberry'], [/carrot/, 'carrot'], [/tomato/, 'tomato'],
    [/broccoli|cauliflower/, 'broccoli'], [/egg(?!plant)/, 'egg'], [/mushroom/, 'mushroom'], [/avocado/, 'avocado'],
    [/bread|toast|bun|tortilla|pita/, 'bread'], [/milk|cream|yogurt|yoghurt/, 'milk'],
    [/cheese|feta|cheddar|parmesan|mozzarella|butter/, 'cheese'], [/chicken|beef|pork|lamb|turkey|bacon|sausage|mince/, 'drumstick'],
    [/salmon|tuna|fish|shrimp|prawn|cod/, 'fish'], [/rice|flour|oat|pasta|noodle|lentil|quinoa|sugar|grain/, 'riceBag'],
    [/banana/, 'banana'], [/apple|pear/, 'apple'], [/lemon|lime|orange/, 'lemon'], [/onion|shallot/, 'onion'], [/garlic/, 'garlic'],
    [/spinach|lettuce|kale|basil|parsley|cilantro|herb|cabbage/, 'leaf'], [/pepper|chili|chilli/, 'pepper'],
    [/potato/, 'potato'], [/oil|sauce|vinegar|syrup|honey/, 'bottle'], [/bean|chickpea|corn|can/, 'can']
  ];
  const byCategory = { FRUITS: 'apple', VEGETABLES: 'carrot', DAIRY: 'milk', MEAT_SEAFOOD: 'drumstick', GRAINS: 'riceBag',
    SPICES: 'jar', CANNED_GOODS: 'can', OTHERS: 'jar' };

  function foodKey(name, category) {
    const n = (name || '').toLowerCase();
    for (const [re, key] of byName) if (re.test(n)) return key;
    return byCategory[category] || 'jar';
  }

  function food(name, category, mood = 'happy', size = 56) {
    const key = foods[name] ? name : foodKey(name, category);
    return `<svg class="food-svg" viewBox="0 0 80 80" width="${size}" height="${size}" aria-hidden="true">${foods[key](mood)}</svg>`;
  }

  /* ---------- the chef mascot ---------- */
  function mascot(mood = 'happy', size = 160) {
    let eyes, mouth, extra = '';
    if (mood === 'excited') {
      eyes = `<path d="M70 92 l4 -6 l4 6 M102 92 l4 -6 l4 6" stroke="${OUT}" stroke-width="3.2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`;
      mouth = `<path d="M80 104 q10 14 20 0Z" fill="${C.coral}" stroke="${OUT}" stroke-width="3" stroke-linejoin="round"/>`;
      extra = `<g fill="${C.butter}" stroke="${OUT}" stroke-width="2">${star(28, 60, 8)}${star(152, 48, 7)}</g>`;
    } else if (mood === 'worried' || mood === 'alert') {
      eyes = `<circle cx="74" cy="90" r="5" fill="${OUT}"/><circle cx="106" cy="90" r="5" fill="${OUT}"/>
              <circle cx="76" cy="88" r="1.6" fill="#fff"/><circle cx="108" cy="88" r="1.6" fill="#fff"/>`;
      mouth = mood === 'worried'
        ? `<path d="M82 108 q8 -6 16 0" stroke="${OUT}" stroke-width="3" fill="none" stroke-linecap="round"/>`
        : `<ellipse cx="90" cy="106" rx="5" ry="6" fill="${C.coral}" stroke="${OUT}" stroke-width="3"/>`;
      extra = mood === 'worried'
        ? `<path d="M128 70 q6 10 0 14 q-6 -4 0 -14Z" fill="#9FD8F0" stroke="${OUT}" stroke-width="2"/>`
        : `<path d="M140 52 v14 M140 74 v3" stroke="${C.coral}" stroke-width="5" stroke-linecap="round"/>`;
    } else {
      eyes = `<path d="M68 92 q6 -7 12 0 M100 92 q6 -7 12 0" stroke="${OUT}" stroke-width="3.2" fill="none" stroke-linecap="round"/>`;
      mouth = `<path d="M82 102 q8 9 16 0" stroke="${OUT}" stroke-width="3.2" fill="none" stroke-linecap="round"/>`;
    }
    return `<svg class="mascot mascot-${mood}" viewBox="0 0 180 200" width="${size}" height="${size * 200 / 180}" role="img" aria-label="PantryPal chef">
      ${extra}
      <g class="mascot-hat">
        <path d="M52 64 C30 62 30 30 54 32 C56 12 84 6 92 22 C102 8 130 14 126 36 C150 34 150 64 128 64Z" fill="#fff" stroke="${OUT}" stroke-width="3.5" stroke-linejoin="round"/>
        <rect x="52" y="56" width="76" height="16" rx="4" fill="#fff" stroke="${OUT}" stroke-width="3.5"/>
        <path d="M74 40 v12 M92 34 v18 M110 40 v12" stroke="${C.mint}" stroke-width="3" stroke-linecap="round"/>
      </g>
      <path d="M50 178 C46 148 60 132 90 132 C120 132 134 148 130 178Z" fill="#fff" stroke="${OUT}" stroke-width="3.5" stroke-linejoin="round"/>
      <path d="M78 134 L90 150 L102 134" fill="${C.coral}" stroke="${OUT}" stroke-width="3" stroke-linejoin="round"/>
      <circle cx="90" cy="160" r="3" fill="${OUT}"/><circle cx="90" cy="170" r="3" fill="${OUT}"/>
      <ellipse cx="90" cy="98" rx="46" ry="38" fill="#FFE3C8" stroke="${OUT}" stroke-width="3.5"/>
      <ellipse cx="62" cy="106" rx="8" ry="5" fill="${C.coral}" opacity=".7"/><ellipse cx="118" cy="106" rx="8" ry="5" fill="${C.coral}" opacity=".7"/>
      ${eyes}${mouth}
      <g class="mascot-spoon"><path d="M140 176 L156 124" stroke="${OUT}" stroke-width="5" stroke-linecap="round"/>
      <ellipse cx="158" cy="116" rx="8" ry="11" transform="rotate(20 158 116)" fill="${C.butter}" stroke="${OUT}" stroke-width="3"/></g>
      <circle cx="136" cy="168" r="9" fill="#FFE3C8" stroke="${OUT}" stroke-width="3"/>
    </svg>`;
  }

  function star(x, y, r) {
    const pts = [];
    for (let i = 0; i < 10; i++) {
      const a = Math.PI / 5 * i - Math.PI / 2, rr = i % 2 ? r * .45 : r;
      pts.push((x + rr * Math.cos(a)).toFixed(1) + ',' + (y + rr * Math.sin(a)).toFixed(1));
    }
    return `<polygon points="${pts.join(' ')}" stroke-linejoin="round"/>`;
  }

  /* ---------- dish illustrations: "bowl:#c1,#c2,#c3" ---------- */
  function dish(spec, size = 120) {
    const [type, cols] = (spec || 'bowl:#F2C14E,#E4572E,#76B041').split(':');
    const [a, b, c] = (cols || '').split(',');
    const s = 'stroke="' + OUT + '" stroke-width="3" stroke-linejoin="round" stroke-linecap="round"';
    const blobs = (cx, cy, w) => `<circle cx="${cx - w * .45}" cy="${cy}" r="${w * .16}" fill="${b}" ${s}/>
      <circle cx="${cx + w * .05}" cy="${cy - w * .08}" r="${w * .14}" fill="${c}" ${s}/>
      <circle cx="${cx + w * .45}" cy="${cy + w * .02}" r="${w * .12}" fill="${b}" ${s}/>
      <path d="M${cx - w * .15} ${cy + w * .1} q${w * .1} ${-w * .12} ${w * .2} 0" stroke="${c}" stroke-width="5" fill="none" stroke-linecap="round"/>`;
    const steam = `<g class="steam" stroke="${C.sage}" stroke-width="3" fill="none" stroke-linecap="round" opacity=".8">
      <path d="M48 22 q-6 -8 0 -14"/><path d="M60 18 q-6 -8 0 -14"/><path d="M72 22 q-6 -8 0 -14"/></g>`;
    const art = {
      bowl: `${steam}<path d="M14 56 Q60 46 106 56 Q102 96 60 98 Q18 96 14 56Z" fill="${C.cream}" ${s}/>
             <ellipse cx="60" cy="56" rx="46" ry="10" fill="${a}" ${s}/>${blobs(60, 52, 54)}
             <path d="M22 66 Q60 74 98 66" stroke="${C.mint}" stroke-width="6" fill="none"/>`,
      pot: `${steam}<rect x="20" y="44" width="80" height="52" rx="10" fill="${C.sage}" ${s}/>
             <ellipse cx="60" cy="44" rx="40" ry="9" fill="${a}" ${s}/><path d="M12 54 h8 M100 54 h8" ${s}/>${blobs(60, 42, 40)}`,
      pan: `<path d="M86 66 L114 54" stroke="${OUT}" stroke-width="7" stroke-linecap="round"/>
            <ellipse cx="54" cy="66" rx="44" ry="22" fill="#5d6b66" ${s}/><ellipse cx="54" cy="62" rx="36" ry="16" fill="${a}" ${s}/>${blobs(54, 60, 44)}`,
      plate: `<ellipse cx="60" cy="70" rx="52" ry="22" fill="#fff" ${s}/><ellipse cx="60" cy="68" rx="38" ry="14" fill="${C.mint}"/>
              <path d="M30 66 q30 -26 60 0Z" fill="${a}" ${s}/>${blobs(60, 64, 40)}`,
      stack: `<ellipse cx="60" cy="88" rx="48" ry="12" fill="#fff" ${s}/>
              ${[78, 66, 54, 42].map(y => `<ellipse cx="60" cy="${y}" rx="38" ry="10" fill="${a}" ${s}/>`).join('')}
              <path d="M40 36 q20 -10 40 0 q-4 14 -20 22 q-16 -8 -20 -22Z" fill="${b}" ${s}/>
              <rect x="52" y="24" width="16" height="10" rx="2" fill="${C.butter}" ${s}/><circle cx="84" cy="34" r="6" fill="${c}" ${s}/>`,
      toast: `<path d="M24 92 L24 46 C14 40 18 18 40 20 L80 20 C102 18 106 40 96 46 L96 92Z" fill="${b}" ${s}/>
              <path d="M32 84 L32 50 C24 44 28 28 42 28 L78 28 C92 28 96 44 88 50 L88 84Z" fill="${a}"/>
              <circle cx="48" cy="54" r="10" fill="${c}" ${s}/><circle cx="70" cy="64" r="8" fill="${c}" ${s}/>`,
      wrap: `<path d="M16 74 C30 30 90 30 104 74 C80 88 40 88 16 74Z" fill="${a}" ${s}/>
             <path d="M30 60 q30 -26 60 0" fill="${c}" ${s}/><path d="M40 56 q20 -16 40 0" fill="${C.coral}" ${s}/><path d="M50 50 q10 -6 20 0" fill="${b}" ${s}/>`,
      salad: `<path d="M12 58 Q60 50 108 58 Q100 98 60 98 Q20 98 12 58Z" fill="#fff" ${s}/>
              ${[[30, 48, a], [50, 42, a], [72, 44, a], [88, 50, a]].map(([x, y, f]) => `<path d="M${x - 12} ${y + 10} q12 -24 24 0Z" fill="${f}" ${s}/>`).join('')}
              <circle cx="44" cy="54" r="8" fill="${b}" ${s}/><rect x="64" y="48" width="12" height="12" rx="2" fill="${c}" ${s}/>`,
      glass: `<path d="M34 20 L86 20 L78 98 L42 98Z" fill="#fff" ${s}/><path d="M38 44 L82 44 L77 94 L43 94Z" fill="${a}"/>
              <path d="M38 64 L80 64 L77 94 L43 94Z" fill="${b}"/><path d="M70 6 L62 50" stroke="${C.coral}" stroke-width="6" stroke-linecap="round"/>
              <circle cx="46" cy="36" r="7" fill="${c}" ${s}/><path d="M34 20 L86 20 L78 98 L42 98Z" fill="none" ${s}/>`,
      loaf: `<path d="M14 90 L18 52 C18 24 102 24 102 52 L106 90Z" fill="${a}" ${s}/>
             <path d="M38 40 l8 10 M56 36 l8 10 M74 40 l8 10" stroke="${b}" stroke-width="5" stroke-linecap="round"/>
             <rect x="10" y="88" width="100" height="10" rx="5" fill="${C.mint}" ${s}/>`,
      dish: `<rect x="12" y="50" width="96" height="44" rx="14" fill="${C.lav}" ${s}/>
             <path d="M18 54 Q60 36 102 54 L100 62 Q60 50 20 62Z" fill="${a}" ${s}/>
             <circle cx="40" cy="52" r="6" fill="${c}" ${s}/><circle cx="76" cy="50" r="7" fill="${b}" ${s}/><path d="M4 66 h8 M108 66 h8" ${s}/>`,
      muffin: `<path d="M30 60 L38 98 L82 98 L90 60Z" fill="${C.coral}" ${s}/><path d="M42 62 v34 M60 62 v36 M78 62 v34" stroke="${OUT}" stroke-width="2" opacity=".4"/>
               <path d="M24 62 C16 36 44 22 60 26 C78 22 104 36 96 62Z" fill="${a}" ${s}/><circle cx="48" cy="44" r="5" fill="${b}"/><circle cx="70" cy="40" r="5" fill="${c}"/>`
    };
    return `<svg class="dish-svg" viewBox="0 0 120 110" width="${size}" height="${size * 110 / 120}" aria-hidden="true">${art[type] || art.bowl}</svg>`;
  }

  /* ---------- pixel icons: each row is a string, letters map to colours ---------- */
  const PAL = { f: C.forest, c: C.coral, y: C.butter, m: C.mint, s: C.sage, l: C.lav, w: '#fff', g: '#8a9a94', r: '#E4572E' };
  const PIX = {
    pot: ['..f..f..f..', '...........', '.fffffffff.', 'ffcccccccff', '.fcccccccf.', '.fcwccccc f.'.replace(' ', 'c'), '.fcccccccf.', '.fcccccccf.', '..fffffff..'],
    pan: ['...........', '..fffff....', '.fgggggf...', 'fgyyyyygf..', 'fgywwyygfff', 'fgyyyyygfff', '.fgggggf...', '..fffff....'],
    star: ['....f....', '...fyf...', '...fyf...', 'ffffyffff', 'fyyyyyyyf', '.fyyyyyf.', '..fyfyf..', '.fyf.fyf.', '.ff...ff.'],
    heart: ['.ff...ff.', 'fccf.fccf', 'fcwccccf'.padEnd(9, 'f'), 'fccccccf'.padEnd(9, 'f'), '.fccccf'.padEnd(8, 'f').padEnd(9, '.'), '..fcccf..', '...fcf...', '....f....'],
    sparkle: ['...f...', '...y...', '..fyf..', 'fyyyyyf', '..fyf..', '...y...', '...f...'],
    bell: ['....f....', '...fff...', '..fyyyf..', '..fyyyf..', '.fyyyyyf.', '.fyyyyyf.', 'fffffffff', '...fcf...'],
    cart: ['ff........', '.f........', '.ffffffff.', '.fmmmmmmf.', '.fmmmmmmf.', '..ffffff..', '..f....f..', '.fcf..fcf.'],
    cal: ['.f.....f.', 'fffffffff', 'fcccccccf', 'fffffffff', 'fwmwmwmwf', 'fwwwwwwwf', 'fwmwmwcwf', 'fffffffff'],
    book: ['ffffffff.', 'fllllllff', 'flwwwwlff', 'fllllllff', 'flwwwlllf', 'fllllllff', 'fwwwwwwwf', 'ffffffff.'],
    leaf: ['......ff', '....ffsf', '..ffsssf', '.fsssmsf', 'fssmssf.', 'fsmssf..', 'ffsff...', 'f.......'],
    user: ['..fff..', '.fyyyf.', '.fyyyf.', '..fff..', '.fmmmf.', 'fmmmmmf', 'fmmmmmf'],
    home: ['....f....', '...fcf...', '..fcccf..', '.fcccccf.', 'fffffffff', '.fwwmwwf.', '.fwwmwwf.', '.fffffff.'],
    clock: ['..fff..', '.fwfwf.', 'fwwfwwf', 'fwwffwf', 'fwwwwwf', '.fwwwf.', '..fff..'],
    search: ['.ffff...', 'fwwwwf..', 'fwmmwf..', 'fwmmwf..', 'fwwwwf..', '.ffffff.', '.....fff', '......ff'],
    plate: ['..fffff..', '.fwwwwwf.', 'fwmmmmmwf', 'fwmmmmmwf', '.fwwwwwf.', '..fffff..']
  };
  function pixel(name, size = 22) {
    const rows = PIX[name] || PIX.star;
    const w = Math.max(...rows.map(r => r.length)), h = rows.length;
    let rects = '';
    rows.forEach((row, y) => [...row].forEach((ch, x) => {
      if (PAL[ch]) rects += `<rect x="${x}" y="${y}" width="1.02" height="1.02" fill="${PAL[ch]}"/>`;
    }));
    return `<svg class="pixel" viewBox="0 0 ${w} ${h}" width="${size}" height="${size * h / w}" shape-rendering="crispEdges" aria-hidden="true">${rects}</svg>`;
  }

  window.Art = { mascot, food, dish, pixel, foodKey, colors: C };
})();
