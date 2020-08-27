package de.exbio.reposcapeweb.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestRepository extends CrudRepository<TestDoc,String> {
    public Optional<TestDoc> findById(String id);
}
