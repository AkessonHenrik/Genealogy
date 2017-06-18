package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Moveevent {
    private int eventid;
    private int locationid;

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
    public int getLocationid() {
        return locationid;
    }

    public void setLocationid(int locationid) {
        this.locationid = locationid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Moveevent moveevent = (Moveevent) o;

        if (eventid != moveevent.eventid) return false;
        if (locationid != moveevent.locationid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventid;
        result = 31 * result + locationid;
        return result;
    }
}
