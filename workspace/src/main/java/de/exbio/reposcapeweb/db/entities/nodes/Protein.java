package de.exbio.reposcapeweb.db.entities.nodes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.RepoTrialNode;
import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.FilterKey;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

    public Protein() {
    }

    public String primaryDomainId;
    public String domainIds;
    @Column(columnDefinition = "TEXT")
    public String sequence;
    public String displayName;
    @Column(columnDefinition = "TEXT")
    public String synonyms;
    @Column(columnDefinition = "TEXT")
    public String comments;
    public String geneName;
    public String taxid;

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
    public void setType(String type){}

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
    public String getPrimaryId() {
        return getPrimaryDomainId();
    }


    @Override
    public String getUniqueId() {
        return getPrimaryId();
    }

    @Override
    public Map<FilterKey, FilterEntry> toFilter() {
        //TODO implement
        return null;
    }

}
