package manager;

import com.jfoenix.controls.*;
import com.sun.javafx.binding.StringFormatter;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.converter.LocalTimeStringConverter;
import org.controlsfx.control.textfield.TextFields;

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
    @FXML public JFXTextField editMeetingAttendeesSearch;
    @FXML public FlowPane editMeetingAttendeesList;
    @FXML public JFXButton editMeetingCancel;
    @FXML public JFXButton editMeetingSchedule;
    @FXML public Label editMeetingAttendeesSearchLabel;
    @FXML public Label editMeetingLocationLabel;
    @FXML public JFXTextField editMeetingLocation;
    @FXML public Label editMeetingPriorityLabel;
    @FXML public JFXComboBox editMeetingPriority;
    @FXML public Label meetingDetailsSubject;
    @FXML public Label meetingDetailsDate;
    @FXML public Label meetingDetailsStart;
    @FXML public Label meetingDetailsEnd;
    @FXML public Label meetingDetailsDuration;
    @FXML public Label meetingDetailsLocation;
    @FXML public Label meetingDetailsPriority;
    @FXML public Label meetingDetailsDescription;
    @FXML public Label meetingDetailsAttendees;
    @FXML public VBox meetingDetailsVBox;
    @FXML public GridPane meetingDetailsButtonsGrid;
    @FXML public VBox editMeetingSuggestionVBox;
    @FXML public Accordion editMeetingSuggestionAccordion;
    @FXML public JFXButton editMeetingSuggestionListCancel;
    @FXML public JFXTimePicker scheduleStart;
    @FXML public JFXTimePicker scheduleEnd;
    @FXML public JFXComboBox scheduleIntervals;


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

    // TODO: 21/03/2018 add day start and day end
    private Instant dayStart;
    private Instant dayEnd;

    @FXML
    void initialize() {
        db.setup();
        // TODO: 16/03/2018 change the way employeeID is stored and accessed on a top level

        AnchorPane.setRightAnchor(rootDiaryPane, 0.0);
        AnchorPane.setBottomAnchor(rootDiaryPane, 0.0);
        AnchorPane.setLeftAnchor(rootDiaryPane, 0.0);
        AnchorPane.setTopAnchor(rootDiaryPane, 0.0);

        scheduleStart.setIs24HourView(true);
        scheduleStart.setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm")));

        scheduleEnd.setIs24HourView(true);
        scheduleEnd.setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm")));
        rootDiaryPane.getStylesheets().add(getClass().getResource("/css/main.css").toString());
        editMeetingRootPane.getStylesheets().add(getClass().getResource("/css/editMeetings.css").toString());

        scheduleIntervals.getItems().addAll(
                FXCollections.observableArrayList("15 min", "30 min", "1 hour"));
        scheduleIntervals.setValue("15 min");

        scheduleStart.setValue(LocalTime.parse("08:00"));
        scheduleEnd.setValue(LocalTime.parse("20:00"));

        dayStart = stringToInstant(scheduleStart.getValue().toString(), "HH:mm");
        dayEnd = stringToInstant(scheduleEnd.getValue().toString(), "HH:mm");


        currentWeek.setText(String.format("WEEK %d", currentDate.get(Calendar.WEEK_OF_MONTH)));
        currentDayIndic = columnHeads.getChildren().get(weekdayTranslate[currentDate.get(Calendar.DAY_OF_WEEK)]);
        indicateCurrentDay();
        datePicker.setValue(LocalDate.now());


        startTime.set(Calendar.HOUR_OF_DAY, 8);
        startTime.set(Calendar.MINUTE, 0);
        interval.set(Calendar.MINUTE, 15);
        interval.set(Calendar.HOUR_OF_DAY, 0);


        // TODO: 21/03/2018 change event handler from focused to character  typed 
        
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

        editMeetingLocation.focusedProperty().addListener((o, oldVal, newVal) -> {
            if(!newVal) {
                if(editMeetingLocation.getText().equals("")) {
                    editMeetingLocationLabel.setStyle("-fx-text-fill: red");
                } else {
                    editMeetingLocationLabel.setStyle(null);
                }
            }
        });

        editMeetingPriority.getItems().addAll(new Label("Low"), new Label("Medium"), new Label("High"));

        editMeetingPriority.focusedProperty().addListener((o, oldVal, newVal) -> {
            if(!newVal) {
                if(editMeetingPriority.getValue() == null) {
                    editMeetingPriorityLabel.setStyle("-fx-text-fill: red");
                } else {
                    editMeetingPriorityLabel.setStyle(null);
                }
            }
        });

        editMeetingIsRecurring.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println(editMeetingIsRecurring.isSelected());
                if(editMeetingIsRecurring.isSelected()) {
                    // TODO: 19/03/2018 Add recurrence support
                } else {
                    editMeetingSchedule.setDisable(false);
                }
            }
        });

        editMeetingSuggestionListCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                closePane(editMeetingSuggestionVBox);
            }
        });

        scheduleStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dayStart = stringToInstant(scheduleStart.getValue().toString(), "HH:mm");
                System.out.println(scheduleStart.getValue().toString());
            }
        });

        scheduleEnd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dayEnd = stringToInstant(scheduleEnd.getValue().toString(), "HH:mm");
            }
        });

        System.out.println(Locale.getDefault() + ": " + startTime.getFirstDayOfWeek());
        updateTimeInterval();
        displayMeetings();

    }

    private Instant stringToInstant(String time, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(time).toInstant();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return null;
    }

    private void constructEditEventDialog(Event e) {
        editMeetingAttendeesList.getChildren().clear();

        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(100));
        fadeTransition.setNode(editMeetingVBox);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();

        editMeetingVBox.setDisable(false);
        editMeetingVBox.setOpacity(1.0);
        editMeetingVBox.setStyle("-fx-background-color: rgb(255, 255, 255, 1)");

        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!inHierarchy(event.getPickResult().getIntersectedNode(), editMeetingVBox) &&
                        !editMeetingVBox.isDisabled()) {
                    meetingsGrid.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
                    closePane(editMeetingVBox);
                }
            }
        };
        meetingsGrid.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);


        editMeetingTitle.setText(e.getTitle());
        editMeetingDate.setValue(e.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        editMeetingLocation.setText(e.getLocation());
        editMeetingTime.setValue(LocalDateTime.ofInstant(
                e.getStart().toInstant(), ZoneId.systemDefault()).toLocalTime());

        long duration = (e.getEnd().getTime() - e.getStart().getTime());
//        LocalTime.
//        LocalDateTime convertedDur = LocalDateTime.ofInstant(Instant.ofEpochMilli(duration), ZoneId.systemDefault());
        editMeetingDuration.setValue(LocalTime.from(Instant.ofEpochMilli(duration).atZone(ZoneId.of("UTC"))));

        // TODO: 19/03/2018 do recurrence here

        editMeetingPriority.getSelectionModel().select(e.getPriority());

        editMeetingDescription.setText(e.getDesc());

        LinkedList<Employee> employees = db.getEmployees();
        ArrayList<String> tmp = new ArrayList<>();
        for(Employee tmpEmp : employees) {
            StringJoiner joiner = new StringJoiner(", ");
            joiner.add(tmpEmp.getName());
            joiner.add(String.format("%s", tmpEmp.getEmail()));
            tmp.add(joiner.toString());
        }

        TextFields.bindAutoCompletion(editMeetingAttendeesSearch, tmp);


        LinkedList<Employee> eventAttendees = db.getEventAttendees(e.getId());

        editMeetingAttendeesSearch.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER) {
                    editMeetingAttendeesSearchLabel.setStyle(null);
                    String user[] = editMeetingAttendeesSearch.getText().split(", ");
                    boolean contains = false;
                    for(Employee emp : eventAttendees) {
                        if(user.length > 0 && emp.getEmail().equals(user[1])) {
                            contains = true;
                            editMeetingAttendeesSearchLabel.setStyle("-fx-text-fill: red");
                            break;
                        }
                    }
                    if(!contains) {
                        for(Employee emp : employees) {
                            if(emp.getEmail().equals(user[1])) {
                                editMeetingAttendeesSearchLabel.setStyle(null);
                                eventAttendees.add(emp);
                                editMeetingAttendeesList.getChildren().add(createAttendeeHBox(emp, eventAttendees));
                                editMeetingAttendeesSearch.setText("");
                            }
                        }
                    }
                }
                else {
                    editMeetingAttendeesSearchLabel.setStyle(null);
                }
            }
        });

        for(Employee emp : eventAttendees) {
            editMeetingAttendeesList.getChildren().add(createAttendeeHBox(emp, eventAttendees));
        }

        EventHandler<ActionEvent> cancelEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                editMeetingCancel.setOnAction(null);
                closePane(editMeetingVBox);
            }
        };
        editMeetingCancel.setOnAction(cancelEvent);

        editMeetingSchedule.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(validateForSchedule(eventAttendees)) {
                    // TODO: 21/03/2018 check if event only has one person and that person is the organizer
                    try {
                        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        start.setTime(sdf.parse(editMeetingDate.getValue().toString()));

                        String time[] = editMeetingTime.getValue().toString().split(":");
                        int hours = Integer.parseInt(time[0]);
                        int minutes = Integer.parseInt(time[1]);

                        start.add(Calendar.HOUR_OF_DAY, hours);
                        start.add(Calendar.MINUTE, minutes);

                        time = editMeetingDuration.getValue().toString().split(":");
                        hours = Integer.parseInt(time[0]);
                        minutes = Integer.parseInt(time[1]);

                        Calendar end = (Calendar) start.clone();
                        end.add(Calendar.HOUR_OF_DAY, hours);
                        end.add(Calendar.MINUTE, minutes);

                        Long newDuration = end.getTimeInMillis() - start.getTimeInMillis();
                        Calendar newDayStart = (Calendar) start.clone();
                        newDayStart.set(Calendar.HOUR_OF_DAY, ((int)dayStart.toEpochMilli() / 3600000) + 1);
                        newDayStart.set(Calendar.MINUTE, (int)(dayStart.toEpochMilli() % 3600000 / 60000));

                        Calendar newDayEnd = (Calendar) end.clone();
                        newDayEnd.set(Calendar.HOUR_OF_DAY, ((int)dayEnd.toEpochMilli() / 3600000) + 1);
                        newDayEnd.set(Calendar.MINUTE, (int)dayEnd.toEpochMilli() % 3600000 / 60000);

                        System.out.printf("\nnew start date: %s\nnew day end: %s\n", newDayStart.getTime(), newDayEnd.getTime());


                        LinkedList<Event> events = db.getEmployeesEventsInRange(eventAttendees,
                                editMeetingDate.getValue().toString(), e.getId());



                        events.forEach((v) -> {
                            System.out.printf("\nEvents: %s - %s", v.getStart().toString(), v.getEnd().toString());
                        });
                        LinkedList<Event> busy = new LinkedList<>();
                        LinkedList<Event> open = new LinkedList<>();

                        System.out.printf("\nEvents size: %d\n", events.size());

                        if(events.size()  == 0) {
                            open.add(new Event(newDayStart.toInstant().toString(), newDayEnd.toInstant().toString()));
                        } else {
                            Event ev = events.get(0);
                            for (int i = 1; i < events.size(); i++) {
                                int value = events.get(i).getStart().compareTo(ev.getEnd());

                                if (value <= 0) {
                                    ev.setEnd(events.get(i).getEnd());
                                } else {
                                    busy.add(ev);
                                    ev = events.get(i);
                                }
                            }
                            busy.add(ev);


                            for (int i = 0; i < busy.size(); i++) {
                                if(i == 0) {
                                    if(busy.get(i).getStart().getTime() - newDayStart.getTimeInMillis() >= newDuration) {
                                        open.add(new Event(newDayStart.toInstant().toString(), busy.get(i).getStart().toInstant().toString()));
                                    }
                                } else if(i == busy.size() - 1) {
                                    if(newDayEnd.getTimeInMillis() - busy.get(i).getEnd().getTime() >= newDuration) {
                                        open.add(new Event(busy.get(i).getEnd().toInstant().toString(), newDayEnd.toInstant().toString()));
                                    }
                                } else {
                                    if(busy.get(i).getStart().getTime() - busy.get(i-1).getEnd().getTime() >= newDuration) {
                                        open.add(new Event(busy.get(i-1).getEnd().toInstant().toString(),
                                                busy.get(i).getStart().toInstant().toString()));
                                    }
                                }
                            }
                        }
                        busy.forEach((v) -> {
                            System.out.printf("\nBusy: %s - %s", v.getStart().toString(), v.getEnd().toString());
                        });
//
                        open.forEach((v) -> {
                            System.out.printf("\nOpen: %s - %s", v.getStart().toString(), v.getEnd().toString());
                        });

                        boolean fits = false;
                        for (Event tmpE : open) {
                            System.out.printf("\nOpen slot start: %s\nOpen slot end: %s\n", tmpE.getStart().toString(), tmpE.getEnd().toString());;
                            System.out.printf("\nEvent slot start: %s\nEvent slot end: %s\n", start.getTime(), end.getTime());
                            if(tmpE.getStart().compareTo(start.getTime()) <= 0 &&
                                    tmpE.getEnd().compareTo(end.getTime()) >= 0) {
                                System.out.println("time slot fits");
                                fits = true;
                            } else {
                                System.out.println("time slot does not fit");
                            }
                        }

                        if(fits) {
                            System.out.println("you can schedule the meeting in the wanted time frame");
                            // TODO: 22/03/2018 add alert if tx fails
                            Event tmpEvent = new Event(e.getId(), editMeetingTitle.getText(),
                                    editMeetingDescription.getText(), start.getTime().toInstant().toString(),
                                    end.getTime().toInstant().toString(), e.getOrganizer().getId(),
                                    editMeetingLocation.getText(),
                                    editMeetingPriority.getSelectionModel().getSelectedIndex());
                                    closePane(editMeetingVBox);

                            if(db.updateEvent(tmpEvent, eventAttendees)) {
                                System.out.println("event updated");
                                displayMeetings();
                            }

//                            Event(int eid, String etitle, String edesc, String estart, String eend, int eorg, String eloc, int eprio) {

                        } else {
                            // TODO: 22/03/2018 inform user if meeting cannot be scheduled today
                            System.out.printf("%s", Instant.ofEpochMilli(newDuration).toString());
                            System.out.println("you cannot schedule the meeting in the wanted time frame");
                            ObservableList<String> list = FXCollections.observableArrayList();
                            editMeetingSuggestionAccordion.getPanes().clear();

                            if(open.size() == 0) {
                                TitledPane tp = new TitledPane();
                                tp.setText("The meeting cannot be scheduled today, please try another day");
                                editMeetingSuggestionAccordion.getPanes().add(tp);
                            } else {
                                open.forEach((v) -> {
                                    Instant upperBound = Instant.ofEpochMilli(v.getEnd().getTime() - newDuration);
                                    editMeetingSuggestionAccordion.getPanes().add(createTitledPane(v.getStart().toInstant(), upperBound));
                                    list.add(v.getStart().toString());
                                });
                                editMeetingSuggestionVBox.setDisable(false);
                                editMeetingSuggestionVBox.setOpacity(1.0);
                            }
                        }

                    } catch (ParseException e) {
                        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                    }
                }
            }
        });
    }


    private TitledPane createTitledPane(Instant start, Instant end) {
        TitledPane tp = new TitledPane();
        tp.getStylesheets().add(getClass().getResource("/css/main.css").toString());
        tp.setText(String.format("%s - %s", Date.from(start).toString(), Date.from(end).toString()));

        GridPane gridPane = new GridPane();

        Label hiddenTime = new Label(String.format("%d", start.toEpochMilli()));
        hiddenTime.setDisable(true);
        hiddenTime.setOpacity(0);
        hiddenTime.setMaxWidth(0);
        hiddenTime.setMaxHeight(0);


        VBox retainer = new VBox();
        Label label = new Label("Meeting date:");
        label.setMinWidth(125);
        label.setMinHeight(25);

        String css = "-fx-background-radius: 5; -fx-background-color: rgb(49, 68, 99);" +
                "-fx-text-fill: white; -fx-font: 15 Helvetica; -fx-font-style: bold; -fx-padding: 2, 2, 2, 2";
        label.setStyle(css);

        Label selectedDate = new Label(new SimpleDateFormat("yyyy-MM-dd").format(Date.from(start)));
        selectedDate.setStyle("-fx-font: 15 Helvetica; -fx-font-style: oblique");
        retainer.getChildren().addAll(label, selectedDate);

        retainer.setAlignment(Pos.CENTER);
        GridPane.setHgrow(retainer, Priority.ALWAYS);

        gridPane.add(retainer, 0, 0);

        retainer = new VBox();

        label = new Label("Meeting time: ");
        label.setMinWidth(125);
        label.setMinHeight(25);
        label.setStyle(css);

        Label selectedTime = new Label(new SimpleDateFormat("HH:mm").format(Date.from(end)));
        selectedTime.setStyle("-fx-font: 15 Helvetica; -fx-font-style: oblique");
        retainer.getChildren().addAll(label, selectedTime);

        retainer.setAlignment(Pos.CENTER);
        GridPane.setHgrow(retainer, Priority.ALWAYS);

        gridPane.add(retainer, 1, 0);

        Slider slider = new Slider();
        slider.setMin(start.toEpochMilli());
        slider.setMax(end.toEpochMilli());

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Date date = Date.from(Instant.ofEpochMilli(newValue.longValue()));
                hiddenTime.setText(String.format("%d",newValue.longValue()));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                selectedDate.setText(formatter.format(date));
                formatter = new SimpleDateFormat("HH:mm");
                selectedTime.setText(formatter.format(date));
            }
        });

        VBox vBox = new VBox();
        vBox.getChildren().add(slider);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.setAlignment(Pos.CENTER);
        GridPane.setHgrow(vBox, Priority.ALWAYS);
        vBox.setMinHeight(50);
        gridPane.add(vBox, 0, 1, 2, 1);

        JFXButton button = new JFXButton("Choose");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Long changedTime = Long.parseLong(hiddenTime.getText());
                editMeetingDate.setValue(Instant.ofEpochMilli(changedTime).atZone(ZoneId.systemDefault()).toLocalDate());
                editMeetingTime.setValue(Instant.ofEpochMilli(changedTime).atZone(ZoneId.systemDefault()).toLocalTime());
                closePane(editMeetingSuggestionVBox);
            }
        });
        button.setStyle("-fx-background-color: rgb(48, 68, 99);-fx-text-fill: white");
        vBox = new VBox(button);
        vBox.setAlignment(Pos.CENTER);
        gridPane.add(vBox, 0, 2, 2, 1);
        gridPane.add(hiddenTime, 0, 3);

        ColumnConstraints cConstraints = new ColumnConstraints();
        cConstraints.setMaxWidth(250);
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setValignment(VPos.CENTER);
        gridPane.getColumnConstraints().add(cConstraints);
        tp.setContent(gridPane);

        return tp;
    }

    private HBox createAttendeeHBox(Employee emp, LinkedList<Employee> eventAttendees) {
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
                eventAttendees.remove(emp);
                editMeetingAttendeesList.getChildren().remove(hBox);
            }
        });

        hBox.getChildren().add(btn);
        hBox.setAlignment(Pos.CENTER);
        hBox.setStyle("-fx-background-color: rgb(49, 68, 99);-fx-background-radius: 5;");
        FlowPane.setMargin(hBox, new Insets(0, 5, 5, 0));
        return hBox;
    }

    private boolean validateForSchedule(LinkedList<Employee> employees) {
        boolean valid = true;
        if(editMeetingTitle.getText().equals("")) {
            editMeetingTitleLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        if(editMeetingDate.getValue() == null) {
            editMeetingDateLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        if(editMeetingTime.getValue() == null) {
            editMeetingTimeLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        if(editMeetingDuration.getValue() == null) {
            editMeetingDurationLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        if(employees.size() == 0) {
            editMeetingAttendeesSearchLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        if(editMeetingLocation.getText().equals("")) {
            editMeetingLocationLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        if(editMeetingPriority.getValue() == null) {
            editMeetingPriorityLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        return valid;
    }

    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) {
            return true;
        }
        while (node != null) {
            if (node == potentialHierarchyElement) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    private void closePane(VBox box) {

        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(100));
        fadeTransition.setNode(box);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadeTransition.play();
        box.setDisable(true);
        box.setOpacity(0);
    }

    private void constructDetailedMeetingDialog(Event e) {
        meetingDetailsButtonsGrid.getChildren().clear();

        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(100));
        fadeTransition.setNode(meetingDetailsVBox);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();

        meetingDetailsVBox.setDisable(false);
        meetingDetailsVBox.setOpacity(1.0);
        meetingDetailsVBox.setStyle("-fx-background-color: rgb(255, 255, 255, 1)");
        meetingDetailsVBox.setMouseTransparent(false);

        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!inHierarchy(event.getPickResult().getIntersectedNode(), meetingDetailsVBox)) {
                    meetingsGrid.removeEventFilter(MouseEvent.MOUSE_CLICKED, this);
                    closePane(meetingDetailsVBox);
                }
            }
        };
        meetingsGrid.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);

        meetingDetailsSubject.setText(e.getTitle());
        meetingDetailsDate.setText(new SimpleDateFormat("YYY-MM-dd").format(e.getStart()));
        meetingDetailsStart.setText(new SimpleDateFormat("HH:mm").format(e.getStart()));
        meetingDetailsEnd.setText(new SimpleDateFormat("HH:mm").format(e.getEnd()));
        long duration = (e.getEnd().getTime() - e.getStart().getTime()) / 60000;
        meetingDetailsDuration.setText(String.format("%d minutes", duration));
        meetingDetailsLocation.setText(e.getLocation());
        String priority[] = {"Low", "Medium", "High"};
        meetingDetailsPriority.setText(priority[e.getPriority()]);
        meetingDetailsDescription.setText(e.getDesc());

        LinkedList<Employee> attendees = db.getEventAttendees(e.getId());
        StringJoiner joiner = new StringJoiner(", ");
        for(Employee emp : attendees) {
            joiner.add(emp.getName());
        }
        meetingDetailsAttendees.setText(joiner.toString());

        GridPane buttonGrid = new GridPane();
        Button okay = createButtonForDialog("Okay");

        okay.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                meetingsGrid.removeEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);
                closePane(meetingDetailsVBox);
            }
        });
        buttonGrid.add(okay, 0, 0);

        if(employeeID == e.getOrganizer().getId()) {
            Button edit = createButtonForDialog("Edit");

            edit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    meetingsGrid.removeEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);
                    closePane(meetingDetailsVBox);
                    constructEditEventDialog(e);
                    System.out.println("Editing");
                }
            });
            buttonGrid.add(edit, 1, 0);
        }
        GridPane.setHgrow(buttonGrid, Priority.ALWAYS);
        meetingDetailsButtonsGrid.add(buttonGrid, 0, 3);
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
        int inter = interval.get(Calendar.MINUTE) + interval.get(Calendar.HOUR_OF_DAY) * 60;
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        cal.set(Calendar.SECOND, 0);
        int startDate = cal.get(Calendar.DATE);

        System.out.println(inter);
        for(int i = 1; i <= (24-h)*60; i++) {
            Pane p = new Pane();
            p.setMinHeight(60/inter);
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

            cal.add(Calendar.MINUTE, inter);
            index += inter;
        }
        displayMeetings();
    }


    public void intervalAndTimeWatcher(ActionEvent action) {
//        System.out.println(scheduleStart.getValue());
//        System.out.println(scheduleEnd.getValue());
//        System.out.println(scheduleIntervals.getValue());
        Date i;
        if(scheduleIntervals.getValue().toString().contains("min")) {
            i = verifyTime(scheduleIntervals.getValue().toString(), "mm 'min'");
        } else {
            i = verifyTime(scheduleIntervals.getValue().toString(), "HH 'hour'");
        }
        Date t = verifyTime(scheduleStart.getValue().toString(), "HH:mm");
        System.out.println(i);

        if(t != null) {
            scheduleStart.setStyle(null);
            startTime.setTime(t);
        }

        if(i != null) {
            scheduleIntervals.setStyle(null);
            interval.setTime(i);
        }
        updateTimeInterval();
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
        events = db.getEmployeeEvents(employeeID);
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
