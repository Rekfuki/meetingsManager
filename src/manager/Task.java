package manager;

/**
 * Class to store user task related methods and fields
 */
public class Task {
    /**
     * task id, has to be unique
     */
    private int id;
    /**
     * task description
     */
    private String description;
    /**
     * task priority 0-2
     */
    private int priority;


    /**
     * Constructor used to create tasks using information from the database
     * @param tId task id
     * @param tDesc task description
     * @param tPrio task priority
     */
    Task(int tId, String tDesc, int tPrio) {
        this.id = tId;
        this.description  = tDesc;
        this.priority = tPrio;
    }

    /**
     * Constructor used to create task using user's entered information
     * @param tDesc task description
     * @param tPrio  task priority
     */
    Task(String tDesc, int tPrio) {
        this.id = 0;
        this.description = tDesc;
        this.priority = tPrio;
    }

    /**
     * Gets id of the task
     * @return task id
     */
    public int getId() {
        return id;
    }

    /**
     * sets task id
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets task description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets task priority
     * @return
     */
    public int getPriority() {
        return priority;
    }
}
