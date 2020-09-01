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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

    @JsonIgnore
    @Transient
    public static HashMap<Integer,String> idToDomainMap = new HashMap<>();
    @JsonIgnore
    @Transient
    public static HashMap<String,Integer> domainToIdMap = new HashMap<>();

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

//    public static boolean validateFormat(HashSet<String> attributes) {
//        for (String a : Protein.attributes)
//            if (!attributes.remove(a))
//                return false;
//        return attributes.isEmpty();
//    }

    @Override
    public String getPrimaryId() {
        return getPrimaryDomainId();
    }

//    @Override
//    public HashMap<Integer, String> getIdToDomainMap() {
//        if(Drug.idToDomainMap==null)
//            Drug.idToDomainMap=new HashMap<>();
//        return Drug.idToDomainMap;
//    }
//
//    @Override
//    public HashMap<String, Integer> getDomainToIdMap() {
//        if(Drug.domainToIdMap==null)
//            Drug.domainToIdMap=new HashMap<>();
//        return Drug.domainToIdMap;
//    }

    @Override
    public String getUniqueId() {
        return getPrimaryId();
    }

}
