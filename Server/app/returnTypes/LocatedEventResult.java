package returnTypes;

public class LocatedEventResult extends EventResult {
    public String type = "LocatedEvent";
    public LocationResult location;

    public LocatedEventResult(int id, LocationResult location, String name, String description, String[] time) {
        super(id, name, description, time);
        this.location = location;
    }
}
