package manager;

import org.sqlite.SQLiteConfig;

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
                    "title text not null," +
                    "description text not null," +
                    "deadline text not null)";
            stmt.executeUpdate(sql);
            sql = "create table employees_events " +
                    "(employee_id integer not null," +
                    "event_id integer not null," +
                    "foreign key(employee_id) references employees(id)," +
                    "foreign key(event_id) references events(id))";
            stmt.executeUpdate(sql);
            sql = "create table employees_events_task " +
                    "(employee_id integer not null," +
                    "event_id integer not null," +
                    "task_id integer not null," +
                    "foreign key(employee_id) references employees(id)," +
                    "foreign key(event_id) references events(id)," +
                    "foreign key(task_id) references tasks(id))";
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
        String tables[] = {"employees", "events", "tasks", "employees_events", "employees_events_task"};
        try {
            DatabaseMetaData md = c.getMetaData();
            for(int i = 0; i < 5; i++) {
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
//        System.out.printf("start: %s\nend: %s\n", start, end);
        StringJoiner joiner = new StringJoiner(", ");
        for(Employee e : employees) {
            joiner.add(String.format("%d", e.getId()));
        }

        String sql = String.format("select distinct ev.start, ev.end " +
                "from events ev " +
                "join employees_events ee on ee.event_id = ev.id " +
                "where ee.employee_id in(%s) " +
                "and ev.start like ? " +
                "and ev.id != ?" +
                "order by ev.start", joiner.toString());

        LinkedList<Event> events = new LinkedList<>();
        try {
//            PreparedStatement stmt = c.prepareStatement("select ev.start, ev.end from events ev " +
//                    "join employees_events ee on ee.event_id = ev.id where ee.employee_id in ("+joiner.toString()+") " +
//                    "and ev.start >= ? and ev.end <= ? order by ev.start");

            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, start+"%");
            stmt.setInt(2, id);
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

    public boolean removeEmployeeFromEvent(int employeeId, int eventId) {
        try {
            System.out.println(employeeId);
            System.out.println(eventId);
            PreparedStatement stmt = c.prepareStatement("delete from employees_events " +
                    "where employee_id = ? and event_id = ?");
            stmt.setInt(1, employeeId);
            stmt.setInt(2, eventId);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return false;
    }

    public void createEmployee(String name, String login, String paswd){
        String sql = "INSERT INTO employees(name, login, paswd) VALUES(?,?,?)";

        try(PreparedStatement pstmt = c.prepareStatement(sql)){
            pstmt.setString(1, name);
            pstmt.setString(2, login);
            pstmt.setString(2, paswd);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createEvent(String title, String description, String start, String end){
        String sql = "INSERT INTO events(title, description, start, end) VALUES(?,?,?,?)";

        try(PreparedStatement pstmt = c.prepareStatement(sql)){
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, start);
            pstmt.setString(4, end);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createTask(String title, String description, String deadline){
        String sql = "INSERT INTO tasks(title, description, deadline) VALUES(?,?,?)";

        try(PreparedStatement pstmt = c.prepareStatement(sql)){
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, deadline);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
