package Java.ReactionDiffusion;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Reaction-Diffusion System (Turing Patterns) using Java AWT
 * This demonstrates object-oriented principles in creative coding
 */
public class ReactionDiffusionSimulation extends JFrame implements ChangeListener {
    private ReactionDiffusionPanel panel;
    private JSlider feedSlider;
    private JSlider killSlider;
    private JLabel fpsLabel;
    private boolean paused = false;
    
    public ReactionDiffusionSimulation() {
        setTitle("Reaction-Diffusion System (Turing Patterns)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        // Create sliders
        JPanel feedPanel = new JPanel(new BorderLayout());
        feedPanel.add(new JLabel("Feed Rate:"), BorderLayout.WEST);
        feedSlider = new JSlider(JSlider.HORIZONTAL, 10, 100, 55);
        feedSlider.addChangeListener(this);
        feedPanel.add(feedSlider, BorderLayout.CENTER);
        controlPanel.add(feedPanel);
        
        JPanel killPanel = new JPanel(new BorderLayout());
        killPanel.add(new JLabel("Kill Rate:"), BorderLayout.WEST);
        killSlider = new JSlider(JSlider.HORIZONTAL, 10, 100, 62);
        killSlider.addChangeListener(this);
        killPanel.add(killSlider, BorderLayout.CENTER);
        controlPanel.add(killPanel);
        
        // Create buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.resetSystem();
            }
        });
        buttonPanel.add(resetButton);
        
        JButton pauseButton = new JButton("Pause/Resume");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paused = !paused;
                panel.setPaused(paused);
            }
        });
        buttonPanel.add(pauseButton);
        
        controlPanel.add(buttonPanel);
        
        // FPS display
        fpsLabel = new JLabel("FPS: 0");
        controlPanel.add(fpsLabel);
        
        // Create simulation panel
        panel = new ReactionDiffusionPanel(this);
        
        // Add components to frame
        add(controlPanel, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        // Update parameters when sliders change
        double feed = feedSlider.getValue() / 1000.0;
        double kill = killSlider.getValue() / 1000.0;
        panel.setParameters(feed, kill);
    }
    
    public void updateFPS(int fps) {
        fpsLabel.setText("FPS: " + fps);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReactionDiffusionSimulation());
    }
    
    /**
     * Inner class for the panel where the simulation takes place
     */
    private class ReactionDiffusionPanel extends JPanel implements MouseListener, MouseMotionListener {
        private static final int WIDTH = 800;
        private static final int HEIGHT = 600;
        
        private ReactionDiffusionSystem system;
        private Timer timer;
        private BufferedImage image;
        private int frameCount = 0;
        private long lastFpsTime = 0;
        private ReactionDiffusionSimulation parent;
        
        public ReactionDiffusionPanel(ReactionDiffusionSimulation parent) {
            this.parent = parent;
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(Color.BLACK);
            
            // Initialize the system
            system = new ReactionDiffusionSystem(WIDTH, HEIGHT);
            system.initialize();
            
            // Create image for display
            image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            
            // Add mouse listeners
            addMouseListener(this);
            addMouseMotionListener(this);
            
            // Animation timer (target 30 FPS)
            timer = new Timer(33, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    update();
                    repaint();
                    
                    // Calculate FPS
                    frameCount++;
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastFpsTime > 1000) {
                        parent.updateFPS(frameCount);
                        frameCount = 0;
                        lastFpsTime = currentTime;
                    }
                }
            });
            timer.start();
        }
        
        public void resetSystem() {
            system.initialize();
        }
        
        public void setPaused(boolean paused) {
            if (paused) {
                if (timer.isRunning()) {
                    timer.stop();
                }
            } else {
                if (!timer.isRunning()) {
                    timer.start();
                }
            }
        }
        
        public void setParameters(double feed, double kill) {
            system.setParameters(feed, kill);
        }
        
        private void update() {
            system.update();
            updateImage();
        }
        
        private void updateImage() {
            // Transfer system state to the image for display
            double[][] gridB = system.getGridB();
            
            for (int i = 0; i < WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    // Map the chemical B concentration to a grayscale value
                    int color = (int)((1 - gridB[i][j]) * 255);
                    color = Math.max(0, Math.min(255, color));
                    
                    // Create RGB color
                    int rgb = (color << 16) | (color << 8) | color;
                    image.setRGB(i, j, rgb);
                }
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Draw the image
            g.drawImage(image, 0, 0, null);
            
            // Draw parameters
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 150, 60);
            g.setColor(Color.BLACK);
            g.drawString("Feed: " + String.format("%.3f", system.getFeed()), 10, 20);
            g.drawString("Kill: " + String.format("%.3f", system.getKill()), 10, 40);
        }

        // Mouse event handlers
        @Override
        public void mousePressed(MouseEvent e) {
            system.addChemical(e.getX(), e.getY());
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            system.addChemical(e.getX(), e.getY());
        }
        
        // Unused mouse events
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        @Override public void mouseMoved(MouseEvent e) {}
    }
}

/**
 * Class representing the reaction-diffusion system
 */
class ReactionDiffusionSystem {
    private int width;
    private int height;
    
    // Grid arrays for chemicals A and B
    private double[][] gridA;
    private double[][] gridB;
    private double[][] nextA;
    private double[][] nextB;
    
    // Simulation parameters (Gray-Scott model)
    private double dA = 1.0;  // Diffusion rate of A
    private double dB = 0.5;  // Diffusion rate of B
    private double feed = 0.055;  // Feed rate
    private double kill = 0.062;  // Kill rate
    
    // Performance optimization
    private ExecutorService executor;
    private int numThreads;
    
    public ReactionDiffusionSystem(int width, int height) {
        this.width = width;
        this.height = height;
        
        // Initialize grid arrays
        gridA = new double[width][height];
        gridB = new double[width][height];
        nextA = new double[width][height];
        nextB = new double[width][height];
        
        // Setup thread pool for parallel computation
        numThreads = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(numThreads);
    }
    
    public void initialize() {
        // Initialize with a uniform state (A=1, B=0)
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                gridA[i][j] = 1.0;
                gridB[i][j] = 0.0;
                nextA[i][j] = 1.0;
                nextB[i][j] = 0.0;
            }
        }
        
        // Add some chemical B in the center
        addChemicalSquare(width / 2, height / 2, 20);
    }
    
    public void setParameters(double feed, double kill) {
        this.feed = feed;
        this.kill = kill;
    }
    
    public double getFeed() {
        return feed;
    }
    
    public double getKill() {
        return kill;
    }
    
    public double[][] getGridB() {
        return gridB;
    }
    
    public void addChemical(int x, int y) {
        // Add chemical B in a circular pattern
        int radius = 5;
        
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int posX = x + i;
                int posY = y + j;
                
                if (posX >= 0 && posX < width && posY >= 0 && posY < height) {
                    // Add chemical in a circular pattern
                    if (i*i + j*j <= radius*radius) {
                        gridB[posX][posY] = 1.0;
                        gridA[posX][posY] = 0.0;
                    }
                }
            }
        }
    }
    
    public void addChemicalSquare(int x, int y, int size) {
        // Add a square of chemical B
        int halfSize = size / 2;
        
        for (int i = -halfSize; i <= halfSize; i++) {
            for (int j = -halfSize; j <= halfSize; j++) {
                int posX = x + i;
                int posY = y + j;
                
                if (posX >= 0 && posX < width && posY >= 0 && posY < height) {
                    gridB[posX][posY] = 1.0;
                    gridA[posX][posY] = 0.0;
                }
            }
        }
    }
    
    public void update() {
        // Use parallel processing for better performance
        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int threadIndex = threadId;
            executor.execute(() -> updateRegion(threadIndex));
        }
        
        try {
            executor.awaitTermination(16, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Swap grid arrays
        double[][] tempA = gridA;
        gridA = nextA;
        nextA = tempA;
        
        double[][] tempB = gridB;
        gridB = nextB;
        nextB = tempB;
    }
    
    private void updateRegion(int threadId) {
        // Each thread processes a horizontal slice of the grid
        int startY = (height / numThreads) * threadId;
        int endY = (threadId == numThreads - 1) ? height : (height / numThreads) * (threadId + 1);
        
        // Apply the Gray-Scott reaction-diffusion formula
        int skip = 1; // For better performance, we can compute every other pixel
        
        for (int i = skip; i < width - skip; i += skip) {
            for (int j = startY; j < endY - skip && j < height - skip; j += skip) {
                // Get current values
                double a = gridA[i][j];
                double b = gridB[i][j];
                
                // Calculate the Laplacian for diffusion
                double laplaceA = 0;
                double laplaceB = 0;
                
                // 3x3 kernel
                laplaceA += gridA[i-skip][j] * 0.2;
                laplaceA += gridA[i+skip][j] * 0.2;
                laplaceA += gridA[i][j-skip] * 0.2;
                laplaceA += gridA[i][j+skip] * 0.2;
                laplaceA += gridA[i-skip][j-skip] * 0.05;
                laplaceA += gridA[i+skip][j-skip] * 0.05;
                laplaceA += gridA[i-skip][j+skip] * 0.05;
                laplaceA += gridA[i+skip][j+skip] * 0.05;
                laplaceA -= gridA[i][j] * 1.0;
                
                laplaceB += gridB[i-skip][j] * 0.2;
                laplaceB += gridB[i+skip][j] * 0.2;
                laplaceB += gridB[i][j-skip] * 0.2;
                laplaceB += gridB[i][j+skip] * 0.2;
                laplaceB += gridB[i-skip][j-skip] * 0.05;
                laplaceB += gridB[i+skip][j-skip] * 0.05;
                laplaceB += gridB[i-skip][j+skip] * 0.05;
                laplaceB += gridB[i+skip][j+skip] * 0.05;
                laplaceB -= gridB[i][j] * 1.0;
                
                // Gray-Scott reaction-diffusion formula
                double reaction = a * b * b;
                
                // Update rules
                nextA[i][j] = a + (dA * laplaceA - reaction + feed * (1 - a)) * 0.9;
                nextB[i][j] = b + (dB * laplaceB + reaction - (kill + feed) * b) * 0.9;
                
                // Constrain values for stability
                nextA[i][j] = Math.max(0, Math.min(1, nextA[i][j]));
                nextB[i][j] = Math.max(0, Math.min(1, nextB[i][j]));
                
                // Copy values to neighboring cells when using skip
                if (skip > 1) {
                    for (int di = 0; di < skip && i + di < width; di++) {
                        for (int dj = 0; dj < skip && j + dj < height; dj++) {
                            if (di != 0 || dj != 0) {
                                nextA[i + di][j + dj] = nextA[i][j];
                                nextB[i + di][j + dj] = nextB[i][j];
                            }
                        }
                    }
                }
            }
        }
    }
}