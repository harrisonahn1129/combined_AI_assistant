# Combined AI Assistant

This Java application allows users to interact with multiple AI language models simultaneously, specifically ChatGPT and Perplexity AI. The application provides a split-screen interface where responses from both AI models are displayed side-by-side for comparison.

## Project Overview

The Combined AI Assistant leverages the unique strengths of different AI models, providing users with a more comprehensive set of responses compared to a single AI service. This approach enables users to compare how different AI systems respond to the same query, helping users get more diverse perspectives and information.

## Features

- **Split-screen interface**: Left panel displays ChatGPT responses, right panel shows Perplexity responses
- **Concurrent API calls**: Background threading allows for simultaneous requests to both AI services
- **Simple input interface**: Single input field for sending queries to both models at once
- **Improved response display**: Loading messages are replaced with responses when received
- **Response formatting**: Automatic cleaning of special formatting (LaTeX, markdown) for easier reading
- **Conversation history**: SQLite database integration for storing and retrieving past conversations
- **History viewer**: UI for browsing, viewing, and loading previous conversations
- **Export functionality**: Export conversation history to JSON files
- **Direct API access**: Configure API keys directly from the main menu bar
- **Enhanced security**: API keys are stored only in memory for improved security

## Technical Implementation

### Architecture
The application follows a modular design with these main components:
- **App_runner**: Main application class that initializes the UI and coordinates components
- **API Handlers**: Classes for interacting with external AI services (ChatGPT and Perplexity)
- **Panels**: UI components for displaying responses and handling user input
- **Database handler**: Handles conversation storage and retrieval using SQLite

### Recent Improvements
The application has received several improvements:
- **Direct API configuration**: API Settings menu item now appears directly in the menu bar for one-click access
- **Enhanced API key security**: API keys are now stored only in memory and not persisted to the database
- **Improved conversation details dialog**: Conversation detail windows now appear on top of the history dialog
- **SQLite database integration**: Persistent storage of conversation history
- **Conversation history UI**: Added interface to browse, view, and reload past conversations
- **Enhanced UI responsiveness**: Loading messages are now replaced by actual responses when received
- **Better conversation flow**: Visual separation between queries with improved formatting
- **String-based JSON parsing**: Removed dependency on Gson library for simpler implementation
- **Improved text processing**: Added LaTeX/markdown cleanup for ChatGPT responses
- **System prompts**: Added system prompts to get cleaner responses from both AI services

### GUI
The application uses Java Swing to create a clean and functional user interface with:
- Streamlined menu bar with direct access to API Settings
- JSplitPane for dual-display of AI responses
- JTextPane for improved text display with proper wrapping
- JTable for conversation history browsing
- Modal dialog hierarchy for improved user experience
- Custom scroll behavior for better user experience
- Loading indicators that show progress while waiting for AI responses

### Multithreading
The application implements asynchronous processing through:
- CompletableFuture for non-blocking API calls
- ExecutorService for managing background tasks
- Synchronized UI updates using SwingUtilities.invokeLater()

### Database Storage
The application uses SQLite for data persistence:
- Lightweight, file-based database requiring no separate server
- Tables for conversations (API keys now stored only in memory)
- Simple JDBC-based interface
- Fallback to in-memory storage if database connection fails

### Security
The application implements security measures:
- API keys are stored only in memory during runtime
- API keys are never persisted to disk or database
- Password fields mask sensitive input
- API keys are applied directly to handlers without intermediary storage

### Networking
API communication is handled with Java's built-in networking capabilities:
- HttpURLConnection for REST API requests
- String-based JSON parsing for minimal dependencies
- Retry mechanisms with exponential backoff for failed requests
- Error handling for API limitations and network issues

### Response Parsing
The application includes custom JSON parsing:
- String-based JSON extraction to minimize external dependencies
- Response cleaning to remove LaTeX formulas, markdown formatting, and special characters
- Consistent text presentation across both AI services

## API Integration

### ChatGPT
- Uses OpenAI's chat completions API
- Custom system prompt to get cleaner responses
- Response formatting to improve readability

### Perplexity
- Uses Perplexity AI's chat completions API
- Custom system prompt to ensure consistent formatting
- Response cleaning to remove unwanted formatting

## User Experience Improvements
The application now features:
- One-click API configuration through the main menu bar
- Improved dialog hierarchy for conversation details viewing
- Conversation history browsing and retrieval
- Cleaner conversation history display with better formatting
- Loading messages that get replaced by responses when received
- Preservation of conversation history between queries
- Improved visual separation between different queries and responses

## Setup and Usage

1. Clone this repository
2. Open the project in your Java IDE (Eclipse or similar)
3. Ensure the SQLite JDBC driver is in the lib folder
4. Obtain API keys for both ChatGPT and Perplexity AI
5. Launch the application
6. Click "API Settings" in the menu bar to enter your API keys
7. Enter your query in the input field and click "Submit to Both AIs"
8. View the responses side by side in the main interface
9. Access past conversations through File → View History

## API Keys

This application requires API keys for both services:
- **ChatGPT**: Obtain from [OpenAI Platform](https://platform.openai.com/)
- **Perplexity**: Obtain from [Perplexity AI](https://www.perplexity.ai/)

## Project Structure

```
src/
├── app_runner/        # Main application classes
├── api_calls/         # API communication handlers
├── panels/            # UI components
└── database/          # Data storage components
lib/
└── sqlite-jdbc-*.jar  # SQLite JDBC driver
```

## Future Enhancements

Potential improvements for future versions:
- Additional AI model integrations
- Advanced query options and model parameters
- Enhanced search functionality for conversation history
- Export to additional formats (PDF, HTML)

## Contributors

- Byeong Heon Ahn (bha233)
- David Hong (sh8348)

## License

This project is created for educational purposes as part of a final project assignment.
