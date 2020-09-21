package de.exbio.reposcapeweb.db.services.controller;

import de.exbio.reposcapeweb.db.entities.nodes.*;
import de.exbio.reposcapeweb.db.services.nodes.*;
import de.exbio.reposcapeweb.filter.NodeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

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

    public NodeFilter filterDrugs() {
        return drugService.getFilter();
    }

    public Iterable<Drug> findDrugs(Collection<Integer> ids) {
        return drugService.findAllByIds(ids);
    }

    public NodeFilter filterGenes() {
        return geneService.getFilter();
    }

    public Iterable<Gene> findGenes(Collection<Integer> ids) {
        return geneService.findAllByIds(ids);
    }

    public NodeFilter filterPathways() {
        return pathwayService.getFilter();
    }

    public Iterable<Pathway> findPathways(Collection<Integer> ids) {
        return pathwayService.findAllByIds(ids);
    }

    public NodeFilter filterProteins() {
        return proteinService.getFilter();
    }

    public Iterable<Protein> findProteins(Collection<Integer> ids) {
        return proteinService.findAllByIds(ids);
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
}
