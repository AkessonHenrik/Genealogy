package models;

import javax.persistence.*;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Groupaccess {
    private int id;
    private Integer groupid;
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
    @Column(name = "groupid")
    public Integer getGroupid() {
        return groupid;
    }

    public void setGroupid(Integer groupid) {
        this.groupid = groupid;
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

        Groupaccess that = (Groupaccess) o;

        if (id != that.id) return false;
        if (groupid != null ? !groupid.equals(that.groupid) : that.groupid != null) return false;
        return accessid != null ? accessid.equals(that.accessid) : that.accessid == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (groupid != null ? groupid.hashCode() : 0);
        result = 31 * result + (accessid != null ? accessid.hashCode() : 0);
        return result;
    }
}
