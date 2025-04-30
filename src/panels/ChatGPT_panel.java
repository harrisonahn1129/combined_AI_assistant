package panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import api_calls.ChatGPT_api_handler;

/**
 * Panel for displaying ChatGPT responses
 * Displays conversation history with the ChatGPT model
 */
public class ChatGPT_panel extends JPanel {
    private ChatGPT_api_handler apiHandler;
    private JTextArea responseArea;
    private JScrollPane scrollPane;
    private JButton clearButton;
    
    /**
     * Constructor initializes the panel with the API handler
     * @param apiHandler the ChatGPT API handler
     */
    public ChatGPT_panel(ChatGPT_api_handler apiHandler) {
        this.apiHandler = apiHandler;
        initializeUI();
    }
    
    /**
     * Initializes the UI components of the panel
     */
    private void initializeUI() {
        // Set layout manager
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("ChatGPT Responses"));
        
        // Initialize the response text area
        responseArea = new JTextArea();
        responseArea.setEditable(false);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Create scroll pane for the response area
        scrollPane = new JScrollPane(responseArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Create a button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearConversation();
            }
        });
        buttonPanel.add(clearButton);
        
        // Add components to the panel
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Displays a response from ChatGPT
     * @param query The user's query
     * @param response The ChatGPT response
     */
    public void displayResponse(String query, String response) {
        // Format and append the query and response
        String formattedText = "\n-------------------\n";
        formattedText += "Query: " + query + "\n\n";
        formattedText += response + "\n\n";
        
        // Append to the text area
        responseArea.append(formattedText);
        
        // Scroll to the bottom to show the latest response
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    /**
     * Clears all conversation history from the panel
     */
    public void clearConversation() {
        responseArea.setText("");
    }
    
    /**
     * Shows a loading indicator while waiting for an API response
     * @param isLoading true to show loading, false to hide
     */
    public void setLoading(boolean isLoading) {
        if (isLoading) {
            responseArea.append("\nFetching response from ChatGPT...\n");
            // Scroll to make the loading message visible
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }
    }
    
    public ChatGPT_api_handler getApiHandler() {
        return this.apiHandler;
    }
}
