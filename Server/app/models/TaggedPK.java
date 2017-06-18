package models;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by Henrik on 17/06/2017.
 */
public class TaggedPK implements Serializable {
    private int profileid;
    private int postid;

    @Column(name = "profileid")
    @Id
    public int getProfileid() {
        return profileid;
    }

    public void setProfileid(int profileid) {
        this.profileid = profileid;
    }

    @Column(name = "postid")
    @Id
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

        TaggedPK taggedPK = (TaggedPK) o;

        if (profileid != taggedPK.profileid) return false;
        if (postid != taggedPK.postid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = profileid;
        result = 31 * result + postid;
        return result;
    }
}
