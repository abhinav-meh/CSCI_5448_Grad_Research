import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * A single-file Java particle system demonstration.
 *
 * Usage:
 *  1. Save this file as ParticlesSystem.java
 *  2. Compile: javac ParticlesSystem.java
 *  3. Run: java ParticlesSystem
 */
public class ParticlesSystem extends JPanel implements ActionListener {
    private Timer timer;
    private ParticleController particleController;
    private Vector2D gravity;

    public ParticlesSystem() {
        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.BLACK);

        // The controller manages all particles, with an origin near the top-center.
        particleController = new ParticleController(new Vector2D(250, 50));
        gravity = new Vector2D(0, 0.1);

        // Set up a timer for ~60 fps animation.
        timer = new Timer(16, this);
        timer.start();

        // Add a mouse listener to apply an extra force ("wind") on click.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Calculate a force vector based on mouse click position relative to the origin.
                Vector2D wind = new Vector2D(
                        (e.getX() - particleController.origin.x) / 50.0,
                        (e.getY() - particleController.origin.y) / 50.0
                );
                particleController.applyForce(wind);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Apply gravity to all particles each frame.
        particleController.applyForce(gravity);
        // Update the particle system (move particles, remove dead ones, etc.).
        particleController.run();
        // Randomly add new particles at the origin (20% chance each frame).
        if (Math.random() < 0.2) {
            particleController.addParticle(new Particle(
                    new Vector2D(particleController.origin.x, particleController.origin.y)
            ));
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Let the particle controller render all its particles.
        particleController.display(g2d);
    }

    /**
     * Main method: creates a window and shows the particle system.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Particle System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ParticlesSystem app = new ParticlesSystem();
        frame.add(app);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

/**
 * A simple 2D vector class for basic vector arithmetic.
 */
class Vector2D {
    public double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
    }

    public void multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }

    public Vector2D copy() {
        return new Vector2D(this.x, this.y);
    }
}

/**
 * Represents an individual particle with position, velocity, acceleration,
 * and a limited lifespan for fading.
 */
class Particle {
    Vector2D location;
    Vector2D velocity;
    Vector2D acceleration;
    double lifespan;

    public Particle(Vector2D location) {
        // Start at the given location.
        this.location = location.copy();
        // Small random velocity.
        this.velocity = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1);
        this.acceleration = new Vector2D(0, 0);
        // Lifespan controls fading (255 = fully opaque).
        this.lifespan = 255;
    }

    public void applyForce(Vector2D force) {
        // Accumulate the force in acceleration.
        acceleration.add(force);
    }

    public void update() {
        // Update velocity and location.
        velocity.add(acceleration);
        location.add(velocity);
        // Reset acceleration each frame.
        acceleration.multiply(0);

        // Fade out the particle gradually.
        lifespan -= 2.0;
    }

    public void display(Graphics2D g2d) {
        // Draw the particle as a small circle with alpha = lifespan.
        g2d.setColor(new Color(255, 255, 255, (int) Math.max(lifespan, 0)));
        g2d.fillOval((int) location.x, (int) location.y, 8, 8);
    }

    public boolean isDead() {
        return lifespan < 0;
    }
}

/**
 * Manages a collection of particles, applying forces and updating them over time.
 */
class ParticleController {
    ArrayList<Particle> particles;
    Vector2D origin;

    public ParticleController(Vector2D origin) {
        this.origin = origin.copy();
        particles = new ArrayList<>();
    }

    public void addParticle(Particle p) {
        particles.add(p);
    }

    public void applyForce(Vector2D force) {
        for (Particle p : particles) {
            p.applyForce(force);
        }
    }

    public void run() {
        // Update all particles and remove any that have died.
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead()) {
                particles.remove(i);
            }
        }
    }

    public void display(Graphics2D g2d) {
        for (Particle p : particles) {
            p.display(g2d);
        }
    }
}
