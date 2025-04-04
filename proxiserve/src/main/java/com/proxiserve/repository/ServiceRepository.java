package com.proxiserve.repository;

import com.proxiserve.model.Services;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceRepository extends MongoRepository<Services, String> {

    List<Services> findByArtisanId(String artisanId);

    
}
