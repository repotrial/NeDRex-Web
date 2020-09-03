package de.exbio.reposcapeweb.db.entities.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.RepoTrialNode;
import de.exbio.reposcapeweb.filter.FilterContainer;
import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.FilterKey;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

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


    private String primaryDomainId;
    @Column(columnDefinition = "TEXT")
    private String domainIds;
    private String displayName;
    @Column(columnDefinition = "TEXT")
    private String synonyms;
    private String icd10;
    @Column(columnDefinition = "TEXT")
    private String description;

    public Disorder() {
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

    public void setValues(Disorder other) {
        this.icd10 = other.icd10;
        this.synonyms = other.synonyms;
        this.domainIds = other.domainIds;
        this.description = other.description;
        this.displayName = other.displayName;
        this.primaryDomainId = other.primaryDomainId;
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
    public Map<FilterKey, FilterEntry> toFilter() {
        HashMap<FilterKey, FilterEntry> map = new HashMap<>();

        map.put(new FilterKey(displayName), new FilterEntry(displayName, FilterEntry.FilterTypes.NAME, id));

        FilterContainer.builder(getDomainIds(), new FilterEntry(displayName, FilterEntry.FilterTypes.ALTERNATIVE, id), map);
        FilterContainer.builder(getSynonyms().stream().filter(f -> !f.equals(primaryDomainId)).collect(Collectors.toSet()), new FilterEntry(displayName, FilterEntry.FilterTypes.ALIAS, id), map);

        return map;
    }
}
