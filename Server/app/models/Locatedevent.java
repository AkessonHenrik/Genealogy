package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 17/06/2017.
 */
@Entity
public class Locatedevent {
    private int eventid;
    private Integer locationid;

    @Id
    @Column(name = "eventid")
    public int getEventid() {
        return eventid;
    }

    public void setEventid(int eventid) {
        this.eventid = eventid;
    }

    @Basic
    @Column(name = "locationid")
    public Integer getLocationid() {
        return locationid;
    }

    public void setLocationid(Integer locationid) {
        this.locationid = locationid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Locatedevent that = (Locatedevent) o;

        if (eventid != that.eventid) return false;
        if (locationid != null ? !locationid.equals(that.locationid) : that.locationid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventid;
        result = 31 * result + (locationid != null ? locationid.hashCode() : 0);
        return result;
    }
}
