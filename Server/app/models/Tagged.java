package models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Tagged {
    private int id;
    private Integer profileid;
    private Integer postid;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "profileid")
    public Integer getProfileid() {
        return profileid;
    }

    public void setProfileid(Integer profileid) {
        this.profileid = profileid;
    }

    @Basic
    @Column(name = "postid")
    public Integer getPostid() {
        return postid;
    }

    public void setPostid(Integer postid) {
        this.postid = postid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tagged tagged = (Tagged) o;

        if (id != tagged.id) return false;
        if (profileid != null ? !profileid.equals(tagged.profileid) : tagged.profileid != null) return false;
        if (postid != null ? !postid.equals(tagged.postid) : tagged.postid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (profileid != null ? profileid.hashCode() : 0);
        result = 31 * result + (postid != null ? postid.hashCode() : 0);
        return result;
    }
}
