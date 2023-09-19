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
@Table(name = "disorders")
public class Disorder extends RepoTrialNode {

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
    public static String[] attributeLabels;

    @Transient
    @JsonIgnore
    public static HashMap<String,String> name2labelMap;

    @Transient
    @JsonIgnore
    public static HashMap<String,String> label2NameMap;

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
    @Column(columnDefinition = "TEXT")
    private String domainIds;
    @Column(nullable = false)
    private String displayName;
    @Column(columnDefinition = "TEXT")
    private String synonyms;
    private String icd10;



    @Column(columnDefinition = "TEXT")
    private String description;

    public Disorder() {
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
    public LinkedList<String> getDomainIds() {
        return StringUtils.stringToList(domainIds);
    }

    @JsonSetter
    public void setDomainIds(List<String> domainIds) {
        //TODO cache lists

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
    public LinkedList<String> getSynonyms() {
        return StringUtils.stringToList(synonyms);
    }

    @JsonSetter
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = StringUtils.listToString(synonyms);
    }

    @JsonGetter
    public LinkedList<String> getIcd10() {
        return StringUtils.stringToList(icd10);
    }

    @JsonSetter
    public void setIcd10(List<String> icd10) {
        this.icd10 = StringUtils.listToString(icd10);
    }

    public String getDescription() {
        return description;
    }

    @JsonGetter
    public String getType() {
        return "Disorder";
    }

    @JsonSetter
    public void setType(String type) {
    }

    @JsonSetter
    public void setPrimaryDomainId(String domainId) {
        this.primaryDomainId = domainId;
        if (this.displayName == null)
            this.displayName = domainId;
    }

    public void setValues(Disorder other) {
        this.icd10 = other.icd10;
        this.synonyms = other.synonyms;
        this.domainIds = other.domainIds;
        this.description = other.description;
        this.displayName = other.displayName;
        this.dataSources = other.dataSources;
        this.primaryDomainId = other.primaryDomainId;
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

    @Override
    public EnumMap<FilterType, Map<String, FilterEntry>> toDistinctFilter() {
        EnumMap<FilterType, Map<String, FilterEntry>> map = new EnumMap<>(FilterType.class);

        map.put(FilterType.ICD10, new HashMap<>());
        FilterEntry icd10Entry = new FilterEntry(displayName, FilterType.ICD10, id);
        getIcd10().forEach(icd-> map.get(FilterType.ICD10).put(icd, icd10Entry));

        return map;
    }

    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String,Object> values = new HashMap<>();
        values.put("id",id);
        values.put("displayName",getDisplayName());
        values.put("synonyms",getSynonyms());
        values.put("type",getType());
        values.put("domainIds",getDomainIds());
        values.put("primaryDomainId",getPrimaryDomainId());
        values.put("description",getDescription());
        values.put("dataSources", getDataSources());
        values.put("icd10",getIcd10());
        return values;
    }

    @Override
    public HashMap<String, Object> getAsMap(HashSet<String> attributes) {
        HashMap<String,Object> values = new HashMap<>();
        getAsMap().forEach((k,v)->{
            if(attributes.contains(k))
                values.put(k,v);
        });
        return values;
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


        if (getSynonyms().size() > 0) {
            FilterEntry syns = new FilterEntry(displayName, FilterType.SYNONYM, id);
            map.put(FilterType.SYNONYM, new HashMap<>());
            getSynonyms().forEach(syn -> {
                if (!displayName.equals(syn))
                    map.get(FilterType.SYNONYM).put(syn, syns);
            });
        }
        return map;
    }
}
