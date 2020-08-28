package de.exbio.reposcapeweb.db.repositories;

import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrugRepository extends CrudRepository<Drug,Long> {

}
