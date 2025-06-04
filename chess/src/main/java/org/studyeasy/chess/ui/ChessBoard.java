package org.studyeasy.chess.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.studyeasy.chess.ai.OpenAIChessEngine;
import org.studyeasy.chess.model.Board;
import org.studyeasy.chess.model.Piece;
import org.studyeasy.chess.model.PieceColor;
import org.studyeasy.chess.model.PieceType;
import org.studyeasy.chess.model.Position;
import org.studyeasy.chess.ui.MainMenu.GameMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ChessBoard extends BorderPane {
    private static final int MIN_SQUARE_SIZE = 60;
    private int currentSquareSize = 80;
    private final Board board;
    private final GameMode gameMode;
    private Position selectedPosition;
    private OpenAIChessEngine aiEngine;
    private final List<String> moveHistory = new ArrayList<>();
    private final Rectangle[][] squares = new Rectangle[8][8];
    private PieceColor currentTurn = PieceColor.WHITE; // White moves first
    
    // Timer related fields
    private final int timeControlMinutes;
    private int whiteTimeSeconds;
    private int blackTimeSeconds;
    private Timeline whiteTimer;
    private Timeline blackTimer;
    private Label whiteTimerLabel;
    private Label blackTimerLabel;
    private boolean gameEnded = false;
    
    // Map to cache piece images
    private final Map<String, Image> pieceImages = new HashMap<>();
    
    // Lists to track captured pieces
    private final List<Piece> capturedByWhite = new ArrayList<>();
    private final List<Piece> capturedByBlack = new ArrayList<>();
    
    // UI elements for captured pieces
    private final HBox whiteCapturedPiecesBox = new HBox(5);
    private final HBox blackCapturedPiecesBox = new HBox(5);
    private final GridPane boardGrid = new GridPane();
    private final VBox centerBox = new VBox(10);
    
    // Constructor for friend mode (no API key needed)
    public ChessBoard(GameMode gameMode, int timeControlMinutes) {
        this.gameMode = gameMode;
        this.timeControlMinutes = timeControlMinutes;
        this.board = new Board();
        
        // Initialize timers
        this.whiteTimeSeconds = timeControlMinutes * 60;
        this.blackTimeSeconds = timeControlMinutes * 60;
        
        setPadding(new Insets(20));
        
        // Setup captured pieces areas
        setupCapturedPiecesAreas();
        
        // Setup board grid
        boardGrid.setPadding(new Insets(0));
        boardGrid.setHgap(0);
        boardGrid.setVgap(0);
        boardGrid.setAlignment(Pos.CENTER);
        
        // Load piece images
        loadPieceImages();

        drawBoard();
        setupPieces();
        
        // Add board to center box
        centerBox.getChildren().add(boardGrid);
        centerBox.setAlignment(Pos.CENTER);
        
        // Setup layout based on time control
        if (timeControlMinutes > 0) {
            // With time control
            setupTimerDisplay();
            initializeTimers();
        } else {
            // No time control, just add the captured pieces areas
            setTop(blackCapturedPiecesBox);
            setCenter(centerBox);
            setBottom(whiteCapturedPiecesBox);
        }
        
        // Add listener to resize the board when the window size changes
        widthProperty().addListener((obs, oldVal, newVal) -> resizeBoard());
        heightProperty().addListener((obs, oldVal, newVal) -> resizeBoard());
        
        // Start white's timer if time control is enabled
        if (timeControlMinutes > 0) {
            startTimer(PieceColor.WHITE);
        }
    }
    
    // Constructor for AI mode
    public ChessBoard(GameMode gameMode, String apiKey, int timeControlMinutes) {
        this(gameMode, timeControlMinutes);
        if (gameMode == GameMode.AI && apiKey != null && !apiKey.trim().isEmpty()) {
            this.aiEngine = new OpenAIChessEngine(apiKey);
        }
    }
    
    private void setupCapturedPiecesAreas() {
        // Setup area for pieces captured by white (black pieces)
        whiteCapturedPiecesBox.setPadding(new Insets(10));
        whiteCapturedPiecesBox.setAlignment(Pos.CENTER);
        whiteCapturedPiecesBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");
        whiteCapturedPiecesBox.setMinHeight(50);
        whiteCapturedPiecesBox.setPrefHeight(80);
        whiteCapturedPiecesBox.setFillHeight(true);
        HBox.setHgrow(whiteCapturedPiecesBox, Priority.ALWAYS);
        
        // Setup area for pieces captured by black (white pieces)
        blackCapturedPiecesBox.setPadding(new Insets(10));
        blackCapturedPiecesBox.setAlignment(Pos.CENTER);
        blackCapturedPiecesBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");
        blackCapturedPiecesBox.setMinHeight(50);
        blackCapturedPiecesBox.setPrefHeight(80);
        blackCapturedPiecesBox.setFillHeight(true);
        HBox.setHgrow(blackCapturedPiecesBox, Priority.ALWAYS);
    }
    
    private void setupTimerDisplay() {
        // Create timer labels
        whiteTimerLabel = new Label(formatTime(whiteTimeSeconds));
        blackTimerLabel = new Label(formatTime(blackTimeSeconds));
        
        // Style the timer labels
        String timerStyle = "-fx-background-color: #e0e0e0; -fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-radius: 5px;";
        whiteTimerLabel.setStyle(timerStyle);
        blackTimerLabel.setStyle(timerStyle);
        whiteTimerLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        blackTimerLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        whiteTimerLabel.setTextFill(Color.BLACK);
        blackTimerLabel.setTextFill(Color.BLACK);
        whiteTimerLabel.setAlignment(Pos.CENTER);
        blackTimerLabel.setAlignment(Pos.CENTER);
        whiteTimerLabel.setMaxWidth(Double.MAX_VALUE);
        blackTimerLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Create timer boxes
        HBox whiteTimerBox = new HBox(10);
        whiteTimerBox.setAlignment(Pos.CENTER);
        whiteTimerBox.getChildren().add(whiteTimerLabel);
        
        HBox blackTimerBox = new HBox(10);
        blackTimerBox.setAlignment(Pos.CENTER);
        blackTimerBox.getChildren().add(blackTimerLabel);
        
        // Create containers for timers and captured pieces
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(blackTimerBox, blackCapturedPiecesBox);
        
        VBox bottomBox = new VBox(10);
        bottomBox.getChildren().addAll(whiteCapturedPiecesBox, whiteTimerBox);
        
        // Set the layout
        setTop(topBox);
        setCenter(centerBox);
        setBottom(bottomBox);
    }
    
    private void initializeTimers() {
        // Create white player's timer
        whiteTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                if (whiteTimeSeconds > 0) {
                    whiteTimeSeconds--;
                    whiteTimerLabel.setText(formatTime(whiteTimeSeconds));
                    
                    // Change color to red when time is running low (less than 30 seconds)
                    if (whiteTimeSeconds < 30) {
                        whiteTimerLabel.setTextFill(Color.RED);
                    }
                    
                    if (whiteTimeSeconds == 0) {
                        handleTimeOut(PieceColor.WHITE);
                    }
                }
            })
        );
        whiteTimer.setCycleCount(Timeline.INDEFINITE);
        
        // Create black player's timer
        blackTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                if (blackTimeSeconds > 0) {
                    blackTimeSeconds--;
                    blackTimerLabel.setText(formatTime(blackTimeSeconds));
                    
                    // Change color to red when time is running low (less than 30 seconds)
                    if (blackTimeSeconds < 30) {
                        blackTimerLabel.setTextFill(Color.RED);
                    }
                    
                    if (blackTimeSeconds == 0) {
                        handleTimeOut(PieceColor.BLACK);
                    }
                }
            })
        );
        blackTimer.setCycleCount(Timeline.INDEFINITE);
    }
    
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private void startTimer(PieceColor color) {
        if (timeControlMinutes <= 0) return; // No time control
        
        if (color == PieceColor.WHITE) {
            blackTimer.stop();
            whiteTimer.play();
            whiteTimerLabel.setStyle(whiteTimerLabel.getStyle() + "-fx-border-width: 3px; -fx-border-color: blue;");
            blackTimerLabel.setStyle(blackTimerLabel.getStyle().replace("-fx-border-width: 3px; -fx-border-color: blue;", ""));
        } else {
            whiteTimer.stop();
            blackTimer.play();
            blackTimerLabel.setStyle(blackTimerLabel.getStyle() + "-fx-border-width: 3px; -fx-border-color: blue;");
            whiteTimerLabel.setStyle(whiteTimerLabel.getStyle().replace("-fx-border-width: 3px; -fx-border-color: blue;", ""));
        }
    }
    
    private void handleTimeOut(PieceColor color) {
        if (gameEnded) return;
        
        gameEnded = true;
        
        // Stop both timers
        if (whiteTimer != null) whiteTimer.stop();
        if (blackTimer != null) blackTimer.stop();
        
        // Determine the winner
        String winner = (color == PieceColor.WHITE) ? "Black" : "White";
        
        // Show game over dialog
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText("Time's up!");
            alert.setContentText(winner + " wins by timeout!");
            
            alert.showAndWait();
            
            // Game is now ended, further moves are disabled
            // To return to main menu, the application would need to be restarted
            // or we would need a reference to the primary stage
        });
    }
    
    private void loadPieceImages() {
        // Load images for each piece type and color
        for (PieceColor color : PieceColor.values()) {
            for (PieceType type : PieceType.values()) {
                String colorName = color.toString().toLowerCase();
                String typeName = type.toString().toLowerCase();
                String imagePath = "/images/chess/" + colorName + "_" + typeName + ".png";
                
                try {
                    // Check if resource exists before loading
                    if (getClass().getResource(imagePath) != null) {
                        Image image = new Image(getClass().getResourceAsStream(imagePath));
                        pieceImages.put(colorName + "_" + typeName, image);
                    } else {
                        System.err.println("Image resource not found: " + imagePath);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load image: " + imagePath);
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void drawBoard() {
        boardGrid.getChildren().clear();
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Rectangle square = new Rectangle(currentSquareSize, currentSquareSize);
                square.setFill((row + col) % 2 == 0 ? Color.BEIGE : Color.BROWN);
                
                final int r = row;
                final int c = col;
                square.setOnMouseClicked(e -> handleSquareClick(r, c));
                
                boardGrid.add(square, col, row);
                squares[row][col] = square;
            }
        }
    }
    
    private void resizeBoard() {
        // Calculate the new square size based on the available space
        double availableWidth = getWidth() - getPadding().getLeft() - getPadding().getRight();
        double availableHeight = getHeight() - getPadding().getTop() - getPadding().getBottom() 
                               - whiteCapturedPiecesBox.getHeight() - blackCapturedPiecesBox.getHeight()
                               - whiteTimerLabel.getHeight() - blackTimerLabel.getHeight()
                               - centerBox.getSpacing() * 2;
        
        int newSquareSize = (int) Math.min(availableWidth / 8, availableHeight / 8);
        newSquareSize = Math.max(newSquareSize, MIN_SQUARE_SIZE); // Ensure minimum size
        
        if (newSquareSize != currentSquareSize) {
            currentSquareSize = newSquareSize;
            
            // Resize all squares
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    squares[row][col].setWidth(currentSquareSize);
                    squares[row][col].setHeight(currentSquareSize);
                }
            }
            
            // Refresh the board to resize all pieces
            refreshBoardUI();
        }
    }
    
    private void setupPieces() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece != null) {
                    addPieceToUI(piece, row, col);
                }
            }
        }
    }
    
    private void addPieceToUI(Piece piece, int row, int col) {
        String colorName = piece.getColor().toString().toLowerCase();
        String typeName = piece.getType().toString().toLowerCase();
        String imageKey = colorName + "_" + typeName;
        
        Image pieceImage = pieceImages.get(imageKey);
        
        if (pieceImage != null) {
            ImageView imageView = new ImageView(pieceImage);
            imageView.setFitWidth(currentSquareSize - 10);
            imageView.setFitHeight(currentSquareSize - 10);
            imageView.setMouseTransparent(true);
            
            boardGrid.add(imageView, col, row);
        } else {
            // Fallback to text representation if image not found
            Label pieceLabel = new Label(
                    typeName.substring(0, 1).toUpperCase()
            );
            
            pieceLabel.setStyle("-fx-font-size: " + (currentSquareSize / 2) + "px; -fx-font-weight: bold;");
            pieceLabel.setTextFill(colorName.equals("white") ? Color.WHITE : Color.BLACK);
            pieceLabel.setAlignment(Pos.CENTER);
            pieceLabel.setPrefSize(currentSquareSize, currentSquareSize);
            pieceLabel.setMouseTransparent(true);
            
            boardGrid.add(pieceLabel, col, row);
        }
    }
    
    private void addCapturedPieceToUI(Piece piece) {
        String colorName = piece.getColor().toString().toLowerCase();
        String typeName = piece.getType().toString().toLowerCase();
        String imageKey = colorName + "_" + typeName;
        
        Image pieceImage = pieceImages.get(imageKey);
        
        if (pieceImage != null) {
            ImageView imageView = new ImageView(pieceImage);
            imageView.setFitWidth(currentSquareSize / 2);
            imageView.setFitHeight(currentSquareSize / 2);
            
            // Add to the appropriate captured pieces area
            if (piece.getColor() == PieceColor.WHITE) {
                blackCapturedPiecesBox.getChildren().add(imageView);
            } else {
                whiteCapturedPiecesBox.getChildren().add(imageView);
            }
        }
    }
    
    private void handleSquareClick(int row, int col) {
        if (gameEnded) return;
        
        System.out.println("Clicked: " + row + "," + col);
        Position clickedPosition = new Position(row, col);
        
        if (selectedPosition == null) {
            // First click - select a piece
            Piece piece = board.getPiece(clickedPosition);
            
            // Only allow selecting pieces of the current turn's color
            if (piece != null && piece.getColor() == currentTurn) {
                selectedPosition = clickedPosition;
                squares[row][col].setStroke(Color.BLUE);
                squares[row][col].setStrokeWidth(3);
                System.out.println("Selected: " + piece.getType());
            }
        } else {
            // Second click - attempt to move the piece
            int fromRow = selectedPosition.row();
            int fromCol = selectedPosition.col();
            Piece piece = board.getPiece(selectedPosition);
            
            // Clear the selection highlight
            squares[fromRow][fromCol].setStroke(null);
            
            if (piece != null) {
                // Check if the move is valid according to chess rules
                if (board.isValidMove(selectedPosition, clickedPosition, currentTurn)) {
                    // Check if there's a piece to capture
                    Piece capturedPiece = board.getPiece(clickedPosition);
                    if (capturedPiece != null) {
                        // Add to captured pieces list
                        if (currentTurn == PieceColor.WHITE) {
                            capturedByWhite.add(capturedPiece);
                        } else {
                            capturedByBlack.add(capturedPiece);
                        }
                        // Update the UI for captured pieces
                        addCapturedPieceToUI(capturedPiece);
                    }
                    
                    // Record the move
                    String moveNotation = selectedPosition.toChessNotation() + clickedPosition.toChessNotation();
                    moveHistory.add(moveNotation);
                    
                    // Move the piece on the board model
                    board.movePiece(selectedPosition, clickedPosition);
                    
                    // Update the UI
                    refreshBoardUI();
                    
                    System.out.println("Moved from " + fromRow + "," + fromCol + " to " + row + "," + col);
                    
                    // Switch turns
                    currentTurn = (currentTurn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
                    
                    // Switch timers if time control is enabled
                    if (timeControlMinutes > 0) {
                        startTimer(currentTurn);
                    }
                    
                    // If playing against AI and it's AI's turn (BLACK), make AI move
                    if (gameMode == GameMode.AI && aiEngine != null && currentTurn == PieceColor.BLACK) {
                        makeAIMove();
                    }
                }
            }
            
            // Reset selection
            selectedPosition = null;
        }
    }
    
    private void refreshBoardUI() {
        // Clear all piece images and labels from the board
        boardGrid.getChildren().removeIf(node -> node instanceof ImageView || node instanceof Label && 
                                        !(node == whiteTimerLabel || node == blackTimerLabel));
        
        // Keep the squares
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (!boardGrid.getChildren().contains(squares[row][col])) {
                    boardGrid.add(squares[row][col], col, row);
                }
            }
        }
        
        // Redraw all pieces
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece != null) {
                    addPieceToUI(piece, row, col);
                }
            }
        }
    }
    
    private void makeAIMove() {
        if (aiEngine != null) {
            System.out.println("AI is thinking...");
            try {
                Position[] move = aiEngine.getNextMove(board, moveHistory);
                if (move != null && move.length == 2) {
                    System.out.println("AI suggested move: " + move[0].toChessNotation() + " to " + move[1].toChessNotation());
                    
                    // Check if the move is valid
                    if (board.isValidMove(move[0], move[1], currentTurn)) {
                        System.out.println("AI move is valid, executing...");
                        
                        // Check if there's a piece to capture
                        Piece capturedPiece = board.getPiece(move[1]);
                        if (capturedPiece != null) {
                            // Add to captured pieces list
                            capturedByBlack.add(capturedPiece);
                            // Update the UI for captured pieces
                            addCapturedPieceToUI(capturedPiece);
                        }
                        
                        // Record the move
                        String moveNotation = move[0].toChessNotation() + move[1].toChessNotation();
                        moveHistory.add(moveNotation);
                        
                        // Move the piece on the board model
                        board.movePiece(move[0], move[1]);
                        
                        // Update the UI
                        refreshBoardUI();
                        
                        System.out.println("AI moved from " + move[0].toChessNotation() + 
                                          " to " + move[1].toChessNotation());
                        
                        // Switch turns back to player
                        currentTurn = PieceColor.WHITE;
                        
                        // Switch active timer
                        if (timeControlMinutes > 0) {
                            startTimer(currentTurn);
                        }
                    } else {
                        System.err.println("AI suggested an invalid move: " + 
                                          move[0].toChessNotation() + " to " + move[1].toChessNotation());
                        // Try a default move as fallback
                        tryDefaultMove();
                    }
                } else {
                    System.err.println("AI returned null or incomplete move");
                    tryDefaultMove();
                }
            } catch (Exception e) {
                System.err.println("Error during AI move: " + e.getMessage());
                e.printStackTrace();
                tryDefaultMove();
            }
        } else {
            System.err.println("AI engine is null");
        }
    }

    private void tryDefaultMove() {
        System.out.println("Trying a default move for AI");
        // Try to find any valid move for a black piece
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Position from = new Position(fromRow, fromCol);
                Piece piece = board.getPiece(from);
                
                if (piece != null && piece.getColor() == PieceColor.BLACK) {
                    // Try all possible destinations
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            Position to = new Position(toRow, toCol);
                            
                            if (board.isValidMove(from, to, PieceColor.BLACK)) {
                                // Check if there's a piece to capture
                                Piece capturedPiece = board.getPiece(to);
                                if (capturedPiece != null) {
                                    // Add to captured pieces list
                                    capturedByBlack.add(capturedPiece);
                                    // Update the UI for captured pieces
                                    addCapturedPieceToUI(capturedPiece);
                                }
                                
                                // Found a valid move, execute it
                                String moveNotation = from.toChessNotation() + to.toChessNotation();
                                moveHistory.add(moveNotation);
                                
                                board.movePiece(from, to);
                                refreshBoardUI();
                                
                                System.out.println("Fallback AI moved from " + from.toChessNotation() + 
                                                  " to " + to.toChessNotation());
                                
                                currentTurn = PieceColor.WHITE;
                                
                                // Switch active timer
                                if (timeControlMinutes > 0) {
                                    startTimer(currentTurn);
                                }
                                
                                return;
                            }
                        }
                    }
                }
            }
        }
        
        System.err.println("Could not find any valid move for AI");
    }
}
