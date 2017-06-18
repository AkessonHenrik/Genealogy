package models;

import javax.persistence.*;

/**
 * Created by Henrik on 17/06/2017.
 */
@Entity
@Table(name = "provincecountry",
        uniqueConstraints = {@UniqueConstraint(columnNames =
                {"countryid", "provinceid"})})
public class Provincecountry {
    private int id;
    private Integer provinceid;
    private Integer countryid;

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
    @Column(name = "provinceid")
    public Integer getProvinceid() {
        return provinceid;
    }

    public void setProvinceid(Integer provinceid) {
        this.provinceid = provinceid;
    }

    @Basic
    @Column(name = "countryid")
    public Integer getCountryid() {
        return countryid;
    }

    public void setCountryid(Integer countryid) {
        this.countryid = countryid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Provincecountry that = (Provincecountry) o;

        if (id != that.id) return false;
        if (provinceid != null ? !provinceid.equals(that.provinceid) : that.provinceid != null) return false;
        if (countryid != null ? !countryid.equals(that.countryid) : that.countryid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (provinceid != null ? provinceid.hashCode() : 0);
        result = 31 * result + (countryid != null ? countryid.hashCode() : 0);
        return result;
    }
}
