package org.example;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.message.StringMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UserAgent extends Agent {

    private JFrame frame;
    private JTextArea outputArea;
    private JTextField inputField;
    private JButton addButton, showButton, predictButton, clearButton;
    private JPanel quickActionsPanel;

    @Override
    protected void activate() {
        getLogger().info("UserAgent activating...");

        madkit.kernel.AbstractAgent.ReturnCode roleResult =
                requestRole("grocery", "main", "user");
        getLogger().info("Request user role result: " + roleResult);

        pause(2000);
        getLogger().info("UserAgent ready!");

        // Create GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(this::createGUI);
    }

    private void createGUI() {
        frame = new JFrame("NutriCart Assistant");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(10, 10));

        // Add window listener for proper shutdown
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        frame,
                        "Are you sure you want to exit?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION
                );
                if (result == JOptionPane.YES_OPTION) {
                    appendOutput("\n Goodbye! Shutting down...");
                    pause(500);
                    System.exit(0);
                }
            }
        });

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        frame.add(headerPanel, BorderLayout.NORTH);

        // Output Area (Center)
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setBackground(new Color(245, 245, 245));
        outputArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        frame.add(scrollPane, BorderLayout.CENTER);

        // Quick Actions Panel (East)
        quickActionsPanel = createQuickActionsPanel();
        frame.add(quickActionsPanel, BorderLayout.EAST);

        // Input Panel (South)
        JPanel inputPanel = createInputPanel();
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Show welcome message
        showWelcomeMessage();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        inputField.requestFocus();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("NutriCart Assistant");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Manage your groceries intelligently");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(230, 230, 230));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        headerPanel.add(textPanel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        panel.setPreferredSize(new Dimension(180, 0));

        // Create buttons
        addButton = createStyledButton("Add Item", new Color(46, 125, 50));
        showButton = createStyledButton("Show List", new Color(25, 118, 210));
        predictButton = createStyledButton("Predictions", new Color(156, 39, 176));
        clearButton = createStyledButton("Clear Log", new Color(198, 40, 40));

        JButton expButton = createStyledButton("Add w/ Expiry", new Color(255, 152, 0));
        JButton healthButton = createStyledButton("Healthy Alt", new Color(0, 150, 136));
        JButton exitButton = createStyledButton("Exit", new Color(84, 110, 122));

        // Add action listeners
        addButton.addActionListener(e -> showAddItemDialog());
        showButton.addActionListener(e -> sendCommand("show"));
        predictButton.addActionListener(e -> sendCommand("predict"));
        clearButton.addActionListener(e -> outputArea.setText(""));
        expButton.addActionListener(e -> showAddItemWithExpiryDialog());
        healthButton.addActionListener(e -> showHealthyAlternativeDialog());
        exitButton.addActionListener(e -> frame.dispatchEvent(
                new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));

        panel.add(addButton);
        panel.add(expButton);
        panel.add(showButton);
        panel.add(predictButton);
        panel.add(healthButton);
        panel.add(clearButton);
        panel.add(exitButton);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
//        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel promptLabel = new JLabel("Command:");
        promptLabel.setFont(new Font("Arial", Font.BOLD, 12));

        inputField = new JTextField();
        inputField.setFont(new Font("Consolas", Font.PLAIN, 13));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Action listeners
        inputField.addActionListener(e -> processSendCommand());
        sendButton.addActionListener(e -> processSendCommand());

        panel.add(promptLabel, BorderLayout.WEST);
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private void showWelcomeMessage() {
        appendOutput("╔════════════════════════════════════════╗");
        appendOutput("║   Welcome to NutriCart Assistant       ║");
        appendOutput("╚════════════════════════════════════════╝");
        appendOutput("");
        appendOutput("Use the buttons on the right for quick actions,");
        appendOutput("or type commands below:");
        appendOutput("  • add <item>");
        appendOutput("  • exp <item> <days>");
        appendOutput("  • show");
        appendOutput("  • predict");
        appendOutput("  • healthy <item>");
        appendOutput("");
        appendOutput("Ready to assist you!");
        appendOutput("═══════════════════════════════════════════");
        appendOutput("");
    }

    private void showAddItemDialog() {
        String item = JOptionPane.showInputDialog(
                frame,
                "Enter item name:",
                "Add Item",
                JOptionPane.QUESTION_MESSAGE
        );

        if (item != null && !item.trim().isEmpty()) {
            sendCommand("add " + item.trim());
        }
    }

    private void showAddItemWithExpiryDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField itemField = new JTextField();
        JTextField daysField = new JTextField();

        panel.add(new JLabel("Item:"));
        panel.add(itemField);
        panel.add(new JLabel("Days until expiry:"));
        panel.add(daysField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Add Item with Expiry",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String item = itemField.getText().trim();
            String days = daysField.getText().trim();

            if (!item.isEmpty() && !days.isEmpty()) {
                sendCommand("exp " + item + " " + days);
            }
        }
    }

    private void showHealthyAlternativeDialog() {
        String item = JOptionPane.showInputDialog(
                frame,
                "Enter item to find healthy alternative:",
                "Healthy Alternative",
                JOptionPane.QUESTION_MESSAGE
        );

        if (item != null && !item.trim().isEmpty()) {
            sendCommand("healthy " + item.trim());
        }
    }

    private void processSendCommand() {
        String input = inputField.getText().trim();
        inputField.setText("");

        if (input.isEmpty()) {
            return;
        }

        appendOutput(">> " + input);
        sendCommand(input);
    }

    private void sendCommand(String command) {
        madkit.kernel.AbstractAgent.ReturnCode sent =
                sendMessage("grocery", "main", "groceryAgent",
                        new StringMessage(command));

        if (sent != madkit.kernel.AbstractAgent.ReturnCode.SUCCESS) {
            appendOutput("⚠ Warning: Could not send message! ReturnCode: " + sent);
        }

        pause(100);
    }

    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    @Override
    protected void live() {
        // Listen for messages from other agents and display in GUI
        while (true) {
            Message msg = waitNextMessage(100);

            if (msg instanceof StringMessage) {
                String content = ((StringMessage) msg).getContent();

                // Check if it's a log message from other agents
                if (content.startsWith("LOG:")) {
                    String logMessage = content.substring(4);
                    appendOutput(logMessage);
                }
            }

            pause(100);
        }
    }

    @Override
    protected void end() {
        getLogger().info("UserAgent shutting down...");
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }
}