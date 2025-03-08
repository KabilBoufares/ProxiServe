package com.proxiserve.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;



@Document(collection = "clients")
@Data
@NoArgsConstructor

public class Client {

    @Id
    private String id;

    @NotBlank(message = "User ID cannot be blank")
    private String userId; // Référence au User

    @NotBlank(message = "Nom du client obligatoire")
    private String fullName;

    private String phoneNumber;
}
