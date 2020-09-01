package de.exbio.reposcapeweb.db.repositories.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import de.exbio.reposcapeweb.db.entities.nodes.Pathway;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PathwayRepository extends CrudRepository<Pathway,Long> {
    List<Pathway> findAllByPrimaryDomainIdIn(Collection<String> ids);
}
