package com.proxiserve.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.proxiserve.model.Client;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {

}
