// hover mouse on canvas after hitting play
// created using tutorial by Patt Vira "https://www.youtube.com/watch?v=QlpadcXok8U"
let scaleFactor = 1;
let growing = true;
let ps = [];



function setup() {
  createCanvas(800,1000); 
  colorMode(HSB, 255)
  
 // ps = new System(width/2, height/2);
  
  
}

function draw() {
   background(0);
      push();

  if(growing && scaleFactor < 2){
    scaleFactor +=0.02;
  }
  else {
    growing = false;
  }
  
  translate(width/2, height/2);
  scale(scaleFactor);
  
  ellipseMode(CENTER);
  noStroke()
  // stroke(255);
  // strokeWeight(5);
  fill(255,20);
  ellipse(0, 0, 300, 300,);
  pop();

  
  beginClip();
  // stroke(255);
  // strokeWeight(5);
  ellipse(width/2, height/2, 600, 600);
  endClip();
  
  
  
  
  
  if(mouseIsPressed){
    ps.push(new System(width/2,height/2));
  
//   if(mouseClicked()){
//     ps.push(new System(width/2,height/2));
    
//   }
    for (let i = ps.length-1 ; i>= 0; i--){
    ps[i].update();
    ps[i].display();
      if (ps[i].done){
        ps.splice(i,1);
      }
      
    
  }  
}

}