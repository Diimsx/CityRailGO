import javafx.application.Application;
import javafx.stage.Stage;
import util.SceneManager;

public class main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.setPrimaryStage(primaryStage);
        primaryStage.setTitle("CityRailGO - Sistem Pemesanan Tiket Kereta");
        primaryStage.setResizable(false);
        SceneManager.switchScene("login.fxml");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}