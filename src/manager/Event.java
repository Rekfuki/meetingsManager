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
    private int organizer;
    private String location;


    Event(int eid, String etitle, String edesc, String estart, String eend, int eorg) {
        this.id = eid;
        this.title = etitle;
        this.desc = edesc;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            this.start = sdf.parse(estart);
            System.out.println(this.start);
            this.end = sdf.parse(eend);
            System.out.println(this.end);
        } catch (Exception ex) {
            System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
        }

        this.organizer = eorg;
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
    public int getOrganizer() {
        return organizer;
    }

    public void setOrganizer(int organizer) {
        this.organizer = organizer;
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public void print(){
        System.out.printf("\nEvent id: %d\nEvent name: %s\nEvent desc: %s\nEvent start: %s\nEvent end: %s",
                id, title, desc, start, end);
    }
}
