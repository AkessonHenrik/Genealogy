package returnTypes;

public class EventResult {
    public int id;
    public String name, description;
    public String[] time;


    public EventResult(int id, String name, String description, String[] time) {
        this.id = id;
        this.time = time;
        this.name = name;
        this.description = description;
    }
}

