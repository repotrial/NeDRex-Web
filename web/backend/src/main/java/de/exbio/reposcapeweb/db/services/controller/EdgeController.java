package de.exbio.reposcapeweb.db.services.controller;

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
    private final DrugHasContraindicationService drugHasContraindicationService;
    private final ObjectMapper objectMapper;

    public static HashMap<String,String> edgeName2LabelMap =new HashMap<>();
    public static HashMap<String,String> edgeLabel2NameMap = new HashMap<>();


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
            DrugHasContraindicationService drugHasContraindicationService,
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
        this.drugHasContraindicationService=drugHasContraindicationService;
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


    public HashSet<Integer> getDrugHasContraindicationFrom(int id) {
        return drugHasContraindicationService.getEdgesFrom(id);
    }

    public boolean isDrugHasContraindicationFrom(int id1, int id2) {
        return drugHasContraindicationService.isEdgeFrom(id1, id2);
    }

    public HashSet<Integer> getDrugHasContraindicationTo(int id) {
        return drugHasContraindicationService.getEdgesTo(id);
    }

    public boolean isDrugHasContraindicationTo(int id1, int id2) {
        return drugHasContraindicationService.isEdgeTo(id1, id2);
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


    public List<DrugHasContraindication> findAllDrugHasContraindication(List<PairId> ids) {
        return drugHasContraindicationService.getEntries(ids);
    }

    public DrugHasContraindication findDrugHasContraindication(PairId id) {
        return drugHasContraindicationService.setDomainIds(drugHasContraindicationService.find(id).orElseGet(null));
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
        return switch (Graphs.getEdge(edgeId)) {
            case "GeneAssociatedWithDisorder" -> associatedWithDisorderService.findAllGenes();
            case "DrugTargetGene" -> drugHasTargetService.findAllGenes();
            case "ProteinEncodedBy" -> proteinEncodedByService.findAll();
            case "DrugIndication" -> drugHasIndicationService.findAll();
            case "DrugContraindication" -> drugHasContraindicationService.findAll();
            case "DrugTargetProtein" -> drugHasTargetService.findAllProteins();
            case "ProteinProteinInteraction" -> proteinInteractsWithProteinService.findAllProteins();
            case "ProteinPathway" -> proteinInPathwayService.findAll();
            case "ProteinAssociatedWithDisorder" -> associatedWithDisorderService.findAllProteins();
            case "DisorderHierarchy" -> disorderIsADisorderService.findAll();
            case "DisorderComorbidity" -> disorderComorbidWithDisorderService.findAll();
            case "GeneGeneInteraction" -> proteinInteractsWithProteinService.findAllGenes();
            default -> null;
        };
    }

    public boolean isEdgeFrom(int edgeId, int node1, int node2, Integer k1, Integer k2) {
        return switch (Graphs.getEdge(edgeId)) {
            case "GeneAssociatedWithDisorder" -> isGenesAssociatedWithDisorderFrom(k1, k2);
            case "DrugTargetGene" -> isDrugHasTargetGeneFrom(k1, k2);
            case "ProteinEncodedBy" -> isProteinEncodedByFrom(k1, k2);
            case "DrugIndication" -> isDrugHasIndicationFrom(k1, k2);
            case "DrugContraindication" -> isDrugHasContraindicationFrom(k1, k2);
            case "DrugTargetProtein" -> isDrugHasTargetProteinFrom(k1, k2);
            case "ProteinProteinInteraction" -> isProteinInteractsWithProtein(k1, k2);
            case "ProteinPathway" -> isProteinInPathwayFrom(k1, k2);
            case "ProteinAssociatedWithDisorder" -> isProteinAssociatedWithDisorderFrom(k1, k2);
            case "DisorderHierarchy" -> isDisorderIsADisorder(k1, k2);
            case "DisorderComorbidity" -> isDisorderComorbidWithDisorder(k1, k2);
            case "GeneGeneInteraction" -> isGeneInteractsWithGene(k1, k2);
            default -> false;
        };
    }

    public HashSet<Integer> getEdges(int edgeId, int firstType, Integer node) {
        if (Graphs.getNodesfromEdge(edgeId).first == firstType)
            return getEdgesFrom(edgeId, node);
        return getEdgesTo(edgeId, node);
    }

    private HashSet<Integer> getEdgesFrom(int edgeId, Integer node) {
        return switch (Graphs.getEdge(edgeId)) {
            case "GeneAssociatedWithDisorder" -> getGenesAssociatedWithDisorderFrom(node);
            case "DrugTargetGene" -> getDrugHasTargetGeneFrom(node);
            case "ProteinEncodedBy" -> getProteinEncodedByFrom(node);
            case "DrugIndication" -> getDrugHasIndicationFrom(node);
            case "DrugContraindication" -> getDrugHasContraindicationFrom(node);
            case "DrugTargetProtein" -> getDrugHasTargetProteinFrom(node);
            case "ProteinProteinInteraction" -> getProteinInteractsWithProtein(node);
            case "ProteinPathway" -> getProteinInPathwayFrom(node);
            case "ProteinAssociatedWithDisorder" -> getProteinAssociatedWithDisorderFrom(node);
            case "DisorderHierarchy" -> getDisorderIsADisorder(node);
            case "DisorderComorbidity" -> getDisorderComorbidWithDisorder(node);
            case "GeneGeneInteraction" -> getGeneInteractsWithGene(node);
            default -> null;
        };
    }

    private HashSet<Integer> getEdgesTo(int edgeId, Integer node) {
        return switch (Graphs.getEdge(edgeId)) {
            case "GeneAssociatedWithDisorder" -> getGenesAssociatedWithDisorderTo(node);
            case "DrugTargetGene" -> getDrugHasTargetGeneTo(node);
            case "ProteinEncodedBy" -> getProteinEncodedByTo(node);
            case "DrugIndication" -> getDrugHasIndicationTo(node);
            case "DrugContraindication" -> getDrugHasContraindicationTo(node);
            case "DrugTargetProtein" -> getDrugHasTargetProteinTo(node);
            case "ProteinProteinInteraction" -> getProteinInteractsWithProtein(node);
            case "ProteinPathway" -> getProteinInPathwayTo(node);
            case "ProteinAssociatedWithDisorder" -> getProteinAssociatedWithDisorderTo(node);
            case "DisorderHierarchy" -> getDisorderIsADisorder(node);
            case "DisorderComorbidity" -> getDisorderComorbidWithDisorder(node);
            case "GeneGeneInteraction" -> getGeneInteractsWithGene(node);
            default -> null;
        };
    }


    public String[] getListAttributes(Integer type) {
        return switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder" -> GeneAssociatedWithDisorder.getListAttributes();
            case "DrugTargetGene" -> DrugHasTargetGene.getListAttributes();
            case "ProteinEncodedBy" -> ProteinEncodedBy.getListAttributes();
            case "DrugIndication" -> DrugHasIndication.getListAttributes();
            case "DrugContraindication" -> DrugHasContraindication.getListAttributes();
            case "DrugTargetProtein" -> DrugHasTargetProtein.getListAttributes();
            case "ProteinProteinInteraction" -> ProteinInteractsWithProtein.getListAttributes();
            case "ProteinPathway" -> ProteinInPathway.getListAttributes();
            case "ProteinAssociatedWithDisorder" -> ProteinAssociatedWithDisorder.getListAttributes();
            case "DisorderHierarchy" -> DisorderIsADisorder.getListAttributes();
            case "DisorderComorbidity" -> DisorderComorbidWithDisorder.getListAttributes();
            case "GeneGeneInteraction" -> GeneInteractsWithGene.getListAttributes();
            default -> null;
        };
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
            case "GeneAssociatedWithDisorder" -> chunks.stream().map(this::findAllGeneAssociatedWithDisorder).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                associatedWithDisorderService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "DrugTargetGene" -> chunks.stream().map(this::findAllDrugHasTargetGene).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                drugHasTargetService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "ProteinEncodedBy" -> chunks.stream().map(this::findAllProteinEncodedBy).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                proteinEncodedByService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "DrugIndication" -> chunks.stream().map(this::findAllDrugHasIndication).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                drugHasIndicationService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "DrugContraindication" -> chunks.stream().map(this::findAllDrugHasContraindication).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                drugHasContraindicationService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "DrugTargetProtein" -> chunks.stream().map(this::findAllDrugHasTargetProtein).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                drugHasTargetService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "ProteinProteinInteraction" -> chunks.stream().map(this::findAllProteinInteractsWithProtein).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                proteinInteractsWithProteinService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "GeneGeneInteraction" -> chunks.stream().map(this::findAllGeneInteractsWithGene).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                proteinInteractsWithProteinService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "ProteinPathway" -> chunks.stream().map(this::findAllProteinInPathway).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                proteinInPathwayService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "ProteinAssociatedWithDisorder" -> chunks.stream().map(this::findAllProteinAssociatedWithDisorder).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                associatedWithDisorderService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "DisorderHierarchy" -> chunks.stream().map(this::findAllDisorderIsADisorder).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                disorderIsADisorderService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
            case "DisorderComorbidity" -> chunks.stream().map(this::findAllDisorderComorbidWithDisorder).flatMap(Collection::stream).collect(Collectors.toList()).forEach(e -> {
                disorderComorbidWithDisorderService.setDomainIds(e);
                values.add(e.getAsMap(attributes));
            });
        }
        return values;
    }

    public HashMap<String, Object> edgeToAttributeList(Integer type, PairId id) {
        return switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder" -> findGeneAssociatedWithDisorder(id).getAsMap();
            case "DrugTargetGene" -> findDrugHasTargetGene(id).getAsMap();
            case "ProteinEncodedBy" -> findProteinEncodedBy(id).getAsMap();
            case "DrugIndication" -> findDrugHasIndication(id).getAsMap();
            case "DrugContraindication" -> findDrugHasContraindication(id).getAsMap();
            case "DrugTargetProtein" -> findDrugHasTargetProtein(id).getAsMap();
            case "ProteinProteinInteraction" -> findProteinInteractsWithProtein(id).getAsMap();
            case "GeneGeneInteraction" -> findGeneInteractsWithGene(id).getAsMap();
            case "ProteinPathway" -> findProteinInPathway(id).getAsMap();
            case "ProteinAssociatedWithDisorder" -> findProteinAssociatedWithDisorder(id).getAsMap();
            case "DisorderHierarchy" -> findDisorderIsADisorder(id).getAsMap();
            case "DisorderComorbidity" -> findDisorderComorbidWithDisorder(id).getAsMap();
            default -> null;
        };
    }

    public String[] getAttributes(Integer type) {
        return switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder" -> GeneAssociatedWithDisorder.allAttributes;
            case "DrugTargetGene" -> DrugHasTargetGene.allAttributes;
            case "ProteinEncodedBy" -> ProteinEncodedBy.allAttributes;
            case "DrugIndication" -> DrugHasIndication.allAttributes;
            case "DrugContraindication" -> DrugHasContraindication.allAttributes;
            case "DrugTargetProtein" -> DrugHasTargetProtein.allAttributes;
            case "ProteinProteinInteraction" -> ProteinInteractsWithProtein.allAttributes;
            case "ProteinPathway" -> ProteinInPathway.allAttributes;
            case "ProteinAssociatedWithDisorder" -> ProteinAssociatedWithDisorder.allAttributes;
            case "DisorderHierarchy" -> DisorderIsADisorder.allAttributes;
            case "DisorderComorbidity" -> DisorderComorbidWithDisorder.allAttributes;
            case "GeneGeneInteraction" -> GeneInteractsWithGene.allAttributes;
            default -> null;
        };
    }

    public String[] isExperimental(Integer type) {
        return switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder" -> GeneAssociatedWithDisorder.allAttributeTypes;
            case "DrugTargetGene" -> DrugHasTargetGene.allAttributeTypes;
            case "ProteinEncodedBy" -> ProteinEncodedBy.allAttributeTypes;
            case "DrugIndication" -> DrugHasIndication.allAttributeTypes;
            case "DrugContraindication" -> DrugHasContraindication.allAttributeTypes;
            case "DrugTargetProtein" -> DrugHasTargetProtein.allAttributeTypes;
            case "ProteinProteinInteraction" -> ProteinInteractsWithProtein.allAttributeTypes;
            case "ProteinPathway" -> ProteinInPathway.allAttributeTypes;
            case "ProteinAssociatedWithDisorder" -> ProteinAssociatedWithDisorder.allAttributeTypes;
            case "DisorderHierarchy" -> DisorderIsADisorder.allAttributeTypes;
            case "DisorderComorbidity" -> DisorderComorbidWithDisorder.allAttributeTypes;
            case "GeneGeneInteraction" -> GeneInteractsWithGene.allAttributeTypes;
            default -> null;
        };
    }

    public boolean isExperimental(Integer type, int id1, int id2) {
        return switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder" -> true;
            case "DrugTargetGene" -> true;
            case "ProteinEncodedBy" -> true;
            case "DrugIndication" -> true;
            case "DrugContraindication" -> true;
            case "DrugTargetProtein" -> true;
            case "ProteinProteinInteraction" -> proteinInteractsWithProteinService.isExperimentalProtein(id1, id2);
            case "ProteinPathway" -> true;
            case "ProteinAssociatedWithDisorder" -> true;
            case "DisorderHierarchy" -> true;
            case "DisorderComorbidity" -> true;
            case "GeneGeneInteraction" -> proteinInteractsWithProteinService.isExperimentalGene(id1, id2);
            default -> true;
        };
    }

    public Boolean[] getIdAttributes(Integer type) {
        return switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder" -> GeneAssociatedWithDisorder.idAttributes;
            case "DrugTargetGene" -> DrugHasTargetGene.idAttributes;
            case "ProteinEncodedBy" -> ProteinEncodedBy.idAttributes;
            case "DrugIndication" -> DrugHasIndication.idAttributes;
            case "DrugContraindication" -> DrugHasContraindication.idAttributes;
            case "DrugTargetProtein" -> DrugHasTargetProtein.idAttributes;
            case "ProteinProteinInteraction" -> ProteinInteractsWithProtein.idAttributes;
            case "ProteinPathway" -> ProteinInPathway.idAttributes;
            case "ProteinAssociatedWithDisorder" -> ProteinAssociatedWithDisorder.idAttributes;
            case "DisorderHierarchy" -> DisorderIsADisorder.idAttributes;
            case "DisorderComorbidity" -> DisorderComorbidWithDisorder.idAttributes;
            case "GeneGeneInteraction" -> GeneInteractsWithGene.idAttributes;
            default -> null;
        };
    }

    public boolean getDirection(Integer type) {
        return switch (Graphs.getEdge(type)) {
            case "GeneAssociatedWithDisorder", "ProteinAssociatedWithDisorder" -> associatedWithDisorderService.isDirected();
            case "DrugTargetGene", "DrugTargetProtein" -> drugHasTargetService.isDirected();
            case "ProteinEncodedBy" -> proteinEncodedByService.isDirected();
            case "DrugIndication" -> drugHasIndicationService.isDirected();
            case "DrugContraindication" -> drugHasContraindicationService.isDirected();
            case "ProteinProteinInteraction", "GeneGeneInteraction" -> proteinInteractsWithProteinService.isDirected();
            case "ProteinPathway" -> proteinInPathwayService.isDirected();
            case "DisorderHierarchy" -> disorderIsADisorderService.isDirected();
            case "DisorderComorbidity" -> disorderComorbidWithDisorderService.isDirected();
            default -> false;
        };
    }

    public Long getEdgeCount(String type) {
        return switch (type) {
            case "GeneAssociatedWithDisorder" -> associatedWithDisorderService.getGeneCount();
            case "DrugTargetGene" -> drugHasTargetService.getGeneCount();
            case "ProteinEncodedBy" -> proteinEncodedByService.getCount();
            case "DrugIndication" -> drugHasIndicationService.getCount();
            case "DrugContraindication" -> drugHasContraindicationService.getCount();
            case "DrugTargetProtein" -> drugHasTargetService.getProteinCount();
            case "ProteinProteinInteraction" -> proteinInteractsWithProteinService.getProteinCount();
            case "ProteinPathway" -> proteinInPathwayService.getCount();
            case "ProteinAssociatedWithDisorder" -> associatedWithDisorderService.getProteinCount();
            case "DisorderHierarchy" -> disorderIsADisorderService.getCount();
            case "DisorderComorbidity" -> disorderComorbidWithDisorderService.getCount();
            case "GeneGeneInteraction" -> proteinInteractsWithProteinService.getGeneCount();
            default -> null;
        };
    }

    public HashMap<String, String> getAttributeLabelMap(String type) {
        return switch (type) {
            case "GeneAssociatedWithDisorder" -> GeneAssociatedWithDisorder.label2NameMap;
            case "DrugTargetGene" -> DrugHasTargetGene.label2NameMap;
            case "ProteinEncodedBy" -> ProteinEncodedBy.label2NameMap;
            case "DrugIndication" -> DrugHasIndication.label2NameMap;
            case "DrugContraindication" -> DrugHasContraindication.label2NameMap;
            case "DrugTargetProtein" -> DrugHasTargetProtein.label2NameMap;
            case "ProteinProteinInteraction" -> ProteinInteractsWithProtein.label2NameMap;
            case "ProteinPathway" -> ProteinInPathway.label2NameMap;
            case "ProteinAssociatedWithDisorder" -> ProteinAssociatedWithDisorder.label2NameMap;
            case "DisorderHierarchy" -> DisorderIsADisorder.label2NameMap;
            case "DisorderComorbidity" -> DisorderComorbidWithDisorder.label2NameMap;
            case "GeneGeneInteraction" -> GeneInteractsWithGene.label2NameMap;
            default -> null;
        };

    }

    public void setUp() {
        GeneAssociatedWithDisorder.setUpNameMaps();
        DrugHasTargetGene.setUpNameMaps();
        ProteinEncodedBy.setUpNameMaps();
        DrugHasIndication.setUpNameMaps();
        DrugHasContraindication.setUpNameMaps();
        DrugHasTargetProtein.setUpNameMaps();
        ProteinInteractsWithProtein.setUpNameMaps();
        ProteinInPathway.setUpNameMaps();
        ProteinAssociatedWithDisorder.setUpNameMaps();
        DisorderIsADisorder.setUpNameMaps();
        GeneInteractsWithGene.setUpNameMaps();
    }
}
