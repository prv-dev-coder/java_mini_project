import javax.swing.*;
import java.awt.*;

public class ReadersWritersSwing extends JFrame {

    private JTextArea logArea;
    private JButton startBtn;
    private JLabel readerCountLabel, writerStatusLabel;

    private AnimationPanel animationPanel;

    private int readCount = 0;
    private boolean writing = false;
    private int data = 0;

    private boolean running = false;

    public ReadersWritersSwing() {
        setTitle("Readers-Writers Simulation");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBackground(new Color(20,20,30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setContentPane(mainPanel);

        // TOP
        JPanel top = new JPanel();
        top.setBackground(new Color(20,20,30));

        startBtn = new JButton("Start Simulation");
        startBtn.setBackground(new Color(0,200,120));
        top.add(startBtn);

        mainPanel.add(top, BorderLayout.NORTH);

        // CENTER
        JPanel center = new JPanel(new GridLayout(1,2,10,10));
        center.setBackground(new Color(20,20,30));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(30,30,45));
        logArea.setForeground(Color.GREEN);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Logs"));

        center.add(scroll);

        animationPanel = new AnimationPanel();
        center.add(animationPanel);

        mainPanel.add(center, BorderLayout.CENTER);

        // BOTTOM
        JPanel bottom = new JPanel(new GridLayout(2,1));
        bottom.setBackground(new Color(20,20,30));

        readerCountLabel = new JLabel("Active Readers: 0");
        readerCountLabel.setForeground(Color.CYAN);

        writerStatusLabel = new JLabel("Writer: Idle");
        writerStatusLabel.setForeground(Color.RED);

        bottom.add(readerCountLabel);
        bottom.add(writerStatusLabel);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> startSimulation());
    }

    private synchronized void startReading(int id) throws InterruptedException {
        while (writing) wait();

        readCount++;
        log("Reader " + id + " is reading...");
        updateUIState();
    }

    private synchronized void stopReading(int id) {
        readCount--;
        log("Reader " + id + " finished reading");

        if (readCount == 0) notifyAll();
        updateUIState();
    }

    private synchronized void startWriting(int id) throws InterruptedException {
        while (writing || readCount > 0) wait();

        writing = true;
        data++;
        log("Writer " + id + " is writing... Data=" + data);
        updateUIState();
    }

    private synchronized void stopWriting(int id) {
        writing = false;
        log("Writer " + id + " finished writing");

        notifyAll();
        updateUIState();
    }

    private void startSimulation() {
        running = true;

        // Multiple readers
        for (int i = 1; i <= 3; i++) {
            int id = i;
            new Thread(() -> {
                while (running) {
                    try {
                        startReading(id);
                        animationPanel.setMode("READ");
                        Thread.sleep(1000);
                        stopReading(id);
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {}
                }
            }).start();
        }

        // Writers
        for (int i = 1; i <= 2; i++) {
            int id = i;
            new Thread(() -> {
                while (running) {
                    try {
                        Thread.sleep(2000);
                        startWriting(id);
                        animationPanel.setMode("WRITE");
                        Thread.sleep(1500);
                        stopWriting(id);
                    } catch (InterruptedException e) {}
                }
            }).start();
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
        });
    }

    private void updateUIState() {
        SwingUtilities.invokeLater(() -> {
            readerCountLabel.setText("Active Readers: " + readCount);
            writerStatusLabel.setText("Writer: " + (writing ? "Writing..." : "Idle"));
            animationPanel.repaint();
        });
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
            g.drawString("Readers", 80, 50);
            g.drawString("Shared Data", 200, 50);
            g.drawString("Writer", 350, 50);

            if (mode.equals("READ")) {
                g.setColor(Color.CYAN);
                g.fillOval(120, 80, 20, 20);
            } else if (mode.equals("WRITE")) {
                g.setColor(Color.RED);
                g.fillOval(260, 80, 20, 20);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ReadersWritersSwing().setVisible(true);
        });
    }
}