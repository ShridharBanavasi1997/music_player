package com.cumulations.music_player.dto.request_body;

public class UserUpdateRequestBody {
    private String firstName;
    private String lastName;

    public UserUpdateRequestBody() {
    }

    public UserUpdateRequestBody(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
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
}
