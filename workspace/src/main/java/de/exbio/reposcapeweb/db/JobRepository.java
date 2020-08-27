package de.exbio.reposcapeweb.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//@Repository
public interface JobRepository extends CrudRepository<JobDoc,String> {
    public Optional<JobDoc> findById(String id);

}
