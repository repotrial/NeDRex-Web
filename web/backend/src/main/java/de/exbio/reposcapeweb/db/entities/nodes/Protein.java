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
@Table(name = "proteins")
public class Protein extends RepoTrialNode {

    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(Disorder.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private int id;

    @Transient
    @JsonIgnore
    public static final HashSet<String> attributes = new HashSet<>(Arrays.asList("displayName", "type", "domainIds", "taxid", "primaryDomainId", "sequence", "synonyms", "geneName", "comments"));

    @Transient
    @JsonIgnore
    public final static String[] allAttributes = new String[]{"id","primaryDomainId","domainIds", "displayName", "geneName", "taxid","sequence", "synonyms","comments","type"};

    public Protein() {
    }

    @Column(nullable = false)
    public String primaryDomainId;
    public String domainIds;
    @Column(columnDefinition = "TEXT")
    public String sequence;
    @Column(nullable = false)
    public String displayName;
    @Column(columnDefinition = "TEXT")
    public String synonyms;
    @Column(columnDefinition = "TEXT")
    public String comments;
    public String geneName;
    public String taxid;

    public static String[] getListAttributes() {
        return new String[]{"id", "displayName", "geneName", "taxid"};
    }

    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("displayName", getDisplayName());
        values.put("type", getType());
        values.put("domainIds", getDomainIds());
        values.put("taxid", getTaxid());
        values.put("sequence", getSequence());
        values.put("synonyms", getSynonyms());
        values.put("geneName", getGeneName());
        values.put("primaryDomainId", getPrimaryDomainId());
        values.put("comments", getComments());
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

    public String getSequence() {
        return sequence;
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

    public String getComments() {
        return comments;
    }

    public String getGeneName() {
        return geneName;
    }

    public String getTaxid() {
        return taxid;
    }

    @JsonGetter
    public String getType() {
        return "Protein";
    }

    @JsonSetter
    public void setType(String type) {
    }

    public void setValues(Protein other) {
        this.sequence = other.sequence;
        this.domainIds = other.domainIds;
        this.comments = other.comments;
        this.geneName = other.geneName;
        this.taxid = other.taxid;
        this.displayName = other.displayName;
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

        if (geneName != null) {
            map.put(FilterType.ORIGIN, new HashMap<>());
            map.get(FilterType.ORIGIN).put(new FilterKey(geneName), new FilterEntry(displayName, FilterType.ORIGIN, id));
        }

        return map;
    }

    @Override
    public EnumMap<FilterType, Map<FilterKey, FilterEntry>> toDistinctFilter() {
        return new EnumMap<>(FilterType.class);
    }
}
