package models;

import javax.persistence.*;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Eventmedia {
    private int id;
    private Integer mediaid;
    private Integer eventid;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

        if (id != that.id) return false;
        if (mediaid != null ? !mediaid.equals(that.mediaid) : that.mediaid != null) return false;
        return eventid != null ? eventid.equals(that.eventid) : that.eventid == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (mediaid != null ? mediaid.hashCode() : 0);
        result = 31 * result + (eventid != null ? eventid.hashCode() : 0);
        return result;
    }
}
