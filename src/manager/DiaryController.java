package manager;

import com.jfoenix.controls.*;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.converter.LocalTimeStringConverter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;


//public class DiaryController {
public class DiaryController  implements Initializable {
    @FXML public Label currentWeek;
    @FXML public JFXDatePicker datePicker;
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
    @FXML public JFXButton meetingDetailsDelete;
    @FXML public VBox meetingDetailsVBox;
    @FXML public GridPane meetingDetailsButtonsGrid;
    @FXML public VBox editMeetingSuggestionVBox;
    @FXML public Accordion editMeetingSuggestionAccordion;
    @FXML public JFXButton editMeetingSuggestionListCancel;
    @FXML public JFXTimePicker scheduleStart;
    @FXML public JFXTimePicker scheduleEnd;
    @FXML public JFXComboBox scheduleIntervals;
    @FXML public JFXButton createNewEventButton;
    @FXML public JFXButton eventListButton;
    @FXML public JFXButton taskListButton;
    @FXML public JFXButton eventListViewCloseButton;
    @FXML public VBox eventListViewVBox;
    @FXML public GridPane eventListViewListGrid;
    @FXML public JFXButton taskListViewAdd;
    @FXML public JFXButton taskListViewClose;
    @FXML public VBox taskListViewVBox;
    @FXML public GridPane taskListViewListGrid;
    @FXML public JFXButton editTaskDelete;
    @FXML public JFXTextArea editTaskDescription;
    @FXML public JFXComboBox editTaskPriority;
    @FXML public JFXButton editTaskCancel;
    @FXML public JFXButton editTaskUpdate;
    @FXML public VBox editTaskListVBox;
    @FXML public JFXButton signOut;
    @FXML public JFXButton searchPotentialClose;
    @FXML public JFXDatePicker searchPotentialStartDate;
    @FXML public JFXDatePicker searchPotentialEndDate;
    @FXML public JFXTextField searchPotentialAttendees;
    @FXML public FlowPane searchPotentialAttendeesList;
    @FXML public Accordion searchPotentialAccordion;
    @FXML public VBox searchPotentialVBox;
    @FXML public JFXButton searchPotentialDates;
    @FXML public Label searchPotentialStartDateLabel;
    @FXML public Label searchPotentialEndDateLabel;
    @FXML public Label searchPotentialAttendeesLabel;
    @FXML public JFXButton searchPotentialSearch;
    @FXML public Label searchPotentialDurationLabel;
    @FXML public JFXTimePicker searchPotentialDuration;
    @FXML public Label currentUserName;
    @FXML public Label searchPotentialTimeTaken;


    private Calendar startTime = Calendar.getInstance(Locale.getDefault());
    private Calendar interval = Calendar.getInstance(Locale.getDefault());
    private Calendar currentDate = Calendar.getInstance(Locale.getDefault());
    private Database db = new Database();
    // TODO: 24/03/2018 remove before deploying 
    private int currentEmployeeId = 1;
    private Employee currentUser;
    private LinkedList<Event> events;
    private LinkedList<Event> displayedEvents = new LinkedList<>();
    private LinkedList<StackPane> panes = new LinkedList<>();
    private LinkedList<Employee> eventAttendees = new LinkedList<>();
    private Node currentDayIndic;
    private int[] weekdayTranslate = {0, 7, 1, 2, 3, 4, 5, 6};

    private Calendar dayStart = Calendar.getInstance();
    private Calendar dayEnd = Calendar.getInstance();

//    public void initialize(URL location, ResourceBundle resources) {
//
//    }

    DiaryController(Employee currentUser) {
        this.currentUser = currentUser;
    }

    public void initialize(URL location, ResourceBundle resources) {

//    public void initialize() {
        db.setup();
//        currentUser = db.getEmployeeByID(currentEmployeeId);
        AnchorPane.setRightAnchor(rootDiaryPane, 0.0);
        AnchorPane.setBottomAnchor(rootDiaryPane, 0.0);
        AnchorPane.setLeftAnchor(rootDiaryPane, 0.0);
        AnchorPane.setTopAnchor(rootDiaryPane, 0.0);

        currentUserName.setText(currentUser.getName());

        scheduleStart.setIs24HourView(true);
        scheduleStart.setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm")));

        scheduleEnd.setIs24HourView(true);
        scheduleEnd.setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm")));
        rootDiaryPane.getStylesheets().add(getClass().getResource("/css/main.css").toString());
        editMeetingRootPane.getStylesheets().add(getClass().getResource("/css/editMeetings.css").toString());
        searchPotentialVBox.getStylesheets().add(getClass().getResource("/css/editMeetings.css").toString());


        scheduleIntervals.getItems().addAll(
                FXCollections.observableArrayList("15 min", "30 min", "1 hour"));
        scheduleIntervals.getSelectionModel().select(0);

        editTaskPriority.getItems().addAll(FXCollections.observableArrayList("Low", "Medium", "High"));
        editTaskPriority.getSelectionModel().select(0);

        scheduleStart.setValue(LocalTime.parse("08:00"));
        scheduleEnd.setValue(LocalTime.parse("20:00"));

        dayStart = setDayStartEndCalendar("8:00", dayStart);
        dayEnd = setDayStartEndCalendar("20:00", dayEnd);

        currentWeek.setText(String.format("WEEK %d", currentDate.get(Calendar.WEEK_OF_MONTH)));
        currentDayIndic = columnHeads.getChildren().get(weekdayTranslate[currentDate.get(Calendar.DAY_OF_WEEK)]);
        indicateCurrentDay();
        datePicker.setValue(LocalDate.now());


        startTime.set(Calendar.HOUR_OF_DAY, 8);
        startTime.set(Calendar.MINUTE, 0);
        interval.set(Calendar.MINUTE, 15);
        interval.set(Calendar.HOUR_OF_DAY, 0);

        signOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    javafx.concurrent.Task<Parent> loadTask = new javafx.concurrent.Task<Parent>() {
                        @Override
                        public Parent call() throws IOException {
                            return (StackPane) FXMLLoader.load(getClass().getResource("/fxml/startup.fxml"));
                        }
                    };
                    loadTask.setOnSucceeded(e -> {
                        rootDiaryPane.getChildren().setAll(loadTask.getValue());
                    });

                    loadTask.setOnFailed(e -> loadTask.getException().printStackTrace());

                    Thread thread = new Thread(loadTask);
                    thread.start();

                } catch (Exception e) {
                    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                }
            }
        });

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

        editMeetingCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                closePane(editMeetingVBox);
                if(!editMeetingSuggestionVBox.isDisabled()){
                    closePane(editMeetingSuggestionVBox);
                }
            }
        });

        searchPotentialStartDate.focusedProperty().addListener((o, oldVal, newVal) -> {
            if(!newVal) {
                if(searchPotentialStartDate.getValue() == null) {
                    searchPotentialStartDateLabel.setStyle("-fx-text-fill: red");
                } else {
                    searchPotentialStartDateLabel.setStyle(null);
                }
            }
        });


        searchPotentialEndDate.focusedProperty().addListener((o, oldVal, newVal) -> {
            if(!newVal) {
                if(searchPotentialEndDate.getValue() == null) {
                    searchPotentialEndDateLabel.setStyle("-fx-text-fill: red");
                } else {
                    searchPotentialEndDateLabel.setStyle(null);
                }
            }
        });

        searchPotentialDuration.setIs24HourView(true);
        searchPotentialDuration.setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm")));
        searchPotentialDuration.focusedProperty().addListener((o, oldVal, newVal) -> {
            if(!newVal) {
                if(searchPotentialDuration.getValue() == null) {
                    searchPotentialDurationLabel.setStyle("-fx-text-fill: red");
                } else {
                    searchPotentialDurationLabel.setStyle(null);
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
                dayStart = setDayStartEndCalendar(scheduleStart.getValue().toString(), dayStart);
                System.out.println(scheduleStart.getValue().toString());
            }
        });

        scheduleEnd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dayEnd = setDayStartEndCalendar(scheduleEnd.getValue().toString(), dayEnd);
            }
        });

        createNewEventButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(editMeetingVBox.isDisabled()) {
                    Event e = new Event();
                    e.setId(0);
                    e.setOrganizer(currentUser);
                    constructEditEventDialog(e);
                } else {
                    closePane(editMeetingVBox);
                    closePane(editMeetingSuggestionVBox);
                }
            }
        });

        eventListButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(eventListViewVBox.isDisabled()){
                    constructEventListView();
                } else {
                    closePane(eventListViewVBox);
                }
            }
        });

        eventListViewCloseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                closePane(eventListViewVBox);
            }
        });

        taskListButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(taskListViewVBox.isDisabled()) {
                    constructTaskListView();
                } else {
                    closePane(taskListViewVBox);
                }
            }
        });

        taskListViewClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                closePane(taskListViewVBox);
            }
        });

        taskListViewAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(editTaskListVBox.isDisabled()) {
                    constructEditTaskView(new Task("", 0));
                }
            }
        });

        editTaskCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                closePane(editTaskListVBox);
            }
        });

        searchPotentialDates.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(searchPotentialVBox.isDisabled()) {
                    constructSearchPane();
                } else {
                    closePane(searchPotentialVBox);
                }
            }
        });

        searchPotentialClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                closePane(searchPotentialVBox);
            }
        });

        updateTimeInterval();
        displayMeetings();
    }

    private Calendar setDayStartEndCalendar(String t, Calendar c) {
        String time[] = t.split(":");
        int h = Integer.parseInt(time[0]);
        int m = Integer.parseInt(time[1]);

        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        c.set(Calendar.SECOND, 0);

        return c;
    }

    private void resetSearchPane() {
        searchPotentialStartDate.setValue(null);
        searchPotentialEndDate.setValue(null);
        searchPotentialDuration.setValue(null);
        searchPotentialAttendees.clear();
        searchPotentialAttendeesList.getChildren().clear();
        searchPotentialAccordion.getPanes().clear();

        searchPotentialStartDateLabel.setStyle(null);
        searchPotentialEndDateLabel.setStyle(null);
        searchPotentialDurationLabel.setStyle(null);
        searchPotentialAttendeesLabel.setStyle(null);
        searchPotentialTimeTaken.setText("");
    }

    private void constructSearchPane() {
        resetSearchPane();
        showPane(searchPotentialVBox);
        eventAttendees.clear();

        LinkedList<Employee> employees = db.getEmployees();
        ArrayList<String> tmp = new ArrayList<>();
        for (Employee tmpEmp : employees) {
            StringJoiner joiner = new StringJoiner(", ");
            joiner.add(tmpEmp.getName());
            joiner.add(String.format("%s", tmpEmp.getEmail()));
            tmp.add(joiner.toString());
        }

        TextFields.bindAutoCompletion(searchPotentialAttendees, tmp);


        searchPotentialAttendees.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER) {
                    searchPotentialAttendeesLabel.setStyle(null);
                    String user[] = searchPotentialAttendees.getText().split(", ");
                    boolean contains = false;
                    for(Employee emp : eventAttendees) {
                        if(user.length > 0) {
                            if(emp.getEmail().equals(user[1])) {
                                contains = true;
                                searchPotentialAttendeesLabel.setStyle("-fx-text-fill: red");
                                break;
                            }
                        }
                    }
                    if(!contains) {
                        for(Employee emp : employees) {
                            if(user.length > 0) {
                                if (emp.getEmail().equals(user[1])) {
                                    searchPotentialAttendeesLabel.setStyle(null);
                                    eventAttendees.add(emp);
                                    searchPotentialAttendeesList.getChildren().add(createAttendeeHBox(emp, eventAttendees));
                                    searchPotentialAttendees.setText("");
                                }
                            }
                        }
                    }
                }
                else {
                    searchPotentialAttendeesLabel.setStyle(null);
                }
            }
        });

        searchPotentialSearch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(validateForSearch(eventAttendees)) {
                    try {
                        long startSearchTime = System.nanoTime();
                        System.out.println("Validation passed");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        Calendar start = Calendar.getInstance();
                        start.setTime(sdf.parse(searchPotentialStartDate.getValue().toString()));

                        Calendar end = Calendar.getInstance();
                        end.setTime(sdf.parse(searchPotentialEndDate.getValue().toString()));

                        String time[] = searchPotentialDuration.getValue().toString().split(":");
                        long duration = (Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1])) * 60000;


                        LinkedList<Event> allAvailableSlots = new LinkedList<>();
                        for(; start.getTime().compareTo(end.getTime()) <= 0;
                            start.add(Calendar.DAY_OF_YEAR, 1)) {

                            LinkedList<Event> open = getOpenSlots(start, duration, 0);
                            allAvailableSlots.addAll(open);
                        }
                        long timeTaken = (System.nanoTime() - startSearchTime) / 1000000;
//                        ObservableList<String> list = FXCollections.observableArrayList();
                        searchPotentialAccordion.getPanes().clear();

                        if(allAvailableSlots.size() == 0) {
                            TitledPane tp = new TitledPane();
                            tp.setText("The meeting cannot be scheduled today, please try another day");
                            searchPotentialAccordion.getPanes().add(tp);
                        } else {
                            allAvailableSlots.forEach((v) -> {
                                Instant upperBound = Instant.ofEpochMilli(v.getEnd().getTime() - duration);
                                searchPotentialAccordion.getPanes().add(createTitledPane(v.getStart().toInstant(), upperBound));
//                                list.add(v.getStart().toString());
                            });
//                            editMeetingSuggestionVBox.setDisable(false);
//                            editMeetingSuggestionVBox.setOpacity(1.0);
                        }
                        allAvailableSlots.forEach((v) -> {
                            System.out.printf("\nOpen: %s - %s", v.getStart(), v.getEnd());


                        });

                        searchPotentialTimeTaken.setText(String.format("%d slots in %d ms",
                                searchPotentialAccordion.getPanes().size(), timeTaken));

                    } catch (Exception e) {
                        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                    }
                }
            }
        });
    }

    private boolean validateForSearch(LinkedList<Employee> employees) {
        boolean valid = true;
        if(searchPotentialStartDate.getValue() == null) {
            searchPotentialStartDateLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        if(searchPotentialEndDate.getValue() == null) {
            searchPotentialEndDateLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        if(searchPotentialDuration.getValue() == null) {
            searchPotentialDurationLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        if(employees.size() == 0) {
            searchPotentialAttendeesLabel.setStyle("-fx-text-fill: red");
            valid = false;
        }

        return valid;
    }

    private void constructEditTaskView(Task t) {
        showPane(editTaskListVBox);
        editTaskDelete.setDisable(true);
        editTaskDelete.setOpacity(0);

        editTaskDescription.clear();
        editTaskPriority.getSelectionModel().select(0);
        if(t.getId() > 0) {
            editTaskDescription.setText(t.getDescription());
            editTaskPriority.getSelectionModel().select(t.getPriority());
            editTaskDelete.setDisable(false);
            editTaskDelete.setOpacity(1);
        }

        editTaskUpdate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                int priority = editTaskPriority.getSelectionModel().getSelectedIndex();
                if(t.getId() > 0) {
                    Task task = new Task(t.getId(), editTaskDescription.getText(), priority);
                    if(!db.updateTask(task)) {
                        System.out.println("Failed to update task");
                    }
                } else {
                    Task task = new Task(editTaskDescription.getText(), priority);
                    if(!db.createTask(currentUser.getId(), task)) {
                        System.out.println("Failed to update task");
                    }
                }
                closePane(editTaskListVBox);
                populateTaskListView();
            }
        });

        editTaskDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!db.deleteTask(t.getId())) {
                    System.out.println("Failed to delete task");
                }
                populateTaskListView();
                closePane(editTaskListVBox);
            }
        });
    }

    private void constructTaskListView() {
        showPane(taskListViewVBox);
        populateTaskListView();
    }

    private void populateTaskListView() {
        taskListViewListGrid.getChildren().clear();
        LinkedList<Task> tasks = db.getEmployeeTasks(currentUser.getId());
        System.out.printf("Tasks: %d", tasks.size());

        for(Task t : tasks) {
           insertNewMeeting(t);
        }
    }

    private void insertNewMeeting(Task t) {
        Integer rows = 0;
        try {
            Method method = taskListViewListGrid.getClass().getDeclaredMethod("getNumberOfRows");
            method.setAccessible(true);
            rows = (Integer) method.invoke(taskListViewListGrid);
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }

        GridPane gridPane = new GridPane();

        HBox hBox = createTaskListViewColumn(t.getDescription());
        hBox.setMinWidth(900);
        hBox.setMaxWidth(900);

        gridPane.add(hBox, 0, 0);

        String priorities[] = {"Low", "Medium", "High"};

        hBox = createTaskListViewColumn(priorities[t.getPriority()]);
        hBox.setMinWidth(100);
        hBox.setMaxWidth(100);
        hBox.setAlignment(Pos.CENTER);
        gridPane.add(hBox, 1, 0);

        VBox vBox = new VBox(gridPane);

        String colors[] = getPriorityColours(t.getPriority());

        vBox.setStyle(colors[0]);

        vBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(editTaskListVBox.isDisabled()) {
                    constructEditTaskView(t);
                }
            }
        });

        vBox.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                vBox.setStyle("-fx-border-color: blue"+colors[0]);
            }
        });

        vBox.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                vBox.setStyle(colors[0]);
            }
        });


        taskListViewListGrid.add(vBox, 0, rows, 3, 1);
    }

    private HBox createTaskListViewColumn(String data) {
        Label label = new Label(data);
        HBox.setMargin(label, new Insets(0, 10, 0, 10));
        HBox hBox = new HBox(label);
        HBox.setMargin(hBox, new Insets(0, 10, 0, 10));
        hBox.setMinHeight(25);

        return hBox;
    }

    private void constructEventListView() {
        showPane(eventListViewVBox);

        populateEventListView();
    }

    private void populateEventListView() {
        eventListViewListGrid.getChildren().clear();
        String priority[] = {"Low", "Medium", "High"};

        for(int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            GridPane gridPane = new GridPane();
            VBox vbox = new VBox(gridPane);

            gridPane.add(createListViewColumn(e.getTitle()), 0, i);
            gridPane.add(createListViewColumn(e.getDesc()), 1, i);
            gridPane.add(createListViewColumn(e.getStart().toString()), 2, i);
            gridPane.add(createListViewColumn(e.getEnd().toString()), 3, i);
            gridPane.add(createListViewColumn(e.getOrganizer().getName()), 4, i);
            gridPane.add(createListViewColumn(e.getLocation()), 5, i);
            gridPane.add(createListViewColumn(priority[e.getPriority()]), 6, i);

            String colors[] = getPriorityColours(e.getPriority());

            vbox.setStyle(colors[0]);
            vbox.setMinHeight(25);

            vbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if(meetingDetailsVBox.isDisabled()) {
                        constructDetailedMeetingDialog(e);
                    }
                }
            });

            vbox.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    vbox.setStyle("-fx-border-color: blue"+colors[0]);
                }
            });

            vbox.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    vbox.setStyle(colors[0]);
                }
            });

            eventListViewListGrid.add(vbox, 0, i, 8, 1);
        }
        RowConstraints rc = new RowConstraints();
        rc.setValignment(VPos.CENTER);
        eventListViewListGrid.getRowConstraints().add(rc);
    }

    private HBox createListViewColumn(String data) {
        Label label = new Label(data);
        HBox.setMargin(label, new Insets(0, 10, 0, 10));
        HBox hbox = new HBox(label);
        hbox.setMinWidth(143);
        hbox.setMaxWidth(143);
        hbox.setAlignment(Pos.CENTER);

        return hbox;
    }

    private void clearEditEventDialogFields(){
        editMeetingTitle.clear();
        editMeetingDate.setValue(null);
        editMeetingLocation.clear();
        editMeetingTime.setValue(null);
        editMeetingDuration.setValue(null);
        editMeetingDescription.clear();
        editMeetingAttendeesSearch.clear();
        editMeetingAttendeesList.getChildren().clear();
        editMeetingPriority.getSelectionModel().select(0);

        editMeetingTitleLabel.setStyle(null);
        editMeetingDateLabel.setStyle(null);
        editMeetingLocationLabel.setStyle(null);
        editMeetingTimeLabel.setStyle(null);
        editMeetingDurationLabel.setStyle(null);
        editMeetingPriorityLabel.setStyle(null);
        editMeetingAttendeesSearchLabel.setStyle(null);
    }

    private void constructEditEventDialog(Event e) {
        showPane(editMeetingVBox);

//        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                if(!inHierarchy(event.getPickResult().getIntersectedNode(), editMeetingVBox) &&
//                        !editMeetingVBox.isDisabled()) {
//                    meetingsGrid.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
//                    closePane(editMeetingVBox);
//                }
//            }
//        };
//        meetingsGrid.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);

        LinkedList<Employee> employees = db.getEmployees();

        if (e.getId() > 0) {
            clearEditEventDialogFields();
            editMeetingTitle.setText(e.getTitle());
            editMeetingDate.setValue(e.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            editMeetingLocation.setText(e.getLocation());
            editMeetingTime.setValue(LocalDateTime.ofInstant(
                    e.getStart().toInstant(), ZoneId.systemDefault()).toLocalTime());

            long duration = (e.getEnd().getTime() - e.getStart().getTime());
            editMeetingDuration.setValue(LocalTime.from(Instant.ofEpochMilli(duration).atZone(ZoneId.of("UTC"))));

            editMeetingPriority.getSelectionModel().select(e.getPriority());

            editMeetingDescription.setText(e.getDesc());

            eventAttendees = db.getEventAttendees(e.getId());

        } else if(e.getId() == 0){
            clearEditEventDialogFields();
            eventAttendees.clear();
            eventAttendees.add(currentUser);
        } else {
            boolean isInTheList = false;
            for(Employee tmp : eventAttendees) {
                if(tmp.getEmail().equals(currentUser.getEmail())) {
                    isInTheList = true;
                    break;
                }
            }
            if(!isInTheList) {
                System.out.println("user is not in the list");
                eventAttendees.add(currentUser);
            }
        }



        ArrayList<String> tmp = new ArrayList<>();
        for (Employee tmpEmp : employees) {
            StringJoiner joiner = new StringJoiner(", ");
            joiner.add(tmpEmp.getName());
            joiner.add(String.format("%s", tmpEmp.getEmail()));
            tmp.add(joiner.toString());
        }

        TextFields.bindAutoCompletion(editMeetingAttendeesSearch, tmp);

        editMeetingAttendeesSearch.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER) {
                    editMeetingAttendeesSearchLabel.setStyle(null);
                    String user[] = editMeetingAttendeesSearch.getText().split(", ");
                    boolean contains = false;
                    for(Employee emp : eventAttendees) {
                        if(user.length > 0){
                            if(emp.getEmail().equals(user[1])) {
                                contains = true;
                                editMeetingAttendeesSearchLabel.setStyle("-fx-text-fill: red");
                                break;
                            }
                        }
                    }
                    if(!contains) {
                        for(Employee emp : employees) {
                            if(user.length > 0) {
                                if (emp.getEmail().equals(user[1])) {
                                    editMeetingAttendeesSearchLabel.setStyle(null);
                                    eventAttendees.add(emp);
                                    editMeetingAttendeesList.getChildren().add(createAttendeeHBox(emp, eventAttendees));
                                    editMeetingAttendeesSearch.setText("");
                                }
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

//        EventHandler<ActionEvent> cancelEvent = new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                editMeetingCancel.setOnAction(null);
//                closePane(editMeetingVBox);
//                closePane(editMeetingSuggestionVBox);
//            }
//        };
//        editMeetingCancel.setOnAction(cancelEvent);

        editMeetingSchedule.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(validateForSchedule(eventAttendees)) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        Calendar start = Calendar.getInstance();
                        start.setTime(sdf.parse(editMeetingDate.getValue().toString()));

                        String time[] = editMeetingDuration.getValue().toString().split(":");
                        int h = Integer.parseInt(time[0]);
                        int m = Integer.parseInt(time[1]);

                        long newDuration = (h * 60 + m) * 60000;

//                        Calendar end = (Calendar) start.clone();
//                        end.add(Calendar.HOUR_OF_DAY, h);
//                        end.add(Calendar.MINUTE, m);

                        Calendar eventStart = Calendar.getInstance();
                        eventStart.setTime(sdf.parse(editMeetingDate.getValue().toString()));
                        time = editMeetingTime.getValue().toString().split(":");
                        h = Integer.parseInt(time[0]);
                        m = Integer.parseInt(time[1]);
                        eventStart.set(Calendar.HOUR_OF_DAY, h);
                        eventStart.set(Calendar.MINUTE, m);

                        Calendar eventEnd = (Calendar) eventStart.clone();
                        time = editMeetingDuration.getValue().toString().split(":");
                        h = Integer.parseInt(time[0]);
                        m = Integer.parseInt(time[1]);
                        eventEnd.add(Calendar.HOUR_OF_DAY, h);
                        eventEnd.add(Calendar.MINUTE, m);

                        LinkedList<Event> open = getOpenSlots(start, newDuration, e.getId());

                        boolean fits = false;
                        for (Event tmpE : open) {
                            System.out.printf("\nOpen slot start: %s\nOpen slot end: %s\n", tmpE.getStart().toString(), tmpE.getEnd().toString());;
                            System.out.printf("\nEvent slot start: %s\nEvent slot end: %s\n", eventStart.getTime(), eventEnd.getTime());
                            if(tmpE.getStart().compareTo(eventStart.getTime()) <= 0 &&
                                    tmpE.getEnd().compareTo(eventEnd.getTime()) >= 0) {
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
                                    editMeetingDescription.getText(), eventStart.getTime().toInstant().toString(),
                                    eventEnd.getTime().toInstant().toString(), e.getOrganizer().getId(),
                                    editMeetingLocation.getText(),
                                    editMeetingPriority.getSelectionModel().getSelectedIndex());
//                            }

                            if(tmpEvent.getId() <= 0) {
                                if(db.createEvent(tmpEvent, eventAttendees)){
                                    System.out.println("event created");
                                    closePane(editMeetingVBox);
                                    displayMeetings();
                                    populateEventListView();
                                }
                            } else {
                                if(db.updateEvent(tmpEvent, eventAttendees)) {
                                    System.out.println("event updated");
                                    closePane(editMeetingVBox);
                                    displayMeetings();
                                    populateEventListView();
                                } else {
                                    System.out.println("Failed to update meeting");
                                }
                            }
                        } else {
                            System.out.println("you cannot schedule the meeting in the wanted time frame");
//                            ObservableList<String> list = FXCollections.observableArrayList();
                            editMeetingSuggestionAccordion.getPanes().clear();

                            if(open.size() == 0) {
                                TitledPane tp = new TitledPane();
                                tp.setText("The meeting cannot be scheduled today, please try another day");
                                editMeetingSuggestionAccordion.getPanes().add(tp);
                            } else {
                                open.forEach((v) -> {
                                    Instant upperBound = Instant.ofEpochMilli(v.getEnd().getTime() - newDuration);
                                    editMeetingSuggestionAccordion.getPanes().add(createTitledPane(v.getStart().toInstant(), upperBound));
//                                    list.add(v.getStart().toString());
                                });
                                showPane(editMeetingSuggestionVBox);
//                                editMeetingSuggestionVBox.setDisable(false);
//                                editMeetingSuggestionVBox.setOpacity(1.0);
                            }
                        }

                    } catch (ParseException e) {
                        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                    }
                }
            }
        });
    }

    private void populateEditMeetingAttendeeList() {
        editMeetingAttendeesList.getChildren().clear();
        for(Employee emp : eventAttendees) {
            editMeetingAttendeesList.getChildren().add(createAttendeeHBox(emp, eventAttendees));
        }
    }

    private LinkedList<Event> getOpenSlots(Calendar day, long duration, int id) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDay = sdf.format(day.getTime());

        LinkedList<Event> matchedEvents = db.getEmployeesEventsInRange(eventAttendees,
                currentDay, id);

        System.out.printf("\nEvents size: %d\n", matchedEvents.size());

        Calendar start = (Calendar) day.clone();
        Calendar end = (Calendar) day.clone();

        start.set(Calendar.HOUR_OF_DAY, dayStart.get(Calendar.HOUR_OF_DAY));
        start.set(Calendar.MINUTE, dayStart.get(Calendar.MINUTE));

        end.set(Calendar.HOUR_OF_DAY, dayEnd.get(Calendar.HOUR_OF_DAY));
        end.set(Calendar.MINUTE, dayEnd.get(Calendar.MINUTE));

        LinkedList<Event> busy = new LinkedList<>();
        LinkedList<Event> open = new LinkedList<>();

        if(matchedEvents.size()  == 0) {
            open.add(new Event(start.toInstant().toString(), end.toInstant().toString()));
        } else {
            Event ev = matchedEvents.get(0);
            for (int i = 1; i < matchedEvents.size(); i++) {
                int value = matchedEvents.get(i).getStart().compareTo(ev.getEnd());

                if (value <= 0) {
                    ev.setEnd(matchedEvents.get(i).getEnd());
                } else {
                    busy.add(ev);
                    ev = matchedEvents.get(i);
                }
            }
            busy.add(ev);

            int bs = busy.size();


            for (int i = 0; i < bs; i++) {
                if(i == 0) {
                    if(busy.get(i).getStart().getTime() - start.getTimeInMillis() >= duration) {
                        open.add(new Event(start.toInstant().toString(), busy.get(i).getStart().toInstant().toString()));
                    }
                    if(bs == 1) {
                        if(end.getTimeInMillis() - busy.get(i).getEnd().getTime() >= duration) {
                            open.add(new Event(busy.get(i).getEnd().toInstant().toString(), end.toInstant().toString()));
                        }
                    }
                } else if(i == bs - 1) {
                    if(end.getTimeInMillis() - busy.get(i).getEnd().getTime() >= duration) {
                        open.add(new Event(busy.get(i).getEnd().toInstant().toString(), end.toInstant().toString()));
                    }
                } else {
                    if(busy.get(i).getStart().getTime() - busy.get(i-1).getEnd().getTime() >= duration) {
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
        return open;
    }

    private TitledPane createTitledPane(Instant start, Instant end) {
        TitledPane tp = new TitledPane();
        tp.getStylesheets().add(getClass().getResource("/css/main.css").toString());

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, HH:mm");
        String dateFrom = sdf.format(Date.from(start));
        sdf = new SimpleDateFormat("HH:mm");
        String dateTo = sdf.format(Date.from(end));

        tp.setText(String.format("%s - %s", dateFrom, dateTo));

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
                if(editMeetingVBox.isDisabled() && !searchPotentialVBox.isDisabled()) {
                    clearEditEventDialogFields();
//                    populateEditMeetingAttendeeList();
                    editMeetingDuration.setValue(searchPotentialDuration.getValue());
                    closePane(searchPotentialVBox);
//                    showPane(editMeetingVBox);
                    Event e = new Event();
                    e.setId(-1);
                    e.setOrganizer(currentUser);
                    constructEditEventDialog(e);
                } else {
                    closePane(editMeetingSuggestionVBox);
                }
                Long changedTime = Long.parseLong(hiddenTime.getText());
                editMeetingDate.setValue(Instant.ofEpochMilli(changedTime).atZone(ZoneId.systemDefault()).toLocalDate());
                editMeetingTime.setValue(Instant.ofEpochMilli(changedTime).atZone(ZoneId.systemDefault()).toLocalTime());
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
        box.setDisable(true);
        box.setOpacity(0);
//        box.toBack();
    }

    private void showPane(VBox box) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(100));
        fadeTransition.setNode(box);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
        box.setDisable(false);
        box.setOpacity(1.0);
        box.toFront();
    }

    private void constructDetailedMeetingDialog(Event e) {
        meetingDetailsButtonsGrid.getChildren().clear();
        meetingDetailsDelete.setDisable(true);
        meetingDetailsDelete.setOpacity(0);

        showPane(meetingDetailsVBox);

        meetingDetailsVBox.setStyle("-fx-background-color: rgb(255, 255, 255, 1)");

//        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                if(!inHierarchy(event.getPickResult().getIntersectedNode(), meetingDetailsVBox)) {
//                    meetingsGrid.removeEventFilter(MouseEvent.MOUSE_CLICKED, this);
//                    closePane(meetingDetailsVBox);
//                }
//            }
//        };
//        meetingsGrid.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);

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
                closePane(meetingDetailsVBox);
            }
        });
        buttonGrid.add(okay, 0, 0);

        meetingDetailsDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(currentUser.getId() == e.getOrganizer().getId()) {
                    System.out.println("deleting meeting");
                    if(db.deleteEvent(e.getId(), 0)) {
                        closePane(meetingDetailsVBox);
                        displayMeetings();
                        populateEventListView();
                    }
                } else {
                    if(e.getStart().compareTo(currentDate.getTime()) < 0) {
                        System.out.println("deleting meeting");
                        if(db.deleteEvent(e.getId(), currentUser.getId())) {
                            closePane(meetingDetailsVBox);
                            displayMeetings();
                            populateEventListView();
                        }
                    }
                }
            }
        });

        if(currentUser.getId() == e.getOrganizer().getId()) {
            Button edit = createButtonForDialog("Edit");

            edit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
//                    meetingsGrid.removeEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);
                    closePane(meetingDetailsVBox);
                    constructEditEventDialog(e);
                    System.out.println("Editing");
                }
            });
            buttonGrid.add(edit, 1, 0);
        }
        GridPane.setHgrow(buttonGrid, Priority.ALWAYS);
        meetingDetailsButtonsGrid.add(buttonGrid, 0, 3);

        if(currentUser.getId() == e.getOrganizer().getId() || e.getStart().compareTo(currentDate.getTime()) < 0) {
            meetingDetailsDelete.setDisable(false);
            meetingDetailsDelete.setOpacity(1.0);
        }
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
        events = db.getEmployeeEvents(currentUser.getId());
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


        String colors[] = getPriorityColours(event.getPriority());


        time.setStyle(colors[1]);
        pane.setStyle(colors[0]);

        pane.getChildren().addAll(grid);

        return pane;
    }

    private String[] getPriorityColours(int priority) {
        String backAndBorderCol = "";
        String timeColor = "";
        switch (priority) {
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

        return new String[]{backAndBorderCol, timeColor};
    }

    private int[] getMeetingPosition(Calendar start, Calendar end) {
        int minutes = (((end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE)) -
                (start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE))));

        int startMinute = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE) -
                startTime.get(Calendar.HOUR_OF_DAY) * 60 - startTime.get(Calendar.MINUTE);

        System.out.printf("DAY OF THE WEEK: %d", Calendar.DAY_OF_WEEK);

        int dayColumn = weekdayTranslate[start.get(Calendar.DAY_OF_WEEK)];
        return new int[]{dayColumn, startMinute, minutes};
    }

    private void clearMeetings() {
        for(Pane p : panes) {
            meetingsGrid.getChildren().remove(p);
        }
        panes.clear();
        displayedEvents.clear();
    }

    private void indicateCurrentDay() {
        currentDayIndic.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: black");
        currentDayIndic = columnHeads.getChildren().get(weekdayTranslate[currentDate.get(Calendar.DAY_OF_WEEK)]);
        currentDayIndic.setStyle("-fx-border-width: 0 0 3 0; -fx-border-color: green");
    }
}
