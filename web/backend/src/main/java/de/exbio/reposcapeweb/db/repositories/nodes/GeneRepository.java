package de.exbio.reposcapeweb.db.repositories.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Gene;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GeneRepository extends CrudRepository<Gene,Integer> {
    List<Gene> findAllByPrimaryDomainIdIn(Collection<String> ids);
}
