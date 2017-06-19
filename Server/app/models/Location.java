package models;

import javax.persistence.*;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Location {
    private int id;
    private Integer cityprovinceid;
    private Integer provincecountryid;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "cityprovinceid")
    public Integer getCityprovinceid() {
        return cityprovinceid;
    }

    public void setCityprovinceid(Integer cityprovinceid) {
        this.cityprovinceid = cityprovinceid;
    }

    @Basic
    @Column(name = "provincecountryid")
    public Integer getProvincecountryid() {
        return provincecountryid;
    }

    public void setProvincecountryid(Integer provincecountryid) {
        this.provincecountryid = provincecountryid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (id != location.id) return false;
        if (cityprovinceid != null ? !cityprovinceid.equals(location.cityprovinceid) : location.cityprovinceid != null)
            return false;
        if (provincecountryid != null ? !provincecountryid.equals(location.provincecountryid) : location.provincecountryid != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (cityprovinceid != null ? cityprovinceid.hashCode() : 0);
        result = 31 * result + (provincecountryid != null ? provincecountryid.hashCode() : 0);
        return result;
    }
}
