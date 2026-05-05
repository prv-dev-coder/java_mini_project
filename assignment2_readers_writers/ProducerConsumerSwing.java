import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumerSwing extends JFrame {

    private JButton startBtn, stopBtn;
    private JTextArea bufferArea;
    private JLabel producerStatus, consumerStatus;
    private JLabel producedCountLabel, consumedCountLabel;

    private JProgressBar bufferBar;
    private AnimationPanel animationPanel;

    private final int MAX_SIZE = 5;
    private final Queue<Integer> buffer = new LinkedList<>();

    private boolean running = false;
    private Thread producerThread, consumerThread;

    private int producedCount = 0;
    private int consumedCount = 0;

    public ProducerConsumerSwing() {
        setTitle("Producer-Consumer Simulation (Advanced UI)");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBackground(new Color(20,20,30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setContentPane(mainPanel);

        // TOP PANEL
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(20,20,30));

        startBtn = createButton("Start", new Color(0,200,120));
        stopBtn = createButton("Stop", new Color(220,50,50));

        topPanel.add(startBtn);
        topPanel.add(stopBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // CENTER PANEL
        JPanel centerPanel = new JPanel(new GridLayout(1,2,10,10));
        centerPanel.setBackground(new Color(20,20,30));

        // BUFFER DISPLAY
        bufferArea = new JTextArea();
        bufferArea.setEditable(false);
        bufferArea.setBackground(new Color(30,30,45));
        bufferArea.setForeground(new Color(0,255,200));
        bufferArea.setFont(new Font("Consolas", Font.BOLD, 16));

        JScrollPane scroll = new JScrollPane(bufferArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Buffer"));

        centerPanel.add(scroll);

        // ANIMATION PANEL
        animationPanel = new AnimationPanel();
        centerPanel.add(animationPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // BOTTOM PANEL
        JPanel bottomPanel = new JPanel(new GridLayout(3,2,5,5));
        bottomPanel.setBackground(new Color(20,20,30));

        producerStatus = createLabel("Producer: Idle", Color.GREEN);
        consumerStatus = createLabel("Consumer: Idle", Color.RED);

        producedCountLabel = createLabel("Produced: 0", Color.CYAN);
        consumedCountLabel = createLabel("Consumed: 0", Color.ORANGE);

        bufferBar = new JProgressBar(0, MAX_SIZE);
        bufferBar.setStringPainted(true);

        bottomPanel.add(producerStatus);
        bottomPanel.add(consumerStatus);
        bottomPanel.add(producedCountLabel);
        bottomPanel.add(consumedCountLabel);
        bottomPanel.add(new JLabel("Buffer Usage:"));
        bottomPanel.add(bufferBar);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> startSimulation());
        stopBtn.addActionListener(e -> stopSimulation());
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        return btn;
    }

    private JLabel createLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        return label;
    }

    private void startSimulation() {
        running = true;

        producerThread = new Thread(new Producer());
        consumerThread = new Thread(new Consumer());

        producerThread.start();
        consumerThread.start();
    }

    private void stopSimulation() {
        running = false;
        producerThread.interrupt();
        consumerThread.interrupt();
    }

    private void updateUIAll() {
        SwingUtilities.invokeLater(() -> {
            bufferArea.setText(buffer.toString());
            bufferBar.setValue(buffer.size());
            producedCountLabel.setText("Produced: " + producedCount);
            consumedCountLabel.setText("Consumed: " + consumedCount);
            animationPanel.repaint();
        });
    }

    // PRODUCER
    class Producer implements Runnable {
        private int value = 1;

        public void run() {
            while (running) {
                synchronized (buffer) {
                    try {
                        while (buffer.size() == MAX_SIZE) {
                            producerStatus.setText("Buffer Full – Waiting");
                            buffer.wait();
                        }

                        buffer.add(value++);
                        producedCount++;
                        producerStatus.setText("Producing...");
                        animationPanel.setMode("PRODUCE");

                        updateUIAll();
                        buffer.notify();

                    } catch (InterruptedException e) {
                        return;
                    }
                }
                sleep(800);
            }
        }
    }

    // CONSUMER
    class Consumer implements Runnable {
        public void run() {
            while (running) {
                synchronized (buffer) {
                    try {
                        while (buffer.isEmpty()) {
                            consumerStatus.setText("Buffer Empty – Waiting");
                            buffer.wait();
                        }

                        buffer.poll();
                        consumedCount++;
                        consumerStatus.setText("Consuming...");
                        animationPanel.setMode("CONSUME");

                        updateUIAll();
                        buffer.notify();

                    } catch (InterruptedException e) {
                        return;
                    }
                }
                sleep(1200);
            }
        }
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ANIMATION PANEL
    class AnimationPanel extends JPanel {
        private String mode = "";

        public AnimationPanel() {
            setBackground(new Color(25,25,40));
        }

        public void setMode(String m) {
            mode = m;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.WHITE);
            g.drawString("Producer", 50, 50);
            g.drawString("Buffer", 200, 50);
            g.drawString("Consumer", 350, 50);

            if (mode.equals("PRODUCE")) {
                g.setColor(Color.GREEN);
                g.fillOval(120, 70, 20, 20);
            } else if (mode.equals("CONSUME")) {
                g.setColor(Color.RED);
                g.fillOval(280, 70, 20, 20);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ProducerConsumerSwing().setVisible(true);
        });
    }
}