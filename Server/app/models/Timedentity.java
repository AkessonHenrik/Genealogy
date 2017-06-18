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
public class Timedentity {
    private int id;
    private int timeid;
    private Serializable visibility;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
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
    public Serializable getVisibility() {
        return visibility;
    }

    public void setVisibility(Serializable visibility) {
        this.visibility = visibility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timedentity that = (Timedentity) o;

        if (id != that.id) return false;
        if (timeid != that.timeid) return false;
        if (visibility != null ? !visibility.equals(that.visibility) : that.visibility != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + timeid;
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
        return result;
    }
}
