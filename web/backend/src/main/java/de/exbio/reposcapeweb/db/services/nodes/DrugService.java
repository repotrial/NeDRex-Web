package de.exbio.reposcapeweb.db.services.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Disorder;
import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import de.exbio.reposcapeweb.db.repositories.nodes.DrugRepository;
import de.exbio.reposcapeweb.db.services.NodeService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.filter.NodeFilter;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DrugService extends NodeService {


    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final DrugRepository drugRepository;

    private HashMap<Integer, Pair<String,String>> idToDomainMap = new HashMap<>();
    private HashMap<String, Integer> domainToIdMap = new HashMap<>();

    private NodeFilter allFilter;

    @Autowired
    public DrugService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
        allFilter = new NodeFilter();
    }

    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<String, Drug>> updates) {
        log.debug("Applying drug table updates.");
        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion)) {
            drugRepository.deleteAll(drugRepository.findAllByPrimaryDomainIdIn((updates.get(UpdateOperation.Deletion).keySet())));
            updates.get(UpdateOperation.Deletion).values().forEach(d -> {
                idToDomainMap.remove(d.getId());
                domainToIdMap.remove(d.getPrimaryDomainId());
            });
            allFilter.removeByNodeIds(updates.get(UpdateOperation.Deletion).values().stream().map(Drug::getId).collect(Collectors.toSet()));

        }

        LinkedList<Drug> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Drug> toUpdate = updates.get(UpdateOperation.Alteration);

            drugRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                toSave.add(d);
            });
        }
        drugRepository.saveAll(toSave).forEach(d -> {
            idToDomainMap.put(d.getId(), new Pair<>(d.getPrimaryDomainId(),d.getDisplayName()));
            domainToIdMap.put(d.getPrimaryDomainId(), d.getId());
            allFilter.add(d.toDistinctFilter(),d.toUniqueFilter());
        });
        log.debug("Updated drug table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }


    public int map(String primaryDomainId) {
        return getDomainToIdMap().get(primaryDomainId);
    }

    public String map(Integer id){
        return getIdToDomainMap().get(id).first;
    }
    public HashMap<Integer, Pair<String,String>> getIdToDomainMap() {
        return idToDomainMap;
    }

    @Override
    public String[] getListAttributes() {
        return Drug.getListAttributes();
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

    public Iterable<Drug> findAllByIds(Collection<Integer> ids){
        return drugRepository.findAllById(ids);
    }
    public Optional<Drug> findById(Integer id){return drugRepository.findById(id);}

    public String[] getAttributes() {
        return Drug.sourceAttributes.toArray(String[]::new);
    }

    public Iterable<Drug> findAll(){
        return drugRepository.findAll();
    }

    @Override
    public String getName(int id) {
        return idToDomainMap.get(id).second;
    }
    @Override
    public Long getCount() {
        return drugRepository.count();
    }

    @Override
    public void readIdDomainMapsFromDb() {
        findAll().forEach(n->{
            domainToIdMap.put(n.getPrimaryDomainId(),n.getId());
            idToDomainMap.put(n.getId(),new Pair<>(n.getPrimaryDomainId(),n.getDisplayName()));
        });
    }


}
