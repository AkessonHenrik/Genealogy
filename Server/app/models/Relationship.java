package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Relationship {
    private int peopleentityid;
    private Integer profile1;
    private Integer profile2;
    private Serializable type;

    @Id
    @Column(name = "peopleentityid")
    public int getPeopleentityid() {
        return peopleentityid;
    }

    public void setPeopleentityid(int peopleentityid) {
        this.peopleentityid = peopleentityid;
    }

    @Basic
    @Column(name = "profile1")
    public Integer getProfile1() {
        return profile1;
    }

    public void setProfile1(Integer profile1) {
        this.profile1 = profile1;
    }

    @Basic
    @Column(name = "profile2")
    public Integer getProfile2() {
        return profile2;
    }

    public void setProfile2(Integer profile2) {
        this.profile2 = profile2;
    }

    @Basic
    @Column(name = "type")
    public Serializable getType() {
        return type;
    }

    public void setType(Serializable type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Relationship that = (Relationship) o;

        if (peopleentityid != that.peopleentityid) return false;
        if (profile1 != null ? !profile1.equals(that.profile1) : that.profile1 != null) return false;
        if (profile2 != null ? !profile2.equals(that.profile2) : that.profile2 != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = peopleentityid;
        result = 31 * result + (profile1 != null ? profile1.hashCode() : 0);
        result = 31 * result + (profile2 != null ? profile2.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
