package de.exbio.reposcapeweb.db.services.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import de.exbio.reposcapeweb.db.entities.nodes.Pathway;
import de.exbio.reposcapeweb.db.repositories.nodes.PathwayRepository;
import de.exbio.reposcapeweb.db.services.NodeService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.filter.NodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

@Service
public class PathwayService extends NodeService {

    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final PathwayRepository pathwayRepository;

    private HashMap<Integer,String> idToDomainMap = new HashMap<>();
    private HashMap<String,Integer> domainToIdMap = new HashMap<>();

    private NodeFilter allFilter;

    @Autowired
    public PathwayService(PathwayRepository pathwayRepository) {
        this.pathwayRepository = pathwayRepository;
        allFilter = new NodeFilter();
    }

    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<String, Pathway>> updates) {
        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion)) {
            pathwayRepository.deleteAll(pathwayRepository.findAllByPrimaryDomainIdIn(updates.get(UpdateOperation.Deletion).keySet()));
            updates.get(UpdateOperation.Deletion).values().forEach(d -> {
                idToDomainMap.remove(d.getId());
                domainToIdMap.remove(d.getPrimaryDomainId());
            });
            allFilter.removeByNodeIds(updates.get(UpdateOperation.Deletion).values().stream().map(Pathway::getId).collect(Collectors.toSet()));

        }

        LinkedList<Pathway> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Pathway> toUpdate = updates.get(UpdateOperation.Alteration);

            pathwayRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                toSave.add(d);
            });
        }
        pathwayRepository.saveAll(toSave).forEach(d -> {
            idToDomainMap.put(d.getId(), d.getPrimaryDomainId());
            domainToIdMap.put(d.getPrimaryDomainId(), d.getId());
            allFilter.add(d.toDistinctFilter(),d.toUniqueFilter());
        });
        log.debug("Updated pathway table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
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

    public NodeFilter getFilter(){
        return allFilter;
    }

    public void setFilter(NodeFilter nf){
        this.allFilter = nf;
    }
}
