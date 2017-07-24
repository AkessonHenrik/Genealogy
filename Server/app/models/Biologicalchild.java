package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Biologicalchild {
    private int parentsofid;

    @Id
    @Column(name = "parentsofid")
    public int getParentsofid() {
        return parentsofid;
    }

    public void setParentsofid(int parentsofid) {
        this.parentsofid = parentsofid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Biologicalchild that = (Biologicalchild) o;

        return parentsofid == that.parentsofid;
    }

    @Override
    public int hashCode() {
        return parentsofid;
    }
}
