package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Card.loadCardImages();
        Game game = new Game();

        newScene(primaryStage, game);
    }

    public void newScene(Stage primaryStage, Game game) {
        game.setTableBackground(new Image("/table/green.png"));

        MenuItem menuItem1 = new MenuItem("New Game");
        MenuItem menuItem2 = new MenuItem("Restart");
        MenuItem menuItem3 = new MenuItem("Undo");
        MenuItem menuItem4 = new MenuItem("Quit");

        menuItem1.setOnAction((event) -> {
            Game newGame = game.newGame();
            newScene(primaryStage, newGame);
        });
        MenuButton menuButton = new MenuButton("Menu", null, menuItem1, menuItem2, menuItem3, menuItem4);

        // menuItem2.setOnAction((event) -> System.exit(1));
        // menuItem3.setOnAction((event) -> System.exit(1));
        menuItem4.setOnAction((event) -> System.exit(1));

        game.getChildren().addAll(menuButton);
        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
    }

}
