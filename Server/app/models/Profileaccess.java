package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 17/06/2017.
 */
@Entity
public class Profileaccess {
    private int id;
    private Integer accessid;
    private Integer profileid;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "accessid")
    public Integer getAccessid() {
        return accessid;
    }

    public void setAccessid(Integer accessid) {
        this.accessid = accessid;
    }

    @Basic
    @Column(name = "profileid")
    public Integer getProfileid() {
        return profileid;
    }

    public void setProfileid(Integer profileid) {
        this.profileid = profileid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profileaccess that = (Profileaccess) o;

        if (id != that.id) return false;
        if (accessid != null ? !accessid.equals(that.accessid) : that.accessid != null) return false;
        if (profileid != null ? !profileid.equals(that.profileid) : that.profileid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (accessid != null ? accessid.hashCode() : 0);
        result = 31 * result + (profileid != null ? profileid.hashCode() : 0);
        return result;
    }
}
