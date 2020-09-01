package de.exbio.reposcapeweb.db.repositories;

import de.exbio.reposcapeweb.db.entities.edges.DisorderComorbidWithDisorder;
import de.exbio.reposcapeweb.db.entities.edges.ids.PairId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DisorderComorbidWithDisorderRepository extends CrudRepository<DisorderComorbidWithDisorder, PairId> {

    List<DisorderComorbidWithDisorder> findDisorderComorbidWithDisorderByIdIn(Collection<PairId> list);
}
