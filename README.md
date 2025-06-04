# Chess Game

A JavaFX-based chess application with both player vs. player and player vs. AI modes.

## Features

- Play chess against a friend on the same computer
- Play against an AI powered by OpenAI's GPT-4o
- Configurable time controls (1, 3, 5, 10, 15, 30, or 60 minutes per player)
- Visual timer display with color indicators for low time
- Captured pieces display
- Standard chess rules implementation

## Requirements

- Java 17 or higher
- Maven for building the project
- OpenAI API key (for AI opponent mode)

## Building and Running

1. Clone the repository
2. Build with Maven:
   ```
   mvn clean package
   ```
3. Run the application:
   ```
   mvn javafx:run
   ```

## How to Play

1. From the main menu, select either "Play with Friend" or "Play with AI"
2. If playing against the AI, enter your OpenAI API key
3. Select a time control (in minutes) from the dropdown
4. Click on a piece to select it, then click on a destination square to move
5. In AI mode, the computer will automatically make moves for the black pieces
6. The game ends when a player runs out of time or when checkmate occurs

## Project Structure

- `org.studyeasy.chess` - Main application package
- `org.studyeasy.chess.model` - Chess game logic and data models
- `org.studyeasy.chess.ui` - User interface components
- `org.studyeasy.chess.ai` - AI opponent implementation using OpenAI

