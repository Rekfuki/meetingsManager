package manager;

/**
 * Class to store employee relevant information and methods
 */
public class Employee {
    /**
     * employee name
     */
    private String name;

    /**
     * employee id
     */
    private int id;

    /**
     * employee emails, used as username
     */
    private String email;

    /**
     * Constructor used when creating employee from database
     * @param name employee name
     * @param id employee id
     * @param email employee email
     */
    Employee(String name, int id, String email) {
        this.name = name;
        this.id = id;
        this.email = email;
    }

    /**
     * gets employee name
     * @return employee name
     */
    public String getName() {
        return name;
    }

    /**
     * gets employee id
     * @return employee id
     */
    public int getId() {
        return id;
    }

    /**
     * sets employee id
     * @param id employee id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * gets employee email
     * @return employee email
     */
    public String getEmail() {
        return email;
    }
}
