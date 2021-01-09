package de.exbio.reposcapeweb.db.services.controller;

import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.db.entities.RepoTrialEntity;
import de.exbio.reposcapeweb.db.entities.edges.ProteinAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.nodes.*;
import de.exbio.reposcapeweb.db.services.nodes.*;
import de.exbio.reposcapeweb.filter.NodeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NodeController {


    private final DisorderService disorderService;
    private final DrugService drugService;
    private final GeneService geneService;
    private final PathwayService pathwayService;
    private final ProteinService proteinService;


    @Autowired
    public NodeController(
            DisorderService disorderService,
            DrugService drugService,
            GeneService geneService,
            PathwayService pathwayService,
            ProteinService proteinService
    ) {
        this.disorderService = disorderService;
        this.drugService = drugService;
        this.geneService = geneService;
        this.pathwayService = pathwayService;
        this.proteinService = proteinService;
    }


    public NodeFilter filterDisorder() {
        return disorderService.getFilter();
    }

    public Iterable<Disorder> findDisorders(Collection<Integer> ids) {
        return disorderService.findAllByIds(ids);
    }

    public Disorder findDisorder(Integer id) {
        return disorderService.findById(id).orElseGet(null);
    }

    public NodeFilter filterDrugs() {
        return drugService.getFilter();
    }

    public Iterable<Drug> findDrugs(Collection<Integer> ids) {
        return drugService.findAllByIds(ids);
    }

    public Drug findDrug(Integer id) {
        return drugService.findById(id).orElseGet(null);
    }

    public NodeFilter filterGenes() {
        return geneService.getFilter();
    }

    public Iterable<Gene> findGenes(Collection<Integer> ids) {
        return geneService.findAllByIds(ids);
    }

    public Gene findGene(Integer id) {
        return geneService.findById(id).orElseGet(null);
    }

    public NodeFilter filterPathways() {
        return pathwayService.getFilter();
    }

    public Iterable<Pathway> findPathways(Collection<Integer> ids) {
        return pathwayService.findAllByIds(ids);
    }

    public Pathway findPathway(Integer id) {
        return pathwayService.findById(id).orElseGet(null);
    }

    public NodeFilter filterProteins() {
        return proteinService.getFilter();
    }

    public Iterable<Protein> findProteins(Collection<Integer> ids) {
        return proteinService.findAllByIds(ids);
    }

    public Protein findProtein(Integer id) {
        return proteinService.findById(id).orElseGet(null);
    }


    public NodeFilter getFilter(String type) {
        return switch (type) {
            case "disorder" -> filterDisorder();
            case "drug" -> filterDrugs();
            case "gene" -> filterGenes();
            case "pathway" -> filterPathways();
            case "protein" -> filterProteins();
            default -> null;
        };
    }

    public Iterable findByIds(String type, Collection<Integer> ids) {
        return switch (type) {
            case "disorder" -> findDisorders(ids);
            case "drug" -> findDrugs(ids);
            case "gene" -> findGenes(ids);
            case "pathway" -> findPathways(ids);
            case "protein" -> findProteins(ids);
            default -> null;
        };
    }

    public String[] getListAttributes(Integer typeId) {
        return switch (Graphs.getNode(typeId)) {
            case "disorder" -> disorderService.getListAttributes();
            case "drug" -> drugService.getListAttributes();
            case "gene" -> geneService.getListAttributes();
            case "pathway" -> pathwayService.getListAttributes();
            case "protein" -> proteinService.getListAttributes();
            default -> null;
        };
    }

    public LinkedList<HashMap<String, Object>> nodesToAttributeList(Integer typeId, Set<Integer> ids, HashSet<String> attributes, HashMap<String, HashMap<Integer, Object>> custom) {
        LinkedList<HashMap<String, Object>> out = new LinkedList<>();
        switch (Graphs.getNode(typeId)) {
            case "disorder" -> findDisorders(ids).forEach(d -> out.add(d.getAsMap(attributes)));
            case "drug" -> findDrugs(ids).forEach(d -> out.add(d.getAsMap(attributes)));
            case "gene" -> findGenes(ids).forEach(d -> out.add(d.getAsMap(attributes)));
            case "pathway" -> findPathways(ids).forEach(d -> out.add(d.getAsMap(attributes)));
            case "protein" -> findProteins(ids).forEach(d -> out.add(d.getAsMap(attributes)));
        }
        if (custom != null)
            out.forEach(node -> {
                int id = (int) node.get("id");
                custom.forEach((attr, values) -> {
                    if (values.containsKey(id))
                        node.put(attr, values.get(id));
                });
            });
        return out;
    }

    public HashMap<String, Object> nodeToAttributeList(Integer typeId, Integer id) {
        return switch (Graphs.getNode(typeId)) {
            case "disorder" -> findDisorder(id).getAsMap();
            case "drug" -> findDrug(id).getAsMap();
            case "gene" -> findGene(id).getAsMap();
            case "pathway" -> findPathway(id).getAsMap();
            case "protein" -> findProtein(id).getAsMap();
            default -> null;
        };
    }

    public String[] getAttributes(Integer typeId) {
        return switch (Graphs.getNode(typeId)) {
            case "disorder" -> Disorder.allAttributes;
            case "drug" -> Drug.allAttributes;
            case "gene" -> Gene.allAttributes;
            case "pathway" -> Pathway.allAttributes;
            case "protein" -> Protein.allAttributes;
            default -> null;
        };
    }

    public String[] getAttributeLabels(Integer typeId) {
        return switch (Graphs.getNode(typeId)) {
            case "disorder" -> Disorder.attributeLabels;
            case "drug" -> Drug.attributeLabels;
            case "gene" -> Gene.attributeLabels;
            case "pathway" -> Pathway.attributeLabels;
            case "protein" -> Protein.attributeLabels;
            default -> null;
        };
    }

    public String[] getAttributeTypes(Integer typeId) {
        return switch (Graphs.getNode(typeId)) {
            case "disorder" -> Disorder.allAttributeTypes;
            case "drug" -> Drug.allAttributeTypes;
            case "gene" -> Gene.allAttributeTypes;
            case "pathway" -> Pathway.allAttributeTypes;
            case "protein" -> Protein.allAttributeTypes;
            default -> null;
        };
    }

    public String getDomainId(Integer typeId, Integer id) {
        return switch (Graphs.getNode(typeId)) {
            case "disorder" -> disorderService.map(id);
            case "drug" -> drugService.map(id);
            case "gene" -> geneService.map(id);
            case "pathway" -> pathwayService.map(id);
            case "protein" -> proteinService.map(id);
            default -> null;
        };
    }

    public int getId(String type, String id) {
        return switch (type) {
            case "disorder" -> disorderService.map(id);
            case "drug" -> drugService.map(id);
            case "gene" -> geneService.map(id);
            case "pathway" -> pathwayService.map(id);
            case "protein" -> proteinService.map(id);
            default -> -1;
        };
    }

    public String getName(Integer typeId, Integer id) {
        return switch (Graphs.getNode(typeId)) {
            case "disorder" -> disorderService.getName(id);
            case "drug" -> drugService.getName(id);
            case "gene" -> geneService.getName(id);
            case "pathway" -> pathwayService.getName(id);
            case "protein" -> proteinService.getName(id);
            default -> null;
        };
    }

    public Boolean[] getIdAttributes(Integer typeId) {
        return switch (Graphs.getNode(typeId)) {
            case "disorder" -> Disorder.idAttributes;
            case "drug" -> Drug.idAttributes;
            case "gene" -> Gene.idAttributes;
            case "pathway" -> Pathway.idAttributes;
            case "protein" -> Protein.idAttributes;
            default -> null;
        };
    }

    public Iterable findAll(Integer typeId) {
        return switch (Graphs.getNode(typeId)) {
            case "disorder" -> disorderService.findAll();
            case "drug" -> drugService.findAll();
            case "gene" -> geneService.findAll();
            case "pathway" -> pathwayService.findAll();
            case "protein" -> proteinService.findAll();
            default -> null;
        };
    }

    public Long getNodeCount(String type) {
        return switch (type) {
            case "disorder" -> disorderService.getCount();
            case "drug" -> drugService.getCount();
            case "gene" -> geneService.getCount();
            case "pathway" -> pathwayService.getCount();
            case "protein" -> proteinService.getCount();
            default -> null;
        };
    }

    public HashMap<String, String> getAttributeLabelMap(String type) {
        return switch (type) {
            case "disorder" -> Disorder.label2NameMap;
            case "drug" -> Drug.label2NameMap;
            case "gene" -> Gene.label2NameMap;
            case "pathway" -> Pathway.label2NameMap;
            case "protein" -> Protein.label2NameMap;
            default -> null;
        };
    }

    public void setUp() {
        Disorder.setUpNameMaps();
        Drug.setUpNameMaps();
        Gene.setUpNameMaps();
        Pathway.setUpNameMaps();
        Protein.setUpNameMaps();
    }

    public RepoTrialEntity getNode(String type, Integer n) {
        return switch (type) {
            case "disorder" -> disorderService.findById(n).orElse(null);
            case "drug" -> drugService.findById(n).orElse(null);
            case "gene" -> geneService.findById(n).orElse(null);
            case "pathway" -> pathwayService.findById(n).orElse(null);
            case "protein" -> proteinService.findById(n).orElse(null);
            default -> null;
        };
    }
}
