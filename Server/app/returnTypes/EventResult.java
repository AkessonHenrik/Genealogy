package returnTypes;

import models.Media;

import java.util.List;

public class EventResult {
    public int id;
    public String name, description;
    public String[] time;
    public List<Media> media;

    public EventResult(int id, String name, String description, String[] time, List<Media> media) {
        this.id = id;
        this.time = time;
        this.name = name;
        this.description = description;
        this.media = media;
    }
}

