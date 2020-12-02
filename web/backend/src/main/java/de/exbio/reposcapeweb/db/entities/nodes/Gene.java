package de.exbio.reposcapeweb.db.entities.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.RepoTrialNode;
import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.FilterKey;
import de.exbio.reposcapeweb.filter.FilterType;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.FilerException;
import javax.persistence.*;
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
    public static final HashSet<String> attributes = new HashSet<>(Arrays.asList("displayName", "type", "domainIds", "primaryDomainId", "geneType", "symbols", "approvedSymbol", "synonyms", "description", "chromosome", "mapLocation"));


    @Transient
    @JsonIgnore
    public final static String[] allAttributes = new String[]{"id", "primaryDomainId", "domainIds", "displayName", "approvedSymbol", "geneType", "chromosome", "mapLocation", "symbols", "synonyms", "description", "type"};


    @Transient
    @JsonIgnore
    public final static String[] allAttributeTypes = new String[]{"numeric", "", "array", "", "", "", "", "", "array", "array", "", ""};

    @Transient
    @JsonIgnore
    public final static boolean[] idAttributes = new boolean[]{true, true, true, false, false, false, false, false, false, false, false, false};

    @Column(nullable = false)
    private String primaryDomainId;
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

    public static String[] getListAttributes() {
        return new String[]{"id", "displayName", "approvedSymbol", "geneType", "chromosome", "mapLocation"};
    }


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
        ids =new FilterEntry(displayName, FilterType.DOMAIN_ID, id);
    }

    @Override
    public EnumMap<FilterType, Map<FilterKey, FilterEntry>> toUniqueFilter() {
        if(ids ==null)
            createFilterEntry();
        EnumMap<FilterType, Map<FilterKey, FilterEntry>> map = new EnumMap<>(FilterType.class);


        map.put(FilterType.DOMAIN_ID, new HashMap<>());

        if (!getDomainIds().contains(primaryDomainId))
            try {
                primaryDomainId.charAt(0);
                map.get(FilterType.DOMAIN_ID).put(new FilterKey(primaryDomainId), ids);
            } catch (NullPointerException | IndexOutOfBoundsException ignore) {
            }

        getDomainIds().forEach(id -> map.get(FilterType.DOMAIN_ID).put(new FilterKey(id), ids));

        map.put(FilterType.DISPLAY_NAME, new HashMap<>());
        map.get(FilterType.DISPLAY_NAME).put(new FilterKey(displayName), new FilterEntry(displayName, FilterType.DISPLAY_NAME, id));

        if (!displayName.equals(approvedSymbol) & !getSymbols().contains(approvedSymbol) & symbols.length()<2) {
            map.put(FilterType.SYMBOLS, new HashMap<>());
            FilterEntry symbolEntry = new FilterEntry(displayName, FilterType.SYMBOLS, id);
            getSymbols().stream().filter(s -> !s.equals(displayName) | !s.equals(approvedSymbol)).forEach(s -> map.get(FilterType.SYMBOLS).put(new FilterKey(s), symbolEntry));
        }

        FilterEntry syns = new FilterEntry(displayName, FilterType.SYNONYM, id);
        map.put(FilterType.SYNONYM, new HashMap<>());
        getSynonyms().forEach(syn -> {
            if (!displayName.equals(syn))
                map.get(FilterType.SYNONYM).put(new FilterKey(syn), syns);
        });


        return map;
    }

    @Override
    public EnumMap<FilterType, Map<FilterKey, FilterEntry>> toDistinctFilter() {
        if(ids ==null)
            createFilterEntry();
        EnumMap<FilterType,Map<FilterKey,FilterEntry>> filters = new EnumMap<>(FilterType.class);
        HashMap<FilterKey,FilterEntry> geneType = new HashMap<>();
        geneType.put(new FilterKey(this.geneType),new FilterEntry(displayName,FilterType.CATEGORY,id));
        filters.put(FilterType.CATEGORY,geneType);
        HashMap<FilterKey, FilterEntry> chromType = new HashMap<>();
        chromType.put(new FilterKey(this.chromosome), new FilterEntry(displayName, FilterType.GROUP,id));
        filters.put(FilterType.GROUP, chromType);


        return filters;
    }

}
