package de.exbio.reposcapeweb.db.services.controller;

import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
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
        switch (type) {
            case "disorder":
                return filterDisorder();
            case "drug":
                return filterDrugs();
            case "gene":
                return filterGenes();
            case "pathway":
                return filterPathways();
            case "protein":
                return filterProteins();
        }
        return null;
    }

    public String[] getListAttributes(Integer typeId) {
        switch (Graphs.getNode(typeId)) {
            case "disorder":
                return disorderService.getListAttributes();
            case "drug":
                return drugService.getListAttributes();
            case "gene":
                return geneService.getListAttributes();
            case "pathway":
                return pathwayService.getListAttributes();
            case "protein":
                return proteinService.getListAttributes();
        }
        return null;
    }

    public LinkedList<HashMap<String, Object>> nodesToAttributeList(Integer typeId, Set<Integer> ids, HashSet<String> attributes, HashMap<String, HashMap<Integer, Object>> custom) {
        LinkedList<HashMap<String, Object>> out = new LinkedList<>();
        switch (Graphs.getNode(typeId)) {
            case "disorder":
                findDisorders(ids).forEach(d -> out.add(d.getAsMap(attributes)));
                break;
            case "drug":
                findDrugs(ids).forEach(d -> out.add(d.getAsMap(attributes)));
                break;
            case "gene":
                findGenes(ids).forEach(d -> out.add(d.getAsMap(attributes)));
                break;
            case "pathway":
                findPathways(ids).forEach(d -> out.add(d.getAsMap(attributes)));
                break;
            case "protein":
                findProteins(ids).forEach(d -> out.add(d.getAsMap(attributes)));
                break;
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
        switch (Graphs.getNode(typeId)) {
            case "disorder":
                return findDisorder(id).getAsMap();
            case "drug":
                return findDrug(id).getAsMap();
            case "gene":
                return findGene(id).getAsMap();
            case "pathway":
                return findPathway(id).getAsMap();
            case "protein":
                return findProtein(id).getAsMap();
        }
        return null;
    }

    public String[] getAttributes(Integer typeId) {
        switch (Graphs.getNode(typeId)) {
            case "disorder":
                return Disorder.allAttributes;
            case "drug":
                return Drug.allAttributes;
            case "gene":
                return Gene.allAttributes;
            case "pathway":
                return Pathway.allAttributes;
            case "protein":
                return Protein.allAttributes;
        }
        return null;
    }

    public String[] getAttributeTypes(Integer typeId) {
        switch (Graphs.getNode(typeId)) {
            case "disorder":
                return Disorder.allAttributeTypes;
            case "drug":
                return Drug.allAttributeTypes;
            case "gene":
                return Gene.allAttributeTypes;
            case "pathway":
                return Pathway.allAttributeTypes;
            case "protein":
                return Protein.allAttributeTypes;
        }
        return null;
    }

    public String getDomainId(Integer typeId, Integer id) {
        switch (Graphs.getNode(typeId)) {
            case "disorder":
                return disorderService.map(id);
            case "drug":
                return drugService.map(id);
            case "gene":
                return geneService.map(id);
            case "pathway":
                return pathwayService.map(id);
            case "protein":
                return proteinService.map(id);
        }
        return null;
    }

    public String getName(Integer typeId, Integer id) {
        switch (Graphs.getNode(typeId)) {
            case "disorder":
                return disorderService.getName(id);
            case "drug":
                return drugService.getName(id);
            case "gene":
                return geneService.getName(id);
            case "pathway":
                return pathwayService.getName(id);
            case "protein":
                return proteinService.getName(id);
        }
        return null;
    }

    public Boolean[] getIdAttributes(Integer typeId) {
        switch (Graphs.getNode(typeId)) {
            case "disorder":
                return Disorder.idAttributes;
            case "drug":
                return Drug.idAttributes;
            case "gene":
                return Gene.idAttributes;
            case "pathway":
                return Pathway.idAttributes;
            case "protein":
                return Protein.idAttributes;
        }
        return null;
    }

    public Iterable findAll(Integer typeId) {
        switch (Graphs.getNode(typeId)) {
            case "disorder":
                return disorderService.findAll();
            case "drug":
                return drugService.findAll();
            case "gene":
                return geneService.findAll();
            case "pathway":
                return pathwayService.findAll();
            case "protein":
                return proteinService.findAll();
        }
        return null;
    }

    public Long getNodeCount(String type) {
        switch (type) {
            case "disorder":
                return disorderService.getCount();
            case "drug":
                return drugService.getCount();
            case "gene":
                return geneService.getCount();
            case "pathway":
                return pathwayService.getCount();
            case "protein":
                return proteinService.getCount();
        }
        return null;
    }
}
