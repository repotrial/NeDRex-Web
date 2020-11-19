package de.exbio.reposcapeweb.communication.jobs;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends CrudRepository<Job,String> {
}
