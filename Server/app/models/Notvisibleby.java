package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * Created by Henrik on 17/06/2017.
 */
@Entity
@IdClass(NotvisiblebyPK.class)
public class Notvisibleby {
    private int timedentityid;
    private int accessid;

    @Id
    @Column(name = "timedentityid")
    public int getTimedentityid() {
        return timedentityid;
    }

    public void setTimedentityid(int timedentityid) {
        this.timedentityid = timedentityid;
    }

    @Id
    @Column(name = "accessid")
    public int getAccessid() {
        return accessid;
    }

    public void setAccessid(int accessid) {
        this.accessid = accessid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Notvisibleby that = (Notvisibleby) o;

        if (timedentityid != that.timedentityid) return false;
        if (accessid != that.accessid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timedentityid;
        result = 31 * result + accessid;
        return result;
    }
}
