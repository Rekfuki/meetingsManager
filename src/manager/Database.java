package manager;

import org.sqlite.SQLiteConfig;

import java.lang.reflect.Executable;
import java.sql.*;
import java.util.LinkedList;
import java.util.StringJoiner;

class Database {

    private Connection c;

    Database() {
        //Looks stupid, however when first connection happens there is a slight delay and the ui lags.
        //Therefore performing initial connection when initializing allows to avoid the lag.
        connect();
        disconnect();
    }

//    public void finalize(){
//        disconnect();
//    }

    public void setup() {
        if(c == null) {
            return;
        }
        System.out.println("opened database successfully");
        if(!tablesExist()) {
            System.out.println("Tables do not exist\nCreating tables");
            if(createTables()) {
                System.out.println("Tables created successfully");
            } else {
                System.out.println("Failed to create tables");
                return;
            }
        }else {
            System.out.println("Tables already exist");
        }
        System.out.println("Everything is in order");
    }

    private boolean createTables() {
        Statement stmt;
        connect();
        try {
            stmt = c.createStatement();
            String sql = "create table employees " +
                    "(id integer not null primary key autoincrement," +
                    "name text not null," +
                    "login text not null," +
                    "paswd text not null)";
            stmt.executeUpdate(sql);
            sql = "create table events " +
                    "(id integer not null primary key autoincrement," +
                    "title text not null," +
                    "description text," +
                    "start text not null," +
                    "end text not null)";
            stmt.executeUpdate(sql);
            sql = "create table tasks " +
                    "(id integer not null primary key autoincrement," +
                    "priority text not null," +
                    "description text not null)";
            stmt.executeUpdate(sql);
            sql = "create table employees_events " +
                    "(employee_id integer not null," +
                    "event_id integer not null," +
                    "foreign key(employee_id) references employees(id)," +
                    "foreign key(event_id) references events(id))";
            stmt.executeUpdate(sql);

            stmt.executeUpdate(sql);
            stmt.close();

            disconnect();

            return true;
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );

            disconnect();

            return false;
        }
    }

    private boolean tablesExist() {
        connect();
        String tables[] = {"employees", "events", "tasks", "employees_events"};
        try {
            DatabaseMetaData md = c.getMetaData();
            for(int i = 0; i < 4; i++) {
                ResultSet rs = md.getTables(null, null, tables[i], null);
                while(rs.next()) {
                    String tName = rs.getString("TABLE_NAME");
                    if(tName == null || !tName.equals(tables[i])) {
                        disconnect();
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            disconnect();
            return false;
        }
        disconnect();
        return true;
    }

    private void connect() {
        try {
            System.out.println("Connection is closed, tyring to open the connection");
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            c = DriverManager.getConnection("jdbc:sqlite:meetings.db", config.toProperties());
            System.out.println("successfully connected to the database");
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    private void disconnect() {
        try {
            c.close();
        } catch (Exception e ){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public int getEmployeeID(String usrnm, String pswd) {
        System.out.println(usrnm +"\n"+ pswd);
        connect();
        try {
            PreparedStatement stmt =  c.prepareStatement("select id from employees where login=? and paswd=?");
            stmt.setString(1, usrnm);
            stmt.setString(2, pswd);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                System.out.println("Login found");
                int id = rs.getInt("id");
                disconnect();
                return id;
            }
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return 0;
    }

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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return null;
    }

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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return false;
    }

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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return false;
    }

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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return events;
    }

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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return employees;
    }

    private boolean updateEventAttendees(int eventId, LinkedList<Employee> attendees) {
        try {
            PreparedStatement stmt = c.prepareStatement("delete from employees_events where event_id = ?");

            stmt.setInt(1, eventId);
            stmt.executeUpdate();

            stmt = c.prepareStatement("insert into employees_events(employee_id, event_id) values(? ,?)");

            for(Employee e : attendees) {
                stmt.setInt(1, e.getId());
                stmt.setInt(2, eventId);
                stmt.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }
    }

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


            c.commit();
            c.setAutoCommit(false);

            disconnect();

            return true;
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            try {
                c.rollback();
            } catch (SQLException ex) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            }
            disconnect();
            return false;
        }
    }

    public LinkedList<Event> getEmployeesEventsInRange(LinkedList<Employee> employees, String start, int id) {
        connect();
        StringJoiner joiner = new StringJoiner(", ");
        for(Employee e : employees) {
            joiner.add(String.format("%d", e.getId()));
        }

        System.out.printf("Employee id's: %s", joiner.toString());

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
//            PreparedStatement stmt = c.prepareStatement("select ev.start, ev.end from events ev " +
//                    "join employees_events ee on ee.event_id = ev.id where ee.employee_id in ("+joiner.toString()+") " +
//                    "and ev.start >= ? and ev.end <= ? order by ev.start");

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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return events;
    }

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

            c.commit();
            c.setAutoCommit(true);
            disconnect();

            return true;

        } catch (Exception ex) {
            System.err.println( e.getClass().getName() + ": " + ex.getMessage() );
            try {
                c.rollback();
            } catch (SQLException exe) {
                System.err.println( e.getClass().getName() + ": " + ex.getMessage() );
            }
            disconnect();
            return false;
        }
    }

    private boolean deleteEventLinks(int eventId, int employeeId) {

        System.out.printf("Employee id: %d", employeeId);

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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return false;
    }

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

            c.commit();
            c.setAutoCommit(true);
            disconnect();

            return true;
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            try {
                c.rollback();
            } catch (SQLException ex) {
                System.err.println( e.getClass().getName() + ": " + ex.getMessage() );
            }
            return false;
        }
    }

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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return employees;
    }

    public boolean createTask(int employeeId, Task task){
        connect();

        try {
            PreparedStatement stmt = c.prepareStatement("insert into tasks(description, priority, employee_id)" +
                    " values(?, ?, ?)");

            stmt.setString(1, task.getDescription());
            stmt.setInt(2, task.getPriority());
            stmt.setInt(3, employeeId);

            stmt.executeUpdate();
            disconnect();

            return true;

        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );

            disconnect();

            return false;
        }
    }


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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return tasks;
    }

    public boolean updateTask(Task task) {
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("update tasks set description = ?, priority = ?" +
                    " where id = ?");

            stmt.setString(1, task.getDescription());
            stmt.setInt(2, task.getPriority());
            stmt.setInt(3, task.getId());

            stmt.executeUpdate();
            disconnect();

            return true;
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            disconnect();

            return false;
        }
    }

    public boolean deleteTask(int taskId) {
        connect();
        try {
            PreparedStatement stmt = c.prepareStatement("delete from tasks where id = ?");
            stmt.setInt(1, taskId);
            stmt.executeUpdate();

            disconnect();
            return true;

        } catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        disconnect();
        return false;
    }
}
