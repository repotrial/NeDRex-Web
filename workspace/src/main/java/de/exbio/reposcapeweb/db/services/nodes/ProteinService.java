package de.exbio.reposcapeweb.db.services.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Protein;
import de.exbio.reposcapeweb.db.repositories.nodes.ProteinRepository;
import de.exbio.reposcapeweb.db.services.NodeService;
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
public class ProteinService extends NodeService {
    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final ProteinRepository proteinRepository;

    private HashMap<Integer, String> idToDomainMap = new HashMap<>();
    private HashMap<String, Integer> domainToIdMap = new HashMap<>();

    @Autowired
    public ProteinService(ProteinRepository proteinRepository) {
        this.proteinRepository = proteinRepository;
    }

    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<String, Protein>> updates) {
        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion)) {
            proteinRepository.deleteAll(proteinRepository.findAllByPrimaryDomainIdIn(updates.get(UpdateOperation.Deletion).keySet()));
            updates.get(UpdateOperation.Deletion).values().forEach(d -> {
                idToDomainMap.remove(d.getId());
                domainToIdMap.remove(d.getPrimaryDomainId());
            });
        }

        LinkedList<Protein> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Protein> toUpdate = updates.get(UpdateOperation.Alteration);

            proteinRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                toSave.add(d);
            });
        }
        proteinRepository.saveAll(toSave).forEach(d -> {
            idToDomainMap.put(d.getId(), d.getPrimaryDomainId());
            domainToIdMap.put(d.getPrimaryDomainId(), d.getId());
        });
        log.debug("Updated protein table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
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
