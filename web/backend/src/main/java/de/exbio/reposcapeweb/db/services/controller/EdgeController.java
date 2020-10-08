package de.exbio.reposcapeweb.db.services.controller;

import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.db.services.edges.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;


@Service
public class EdgeController {

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



    public boolean isEdge(int edgeId, int node1, int node2, Integer k1, Integer k2) {
        if (Graphs.getNodesfromEdge(edgeId).first == node1)
            return isEdgeFrom(edgeId, node1, node2, k1, k2);
        return isEdgeFrom(edgeId, node2, node1, k2, k1);
    }

    public boolean isEdgeFrom(int edgeId, int node1, int node2, Integer k1, Integer k2) {
        switch (Graphs.getEdge(edgeId)) {
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

    public HashSet<Integer> getEdges(int edgeId, int firstType, Integer node) {
        if (Graphs.getNodesfromEdge(edgeId).first == firstType)
            return getEdgesFrom(edgeId, node);
        return getEdgesTo(edgeId, node);
    }

    private HashSet<Integer> getEdgesFrom(int edgeId, Integer node) {
        switch (Graphs.getEdge(edgeId)) {
            case "GeneAssociatedWithDisorder":
                return getGenesAssociatedWithDisorderFrom(node);
            case "DrugHasTargetGene":
                return getDrugHasTargetGeneFrom(node);
            case "ProteinEncodedBy":
                return getProteinEncodedByFrom(node);
            case "DrugHasIndication":
                return getDrugHasIndicationFrom(node);
            case "DrugHasTargetProtein":
                return getDrugHasTargetProteinFrom(node);
            case "ProteinInteractsWithProtein":
                return getProteinInteractsWithProtein(node);
            case "ProteinInPathway":
                return getProteinInPathwayFrom(node);
            case "ProteinAssociatedWithDisorder":
                return getProteinAssociatedWithDisorderFrom(node);
            case "DisorderIsADisorder":
                return getDisorderIsADisorder(node);
            case "DisorderComorbidWithDisorder":
                return getDisorderComorbidWithDisorder(node);
        }
        return null;
    }

    private HashSet<Integer> getEdgesTo(int edgeId, Integer node) {
        switch (Graphs.getEdge(edgeId)) {
            case "GeneAssociatedWithDisorder":
                return getGenesAssociatedWithDisorderTo(node);
            case "DrugHasTargetGene":
                return getDrugHasTargetGeneTo(node);
            case "ProteinEncodedBy":
                return getProteinEncodedByTo(node);
            case "DrugHasIndication":
                return getDrugHasIndicationTo(node);
            case "DrugHasTargetProtein":
                return getDrugHasTargetProteinTo(node);
            case "ProteinInteractsWithProtein":
                return getProteinInteractsWithProtein(node);
            case "ProteinInPathway":
                return getProteinInPathwayTo(node);
            case "ProteinAssociatedWithDisorder":
                return getProteinAssociatedWithDisorderTo(node);
            case "DisorderIsADisorder":
                return getDisorderIsADisorder(node);
            case "DisorderComorbidWithDisorder":
                return getDisorderComorbidWithDisorder(node);
        }
        return null;
    }


}
