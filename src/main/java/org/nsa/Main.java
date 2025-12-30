package org.nsa;

import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.*;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private final Main thisApp;
    private final JFrame frame;

    private final JButton pauseButton;
    private final JButton stopButton;
    private final JButton rewindButton;
    private final JButton skipButton;
    private final JButton muteButton;
    private JSlider volumeSlider;
    private final JButton dfVolButton;
    private JSlider speedSlider;
    private JButton resetSpeedBtn;
    private JLabel speedLabel;
    private JProgressBar progressBar;
    private JLabel timeLabel;
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

    public static void main(String[] args) {
        new Main(args);
    }

    private String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60));

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Helper method to calculate position
    private void seek(MouseEvent e) {
        float pos = (float) e.getX() / (float) progressBar.getWidth();
        // Constrain position between 0 and 1
        pos = Math.max(0, Math.min(1, pos));
        mediaPlayerComponent.mediaPlayer().controls().setPosition(pos);
        progressBar.setValue((int)(pos * 100));
    }

    public Main(String[] args){
        this.thisApp = this;

        this.frame = new JFrame("My First Media Player");
        frame.setBounds(100, 100, 600, 400);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
                // newPosition is a value between 0.0 and 1.0
                int value = (int) (newPosition * 100);

                // Only update if the user is NOT dragging the mouse (to prevent fighting)
                    progressBar.setValue(value);

                    // Update your time label here too
                    long current = mediaPlayer.status().time();
                    long total = mediaPlayer.status().length();
                    timeLabel.setText(String.format("%s / %s (Left: %s)", formatTime(current), formatTime(total), formatTime(total-current)));
            }

//            @Override
//            public void playing(MediaPlayer mediaPlayer) {
//            }
//            @Override
//            public void stopped(MediaPlayer mediaPlayer) {
//
//            }
//            @Override
//            public void paused(MediaPlayer mediaPlayer) {
//
//            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Congratulations! You have successfully finished the video/audio!",
                                "Celebration",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                });
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Failed to play media",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);

        JPanel controlsPane = new JPanel();
        JPanel progressPanel = new JPanel(new BorderLayout(1, 0));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        timeLabel = new JLabel("00:00 / 00:00 (Left: 00:00)");
        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                seek(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                seek(e);
            }
        });

        progressBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                seek(e); // This allows dragging the bar smoothly
            }
        });


        progressPanel.add(timeLabel, BorderLayout.WEST);
        progressPanel.add(progressBar, BorderLayout.CENTER);

// Finally, add this progressPanel to the top of your main controlsPane
        controlsPane.add(progressPanel, BorderLayout.NORTH);

        JPanel buttonPane = new JPanel();

        pauseButton = new JButton("Play/Pause");
        buttonPane.add(pauseButton);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!mediaPlayerComponent.mediaPlayer().status().isPlaying()) {
                    mediaPlayerComponent.mediaPlayer().controls().play();
                }
                else {
                    mediaPlayerComponent.mediaPlayer().controls().pause();

                }
            }
        });

        stopButton = new JButton("Stop");
        buttonPane.add(stopButton);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().stop();

            }
        });

        rewindButton = new JButton("Rewind");
        buttonPane.add(rewindButton);
        rewindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().skipTime(-10000);
            }
        });

        skipButton = new JButton("Skip");
        buttonPane.add(skipButton);
        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().skipTime(10000);
            }
        });

// 1. Setup Slider (Range 0 to 200, starting at 100)
        volumeSlider = new JSlider(0, 200, 100);
        volumeSlider.setMajorTickSpacing(50);
        volumeSlider.setMinorTickSpacing(10);
        volumeSlider.setPaintTicks(true);

// 2. Slider Logic
        volumeSlider.addChangeListener(e -> {
            int volume = volumeSlider.getValue();
            mediaPlayerComponent.mediaPlayer().audio().setVolume(volume);
            // Optional: update a label if you have one, e.g., volLabel.setText(volume + "%");
        });

// 3. Mute Button (Toggles mute)
        muteButton = new JButton("Mute");

        dfVolButton = new JButton("DfVol");

        muteButton.addActionListener(e -> {
            // 1. Get current state and flip it
            boolean currentlyMuted = mediaPlayerComponent.mediaPlayer().audio().isMute();
            boolean newState = !currentlyMuted;

            // 2. Set the state explicitly so there is no guesswork
            mediaPlayerComponent.mediaPlayer().audio().setMute(newState);

            // 3. Sync the UI to the NEW state
            volumeSlider.setEnabled(!newState);
            dfVolButton.setEnabled(!newState);
            muteButton.setText(newState ? "Unmute" : "Mute");
        });

// 4. Default Volume Button (100%)
        dfVolButton.addActionListener(e -> {
            volumeSlider.setValue(100); // This automatically triggers the ChangeListener
        });

//        buttonPane.add(muteButton);
        JPanel volPane = new JPanel();
        volPane.add(muteButton);
        volPane.add(volumeSlider);
        volPane.add(dfVolButton);

        progressPanel.add(volPane, BorderLayout.EAST);
//        buttonPane.add(dfVolButton);

        speedLabel = new JLabel("Speed: 1.00x");
        buttonPane.add(speedLabel);


        speedSlider = new JSlider(25, 300, 100);
        speedSlider.setMajorTickSpacing(25); // Optional: Marks every 0.25x
        speedSlider.setPaintTicks(true);
        speedSlider.setMinorTickSpacing(5);
        speedSlider.setSnapToTicks(true);
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        labelTable.put(25, new JLabel("0.25"));
        labelTable.put(100, new JLabel("1.0"));
        labelTable.put(200, new JLabel("2.0"));
        labelTable.put(300, new JLabel("3.0"));
        speedSlider.setLabelTable(labelTable);
        speedSlider.setPaintLabels(true);

        buttonPane.add(speedSlider);
        speedSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();

            // Convert the integer (25-300) to the float rate (0.25-3.00)
            float snappedRate = (Math.round(source.getValue() / 5.0f) * 5) / 100.0f;

            // Update the label immediately so the user sees the numbers change while dragging
            speedLabel.setText(String.format("Speed: %.2fx", snappedRate));

            // Only tell the Media Player to change once the user lets go of the mouse
            // This prevents the audio from stuttering while dragging
            if (!source.getValueIsAdjusting()) {
                mediaPlayerComponent.mediaPlayer().controls().setRate(snappedRate);
            }
        });
        resetSpeedBtn = new JButton("Reset");
        resetSpeedBtn.addActionListener(e -> {
            // Simply moving the slider will trigger the ChangeListener above
            speedSlider.setValue(100);
        });
        buttonPane.add(resetSpeedBtn);
        controlsPane.add(buttonPane, BorderLayout.CENTER);

        contentPane.add(controlsPane, BorderLayout.SOUTH);

        frame.setContentPane(contentPane);
        frame.setVisible(true);
        mediaPlayerComponent.mediaPlayer().media().play(args[0]);
    }
}