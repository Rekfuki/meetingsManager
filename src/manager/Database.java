package manager;

import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.LinkedList;

class Database {

    private Connection c;

    Database() {
        //Looks stupid, however when fist connection happens there is a slight delay and the ui lags.
        //Therefore performing initial connection when initializing allows to avoid the lag.
        connect();
        disconnect();
    }

    public void setup() {
        connect();
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
        disconnect();
    }

    private boolean createTables() {
        Statement stmt;
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
            return true;
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
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
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }
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
                return true;
            }
        } catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return false;
    }

    public LinkedList<Event> getEmployeesEvents(int id) {
        connect();
        LinkedList<Event> events = new LinkedList<>();
        try {
            PreparedStatement stmt = c.prepareStatement(
                    "select events.* from events, employees, employees_events " +
                    "where events.id=employees_events.event_id" +
                    " and employees.id=employees_events.employee_id" +
                    " and employees.id=?");

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Event event = new Event(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("start"),
                        rs.getString("end")
                );
                events.add(event);
            }
        } catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return events;
    }

    public void createEmployee(String name, String login, String paswd){
        String sql = "INSERT INTO employees(name, login, paswd) VALUES(?,?,?)";

        connect();
        try(PreparedStatement pstmt = c.prepareStatement(sql)){
            pstmt.setString(1, name);
            pstmt.setString(2, login);
            pstmt.setString(2, paswd);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        disconnect();
    }

    public void createEvent(String title, String description, String start, String end){
        String sql = "INSERT INTO events(title, description, start, end) VALUES(?,?,?,?)";

        connect();
        try(PreparedStatement pstmt = c.prepareStatement(sql)){
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, start);
            pstmt.setString(4, end);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        disconnect();
    }

    public void createTask(String title, String description, String deadline){
        String sql = "INSERT INTO tasks(title, description, deadline) VALUES(?,?,?)";

        connect();
        try(PreparedStatement pstmt = c.prepareStatement(sql)){
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, deadline);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        disconnect();
    }
}
