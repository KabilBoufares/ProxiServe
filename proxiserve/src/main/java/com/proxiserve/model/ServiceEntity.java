package com.proxiserve.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "services")
public class ServiceEntity {

    @Id
    private String id;

    private String title;
    private String description;
    private double price;
    private String artisanId;
}
