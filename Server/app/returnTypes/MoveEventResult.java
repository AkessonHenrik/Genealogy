package returnTypes;

import models.Media;

import java.util.List;

public class MoveEventResult extends EventResult {
    public LocationResult location;
    public String type = "MoveEvent";

    public MoveEventResult(int id, LocationResult location, String name, String description, String[] time, List<Media> media) {
        super(id, name, description, time, media);
        this.location = location;
    }

}
