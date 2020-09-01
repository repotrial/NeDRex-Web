package de.exbio.reposcapeweb.db.repositories.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DrugRepository extends CrudRepository<Drug,Long> {
    List<Drug> findAllByPrimaryDomainIdIn(Collection<String> ids);

}
