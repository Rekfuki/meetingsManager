package manager;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class LoginController {
    @FXML public StackPane rootPane;
    @FXML public JFXButton signIn;
    @FXML public JFXButton signUp;
    @FXML public JFXTextField usernameField;
    @FXML public JFXPasswordField passwordField;
    @FXML public Label loginLabel;
    @FXML public Label signUpLabel;
    @FXML public JFXButton backButton;
    @FXML public StackPane loginPane;
    @FXML public JFXButton gotoSignUp;
    @FXML public StackPane signUpPane;
    @FXML public JFXTextField signUpName;
    @FXML public JFXTextField signUpUsername;
    @FXML public JFXPasswordField signUpPassword;

    private Database db = new Database();
    private int employeeId;


    @FXML
    void initialize() {
        RequiredFieldValidator validator = new RequiredFieldValidator();
        validator.setMessage("NAME IS REQUIRED");
        signUpName.getValidators().add(validator);
        signUpName.focusedProperty().addListener((o, oldVal, newVal) -> {
            if(!newVal) {
                signUpName.validate();
            }
        });

        validator = new RequiredFieldValidator();
        validator.setMessage("EMAIL IS REQUIRED");
        signUpUsername.getValidators().add(validator);
        signUpUsername.focusedProperty().addListener((o, oldVal, newVal) -> {
            if(!newVal) {
                signUpUsername.validate();
            }
        });

        validator = new RequiredFieldValidator();
        validator.setMessage("PASSWORD IS REQUIRED");
        signUpPassword.getValidators().add(validator);
        signUpPassword.focusedProperty().addListener((o, oldVal, newVal) ->{
            if(!newVal) {
                signUpPassword.validate();
            }
        });

    }

    public void attemptSignIn(ActionEvent actionEvent) {
        if(signIn()) {
            loginLabel.setText("SUCCESS");
            loginLabel.setStyle("-fx-text-fill: green");
            loginLabel.setOpacity(1.0);
            // TODO: 15/03/2018 store id and load diary
            try {
                Task<Parent> loadTask = new Task<Parent>() {
                    @Override
                    public Parent call() throws IOException {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/diary.fxml"));
                        DiaryController controller = new DiaryController(db.getEmployeeByID(employeeId));
                        loader.setController(controller);

                        return (StackPane) loader.load();


//                        return (StackPane) FXMLLoader.load(getClass().getResource("/fxml/diary.fxml"));
                    }
                };
                loadTask.setOnSucceeded(e -> {
                    rootPane.getChildren().setAll(loadTask.getValue());
                });

                loadTask.setOnFailed(e -> loadTask.getException().printStackTrace());

                Thread thread = new Thread(loadTask);
                thread.start();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            }
        } else {
            System.out.println("login failed");
            incorrect();
        }
    }
    private boolean signIn(){
        String username = usernameField.getText();
        String password = passwordField.getText();
        employeeId = db.getEmployeeID(username, password);
        return employeeId != 0;
    }

    public void signUp(MouseEvent mouseEvent) {
        if(signUpName.validate() && signUpUsername.validate() && signUpPassword.validate()) {
            if(db.checkUsername(signUpUsername.getText())) {
                if(db.addEmployee(signUpName.getText(), signUpUsername.getText(), signUpPassword.getText())) {
                    signUpLabel.setText("SUCCESS");
                    signUpLabel.setStyle("-fx-text-fill: green");
                    // TODO: 16/03/2018 load diary window and store employee id 
                }
            } else {
                signUpLabel.setText("EMAIL ALREADY EXISTS");
                signUpLabel.setStyle("-fx-text-fill: red");
            }
        }
    }


    private void incorrect() {
        usernameField.setStyle("-jfx-unfocus-color: red");
        passwordField.setStyle("-jfx-unfocus-color: red");
        loginLabel.setText("WRONG EMAIL OR PASSWORD");
        loginLabel.setStyle("-fx-text-fill: red");
        loginLabel.setOpacity(1.0);
    }

    public void backToLogin(MouseEvent mouseEvent) {
        signUpPane.setDisable(true);
        signUpPane.setOpacity(0.0);
        signUpLabel.setStyle(null);
        signUpLabel.setText("");
        signUpName.resetValidation();
        signUpUsername.resetValidation();
        signUpPassword.resetValidation();

        loginPane.setDisable(false);
        loginPane.setOpacity(1.0);
    }

    public void gotoSignUp(MouseEvent mouseEvent) {
        loginPane.setDisable(true);
        loginPane.setOpacity(0.0);
        loginLabel.setStyle(null);
        loginLabel.setOpacity(0);
        loginLabel.setText("");
        signUpPane.setDisable(false);
        signUpPane.setOpacity(1.0);
    }
}
