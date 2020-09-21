package de.exbio.reposcapeweb.db.services.controller;

import de.exbio.reposcapeweb.db.services.edges.*;
import de.exbio.reposcapeweb.utils.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;


@Service
public class EdgeController {

    private HashMap<String, HashMap<String, LinkedList<String>>> nodes2edge;
    private HashMap<String, Pair<String, String>> edge2node;

    private final DisorderComorbidWithDisorderService disorderComorbidWithDisorderService;
    private final DisorderIsADisorderService disorderIsADisorderService;
    private final DrugHasIndicationService drugHasIndicationService;
    private final DrugHasTargetService drugHasTargetService;
    private final AssociatedWithDisorderService associatedWithDisorderService;
    private final ProteinEncodedByService proteinEncodedByService;
    private final ProteinInPathwayService proteinInPathwayService;
    private final ProteinInteractsWithProteinService proteinInteractsWithProteinService;


    @Autowired
    public EdgeController(
            DisorderComorbidWithDisorderService disorderComorbidWithDisorderService,
            DisorderIsADisorderService disorderIsADisorderService,
            DrugHasIndicationService drugHasIndicationService,
            DrugHasTargetService drugHasTargetService,
            AssociatedWithDisorderService associatedWithDisorderService,
            ProteinEncodedByService proteinEncodedByService,
            ProteinInPathwayService proteinInPathwayService,
            ProteinInteractsWithProteinService proteinInteractsWithProteinService

    ) {
        this.disorderComorbidWithDisorderService = disorderComorbidWithDisorderService;
        this.disorderIsADisorderService = disorderIsADisorderService;
        this.drugHasIndicationService = drugHasIndicationService;
        this.drugHasTargetService = drugHasTargetService;
        this.associatedWithDisorderService = associatedWithDisorderService;
        this.proteinEncodedByService = proteinEncodedByService;
        this.proteinInPathwayService = proteinInPathwayService;
        this.proteinInteractsWithProteinService = proteinInteractsWithProteinService;
        initNameMap();
    }

    private void initNameMap() {

        edge2node = new HashMap<>();

        edge2node.put("GeneAssociatedWithDisorder", new Pair<>("gene", "disorder"));
        edge2node.put("DrugHasTargetGene", new Pair<>("drug", "gene"));
        edge2node.put("ProteinEncodedBy", new Pair<>("protein", "gene"));

        edge2node.put("DrugHasIndication", new Pair<>("drug", "disorder"));
        edge2node.put("DrugHasTargetProtein", new Pair<>("drug", "protein"));

        edge2node.put("ProteinInteractsWithProtein", new Pair<>("protein", "protein"));
        edge2node.put("ProteinInPathway", new Pair<>("protein", "pathway"));
        edge2node.put("ProteinAssociatedWithDisorder", new Pair<>("protein", "disorder"));

        edge2node.put("DisorderIsADisorder", new Pair<>("disorder", "disorder"));
        edge2node.put("DisorderComorbidWithDisorder", new Pair<>("disorder", "disorder"));

        nodes2edge = new HashMap<>();
        edge2node.forEach((k, v) -> {
            if (!nodes2edge.containsKey(v.first))
                nodes2edge.put(v.first, new HashMap<>());
            if (!nodes2edge.get(v.first).containsKey(v.second))
                nodes2edge.get(v.first).put(v.second, new LinkedList<>(Collections.singletonList(k)));
            else
                nodes2edge.get(v.first).get(v.second).add(k);

            if (!nodes2edge.containsKey(v.second))
                nodes2edge.put(v.second, new HashMap<>());
            if (!nodes2edge.get(v.second).containsKey(v.first))
                nodes2edge.get(v.second).put(v.first, new LinkedList<>(Collections.singletonList(k)));
            else
                nodes2edge.get(v.second).get(v.first).add(k);

        });
    }


    public HashSet<Integer> getGenesAssociatedWithDisorderFrom(int id) {
        return associatedWithDisorderService.getGeneEdgesFrom(id);
    }

    public boolean isGenesAssociatedWithDisorderFrom(int id1, int id2) {
        return associatedWithDisorderService.isGeneEdgeFrom(id1, id2);
    }

    public HashSet<Integer> getProteinAssociatedWithDisorderFrom(int id) {
        return associatedWithDisorderService.getProteinEdgesFrom(id);
    }

    public boolean isProteinAssociatedWithDisorderFrom(int id1, int id2) {
        return associatedWithDisorderService.isProteinEdgeFrom(id1, id2);
    }

    public boolean isProteinAssociatedWithDisorderTo(int id1, int id2) {
        return associatedWithDisorderService.isProteinEdgeTo(id1, id2);
    }

    public HashSet<Integer> getGenesAssociatedWithDisorderTo(int id) {
        return associatedWithDisorderService.getGeneEdgesTo(id);
    }

    public boolean isGenesAssociatedWithDisorderTo(int id1, int id2) {
        return associatedWithDisorderService.isGeneEdgeTo(id1, id2);
    }

    public HashSet<Integer> getProteinAssociatedWithDisorderTo(int id) {
        return associatedWithDisorderService.getProteinEdgesTo(id);
    }

    public boolean getProteinAssociatedWithDisorderTo(int id1, int id2) {
        return associatedWithDisorderService.isProteinEdgeTo(id1, id2);
    }


    public HashSet<Integer> getDisorderComorbidWithDisorder(int id) {
        return disorderComorbidWithDisorderService.getEdges(id);
    }

    public boolean isDisorderComorbidWithDisorder(int id1, int id2) {
        return disorderComorbidWithDisorderService.isEdge(id1, id2);
    }

    public HashSet<Integer> getDisorderIsADisorder(int id) {
        return disorderIsADisorderService.getEdges(id);
    }

    public boolean isDisorderIsADisorder(int id1, int id2) {
        return disorderIsADisorderService.isEdge(id1, id2);
    }

    public HashSet<Integer> getDrugHasIndicationFrom(int id) {
        return drugHasIndicationService.getEdgesFrom(id);
    }

    public boolean isDrugHasIndicationFrom(int id1, int id2) {
        return drugHasIndicationService.isEdgeFrom(id1, id2);
    }

    public HashSet<Integer> getDrugHasIndicationTo(int id) {
        return drugHasIndicationService.getEdgesTo(id);
    }

    public boolean isDrugHasIndicationTo(int id1, int id2) {
        return drugHasIndicationService.isEdgeTo(id1, id2);
    }


    public HashSet<Integer> getDrugHasTargetGeneFrom(int id) {
        return drugHasTargetService.getGeneEdgesFrom(id);
    }

    public boolean isDrugHasTargetGeneFrom(int id1, int id2) {
        return drugHasTargetService.isGeneEdgeFrom(id1, id2);
    }

    public HashSet<Integer> getDrugHasTargetGeneTo(int id) {
        return drugHasTargetService.getGeneEdgesTo(id);
    }

    public boolean isDrugHasTargetGeneTo(int id1, int id2) {
        return drugHasTargetService.isGeneEdgeTo(id1, id2);
    }

    public HashSet<Integer> getDrugHasTargetProteinFrom(int id) {
        return drugHasTargetService.getProteinEdgesFrom(id);
    }

    public boolean isDrugHasTargetProteinFrom(int id1, int id2) {
        return drugHasTargetService.isProteinEdgeFrom(id1, id2);
    }

    public HashSet<Integer> getDrugHasTargetProteinTo(int id) {
        return drugHasTargetService.getProteinEdgesTo(id);
    }

    public boolean isDrugHasTargetProteinTo(int id1, int id2) {
        return drugHasTargetService.isProteinEdgeTo(id1, id2);
    }


    public HashSet<Integer> getProteinEncodedByTo(int id) {
        return proteinEncodedByService.getEdgesTo(id);
    }

    public boolean isProteinEncodedByTo(int id1, int id2) {
        return proteinEncodedByService.isEdgeTo(id1, id2);
    }

    public HashSet<Integer> getProteinEncodedByFrom(int id) {
        return proteinEncodedByService.getEdgesTo(id);
    }

    public boolean isProteinEncodedByFrom(int id1, int id2) {
        return proteinEncodedByService.isEdgeFrom(id1, id2);
    }


    public HashSet<Integer> getProteinInPathwayTo(int id) {
        return proteinInPathwayService.getEdgesTo(id);
    }

    public boolean isProteinInPathwayTo(int id1, int id2) {
        return proteinInPathwayService.isEdgeTo(id1, id2);
    }

    public HashSet<Integer> getProteinInPathwayFrom(int id) {
        return proteinEncodedByService.getEdgesFrom(id);
    }

    public boolean isProteinInPathwayFrom(int id1, int id2) {
        return proteinEncodedByService.isEdgeFrom(id1, id2);
    }

    public HashSet<Integer> getProteinInteractsWithProtein(int id) {
        return proteinInteractsWithProteinService.getEdges(id);
    }

    public boolean isProteinInteractsWithProtein(int id1, int id2) {
        return proteinInteractsWithProteinService.isEdge(id1, id2);
    }


    public LinkedList<String> mapEdgeName(String node1, String node2) {
        try {
            return nodes2edge.get(node1).get(node2);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Pair<String, String> getEdgeName(String edge) {
        return edge2node.get(edge);
    }

    public boolean isEdge(String edgeName, String node1, String node2, Integer k1, Integer k2) {
        if (edge2node.get(edgeName).first.equals(node1))
            return isEdgeFrom(edgeName, node1, node2, k1, k2);
        return isEdgeFrom(edgeName, node2, node1, k2, k1);
    }

    public boolean isEdgeFrom(String edgeName, String node1, String node2, Integer k1, Integer k2) {
        switch (edgeName) {
            case "GeneAssociatedWithDisorder":
                return isGenesAssociatedWithDisorderFrom(k1, k2);
            case "DrugHasTargetGene":
                return isDrugHasTargetGeneFrom(k1, k2);
            case "ProteinEncodedBy":
                return isProteinEncodedByFrom(k1, k2);
            case "DrugHasIndication":
                return isDrugHasIndicationFrom(k1, k2);
            case "DrugHasTargetProtein":
                return isDrugHasTargetProteinFrom(k1, k2);
            case "ProteinInteractsWithProtein":
                return isProteinInteractsWithProtein(k1, k2);
            case "ProteinInPathway":
                return isProteinInPathwayFrom(k1, k2);
            case "ProteinAssociatedWithDisorder":
                return isProteinAssociatedWithDisorderFrom(k1, k2);
            case "DisorderIsADisorder":
                return isDisorderIsADisorder(k1, k2);
            case "DisorderComorbidWithDisorder":
                return isDisorderComorbidWithDisorder(k1, k2);
        }
        return false;
    }
}
