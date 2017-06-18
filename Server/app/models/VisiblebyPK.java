package models;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by Henrik on 17/06/2017.
 */
public class VisiblebyPK implements Serializable {
    private int timedentityid;
    private int accessid;

    @Column(name = "timedentityid")
    @Id
    public int getTimedentityid() {
        return timedentityid;
    }

    public void setTimedentityid(int timedentityid) {
        this.timedentityid = timedentityid;
    }

    @Column(name = "accessid")
    @Id
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

        VisiblebyPK that = (VisiblebyPK) o;

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
