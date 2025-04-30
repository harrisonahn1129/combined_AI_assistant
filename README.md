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
- **Export functionality**: Export conversation history to JSON files
- **API key management**: Configure API keys for both services through the settings menu

## Technical Implementation

### Architecture
The application follows a modular design with these main components:
- **App_runner**: Main application class that initializes the UI and coordinates components
- **API Handlers**: Classes for interacting with external AI services (ChatGPT and Perplexity)
- **Panels**: UI components for displaying responses and handling user input
- **Database handler**: Handles conversation storage and retrieval

### Recent Improvements
The application has received several improvements:
- **Enhanced UI responsiveness**: Loading messages are now replaced by actual responses when received
- **Better conversation flow**: Visual separation between queries with improved formatting
- **String-based JSON parsing**: Removed dependency on Gson library for simpler implementation
- **Improved text processing**: Added LaTeX/markdown cleanup for ChatGPT responses
- **System prompts**: Added system prompts to get cleaner responses from both AI services

### GUI
The application uses Java Swing to create a clean and functional user interface with:
- JSplitPane for dual-display of AI responses
- JTextPane for improved text display with proper wrapping
- Custom scroll behavior for better user experience
- Menu system for application functions
- Loading indicators that show progress while waiting for AI responses

### Multithreading
The application implements asynchronous processing through:
- CompletableFuture for non-blocking API calls
- ExecutorService for managing background tasks
- Synchronized UI updates using SwingUtilities.invokeLater()

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
- Cleaner conversation history display with better formatting
- Loading messages that get replaced by responses when received
- Preservation of conversation history between queries
- Improved visual separation between different queries and responses

## Setup and Usage

1. Clone this repository
2. Open the project in your Java IDE (Eclipse or similar)
3. Obtain API keys for both ChatGPT and Perplexity AI
4. Launch the application and enter your API keys in Settings → API Settings
5. Enter your query in the input field and click "Submit to Both AIs"
6. View the responses side by side in the main interface

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
```

## Future Enhancements

Potential improvements for future versions:
- Local database integration using SQLite for conversation history
- Additional AI model integrations
- Advanced query options and model parameters

## Contributors

- Byeong Heon Ahn (bha233)
- David Hong (sh8348)

## License

This project is created for educational purposes as part of a final project assignment.
