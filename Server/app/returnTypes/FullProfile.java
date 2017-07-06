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
//        for (EventResult event : events) {
//            if (event.name.equals("born")) {
//                this.born = (LocatedEventResult) event;
//                events.remove(event);
//            } else if (event.name.equals("died")) {
//                this.died = (LocatedEventResult) event;
//                events.remove(event);
//            }
//        }
    }
}
