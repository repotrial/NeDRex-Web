package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DrugHasIndication;
import de.exbio.reposcapeweb.db.entities.edges.ProteinInPathway;
import de.exbio.reposcapeweb.db.entities.edges.ProteinInteractsWithProtein;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinInPathwayRepository;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinInteractsWithProteinRepository;
import de.exbio.reposcapeweb.db.services.nodes.PathwayService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;


@Service
public class ProteinInteractsWithProteinService {

    private final Logger log = LoggerFactory.getLogger(ProteinInteractsWithProteinService.class);

    private final ProteinInteractsWithProteinRepository proteinInteractsWithProteinRepository;

    private final ProteinService proteinService;

    private final boolean directed = true;
    private final HashMap<Integer, HashMap<Integer, Boolean>> edges = new HashMap<>();

    @Autowired
    public ProteinInteractsWithProteinService(PathwayService pathwayService, ProteinService proteinService, ProteinInteractsWithProteinRepository proteinInteractsWithProteinRepository) {
        this.proteinInteractsWithProteinRepository = proteinInteractsWithProteinRepository;
        this.proteinService = proteinService;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, ProteinInteractsWithProtein>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            proteinInteractsWithProteinRepository.deleteAll(proteinInteractsWithProteinRepository.findProteinInteractsWithProteinsByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet())));

        LinkedList<ProteinInteractsWithProtein> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, ProteinInteractsWithProtein> toUpdate = updates.get(UpdateOperation.Alteration);

            proteinInteractsWithProteinRepository.findProteinInteractsWithProteinsByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        proteinInteractsWithProteinRepository.saveAll(toSave);
        log.debug("Updated protein_interacts_with_protein table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }

    public Iterable<ProteinInteractsWithProtein> findAll() {
        return proteinInteractsWithProteinRepository.findAll();
    }

    public void importEdges() {
        findAll().forEach(edge -> {
            importEdge(edge.getPrimaryIds());
        });
    }

    private void importEdge(PairId edge) {
        if (!edges.containsKey(edge.getId1()))
            edges.put(edge.getId1(), new HashMap<>());
        edges.get(edge.getId1()).put(edge.getId2(), true);

        if (!edges.containsKey(edge.getId2()))
            edges.put(edge.getId2(), new HashMap<>());
        edges.get(edge.getId2()).put(edge.getId1(), !directed);
    }

    public boolean isEdge(PairId edge) {
        return isEdge(edge.getId1(), edge.getId2());
    }

    public boolean isEdge(int id1, int id2) {
        try {
            return edges.get(id1).get(id2);
        } catch (NullPointerException e) {
            return false;
        }
    }



    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(proteinService.map(ids.getFirst()), proteinService.map(ids.getSecond()));
    }
}
