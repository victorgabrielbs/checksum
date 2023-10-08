package com.kldv;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxml = new FXMLLoader(App.class.getResource("primary.fxml"));
            Scene scene = new Scene(fxml.load());
            Image icon = new Image(getClass().getResourceAsStream("compact-disc-solid.png"));

            stage.setTitle("Teste uma iso com sua soma de verifição");
            stage.getIcons().add(icon);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
