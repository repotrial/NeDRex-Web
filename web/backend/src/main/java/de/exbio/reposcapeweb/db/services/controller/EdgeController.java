package de.exbio.reposcapeweb.db.services.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.db.entities.edges.*;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.services.edges.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


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
    private final ObjectMapper objectMapper;


    @Autowired
    public EdgeController(
            DisorderComorbidWithDisorderService disorderComorbidWithDisorderService,
            DisorderIsADisorderService disorderIsADisorderService,
            DrugHasIndicationService drugHasIndicationService,
            DrugHasTargetService drugHasTargetService,
            AssociatedWithDisorderService associatedWithDisorderService,
            ProteinEncodedByService proteinEncodedByService,
            ProteinInPathwayService proteinInPathwayService,
            ProteinInteractsWithProteinService proteinInteractsWithProteinService,
            ObjectMapper objectMapper

    ) {
        this.disorderComorbidWithDisorderService = disorderComorbidWithDisorderService;
        this.disorderIsADisorderService = disorderIsADisorderService;
        this.drugHasIndicationService = drugHasIndicationService;
        this.drugHasTargetService = drugHasTargetService;
        this.associatedWithDisorderService = associatedWithDisorderService;
        this.proteinEncodedByService = proteinEncodedByService;
        this.proteinInPathwayService = proteinInPathwayService;
        this.proteinInteractsWithProteinService = proteinInteractsWithProteinService;
        this.objectMapper = objectMapper;
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
        return proteinEncodedByService.getEdgesFrom(id);
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
        return proteinInPathwayService.getEdgesFrom(id);
    }

    public boolean isProteinInPathwayFrom(int id1, int id2) {
        return proteinInPathwayService.isEdgeFrom(id1, id2);
    }

    public HashSet<Integer> getProteinInteractsWithProtein(int id) {
        return proteinInteractsWithProteinService.getProteins(id);
    }


    public boolean isProteinInteractsWithProtein(int id1, int id2) {
        return proteinInteractsWithProteinService.isProteinEdge(id1, id2);
    }

    public boolean isGeneInteractsWithGene(int id1, int id2) {
        return proteinInteractsWithProteinService.isGeneEdge(id1, id2);
    }

    public HashSet<Integer> getGeneInteractsWithGene(int id) {
        return proteinInteractsWithProteinService.getGenes(id);
    }

    public List<DisorderComorbidWithDisorder> findAllDisorderComorbidWithDisorder(Collection<PairId> ids) {
        return disorderComorbidWithDisorderService.getEntries(ids);
    }

    public DisorderComorbidWithDisorder findDisorderComorbidWithDisorder(PairId id) {
        return disorderComorbidWithDisorderService.setDomainIds(disorderComorbidWithDisorderService.find(id).orElseGet(null));
    }

    public List<DisorderIsADisorder> findAllDisorderIsADisorder(Collection<PairId> ids) {
        return disorderIsADisorderService.getEntries(ids);
    }

    public DisorderIsADisorder findDisorderIsADisorder(PairId id) {
        return disorderIsADisorderService.setDomainIds(disorderIsADisorderService.find(id).orElseGet(null));
    }

    public List<DrugHasIndication> findAllDrugHasIndication(List<PairId> ids) {
        return drugHasIndicationService.getEntries(ids);
    }

    public DrugHasIndication findDrugHasIndication(PairId id) {
        return drugHasIndicationService.setDomainIds(drugHasIndicationService.find(id).orElseGet(null));
    }

    public List<DrugHasTargetGene> findAllDrugHasTargetGene(Collection<PairId> ids) {
        return drugHasTargetService.getGenes(ids);
    }

    public DrugHasTargetGene findDrugHasTargetGene(PairId id) {
        return drugHasTargetService.setDomainIds(drugHasTargetService.findGene(id).orElseGet(null));
    }

    public List<DrugHasTargetProtein> findAllDrugHasTargetProtein(Collection<PairId> ids) {
        return drugHasTargetService.getProteins(ids);
    }

    public DrugHasTargetProtein findDrugHasTargetProtein(PairId id) {
        return drugHasTargetService.setDomainIds(drugHasTargetService.findProtein(id).orElseGet(null));
    }

    public List<GeneAssociatedWithDisorder> findAllGeneAssociatedWithDisorder(Collection<PairId> ids) {
        return associatedWithDisorderService.getGenes(ids);
    }

    public GeneAssociatedWithDisorder findGeneAssociatedWithDisorder(PairId id) {
        return associatedWithDisorderService.setDomainIds(associatedWithDisorderService.findGene(id).orElseGet(null));
    }

    public List<GeneInteractsWithGene> findAllGeneInteractsWithGene(Collection<PairId> ids) {
        return proteinInteractsWithProteinService.getGenes(ids);
    }

    public GeneInteractsWithGene findGeneInteractsWithGene(PairId id) {
        return proteinInteractsWithProteinService.setDomainIds(proteinInteractsWithProteinService.findGene(id).orElseGet(null));
    }

    public List<ProteinAssociatedWithDisorder> findAllProteinAssociatedWithDisorder(Collection<PairId> ids) {
        return associatedWithDisorderService.getProteins(ids);
    }

    public ProteinAssociatedWithDisorder findProteinAssociatedWithDisorder(PairId id) {
        return associatedWithDisorderService.setDomainIds(associatedWithDisorderService.findProtein(id).orElseGet(null));
    }

    public List<ProteinEncodedBy> findAllProteinEncodedBy(Collection<PairId> ids) {
        return proteinEncodedByService.getEntries(ids);
    }

    public ProteinEncodedBy findProteinEncodedBy(PairId id) {
        return proteinEncodedByService.setDomainIds(proteinEncodedByService.find(id).orElseGet(null));
    }

    public List<ProteinInPathway> findAllProteinInPathway(Collection<PairId> ids) {
        return proteinInPathwayService.getEntries(ids);
    }

    public ProteinInPathway findProteinInPathway(PairId id) {
        return proteinInPathwayService.setDomainIds(proteinInPathwayService.find(id).orElseGet(null));
    }

    public List<ProteinInteractsWithProtein> findAllProteinInteractsWithProtein(Collection<PairId> ids) {
        return proteinInteractsWithProteinService.getProteins(ids);
    }

    public Iterable<ProteinInteractsWithProtein> findAllProteinInteractsWithProtein() {
        return proteinInteractsWithProteinService.findAllProteins();
    }

    public ProteinInteractsWithProtein findProteinInteractsWithProtein(PairId id) {
        return proteinInteractsWithProteinService.setDomainIds(proteinInteractsWithProteinService.findProtein(id).orElseGet(null));
    }


    public boolean isEdge(int edgeId, int node1, int node2, Integer k1, Integer k2) {
        if (Graphs.getNodesfromEdge(edgeId).first == node1)
            return isEdgeFrom(edgeId, node1, node2, k1, k2);
        return isEdgeFrom(edgeId, node2, node1, k2, k1);
    }

    public Iterable findAll(int edgeId) {
        switch (Graphs.getEdge(edgeId)) {
            case "GeneAssociatedWithDisorder":
                return associatedWithDisorderService.findAllGenes();
            case "DrugHasTargetGene":
                return drugHasTargetService.findAllGenes();
            case "ProteinEncodedBy":
                return proteinEncodedByService.findAll();
            case "DrugHasIndication":
                return drugHasIndicationService.findAll();
            case "DrugHasTargetProtein":
                return drugHasTargetService.findAllProteins();
            case "ProteinInteractsWithProtein":
                return proteinInteractsWithProteinService.findAllProteins();
            case "ProteinInPathway":
                return proteinInPathwayService.findAll();
            case "ProteinAssociatedWithDisorder":
                return associatedWithDisorderService.findAllProteins();
            case "DisorderIsSubtypeOfDisorder":
                return disorderIsADisorderService.findAll();
            case "DisorderComorbidWithDisorder":
                return disorderComorbidWithDisorderService.findAll();
            case "GeneInteractsWithGene":
                return proteinInteractsWithProteinService.findAllGenes();
        }
        return null;
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
            case "DisorderIsSubtypeOfDisorder":
                return isDisorderIsADisorder(k1, k2);
            case "DisorderComorbidWithDisorder":
                return isDisorderComorbidWithDisorder(k1, k2);
            case "GeneInteractsWithGene":
                return isGeneInteractsWithGene(k1, k2);
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
            case "DisorderIsSubtypeOfDisorder":
                return getDisorderIsADisorder(node);
            case "DisorderComorbidWithDisorder":
                return getDisorderComorbidWithDisorder(node);
            case "GeneInteractsWithGene":
                return getGeneInteractsWithGene(node);
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
            case "DisorderIsSubtypeOfDisorder":
                return getDisorderIsADisorder(node);
            case "DisorderComorbidWithDisorder":
                return getDisorderComorbidWithDisorder(node);
            case "GeneInteractsWithGene":
                return getGeneInteractsWithGene(node);
        }
        return null;
    }


    public String[] getListAttributes(Integer type) {
        switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder":
                return GeneAssociatedWithDisorder.getListAttributes();
            case "DrugHasTargetGene":
                return DrugHasTargetGene.getListAttributes();
            case "ProteinEncodedBy":
                return ProteinEncodedBy.getListAttributes();
            case "DrugHasIndication":
                return DrugHasIndication.getListAttributes();
            case "DrugHasTargetProtein":
                return DrugHasTargetProtein.getListAttributes();
            case "ProteinInteractsWithProtein":
                return ProteinInteractsWithProtein.getListAttributes();
            case "ProteinInPathway":
                return ProteinInPathway.getListAttributes();
            case "ProteinAssociatedWithDisorder":
                return ProteinAssociatedWithDisorder.getListAttributes();
            case "DisorderIsSubtypeOfDisorder":
                return DisorderIsADisorder.getListAttributes();
            case "DisorderComorbidWithDisorder":
                return DisorderComorbidWithDisorder.getListAttributes();
            case "GeneInteractsWithGene":
                return GeneInteractsWithGene.getListAttributes();
        }
        return null;
    }

    public LinkedList<HashMap<String,Object>> edgesToAttributeList(Integer type, List<PairId> ids, HashSet<String> attributes) {
        int chunksize = 1000;
        LinkedList<LinkedList<PairId>> chunks = new LinkedList<>();
        for (int i = 0; i < ids.size() / chunksize + 1; i++) {
            LinkedList<PairId> idChunk = new LinkedList<>();
            for (int j = 0; j < chunksize; j++) {
                if (ids.size() <= i * chunksize + j)
                    break;
                idChunk.add(ids.get(i * chunksize + j));
            }
            chunks.add(idChunk);
        }
        LinkedList<HashMap<String, Object>> values = new LinkedList<>();
        switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder":
                chunks.stream().map(this::findAllGeneAssociatedWithDisorder).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    associatedWithDisorderService.setDomainIds(e);
                    values.add(e.getAsMap(attributes));
                });
                break;
            case "DrugHasTargetGene":
                chunks.stream().map(this::findAllDrugHasTargetGene).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    drugHasTargetService.setDomainIds(e);
                    values.add(e.getAsMap(attributes));
                });
                break;
            case "ProteinEncodedBy":
                chunks.stream().map(this::findAllProteinEncodedBy).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    proteinEncodedByService.setDomainIds(e);
                    values.add(e.getAsMap(attributes));
                });
                break;
            case "DrugHasIndication":
                chunks.stream().map(this::findAllDrugHasIndication).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    drugHasIndicationService.setDomainIds(e);
                    values.add(e.getAsMap(attributes));
                });
                break;
            case "DrugHasTargetProtein":
                chunks.stream().map(this::findAllDrugHasTargetProtein).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    drugHasTargetService.setDomainIds(e);
                    values.add(e.getAsMap(attributes));
                });
                break;
            case "ProteinInteractsWithProtein":
                chunks.stream().map(this::findAllProteinInteractsWithProtein).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    proteinInteractsWithProteinService.setDomainIds(e);
                        values.add(e.getAsMap(attributes));
                });
                break;
            case "GeneInteractsWithGene":
                chunks.stream().map(this::findAllGeneInteractsWithGene).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    proteinInteractsWithProteinService.setDomainIds(e);
                        values.add(e.getAsMap(attributes));
                });
                break;
            case "ProteinInPathway":
                chunks.stream().map(this::findAllProteinInPathway).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    proteinInPathwayService.setDomainIds(e);
                        values.add(e.getAsMap(attributes));
                });
                break;
            case "ProteinAssociatedWithDisorder":
                chunks.stream().map(this::findAllProteinAssociatedWithDisorder).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    associatedWithDisorderService.setDomainIds(e);
                        values.add(e.getAsMap(attributes));
                });
                break;
            case "DisorderIsSubtypeOfDisorder":
                chunks.stream().map(this::findAllDisorderIsADisorder).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    disorderIsADisorderService.setDomainIds(e);
                        values.add(e.getAsMap(attributes));
                });
                break;
            case "DisorderComorbidWithDisorder":
                chunks.stream().map(this::findAllDisorderComorbidWithDisorder).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                    disorderComorbidWithDisorderService.setDomainIds(e);
                        values.add(e.getAsMap(attributes));
                });
        }
        return values;
    }

    public HashMap<String, Object> edgeToAttributeList(Integer type, PairId id) {
        switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder":
                return findGeneAssociatedWithDisorder(id).getAsMap();
            case "DrugHasTargetGene":
                return findDrugHasTargetGene(id).getAsMap();
            case "ProteinEncodedBy":
                return findProteinEncodedBy(id).getAsMap();
            case "DrugHasIndication":
                return findDrugHasIndication(id).getAsMap();
            case "DrugHasTargetProtein":
                return findDrugHasTargetProtein(id).getAsMap();
            case "ProteinInteractsWithProtein":
                return findProteinInteractsWithProtein(id).getAsMap();
            case "GeneInteractsWithGene":
                return findGeneInteractsWithGene(id).getAsMap();
            case "ProteinInPathway":
                return findProteinInPathway(id).getAsMap();
            case "ProteinAssociatedWithDisorder":
                return findProteinAssociatedWithDisorder(id).getAsMap();
            case "DisorderIsSubtypeOfDisorder":
                return findDisorderIsADisorder(id).getAsMap();
            case "DisorderComorbidWithDisorder":
                return findDisorderComorbidWithDisorder(id).getAsMap();
        }
        return null;
    }

    public String[] getAttributes(Integer type) {
        switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder":
                return GeneAssociatedWithDisorder.allAttributes;
            case "DrugHasTargetGene":
                return DrugHasTargetGene.allAttributes;
            case "ProteinEncodedBy":
                return ProteinEncodedBy.allAttributes;
            case "DrugHasIndication":
                return DrugHasIndication.allAttributes;
            case "DrugHasTargetProtein":
                return DrugHasTargetProtein.allAttributes;
            case "ProteinInteractsWithProtein":
                return ProteinInteractsWithProtein.allAttributes;
            case "ProteinInPathway":
                return ProteinInPathway.allAttributes;
            case "ProteinAssociatedWithDisorder":
                return ProteinAssociatedWithDisorder.allAttributes;
            case "DisorderIsSubtypeOfDisorder":
                return DisorderIsADisorder.allAttributes;
            case "DisorderComorbidWithDisorder":
                return DisorderComorbidWithDisorder.allAttributes;
            case "GeneInteractsWithGene":
                return GeneInteractsWithGene.allAttributes;
        }
        return null;
    }

    public String[] getAttributeTypes(Integer type) {
        switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder":
                return GeneAssociatedWithDisorder.allAttributeTypes;
            case "DrugHasTargetGene":
                return DrugHasTargetGene.allAttributeTypes;
            case "ProteinEncodedBy":
                return ProteinEncodedBy.allAttributeTypes;
            case "DrugHasIndication":
                return DrugHasIndication.allAttributeTypes;
            case "DrugHasTargetProtein":
                return DrugHasTargetProtein.allAttributeTypes;
            case "ProteinInteractsWithProtein":
                return ProteinInteractsWithProtein.allAttributeTypes;
            case "ProteinInPathway":
                return ProteinInPathway.allAttributeTypes;
            case "ProteinAssociatedWithDisorder":
                return ProteinAssociatedWithDisorder.allAttributeTypes;
            case "DisorderIsSubtypeOfDisorder":
                return DisorderIsADisorder.allAttributeTypes;
            case "DisorderComorbidWithDisorder":
                return DisorderComorbidWithDisorder.allAttributeTypes;
            case "GeneInteractsWithGene":
                return GeneInteractsWithGene.allAttributeTypes;
        }
        return null;
    }

    public boolean[] getIdAttributes(Integer type) {
        switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder":
                return GeneAssociatedWithDisorder.idAttributes;
            case "DrugHasTargetGene":
                return DrugHasTargetGene.idAttributes;
            case "ProteinEncodedBy":
                return ProteinEncodedBy.idAttributes;
            case "DrugHasIndication":
                return DrugHasIndication.idAttributes;
            case "DrugHasTargetProtein":
                return DrugHasTargetProtein.idAttributes;
            case "ProteinInteractsWithProtein":
                return ProteinInteractsWithProtein.idAttributes;
            case "ProteinInPathway":
                return ProteinInPathway.idAttributes;
            case "ProteinAssociatedWithDisorder":
                return ProteinAssociatedWithDisorder.idAttributes;
            case "DisorderIsSubtypeOfDisorder":
                return DisorderIsADisorder.idAttributes;
            case "DisorderComorbidWithDisorder":
                return DisorderComorbidWithDisorder.idAttributes;
            case "GeneInteractsWithGene":
                return GeneInteractsWithGene.idAttributes;
        }
        return null;
    }

    public boolean getDirection(Integer type) {
        switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder":
                return associatedWithDisorderService.isDirected();
            case "DrugHasTargetGene":
                return drugHasTargetService.isDirected();
            case "ProteinEncodedBy":
                return proteinEncodedByService.isDirected();
            case "DrugHasIndication":
                return drugHasIndicationService.isDirected();
            case "DrugHasTargetProtein":
                return drugHasTargetService.isDirected();
            case "ProteinInteractsWithProtein":
                return proteinInteractsWithProteinService.isDirected();
            case "ProteinInPathway":
                return proteinInPathwayService.isDirected();
            case "ProteinAssociatedWithDisorder":
                return associatedWithDisorderService.isDirected();
            case "DisorderIsSubtypeOfDisorder":
                return disorderIsADisorderService.isDirected();
            case "DisorderComorbidWithDisorder":
                return disorderComorbidWithDisorderService.isDirected();
            case "GeneInteractsWithGene":
                return proteinInteractsWithProteinService.isDirected();
        }
        return false;
    }

    public Long getEdgeCount(String type) {
        switch (type) {
            case "GeneAssociatedWithDisorder":
                return associatedWithDisorderService.getGeneCount();
            case "DrugHasTargetGene":
                return drugHasTargetService.getGeneCount();
            case "ProteinEncodedBy":
                return proteinEncodedByService.getCount();
            case "DrugHasIndication":
                return drugHasIndicationService.getCount();
            case "DrugHasTargetProtein":
                return drugHasTargetService.getProteinCount();
            case "ProteinInteractsWithProtein":
                return proteinInteractsWithProteinService.getProteinCount();
            case "ProteinInPathway":
                return proteinInPathwayService.getCount();
            case "ProteinAssociatedWithDisorder":
                return associatedWithDisorderService.getProteinCount();
            case "DisorderIsSubtypeOfDisorder":
                return disorderIsADisorderService.getCount();
            case "DisorderComorbidWithDisorder":
                return disorderComorbidWithDisorderService.getCount();
            case "GeneInteractsWithGene":
                return proteinInteractsWithProteinService.getGeneCount();
        }
        return null;
    }
}
