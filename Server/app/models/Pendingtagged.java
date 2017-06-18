package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * Created by Henrik on 17/06/2017.
 */
@Entity
@IdClass(PendingtaggedPK.class)
public class Pendingtagged {
    private int profileid;
    private int postid;

    @Id
    @Column(name = "profileid")
    public int getProfileid() {
        return profileid;
    }

    public void setProfileid(int profileid) {
        this.profileid = profileid;
    }

    @Id
    @Column(name = "postid")
    public int getPostid() {
        return postid;
    }

    public void setPostid(int postid) {
        this.postid = postid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pendingtagged that = (Pendingtagged) o;

        if (profileid != that.profileid) return false;
        if (postid != that.postid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = profileid;
        result = 31 * result + postid;
        return result;
    }
}
