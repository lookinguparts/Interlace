package art.lookingup.wavetable;

import javax.swing.*;
import java.awt.*;

public class WavetableGenerator extends JFrame {
    private static final int WAVETABLE_SIZE = 32;
    private static final int CANVAS_WIDTH = 600;
    private static final int CANVAS_HEIGHT = 400;
    private static final int MARGIN = 20;
    private static final int GRAPH_WIDTH = CANVAS_WIDTH - 2 * MARGIN;
    private static final int GRAPH_HEIGHT = CANVAS_HEIGHT - 2 * MARGIN;

    private static final int LED_STRIP_LENGTH = 36; // 3 feet in inches
    private static final float LED_SPACING = 0.2f; // 1 inch spacing between LEDs
    private static final int LED_SIZE = 3;
    private static final int NUM_LEDS = (int)(LED_STRIP_LENGTH / LED_SPACING) + 1;

    private static final float WAVE_WIDTH = 24f;
    private static final float WAVE_MAX = 1.0f;
    private static final float WAVE_OFFSET = 0.0f;

    Wavetable wavetable;

    public WavetableGenerator() {
        setTitle("Wavetable Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(CANVAS_WIDTH + 30, CANVAS_HEIGHT + 30);
        setLocationRelativeTo(null);

        wavetable = new SineWavetable(WAVETABLE_SIZE);
        // Physical width is 12" (1 foot)
        wavetable = new TriangleWavetable(WAVETABLE_SIZE);
        //wavetable = new art.lookingup.wavetable.StepWavetable(WAVETABLE_SIZE);
        wavetable.generateWavetable(WAVE_MAX, WAVE_OFFSET);
        Ease ease = new EasePow(3);
        wavetable.ease(ease);

        JPanel wavetablePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawWavetable(g);

            }
        };

        JPanel ledStripPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawLEDStrip(g);
            }
        };
        ledStripPanel.setPreferredSize(new Dimension(CANVAS_WIDTH, 100));

        setLayout(new BorderLayout());
        add(wavetablePanel, BorderLayout.CENTER);
        add(ledStripPanel, BorderLayout.SOUTH);


        Timer timer = new Timer(50, e -> {
            wavetable.pos += 1;
            if (wavetable.pos > LED_STRIP_LENGTH) {
                wavetable.pos = 0;
            }
            repaint();
        });
        timer.start();

    }

    private void drawWavetable(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(MARGIN-5, MARGIN-5, GRAPH_WIDTH+10, GRAPH_HEIGHT+10);

        g2d.setColor(Color.WHITE);
        g2d.drawLine(MARGIN, GRAPH_HEIGHT / 2 + MARGIN + 5, CANVAS_WIDTH - MARGIN, GRAPH_HEIGHT / 2 + MARGIN + 5);
        g2d.setColor(Color.YELLOW);
        for (int i = 0; i < WAVETABLE_SIZE + 1; i++) {
            int x1 = MARGIN + i * GRAPH_WIDTH / (WAVETABLE_SIZE + 1);
            int y1 = MARGIN + (int) ((1 - wavetable.samples[i]) * GRAPH_HEIGHT / 2);
            //g2d.drawLine(x1, y1, x2, y2);
            if (i == WAVETABLE_SIZE) g2d.setColor(Color.RED);
            g2d.drawOval(x1, y1, 5, 5);
        }
    }

    private void drawLEDStrip(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, CANVAS_WIDTH, 100);

        int ledX = MARGIN/2;
        for (int i = 0; i < NUM_LEDS; i++) {
            // remap from screen space to strip space by removing the margin.
            float stripSpaceX = i * LED_SPACING;
            float wavetableValue = wavetable.getSample(stripSpaceX, WAVE_WIDTH);
            int whiteLevel = (int) (wavetableValue * 255);
            Color ledColor = new Color(whiteLevel, whiteLevel, whiteLevel);
            g2d.setColor(ledColor);
            g2d.fillOval(ledX, 50 - LED_SIZE / 2, LED_SIZE, LED_SIZE);
            ledX += LED_SPACING * CANVAS_WIDTH / LED_STRIP_LENGTH;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WavetableGenerator generator = new WavetableGenerator();
            generator.setVisible(true);
        });
    }
}
