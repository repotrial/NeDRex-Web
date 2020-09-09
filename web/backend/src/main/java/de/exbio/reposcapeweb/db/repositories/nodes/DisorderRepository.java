package de.exbio.reposcapeweb.db.repositories.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Disorder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DisorderRepository extends CrudRepository<Disorder,Long> {
    List<Disorder> findAllByPrimaryDomainIdIn(Collection<String> ids);
}
