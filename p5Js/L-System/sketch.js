let lSystem = {
  axiom: 'F',
  iterations: 4,
  angle: 25,
  segmentLength: 10,
  rules: { 'F': 'FF+[+F-F-F]-[-F+F+F]' },
  result: '',
  strokeColor: '#006400',
  backgroundColor: '#ffffff'
};

const presets = {
  plant1: {
    axiom: 'F',
    iterations: 4,
    angle: 25,
    rules: { 'F': 'FF+[+F-F-F]-[-F+F+F]' }
  },
  plant2: {
    axiom: 'X',
    iterations: 5,
    angle: 25,
    rules: { 'X': 'F+[[X]-X]-F[-FX]+X', 'F': 'FF' }
  },
  kochCurve: {
    axiom: 'F',
    iterations: 4,
    angle: 60,
    rules: { 'F': 'F+F-F-F+F' }
  },
  sierpinski: {
    axiom: 'F-G-G',
    iterations: 5,
    angle: 120,
    rules: { 'F': 'F-G+F+G-F', 'G': 'GG' }
  },
  dragon: {
    axiom: 'FX',
    iterations: 10,
    angle: 90,
    rules: { 'X': 'X+YF+', 'Y': '-FX-Y' }
  }
};

function setup() {
  const canvas = createCanvas(windowWidth - 300, windowHeight);
  canvas.parent('canvas-container');
  
  
  document.getElementById('preset').addEventListener('change', applyPreset);
  document.getElementById('generate').addEventListener('click', generateLSystem);
  document.getElementById('strokeColor').addEventListener('input', updateColors);
  document.getElementById('backgroundColor').addEventListener('input', updateColors);
  
 
  generateLSystem();
}

function draw() {
  background(lSystem.backgroundColor);
  

  if (!lSystem.result) return;

  translate(width / 2, height - 50);
  

  const scale = constrain(5000 / lSystem.result.length, 0.1, 1);
  const len = lSystem.segmentLength * scale;
  
  stroke(lSystem.strokeColor);
  strokeWeight(1.5);
  noFill();
  

  drawLSystem(lSystem.result, len, lSystem.angle);
}

function windowResized() {
  resizeCanvas(windowWidth - 300, windowHeight);
}

function applyPreset() {
  const selectedPreset = document.getElementById('preset').value;
  
  if (selectedPreset === 'custom') {
    return; 
  
  const preset = presets[selectedPreset];
  

  document.getElementById('axiom').value = preset.axiom;
  document.getElementById('iterations').value = preset.iterations;
  document.getElementById('angle').value = preset.angle;
  

  let rulesText = '';
  for (const key in preset.rules) {
    rulesText += `${key}=${preset.rules[key]}\n`;
  }
  document.getElementById('rules').value = rulesText.trim();
  

  generateLSystem();
}

function updateColors() {
  lSystem.strokeColor = document.getElementById('strokeColor').value;
  lSystem.backgroundColor = document.getElementById('backgroundColor').value;
}

function generateLSystem() {
  
  lSystem.axiom = document.getElementById('axiom').value;
  lSystem.iterations = parseInt(document.getElementById('iterations').value);
  lSystem.angle = parseFloat(document.getElementById('angle').value);
  lSystem.segmentLength = parseFloat(document.getElementById('length').value);
  

  lSystem.rules = {};
  const rulesText = document.getElementById('rules').value.split('\n');
  for (const rule of rulesText) {
    if (rule.includes('=') && rule.length >= 3) {
      const key = rule.charAt(0);
      const value = rule.substring(2);
      lSystem.rules[key] = value;
    }
  }
  
 
  lSystem.result = lSystem.axiom;
  for (let i = 0; i < lSystem.iterations; i++) {
    let nextGen = '';
    for (let j = 0; j < lSystem.result.length; j++) {
      const current = lSystem.result.charAt(j);
      if (lSystem.rules[current]) {
        nextGen += lSystem.rules[current];
      } else {
        nextGen += current;
      }
    }
    lSystem.result = nextGen;
  }
  

  updateColors();
}

function drawLSystem(lSystemStr, len, angleVal) {
  
  push();
  rotate(radians(-90)); 
  
  const stack = [];
  
  for (let i = 0; i < lSystemStr.length; i++) {
    const c = lSystemStr.charAt(i);
    
    switch (c) {
      case 'F': 
      case 'G': 
        line(0, 0, len, 0);
        translate(len, 0);
        break;
      case 'f': 
        translate(len, 0);
        break;
      case '+': 
        rotate(radians(angleVal));
        break;
      case '-': 
        rotate(radians(-angleVal));
        break;
      case '[': 
        push();
        stack.push({ x: 0, y: 0, angle: 0 }); 
        break;
      case ']': 
        pop();
        if (stack.length > 0) stack.pop(); 
        break;
    }
  }
  
  pop();
}
}