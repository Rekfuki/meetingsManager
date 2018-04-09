package manager;

import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.LinkedList;
import java.util.StringJoiner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Stores database setup method and all of the relevant queries
 */
class Database {
    /**
     * file handler for the logger file
     */
    private FileHandler fh = null;
    /**
     * database connection
     */
    private Connection c;

    /**
     * logger to log what happens into a log file, in case something goes wrong
     */
    private final Logger logger = Logger.getLogger(Database.class.getName());
    /**
     * formatter for logging
     */
    private SimpleFormatter formatter = new SimpleFormatter();
    /**
     * Setups logger and creates a new connection.
     * Connect and disconnect is performed inorder to cache first connection and prevent ui lockout
     */
    Database() {
        //Looks stupid, however when first connection happens there is a slight delay and the ui lags.
        //Therefore performing initial connection when initializing allows to avoid the lag.
        connect();
        disconnect();
    }

    /**
     * Sets up the database at every runtime
     */
    public void setup() {
        if(c == null) {
            return;
        }
        createTables();
    }

    /**
     * Creates all the tables and triggers if they do not exist
     */
    private void createTables() {
        Statement stmt;
        connect();
        try {
            stmt = c.createStatement();
            String sql = "create table if not exists employees " +
                    "(id integer not null primary key autoincrement," +
                    "name text not null," +
                    "login text not null," +
                    "paswd text not null)";
            stmt.executeUpdate(sql);

            sql = "create table if not exists  events " +
                    "(id integer not null primary key autoincrement," +
                    "title text not null," +
                    "description text," +
                    "start text not null," +
                    "end text not null," +
                    "organizer int not null default 0," +
                    "priority int not null default 0," +
                    "location text not null default '')";
            stmt.executeUpdate(sql);

            sql = "create table if not exists tasks " +
                    "(id integer not null primary key autoincrement," +
                    "priority text not null," +
                    "description text not null, " +
                    "employee_id int not null)";
            stmt.executeUpdate(sql);

            sql = "create table if not exists  employees_events " +
                    "(employee_id integer not null," +
                    "event_id integer not null," +
                    "foreign key(employee_id) references employees(id)," +
                    "foreign key(event_id) references events(id))";
            stmt.executeUpdate(sql);

            sql = "create table if not exists  undolog( " +
                    "tid integer not null default 0, " +
                    "sql text default '')";
            stmt.executeUpdate(sql);

            sql = "create trigger if not exists  employees_event_delete before delete on employees_events " +
                    "begin " +
                    "insert into undolog values(0, 'insert into employees_events(employee_id, event_id) " +
                    "values('||old.employee_id||','||old.event_id||')'); " +
                    "end;";
            stmt.executeUpdate(sql);

            sql = "create trigger if not exists  employees_event_insert after insert on employees_events " +
                    "begin " +
                    "insert into undolog values(0, 'delete from employees_events " +
                    "where employee_id='||new.employee_id||' and event_id = '||new.event_id||''); " +
                    "end;";
            stmt.executeUpdate(sql);

            sql = "create trigger if not exists  events_delete before delete on events " +
                    "begin " +
                    "insert into undolog values(0, 'insert into events" +
                    "(id, title, description, start, end, organizer, priority, location) " +
                    "values('||old.id||','||quote(old.title)||', '||quote(old.description)||', " +
                    "'||quote(old.start)||', '||quote(old.end)||', '||old.organizer||', '||old.priority||', " +
                    "'||quote(old.location)||')'); " +
                    "end;";
            stmt.executeUpdate(sql);

            sql = "create trigger if not exists events_insert after insert on events " +
                    "begin " +
                    "  insert into undolog values(0, 'delete from events where id='||new.id||''); " +
                    "end;";
            stmt.executeUpdate(sql);

            sql = "create trigger if not exists events_update after update on events " +
                    "begin " +
                    "insert into undolog values(0, 'update events set title='||quote(old.title)||', " +
                    "description='||quote(old.description)||', start='||quote(old.start)||', end='||quote(old.end)||'," +
                    " organizer='||old.organizer||', priority='||old.priority||'," +
                    " location='||quote(old.location)||' where id='||old.id||''); " +
                    "end;";

            stmt.executeUpdate(sql);

            sql = "create trigger if not exists tasks_insert after insert on tasks " +
                    "begin " +
                    "  insert into undolog values(0, 'delete from tasks where id='||new.id||''); " +
                    "end;";
            stmt.executeUpdate(sql);

            sql = "create trigger if not exists tasks_delete before delete on tasks " +
                    "begin " +
                    "  insert into undolog values(0, 'insert into tasks(id, description, priority, employee_id) " +
                    "values('||old.id||', '||quote(old.description)||', '||old.priority||', '||old.employee_id||')'); " +
                    "end;";
            stmt.executeUpdate(sql);

            sql = "create trigger if not exists tasks_update after update on tasks " +
                    "begin " +
                    "  insert into undolog values(0, 'update tasks set description='||quote(old.description)||'," +
                    " priority='||old.priority||' where id='||old.id||''); " +
                    "end;";
            stmt.executeUpdate(sql);

            stmt.close();

            disconnect();

        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );

            disconnect();
        }
    }

    /**
     * connects to the database
     */
    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true); //foreign keys are enforced because of linked tables
            c = DriverManager.getConnection("jdbc:sqlite:meetings.db", config.toProperties());
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    /**
     * disconnects from the database
     */
    private void disconnect() {
        try {
            c.close();
        } catch (Exception e ){
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    /**
     * Gets employee id from the database
     * @param usrnm employee's username
     * @param pswd employee's password
     * @return employee id
     */
    public int getEmployeeID(String usrnm, String pswd) {
        connect();
        try {
            PreparedStatement stmt =  c.prepareStatement("select id from employees where login=? and paswd=?");
            stmt.setString(1, usrnm);
            stmt.setString(2, pswd);
            ResultSet rs = stmt.executeQuery();

            logToFile("TEST");
            if(rs.next()) {
                int id = rs.getInt("id");
                disconnect();
                return id;
            }
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return 0;
    }

    /**
     * Gets employee information from the database;
     * @param id employee's id
     * @return Employee object with information if employee exists, otherwise null
     */
    public Employee getEmployeeByID(int id) {
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("select id, name, login from employees where id=?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                Employee employee = new Employee(
                        rs.getString("name"),
                        rs.getInt("id"),
                        rs.getString("login"));
                disconnect();
                return employee;
            }

        } catch (Exception e){
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return null;
    }

    /**
     * Checks if the username exists in the database
     * @param username employee's username
     * @return whether the employee exists
     */
    public boolean checkUsername(String username) {
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("select id from employees where login=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if(!rs.next()) {
                disconnect();
                return true;
            }
        } catch (Exception e){
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return false;
    }

    /**
     * Adds a new employee entry to the database.
     * @param name employee's full name
     * @param username employee's username
     * @param password employee's password
     * @return whether the entry was successful
     */
    public boolean addEmployee(String name, String username, String password) {
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("insert into employees(name, login, paswd) values(?,?,?)");
            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, password);
            if(stmt.executeUpdate() > 0) {
                disconnect();

                return true;
            }

        } catch (Exception e){
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return false;
    }

    /**
     * Retrieves all of the events that employee has
     * @param employeeId employee id
     * @return List of employee events
     */
    public LinkedList<Event> getEmployeeEvents(int employeeId) {
        connect();
        LinkedList<Event> events = new LinkedList<>();
        try {
            PreparedStatement stmt = c.prepareStatement(
                    "select events.* from events, employees, employees_events " +
                    "where events.id=employees_events.event_id" +
                    " and employees.id=employees_events.employee_id" +
                    " and employees.id=?");

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Event event = new Event(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("start"),
                        rs.getString("end"),
                        rs.getInt("organizer"),
                        rs.getString("location"),
                        rs.getInt("priority")
                );
                events.add(event);
            }
        } catch (Exception e){
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return events;
    }

    /**
     * Retrieves all of the attendees of specified event
     * @param eventId event id
     * @return list of all the attendees
     */
    public LinkedList<Employee> getEventAttendees(int eventId) {
        connect();
        LinkedList<Employee> employees = new LinkedList<>();
        try {
            PreparedStatement stmt = c.prepareStatement(
                    "select employees.id, employees.name, employees.login from employees, employees_events " +
                            "where employees.id = employees_events.employee_id " +
                            "and employees_events.event_id = ?");

            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Employee employee = new Employee(
                        rs.getString("name"),
                        rs.getInt("id"),
                        rs.getString("login")
                );
                employees.add(employee);
            }
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return employees;
    }

    /**
     * Updates event's attendees
     * @param eventId event id
     * @param attendees list of new attendees
     * @return whether the update was successful
     */
    private boolean updateEventAttendees(int eventId, LinkedList<Employee> attendees) {
        try {
            //select if performed first and then all of the entries are deleted one by one
            //this is done to trigger DB triggers and create inverted sql for undo
            PreparedStatement stmt = c.prepareStatement("select * from employees_events where event_id = ?");

            stmt.setInt(1, eventId);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                stmt = c.prepareStatement("delete from employees_events where event_id = ? and employee_id = ?");
                stmt.setInt(1, eventId);
                stmt.setInt(2, rs.getInt("employee_id"));
                stmt.executeUpdate();
            }


            stmt = c.prepareStatement("insert into employees_events(employee_id, event_id) values(? ,?)");

            for(Employee e : attendees) {
                stmt.setInt(1, e.getId());
                stmt.setInt(2, eventId);
                stmt.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }
    }

    /**
     * Updates the event
     * @param event event object with new information
     * @param attendees list of new attendees that need to be updated
     * @return whether the update was successful
     */
    public boolean updateEvent(Event event, LinkedList<Employee> attendees) {
        connect();
        try {
            c.setAutoCommit(false);
            PreparedStatement stmt = c.prepareStatement("update events set title = ?, description = ?, " +
                    "start = ?, end = ?, organizer = ?, priority = ?, location = ? where id = ?");

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDesc());
            stmt.setString(3, event.getStart().toInstant().toString());
            stmt.setString(4, event.getEnd().toInstant().toString());
            stmt.setInt(5, event.getOrganizer().getId());
            stmt.setInt(6, event.getPriority());
            stmt.setString(7,event.getLocation());
            stmt.setInt(8, event.getId());

            stmt.executeUpdate();

            if(!updateEventAttendees(event.getId(), attendees)){
                c.rollback();
                c.setAutoCommit(true);

                disconnect();
                return false;
            }

            stmt = c.prepareStatement("select max(tid) as mtid from undolog");
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                int newId = rs.getInt("mtid");

                stmt = c.prepareStatement("update undolog set tid = ? where tid = 0");
                stmt.setInt(1, ++newId);
                stmt.executeUpdate();
            }

            c.commit();
            c.setAutoCommit(true);

            disconnect();

            return true;
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
            try {
                c.rollback();
            } catch (SQLException ex) {
                logToFile( e.getClass().getName() + ": " + e.getMessage() );
            }
            disconnect();
            return false;
        }
    }

    /**
     * Gets all of the employees events in the date range specified.
     * @param employees List of employees whose events need to retrieved
     * @param start start date of the event
     * @param id event id. If 0 then event is ignored - means its being edited
     * @return List of all the events
     */
    public LinkedList<Event> getEmployeesEventsInRange(LinkedList<Employee> employees, String start, int id) {
        connect();
        StringJoiner joiner = new StringJoiner(", ");
        for(Employee e : employees) {
            joiner.add(String.format("%d", e.getId()));
        }

        String sql = String.format("select distinct ev.start, ev.end " +
                "from events ev " +
                "join employees_events ee on ee.event_id = ev.id " +
                "where ee.employee_id in(%s) " +
                "and ev.start like ? ", joiner.toString());

        if(id != 0) {
            sql += "and ev.id != ? ";
        }

        sql += "order by ev.start";


        LinkedList<Event> events = new LinkedList<>();
        try {
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, start+"%");

            if(id != 0) {
                stmt.setInt(2, id);
            }

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                events.add(new Event(
                        rs.getString("start"),
                        rs.getString("end")
                ));
            }
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return events;
    }

    /**
     * Creates a new entry in the Event table
     * @param e event object with all of the information
     * @param attendees list of event attendees
     * @return whether the creation was successful
     */
    public boolean createEvent(Event e, LinkedList<Employee> attendees){
        connect();

        try {
            c.setAutoCommit(false);
            PreparedStatement stmt = c.prepareStatement("insert into" +
                    " events(title, description, start, end, organizer, location, priority) values(?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, e.getTitle());
            stmt.setString(2, e.getDesc());
            stmt.setString(3, e.getStart().toInstant().toString());
            stmt.setString(4, e.getEnd().toInstant().toString());
            stmt.setInt(5, e.getOrganizer().getId());
            stmt.setString(6, e.getLocation());
            stmt.setInt(7, e.getPriority());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();

            int eventId = 0;

            if(rs.next()) {
                eventId = rs.getInt(1);
            }

            System.out.printf("new event id: %d", eventId);

            if(!updateEventAttendees(eventId, attendees)) {
                c.rollback();
                disconnect();
                c.setAutoCommit(true);
                return false;
            }

            stmt = c.prepareStatement("select max(tid) as mtid from undolog");
            rs = stmt.executeQuery();

            if(rs.next()) {
                int newId = rs.getInt("mtid");

                stmt = c.prepareStatement("update undolog set tid = ? where tid = 0");
                stmt.setInt(1, ++newId);
                stmt.executeUpdate();
            }

            c.commit();
            c.setAutoCommit(true);

            disconnect();

            return true;

        } catch (Exception ex) {
            logToFile( e.getClass().getName() + ": " + ex.getMessage() );
            try {
                c.rollback();
            } catch (SQLException exe) {
                logToFile( e.getClass().getName() + ": " + ex.getMessage() );
            }
            disconnect();
            return false;
        }
    }

    /**
     * Deletes employee and event link
     * @param eventId event id to be unlinked from
     * @param employeeId employee id to be unlinked. 0 means that on the individual should be unlinked
     * @return whether the unlink was successful
     */
    private boolean deleteEventLinks(int eventId, int employeeId) {
        try {
            String sql = "delete from employees_events where event_id = ?";
            if(employeeId > 0) {
                sql += " and employee_id = ?";
            }

            PreparedStatement stmt = c.prepareStatement(sql);

            stmt.setInt(1, eventId);
            if(employeeId > 0) {
                stmt.setInt(2, employeeId);
            }
            stmt.executeUpdate();

            return true;

        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        return false;
    }

    /**
     * deletes event by firs unlinking employees from events ant then delets the event
     * if employee id = 0 then event is removed for everyone
     * @param eventId event id that needs ot be unlinked
     * @param employeeId employee id
     * @return whether the deletion was successful
     */
    public boolean deleteEvent(int eventId, int employeeId) {
        connect();
        try {
            c.setAutoCommit(false);
            deleteEventLinks(eventId, employeeId);
            if(employeeId == 0) {
                PreparedStatement stmt = c.prepareStatement("delete from events where id = ?");
                stmt.setInt(1, eventId);
                stmt.executeUpdate();
            }

            PreparedStatement stmt = c.prepareStatement("select max(tid) as mtid from undolog");
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                int newId = rs.getInt("mtid");

                stmt = c.prepareStatement("update undolog set tid = ? where tid = 0");
                stmt.setInt(1, ++newId);
                stmt.executeUpdate();
            }
            c.commit();
            c.setAutoCommit(true);

            disconnect();

            return true;
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
            try {
                c.rollback();
            } catch (SQLException ex) {
                logToFile( e.getClass().getName() + ": " + ex.getMessage() );
            }
            return false;
        }
    }

    /**
     * retrieves all of the employees in the database
     * @return list of employees
     */
    public LinkedList<Employee> getEmployees() {
        connect();
        LinkedList<Employee> employees = new LinkedList<>();
        try {
            PreparedStatement stmt = c.prepareStatement("select id, name, login from employees");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Employee e = new Employee(
                        rs.getString("name"),
                        rs.getInt("id"),
                        rs.getString("login"));

                employees.add(e);
            }
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return employees;
    }

    /**
     * Inserts a new task into the database
     * @param employeeId id of the creator
     * @param task Task object that contains all the task information
     * @return whether the insertion was successful
     */
    public boolean createTask(int employeeId, Task task){
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("insert into tasks(description, priority, employee_id)" +
                    " values(?, ?, ?)");

            stmt.setString(1, task.getDescription());
            stmt.setInt(2, task.getPriority());
            stmt.setInt(3, employeeId);

            stmt.executeUpdate();

            stmt = c.prepareStatement("select max(tid) as mtid from undolog");
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                int newId = rs.getInt("mtid");

                stmt = c.prepareStatement("update undolog set tid = ? where tid = 0");
                stmt.setInt(1, ++newId);
                stmt.executeUpdate();
            }

            disconnect();

            return true;

        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );

            disconnect();

            return false;
        }
    }

    /**
     * Retrieves tasks of specified employee
     * @param employeeId id of an employee whose tasks need to be retrieved
     * @return list of tasks
     */
    public LinkedList<Task> getEmployeeTasks(int employeeId) {
        connect();
        LinkedList<Task> tasks = new LinkedList<>();
        try {
           PreparedStatement stmt = c.prepareStatement("select * from tasks where employee_id = ?");
           stmt.setInt(1, employeeId);
           ResultSet rs = stmt.executeQuery();

           while (rs.next()) {
               tasks.add(new Task(
                       rs.getInt("id"),
                       rs.getString("description"),
                       rs.getInt("priority")
               ));
           }

        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return tasks;
    }

    /**
     * updates task entry in the database
     * @param task task object that contains new information
     * @return whether the update was successful
     */
    public boolean updateTask(Task task) {
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("update tasks set description = ?, priority = ?" +
                    " where id = ?");

            stmt.setString(1, task.getDescription());
            stmt.setInt(2, task.getPriority());
            stmt.setInt(3, task.getId());

            stmt.executeUpdate();

            stmt = c.prepareStatement("select max(tid) as mtid from undolog");
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                int newId = rs.getInt("mtid");

                stmt = c.prepareStatement("update undolog set tid = ? where tid = 0");
                stmt.setInt(1, ++newId);
                stmt.executeUpdate();
            }

            disconnect();

            return true;
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
            disconnect();

            return false;
        }
    }

    /**
     * deletes task from the database
     * @param taskId id of the task that needs to be deleted
     * @return if the deletion was successful
     */
    public boolean deleteTask(int taskId) {
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("delete from tasks where id = ?");
            stmt.setInt(1, taskId);
            stmt.executeUpdate();

            stmt = c.prepareStatement("select max(tid) as mtid from undolog");
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                int newId = rs.getInt("mtid");

                stmt = c.prepareStatement("update undolog set tid = ? where tid = 0");
                stmt.setInt(1, ++newId);
                stmt.executeUpdate();
            }

            disconnect();
            return true;

        } catch (Exception e){
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return false;
    }

    /**
     * gets grouped inverted sql's created by triggers and performs them in reversed order to undo an operation
     * @return whether the undo was successful
     */
    public boolean undo() {
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("select max(tid) as mtid from undolog");
            ResultSet rs = stmt.executeQuery();

            int maxId = 0;

            if(rs.next()){
                maxId = rs.getInt("mtid");
            }

            if(maxId == 0) {
                disconnect();
                return false;
            }

            stmt = c.prepareStatement("select sql from undolog where tid = ? order by rowid desc");
            stmt.setInt(1, maxId);
            rs = stmt.executeQuery();

            while(rs.next()) {
                stmt = c.prepareStatement(rs.getString("sql"));
                stmt.executeUpdate();
            }

            stmt = c.prepareStatement("delete from undolog where tid = ?");
            stmt.setInt(1, maxId);
            stmt.executeUpdate();

            stmt = c.prepareStatement("delete from undolog where tid = 0");
            stmt.executeUpdate();

            disconnect();

            return true;

        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();

        return false;
    }

    public void clearLogs() {
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("delete from undolog");
            stmt.executeUpdate();
        } catch (Exception e) {
            logToFile( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    /**
     * write to a log file
     * @param text message to be logged
     */
    private void logToFile(String text) {
        try {
            fh = new FileHandler("logFile.log", true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        fh.setFormatter(formatter);
        logger.addHandler(fh);

        logger.severe(text);
        fh.close();
    }
}