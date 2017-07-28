package models;

import javax.persistence.*;

@Entity
public class Claim {
    private int id;
    private int claimedprofileid;
    private int ownerid;
    private int claimerid;
    private String message;
    private Boolean approved;

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
    @Column(name = "claimedprofileid")
    public int getClaimedprofileid() {
        return claimedprofileid;
    }

    public void setClaimedprofileid(int claimedprofileid) {
        this.claimedprofileid = claimedprofileid;
    }

    @Basic
    @Column(name = "ownerid")
    public int getOwnerid() {
        return ownerid;
    }

    public void setOwnerid(int ownerid) {
        this.ownerid = ownerid;
    }

    @Basic
    @Column(name = "claimerid")
    public int getClaimerid() {
        return claimerid;
    }

    public void setClaimerid(int claimerid) {
        this.claimerid = claimerid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Claim claim = (Claim) o;

        if (id != claim.id) return false;
        if (claimedprofileid != claim.claimedprofileid) return false;
        if (ownerid != claim.ownerid) return false;
        if (claimerid != claim.claimerid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + claimedprofileid;
        result = 31 * result + ownerid;
        result = 31 * result + claimerid;
        return result;
    }

    @Basic
    @Column(name = "message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Basic
    @Column(name = "approved")
    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
}
