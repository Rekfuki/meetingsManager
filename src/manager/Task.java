package manager;

public class Task {
    private int id;
    private String description;
    private int priority;

    Task(int tId, String tDesc, int tPrio) {
        this.id = tId;
        this.description  = tDesc;
        this.priority = tPrio;
    }

    Task(String tDesc, int tPrio) {
        this.id = 0;
        this.description = tDesc;
        this.priority = tPrio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
