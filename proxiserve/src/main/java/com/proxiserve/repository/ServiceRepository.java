package com.proxiserve.repository;

import com.proxiserve.model.ServiceEntity;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceRepository extends MongoRepository<ServiceEntity, String> {

    List<ServiceEntity> findByArtisanId(String artisanId);

    
}
