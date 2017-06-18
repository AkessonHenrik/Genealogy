package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by Henrik on 17/06/2017.
 */
@Entity
public class Eventmedia implements Serializable {
    @Id
    private Integer mediaid;
    @Id
    private Integer eventid;

    @Basic
    @Column(name = "mediaid")
    public Integer getMediaid() {
        return mediaid;
    }

    public void setMediaid(Integer mediaid) {
        this.mediaid = mediaid;
    }

    @Basic
    @Column(name = "eventid")
    public Integer getEventid() {
        return eventid;
    }

    public void setEventid(Integer eventid) {
        this.eventid = eventid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Eventmedia that = (Eventmedia) o;

        if (mediaid != null ? !mediaid.equals(that.mediaid) : that.mediaid != null) return false;
        if (eventid != null ? !eventid.equals(that.eventid) : that.eventid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mediaid != null ? mediaid.hashCode() : 0;
        result = 31 * result + (eventid != null ? eventid.hashCode() : 0);
        return result;
    }
}
