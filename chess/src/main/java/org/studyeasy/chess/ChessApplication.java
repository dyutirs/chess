package org.studyeasy.chess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.studyeasy.chess.ui.MainMenu;

public class ChessApplication extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        MainMenu mainMenu = new MainMenu(primaryStage);
        Scene scene = new Scene(mainMenu, 800, 600);
        
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(650);
        primaryStage.setMinHeight(700);
        primaryStage.show();
        
        // Set stage to maximize on start (optional)
        primaryStage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}