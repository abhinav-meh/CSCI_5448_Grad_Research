// Flocking Behavior Simulation (Boids) using p5.js
// This demonstrates object-oriented principles in creative coding

let flock;
let alignSlider, cohesionSlider, separationSlider;
let visualizeForces = false;

function setup() {
  createCanvas(800, 600);
  
  // Create UI elements
  createP('Adjust flocking behavior:');
  alignSlider = createSlider(0, 5, 1, 0.1);
  alignSlider.parent(createP('Alignment:'));
  
  cohesionSlider = createSlider(0, 5, 1, 0.1);
  cohesionSlider.parent(createP('Cohesion:'));
  
  separationSlider = createSlider(0, 5, 1.5, 0.1);
  separationSlider.parent(createP('Separation:'));
  
  let visualizeButton = createButton('Toggle Force Visualization');
  visualizeButton.mousePressed(() => {
    visualizeForces = !visualizeForces;
  });
  
  // Initialize the flock
  flock = new Flock();
  
  // Add initial boids
  for (let i = 0; i < 100; i++) {
    let boid = new Boid(random(width), random(height));
    flock.addBoid(boid);
  }
}

function draw() {
  background(50);
  
  // Update and display the flock
  flock.run(visualizeForces);
}

// Add a new boid when the mouse is clicked
function mousePressed() {
  if (mouseY < height && mouseY > 0 && mouseX < width && mouseX > 0) {
    let boid = new Boid(mouseX, mouseY);
    flock.addBoid(boid);
    return false; // Prevent default behavior
  }
}

// Flock class to manage all boids
class Flock {
  constructor() {
    this.boids = []; // Array to hold all boids
  }
  
  addBoid(boid) {
    this.boids.push(boid);
  }
  
  run(visualizeForces) {
    for (let boid of this.boids) {
      // Apply flocking behavior
      boid.flock(this.boids);
      
      // Update and display the boid
      boid.update();
      boid.borders();
      boid.display(visualizeForces);
    }
  }
}

// Boid class for individual flocking agents
class Boid {
  constructor(x, y) {
    this.position = createVector(x, y);
    this.velocity = p5.Vector.random2D(); // Initial random direction
    this.velocity.setMag(random(1, 2));   // Random speed
    this.acceleration = createVector(0, 0);
    this.r = 3.0;  // Size of boid
    this.maxSpeed = 3;
    this.maxForce = 0.05;
    
    // Store forces for visualization
    this.alignForce = createVector(0, 0);
    this.cohesionForce = createVector(0, 0);
    this.separationForce = createVector(0, 0);
  }
  
  // Apply all flocking rules
  flock(boids) {
    // Calculate the three main forces
    this.alignForce = this.align(boids);
    this.cohesionForce = this.cohesion(boids);
    this.separationForce = this.separation(boids);
    
    // Apply weights from sliders
    this.alignForce.mult(alignSlider.value());
    this.cohesionForce.mult(cohesionSlider.value());
    this.separationForce.mult(separationSlider.value());
    
    // Add forces to acceleration
    this.acceleration.add(this.alignForce);
    this.acceleration.add(this.cohesionForce);
    this.acceleration.add(this.separationForce);
  }
  
  // Update position based on velocity and acceleration
  update() {
    this.velocity.add(this.acceleration);
    this.velocity.limit(this.maxSpeed);
    this.position.add(this.velocity);
    this.acceleration.mult(0); // Reset acceleration
  }
  
  // Steer toward average direction of nearby boids
  align(boids) {
    let perceptionRadius = 50;
    let steering = createVector();
    let total = 0;
    
    for (let other of boids) {
      let d = dist(this.position.x, this.position.y, other.position.x, other.position.y);
      
      if (other != this && d < perceptionRadius) {
        steering.add(other.velocity);
        total++;
      }
    }
    
    if (total > 0) {
      steering.div(total);
      steering.setMag(this.maxSpeed);
      steering.sub(this.velocity);
      steering.limit(this.maxForce);
    }
    
    return steering;
  }
  
  // Steer toward center of nearby boids
  cohesion(boids) {
    let perceptionRadius = 100;
    let steering = createVector();
    let total = 0;
    
    for (let other of boids) {
      let d = dist(this.position.x, this.position.y, other.position.x, other.position.y);
      
      if (other != this && d < perceptionRadius) {
        steering.add(other.position);
        total++;
      }
    }
    
    if (total > 0) {
      steering.div(total);
      steering.sub(this.position);
      steering.setMag(this.maxSpeed);
      steering.sub(this.velocity);
      steering.limit(this.maxForce);
    }
    
    return steering;
  }
  
  // Steer away from nearby boids to avoid crowding
  separation(boids) {
    let perceptionRadius = 50;
    let steering = createVector();
    let total = 0;
    
    for (let other of boids) {
      let d = dist(this.position.x, this.position.y, other.position.x, other.position.y);
      
      if (other != this && d < perceptionRadius) {
        let diff = p5.Vector.sub(this.position, other.position);
        diff.div(d * d); // Weight by distance (closer boids have more influence)
        steering.add(diff);
        total++;
      }
    }
    
    if (total > 0) {
      steering.div(total);
      steering.setMag(this.maxSpeed);
      steering.sub(this.velocity);
      steering.limit(this.maxForce);
    }
    
    return steering;
  }
  
  // Wrap around screen edges
  borders() {
    if (this.position.x < -this.r) this.position.x = width + this.r;
    if (this.position.y < -this.r) this.position.y = height + this.r;
    if (this.position.x > width + this.r) this.position.x = -this.r;
    if (this.position.y > height + this.r) this.position.y = -this.r;
  }
  
  // Draw the boid and optionally visualize forces
  display(visualizeForces) {
    // Calculate heading angle
    let theta = this.velocity.heading() + PI/2;
    
    // Draw boid as a triangle
    fill(200);
    stroke(25);
    strokeWeight(1);
    
    push();
    translate(this.position.x, this.position.y);
    rotate(theta);
    beginShape();
    vertex(0, -this.r * 2);
    vertex(-this.r, this.r * 2);
    vertex(this.r, this.r * 2);
    endShape(CLOSE);
    pop();
    
    // Visualize forces if enabled
    if (visualizeForces) {
      const forceScale = 100; // Scale up forces for visibility
      
      // Alignment force - blue
      stroke(0, 0, 255);
      let alignEnd = p5.Vector.add(this.position, p5.Vector.mult(this.alignForce, forceScale));
      line(this.position.x, this.position.y, alignEnd.x, alignEnd.y);
      
      // Cohesion force - green
      stroke(0, 255, 0);
      let cohesionEnd = p5.Vector.add(this.position, p5.Vector.mult(this.cohesionForce, forceScale));
      line(this.position.x, this.position.y, cohesionEnd.x, cohesionEnd.y);
      
      // Separation force - red
      stroke(255, 0, 0);
      let separationEnd = p5.Vector.add(this.position, p5.Vector.mult(this.separationForce, forceScale));
      line(this.position.x, this.position.y, separationEnd.x, separationEnd.y);
    }
  }
}