package Java.Boids;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Random;

/**
 * Flocking Behavior Simulation (Boids) using Java AWT
 * This demonstrates object-oriented principles in creative coding
 */
public class FlockingSimulation extends JFrame implements ChangeListener {
    private FlockPanel panel;
    private JSlider alignmentSlider;
    private JSlider cohesionSlider;
    private JSlider separationSlider;
    private JCheckBox visualizeCheckbox;
    
    public FlockingSimulation() {
        setTitle("Flocking Behavior Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        // Create sliders
        JPanel alignPanel = new JPanel(new BorderLayout());
        alignPanel.add(new JLabel("Alignment:"), BorderLayout.WEST);
        alignmentSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 10);
        alignmentSlider.addChangeListener(this);
        alignPanel.add(alignmentSlider, BorderLayout.CENTER);
        controlPanel.add(alignPanel);
        
        JPanel cohesionPanel = new JPanel(new BorderLayout());
        cohesionPanel.add(new JLabel("Cohesion:"), BorderLayout.WEST);
        cohesionSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 10);
        cohesionSlider.addChangeListener(this);
        cohesionPanel.add(cohesionSlider, BorderLayout.CENTER);
        controlPanel.add(cohesionPanel);
        
        JPanel separationPanel = new JPanel(new BorderLayout());
        separationPanel.add(new JLabel("Separation:"), BorderLayout.WEST);
        separationSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 15);
        separationSlider.addChangeListener(this);
        separationPanel.add(separationSlider, BorderLayout.CENTER);
        controlPanel.add(separationPanel);
        
        // Create visualization checkbox
        visualizeCheckbox = new JCheckBox("Visualize Forces");
        visualizeCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.setVisualizeForces(visualizeCheckbox.isSelected());
            }
        });
        controlPanel.add(visualizeCheckbox);
        
        // Add button to add more boids
        JButton addButton = new JButton("Add 10 Boids");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.addBoids(10);
            }
        });
        controlPanel.add(addButton);
        
        // Create simulation panel
        panel = new FlockPanel();
        
        // Add components to frame
        add(controlPanel, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        // Update weights when sliders change
        panel.setWeights(
            alignmentSlider.getValue() / 10.0,
            cohesionSlider.getValue() / 10.0,
            separationSlider.getValue() / 10.0
        );
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlockingSimulation());
    }
    
    /**
     * Inner class for the panel where the flocking simulation takes place
     */
    private class FlockPanel extends JPanel implements ActionListener, MouseListener {
        private static final int WIDTH = 800;
        private static final int HEIGHT = 600;
        
        private Flock flock;
        private Timer timer;
        private Random random;
        
        private double alignWeight = 1.0;
        private double cohesionWeight = 1.0;
        private double separationWeight = 1.5;
        private boolean visualizeForces = false;
        
        public FlockPanel() {
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(Color.DARK_GRAY);
            
            random = new Random();
            flock = new Flock();
            
            // Add initial boids
            addBoids(100);
            
            // Add mouse listener to add boids
            addMouseListener(this);
            
            // Start animation timer
            timer = new Timer(16, this); // ~60 FPS
            timer.start();
        }
        
        public void addBoids(int count) {
            for (int i = 0; i < count; i++) {
                flock.addBoid(new Boid(
                    random.nextDouble() * WIDTH,
                    random.nextDouble() * HEIGHT,
                    random
                ));
            }
        }
        
        public void setWeights(double align, double cohesion, double separation) {
            this.alignWeight = align;
            this.cohesionWeight = cohesion;
            this.separationWeight = separation;
        }
        
        public void setVisualizeForces(boolean visualize) {
            this.visualizeForces = visualize;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Run the flock
            flock.run(g2d, alignWeight, cohesionWeight, separationWeight, visualizeForces);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            repaint();
        }
        
        // MouseListener methods
        @Override
        public void mousePressed(MouseEvent e) {
            // Add a new boid at the mouse position
            flock.addBoid(new Boid(e.getX(), e.getY(), random));
        }
        
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
}

/**
 * Flock class to manage all boids
 */
class Flock {
    private ArrayList<Boid> boids;
    
    public Flock() {
        boids = new ArrayList<>();
    }
    
    public void addBoid(Boid boid) {
        boids.add(boid);
    }
    
    public void run(Graphics2D g2d, double alignWeight, double cohesionWeight, 
                   double separationWeight, boolean visualizeForces) {
        for (Boid boid : boids) {
            // Apply flocking behavior with specified weights
            boid.flock(boids, alignWeight, cohesionWeight, separationWeight);
            
            // Update and display the boid
            boid.update();
            boid.borders();
            boid.display(g2d, visualizeForces);
        }
    }
}

/**
 * Boid class for individual flocking agents
 */
class Boid {
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
    private double r = 3.0;  // Size of boid
    private double maxSpeed = 3.0;
    private double maxForce = 0.05;
    
    // Store forces for visualization
    private Vector2D alignForce;
    private Vector2D cohesionForce;
    private Vector2D separationForce;
    
    // Cache the triangle shape for efficiency
    private Path2D shape;
    
    public Boid(double x, double y, Random random) {
        position = new Vector2D(x, y);
        
        // Initialize with random velocity
        double angle = random.nextDouble() * Math.PI * 2;
        velocity = new Vector2D(Math.cos(angle), Math.sin(angle));
        velocity.mult(random.nextDouble() + 1); // Random speed between 1 and 2
        
        acceleration = new Vector2D(0, 0);
        
        alignForce = new Vector2D(0, 0);
        cohesionForce = new Vector2D(0, 0);
        separationForce = new Vector2D(0, 0);
        
        // Create the triangle shape
        shape = new Path2D.Double();
        shape.moveTo(0, -r * 2);
        shape.lineTo(-r, r * 2);
        shape.lineTo(r, r * 2);
        shape.closePath();
    }
    
    public void flock(ArrayList<Boid> boids, double alignWeight, 
                     double cohesionWeight, double separationWeight) {
        // Calculate the three main forces
        alignForce = align(boids);
        cohesionForce = cohesion(boids);
        separationForce = separation(boids);
        
        // Apply weights
        alignForce.mult(alignWeight);
        cohesionForce.mult(cohesionWeight);
        separationForce.mult(separationWeight);
        
        // Add forces to acceleration
        acceleration.add(alignForce);
        acceleration.add(cohesionForce);
        acceleration.add(separationForce);
    }
    
    public void update() {
        // Update velocity and position
        velocity.add(acceleration);
        velocity.limit(maxSpeed);
        position.add(velocity);
        
        // Reset acceleration
        acceleration.mult(0);
    }
    
    // Steer toward average direction of nearby boids
    private Vector2D align(ArrayList<Boid> boids) {
        double perceptionRadius = 50;
        Vector2D sum = new Vector2D(0, 0);
        int count = 0;
        
        for (Boid other : boids) {
            double d = dist(position, other.position);
            
            if (other != this && d < perceptionRadius) {
                sum.add(other.velocity);
                count++;
            }
        }
        
        if (count > 0) {
            sum.mult(1.0 / count);
            sum.normalize();
            sum.mult(maxSpeed);
            
            Vector2D steer = sum.copy();
            steer.sub(velocity);
            steer.limit(maxForce);
            return steer;
        }
        
        return new Vector2D(0, 0);
    }
    
    // Steer toward center of nearby boids
    private Vector2D cohesion(ArrayList<Boid> boids) {
        double perceptionRadius = 100;
        Vector2D sum = new Vector2D(0, 0);
        int count = 0;
        
        for (Boid other : boids) {
            double d = dist(position, other.position);
            
            if (other != this && d < perceptionRadius) {
                sum.add(other.position);
                count++;
            }
        }
        
        if (count > 0) {
            sum.mult(1.0 / count);
            return seek(sum);
        }
        
        return new Vector2D(0, 0);
    }
    
    // Steer away from nearby boids to avoid crowding
    private Vector2D separation(ArrayList<Boid> boids) {
        double perceptionRadius = 50;
        Vector2D sum = new Vector2D(0, 0);
        int count = 0;
        
        for (Boid other : boids) {
            double d = dist(position, other.position);
            
            if (other != this && d < perceptionRadius) {
                Vector2D diff = new Vector2D(position.x - other.position.x, position.y - other.position.y);
                diff.normalize();
                diff.mult(1.0 / d); // Weight by distance (closer boids have more influence)
                sum.add(diff);
                count++;
            }
        }
        
        if (count > 0) {
            sum.mult(1.0 / count);
            sum.normalize();
            sum.mult(maxSpeed);
            
            Vector2D steer = sum.copy();
            steer.sub(velocity);
            steer.limit(maxForce);
            return steer;
        }
        
        return new Vector2D(0, 0);
    }
    
    // Calculate steering force towards a target
    private Vector2D seek(Vector2D target) {
        Vector2D desired = new Vector2D(target.x - position.x, target.y - position.y);
        desired.normalize();
        desired.mult(maxSpeed);
        
        Vector2D steer = desired.copy();
        steer.sub(velocity);
        steer.limit(maxForce);
        return steer;
    }
    
    // Wrap around screen edges
    public void borders() {
        if (position.x < -r) position.x = 800 + r;
        if (position.y < -r) position.y = 600 + r;
        if (position.x > 800 + r) position.x = -r;
        if (position.y > 600 + r) position.y = -r;
    }
    
    // Draw the boid and optionally visualize forces
    public void display(Graphics2D g2d, boolean visualizeForces) {
        // Calculate heading angle
        double theta = Math.atan2(velocity.y, velocity.x) + Math.PI/2;
        
        // Draw boid as a triangle
        AffineTransform transform = new AffineTransform();
        transform.translate(position.x, position.y);
        transform.rotate(theta);
        
        g2d.setColor(Color.WHITE);
        g2d.fill(transform.createTransformedShape(shape));
        g2d.setColor(Color.BLACK);
        g2d.draw(transform.createTransformedShape(shape));
        
        // Visualize forces if enabled
        if (visualizeForces) {
            double forceScale = 100; // Scale up forces for visibility
            
            // Alignment force - blue
            g2d.setColor(Color.BLUE);
            drawForce(g2d, alignForce, forceScale);
            
            // Cohesion force - green
            g2d.setColor(Color.GREEN);
            drawForce(g2d, cohesionForce, forceScale);
            
            // Separation force - red
            g2d.setColor(Color.RED);
            drawForce(g2d, separationForce, forceScale);
        }
    }
    
    // Helper method to draw a force vector
    private void drawForce(Graphics2D g2d, Vector2D force, double scale) {
        Vector2D scaledForce = force.copy();
        scaledForce.mult(scale);
        g2d.drawLine(
            (int) position.x, 
            (int) position.y, 
            (int) (position.x + scaledForce.x), 
            (int) (position.y + scaledForce.y)
        );
    }
    
    // Helper method to calculate distance between vectors
    private double dist(Vector2D v1, Vector2D v2) {
        double dx = v1.x - v2.x;
        double dy = v1.y - v2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

/**
 * Vector class for 2D vector operations
 */
class Vector2D {
    public double x;
    public double y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void add(Vector2D v) {
        x += v.x;
        y += v.y;
    }
    
    public void sub(Vector2D v) {
        x -= v.x;
        y -= v.y;
    }
    
    public void mult(double scalar) {
        x *= scalar;
        y *= scalar;
    }
    
    public double mag() {
        return Math.sqrt(x * x + y * y);
    }
    
    public void limit(double max) {
        if (mag() > max) {
            normalize();
            mult(max);
        }
    }
    
    public void normalize() {
        double m = mag();
        if (m != 0) {
            x /= m;
            y /= m;
        }
    }
    
    public double heading() {
        return Math.atan2(y, x);
    }
    
    public static Vector2D fromAngle(double angle) {
        return new Vector2D(Math.cos(angle), Math.sin(angle));
    }
    
    public Vector2D copy() {
        return new Vector2D(x, y);
    }
}