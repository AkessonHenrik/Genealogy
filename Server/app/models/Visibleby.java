package models;

import javax.persistence.*;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Visibleby {
    private int id;
    private Integer timedentityid;
    private Integer accessid;

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
    @Column(name = "accessid")
    public Integer getAccessid() {
        return accessid;
    }

    public void setAccessid(Integer accessid) {
        this.accessid = accessid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Visibleby visibleby = (Visibleby) o;

        if (id != visibleby.id) return false;
        if (timedentityid != null ? !timedentityid.equals(visibleby.timedentityid) : visibleby.timedentityid != null)
            return false;
        if (accessid != null ? !accessid.equals(visibleby.accessid) : visibleby.accessid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (timedentityid != null ? timedentityid.hashCode() : 0);
        result = 31 * result + (accessid != null ? accessid.hashCode() : 0);
        return result;
    }
}
