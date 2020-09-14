package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DisorderIsADisorder;
import de.exbio.reposcapeweb.db.entities.edges.DrugHasTargetGene;
import de.exbio.reposcapeweb.db.entities.edges.DrugHasTargetProtein;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.DrugHasTargetGeneRepository;
import de.exbio.reposcapeweb.db.repositories.edges.DrugHasTargetProteinRepository;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

@Service
public class DrugHasTargetService {
    private final Logger log = LoggerFactory.getLogger(DrugHasTargetService.class);

    private final DrugHasTargetProteinRepository drugHasTargetProteinRepository;
    private final DrugHasTargetGeneRepository drugHasTargetGeneRepository;

    private final DrugService drugService;

    private final ProteinService proteinService;

    private final boolean directed = true;
    private final HashMap<Integer, HashMap<Integer, Boolean>> proteinEdges = new HashMap<>();
    private final HashMap<Integer, HashMap<Integer, Boolean>> geneEdges = new HashMap<>();

    private final DataSource dataSource;
    private final String clearQuery = "DELETE FROM drug_has_target_gene";
    private final String generationQuery = "INSERT IGNORE INTO drug_has_target_gene SELECT drug_has_target_protein.id_1, protein_encoded_by.id_2 FROM protein_encoded_by INNER JOIN drug_has_target_protein ON protein_encoded_by.id_1=drug_has_target_protein.id_2;";
    private PreparedStatement clearPs = null;
    private PreparedStatement generationPs = null;

    @Autowired
    public DrugHasTargetService(DrugService drugService, ProteinService proteinService, DrugHasTargetProteinRepository drugHasTargetProteinRepository, DrugHasTargetGeneRepository drugHasTargetGeneRepository, DataSource dataSource) {
        this.drugService = drugService;
        this.drugHasTargetProteinRepository = drugHasTargetProteinRepository;
        this.drugHasTargetGeneRepository = drugHasTargetGeneRepository;
        this.proteinService = proteinService;
        this.dataSource = dataSource;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, DrugHasTargetProtein>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            drugHasTargetProteinRepository.deleteAll(drugHasTargetProteinRepository.findDrugHasTargetsByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet())));

        LinkedList<DrugHasTargetProtein> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, DrugHasTargetProtein> toUpdate = updates.get(UpdateOperation.Alteration);

            drugHasTargetProteinRepository.findDrugHasTargetsByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        drugHasTargetProteinRepository.saveAll(toSave);
        log.debug("Updated drug_has_target(_protein) table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        log.debug("Deriving entries for drug_has_target_gene.");
        if (!generateGeneEntries())
            return false;
        log.debug("Derived " + drugHasTargetGeneRepository.count() + " drug -> gene relations.");
        return true;
    }

    public boolean generateGeneEntries() {
        log.debug("Generating entries for drug_has_target_gene from drug_has_target(_protein).");
        try (Connection con = dataSource.getConnection()) {
            clearPs = con.prepareCall(clearQuery);
            generationPs = con.prepareStatement(generationQuery);
            clearPs.executeUpdate();
            generationPs.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }


    public Iterable<DrugHasTargetGene> findAllGenes() {
        return drugHasTargetGeneRepository.findAll();
    }

    public void importEdges() {
        findAllGenes().forEach(edge -> {
            importGeneEdge(edge.getPrimaryIds());
        });
        findAllGenes().forEach(edge -> {
            importGeneEdge(edge.getPrimaryIds());
        });

        findAllProteins().forEach(edge -> {
            importProteinEdge(edge.getPrimaryIds());
        });
        findAllProteins().forEach(edge -> {
            importProteinEdge(edge.getPrimaryIds());
        });
    }

    private void importGeneEdge(PairId edge) {
        if (!geneEdges.containsKey(edge.getId1()))
            geneEdges.put(edge.getId1(), new HashMap<>());
        geneEdges.get(edge.getId1()).put(edge.getId2(), true);

        if (!geneEdges.containsKey(edge.getId2()))
            geneEdges.put(edge.getId2(), new HashMap<>());
        geneEdges.get(edge.getId2()).put(edge.getId1(), !directed);
    }

    public boolean isGeneEdge(PairId edge) {
        return isGeneEdge(edge.getId1(), edge.getId2());
    }

    public boolean isGeneEdge(int id1, int id2) {
        try {
            return geneEdges.get(id1).get(id2);
        } catch (NullPointerException e) {
            return false;
        }
    }


    public Iterable<DrugHasTargetProtein> findAllProteins() {
        return drugHasTargetProteinRepository.findAll();
    }

    private void importProteinEdge(PairId edge) {
        if (!proteinEdges.containsKey(edge.getId1()))
            proteinEdges.put(edge.getId1(), new HashMap<>());
        proteinEdges.get(edge.getId1()).put(edge.getId2(), true);

        if (!proteinEdges.containsKey(edge.getId2()))
            proteinEdges.put(edge.getId2(), new HashMap<>());
        proteinEdges.get(edge.getId2()).put(edge.getId1(), !directed);
    }

    public boolean isProteinEdge(PairId edge) {
        return isProteinEdge(edge.getId1(), edge.getId2());
    }

    public boolean isProteinEdge(int id1, int id2) {
        try {
            return proteinEdges.get(id1).get(id2);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(drugService.map(ids.getFirst()), proteinService.map(ids.getSecond()));
    }
}
