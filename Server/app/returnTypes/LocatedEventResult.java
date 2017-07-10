package returnTypes;

import models.Media;

import java.util.List;

public class LocatedEventResult extends EventResult {
    public String type = "LocatedEvent";
    public LocationResult location;

    public LocatedEventResult(int id, LocationResult location, String name, String description, String[] time, List<Media> media) {
        super(id, name, description, time, media);
        this.location = location;
    }
}
