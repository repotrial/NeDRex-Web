package de.exbio.reposcapeweb.db.services.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Gene;
import de.exbio.reposcapeweb.db.repositories.nodes.GeneRepository;
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
public class GeneService extends NodeService {
    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final GeneRepository geneRepository;

    private HashMap<Integer, Pair<String,String>> idToDomainMap = new HashMap<>();
    private HashMap<String, Integer> domainToIdMap = new HashMap<>();

    private NodeFilter allFilter;

    @Autowired
    public GeneService(GeneRepository geneRepository) {
        this.geneRepository = geneRepository;
        allFilter = new NodeFilter();
    }

    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<String, Gene>> updates) {
        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion)) {
            geneRepository.deleteAll(geneRepository.findAllByPrimaryDomainIdIn(updates.get(UpdateOperation.Deletion).keySet()));
            updates.get(UpdateOperation.Deletion).values().forEach(d -> {
                idToDomainMap.remove(d.getId());
                domainToIdMap.remove(d.getPrimaryDomainId());
            });
            allFilter.removeByNodeIds(updates.get(UpdateOperation.Deletion).values().stream().map(Gene::getId).collect(Collectors.toSet()));

        }

        LinkedList<Gene> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Gene> toUpdate = updates.get(UpdateOperation.Alteration);

            geneRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                toSave.add(d);
            });
        }
        geneRepository.saveAll(toSave).forEach(d -> {
            idToDomainMap.put(d.getId(), new Pair<>(d.getPrimaryDomainId(),d.getDisplayName()));
            domainToIdMap.put(d.getPrimaryDomainId(), d.getId());
            allFilter.add(d.toDistinctFilter(),d.toUniqueFilter());
        });
        log.debug("Updated gene table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
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
        return Gene.getListAttributes();
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

    public Iterable<Gene> findAllByIds(Collection<Integer> ids){
        return geneRepository.findAllById(ids);
    }

    public Optional<Gene> findById(Integer id){return geneRepository.findById(id);}

    public String[] getAttributes() {
        return Gene.sourceAttributes.toArray(String[]::new);
    }

    public Iterable<Gene> findAll(){
        return geneRepository.findAll();
    }

    @Override
    public String getName(int id) {
        return idToDomainMap.get(id).second;
    }

    @Override
    public Long getCount() {
        return geneRepository.count();
    }

}
