package models;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Comment {
    private int id;
    private Integer postid;
    private Integer commenter;
    private Timestamp postedon;
    private String content;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "postid")
    public Integer getPostid() {
        return postid;
    }

    public void setPostid(Integer postid) {
        this.postid = postid;
    }

    @Basic
    @Column(name = "commenter")
    public Integer getCommenter() {
        return commenter;
    }


    public void setCommenter(Integer commenter) {
        this.commenter = commenter;
    }

    @Basic
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic
    @Column(name = "postedon")
    public Timestamp getPostedon() {
        return postedon;
    }

    public void setPostedon(Timestamp postedon) {
        this.postedon = postedon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Comment comment = (Comment) o;

        if (id != comment.id) return false;
        if (postid != null ? !postid.equals(comment.postid) : comment.postid != null) return false;
        if (commenter != null ? !commenter.equals(comment.commenter) : comment.commenter != null) return false;
        return postedon != null ? postedon.equals(comment.postedon) : comment.postedon == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (postid != null ? postid.hashCode() : 0);
        result = 31 * result + (commenter != null ? commenter.hashCode() : 0);
        result = 31 * result + (postedon != null ? postedon.hashCode() : 0);
        return result;
    }
}
