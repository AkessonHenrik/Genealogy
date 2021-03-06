package models;

import javax.persistence.*;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Timedentityowner {
    private int id;
    private Integer timedentityid;
    private Integer peopleorrelationshipid;

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
    @Column(name = "timedentityid")
    public Integer getTimedentityid() {
        return timedentityid;
    }

    public void setTimedentityid(Integer timedentityid) {
        this.timedentityid = timedentityid;
    }

    @Basic
    @Column(name = "peopleorrelationshipid")
    public Integer getPeopleorrelationshipid() {
        return peopleorrelationshipid;
    }

    public void setPeopleorrelationshipid(Integer peopleorrelationshipid) {
        this.peopleorrelationshipid = peopleorrelationshipid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timedentityowner that = (Timedentityowner) o;

        if (id != that.id) return false;
        if (timedentityid != null ? !timedentityid.equals(that.timedentityid) : that.timedentityid != null)
            return false;
        return peopleorrelationshipid != null ? peopleorrelationshipid.equals(that.peopleorrelationshipid) : that.peopleorrelationshipid == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (timedentityid != null ? timedentityid.hashCode() : 0);
        result = 31 * result + (peopleorrelationshipid != null ? peopleorrelationshipid.hashCode() : 0);
        return result;
    }
}
