package org.studyeasy.chess.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu extends VBox {
    private final Stage primaryStage;
    private PasswordField apiKeyField;
    private ComboBox<Integer> timeControlComboBox;
    
    public MainMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setupUI();
    }
    
    private void setupUI() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        
        Button playWithFriendButton = new Button("Play with Friend");
        Button playWithAIButton = new Button("Play with AI");
        
        // API Key input
        HBox apiKeyBox = new HBox(10);
        apiKeyBox.setAlignment(Pos.CENTER);
        Label apiKeyLabel = new Label("OpenAI API Key:");
        apiKeyField = new PasswordField();
        apiKeyField.setPrefWidth(300);
        apiKeyBox.getChildren().addAll(apiKeyLabel, apiKeyField);
        
        // Time control selection
        HBox timeControlBox = new HBox(10);
        timeControlBox.setAlignment(Pos.CENTER);
        Label timeControlLabel = new Label("Time Control (minutes):");
        timeControlComboBox = new ComboBox<>(FXCollections.observableArrayList(
            0, 1, 3, 5, 10, 15, 30, 60
        ));
        timeControlComboBox.setValue(1); // Default to 1 minute
        timeControlComboBox.setPromptText("Select time");
        timeControlBox.getChildren().addAll(timeControlLabel, timeControlComboBox);
        
        playWithFriendButton.setOnAction(e -> startGame(GameMode.FRIEND, null));
        playWithAIButton.setOnAction(e -> {
            String apiKey = apiKeyField.getText();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                showError("Please enter an OpenAI API key");
            } else {
                startGame(GameMode.AI, apiKey);
            }
        });
        
        getChildren().addAll(playWithFriendButton, playWithAIButton, apiKeyBox, timeControlBox);
    }
    
    private void showError(String message) {
        // Simple error display - you could enhance this with a proper dialog
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red;");
        
        // Remove any previous error message
        getChildren().removeIf(node -> node instanceof Label && ((Label) node).getTextFill().toString().contains("red"));
        
        // Add the new error message
        getChildren().add(errorLabel);
    }
    
    private void startGame(GameMode mode, String apiKey) {
        // Get the selected time control
        int timeControlMinutes = timeControlComboBox.getValue();
        
        // Create the chess board with the appropriate mode, API key, and time control
        ChessBoard chessBoard;
        if (mode == GameMode.AI && apiKey != null) {
            chessBoard = new ChessBoard(mode, apiKey, timeControlMinutes);
        } else {
            chessBoard = new ChessBoard(mode, timeControlMinutes);
        }
        
        Scene gameScene = new Scene(chessBoard, 800, 800);
        primaryStage.setScene(gameScene);
    }
    
    public enum GameMode {
        FRIEND, AI
    }
}