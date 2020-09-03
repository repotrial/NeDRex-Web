package de.exbio.reposcapeweb.db.entities.edges;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.entities.nodes.RepoTrialEdge;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "protein_associated_with_disorder")
public class ProteinAssociatedWithDisorder extends RepoTrialEdge implements Serializable {

    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(ProteinAssociatedWithDisorder.class);

    @JsonIgnore
    @EmbeddedId
    private PairId id;

    @Transient
    @JsonIgnore
    public final static HashSet<String> attributes = new HashSet<>(Arrays.asList("targetDomainId", "type", "sourceDomainId", "score", "assertedBy"));


    @Transient
    private String targetDomainId;
    @Transient
    private String sourceDomainId;

    private String score;

    private String assertedBy;


    public ProteinAssociatedWithDisorder() {
    }

    public String getTargetDomainId() {
        return targetDomainId;
    }

    public String getSourceDomainId() {
        return sourceDomainId;
    }

    @JsonGetter
    public String getType() {
        return "GeneAssociatedWithDisorder";
    }

    public String getScore() {
        return score;
    }

    @JsonSetter
    public void setType(String type) {
    }

    public List<String> getAssertedBy() {
        return StringUtils.stringToList(assertedBy);
    }

    public void setAssertedBy(List<String> assertedBy) {
        this.assertedBy = StringUtils.listToString(assertedBy);
    }

    public void setValues(ProteinAssociatedWithDisorder other) {
        this.sourceDomainId = other.sourceDomainId;
        this.targetDomainId = other.targetDomainId;
        this.assertedBy = other.assertedBy;
        this.score = other.score;
    }

    @Override
    public PairId getPrimaryIds() {
        return id;
    }

    public void setId(PairId id) {
        this.id = id;
    }

    @Override
    public Pair<String, String> getIdsToMap() {
        return new Pair<>(sourceDomainId, targetDomainId);
    }
}
