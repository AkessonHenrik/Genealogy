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
public class Media {
    private int postid;
    private String path;
    private Integer type;

    @Id
    @Column(name = "postid")
    public int getPostid() {
        return postid;
    }

    public void setPostid(int postid) {
        this.postid = postid;
    }

    @Basic
    @Column(name = "path")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        if (postid != media.postid) return false;
        if (path != null ? !path.equals(media.path) : media.path != null) return false;
        return type != null ? type.equals(media.type) : media.type == null;
    }

    @Override
    public int hashCode() {
        int result = postid;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
