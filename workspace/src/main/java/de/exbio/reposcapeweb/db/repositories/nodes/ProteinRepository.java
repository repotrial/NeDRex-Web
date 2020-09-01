package de.exbio.reposcapeweb.db.repositories.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Protein;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProteinRepository extends CrudRepository<Protein,Long> {
    List<Protein> findAllByPrimaryDomainIdIn(Collection<String> ids);
}
