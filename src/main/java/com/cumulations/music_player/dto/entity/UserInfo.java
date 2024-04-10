package com.cumulations.music_player.dto.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String id;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    @Column(name = "email", unique = true)
    String email;

    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password;

    @Column(name = "active")
    @JsonIgnore
    boolean active;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    List<Role> roles;

    public UserInfo() {
    }

    public UserInfo(String firstName, String lastName, String email, String password) {
        this(firstName, lastName, email, password, true);
    }


    public UserInfo(String firstName, String lastName, String email, String password, boolean active) {
        this.active = active;
        this.password = password;
        this.email = email;
        this.lastName = lastName;
        this.firstName = firstName;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        if (roles == null) roles = new ArrayList<>();
        roles.add(role);
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", active=" + active +
                '}';
    }
}
