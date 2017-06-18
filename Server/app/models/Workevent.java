package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Workevent {
    private int eventid;
    private String positionheld;
    private Integer companyid;
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
    @Column(name = "positionheld")
    public String getPositionheld() {
        return positionheld;
    }

    public void setPositionheld(String positionheld) {
        this.positionheld = positionheld;
    }

    @Basic
    @Column(name = "companyid")
    public Integer getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Integer companyid) {
        this.companyid = companyid;
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

        Workevent workevent = (Workevent) o;

        if (eventid != workevent.eventid) return false;
        if (positionheld != null ? !positionheld.equals(workevent.positionheld) : workevent.positionheld != null)
            return false;
        if (companyid != null ? !companyid.equals(workevent.companyid) : workevent.companyid != null) return false;
        if (locationid != null ? !locationid.equals(workevent.locationid) : workevent.locationid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventid;
        result = 31 * result + (positionheld != null ? positionheld.hashCode() : 0);
        result = 31 * result + (companyid != null ? companyid.hashCode() : 0);
        result = 31 * result + (locationid != null ? locationid.hashCode() : 0);
        return result;
    }
}
