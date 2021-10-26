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
@Table(name = "pathways", indexes = @Index(name = "domainId", columnList = "primaryDomainId", unique = true))
public class Pathway extends RepoTrialNode {
    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(Pathway.class);

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
    @Column(nullable = false)
    private String displayName;
    private String domainIds;
    private Integer taxid;


    public Pathway() {

    }

    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("displayName", getDisplayName());
        values.put("type", getType());
        values.put("domainIds", getDomainIds());
        values.put("taxid",getTaxid());
        values.put("primaryDomainId", getPrimaryDomainId());
        values.put("species", getSpecies());
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

    public LinkedList<String> getDomainIds() {
        return StringUtils.stringToList(domainIds);
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonSetter
    public void setDomainIds(LinkedList<String> domainIds) {
        this.domainIds = StringUtils.listToString(domainIds);
    }

    @JsonGetter
    public String getSpecies() {
        return "Homo sapiens";
    }

    @JsonSetter
    public void setSpecies(String species) {
    }

    @JsonGetter
    public String getType() {
        return "Pathway";
    }

    @JsonSetter
    public void setType(String type) {
    }

    public Integer getTaxid() {
        return taxid;
    }

    public void setTaxid(Integer taxid) {
        this.taxid = taxid;
    }

    public void setValues(Pathway other) {
        this.domainIds = other.domainIds;
        this.primaryDomainId = other.primaryDomainId;
        this.displayName = other.displayName;

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


        return map;
    }

    @Override
    public EnumMap<FilterType, Map<String, FilterEntry>> toDistinctFilter() {
        return new EnumMap<>(FilterType.class);
    }

}
