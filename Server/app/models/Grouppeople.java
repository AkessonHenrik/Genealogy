package models;

import javax.persistence.*;

/**
 * Created by Henrik on 19/07/2017.
 */
@Entity
public class Grouppeople {
    private int id;
    private int groupid;
    private int profileid;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Grouppeople that = (Grouppeople) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Basic
    @Column(name = "groupid")
    public int getGroupid() {
        return groupid;
    }

    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }

    @Basic
    @Column(name = "profileid")
    public int getProfileid() {
        return profileid;
    }

    public void setProfileid(int profileid) {
        this.profileid = profileid;
    }
}
