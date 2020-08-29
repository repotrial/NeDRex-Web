package de.exbio.reposcapeweb.db.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import de.exbio.reposcapeweb.db.repositories.DrugRepository;
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
public class DrugService {


    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final DrugRepository drugRepository;

    @Autowired
    public DrugService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<String, Drug>> updates) {
        log.debug("Applying drug table updates.");
        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion))
            drugRepository.deleteAll(drugRepository.findAllByPrimaryDomainIdIn(updates.get(UpdateOperation.Deletion).keySet()));

        LinkedList<Drug> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Drug> toUpdate = updates.get(UpdateOperation.Alteration);

            drugRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                toSave.add(d);
            });
        }
        drugRepository.saveAll(toSave);
        log.debug("Updated drug table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }

}
