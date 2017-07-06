package returnTypes;

public class MoveEventResult extends EventResult {
    public LocationResult location;
    public String type = "MoveEvent";

    public MoveEventResult(int id, LocationResult location, String name, String description, String[] time) {
        super(id, name, description, time);
        this.location = location;
    }

}
