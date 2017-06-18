package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * Created by Henrik on 17/06/2017.
 */
@Entity
@IdClass(TimedentityownerPK.class)
public class Timedentityowner {
    private int timedentityid;
    private int peopleorrelationshipid;

    @Id
    @Column(name = "timedentityid")
    public int getTimedentityid() {
        return timedentityid;
    }

    public void setTimedentityid(int timedentityid) {
        this.timedentityid = timedentityid;
    }

    @Id
    @Column(name = "peopleorrelationshipid")
    public int getPeopleorrelationshipid() {
        return peopleorrelationshipid;
    }

    public void setPeopleorrelationshipid(int peopleorrelationshipid) {
        this.peopleorrelationshipid = peopleorrelationshipid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timedentityowner that = (Timedentityowner) o;

        if (timedentityid != that.timedentityid) return false;
        if (peopleorrelationshipid != that.peopleorrelationshipid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timedentityid;
        result = 31 * result + peopleorrelationshipid;
        return result;
    }
}
