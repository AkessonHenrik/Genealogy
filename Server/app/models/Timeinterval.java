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
public class Timeinterval {
    private int timeid;
    private Date begintime;
    private Date endtime;

    @Id
    @Column(name = "timeid")
    public int getTimeid() {
        return timeid;
    }

    public void setTimeid(int timeid) {
        this.timeid = timeid;
    }

    @Basic
    @Column(name = "begintime")
    public Date getBegintime() {
        return begintime;
    }

    public void setBegintime(Date begintime) {
        this.begintime = begintime;
    }


    @Basic
    @Column(name = "endtime")
    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timeinterval that = (Timeinterval) o;

        if (timeid != that.timeid) return false;
        if (begintime != null ? !begintime.equals(that.begintime) : that.begintime != null) return false;
        return endtime != null ? endtime.equals(that.endtime) : that.endtime == null;
    }

    @Override
    public int hashCode() {
        int result = timeid;
        result = 31 * result + (begintime != null ? begintime.hashCode() : 0);
        result = 31 * result + (endtime != null ? endtime.hashCode() : 0);
        return result;
    }

}
