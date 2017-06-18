package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Ghost {
    private int profileid;
    private Integer owner;

    @Id
    @Column(name = "profileid")
    public int getProfileid() {
        return profileid;
    }

    public void setProfileid(int profileid) {
        this.profileid = profileid;
    }

    @Basic
    @Column(name = "owner")
    public Integer getOwner() {
        return owner;
    }

    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ghost ghost = (Ghost) o;

        if (profileid != ghost.profileid) return false;
        if (owner != null ? !owner.equals(ghost.owner) : ghost.owner != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = profileid;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        return result;
    }
}
