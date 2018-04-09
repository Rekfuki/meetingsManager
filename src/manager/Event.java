package manager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Class to store event related methods and fields
 */
public class Event {
    /**
     * event id, has to be unique
     */
    private int id;
    /**
     * event title
     */
    private String title;
    /**
     * event description
     */
    private String desc;
    /**
     * event start date and time
     */
    private Date start;
    /**
     * event end date and time
     */
    private Date end;
    /**
     * organizer of the event
     */
    private Employee organizer;
    /**
     * location of the event
     */
    private String location;
    /**
     * priority of the event
     */
    private int priority;

    /**
     * Event constructor that is used when creating event object from database
     * @param eid event id
     * @param etitle event title
     * @param edesc event description
     * @param estart event start date
     * @param eend event end date
     * @param eorg event organizer
     * @param eloc event location
     * @param eprio event priority
     */
    Event(int eid, String etitle, String edesc, String estart, String eend, int eorg, String eloc, int eprio) {
        this.id = eid;
        this.title = etitle;
        this.desc = edesc;

        //new formatter to convert instant time string to date
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

    /**
     * Event constructor that is used when creating event from just start time and end time
     * @param eStart event start
     * @param eEnd event end
     */
    Event(String eStart, String eEnd) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            this.start = sdf.parse(eStart);
            this.end = sdf.parse(eEnd);
        } catch (Exception ex) {
            System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
        }
    }

    /**
     * Event constructor that is used when creating an empty event for validation purposes.
     * Allows to perform checks where the event came from and what is suppose to be done with it
     */
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

    /**
     * gets event id
     * @return event id
     */
    public int getId() {
        return id;
    }

    /**
     * sets event id
     * @param id event id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * gets event title
     * @return event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * gets event description
     * @return event description
     */
    public String getDesc() {
        if(desc == null) {
            return "";
        }
        return desc;
    }

    /**
     * gets event start date
     * @return event start date
     */
    public Date getStart() {
        return start;
    }

    /**
     * gets event end date
     * @return event end date
     */
    public Date getEnd() {
        return end;
    }

    /**
     * sets event end time
     * @param end event end time
     */
    public void setEnd(Date end) {
        this.end = end;
    }

    /**
     * gets event organizer
     * @return event organizer
     */
    public Employee getOrganizer() {
        return organizer;
    }

    /**
     * sets event organizer
     * @param organizer event organizer
     */
    public void setOrganizer(Employee organizer) {
        this.organizer = organizer;
    }

    /**
     * gets event location
     * @return revent location
     */
    public String getLocation() {
        return location;
    }

    /**
     * gets event priority
     * @return event priority
     */
    public int getPriority() {
        return priority;
    }
}
