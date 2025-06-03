package de.hitec.nhplus.model;

public class Caregiver {
    private long cid;
    private String firstName;
    private String surname;
    private String telephone;
    private String username;
    private String password;

    public Caregiver(String firstName, String surname, String telephone) {
        this.firstName = firstName;
        this.surname = surname;
        this.telephone = telephone;
    }

    public Caregiver(long cid, String username, String password, String firstName, String surname, String telephone) {
        this.cid = cid;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.surname = surname;
        this.telephone = telephone;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return firstName + " " + surname;
    }

    public String getLastName() {
        return surname;
    }

    public void setLastName(String lastName) {
        this.surname = lastName;
    }

    public String getPhoneNumber() {
        return telephone;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.telephone = phoneNumber;
    }

    @Override
    public String toString() {
        return "Caregiver{" +
                "cid=" + cid +
                ", firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", telephone='" + telephone + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}