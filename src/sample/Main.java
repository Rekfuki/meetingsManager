package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static int currentEmployeeID;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/startup.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1200, 1000));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void setCurrentEmployeeID(int id) {
        currentEmployeeID = id;
    }

    public static int getCurrentEmployeeID() {
        return currentEmployeeID;
    }
}
