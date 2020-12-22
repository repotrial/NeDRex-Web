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

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "drugs", indexes = @Index(name = "domainId", columnList = "primaryDomainId", unique = true))
public class Drug extends RepoTrialNode {
    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(Drug.class);

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
    public static Boolean[] idAttributes;

    @Transient
    @JsonIgnore
    public static String[] listAttributes;

    public static String[] getListAttributes() {
        return listAttributes;
    }
    @Column(nullable = false)
    private String primaryDomainId;
    private String domainIds;
    @Column(nullable = false)
    private String displayName;
    @Column(columnDefinition = "TEXT")
    private String synonyms;
    @Column(nullable = false)
    private DrugType type;
    @Column(columnDefinition = "TEXT")
    private String drugCategories;
    private String drugGroups;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String casNumber;
    @Column(columnDefinition = "TEXT")
    private String indication;
    @Column(columnDefinition = "TEXT")
    private String sequences;
    @Column(columnDefinition = "TEXT")
    private String iupacName;
    @Column(columnDefinition = "TEXT")
    private String smiles;
    //    @Lob
    @Column(columnDefinition = "TEXT")
    private String inchi;
    private String molecularFormula;


    public Drug() {

    }

    public static boolean validateFormat(HashSet<String> attributes) {
        for (String a : Drug.sourceAttributes)
            if (!attributes.remove(a))
                return false;
        return attributes.isEmpty();
    }


    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("molecularFormula", getMolecularFormula());
        values.put("displayName", getDisplayName());
        values.put("inchi", getInchi());
        values.put("type", getType().name());
        values.put("domainIds", getDomainIds());
        values.put("smiles", getSmiles());
        values.put("synonyms", getSynonyms());
        values.put("primaryDomainId", getPrimaryDomainId());
        values.put("casNumber", getCasNumber());
        values.put("drugCategories", getDrugCategories());
        values.put("drugGroups", getDrugGroups());
        values.put("_cls", get_cls());
        values.put("sequences", getSequences());
        values.put("iupacName", getIupacName());
        values.put("primaryDataset", getPrimaryDataset());
        values.put("indication", getIndication());
        values.put("allDatasets", getAllDatasets());
        values.put("description", getDescription());
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

    @Override
    public String getPrimaryId() {
        return primaryDomainId;
    }

    public int getId() {
        return id;
    }

    public String getPrimaryDomainId() {
        return primaryDomainId;
    }

    public List<String> getDomainIds() {
        return StringUtils.stringToList(domainIds);
    }

    @JsonGetter
    public String get_cls() {
        return "Drug." + type.name();
    }

    @JsonSetter
    public void set_cls(String _cls) {
    }

    public List<String> getSynonyms() {
        return StringUtils.stringToList(synonyms);
    }

    @JsonGetter
    public String getPrimaryDataset() {
        return "DrugBank";
    }

    @JsonSetter
    public void setPrimaryDataset(String dataset) {
    }

    @JsonGetter
    public List<String> getAllDatasets() {
        return new ArrayList<>(Collections.singletonList("DrugBank"));
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDrugCategories() {
        return StringUtils.stringToList(drugCategories);
    }

    public List<String> getDrugGroups() {
        return StringUtils.stringToList(drugGroups);
    }

    public String getDescription() {
        return description;
    }

    public String getCasNumber() {
        return casNumber;
    }

    @JsonSetter
    public void setType(String type) {
        this.type = DrugType.valueOf(type);
    }


    public void setType(DrugType type) {
        this.type = type;
    }

    public DrugType getType() {
        return type;
    }

    public String getIndication() {
        return indication;
    }

    public List<String> getSequences() {
        try {
            return StringUtils.stringToList(sequences);
        } catch (NullPointerException e) {
            return new LinkedList<>();
        }
    }

    public String getIupacName() {
        return iupacName;
    }

    public String getSmiles() {
        return smiles;
    }

    public String getInchi() {
        return inchi;
    }

    public String getMolecularFormula() {
        return molecularFormula;
    }

    @JsonSetter
    public void setDomainIds(List<String> domainIds) {
        this.domainIds = StringUtils.listToString(domainIds);
    }

    @JsonSetter
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = StringUtils.listToString(synonyms);
    }

    @JsonSetter
    public void setDrugCategories(List<String> drugCategories) {
        this.drugCategories = StringUtils.listToString(drugCategories);
    }

    @JsonSetter
    public void setDrugGroups(List<String> drugGroups) {
        this.drugGroups = StringUtils.listToString(drugGroups);
    }

    @JsonSetter
    public void setSequences(List<String> sequences) {
        this.sequences = StringUtils.listToString(sequences);
    }

    @JsonSetter
    public void setAllDatasets(List<String> allDatasets) {
    }

    public void setValues(Drug other) {
        this.domainIds = other.domainIds;
        this.displayName = other.displayName;
        this.synonyms = other.synonyms;
        this.type = other.type;
        this.drugCategories = other.drugCategories;
        this.drugGroups = other.drugGroups;
        this.description = other.description;
        this.casNumber = other.casNumber;
        this.indication = other.indication;
        this.sequences = other.sequences;
        this.iupacName = other.iupacName;
        this.smiles = other.smiles;
        this.inchi = other.inchi;
        this.molecularFormula = other.molecularFormula;
    }


    @Override
    public String getUniqueId() {
        return getPrimaryId();
    }

    @Override
    public EnumMap<FilterType, Map<FilterKey, FilterEntry>> toUniqueFilter() {
        EnumMap<FilterType, Map<FilterKey, FilterEntry>> map = new EnumMap<>(FilterType.class);

        FilterEntry ids = new FilterEntry(displayName, FilterType.DOMAIN_ID, id);

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


        FilterEntry syns = new FilterEntry(displayName, FilterType.SYNONYM, id);
        map.put(FilterType.SYNONYM, new HashMap<>());
        getSynonyms().forEach(syn -> {
            if (!displayName.equals(syn))
                map.get(FilterType.SYNONYM).put(new FilterKey(syn), syns);
        });

        try {
            iupacName.charAt(0);
            map.put(FilterType.IUPAC, new HashMap<>());
            map.get(FilterType.IUPAC).put(new FilterKey(iupacName), new FilterEntry(displayName, FilterType.IUPAC, id));
        } catch (NullPointerException | IndexOutOfBoundsException ignore) {
        }

        return map;
    }

    @Override
    public EnumMap<FilterType, Map<FilterKey, FilterEntry>> toDistinctFilter() {
        EnumMap<FilterType, Map<FilterKey, FilterEntry>> map = new EnumMap<>(FilterType.class);

        if (getDrugCategories().size() > 0) {
            map.put(FilterType.CATEGORY, new TreeMap<>());
            FilterEntry catEntry = new FilterEntry(displayName, FilterType.CATEGORY, id);
            getDrugCategories().forEach(cat -> {
                map.get(FilterType.CATEGORY).put(new FilterKey(cat), catEntry);
            });
        }

        if (getDrugGroups().size() > 0) {
            map.put(FilterType.GROUP, new TreeMap<>());
            FilterEntry catEntry = new FilterEntry(displayName, FilterType.GROUP, id);
            getDrugGroups().forEach(g -> {
                map.get(FilterType.GROUP).put(new FilterKey(g), catEntry);
            });
        }

        map.put(FilterType.TYPE, new TreeMap<>());
        map.get(FilterType.TYPE).put(new FilterKey(type.name()), new FilterEntry(displayName, FilterType.TYPE, id));
        return map;
    }

}

