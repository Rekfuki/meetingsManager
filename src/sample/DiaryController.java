package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.controlsfx.control.PopOver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;


public class DiaryController {
    @FXML public Label currentWeek;
    @FXML public JFXButton decWeek;
    @FXML public JFXButton incWeek;
    @FXML public JFXDatePicker datePicker;
    @FXML public ColumnConstraints timeCol;
    @FXML public GridPane meetingsGrid;
    @FXML public TextField timeField;
    @FXML public TextField intervalField;
    @FXML public AnchorPane rootDiaryPane;


    private Calendar startTime = Calendar.getInstance();
    private Calendar interval = Calendar.getInstance();
    private Calendar currentTime = Calendar.getInstance();
    private Database db = new Database();
    public Stage stage;

    @FXML
    void initialize() {
        // TODO: 15/03/2018 Create login window and save id received after a successful login attempt 
        db.setup();
        System.out.printf("\nID: %d", Main.getCurrentEmployeeID());
        AnchorPane.setRightAnchor(rootDiaryPane, 0.0);
        AnchorPane.setBottomAnchor(rootDiaryPane, 0.0);
        AnchorPane.setLeftAnchor(rootDiaryPane, 0.0);
        AnchorPane.setTopAnchor(rootDiaryPane, 0.0);

        currentWeek.setText(String.format("WEEK %d", currentTime.get(Calendar.WEEK_OF_MONTH)));
        datePicker.setValue(LocalDate.now());
        startTime.set(Calendar.HOUR, 8);
        startTime.set(Calendar.MINUTE, 0);
        interval.set(Calendar.MINUTE, 30);

        updateTimeInterval();
//        meetingsGrid.setGridLinesVisible(true);
        Pane p = new Pane(new Label("Test meeting"));

        p.setStyle("-fx-background-color: grey");
        p.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.print("test");
                constructDialog(p);
//                JFXDialog d = constructDialog();
//                d.show();
            }
        });
        meetingsGrid.add(p, 6, 123, 1, 45);

    }

    private void setupGrid() {
        int hour = startTime.get(Calendar.HOUR);
        int minutes = startTime.get(Calendar.MINUTE);


    }
    //// TODO: 13/03/2018 pass params to construct the dialog 
    private void constructDialog(Pane p) {
        PopOver pop = new PopOver();
        VBox box = new VBox();
        box.setPadding(new Insets(10));

        box.getChildren().add(new Text("test"));

        pop.setContentNode(box);

        pop.show(p);
//        JFXDialogLayout content = new JFXDialogLayout();
//        content.setHeading(new Text("heading"));
//        content.setBody(new Text("some information about the meeting goes here"));
//        StackPane stackPane = new StackPane();
//        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
//        JFXButton button = new JFXButton("Okay");
//        button.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                dialog.close();
//            }
//        });
//        content.setActions(button);
//        Scene scene = new Scene(stackPane, 300, 250);
////        meetingsGrid.
//        dialog.show();
//        return dialog;
    }
    private void updateTimeInterval() {
        meetingsGrid.getChildren().clear();
        DateFormat df = new SimpleDateFormat("HH:mm");
        Calendar cal = Calendar.getInstance();
        int h = startTime.get(Calendar.HOUR);
        int m = startTime.get(Calendar.MINUTE);
        int inter = interval.get(Calendar.MINUTE);
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        cal.set(Calendar.SECOND, 0);
        int startDate = cal.get(Calendar.DATE);

        System.out.println(inter);
        for(int i = 1; i <= (24-h)*60; i++) {
            Pane p = new Pane();
            p.setMinHeight(2);
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.NEVER);
            meetingsGrid.getRowConstraints().add(rc);
            if(i % inter == 0) {
                p.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: black");
                meetingsGrid.add(p, 0, i, 8, 1);
                continue;
            }
            meetingsGrid.add(p, 0, i);
        }

        int index = 0;
        while (cal.get(Calendar.DATE) == startDate) {
            Label txt = new Label(df.format(cal.getTime()));
            txt.setStyle("-fx-text-fill: black");
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.setMinSize(125, 30);
            hbox.getChildren().add(txt);

            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            meetingsGrid.getRowConstraints().add(rc);
            meetingsGrid.add(hbox, 0, index, 1, inter);
            System.out.println(df.format(cal.getTime()));

            cal.add(Calendar.MINUTE, inter);
            index += inter;
        }
    }


    public void intervalAndTimeWatcher(KeyEvent keyEvent) {
        Date t = verifyTime(timeField.getCharacters().toString(), "HH:mm");
        Date i = verifyTime(intervalField.getCharacters().toString(), "mm");

        if(t != null) {
            intervalField.setStyle("-fx-border-color: green");
        } else {
            intervalField.setStyle("-fx-border-color: red");
        }

        if(i != null) {
            intervalField.setStyle("-fx-border-color: green");
        } else {
            intervalField.setStyle("-fx-border-color: red");
        }

        if(keyEvent.getCode() == KeyCode.ENTER) {
            if(t != null) {
                timeField.setStyle("-fx-border-color: black");
                startTime.setTime(t);
            }

            if(i != null) {
                intervalField.setStyle("-fx-border-color: black");
                interval.setTime(i);
            }
            updateTimeInterval();
        }
    }

    private Date verifyTime(String time, String pattern) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setLenient(false); //this will not enable 25:67 for example
        try {
            return dateFormat.parse(time);
        } catch (ParseException e) {
            return null;
        }
    }


    public void changeWeekEvent(MouseEvent mouseEvent) {
        JFXButton btn = (JFXButton) mouseEvent.getSource();
        String id = btn.getId();

        switch(id) {
            case "incWeek":
                currentTime.add(Calendar.WEEK_OF_MONTH, 1);
                // TODO: 14/03/2018 increase week
                break;
            case "decWeek":
                currentTime.add(Calendar.WEEK_OF_MONTH, -1);
                // TODO: 14/03/2018 decrease week
                break;
        }
        currentWeek.setText(String.format("WEEK %d", currentTime.get(Calendar.WEEK_OF_MONTH)));
        Date input = currentTime.getTime();
        datePicker.setValue(input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public void dateChosen(ActionEvent actionEvent) {
        currentTime.setTime(Date.from(
                datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        System.out.println(currentTime.getTime().toInstant());
        currentWeek.setText(String.format("WEEK %d", currentTime.get(Calendar.WEEK_OF_MONTH)));
    }
}
