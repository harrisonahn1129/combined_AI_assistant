package app_runner;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
        JMenuItem historyItem = new JMenuItem("View History");
        JMenuItem clearItem = new JMenuItem("Clear All");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        fileMenu.add(historyItem);
        fileMenu.add(exportItem);
        fileMenu.add(clearItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // API Settings menu item (now directly in the menu bar)
        JMenuItem apiSettingsItem = new JMenuItem("API Settings");
        
        // Add action listeners
        exitItem.addActionListener(e -> shutdownApplication());
        clearItem.addActionListener(e -> {
            chatGPTPanel.clearConversation();
            perplexityPanel.clearConversation();
        });
        
        apiSettingsItem.addActionListener(e -> showApiSettingsDialog());
        
        // Add history viewer action listener
        historyItem.addActionListener(e -> showHistoryDialog());
        
        // Add export action listener
        exportItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Conversation");
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
            
            int userSelection = fileChooser.showSaveDialog(mainFrame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".json")) {
                    filePath += ".json";
                }
                
                boolean success = dbHandler.exportConversations(filePath);
                if (success) {
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Conversations exported successfully.", 
                        "Export Complete", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Failed to export conversations.", 
                        "Export Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Add menus and items to menu bar
        menuBar.add(fileMenu);
        menuBar.add(apiSettingsItem); // Add API Settings directly to menu bar
        
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
            
            // Update API handlers directly without saving to database
            if (!chatGPTKey.isEmpty()) {
                // Find the API handler instance and update it
                for (Component comp : mainFrame.getContentPane().getComponents()) {
                    if (comp instanceof JSplitPane) {
                        JSplitPane pane = (JSplitPane)comp;
                        if (pane.getLeftComponent() instanceof JScrollPane && 
                            ((JScrollPane)pane.getLeftComponent()).getViewport().getView() instanceof ChatGPT_panel) {
                            ChatGPT_panel panel = (ChatGPT_panel)((JScrollPane)pane.getLeftComponent()).getViewport().getView();
                            // Update the API handler
                            panel.getApiHandler().setApiKey(chatGPTKey);
                        }
                    }
                }
            }
            
            if (!perplexityKey.isEmpty()) {
                // Find the API handler instance and update it
                for (Component comp : mainFrame.getContentPane().getComponents()) {
                    if (comp instanceof JSplitPane) {
                        JSplitPane pane = (JSplitPane)comp;
                        if (pane.getRightComponent() instanceof JScrollPane && 
                            ((JScrollPane)pane.getRightComponent()).getViewport().getView() instanceof Perplexity_panel) {
                            Perplexity_panel panel = (Perplexity_panel)((JScrollPane)pane.getRightComponent()).getViewport().getView();
                            // Update the API handler
                            panel.getApiHandler().setApiKey(perplexityKey);
                        }
                    }
                }
            }
            
            // Show confirmation message
            if (!chatGPTKey.isEmpty() || !perplexityKey.isEmpty()) {
                JOptionPane.showMessageDialog(settingsDialog, 
                    "API key(s) have been set successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            settingsDialog.dispose();
        });
        
        cancelButton.addActionListener(e -> settingsDialog.dispose());
        
        // Center and show the dialog
        settingsDialog.setLocationRelativeTo(mainFrame);
        settingsDialog.setVisible(true);
    }
    
    /**
     * Shows the conversation history dialog
     */
    private void showHistoryDialog() {
        JDialog historyDialog = new JDialog(mainFrame, "Conversation History", true);
        historyDialog.setSize(800, 500);
        historyDialog.setLayout(new BorderLayout(10, 10));
        
        // Create table with history data
        String[] columnNames = {"Date", "Query", "View Details"};
        
        List<Map<String, Object>> conversations = dbHandler.getConversationHistory(50); // Get up to 50 conversations
        Object[][] data = new Object[conversations.size()][3];
        
        for (int i = 0; i < conversations.size(); i++) {
            Map<String, Object> conv = conversations.get(i);
            long timestamp = (long) conv.get("timestamp");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(new Date(timestamp));
            
            data[i][0] = formattedDate;
            data[i][1] = conv.get("userQuery");
            data[i][2] = "View";
        }
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only make the "View Details" column clickable
            }
        };
        
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(450);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        // Add button to view conversation - passing historyDialog as parent
        table.getColumn("View Details").setCellRenderer(new ButtonRenderer());
        table.getColumn("View Details").setCellEditor(new ButtonEditor(new JCheckBox(), conversations, this, historyDialog));
        
        JScrollPane scrollPane = new JScrollPane(table);
        historyDialog.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> historyDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Center and show dialog
        historyDialog.setLocationRelativeTo(mainFrame);
        historyDialog.setVisible(true);
    }
    
    /**
     * Shows conversation details in a new dialog
     */
    public void showConversationDetails(Map<String, Object> conversation, JDialog parentDialog) {
        // Use parentDialog (historyDialog) as the parent instead of mainFrame
        JDialog detailsDialog = new JDialog(parentDialog, "Conversation Details", true);
        detailsDialog.setSize(900, 600);
        detailsDialog.setLayout(new BorderLayout(10, 10));
        
        String query = (String) conversation.get("userQuery");
        String chatGPTResponse = (String) conversation.get("chatGPTResponse");
        String perplexityResponse = (String) conversation.get("perplexityResponse");
        long timestamp = (long) conversation.get("timestamp");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(new Date(timestamp));
        
        // Query panel
        JPanel queryPanel = new JPanel(new BorderLayout(5, 5));
        queryPanel.setBorder(BorderFactory.createTitledBorder("Query"));
        
        JTextArea queryArea = new JTextArea(query);
        queryArea.setEditable(false);
        queryArea.setLineWrap(true);
        queryArea.setWrapStyleWord(true);
        queryPanel.add(new JScrollPane(queryArea), BorderLayout.CENTER);
        
        // Date label
        JLabel dateLabel = new JLabel("Date: " + formattedDate);
        queryPanel.add(dateLabel, BorderLayout.NORTH);
        
        // Response panels in a split pane
        JPanel chatGPTResponsePanel = new JPanel(new BorderLayout(5, 5));
        chatGPTResponsePanel.setBorder(BorderFactory.createTitledBorder("ChatGPT Response"));
        
        JTextArea chatGPTArea = new JTextArea(chatGPTResponse);
        chatGPTArea.setEditable(false);
        chatGPTArea.setLineWrap(true);
        chatGPTArea.setWrapStyleWord(true);
        chatGPTResponsePanel.add(new JScrollPane(chatGPTArea), BorderLayout.CENTER);
        
        JPanel perplexityResponsePanel = new JPanel(new BorderLayout(5, 5));
        perplexityResponsePanel.setBorder(BorderFactory.createTitledBorder("Perplexity Response"));
        
        JTextArea perplexityArea = new JTextArea(perplexityResponse);
        perplexityArea.setEditable(false);
        perplexityArea.setLineWrap(true);
        perplexityArea.setWrapStyleWord(true);
        perplexityResponsePanel.add(new JScrollPane(perplexityArea), BorderLayout.CENTER);
        
        JSplitPane responseSplitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            chatGPTResponsePanel,
            perplexityResponsePanel
        );
        responseSplitPane.setResizeWeight(0.5);
        
        // Load current conversation button
        JButton loadButton = new JButton("Load to Current Session");
        loadButton.addActionListener(e -> {
            // Display the conversation in the main panels using the correct instance variables
            this.chatGPTPanel.displayResponse(query, chatGPTResponse);
            this.perplexityPanel.displayResponse(query, perplexityResponse);
            detailsDialog.dispose();
        });
        
        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> detailsDialog.dispose());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(loadButton);
        buttonPanel.add(closeButton);
        
        // Add components to dialog
        detailsDialog.add(queryPanel, BorderLayout.NORTH);
        detailsDialog.add(responseSplitPane, BorderLayout.CENTER);
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Center and show dialog relative to parent dialog
        detailsDialog.setLocationRelativeTo(parentDialog);
        detailsDialog.setVisible(true);
    }
    
    /**
     * Cleans up resources and shuts down the application
     */
    private void shutdownApplication() {
        System.out.println("Shutting down application...");
        
        // Shut down API handlers first
        try {
            // Get references to the API handlers and shut them down
            if (chatGPTPanel != null) {
                // This assumes you have a method to get the API handler from the panel
                // If not, consider adding such a method or restructuring
                chatGPTPanel.getApiHandler().shutdown();
            }
            
            if (perplexityPanel != null) {
                perplexityPanel.getApiHandler().shutdown();
            }
        } catch (Exception e) {
            System.err.println("Error shutting down API handlers: " + e.getMessage());
        }
        
        // Shut down background tasks
        if (backgroundPanel != null) {
            backgroundPanel.shutdown();
        }
        
        // Close database connection
        if (dbHandler != null) {
            dbHandler.close();
        }
        
        // Force exit after a short delay to ensure all resources are released
        new Thread(() -> {
            try {
                Thread.sleep(500); // Wait for 0.5 seconds
                System.out.println("Forcing exit...");
                System.exit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // Terminate immediately
                System.exit(1);
            }
        }).start();
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

/**
 * Button renderer for the history table
 */
class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

/**
 * Button editor for the history table
 */
class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean isPushed;
    private List<Map<String, Object>> conversations;
    private App_runner appRunner;
    private JDialog parentDialog; // Store reference to parent dialog
    
    public ButtonEditor(JCheckBox checkBox, List<Map<String, Object>> conversations, App_runner appRunner, JDialog parentDialog) {
        super(checkBox);
        this.conversations = conversations;
        this.appRunner = appRunner;
        this.parentDialog = parentDialog; // Save the parent dialog reference
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }
    
    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            int selectedRow = 0;
            JTable table = (JTable) button.getParent();
            selectedRow = table.getSelectedRow();
            
            if (selectedRow >= 0 && selectedRow < conversations.size()) {
                Map<String, Object> selectedConversation = conversations.get(selectedRow);
                // Pass parent dialog to showConversationDetails
                appRunner.showConversationDetails(selectedConversation, parentDialog);
            }
        }
        isPushed = false;
        return label;
    }
    
    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
}
