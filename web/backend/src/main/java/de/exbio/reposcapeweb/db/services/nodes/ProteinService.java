package de.exbio.reposcapeweb.db.services.nodes;

import de.exbio.reposcapeweb.db.entities.nodes.Disorder;
import de.exbio.reposcapeweb.db.entities.nodes.Protein;
import de.exbio.reposcapeweb.db.repositories.nodes.ProteinRepository;
import de.exbio.reposcapeweb.db.services.NodeService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.filter.NodeFilter;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class ProteinService extends NodeService {
    private final Logger log = LoggerFactory.getLogger(DrugService.class);
    private final ProteinRepository proteinRepository;

    private HashMap<Integer, Pair<String, String>> idToDomainMap = new HashMap<>();
    private HashMap<String, Integer> domainToIdMap = new HashMap<>();

    private NodeFilter allFilter;

    @Autowired
    public ProteinService(ProteinRepository proteinRepository) {
        this.proteinRepository = proteinRepository;
        allFilter = new NodeFilter();
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
            allFilter.removeByNodeIds(updates.get(UpdateOperation.Deletion).values().stream().map(Protein::getId).collect(Collectors.toSet()));
        }

        LinkedList<Protein> toSave = updates.get(UpdateOperation.Insertion).values().stream().filter(p -> p.taxid > -1).collect(Collectors.toCollection( LinkedList::new));
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<String, Protein> toUpdate = updates.get(UpdateOperation.Alteration);

            proteinRepository.findAllByPrimaryDomainIdIn(new HashSet<>(toUpdate.keySet())).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryDomainId()));
                if (d.taxid > -1)
                    toSave.add(d);
            });
        }
        proteinRepository.saveAll(toSave).forEach(d -> {
            idToDomainMap.put(d.getId(), new Pair<>(d.getPrimaryDomainId(), d.getDisplayName()));
            domainToIdMap.put(d.getPrimaryDomainId(), d.getId());
            allFilter.add(d.toDistinctFilter(), d.toUniqueFilter());
        });
        log.debug("Updated protein table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }


    public int map(String primaryDomainId) {
        return getDomainToIdMap().get(primaryDomainId);
    }

    public String map(Integer id) {
        return getIdToDomainMap().get(id).first;
    }

    public HashMap<Integer, Pair<String, String>> getIdToDomainMap() {
        return idToDomainMap;
    }

    @Override
    public String[] getListAttributes() {
        return Protein.getListAttributes();
    }

    public HashMap<String, Integer> getDomainToIdMap() {
        return domainToIdMap;
    }

    public NodeFilter getFilter() {
        return allFilter;
    }

    public void setFilter(NodeFilter nf) {
        this.allFilter = nf;
    }

    public Iterable<Protein> findAllByIds(Collection<Integer> ids) {
        return proteinRepository.findAllById(ids);
    }

    public Optional<Protein> findById(Integer id) {
        return proteinRepository.findById(id);
    }

    public String[] getAttributes() {
        return Protein.attributes.toArray(String[]::new);
    }

    public Iterable<Protein> findAll(){
        return proteinRepository.findAll();
    }

    @Override
    public String getName(int id) {
        return idToDomainMap.get(id).second;
    }
}
