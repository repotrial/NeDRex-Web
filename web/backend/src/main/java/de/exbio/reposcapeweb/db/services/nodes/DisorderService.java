package de.exbio.reposcapeweb.db.services.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Disorder;
import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import de.exbio.reposcapeweb.db.repositories.nodes.DisorderRepository;
import de.exbio.reposcapeweb.db.services.NodeService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.filter.FilterService;
import de.exbio.reposcapeweb.filter.NodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DisorderService extends NodeService {
    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final DisorderRepository disorderRepository;
    private final FilterService filterService;

    private HashMap<Integer, String> idToDomainMap = new HashMap<>();
    private HashMap<String, Integer> domainToIdMap = new HashMap<>();

    private NodeFilter allFilter;

    @Autowired
    public DisorderService(DisorderRepository disorderRepository, FilterService filterService) {
        this.disorderRepository = disorderRepository;
        this.filterService = filterService;
        allFilter = new NodeFilter();
    }

    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<String, Disorder>> updates) {
        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion)) {
            disorderRepository.deleteAll(disorderRepository.findAllByPrimaryDomainIdIn(updates.get(UpdateOperation.Deletion).keySet()));
            updates.get(UpdateOperation.Deletion).values().forEach(d -> {
                idToDomainMap.remove(d.getId());
                domainToIdMap.remove(d.getPrimaryDomainId());
            });
            allFilter.removeByNodeIds(updates.get(UpdateOperation.Deletion).values().stream().map(Disorder::getId).collect(Collectors.toSet()));
        }
        LinkedList<Disorder> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Disorder> toUpdate = updates.get(UpdateOperation.Alteration);

            disorderRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                toSave.add(d);
            });
        }
        disorderRepository.saveAll(toSave).forEach(d -> {
            idToDomainMap.put(d.getId(), d.getPrimaryDomainId());
            domainToIdMap.put(d.getPrimaryDomainId(), d.getId());
            allFilter.add(d.toDistinctFilter(),d.toUniqueFilter());
        });
        log.debug("Updated disorder table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }

    public int map(String primaryDomainId) {
        return getDomainToIdMap().get(primaryDomainId);
    }

    public String map(Integer id){
        return getIdToDomainMap().get(id);
    }

    public HashMap<Integer, String> getIdToDomainMap() {
        return idToDomainMap;
    }

    @Override
    public String[] getListAttributes() {
        return Disorder.getListAttributes();
    }

    public HashMap<String, Integer> getDomainToIdMap() {
        return domainToIdMap;
    }

    public NodeFilter getFilter() {
        return allFilter;
    }

    public void setFilter(NodeFilter nf){
        this.allFilter = nf;
    }

    public Iterable<Disorder> findAllByIds(Collection<Integer> ids){
        return disorderRepository.findAllById(ids);
    }

    public Optional<Disorder> findById(Integer id){return disorderRepository.findById(id);}

    public String[] getAttributes() {
        return Disorder.attributes.toArray(String[]::new);
    }
}
