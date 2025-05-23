package app_runner;

import javax.swing.*;
import java.awt.*;
import panels.*;
import api_calls.*;
import database.*;

/**
 * Main application class that initializes the Combined AI Assistant
 * This class sets up the main frame, arranges all panels, and starts the application
 */
public class App_runner {
    private JFrame mainFrame;
    private ChatGPT_panel chatGPTPanel;
    private Perplexity_panel perplexityPanel;
    private Input_panel inputPanel;
    private Background_panel backgroundPanel;
    private Database_handler dbHandler;
    
    /**
     * Constructor that initializes all UI components and database connection
     */
    public App_runner() {
        // Initialize the database handler for conversation storage
        dbHandler = new Database_handler();
        
        // Initialize the background task handler
        backgroundPanel = new Background_panel();
        
        // Set up the main application window
        mainFrame = new JFrame("Combined AI Assistant");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 800);
        mainFrame.setLayout(new BorderLayout());
        
        // Set up the API handlers for both AI services
        ChatGPT_api_handler chatGPTHandler = new ChatGPT_api_handler();
        Perplexity_api_handler perplexityHandler = new Perplexity_api_handler();
        
        // Initialize UI panels
        chatGPTPanel = new ChatGPT_panel(chatGPTHandler);
        perplexityPanel = new Perplexity_panel(perplexityHandler);
        inputPanel = new Input_panel(chatGPTHandler, perplexityHandler, dbHandler);
        
        // Connect the panels
        inputPanel.setPanels(chatGPTPanel, perplexityPanel);
        
        // Create a split pane for the two AI response panels
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(chatGPTPanel),
            new JScrollPane(perplexityPanel)
        );
        splitPane.setResizeWeight(0.5); // Equal resizing
        
        // Add components to the main frame
        mainFrame.add(splitPane, BorderLayout.CENTER);
        mainFrame.add(inputPanel, BorderLayout.SOUTH);
        
        // Setup menu
        setupMenu();
        
        // Add shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdownApplication();
        }));
    }
    
    /**
     * Creates and attaches the application menu
     */
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export Conversation");
        JMenuItem clearItem = new JMenuItem("Clear All");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        fileMenu.add(exportItem);
        fileMenu.add(clearItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Settings menu
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem apiSettingsItem = new JMenuItem("API Settings");
        JMenuItem themeItem = new JMenuItem("Theme");
        
        settingsMenu.add(apiSettingsItem);
        settingsMenu.add(themeItem);
        
        // Add action listeners
        exitItem.addActionListener(e -> shutdownApplication());
        clearItem.addActionListener(e -> {
            chatGPTPanel.clearConversation();
            perplexityPanel.clearConversation();
        });
        
        apiSettingsItem.addActionListener(e -> showApiSettingsDialog());
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        
        // Set the menu bar
        mainFrame.setJMenuBar(menuBar);
    }
    
    /**
     * Shows the API settings dialog
     */
    private void showApiSettingsDialog() {
        JDialog settingsDialog = new JDialog(mainFrame, "API Settings", true);
        settingsDialog.setLayout(new GridLayout(0, 2, 10, 10));
        settingsDialog.setSize(400, 200);
        
        // ChatGPT API key field
        JLabel chatGPTLabel = new JLabel("ChatGPT API Key:");
        JPasswordField chatGPTField = new JPasswordField(20);
        
        // Perplexity API key field
        JLabel perplexityLabel = new JLabel("Perplexity API Key:");
        JPasswordField perplexityField = new JPasswordField(20);
        
        // Buttons
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        // Add components
        settingsDialog.add(chatGPTLabel);
        settingsDialog.add(chatGPTField);
        settingsDialog.add(perplexityLabel);
        settingsDialog.add(perplexityField);
        settingsDialog.add(saveButton);
        settingsDialog.add(cancelButton);
        
        // Action listeners
        saveButton.addActionListener(e -> {
            // Get values from fields
            String chatGPTKey = new String(chatGPTField.getPassword());
            String perplexityKey = new String(perplexityField.getPassword());
            
            // Save the API keys securely
            if (!chatGPTKey.isEmpty()) {
                dbHandler.saveApiKey("chatgpt", chatGPTKey);
                // Find the API handler instances and update them
                for (Component comp : mainFrame.getContentPane().getComponents()) {
                    if (comp instanceof JSplitPane) {
                        JSplitPane pane = (JSplitPane)comp;
                        if (pane.getLeftComponent() instanceof JScrollPane && 
                            ((JScrollPane)pane.getLeftComponent()).getViewport().getView() instanceof ChatGPT_panel) {
                            ChatGPT_panel panel = (ChatGPT_panel)((JScrollPane)pane.getLeftComponent()).getViewport().getView();
                            // In a real implementation, you would extract the handler and update it
                        }
                    }
                }
            }
            
            if (!perplexityKey.isEmpty()) {
                dbHandler.saveApiKey("perplexity", perplexityKey);
                // Similar code to update the Perplexity handler
            }
            
            settingsDialog.dispose();
        });
        
        cancelButton.addActionListener(e -> settingsDialog.dispose());
        
        // Center and show the dialog
        settingsDialog.setLocationRelativeTo(mainFrame);
        settingsDialog.setVisible(true);
    }
    
    /**
     * Cleans up resources and shuts down the application
     */
    private void shutdownApplication() {
        // Shut down background tasks
        if (backgroundPanel != null) {
            backgroundPanel.shutdown();
        }
        
        // Close database connection
        if (dbHandler != null) {
            dbHandler.close();
        }
        
        // Exit the application
        System.exit(0);
    }
    
    /**
     * Displays the main application window
     */
    public void start() {
        mainFrame.setVisible(true);
    }
    
    /**
     * Application entry point
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Use the Event Dispatch Thread for Swing applications
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            App_runner app = new App_runner();
            app.start();
        });
    }
}
