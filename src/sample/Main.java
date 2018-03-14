package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = (Parent)loader.load();
        Controller controller = loader.getController();
        controller.stage = primaryStage;

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1200, 1000));
//        Controller c = new Controller();
//        c.fillHours(120);
//        c.updateTimeInterval(30);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
