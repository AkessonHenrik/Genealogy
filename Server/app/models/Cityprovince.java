package models;

import javax.persistence.*;

/**
 * Created by Henrik on 17/06/2017.
 */
@Entity
@Table(name = "cityprovince",
        uniqueConstraints = {@UniqueConstraint(columnNames =
                {"cityid", "provinceid"})})
public class Cityprovince {
    private int id;
    private Integer cityid;
    private Integer provinceid;

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
    @Column(name = "cityid")
    public Integer getCityid() {
        return cityid;
    }

    public void setCityid(Integer cityid) {
        this.cityid = cityid;
    }

    @Basic
    @Column(name = "provinceid")
    public Integer getProvinceid() {
        return provinceid;
    }

    public void setProvinceid(Integer provinceid) {
        this.provinceid = provinceid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cityprovince that = (Cityprovince) o;

        if (id != that.id) return false;
        if (cityid != null ? !cityid.equals(that.cityid) : that.cityid != null) return false;
        if (provinceid != null ? !provinceid.equals(that.provinceid) : that.provinceid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (cityid != null ? cityid.hashCode() : 0);
        result = 31 * result + (provinceid != null ? provinceid.hashCode() : 0);
        return result;
    }
}
