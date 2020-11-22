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
    public static final HashSet<String> attributes = new HashSet<>(Arrays.asList("displayName", "type", "domainIds", "primaryDomainId", "species"));


    @Transient
    @JsonIgnore
    public final static String[] allAttributes = new String[]{"id", "primaryDomainId", "displayName", "domainIds", "species", "type"};

    @Transient
    @JsonIgnore
    public final static String[] allAttributeTypes = new String[]{"numeric", "", "", "array", "array", ""};

    @Transient
    @JsonIgnore
    public final static boolean[] idAttributes = new boolean[]{true, true, false, true, false, false};


    @Column(nullable = false)
    private String primaryDomainId;
    @Column(nullable = false)
    private String displayName;
    private String domainIds;

    public Pathway() {

    }

    public static String[] getListAttributes() {
        return new String[]{"id", "displayName", "primaryDomainId"};
    }

    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("displayName", getDisplayName());
        values.put("type", getType());
        values.put("domainIds", getDomainIds());
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


        return map;
    }

    @Override
    public EnumMap<FilterType, Map<FilterKey, FilterEntry>> toDistinctFilter() {
        return new EnumMap<>(FilterType.class);
    }

}
