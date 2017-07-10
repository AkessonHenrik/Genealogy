package returnTypes;

import models.Media;

import java.util.List;

public class WorkEventResult extends EventResult {
    public LocationResult location;
    public String company, position;
    public String type = "WorkEvent";

    public WorkEventResult(int id, String company, String position, LocationResult location, String name, String description, String[] time, List<Media> media) {
        super(id, name, description, time, media);
        this.location = location;
        this.company = company;
        this.position = position;
    }
}
