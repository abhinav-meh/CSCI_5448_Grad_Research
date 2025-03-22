import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class LSystem extends JFrame {
    private static final long serialVersionUID = 1L;
    private LSystemPanel panel;
    private JPanel controlPanel;
    private JComboBox<String> presetComboBox;
    private JTextField axiomField, iterationsField, angleField;
    private JTextArea rulesArea;
    private JButton generateButton;
    
   
    private String axiom = "F";
    private int iterations = 4;
    private double angle = 25.0;
    private Map<Character, String> rules = new HashMap<>();
    
    public LSystem() {
        setTitle("L-System Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        
        rules.put('F', "FF+[+F-F-F]-[-F+F+F]");
        
       
        panel = new LSystemPanel();
        panel.setPreferredSize(new Dimension(800, 600));
        
       
        setupControlPanel();
        
        
        add(panel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        
        generateLSystem();
    }
    
    private void setupControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
       
        JLabel presetLabel = new JLabel("Presets:");
        presetComboBox = new JComboBox<>(new String[]{
            "Plant 1", "Plant 2", "Koch Curve", "Sierpinski Triangle", "Dragon Curve", "Custom"
        });
        presetComboBox.addActionListener(e -> applyPreset());
        
       
        JLabel axiomLabel = new JLabel("Axiom:");
        axiomField = new JTextField(axiom, 10);
        
        
        JLabel iterationsLabel = new JLabel("Iterations:");
        iterationsField = new JTextField(String.valueOf(iterations), 5);
        
        
        JLabel angleLabel = new JLabel("Angle (degrees):");
        angleField = new JTextField(String.valueOf(angle), 5);
        
        
        JLabel rulesLabel = new JLabel("Rules (one per line, format: X=YYYY):");
        rulesArea = new JTextArea(5, 20);
        rulesArea.setText("F=FF+[+F-F-F]-[-F+F+F]");
        JScrollPane rulesScrollPane = new JScrollPane(rulesArea);
        
        
        generateButton = new JButton("Generate");
        generateButton.addActionListener(e -> generateLSystem());
        
       
        controlPanel.add(presetLabel);
        controlPanel.add(presetComboBox);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(axiomLabel);
        controlPanel.add(axiomField);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(iterationsLabel);
        controlPanel.add(iterationsField);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(angleLabel);
        controlPanel.add(angleField);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(rulesLabel);
        controlPanel.add(rulesScrollPane);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(generateButton);
    }
    
    private void applyPreset() {
        String selectedPreset = (String) presetComboBox.getSelectedItem();
        
        switch (selectedPreset) {
            case "Plant 1":
                axiomField.setText("F");
                iterationsField.setText("4");
                angleField.setText("25");
                rulesArea.setText("F=FF+[+F-F-F]-[-F+F+F]");
                break;
            case "Plant 2":
                axiomField.setText("X");
                iterationsField.setText("5");
                angleField.setText("25");
                rulesArea.setText("X=F+[[X]-X]-F[-FX]+X\nF=FF");
                break;
            case "Koch Curve":
                axiomField.setText("F");
                iterationsField.setText("4");
                angleField.setText("60");
                rulesArea.setText("F=F+F-F-F+F");
                break;
            case "Sierpinski Triangle":
                axiomField.setText("F-G-G");
                iterationsField.setText("5");
                angleField.setText("120");
                rulesArea.setText("F=F-G+F+G-F\nG=GG");
                break;
            case "Dragon Curve":
                axiomField.setText("FX");
                iterationsField.setText("10");
                angleField.setText("90");
                rulesArea.setText("X=X+YF+\nY=-FX-Y");
                break;
            case "Custom":
                
                break;
        }
    }
    
    private void generateLSystem() {
        try {
            
            axiom = axiomField.getText();
            iterations = Integer.parseInt(iterationsField.getText());
            angle = Double.parseDouble(angleField.getText());
            
        
            rules.clear();
            String[] ruleLines = rulesArea.getText().split("\n");
            for (String rule : ruleLines) {
                if (rule.contains("=") && rule.length() >= 3) {
                    char key = rule.charAt(0);
                    String value = rule.substring(2);
                    rules.put(key, value);
                }
            }
            
           
            panel.updateLSystem(axiom, rules, iterations, angle);
            panel.repaint();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers for iterations and angle.", 
                "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private class LSystemPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private String lSystemString;
        private double drawAngle;
        
        public LSystemPanel() {
            setBackground(Color.WHITE);
            lSystemString = "";
            drawAngle = 25.0;
        }
        
        public void updateLSystem(String axiom, Map<Character, String> rules, int iterations, double angle) {
           
            lSystemString = axiom;
            for (int i = 0; i < iterations; i++) {
                StringBuilder nextGen = new StringBuilder();
                for (int j = 0; j < lSystemString.length(); j++) {
                    char current = lSystemString.charAt(j);
                    if (rules.containsKey(current)) {
                        nextGen.append(rules.get(current));
                    } else {
                        nextGen.append(current);
                    }
                }
                lSystemString = nextGen.toString();
            }
            
            this.drawAngle = angle;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
           
            if (lSystemString.isEmpty()) {
                return;
            }
            
            
            g2d.setColor(new Color(0, 100, 0));  
            g2d.setStroke(new BasicStroke(1.0f));
            
            
            int startX = getWidth() / 2;
            int startY = getHeight() - 50;
            
            
            drawLSystem(g2d, startX, startY);
        }
        
        private void drawLSystem(Graphics2D g2d, int startX, int startY) {
           
            double scale = Math.min(1.0, 10000.0 / lSystemString.length());
            double length = 10.0 * scale;
            
            
            double x = startX;
            double y = startY;
            double angle = -90.0;  
            
            
            Stack<double[]> stack = new Stack<>();
            
            
            for (int i = 0; i < lSystemString.length(); i++) {
                char c = lSystemString.charAt(i);
                
                switch (c) {
                    case 'F': 
                    case 'G': 
                        double x2 = x + length * Math.cos(Math.toRadians(angle));
                        double y2 = y + length * Math.sin(Math.toRadians(angle));
                        g2d.drawLine((int) x, (int) y, (int) x2, (int) y2);
                        x = x2;
                        y = y2;
                        break;
                    case 'f':  
                        x += length * Math.cos(Math.toRadians(angle));
                        y += length * Math.sin(Math.toRadians(angle));
                        break;
                    case '+': 
                        angle += drawAngle;
                        break;
                    case '-':  
                        angle -= drawAngle;
                        break;
                    case '[':  
                        stack.push(new double[]{x, y, angle});
                        break;
                    case ']':  
                        if (!stack.isEmpty()) {
                            double[] state = stack.pop();
                            x = state[0];
                            y = state[1];
                            angle = state[2];
                        }
                        break;
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LSystem());
    }
}