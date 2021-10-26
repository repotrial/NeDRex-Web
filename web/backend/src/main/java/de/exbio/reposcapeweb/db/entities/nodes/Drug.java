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
    public static String[] attributeLabels;

    @Transient
    @JsonIgnore
    public static HashMap<String, String> name2labelMap;

    @Transient
    @JsonIgnore
    public static HashMap<String, String> label2NameMap;

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
//    @Column(nullable = false)
//    private DrugType drugType;
    @Column(columnDefinition = "TEXT")
    private String drugCategories;
    private String drugGroups;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String casNumber;
    @Column(columnDefinition = "TEXT")
    private String indication;
//        @Column(columnDefinition = "TEXT")
//    private String sequences;
    @Column(columnDefinition = "TEXT")
    private String sequence;
    @Column(columnDefinition = "TEXT")
    private String iupacName;
    @Column(columnDefinition = "TEXT")
    private String smiles;
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

    public static void setUpNameMaps() {
        label2NameMap = new HashMap<>();
        name2labelMap = new HashMap<>();
        for (int i = 0; i < allAttributes.length; i++) {
            label2NameMap.put(allAttributes[i], attributeLabels[i]);
            name2labelMap.put(attributeLabels[i], allAttributes[i]);
        }
    }


    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("molecularFormula", getMolecularFormula());
        values.put("displayName", getDisplayName());
        values.put("inchi", getInchi());
        values.put("type", getType());
        values.put("domainIds", getDomainIds());
        values.put("smiles", getSmiles());
        values.put("synonyms", getSynonyms());
        values.put("primaryDomainId", getPrimaryDomainId());
        values.put("casNumber", getCasNumber());
        values.put("drugCategories", getDrugCategories());
        values.put("drugGroups", getDrugGroups());
//        values.put("_cls", drugType.name());
//        values.put("sequences", getSequences());
        values.put("sequence", getSequence());
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

//    @JsonGetter
//    public String get_cls() {
//        return "Drug." + drugType.name();
//    }
//
//    @JsonSetter
//    public void set_cls(String _cls) {
//        this.drugType = DrugType.valueOf(StringUtils.split(_cls, '.').get(1));
//    }

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


    //    @JsonSetter
    public void setType(String type) {
//        this.drugType = DrugType.valueOf(type);
    }

    public String getType() {
        return "Drug";
    }

    public String getIndication() {
        return indication;
    }
//
//        public List<String> getSequences() {
//        try {
//            return StringUtils.stringToList(sequences);
//        } catch (NullPointerException e) {
//            return new LinkedList<>();
//        }
//    }
    public List<String> getSequence() {
        try {
            return StringUtils.stringToList(sequence);
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

//        @JsonSetter
//    public void setSequences(List<String> sequences) {
//        this.sequences = StringUtils.listToString(sequences);
//    }
    @JsonSetter
    public void setSequence(List<String> sequence) {
        this.sequence = StringUtils.listToString(sequence);
    }


    @JsonSetter
    public void setAllDatasets(List<String> allDatasets) {
    }

    public void setValues(Drug other) {
        this.domainIds = other.domainIds;
        this.displayName = other.displayName;
        this.synonyms = other.synonyms;
//        this.drugType = other.drugType;
        this.drugCategories = other.drugCategories;
        this.drugGroups = other.drugGroups;
        this.description = other.description;
        this.casNumber = other.casNumber;
        this.indication = other.indication;
//        this.sequences = other.sequences;
        this.sequence = other.sequence;
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
    public EnumMap<FilterType, Map<String, FilterEntry>> toUniqueFilter() {
        EnumMap<FilterType, Map<String, FilterEntry>> map = new EnumMap<>(FilterType.class);

        FilterEntry ids = new FilterEntry(displayName, FilterType.ID, id);

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


        if (getSynonyms().size() > 0 && getSynonyms().get(0).length() > 0) {
            FilterEntry syns = new FilterEntry(displayName, FilterType.SYNONYM, id);
            map.put(FilterType.SYNONYM, new HashMap<>());
            getSynonyms().forEach(syn -> {
                if (!displayName.equals(syn) && syn.length() > 0)
                    map.get(FilterType.SYNONYM).put(syn, syns);
            });
        }
        try {
            iupacName.charAt(0);
            map.put(FilterType.IUPAC, new HashMap<>());
            map.get(FilterType.IUPAC).put(iupacName, new FilterEntry(displayName, FilterType.IUPAC, id));
        } catch (NullPointerException | IndexOutOfBoundsException ignore) {
        }
        return map;
    }

    @Override
    public EnumMap<FilterType, Map<String, FilterEntry>> toDistinctFilter() {
        EnumMap<FilterType, Map<String, FilterEntry>> map = new EnumMap<>(FilterType.class);

        if (getDrugCategories().size() > 0 && getDrugCategories().get(0).length() > 0) {
            map.put(FilterType.CATEGORY, new TreeMap<>());
            FilterEntry catEntry = new FilterEntry(displayName, FilterType.CATEGORY, id);
            getDrugCategories().forEach(cat -> {
                if (cat.length() > 0)
                    map.get(FilterType.CATEGORY).put(cat, catEntry);
            });
        }

        if (getDrugGroups().size() > 0 && getDrugGroups().get(0).length() > 0) {
            map.put(FilterType.GROUP, new TreeMap<>());
            FilterEntry catEntry = new FilterEntry(displayName, FilterType.GROUP, id);
            getDrugGroups().forEach(g -> {
                if (g.length() > 0)
                    map.get(FilterType.GROUP).put(g, catEntry);
            });
        }

//        map.put(FilterType.TYPE, new TreeMap<>());
//        map.get(FilterType.TYPE).put(drugType.name(), new FilterEntry(displayName, FilterType.TYPE, id));
        return map;
    }

}

