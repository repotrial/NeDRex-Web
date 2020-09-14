package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DrugHasTargetGene;
import de.exbio.reposcapeweb.db.entities.edges.DrugHasTargetProtein;
import de.exbio.reposcapeweb.db.entities.edges.GeneAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.edges.ProteinAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.GeneAssociatedWithDisorderRepository;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinAssociatedWithDisorderRepository;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.services.nodes.GeneService;
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
public class GeneAssociatedWithDisorderService {

    private final Logger log = LoggerFactory.getLogger(GeneAssociatedWithDisorder.class);

    private final GeneAssociatedWithDisorderRepository geneAssociatedWithDisorderRepository;
    private final ProteinAssociatedWithDisorderRepository proteinAssociatedWithDisorderRepository;

    private final GeneService geneService;

    private final DisorderService disorderService;

    private final boolean directed = true;
    private final HashMap<Integer, HashMap<Integer, Boolean>> proteinEdges = new HashMap<>();
    private final HashMap<Integer, HashMap<Integer, Boolean>> geneEdges = new HashMap<>();

    private final DataSource dataSource;
    private final String clearQuery = "DELETE FROM protein_associated_with_disorder";
    private final String generationQuery = "INSERT IGNORE INTO protein_associated_with_disorder (id_1 , id_2, score,asserted_by) SELECT protein_encoded_by.id_1, gene_associated_with_disorder.id_2,gene_associated_with_disorder.score, gene_associated_with_disorder.asserted_by " +
            "FROM protein_encoded_by INNER JOIN gene_associated_with_disorder " +
            "ON protein_encoded_by.id_2=gene_associated_with_disorder.id_1;";
    private PreparedStatement clearPs = null;
    private PreparedStatement generationPs = null;

    @Autowired
    public GeneAssociatedWithDisorderService(DataSource dataSource, GeneService geneService, DisorderService disorderService, GeneAssociatedWithDisorderRepository geneAssociatedWithDisorderRepository, ProteinAssociatedWithDisorderRepository proteinAssociatedWithDisorderRepository) {
        this.geneAssociatedWithDisorderRepository = geneAssociatedWithDisorderRepository;
        this.proteinAssociatedWithDisorderRepository = proteinAssociatedWithDisorderRepository;
        this.disorderService = disorderService;
        this.geneService = geneService;
        this.dataSource = dataSource;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, GeneAssociatedWithDisorder>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            geneAssociatedWithDisorderRepository.deleteAll(geneAssociatedWithDisorderRepository.findGeneAssociatedWithDisorderByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet())));

        LinkedList<GeneAssociatedWithDisorder> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, GeneAssociatedWithDisorder> toUpdate = updates.get(UpdateOperation.Alteration);

            geneAssociatedWithDisorderRepository.findGeneAssociatedWithDisorderByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        geneAssociatedWithDisorderRepository.saveAll(toSave);
        log.debug("Updated gene_associated_with_disorder table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        log.debug("Deriving entries for protein_associated_with_disorder.");
        if (!generateGeneEntries())
            return false;
        log.debug("Derived " + proteinAssociatedWithDisorderRepository.count() + " protein -> disorder relations.");
        return true;
    }


    public boolean generateGeneEntries() {
        log.debug("Generating entries for protein_associated_with_disorder from gene_associated_with_disorder(_protein).");
        try (Connection con = dataSource.getConnection()) {
            clearPs = con.prepareStatement(clearQuery);
            generationPs = con.prepareStatement(generationQuery);
            clearPs.executeUpdate();
            generationPs.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }

    public Iterable<GeneAssociatedWithDisorder> findAllGenes() {
        return geneAssociatedWithDisorderRepository.findAll();
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


    public Iterable<ProteinAssociatedWithDisorder> findAllProteins() {
        return proteinAssociatedWithDisorderRepository.findAll();
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
        return new PairId(geneService.map(ids.getFirst()), disorderService.map(ids.getSecond()));
    }
}
