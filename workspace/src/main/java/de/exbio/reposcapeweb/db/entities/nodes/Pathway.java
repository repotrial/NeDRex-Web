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
import java.util.LinkedList;
import java.util.Map;

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

    private String primaryDomainId;

    private String displayName;

    private String domainIds;

    private String species;

    public Pathway() {

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

    public LinkedList<String> getSpecies() {
        return StringUtils.stringToList(species);
    }

    @JsonGetter
    public String getType() {
        return "Pathway";
    }

    @JsonSetter
    public void setType(String type){}

    public void setValues(Pathway other) {
        this.species = other.species;
        this.domainIds = other.domainIds;
        this.primaryDomainId = other.primaryDomainId;
        this.displayName=other.displayName;

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
