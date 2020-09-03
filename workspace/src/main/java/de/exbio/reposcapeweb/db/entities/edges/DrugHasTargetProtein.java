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
@Table(name = "drug_has_target_protein")
public class DrugHasTargetProtein extends RepoTrialEdge implements Serializable {

    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(DrugHasTargetProtein.class);

    @JsonIgnore
    @EmbeddedId
    private PairId id;

    @Transient
    @JsonIgnore
    public final static HashSet<String> attributes = new HashSet<>(Arrays.asList("targetDomainId", "type", "sourceDomainId", "actions", "databases"));

    @Transient
    private String targetDomainId;
    @Transient
    private String sourceDomainId;

    private String actions;

    private String sourceDatabases;

    public DrugHasTargetProtein() {
    }

    public String getTargetDomainId() {
        return targetDomainId;
    }

    public String getSourceDomainId() {
        return sourceDomainId;
    }

    @JsonGetter
    public String getType() {
        return "DrugHasTarget";
    }

    @JsonSetter
    public void setType(String type) {
    }

    public List<String> getActions() {
        return StringUtils.stringToList(actions);
    }

    public void setActions(List<String> actions) {
        this.actions = StringUtils.listToString(actions);
    }

    public List<String> getDatabases() {
        return StringUtils.stringToList(sourceDatabases);
    }

    public void setDatabases(List<String> databases) {
        this.sourceDatabases = StringUtils.listToString(databases);
    }

    public void setValues(DrugHasTargetProtein other) {
        this.sourceDomainId = other.sourceDomainId;
        this.targetDomainId = other.targetDomainId;
        this.actions = other.actions;
        this.sourceDatabases = other.sourceDatabases;
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
