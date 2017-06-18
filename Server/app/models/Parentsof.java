package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 17/06/2017.
 */
@Entity
public class Parentsof {
    private int timedentityid;
    private int childid;
    private int parentsid;

    @Id
    @Column(name = "timedentityid")
    public int getTimedentityid() {
        return timedentityid;
    }

    public void setTimedentityid(int timedentityid) {
        this.timedentityid = timedentityid;
    }

    @Basic
    @Column(name = "childid")
    public int getChildid() {
        return childid;
    }

    public void setChildid(int childid) {
        this.childid = childid;
    }

    @Basic
    @Column(name = "parentsid")
    public int getParentsid() {
        return parentsid;
    }

    public void setParentsid(int parentsid) {
        this.parentsid = parentsid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parentsof parentsof = (Parentsof) o;

        if (timedentityid != parentsof.timedentityid) return false;
        if (childid != parentsof.childid) return false;
        if (parentsid != parentsof.parentsid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timedentityid;
        result = 31 * result + childid;
        result = 31 * result + parentsid;
        return result;
    }
}
