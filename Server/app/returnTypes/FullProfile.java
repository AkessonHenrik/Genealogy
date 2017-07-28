package returnTypes;

import java.util.List;

public class FullProfile {
    public ProfileResult profile;
    public List<EventResult> events;
    public LocatedEventResult born, died;

    public FullProfile(ProfileResult profile, List<EventResult> events) {
        this.profile = profile;
        this.events = events;
    }
}
