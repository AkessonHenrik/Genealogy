package models;

import javax.persistence.*;

/**
 * Created by Henrik on 18/06/2017.
 */
@Entity
public class Account {
    private int profileid;
    private String email;
    private String password;
    private int id;


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
    @Column(name = "profileid")
    public int getProfileid() {
        return profileid;
    }

    public void setProfileid(int profileid) {
        this.profileid = profileid;
    }

    @Basic
    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (profileid != account.profileid) return false;
        if (email != null ? !email.equals(account.email) : account.email != null) return false;
        return password != null ? password.equals(account.password) : account.password == null;
    }

    @Override
    public int hashCode() {
        int result = profileid;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

}
