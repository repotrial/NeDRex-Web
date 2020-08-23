package de.exbio.reposcapeweb.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends MongoRepository<JobDoc, String> {
    public Optional<JobDoc> findById(String id);

}
