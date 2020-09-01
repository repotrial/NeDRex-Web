package de.exbio.reposcapeweb.db.entities.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.RepoTrialEntity;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "genes")
public class Gene extends RepoTrialNode {


    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(Disorder.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private int id;
    @Transient
    @JsonIgnore
    public static final HashSet<String> attributes = new HashSet<>(Arrays.asList("displayName", "type", "domainIds", "primaryDomainId", "geneType", "symbols", "approvedSymbol", "synonyms", "description", "chromosome", "mapLocation"));

    @JsonIgnore
    @Transient
    public static HashMap<Integer,String> idToDomainMap = new HashMap<>();
    @JsonIgnore
    @Transient
    public static HashMap<String,Integer> domainToIdMap = new HashMap<>();

    private String primaryDomainId;
    private String domainIds;
    private String displayName;
    private String synonyms;
    private String approvedSymbol;
    private String symbols;
    private String description;
    private String chromosome;
    private String mapLocation;
    private String geneType;


    public String getPrimaryDomainId() {
        return primaryDomainId;
    }

    @JsonGetter
    public List<String> getDomainIds() {
        return StringUtils.stringToList(domainIds);
    }

    @JsonSetter
    public void setDomainIds(List<String> domainIds) {
        this.domainIds = StringUtils.listToString(domainIds);
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonGetter
    public List<String> getSynonyms() {
        return StringUtils.stringToList(synonyms);
    }

    @JsonSetter
    public void setSynonyms(List<String> synonyms) {
        this.symbols = StringUtils.listToString(synonyms);
    }

    public String getApprovedSymbol() {
        return approvedSymbol;
    }

    @JsonGetter
    public List<String> getSymbols() {
        return StringUtils.stringToList(symbols);
    }

    @JsonSetter
    public void setSymbols(List<String> symbols) {
        this.symbols = StringUtils.listToString(symbols);
    }

    public String getDescription() {
        return description;
    }

    public String getChromosome() {
        return chromosome;
    }

    public String getMapLocation() {
        return mapLocation;
    }

    public String getGeneType() {
        return geneType;
    }

    @JsonGetter
    public String getType() {
        return "Gene";
    }

    @JsonSetter
    public void setType(String type) {
    }

    public void setValues(Gene other) {
        this.symbols = other.symbols;
        this.domainIds = other.domainIds;
        this.approvedSymbol = other.approvedSymbol;
        this.chromosome = other.chromosome;
        this.description = other.description;
        this.displayName = other.displayName;
        this.geneType = other.geneType;
        this.mapLocation = other.mapLocation;
        this.primaryDomainId = other.primaryDomainId;
        this.synonyms = other.synonyms;

    }

//    public static boolean validateFormat(HashSet<String> attributes) {
//        for (String a : Gene.attributes)
//            if (!attributes.remove(a))
//                return false;
//        return attributes.isEmpty();
//    }

    @Override
    public String getPrimaryId() {
        return getPrimaryDomainId();
    }

//    @Override
//    public HashMap<Integer, String> getIdToDomainMap() {
//        if(Drug.idToDomainMap==null)
//            Drug.idToDomainMap=new HashMap<>();
//        return Drug.idToDomainMap;
//    }
//
//    @Override
//    public HashMap<String, Integer> getDomainToIdMap() {
//        if(Drug.domainToIdMap==null)
//            Drug.domainToIdMap=new HashMap<>();
//        return Drug.domainToIdMap;
//    }

    @Override
    public String getUniqueId() {
        return getPrimaryId();
    }
}
