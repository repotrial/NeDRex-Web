package de.exbio.reposcapeweb.db.entities.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.RepoTrialNode;
import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.FilterType;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.util.*;

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
    public static HashSet<String> sourceAttributes;

    @Transient
    @JsonIgnore
    public static String[] allAttributes;

    @Transient
    @JsonIgnore
    public static String[] allAttributeTypes;

    @Transient
    @JsonIgnore
    public static String[] attributeLabels;

    @Transient
    @JsonIgnore
    public static HashMap<String,String> name2labelMap;

    @Transient
    @JsonIgnore
    public static HashMap<String,String> label2NameMap;

    @Transient
    @JsonIgnore
    public static Boolean[] idAttributes;

    @Transient
    @JsonIgnore
    public static String[] listAttributes;

    public static String[] getListAttributes() {
        return listAttributes;
    }

    @Column(nullable = false)
    private String primaryDomainId;
    @Column(columnDefinition = "TEXT")
    private String domainIds;
    @Column(nullable = false)
    private String displayName;
    @Column(columnDefinition = "TEXT")
    private String synonyms;
    private String approvedSymbol;
    private String symbols;
    private String description;
    private String chromosome;
    private String mapLocation;
    private String geneType;



    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("displayName", getDisplayName());
        values.put("type", getType());
        values.put("domainIds", getDomainIds());
        values.put("synonyms", getSynonyms());
        values.put("geneType", getGeneType());
        values.put("symbols", getSymbols());
        values.put("approvedSymbol", getApprovedSymbol());
        values.put("dataSources", getDataSources());
        values.put("description", getDescription());
        values.put("chromosome", getChromosome());
        values.put("mapLocation", getMapLocation());
        values.put("primaryDomainId", getPrimaryDomainId());
        return values;
    }

    @Override
    public HashMap<String, Object> getAsMap(HashSet<String> attributes) {
        HashMap<String, Object> values = new HashMap<>();
        getAsMap().forEach((k, v) -> {
            if (attributes.contains(k))
                values.put(k, v);
        });
        return values;
    }

    public static void setUpNameMaps() {
        label2NameMap=new HashMap<>();
        name2labelMap = new HashMap<>();
        for (int i = 0; i < allAttributes.length; i++) {
            label2NameMap.put(allAttributes[i],attributeLabels[i]);
            name2labelMap.put(attributeLabels[i], allAttributes[i]);
        }
    }

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

    @Column(columnDefinition = "TEXT")
    private String dataSources;
    @JsonGetter
    public LinkedList<String> getDataSources() {
        return StringUtils.stringToList(dataSources);
    }

    @JsonSetter
    public void setDataSources(List<String> dataSources) {
        this.dataSources = StringUtils.listToString(dataSources);
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
        this.synonyms = StringUtils.listToString(synonyms);
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
        this.dataSources = other.dataSources;
        this.geneType = other.geneType;
        this.mapLocation = other.mapLocation;
        this.primaryDomainId = other.primaryDomainId;
        this.synonyms = other.synonyms;

    }


    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getPrimaryId() {
        return getPrimaryDomainId();
    }


    @Override
    public String getUniqueId() {
        return getPrimaryId();
    }

    @Transient
    private FilterEntry ids = null;

    public void createFilterEntry(){
        ids =new FilterEntry(displayName, FilterType.ID, id);
    }

    @Override
    public EnumMap<FilterType, Map<String, FilterEntry>> toUniqueFilter() {
        if(ids ==null)
            createFilterEntry();
        EnumMap<FilterType, Map<String, FilterEntry>> map = new EnumMap<>(FilterType.class);


        map.put(FilterType.ID, new HashMap<>());

        if (!getDomainIds().contains(primaryDomainId))
            try {
                primaryDomainId.charAt(0);
                map.get(FilterType.ID).put(primaryDomainId, ids);
            } catch (NullPointerException | IndexOutOfBoundsException ignore) {
            }

        getDomainIds().forEach(id -> map.get(FilterType.ID).put(id, ids));

        map.put(FilterType.NAME, new HashMap<>());
        map.get(FilterType.NAME).put(displayName, new FilterEntry(displayName, FilterType.NAME, id));

        if (! (getSymbols().size()==0 | (displayName.equals(approvedSymbol) & getSymbols().contains(approvedSymbol) & getSymbols().size()==1))) {
            map.put(FilterType.SYMBOL, new HashMap<>());
            FilterEntry symbolEntry = new FilterEntry(displayName, FilterType.SYMBOL, id);
            getSymbols().stream().filter(s -> (!s.equals(displayName) | !s.equals(approvedSymbol)) & s.length()>0 ).forEach(s -> map.get(FilterType.SYMBOL).put(s, symbolEntry));
        }

        FilterEntry syns = new FilterEntry(displayName, FilterType.SYNONYM, id);
        map.put(FilterType.SYNONYM, new HashMap<>());
        getSynonyms().forEach(syn -> {
            if (!displayName.equals(syn) & syn.length()>0)
                map.get(FilterType.SYNONYM).put(syn, syns);
        });

        return map;
    }

    @Override
    public EnumMap<FilterType, Map<String, FilterEntry>> toDistinctFilter() {
        if(ids ==null)
            createFilterEntry();
        EnumMap<FilterType,Map<String,FilterEntry>> filters = new EnumMap<>(FilterType.class);
        HashMap<String,FilterEntry> geneType = new HashMap<>();
        geneType.put(this.geneType,new FilterEntry(displayName,FilterType.CATEGORY,id));
        filters.put(FilterType.CATEGORY,geneType);
        HashMap<String, FilterEntry> chromType = new HashMap<>();
        chromType.put(this.chromosome, new FilterEntry(displayName, FilterType.CHROMOSOME,id));
        filters.put(FilterType.CHROMOSOME, chromType);


        return filters;
    }

}
