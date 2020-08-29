package de.exbio.reposcapeweb.db.services;

import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import de.exbio.reposcapeweb.db.entities.nodes.Pathway;
import de.exbio.reposcapeweb.db.repositories.DrugRepository;
import de.exbio.reposcapeweb.db.repositories.PathwayRepository;
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
public class PathwayService {

    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final PathwayRepository pathwayRepository;

    @Autowired
    public PathwayService(PathwayRepository pathwayRepository) {
        this.pathwayRepository = pathwayRepository;
    }

    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<String, Pathway>> updates) {
        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion))
            pathwayRepository.deleteAll(pathwayRepository.findAllByPrimaryDomainIdIn(updates.get(UpdateOperation.Deletion).keySet()));

        LinkedList<Pathway> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Pathway> toUpdate = updates.get(UpdateOperation.Alteration);

            pathwayRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                toSave.add(d);
            });
        }
        pathwayRepository.saveAll(toSave);
        log.debug("Updated pathway table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }
}
