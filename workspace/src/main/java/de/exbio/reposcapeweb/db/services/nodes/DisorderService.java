package de.exbio.reposcapeweb.db.services.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Disorder;
import de.exbio.reposcapeweb.db.repositories.nodes.DisorderRepository;
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
public class DisorderService {
    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final DisorderRepository disorderRepository;

    private HashMap<Integer,String> idToDomainMap = new HashMap<>();
    private HashMap<String,Integer> domainToIdMap = new HashMap<>();

    @Autowired
    public DisorderService(DisorderRepository disorderRepository) {
        this.disorderRepository = disorderRepository;
    }

    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<String, Disorder>> updates) {
        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion))
            disorderRepository.deleteAll(disorderRepository.findAllByPrimaryDomainIdIn(updates.get(UpdateOperation.Deletion).keySet()));

        LinkedList<Disorder> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Disorder> toUpdate = updates.get(UpdateOperation.Alteration);

            disorderRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                toSave.add(d);
            });
        }
        disorderRepository.saveAll(toSave);
        log.debug("Updated disorder table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }

    public int map(String primaryDomainId) {
        return getDomainToIdMap().get(primaryDomainId);
    }

    public HashMap<Integer, String> getIdToDomainMap() {
        return idToDomainMap;
    }

    public HashMap<String, Integer> getDomainToIdMap() {
        return domainToIdMap;
    }
}
