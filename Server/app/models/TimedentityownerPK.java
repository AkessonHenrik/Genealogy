package models;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by Henrik on 17/06/2017.
 */
public class TimedentityownerPK implements Serializable {
    private int timedentityid;
    private int peopleorrelationshipid;

    @Column(name = "timedentityid")
    @Id
    public int getTimedentityid() {
        return timedentityid;
    }

    public void setTimedentityid(int timedentityid) {
        this.timedentityid = timedentityid;
    }

    @Column(name = "peopleorrelationshipid")
    @Id
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

        TimedentityownerPK that = (TimedentityownerPK) o;

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
