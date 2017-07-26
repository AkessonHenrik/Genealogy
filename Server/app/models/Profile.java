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
public class Profile {
    private int peopleentityid;
    private String firstname;
    private String lastname;
    private int profilepicture;
    private int born;
    private Integer died;
    private Integer gender;

    public void setGender(int gender) {
        this.gender = gender;
    }

    @Id
    @Column(name = "peopleentityid")
    public int getPeopleentityid() {
        return peopleentityid;
    }

    public void setPeopleentityid(int peopleentityid) {
        this.peopleentityid = peopleentityid;
    }

    @Basic
    @Column(name = "firstname")
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @Basic
    @Column(name = "lastname")
    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Basic
    @Column(name = "profilepicture")
    public int getProfilepicture() {
        return profilepicture;
    }

    public void setProfilepicture(int profilepicture) {
        this.profilepicture = profilepicture;
    }

    @Basic
    @Column(name = "born")
    public int getBorn() {
        return born;
    }

    public void setBorn(int born) {
        this.born = born;
    }

    @Basic
    @Column(name = "died")
    public Integer getDied() {
        return died;
    }

    public void setDied(Integer died) {
        this.died = died;
    }

    @Basic
    @Column(name = "gender")
    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile profile = (Profile) o;

        if (peopleentityid != profile.peopleentityid) return false;
        if (profilepicture != profile.profilepicture) return false;
        if (born != profile.born) return false;
        if (firstname != null ? !firstname.equals(profile.firstname) : profile.firstname != null) return false;
        if (lastname != null ? !lastname.equals(profile.lastname) : profile.lastname != null) return false;
        if (died != null ? !died.equals(profile.died) : profile.died != null) return false;
//        if (gender != null ? !gender.equals(profile.gender) : profile.gender != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = peopleentityid;
        result = 31 * result + (firstname != null ? firstname.hashCode() : 0);
        result = 31 * result + (lastname != null ? lastname.hashCode() : 0);
        result = 31 * result + profilepicture;
        result = 31 * result + born;
        result = 31 * result + (died != null ? died.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        return result;
    }
}
