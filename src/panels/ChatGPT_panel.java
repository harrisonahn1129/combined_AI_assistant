package panels;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import api_calls.ChatGPT_api_handler;

/**
 * Panel for displaying ChatGPT responses and conversation history of ChatGPT
 */
public class ChatGPT_panel extends JPanel {
    private ChatGPT_api_handler apiHandler;
    private JTextPane responseArea;
    private JScrollPane scrollPane;
    private JButton clearButton;
    private int loadingMessagePosition = -1; 
    private int loadingMessageLength = 0;   
    
    /**
     * Constructor initializes the panel with the API handler
     */
    public ChatGPT_panel(ChatGPT_api_handler apiHandler) {
        this.apiHandler = apiHandler;
        initializeUI();
    }
    
    public ChatGPT_api_handler getApiHandler() {
        return this.apiHandler;
    }
    
    /**
     * Initializes the UI components of the panel
     */
    private void initializeUI() {
        // Set layout manager
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("ChatGPT Responses"));
        
        // Initialize the response text area with JTextPane for better formatting
        responseArea = new JTextPane();
        responseArea.setEditable(false);
        
        // Set word wrap for JTextPane
        StyledDocument doc = responseArea.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_LEFT);
        doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
        
        // Set content type and font
        responseArea.setContentType("text/plain");
        responseArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        responseArea.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, Boolean.FALSE);
        // Enable word wrap using the StyledEditorKit
        ((StyledEditorKit)responseArea.getEditorKit()).getViewFactory().create(doc.getDefaultRootElement());
        responseArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Remove initial welcome message
        responseArea.setText("");
        
        // Create scroll pane for the response area with improved settings
        scrollPane = new JScrollPane(responseArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Set panel width based on parent's width
        responseArea.setPreferredSize(new Dimension(0, 0));
        
        // Add resize listener to ensure text wraps correctly when panel is resized
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                // Force text wrapping when panel is resized
                SwingUtilities.invokeLater(() -> {
                    scrollPane.setPreferredSize(new Dimension(getWidth() - 20, getHeight() - 60));
                    responseArea.revalidate();
                });
            }
        });
        
        // Add mouse wheel listener to improve scrolling
        responseArea.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                int scrollAmount = e.getUnitsToScroll() * verticalBar.getUnitIncrement();
                int newValue = verticalBar.getValue() + scrollAmount;
                
                // Ensure the value stays within bounds
                newValue = Math.max(newValue, verticalBar.getMinimum());
                newValue = Math.min(newValue, verticalBar.getMaximum() - verticalBar.getVisibleAmount());
                
                verticalBar.setValue(newValue);
                e.consume(); // Prevent further handling
            }
        });
        
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
     */
    public void displayResponse(String query, String response) {
        // Format and append the query and response
        final String formattedText = "--------------------------------------------------\n" +
                                     "Query: " + query + "\n\n" +
                                     response + "\n\n";
        
        // Append to the text pane using document to avoid thread issues
        SwingUtilities.invokeLater(() -> {
            try {
                Document doc = responseArea.getDocument();
                
                // Remove loading message if it exists
                if (loadingMessagePosition >= 0) {
                    doc.remove(loadingMessagePosition, loadingMessageLength);
                    // Insert response at the loading message position
                    doc.insertString(loadingMessagePosition, formattedText, null);
                    // Reset loading message position
                    loadingMessagePosition = -1;
                    loadingMessageLength = 0;
                } else {
                    // Add to the end of document if no loading message exists
                    doc.insertString(doc.getLength(), formattedText, null);
                }
                
                // Ensure scrolling to bottom
                scrollToBottom();
                
                // Force text wrapping
                responseArea.revalidate();
            } catch (Exception e) {
                System.err.println("Error displaying response: " + e.getMessage());
            }
        });
    }
    
    /**
     * Scrolls the view to the bottom of the text area
     */
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            // Multiple methods to ensure scrolling works
            responseArea.setCaretPosition(responseArea.getDocument().getLength());
            
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
            
            // Request focus to ensure scrolling works properly
            scrollPane.getViewport().scrollRectToVisible(
                new Rectangle(0, responseArea.getHeight(), 1, 1)
            );
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
     */
    public void setLoading(boolean isLoading) {
        if (isLoading) {
            final String loadingMessage = "\nFetching response from ChatGPT...\n";
            
            SwingUtilities.invokeLater(() -> {
                try {
                    Document doc = responseArea.getDocument();
                    // Store loading message position and length
                    loadingMessagePosition = doc.getLength();
                    loadingMessageLength = loadingMessage.length();
                    
                    doc.insertString(
                        loadingMessagePosition,
                        loadingMessage,
                        null
                    );
                    // Scroll to make loading message visible
                    scrollToBottom();
                } catch (Exception e) {
                    System.err.println("Error setting loading state: " + e.getMessage());
                }
            });
        }
    }
}
