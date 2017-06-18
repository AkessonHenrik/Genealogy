package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Singletime {
    private int timeid;
    private Date time;

    @Id
    @Column(name = "timeid")
    public int getTimeid() {
        return timeid;
    }

    public void setTimeid(int timeid) {
        this.timeid = timeid;
    }

    @Basic
    @Column(name = "time")
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Singletime that = (Singletime) o;

        if (timeid != that.timeid) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timeid;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }
}
