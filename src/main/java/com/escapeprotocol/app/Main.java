package com.escapeprotocol.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Application entry point for Escape Protocol - The Last Exit.
 *
 * <p>Module 02 only opens a basic dark-themed window to prove that the
 * project builds and runs. Gameplay is added in later modules.
 */
public final class Main {

    /** Window title. Public so tests can check it without opening a window. */
    public static final String TITLE = "Escape Protocol — The Last Exit";

    /** Subtitle shown under the title. */
    public static final String SUBTITLE = "A Java Swing escape-room puzzle game";

    /** Simple welcome text. */
    public static final String WELCOME_MESSAGE = "Welcome, Operative. The facility is locked.";

    /** Current development status shown in the window. */
    public static final String STATUS_MESSAGE =
            "Module 02: project foundation only. Gameplay arrives in later modules.";

    private static final Color BACKGROUND = new Color(0x121212);
    private static final Color TITLE_COLOR = new Color(0x9B59B6);   // purple
    private static final Color TEXT_COLOR = new Color(0xE0E0E0);    // light grey
    private static final Color STATUS_COLOR = new Color(0x6B8E6B);  // muted green

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;

    private Main() {
        // Utility class: not meant to be instantiated.
    }

    /**
     * Starts the application. Swing components must be created on the
     * Event Dispatch Thread, so the work is handed to invokeLater.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowWindow);
    }

    private static void createAndShowWindow() {
        JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setMinimumSize(new Dimension(600, 400));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(Box.createVerticalGlue());
        content.add(makeLabel("ESCAPE PROTOCOL", new Font("Monospaced", Font.BOLD, 48), TITLE_COLOR));
        content.add(Box.createVerticalStrut(8));
        content.add(makeLabel("THE LAST EXIT", new Font("Monospaced", Font.BOLD, 24), TEXT_COLOR));
        content.add(Box.createVerticalStrut(30));
        content.add(makeLabel(WELCOME_MESSAGE, new Font("SansSerif", Font.PLAIN, 18), TEXT_COLOR));
        content.add(Box.createVerticalStrut(12));
        content.add(makeLabel(STATUS_MESSAGE, new Font("SansSerif", Font.ITALIC, 14), STATUS_COLOR));
        content.add(Box.createVerticalGlue());

        frame.setContentPane(content);

        // Center the window on the screen.
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screen.width - WINDOW_WIDTH) / 2, (screen.height - WINDOW_HEIGHT) / 2);

        frame.setVisible(true);
    }

    private static JLabel makeLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        return label;
    }
}