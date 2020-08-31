package de.exbio.reposcapeweb.db.entities.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.RepoTrialEntity;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "disorders")
public class Disorder implements RepoTrialEntity {

    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(Disorder.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private long id;
    @Transient
    @JsonIgnore
    private static final HashSet<String> attributes = new HashSet<>(Arrays.asList("displayName", "synonyms", "type", "domainIds", "primaryDomainId", "description", "icd10"));

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

    public static boolean validateFormat(HashSet<String> attributes) {
        for (String a : Disorder.attributes)
            if (!attributes.remove(a))
                return false;
        return attributes.isEmpty();
    }

    @Override
    public String getPrimaryId() {
        return getPrimaryDomainId();
    }
}
