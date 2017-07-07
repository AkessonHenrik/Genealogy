package returnTypes;

import models.Locatedevent;

import java.util.List;

public class FullProfile {
    public SearchResult profile;
    public List<EventResult> events;
    public LocatedEventResult born, died;

    public FullProfile(SearchResult profile, List<EventResult> events) {
        this.profile = profile;
        this.events = events;
    }
}
