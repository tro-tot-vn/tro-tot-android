package com.trototvn.trototandroid.data.model.admin;

/**
 * Body for POST api/admin/moderators
 * phone pattern: ^(0|84)(3|5|7|8|9)\d{8}$ ; gender: Male|Female ; birthday: yyyy-MM-dd
 */
public class AddModeratorRequest {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final String gender;
    private final String birthday;

    public AddModeratorRequest(String firstName, String lastName, String email,
                               String phone, String gender, String birthday) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.birthday = birthday;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthday() {
        return birthday;
    }
}
