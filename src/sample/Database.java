package sample;

import java.sql.*;

class Database {

    private Connection c;

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
        if(c != null) {
            return;
        }

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:meetings.db");
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
}
