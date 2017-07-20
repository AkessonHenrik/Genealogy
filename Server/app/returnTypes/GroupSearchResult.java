package returnTypes;

import java.util.List;

public class GroupSearchResult {
    public int id;
    public int owner;
    public List<String> people;
    public String name;

    public GroupSearchResult(int id, int owner, List<String> people, String name) {
        this.id = id;
        this.owner = owner;
        this.people = people;
        this.name = name;
    }
}
