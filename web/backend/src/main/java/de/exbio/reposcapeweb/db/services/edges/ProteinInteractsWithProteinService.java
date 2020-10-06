package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.ProteinInteractsWithProtein;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.GeneInteractsWithGeneRepository;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinInteractsWithProteinRepository;
import de.exbio.reposcapeweb.db.services.nodes.PathwayService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ProteinInteractsWithProteinService {

    private final Logger log = LoggerFactory.getLogger(ProteinInteractsWithProteinService.class);

    private final ProteinInteractsWithProteinRepository proteinInteractsWithProteinRepository;
    private final GeneInteractsWithGeneRepository geneInteractsWithGeneRepository;

    private final ProteinService proteinService;

    private final boolean directed = true;
    private final HashMap<Integer, HashMap<Integer, Boolean>> edges = new HashMap<>();

    private final DataSource dataSource;
    private final String clearQuery = "DELETE FROM gene_interacts_with_gene";
    private final String generationQuery = "INSERT IGNORE INTO gene_interacts_with_gene SELECT enc1.id_2, enc2.id_2 FROM protein_interacts_with_protein ppi INNER JOIN protein_encoded_by enc1 ON ppi.id_1=enc1.id_1 INNER JOIN protein_encoded_by enc2 on ppi.id_2=enc2.id_1;";
    private PreparedStatement clearPs = null;
    private PreparedStatement generationPs = null;
    @Autowired
    public ProteinInteractsWithProteinService(ProteinService proteinService, ProteinInteractsWithProteinRepository proteinInteractsWithProteinRepository, GeneInteractsWithGeneRepository geneInteractsWithGeneRepository, DataSource dataSource) {
        this.proteinInteractsWithProteinRepository = proteinInteractsWithProteinRepository;
        this.geneInteractsWithGeneRepository=geneInteractsWithGeneRepository;
        this.proteinService = proteinService;
        this.dataSource = dataSource;
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
        log.debug("Deriving entries for gene_interacts_with_gene.");
        if (!generateGeneEntries())
            return false;
        log.debug("Derived " + geneInteractsWithGeneRepository.count() + " gene -> gene relations.");

        return true;
    }

    public boolean generateGeneEntries() {
        log.debug("Generating entries for gene_interacts_with_gene from drug_has_target(_protein).");
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

    public HashSet<Integer> getEdges(int id){
        return edges.get(id).entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));
    }



    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(proteinService.map(ids.getFirst()), proteinService.map(ids.getSecond()));
    }
}
