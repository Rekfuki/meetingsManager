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


/**
 * controller for the login/signup part of GUI
 */
public class LoginController {

    /**
     * all of the graphical fxml elements
     */
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

    /**
     * instance of a database connection
     */
    private Database db = new Database();

    /**
     * employee id that is retrieved after successful login
     */
    private int employeeId;


    /**
     * Runs during the start of the login window
     */
    @FXML
    void initialize() {
        db.setup(); //checks if database contains tables and triggers
        RequiredFieldValidator validator = new RequiredFieldValidator();
        validator.setMessage("NAME IS REQUIRED");
        signUpName.getValidators().add(validator);
        signUpName.focusedProperty().addListener((o, oldVal, newVal) -> { //event listener is bound to the name field
            if(!newVal) {
                signUpName.validate();
            }
        });

        validator = new RequiredFieldValidator();
        validator.setMessage("EMAIL IS REQUIRED");
        signUpUsername.getValidators().add(validator);
        signUpUsername.focusedProperty().addListener((o, oldVal, newVal) -> { //event listener is bound to the username field
            if(!newVal) {
                signUpUsername.validate();
            }
        });

        validator = new RequiredFieldValidator();
        validator.setMessage("PASSWORD IS REQUIRED");
        signUpPassword.getValidators().add(validator);
        signUpPassword.focusedProperty().addListener((o, oldVal, newVal) ->{ //event listener is bound to the password field
            if(!newVal) {
                signUpPassword.validate();
            }
        });

    }

    /**
     * Event that fires whenever sign in is pressed
     * @param actionEvent Type of an actionEvent
     */
    public void attemptSignIn(ActionEvent actionEvent) {
        if(signIn()) { //if login is successful
            loginLabel.setText("SUCCESS");
            loginLabel.setStyle("-fx-text-fill: green");
            loginLabel.setOpacity(1.0);
            try { // diary fxml file and controller is created in separate thread to prevent GUI lockout
                Task<Parent> loadTask = new Task<Parent>() {
                    @Override
                    public Parent call() throws IOException {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/diary.fxml"));
                        DiaryController controller = new DiaryController(db.getEmployeeByID(employeeId));
                        loader.setController(controller);

                        return (StackPane) loader.load();
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
            incorrect();
        }
    }

    /**
     * Retrieves text from username and password fields and retrieves user id from the database
     * @return user exists in the database
     */
    private boolean signIn(){
        String username = usernameField.getText();
        String password = passwordField.getText();
        employeeId = db.getEmployeeID(username, password);
        return employeeId != 0;
    }

    /**
     * Event that fires whenever sign up is pressed
     * @param mouseEvent type of a mouse event
     */
    public void signUp(MouseEvent mouseEvent) {
        if(signUpName.validate() && signUpUsername.validate() && signUpPassword.validate()) { //if validation pass
            if(db.checkUsername(signUpUsername.getText())) { //if username is not taken
                if(db.addEmployee(signUpName.getText(), signUpUsername.getText(), signUpPassword.getText())) { //if user is created successfully
                    signUpLabel.setText("SUCCESS");
                    signUpLabel.setStyle("-fx-text-fill: green");
                    loginLabel.setText("USER SUCCESSFULLY CREATED");
                    loginLabel.setStyle("-fx-text-fill: green");

                    backToLogin(null); //returns back to the login widow
                }
            } else {
                signUpLabel.setText("EMAIL ALREADY EXISTS");
                signUpLabel.setStyle("-fx-text-fill: red");
            }
        }
    }

    /**
     * Indicates if login fails
     */
    private void incorrect() {
        usernameField.setStyle("-jfx-unfocus-color: red");
        passwordField.setStyle("-jfx-unfocus-color: red");
        loginLabel.setText("WRONG EMAIL OR PASSWORD");
        loginLabel.setStyle("-fx-text-fill: red");
        loginLabel.setOpacity(1.0);
    }

    /**
     * Goes back to login pane when back is pressed or new user is successfully created.
     * Resets all fields and labels
     * @param mouseEvent type of a mouse event
     */
    public void backToLogin(MouseEvent mouseEvent) {
        signUpPane.setDisable(true);
        signUpPane.setOpacity(0.0);
        signUpLabel.setStyle(null);
        signUpLabel.setText("");
        signUpName.resetValidation();
        signUpUsername.resetValidation();
        signUpPassword.resetValidation();
        signUpName.clear();
        signUpUsername.clear();
        signUpPassword.clear();

        loginPane.setDisable(false);
        loginPane.setOpacity(1.0);
    }

    /**
     * goes to sign up pane when sign up button is pressed.
     * clears all of the fields
     * @param mouseEvent type of a mouse event
     */
    public void gotoSignUp(MouseEvent mouseEvent) {
        loginPane.setDisable(true);
        loginPane.setOpacity(0.0);
        loginLabel.setStyle(null);
        loginLabel.setText("");
        signUpPane.setDisable(false);
        signUpPane.setOpacity(1.0);
        usernameField.clear();
        passwordField.clear();
    }
}
