package com.proxiserve.repository;

import com.proxiserve.model.Certification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CertificationRepository extends MongoRepository<Certification, String> {
    List<Certification> findByArtisanId(String artisanId);
}
