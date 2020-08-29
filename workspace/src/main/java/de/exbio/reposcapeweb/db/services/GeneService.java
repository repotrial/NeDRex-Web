package de.exbio.reposcapeweb.db.services;

import de.exbio.reposcapeweb.db.entities.nodes.Gene;
import de.exbio.reposcapeweb.db.repositories.GeneRepository;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

@Service
public class GeneService {
    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final GeneRepository geneRepository;

    @Autowired
    public GeneService(GeneRepository geneRepository) {
        this.geneRepository = geneRepository;
    }

    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<String, Gene>> updates) {
        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion))
            geneRepository.deleteAll(geneRepository.findAllByPrimaryDomainIdIn(updates.get(UpdateOperation.Deletion).keySet()));

        LinkedList<Gene> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Gene> toUpdate = updates.get(UpdateOperation.Alteration);

            geneRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                toSave.add(d);
            });
        }
        geneRepository.saveAll(toSave);
        log.debug("Updated gene table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }

}
