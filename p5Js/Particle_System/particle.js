class Particle{
    constructor(x,y){
      this.pos = createVector(x,y);
      this.vel = createVector(0,0);
      this.acc = p5.Vector.random2D();
      this.acc.mult(random(0.5));
      this.life = 255;
      this.done = false;
      this.hueValue = 0;
      
      
      
    
    }
    update(){
      this.finished();
      this.vel.add(this.acc);
      this.pos.add(this.vel);
      this.life -= 5;
      
      if(this.hueValue>255){
        this.hueValue = 0;
      }
      
      this.hueValue += 3;
      
      
      
    }
    
    display(){
      fill(this.hueValue, 255 ,this.life);
      noStroke();
      ellipse(this.pos.x , this.pos.y , 5, 5);
    }
    
    finished(){
      if (this.life < 0){
        this.done = true;
      }
      else { 
      this.done = false;
      }
    }
  }