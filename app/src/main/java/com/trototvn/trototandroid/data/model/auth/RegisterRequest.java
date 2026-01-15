package com.trototvn.trototandroid.data.model.auth;

/**
 * Register request model
 */
public class RegisterRequest {
    private String phone;
    private String email;
    private String firstName;
    private String lastName;
    private String birthday; // ISO format
    private String gender; // "Male" or "Female"
    private String password;
    private String currentCity;
    private String currentDistrict;
    private String currentJob; // "Student" or "Employed"

    public RegisterRequest(String phone, String email, String firstName, String lastName,
                           String birthday, String gender, String password,
                           String currentCity, String currentDistrict, String currentJob) {
        this.phone = phone;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.gender = gender;
        this.password = password;
        this.currentCity = currentCity;
        this.currentDistrict = currentDistrict;
        this.currentJob = currentJob;
    }

    // Getters and Setters
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    public String getCurrentDistrict() {
        return currentDistrict;
    }

    public void setCurrentDistrict(String currentDistrict) {
        this.currentDistrict = currentDistrict;
    }

    public String getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(String currentJob) {
        this.currentJob = currentJob;
    }
}
