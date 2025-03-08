package com.proxiserve.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Data
@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Role cannot be blank")
    private String role;

    // Champs spécifiques aux clients
    private String fullName;
    private String phoneNumber;

    // Champs pour les artisans
    private String profession;
    private String companyName;
    private List<String> serviceCategories;
    private Double latitude;
    private Double longitude;
}
