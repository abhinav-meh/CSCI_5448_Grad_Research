import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Flow Field Simulation using Java AWT
 * This demonstrates object-oriented principles in creative coding
 */
public class FlowFieldSimulation extends JFrame {
    private FlowFieldPanel panel;
    
    public FlowFieldSimulation() {
        setTitle("Flow Field Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new FlowFieldPanel();
        add(panel);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlowFieldSimulation());
    }
    
    /**
     * Inner class for the panel where the animation takes place
     */
    private class FlowFieldPanel extends JPanel implements ActionListener {
        private static final int WIDTH = 800;
        private static final int HEIGHT = 600;
        private static final int CELL_SIZE = 20;
        private static final int NUM_PARTICLES = 1000;
        
        private FlowField flowField;
        private ArrayList<Particle> particles;
        private Timer timer;
        private Random random;
        private int frameCount;
        
        public FlowFieldPanel() {
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(Color.BLACK);
            
            // Initialize random generator
            random = new Random();
            
            // Create flow field
            flowField = new FlowField(CELL_SIZE, WIDTH, HEIGHT);
            flowField.update();
            
            // Create particles
            particles = new ArrayList<>();
            for (int i = 0; i < NUM_PARTICLES; i++) {
                particles.add(new Particle(random));
            }
            
            // Mouse listener for interaction
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    flowField.addDisturbance(e.getX(), e.getY());
                }
            });
            
            // Animation timer
            timer = new Timer(16, this); // ~60 FPS
            timer.start();
            frameCount = 0;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw semi-transparent rectangle for trail effect
            g2d.setColor(new Color(0, 0, 0, 20));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw particles
            for (Particle particle : particles) {
                particle.display(g2d);
            }
            
            // Optionally visualize the flow field
            // flowField.display(g2d);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            frameCount++;
            
            // Update flow field occasionally
            if (frameCount % 60 == 0) {
                flowField.update();
            }
            
            // Update particles
            for (Particle particle : particles) {
                Vector2D force = flowField.getForce(particle.position.x, particle.position.y);
                particle.applyForce(force);
                particle.update();
            }
            
            repaint();
        }
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

/**
 * Flow Field class to manage the vector field
 */
class FlowField {
    private final int cellSize;
    private final int cols;
    private final int rows;
    private Vector2D[] field;
    private double zoff;
    private final int width;
    private final int height;
    private final Random random;
    
    public FlowField(int cellSize, int width, int height) {
        this.cellSize = cellSize;
        this.width = width;
        this.height = height;
        cols = width / cellSize;
        rows = height / cellSize;
        field = new Vector2D[cols * rows];
        zoff = 0;
        random = new Random();
    }
    
    public void update() {
        double xoff = 0;
        for (int i = 0; i < cols; i++) {
            double yoff = 0;
            for (int j = 0; j < rows; j++) {
                // Calculate flow angle using Perlin-like noise
                double angle = noise(xoff, yoff, zoff) * Math.PI * 4;
                
                // Create a vector from the angle
                Vector2D v = Vector2D.fromAngle(angle);
                
                // Store vector in the field array
                int index = i + j * cols;
                field[index] = v;
                
                yoff += 0.1;
            }
            xoff += 0.1;
        }
        zoff += 0.01; // Increment z-offset for flow evolution
    }
    
    public void addDisturbance(int x, int y) {
        // Create a disturbance in the flow field based on mouse position
        int centerX = x / cellSize;
        int centerY = y / cellSize;
        int radius = 5;
        
        for (int i = centerX - radius; i <= centerX + radius; i++) {
            for (int j = centerY - radius; j <= centerY + radius; j++) {
                if (i >= 0 && i < cols && j >= 0 && j < rows) {
                    // Calculate vector pointing away from disturbance center
                    double angle = Math.atan2(j - centerY, i - centerX);
                    Vector2D v = Vector2D.fromAngle(angle);
                    v.mult(3); // Stronger force for disturbance
                    
                    int index = i + j * cols;
                    field[index] = v;
                }
            }
        }
    }
    
    public Vector2D getForce(double x, double y) {
        // Get the force vector at a specific position
        int col = (int) constrain(Math.floor(x / cellSize), 0, cols - 1);
        int row = (int) constrain(Math.floor(y / cellSize), 0, rows - 1);
        int index = col + row * cols;
        return field[index].copy();
    }
    
    public void display(Graphics2D g2d) {
        // Visualize the flow field (for debugging)
        g2d.setColor(new Color(255, 255, 255, 50));
        
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                int index = i + j * cols;
                Vector2D v = field[index];
                
                int x = i * cellSize + cellSize / 2;
                int y = j * cellSize + cellSize / 2;
                
                double angle = v.heading();
                int len = cellSize / 2;
                
                int x2 = (int) (x + Math.cos(angle) * len);
                int y2 = (int) (y + Math.sin(angle) * len);
                
                g2d.drawLine(x, y, x2, y2);
            }
        }
    }
    
    // Simple implementation of noise function (not true Perlin noise)
    private double noise(double x, double y, double z) {
        // Simple hash function to simulate noise
        double value = Math.sin(x * 10 + y * 20 + z * 15) * 0.5 + 0.5;
        value = Math.sin(value * 10) * 0.5 + 0.5;
        return value;
    }
    
    private double constrain(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}

/**
 * Particle class for elements that follow the flow field
 */
class Particle {
    public Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
    private Vector2D prevPosition;
    private final double maxSpeed;
    private double hue;
    private double lifespan;
    private final Random random;
    
    public Particle(Random random) {
        this.random = random;
        position = new Vector2D(random.nextDouble() * 800, random.nextDouble() * 600);
        velocity = new Vector2D(0, 0);
        acceleration = new Vector2D(0, 0);
        prevPosition = position.copy();
        maxSpeed = 2 + random.nextDouble() * 3;
        hue = random.nextDouble() * 360;
        lifespan = 255;
    }
    
    public void applyForce(Vector2D force) {
        acceleration.add(force);
    }
    
    public void update() {
        // Save previous position for drawing trails
        prevPosition = position.copy();
        
        // Update physics
        velocity.add(acceleration);
        velocity.limit(maxSpeed);
        position.add(velocity);
        
        // Reset acceleration
        acceleration.mult(0);
        
        // Gradually reduce lifespan
        lifespan -= 0.5;
        
        // Check boundaries and lifespan
        checkEdges();
    }
    
    private void checkEdges() {
        // Check if particle is out of bounds and reset if needed
        if (position.x < 0 || position.x > 800 || 
            position.y < 0 || position.y > 600 || 
            lifespan <= 0) {
            
            position = new Vector2D(random.nextDouble() * 800, random.nextDouble() * 600);
            velocity = new Vector2D(0, 0);
            prevPosition = position.copy();
            lifespan = 255;
        }
    }
    
    public void display(Graphics2D g2d) {
        // Draw the particle as a line from previous to current position
        int alpha = (int) Math.max(0, Math.min(255, lifespan));
        Color color = Color.getHSBColor((float) (hue / 360), 1.0f, 1.0f);
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        
        g2d.drawLine(
            (int) prevPosition.x, (int) prevPosition.y,
            (int) position.x, (int) position.y
        );
        
        // Slowly shift hue for visual interest
        hue = (hue + 0.5) % 360;
    }
}