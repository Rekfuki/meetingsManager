package manager;

import java.time.Instant;
import java.util.Date;

public class Event {
    private int id;
    private String title;
    private String desc;
    private Date start;
    private Date end;

    Event(int eid, String etitle, String edesc, String estart, String eend) {
        this.id = eid;
        this.title = etitle;
        this.desc = edesc;
        this.start = Date.from(Instant.parse(estart));
        this.end = Date.from(Instant.parse(eend));
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
    public void print(){
        System.out.printf("\nEvent id: %d\nEvent name: %s\nEvent desc: %s\nEvent start: %s\nEvent end: %s",
                id, title, desc, start, end);
    }
}
