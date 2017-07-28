package returnTypes;

import models.Relationship;

import java.sql.Date;

public class RelationshipResult {
    public int id;
    public int profile1;
    public int profile2;
    public int type;
    public Date time;
    public Date begintime;
    public Date endtime;

    public RelationshipResult(int id, int profile1, int profile2, int type, Date time, Date begintime, Date endtime) {
        this.id = id;
        this.profile1 = profile1;
        this.profile2 = profile2;
        this.type = type;
        this.time = time;
        this.begintime = begintime;
        this.endtime = endtime;
    }

    public RelationshipResult() {
    }

    @Override
    public String toString() {
        return "RelationshipSearchResult{" +
                "id=" + id +
                ", profile1=" + profile1 +
                ", profile2=" + profile2 +
                ", type='" + type + '\'' +
                ", time=" + time +
                ", begintime=" + begintime +
                ", endtime=" + endtime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipResult other = (RelationshipResult) o;
        return other.id == this.id;
    }
}
