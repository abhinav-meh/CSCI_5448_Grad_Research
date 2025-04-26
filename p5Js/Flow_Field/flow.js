// Flow Field Simulation using p5.js
// This demonstrates object-oriented principles in creative coding

// Flow field parameters
let field;
let particles = [];
let numParticles = 1000;

function setup() {
  createCanvas(800, 600);
  colorMode(HSB, 255);
  
  // Initialize flow field
  field = new FlowField(20); // Cell size of 20px
  field.update();
  
  // Create particles
  for (let i = 0; i < numParticles; i++) {
    particles.push(new Particle());
  }
  
  background(0);
}

function draw() {
  // Fade background for trail effect
  fill(0, 20);
  noStroke();
  rect(0, 0, width, height);
  
  // Update field occasionally to create movement
  if (frameCount % 60 === 0) {
    field.update();
  }
  
  // Update and display particles
  for (let particle of particles) {
    let force = field.getForce(particle.pos.x, particle.pos.y);
    particle.applyForce(force);
    particle.update();
    particle.display();
  }
  
  // Optionally visualize the flow field
  // field.display();
}

// Mouse interaction to create new flow patterns
function mousePressed() {
  field.addDisturbance(mouseX, mouseY);
}

// FlowField class to manage the vector field
class FlowField {
  constructor(cellSize) {
    this.cellSize = cellSize;
    this.cols = floor(width / cellSize);
    this.rows = floor(height / cellSize);
    this.field = new Array(this.cols * this.rows);
    this.zoff = 0;
  }
  
  update() {
    let xoff = 0;
    for (let i = 0; i < this.cols; i++) {
      let yoff = 0;
      for (let j = 0; j < this.rows; j++) {
        // Calculate flow angle using Perlin noise for organic flow
        const angle = noise(xoff, yoff, this.zoff) * TWO_PI * 2;
        
        // Create a vector from the angle
        const v = p5.Vector.fromAngle(angle);
        v.setMag(1); // Normalize the magnitude
        
        // Store vector in the field array
        const index = i + j * this.cols;
        this.field[index] = v;
        
        yoff += 0.1;
      }
      xoff += 0.1;
    }
    this.zoff += 0.01; // Increment z-offset for flow evolution
  }
  
  addDisturbance(x, y) {
    // Create a disturbance in the flow field based on mouse position
    const centerX = floor(x / this.cellSize);
    const centerY = floor(y / this.cellSize);
    const radius = 5;
    
    for (let i = centerX - radius; i <= centerX + radius; i++) {
      for (let j = centerY - radius; j <= centerY + radius; j++) {
        if (i >= 0 && i < this.cols && j >= 0 && j < this.rows) {
          // Calculate vector pointing away from disturbance center
          const angle = atan2(j - centerY, i - centerX);
          const v = p5.Vector.fromAngle(angle);
          v.setMag(3); // Stronger force for disturbance
          
          const index = i + j * this.cols;
          this.field[index] = v;
        }
      }
    }
  }
  
  getForce(x, y) {
    // Get the force vector at a specific position
    const col = constrain(floor(x / this.cellSize), 0, this.cols - 1);
    const row = constrain(floor(y / this.cellSize), 0, this.rows - 1);
    const index = col + row * this.cols;
    return this.field[index].copy();
  }
  
  display() {
    // Visualize the flow field (for debugging)
    for (let i = 0; i < this.cols; i++) {
      for (let j = 0; j < this.rows; j++) {
        const index = i + j * this.cols;
        const v = this.field[index];
        
        push();
        stroke(255, 50);
        translate(i * this.cellSize + this.cellSize / 2, j * this.cellSize + this.cellSize / 2);
        rotate(v.heading());
        line(0, 0, this.cellSize / 2, 0);
        pop();
      }
    }
  }
}

// Particle class for elements that follow the flow field
class Particle {
  constructor() {
    this.pos = createVector(random(width), random(height));
    this.vel = createVector(0, 0);
    this.acc = createVector(0, 0);
    this.maxSpeed = random(2, 5);
    this.prevPos = this.pos.copy();
    this.hue = random(255);
    this.lifespan = 255;
  }
  
  applyForce(force) {
    this.acc.add(force);
  }
  
  update() {
    // Save previous position for drawing trails
    this.prevPos = this.pos.copy();
    
    // Update physics
    this.vel.add(this.acc);
    this.vel.limit(this.maxSpeed);
    this.pos.add(this.vel);
    this.acc.mult(0);
    
    // Gradually reduce lifespan
    this.lifespan -= 0.5;
    
    // Respawn if off-screen or expired
    this.checkEdges();
  }
  
  checkEdges() {
    // Check if particle is out of bounds and reset if needed
    if (this.pos.x < 0 || this.pos.x > width || 
        this.pos.y < 0 || this.pos.y > height ||
        this.lifespan <= 0) {
      this.pos = createVector(random(width), random(height));
      this.vel = createVector(0, 0);
      this.prevPos = this.pos.copy();
      this.lifespan = 255;
    }
  }
  
  display() {
    // Draw the particle as a line from previous to current position
    stroke(this.hue, 255, 255, this.lifespan);
    strokeWeight(1);
    line(this.prevPos.x, this.prevPos.y, this.pos.x, this.pos.y);
    
    // Slowly shift hue for visual interest
    this.hue = (this.hue + 0.5) % 255;
  }
}