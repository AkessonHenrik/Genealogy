package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Parentsof {
    private int timedentityid;
    private int childid;
    private int parentsid;
    private int parentType;

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

    @Basic
    @Column(name = "parentType")
    public int getParentType() {
        return parentType;
    }

    public void setParentType(int parentType) {
        this.parentType = parentType;
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
        return parentsid == parentsof.parentsid;
    }

    @Override
    public int hashCode() {
        int result = timedentityid;
        result = 31 * result + childid;
        result = 31 * result + parentsid;
        return result;
    }
}
