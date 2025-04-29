package panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import api_calls.ChatGPT_api_handler;
import api_calls.Perplexity_api_handler;
import database.Database_handler;

/**
 * Panel for handling user input and query submission
 * Manages the user interface for entering queries and submitting to both AI models
 */
public class Input_panel extends JPanel {
    private JTextArea inputArea;
    private JButton submitButton;
    private JProgressBar progressBar;
    private ChatGPT_api_handler chatGPTHandler;
    private Perplexity_api_handler perplexityHandler;
    private Database_handler dbHandler;
    private ChatGPT_panel chatGPTPanel;
    private Perplexity_panel perplexityPanel;
    
    /**
     * Constructor initializes the panel with API handlers and database
     * @param chatGPTHandler the ChatGPT API handler
     * @param perplexityHandler the Perplexity API handler
     * @param dbHandler the database handler
     */
    public Input_panel(ChatGPT_api_handler chatGPTHandler, 
                      Perplexity_api_handler perplexityHandler,
                      Database_handler dbHandler) {
        this.chatGPTHandler = chatGPTHandler;
        this.perplexityHandler = perplexityHandler;
        this.dbHandler = dbHandler;
        initializeUI();
    }
    
    /**
     * Sets the reference to the ChatGPT and Perplexity panels for updating UI
     * @param chatGPTPanel the ChatGPT response panel
     * @param perplexityPanel the Perplexity response panel
     */
    public void setPanels(ChatGPT_panel chatGPTPanel, Perplexity_panel perplexityPanel) {
        this.chatGPTPanel = chatGPTPanel;
        this.perplexityPanel = perplexityPanel;
    }
    
    /**
     * Initializes the UI components of the panel
     */
    private void initializeUI() {
        // Set layout manager
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Input Query"));
        
        // Initialize input components
        inputArea = new JTextArea(3, 50);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Create scroll pane for the input area
        JScrollPane scrollPane = new JScrollPane(inputArea);
        
        // Create the button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitButton = new JButton("Submit to Both AIs");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        
        // Add action listener to the submit button
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitQuery();
            }
        });
        
        // Set button to be default button (respond to Enter key)
        inputArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "submit");
        inputArea.getActionMap().put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitButton.doClick();
            }
        });
        
        // Add components to button panel
        buttonPanel.add(progressBar);
        buttonPanel.add(submitButton);
        
        // Add components to the panel
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Submits the user query to both AI services
     */
    private void submitQuery() {
        // Get the user's query
        String query = inputArea.getText().trim();
        
        // Check if query is empty
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a query.", 
                "Empty Query", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if API keys are set
        if (!chatGPTHandler.hasValidApiKey() || !perplexityHandler.hasValidApiKey()) {
            JOptionPane.showMessageDialog(this, 
                "API keys are not configured. Please set them in the Settings menu.", 
                "Missing API Keys", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Generate a unique ID for this conversation
        String conversationId = UUID.randomUUID().toString();
        
        // Set UI to loading state
        setLoading(true);
        
        // Show loading indicators in response panels
        if (chatGPTPanel != null) {
            chatGPTPanel.setLoading(true);
        }
        if (perplexityPanel != null) {
            perplexityPanel.setLoading(true);
        }
        
        // Make asynchronous API calls
        CompletableFuture<String> chatGPTFuture = chatGPTHandler.makeAsyncApiCall(query);
        CompletableFuture<String> perplexityFuture = perplexityHandler.makeAsyncApiCall(query);
        
        // Handle responses when both are complete
        CompletableFuture.allOf(chatGPTFuture, perplexityFuture).thenAccept(v -> {
            // Get results from completed futures
            String chatGPTResponse = chatGPTFuture.join();
            String perplexityResponse = perplexityFuture.join();
            
            // Update UI on the EDT
            SwingUtilities.invokeLater(() -> {
                // Update response panels
                if (chatGPTPanel != null) {
                    chatGPTPanel.displayResponse(query, chatGPTResponse);
                }
                if (perplexityPanel != null) {
                    perplexityPanel.displayResponse(query, perplexityResponse);
                }
                
                // Save to database
                dbHandler.saveConversation(conversationId, query, chatGPTResponse, perplexityResponse);
                
                // Reset UI loading state
                setLoading(false);
                
                // Clear input area for next query
                inputArea.setText("");
            });
        }).exceptionally(ex -> {
            // Handle any exceptions
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Error processing query: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                
                // Reset UI loading state
                setLoading(false);
            });
            return null;
        });
    }
    
    /**
     * Sets the loading state of the UI
     * @param isLoading true to show loading, false to hide
     */
    private void setLoading(boolean isLoading) {
        submitButton.setEnabled(!isLoading);
        progressBar.setVisible(isLoading);
        inputArea.setEditable(!isLoading);
    }
}
