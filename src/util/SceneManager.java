package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/view/fxml/" + fxmlPath)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            System.out.println("Gagal memuat scene " + fxmlPath + ": " + e.getMessage());
        }
    }

    public static <T> T switchSceneAndGetController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/view/fxml/" + fxmlPath)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            return loader.getController();
        } catch (IOException e) {
            System.out.println("Gagal memuat scene " + fxmlPath + ": " + e.getMessage());
            return null;
        }
    }
}