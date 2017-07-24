package models;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Timedentity {
    private Integer id;
    private int timeid;
    private Integer visibility = 0;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "timeid")
    public int getTimeid() {
        return timeid;
    }

    public void setTimeid(int timeid) {
        this.timeid = timeid;
    }

    @Basic
    @Column(name = "visibility")
    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timedentity that = (Timedentity) o;

        if (id != that.id) return false;
        if (timeid != that.timeid) return false;
        return visibility != null ? visibility == that.visibility : that.visibility == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + timeid;
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
        return result;
    }
}
