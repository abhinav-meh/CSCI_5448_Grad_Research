// Reaction-Diffusion System (Turing Patterns) using p5.js
// This demonstrates object-oriented principles in creative coding

// Grid parameters
let grid;
let next;
let w = 800; // Canvas width
let h = 600; // Canvas height

// Simulation parameters (Gray-Scott model)
let dA = 1.0;     // Diffusion rate of A (feed)
let dB = 0.5;     // Diffusion rate of B (kill)
let feed = 0.055;  // Feed rate
let kill = 0.062;  // Kill rate

// UI elements
let feedSlider, killSlider;
let resetButton, pauseButton;
let paused = false;

function setup() {
  createCanvas(w, h);
  pixelDensity(1); // For better performance
  
  // Create controls
  createP('Adjust parameters:');
  
  feedSlider = createSlider(0.01, 0.1, feed, 0.001);
  feedSlider.parent(createP('Feed rate:'));
  
  killSlider = createSlider(0.01, 0.1, kill, 0.001);
  killSlider.parent(createP('Kill rate:'));
  
  resetButton = createButton('Reset Simulation');
  resetButton.mousePressed(resetSimulation);
  
  pauseButton = createButton('Pause/Resume');
  pauseButton.mousePressed(() => paused = !paused);
  
  // Initialize the simulation system
  grid = new ReactionDiffusionSystem(w, h);
  grid.initialize();
}

function draw() {
  // Update parameters from sliders
  feed = feedSlider.value();
  kill = killSlider.value();
  
  // Run simulation if not paused
  if (!paused) {
    grid.update(feed, kill, dA, dB);
  }
  
  // Display the system
  grid.display();
  
  // Show parameters in the corner
  fill(255);
  noStroke();
  rect(0, 0, 150, 60);
  fill(0);
  text('FPS: ' + round(frameRate()), 10, 20);
  text('Feed: ' + feed.toFixed(3), 10, 40);
  text('Kill: ' + kill.toFixed(3), 10, 60);
}

function mouseDragged() {
  // Add chemicals where the mouse is dragged
  grid.addChemical(mouseX, mouseY);
}

function mousePressed() {
  // Add chemicals where the mouse is clicked
  grid.addChemical(mouseX, mouseY);
}

function resetSimulation() {
  grid.initialize();
}

class ReactionDiffusionSystem {
  constructor(width, height) {
    this.width = width;
    this.height = height;
    
    // Initialize 2D arrays for current and next state
    this.gridA = [];
    this.gridB = [];
    this.nextA = [];
    this.nextB = [];
    
    for (let i = 0; i < width; i++) {
      this.gridA[i] = [];
      this.gridB[i] = [];
      this.nextA[i] = [];
      this.nextB[i] = [];
      
      for (let j = 0; j < height; j++) {
        this.gridA[i][j] = 1.0; // Chemical A (majority)
        this.gridB[i][j] = 0.0; // Chemical B (initially sparse)
        this.nextA[i][j] = 1.0;
        this.nextB[i][j] = 0.0;
      }
    }
  }
  
  initialize() {
    // Reset the grid with a uniform state
    for (let i = 0; i < this.width; i++) {
      for (let j = 0; j < this.height; j++) {
        this.gridA[i][j] = 1.0;
        this.gridB[i][j] = 0.0;
        this.nextA[i][j] = 1.0;
        this.nextB[i][j] = 0.0;
      }
    }
    
    // Add a small amount of chemical B in the center
    this.addChemicalSquare(this.width / 2, this.height / 2, 20);
  }
  
  addChemical(x, y) {
    // Add a small amount of chemical B at the mouse position
    const radius = 5;
    
    for (let i = -radius; i <= radius; i++) {
      for (let j = -radius; j <= radius; j++) {
        const posX = floor(x + i);
        const posY = floor(y + j);
        
        // Make sure we're within bounds
        if (posX >= 0 && posX < this.width && posY >= 0 && posY < this.height) {
          // Add chemical B in a circular pattern
          if (i*i + j*j <= radius*radius) {
            this.gridB[posX][posY] = 1.0;
            this.gridA[posX][posY] = 0.0;
          }
        }
      }
    }
  }
  
  addChemicalSquare(x, y, size) {
    // Add a square of chemical B
    const halfSize = floor(size / 2);
    
    for (let i = -halfSize; i <= halfSize; i++) {
      for (let j = -halfSize; j <= halfSize; j++) {
        const posX = floor(x + i);
        const posY = floor(y + j);
        
        // Make sure we're within bounds
        if (posX >= 0 && posX < this.width && posY >= 0 && posY < this.height) {
          this.gridB[posX][posY] = 1.0;
          this.gridA[posX][posY] = 0.0;
        }
      }
    }
  }
  
  update(feed, kill, dA, dB) {
    // Apply the Gray-Scott reaction-diffusion formula
    
    // For better performance, we'll only compute every other pixel
    const skip = 1;
    
    // Reaction-diffusion iterations per frame (more gives faster simulation but lower FPS)
    const iterations = 1;
    
    for (let iter = 0; iter < iterations; iter++) {
      for (let i = skip; i < this.width - skip; i += skip) {
        for (let j = skip; j < this.height - skip; j += skip) {
          // Get current values
          let a = this.gridA[i][j];
          let b = this.gridB[i][j];
          
          // Calculate the Laplacian for diffusion
          let laplaceA = 0;
          let laplaceB = 0;
          
          // 3x3 kernel with center weighted double
          laplaceA += this.gridA[i-skip][j] * 0.2;
          laplaceA += this.gridA[i+skip][j] * 0.2;
          laplaceA += this.gridA[i][j-skip] * 0.2;
          laplaceA += this.gridA[i][j+skip] * 0.2;
          laplaceA += this.gridA[i-skip][j-skip] * 0.05;
          laplaceA += this.gridA[i+skip][j-skip] * 0.05;
          laplaceA += this.gridA[i-skip][j+skip] * 0.05;
          laplaceA += this.gridA[i+skip][j+skip] * 0.05;
          laplaceA -= this.gridA[i][j] * 1.0;
          
          laplaceB += this.gridB[i-skip][j] * 0.2;
          laplaceB += this.gridB[i+skip][j] * 0.2;
          laplaceB += this.gridB[i][j-skip] * 0.2;
          laplaceB += this.gridB[i][j+skip] * 0.2;
          laplaceB += this.gridB[i-skip][j-skip] * 0.05;
          laplaceB += this.gridB[i+skip][j-skip] * 0.05;
          laplaceB += this.gridB[i-skip][j+skip] * 0.05;
          laplaceB += this.gridB[i+skip][j+skip] * 0.05;
          laplaceB -= this.gridB[i][j] * 1.0;
          
          // Gray-Scott reaction-diffusion formula
          const reaction = a * b * b;
          
          // Update rules
          this.nextA[i][j] = a + (dA * laplaceA - reaction + feed * (1 - a)) * 0.9;
          this.nextB[i][j] = b + (dB * laplaceB + reaction - (kill + feed) * b) * 0.9;
          
          // Constrain values for stability
          this.nextA[i][j] = constrain(this.nextA[i][j], 0, 1);
          this.nextB[i][j] = constrain(this.nextB[i][j], 0, 1);
          
          // Copy values to neighboring cells when using skip
          if (skip > 1) {
            for (let di = 0; di < skip && i + di < this.width; di++) {
              for (let dj = 0; dj < skip && j + dj < this.height; dj++) {
                if (di !== 0 || dj !== 0) {
                  this.nextA[i + di][j + dj] = this.nextA[i][j];
                  this.nextB[i + di][j + dj] = this.nextB[i][j];
                }
              }
            }
          }
        }
      }
      
      // Swap grids for next iteration
      [this.gridA, this.nextA] = [this.nextA, this.gridA];
      [this.gridB, this.nextB] = [this.nextB, this.gridB];
    }
  }
  
  display() {
    // Visualization of the reaction-diffusion system
    loadPixels();
    
    for (let i = 0; i < this.width; i++) {
      for (let j = 0; j < this.height; j++) {
        // Map the chemical B concentration to a grayscale value
        const color = floor((1 - this.gridB[i][j]) * 255);
        
        // Calculate pixel position in the pixel array
        const idx = (i + j * width) * 4;
        
        // Set pixel color (grayscale)
        pixels[idx + 0] = color;
        pixels[idx + 1] = color;
        pixels[idx + 2] = color;
        pixels[idx + 3] = 255; // Alpha
      }
    }
    
    updatePixels();
  }
}