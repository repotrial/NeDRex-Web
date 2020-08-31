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

@Entity
@Table(name = "pathways", indexes = @Index(name = "domainId", columnList = "primaryDomainId", unique = true))
public class Pathway implements RepoTrialEntity {
    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(Pathway.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private long id;
    @Transient
    @JsonIgnore
    private static final HashSet<String> attributes = new HashSet<>(Arrays.asList("displayName", "type", "domainIds", "primaryDomainId", "species"));

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

    public static boolean validateFormat(HashSet<String> attributes) {
        for (String a : Pathway.attributes)
            if (!attributes.remove(a))
                return false;
        return attributes.isEmpty();
    }

    @Override
    public String getPrimaryId() {
        return getPrimaryDomainId();
    }
}
