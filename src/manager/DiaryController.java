package manager;

import com.jfoenix.controls.*;
import com.jfoenix.skins.JFXDatePickerContent;
import com.jfoenix.skins.JFXDatePickerSkin;
import com.jfoenix.validation.RequiredFieldValidator;
import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import org.controlsfx.control.spreadsheet.Grid;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class DiaryController {
    @FXML public Label currentWeek;
    @FXML public JFXButton decWeek;
    @FXML public JFXButton incWeek;
    @FXML public JFXDatePicker datePicker;
    @FXML public ColumnConstraints timeCol;
    @FXML public GridPane meetingsGrid;
    @FXML public TextField timeField;
    @FXML public TextField intervalField;
    @FXML public StackPane rootDiaryPane;
    @FXML public GridPane columnHeads;
    @FXML public Label editMeetingTitleLabel;
    @FXML public JFXTextField editMeetingTitle;
    @FXML public Label editMeetingDateLabel;
    @FXML public JFXDatePicker editMeetingDate;
    @FXML public Label editMeetingTimeLabel;
    @FXML public JFXTimePicker editMeetingTime;
    @FXML public Label editMeetingDurationLabel;
    @FXML public JFXTimePicker editMeetingDuration;
    @FXML public JFXCheckBox editMeetingIsRecurring;
    @FXML public GridPane editMeetingRecurrenceConf;
    @FXML public VBox editMeetingVBox;
    @FXML public StackPane editMeetingRootPane;
    @FXML public JFXTextArea editMeetingDescription;
    @FXML public JFXComboBox editMeetingAttendeesSearch;
    @FXML public FlowPane editMeetingAttendeesList;


    private Calendar startTime = Calendar.getInstance(Locale.getDefault());
    private Calendar interval = Calendar.getInstance(Locale.getDefault());
    private Calendar currentDate = Calendar.getInstance(Locale.getDefault());
    private Database db = new Database();
    private int employeeID = 1;
    private LinkedList<Event> events;
    // TODO: 17/03/2018 possibly introduce a new class to store pane and event reference 
    private LinkedList<Event> displayedEvents = new LinkedList<>();
    private LinkedList<StackPane> panes = new LinkedList<>();
    private Node currentDayIndic;
    private int[] weekdayTranslate = {0, 7, 1, 2, 3, 4, 5, 6};

    @FXML
    void initialize() {
        db.setup();
        // TODO: 16/03/2018 change the way employeeID is stored and accessed on a top level
        events = db.getEmployeeEvents(employeeID);

        AnchorPane.setRightAnchor(rootDiaryPane, 0.0);
        AnchorPane.setBottomAnchor(rootDiaryPane, 0.0);
        AnchorPane.setLeftAnchor(rootDiaryPane, 0.0);
        AnchorPane.setTopAnchor(rootDiaryPane, 0.0);

        currentWeek.setText(String.format("WEEK %d", currentDate.get(Calendar.WEEK_OF_MONTH)));
        currentDayIndic = columnHeads.getChildren().get(weekdayTranslate[currentDate.get(Calendar.DAY_OF_WEEK)]);
        indicateCurrentDay();
        datePicker.setValue(LocalDate.now());

        startTime.set(Calendar.HOUR_OF_DAY, 8);
        startTime.set(Calendar.MINUTE, 0);
        interval.set(Calendar.MINUTE, 30);

        editMeetingTitle.focusedProperty().addListener((o, oldVal, newVal) ->{
            System.out.println(editMeetingTitle.getText());
            if(!newVal) {
                if (editMeetingTitle.getText().equals("")) {
                    editMeetingTitleLabel.setStyle("-fx-text-fill: red");
                } else {
                    editMeetingTitleLabel.setStyle(null);
                }
            }
        });

        editMeetingDate.focusedProperty().addListener((o, oldVal, newVal) -> {
            System.out.println(editMeetingDate.getValue());
            if(!newVal) {
                if(editMeetingDate.getValue() == null) {
                    editMeetingDateLabel.setStyle("-fx-text-fill: red");
                }else {
                    editMeetingDateLabel.setStyle(null);
                }
            }
        });

        editMeetingTime.setIs24HourView(true);
        editMeetingTime.setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm")));

        editMeetingTime.focusedProperty().addListener((o, oldVal, newVal) -> {
            if(!newVal) {
                if(editMeetingTime.getValue() == null) {
                    editMeetingTimeLabel.setStyle("-fx-text-fill: red");
                } else {
                    editMeetingTimeLabel.setStyle(null);
                }
            }
        });

        editMeetingDuration.setIs24HourView(true);
        editMeetingDuration.setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm")));

        editMeetingDuration.focusedProperty().addListener((o, oldVal, newVal) -> {
            if(!newVal) {
                if(editMeetingDuration.getValue() == null) {
                    editMeetingDurationLabel.setStyle("-fx-text-fill: red");
                } else {
                    editMeetingDurationLabel.setStyle(null);
                }
            }
        });


        editMeetingIsRecurring.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println(editMeetingIsRecurring.isSelected());
                if(editMeetingIsRecurring.isSelected()) {
                    editMeetingRecurrenceConf.setDisable(false);
                } else {
                    editMeetingRecurrenceConf.setDisable(true);
                }
            }
        });

        // TODO: 19/03/2018 Add recurrence support

        System.out.println(Locale.getDefault() + ": " + startTime.getFirstDayOfWeek());
        updateTimeInterval();
        displayMeetings();

    }
    private void constructEditEventDialog(Event e) {
        editMeetingVBox.setDisable(false);
        editMeetingVBox.setOpacity(1.0);
        editMeetingVBox.setStyle("-fx-background-color: rgb(255, 255, 255, 1)");

        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                meetingsGrid.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
                FadeTransition fadeTransition = new FadeTransition();
                fadeTransition.setDuration(Duration.millis(100));
                fadeTransition.setNode(editMeetingVBox);
                fadeTransition.setFromValue(1);
                fadeTransition.setToValue(0);
                fadeTransition.play();
                editMeetingVBox.setDisable(true);
                editMeetingVBox.setOpacity(0);
            }
        };
        meetingsGrid.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);


        editMeetingTitle.setText(e.getTitle());
        editMeetingDate.setValue(e.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        editMeetingTime.setValue(LocalDateTime.ofInstant(
                e.getStart().toInstant(), ZoneId.systemDefault()).toLocalTime());


        long duration = (e.getEnd().getTime() - e.getStart().getTime());
        editMeetingDuration.setValue(LocalDateTime.ofInstant(Instant.ofEpochMilli(duration),
                ZoneId.systemDefault()).toLocalTime());

        // TODO: 19/03/2018 do recurrence here

        editMeetingDescription.setText(e.getDesc());

        LinkedList<Employee> employees = db.getEventAttendees(e.getId());
        for(Employee emp : employees) {
            HBox hBox = new HBox();

            Label label = new Label(emp.getName());
            label.setStyle("-fx-text-fill: white");
            label.setMinWidth(Control.USE_COMPUTED_SIZE);
            label.setMaxWidth(Control.USE_COMPUTED_SIZE);
            hBox.getChildren().add(label);

            Button btn = new Button("x");
            btn.setStyle("-fx-background-color: rgba(255, 255, 255, 0); -fx-text-fill: white");
            btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
//                    employees.remove(e);
                    editMeetingAttendeesList.getChildren().remove(hBox);
                }
            });

            hBox.getChildren().add(btn);
            hBox.setAlignment(Pos.CENTER);
            hBox.setStyle("-fx-background-color: rgb(49, 68, 99);-fx-background-radius: 5;");
            FlowPane.setMargin(hBox, new Insets(0, 5, 5, 0));
            editMeetingAttendeesList.getChildren().add(hBox);
        }
//        JFXDialogLayout layout = new JFXDialogLayout();
//        JFXDialog dialog = new JFXDialog();
//
//        StackPane rootPane = null;
//
//        try {
//            rootPane = FXMLLoader.load(getClass().getResource("/fxml/createEvent.fxml"));
////            rootPane = FXMLLoader.load(getClass().getResource("/fxml/meetingDetails.fxml"));
//        } catch (Exception ex){
//            System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
//            return;
//        }
//
//        JFXTextField title = (JFXTextField) rootPane.lookup("#editMeetingTitle");
//        Label label[] = { (Label) rootPane.lookup("#editMeetingTitleLabel")};
//        RequiredFieldValidator validator = new RequiredFieldValidator();
//        validator.setMessage("Meeting title is required");
//        title.getValidators().add(validator);
//
////        label[].setStyle("-fx-text-fill: red");
//        title.focusedProperty().addListener((o, oldVal, newVal) -> {
//            if(!newVal) {
//                if(title.getText() == null)  {
//                    // TODO: 19/03/2018 add actual validation
//                    System.out.println(title.getText());
//                    label[0].setStyle("-fx-text-fill: red");
//                } else {
////                    label.setStyle(null);
//                }
//            }
//        });
//
//        JFXDatePicker meetingDatePicker = (JFXDatePicker) rootPane.lookup("#editMeetingDate");
//        meetingDatePicker.focusedProperty().addListener((o, oldVal, newVal) -> {
//            if(!newVal) {
//                System.out.println(meetingDatePicker.getValue());
//                if(meetingDatePicker.getValue() == null) {
//                    meetingDatePicker.setStyle("-fx-text-fill: red");
//                }
//            }
//        });
//
//        JFXTimePicker meetingTimePicker = (JFXTimePicker) rootPane.lookup("#editMeetingTime");
//        meetingTimePicker.setIs24HourView(true);
//        meetingTimePicker.setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"),
//                DateTimeFormatter.ofPattern("HH:mm")));
//
//        meetingTimePicker.focusedProperty().addListener((o, oldVal, newVal) -> {
//            if(!newVal) {
//                System.out.println(meetingTimePicker.getValue());
//            }
//        });
//
//        JFXTimePicker meetingDurationPicker = (JFXTimePicker) rootPane.lookup("#editMeetingDuration");
//        meetingDurationPicker.setIs24HourView(true);
//        meetingDurationPicker.setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"),
//                DateTimeFormatter.ofPattern("HH:mm")));
//        meetingDurationPicker.focusedProperty().addListener((o, oldVal, newVal) -> {
//            if(!newVal) {
//                System.out.println(meetingDurationPicker.getValue());
//            }
//        });
//
//        JFXCheckBox isRecurring = (JFXCheckBox) rootPane.lookup("#editMeetingIsRecurring");
//        GridPane recurrenceConf = (GridPane) rootPane.lookup("#editMeetingRecurrenceConf");
//        isRecurring.selectedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                System.out.println(isRecurring.isSelected());
//                if(isRecurring.isSelected()) {
//                    recurrenceConf.setDisable(false);
//                } else {
//                    recurrenceConf.setDisable(true);
//                }
//            }
//        });
//
//
//        layout.setHeading(rootPane);
//        dialog.setDialogContainer(rootDiaryPane);
//        dialog.setContent(layout);
//        dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
//
//        dialog.focusedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                if(newValue) {
//                    dialog.close();
//                }
//            }
//        });
//        dialog.show();
    }

    private void constructDetailedMeetingDialog(Event e) {
        JFXDialogLayout layout = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog();

        StackPane rootPane = null;
        try {
            rootPane = FXMLLoader.load(getClass().getResource("/fxml/meetingDetails.fxml"));
        } catch (Exception ex) {
            System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
            return;
        }



        // TODO: 18/03/2018 change this from current time to time created 
        Calendar currentTime = Calendar.getInstance();
        Label label = (Label) rootPane.lookup("#currentTime");
        label.setText(new SimpleDateFormat("YYYY MM dd").format(currentTime.getTime()));

        label = (Label) rootPane.lookup("#currentDate");
        label.setText(new SimpleDateFormat("HH:mm").format(currentTime.getTime()));

        label = (Label) rootPane.lookup("#meetingSubject");
        label.setText(e.getTitle());

        label = (Label) rootPane.lookup("#meetingStart");
        label.setText(new SimpleDateFormat("HH:mm").format(e.getStart()));

        label = (Label) rootPane.lookup("#meetingEnd");
        label.setText(new SimpleDateFormat("HH:mm").format(e.getEnd()));

        label = (Label) rootPane.lookup("#meetingDate");
        label.setText(new SimpleDateFormat("YYYY-MM-dd").format(e.getStart()));

        label = (Label) rootPane.lookup("#meetingDuration");
        long duration = (e.getEnd().getTime() - e.getStart().getTime()) / 60000;
        label.setText(String.format("%d minutes", duration));

        label = (Label) rootPane.lookup("#meetingLocation");
        label.setText(e.getLocation());

        label = (Label) rootPane.lookup("#meetingDescription");
        label.setText(e.getDesc());

        LinkedList<Employee> attendees = db.getEventAttendees(e.getId());
        GridPane gridPane = (GridPane) rootPane.lookup("#meetingAttendeesGrid");
        label = new Label();
        label.setStyle("-fx-font: 15 Helvetica; -fx-font-weight: Oblique;");
        StringJoiner joiner = new StringJoiner(", ");
        for(Employee emp : attendees) {
            joiner.add(emp.getName());
        }
        label.setText(joiner.toString());
        label.setWrapText(true);
        gridPane.add(label, 2, 0);

        gridPane = (GridPane) rootPane.lookup("#detailedMeetingGrid");
        GridPane buttonGrid = new GridPane();
        Button okay = createButtonForDialog("Okay");

        okay.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        buttonGrid.add(okay, 0, 0);

        if(employeeID == e.getOrganizer().getId()) {
            Button edit = createButtonForDialog("Edit");

            edit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dialog.close();
                    constructEditEventDialog(e);
                    System.out.println("Editing");
                }
            });
            buttonGrid.add(edit, 1, 0);
        }
        gridPane.add(buttonGrid, 0, 3);
        layout.setHeading(rootPane);

        dialog.setDialogContainer(rootDiaryPane);
        dialog.setContent(layout);
        dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);

        dialog.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue) {
                    dialog.close();
                }
            }
        });
        dialog.show();
    }

    private Button createButtonForDialog(String text) {
        Button button = new Button(text);
        button.setMinHeight(45);
        button.setMinWidth(100);
        button.setStyle("-fx-background-color: rgb(49, 68, 99);" +
                "-fx-font: Helvetica;-fx-text-fill: white");
        GridPane.setHgrow(button, Priority.ALWAYS);
        GridPane.setVgrow(button, Priority.ALWAYS);
        GridPane.setHalignment(button, HPos.CENTER);

        return button;
    }

    private void updateTimeInterval() {
        meetingsGrid.getChildren().clear();
        DateFormat df = new SimpleDateFormat("HH:mm");
        Calendar cal = Calendar.getInstance();
        int h = startTime.get(Calendar.HOUR_OF_DAY);
        int m = startTime.get(Calendar.MINUTE);
        int inter = interval.get(Calendar.MINUTE);
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        cal.set(Calendar.SECOND, 0);
        int startDate = cal.get(Calendar.DATE);

        System.out.println(inter);
        for(int i = 1; i <= (24-h)*60; i++) {
            Pane p = new Pane();
            p.setMinHeight(1);
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.NEVER);
            meetingsGrid.getRowConstraints().add(rc);
            if(i % inter == 0) {
                p.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: rgba(1, 1, 1, 0.1)");
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
                currentDate.add(Calendar.WEEK_OF_MONTH, 1);
                break;
            case "decWeek":
                currentDate.add(Calendar.WEEK_OF_MONTH, -1);
                break;
        }
        currentWeek.setText(String.format("WEEK %d", currentDate.get(Calendar.WEEK_OF_MONTH)));
        Date input = currentDate.getTime();
        datePicker.setValue(input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        displayMeetings();
    }

    public void dateChosen(ActionEvent actionEvent) {
        currentDate.setTime(Date.from(
                datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        System.out.println(currentDate.getTime().toInstant());
        currentWeek.setText(String.format("WEEK %d", currentDate.get(Calendar.WEEK_OF_MONTH)));
        indicateCurrentDay();
        displayMeetings();
    }

    private void displayMeetings() {
        clearMeetings();
        for(Event e : events) {
            Calendar start = Calendar.getInstance();
            start.setTime(e.getStart());

            Calendar end = Calendar.getInstance();
            end.setTime(e.getEnd());

            int startY = start.get(Calendar.YEAR);
            int startM = start.get(Calendar.MONTH);
            int startD = start.get(Calendar.DAY_OF_MONTH);

            int endY = end.get(Calendar.YEAR);
            int endM = end.get(Calendar.MONTH);
            int endD = end.get(Calendar.DAY_OF_MONTH);

            if(startY == endY && startM == endM && startD == endD) {
                if(start.get(Calendar.HOUR_OF_DAY) >= startTime.get(Calendar.HOUR_OF_DAY) &&
                        start.get(Calendar.WEEK_OF_YEAR) == currentDate.get(Calendar.WEEK_OF_YEAR)) {

                    System.out.println("Adding meeting");
                    displayedEvents.add(e);
                    int[] meetingPos = getMeetingPosition(start, end);
                    StackPane pane = createMeetingPane(e);
                    pane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            StackPane p = (StackPane) event.getSource();
                            constructDetailedMeetingDialog(displayedEvents.get(panes.indexOf(p)));
                            System.out.printf("\nEvent: %s", displayedEvents.get(panes.indexOf(p)).getTitle());
                        }
                    });
                    panes.add(pane);
                    meetingsGrid.add(pane, meetingPos[0], meetingPos[1], 1, meetingPos[2]);
                }
            }
        }
    }

    private StackPane createMeetingPane(Event event) {
        StackPane pane = new StackPane();

        GridPane grid = new GridPane();

        Label time = new Label(String.format("%s - %s",
                new SimpleDateFormat("HH:mm").format(event.getStart()),
                new SimpleDateFormat("HH:mm").format(event.getEnd())));
        GridPane.setHgrow(time, Priority.ALWAYS);
        time.setMaxWidth(Double.MAX_VALUE);
        time.setStyle("-fx-font: 13 Helvetica");
        grid.add(time, 0, 0);

        Label label = new Label(event.getTitle());
        label.setStyle("-fx-font: 13 Helvetica");
        grid.add(label, 0, 1);

        long span = (event.getEnd().getTime() - event.getStart().getTime()) / 60000;
        label = new Label(String.format("Duration: %d min", span));
        label.setStyle("-fx-font: 13 Helvetica");
        grid.add(label, 0, 2);

        label = new Label(String.format("Location: %s", event.getLocation()));
        label.setStyle("-fx-font: 13 Helvetica");
        grid.add(label, 0, 3);

        label = new Label(String.format("Organizer: %s", event.getOrganizer().getName()));
        label.setStyle("-fx-font: 13 Helvetica");
        grid.add(label, 0, 4);


        String backAndBorderCol = "";
        String timeColor = "";
        switch (event.getPriority()) {
            case 0:
                backAndBorderCol = "-fx-background-color: rgba(245, 218, 170, 0.7);" +
                        "-fx-border-width: 2, 2, 2, 2; -fx-border-color: rgb(245, 218, 170)";
                timeColor = "-fx-background-color: rgb(245, 218, 170)";
                break;
            case 1:
                backAndBorderCol = "-fx-background-color: rgba(255, 160, 0, 0.7);" +
                        "-fx-border-width: 2, 2, 2, 2; -fx-border-color: rgb(255, 160, 0)";
                timeColor = "-fx-background-color: rgb(255, 160, 0)";
                break;
            case 2:
                backAndBorderCol = "-fx-background-color: rgba(255, 86, 86, 0.7);" +
                        "-fx-border-width: 2, 2, 2, 2; -fx-border-color: rgb(255, 86, 86)";
                timeColor = "-fx-background-color: rgb(255, 86, 86)";
                break;
        }

        time.setStyle(timeColor);
        pane.setStyle(backAndBorderCol);

        pane.getChildren().addAll(grid);

        return pane;
    }

    private int[] getMeetingPosition(Calendar start, Calendar end) {
        int minutes = (((end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE)) -
                (start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE))));

        int startMinute = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE) -
                startTime.get(Calendar.HOUR_OF_DAY) * 60 - startTime.get(Calendar.MINUTE);

        int dayColumn = start.get(Calendar.DAY_OF_WEEK) - 1;
        return new int[]{dayColumn, startMinute, minutes};
    }

    private void clearMeetings() {
        for(Pane p : panes) {
            meetingsGrid.getChildren().remove(p);
        }
    }

    private void indicateCurrentDay() {
        currentDayIndic.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: black");
        currentDayIndic = columnHeads.getChildren().get(weekdayTranslate[currentDate.get(Calendar.DAY_OF_WEEK)]);
        currentDayIndic.setStyle("-fx-border-width: 0 0 3 0; -fx-border-color: green");
    }
}
