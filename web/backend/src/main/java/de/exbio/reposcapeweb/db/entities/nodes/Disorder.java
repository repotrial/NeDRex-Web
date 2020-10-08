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
    public static final HashSet<String> attributes = new HashSet<>(Arrays.asList("displayName", "synonyms", "type", "domainIds", "primaryDomainId", "description", "icd10"));


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

    public static String[] getListAttributes() {
        return new String[]{"displayName", "synonyms",  "domainIds"};
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
    public EnumMap<FilterType, Map<FilterKey, FilterEntry>> toDistinctFilter() {
        EnumMap<FilterType, Map<FilterKey, FilterEntry>> map = new EnumMap<>(FilterType.class);

        //TODO add disorder groups/parents

        return map;
    }

    @Override
    public HashMap<String, String> getAsMap() {
        HashMap<String,String> values = new HashMap<>();
        values.put("displayName",getDisplayName());
        values.put("synonyms",synonyms);
        values.put("type",getType());
        values.put("domainIds",domainIds);
        values.put("primaryDomainId",primaryDomainId);
        values.put("description",getDescription());
        values.put("icd10",icd10);
        return values;
    }

    @Override
    public HashMap<String, String> getAsMap(HashSet<String> attributes) {
        HashMap<String,String> values = new HashMap<>();
        getAsMap().forEach((k,v)->{
            if(attributes.contains(k))
                values.put(k,v);
        });
        return values;
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

        map.put(FilterType.ICD10, new HashMap<>());
        map.get(FilterType.ICD10).put(new FilterKey(icd10), new FilterEntry(displayName, FilterType.ICD10, id));

        boolean notNull = false;
        map.put(FilterType.DISPLAY_NAME, new HashMap<>());
        map.get(FilterType.DISPLAY_NAME).put(new FilterKey(displayName), new FilterEntry(displayName, FilterType.DISPLAY_NAME, id));


        if (getSynonyms().size() > 0) {
            FilterEntry syns = new FilterEntry(displayName, FilterType.SYNONYM, id);
            map.put(FilterType.SYNONYM, new HashMap<>());
            getSynonyms().forEach(syn -> {
                if (!displayName.equals(syn))
                    map.get(FilterType.SYNONYM).put(new FilterKey(syn), syns);
            });
        }
        return map;
    }
}
