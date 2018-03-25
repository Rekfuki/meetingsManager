package manager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Event {
    private int id;
    private String title;
    private String desc;
    private Date start;
    private Date end;
    private Employee organizer;
    private String location;
    private int priority;

    Event(int eid, String etitle, String edesc, String estart, String eend, int eorg, String eloc, int eprio) {
        this.id = eid;
        this.title = etitle;
        this.desc = edesc;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            this.start = sdf.parse(estart);
            this.end = sdf.parse(eend);
        } catch (Exception ex) {
            System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
        }

        this.organizer = new Database().getEmployeeByID(eorg);
        this.location = eloc;
        this.priority = eprio;
    }
    Event(String eStart, String eEnd) {
        System.out.printf("\nEvent constructor start time: %s\nEvent constructor end time: %s\n", eStart, eEnd);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            this.start = sdf.parse(eStart);
            this.end = sdf.parse(eEnd);
        } catch (Exception ex) {
            System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
            System.out.println("Failed to parse date in here");
        }
    }

    Event() {
        this.id = 0;
        this.title = "";
        this.desc = "";
        this.start = null;
        this.end = null;
        this.organizer = null;
        this.location = "";
        this.priority = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        if(desc == null) {
            return "";
        }
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
    public Employee getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Employee organizer) {
        this.organizer = organizer;
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
    public void print(){
        System.out.printf("\nEvent id: %d\nEvent name: %s\nEvent desc: %s\nEvent start: %s\nEvent end: %s",
                id, title, desc, start, end);
    }
}
