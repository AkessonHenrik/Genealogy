package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Post {
    private int timedentityid;

    @Id
    @Column(name = "timedentityid")
    public int getTimedentityid() {
        return timedentityid;
    }

    public void setTimedentityid(int timedentityid) {
        this.timedentityid = timedentityid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        return timedentityid == post.timedentityid;
    }

    @Override
    public int hashCode() {
        return timedentityid;
    }
}
