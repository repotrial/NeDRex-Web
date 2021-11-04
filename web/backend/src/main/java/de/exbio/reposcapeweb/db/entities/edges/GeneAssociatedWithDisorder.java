package de.exbio.reposcapeweb.db.entities.edges;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.RepoTrialEdge;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Entity class for a transitive edge "GeneAssociatedWithDisorder" (gene_associated_with_disorder in db).
 * <i>These entries are derived by combining {@link ProteinAssociatedWithDisorder} and {@link ProteinEncodedBy} for giving the opportunity for better data discovery.</i>
 * The attributes primaryDomainIds of {@link GeneAssociatedWithDisorder#sourceDomainId} and {@link GeneAssociatedWithDisorder#targetDomainId} contain the ids of the {@link de.exbio.reposcapeweb.db.entities.nodes.Gene} (source) and a identified association with a {@link de.exbio.reposcapeweb.db.entities.nodes.Disorder} (target).
 * These primaryDomainIds from RepoTrial are converted to numeric node ids autogenerated on insertion of the nodes into the db.
 * Further the "type" attribute is also not included in the database, having only one possible value.
 * The class extends the {@link RepoTrialEdge} abstract class, for some methods used during import of the different RepoTrial entities.
 *
 * @author Andreas Maier
 */
@Entity
@Table(name = "gene_associated_with_disorder")
public class GeneAssociatedWithDisorder extends RepoTrialEdge implements Serializable {

    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(GeneAssociatedWithDisorder.class);

    @JsonIgnore
    @EmbeddedId
    private PairId id;
    @Transient
    @JsonIgnore
    public static HashSet<String> sourceAttributes;

    @Transient
    @JsonIgnore
    public static String[] allAttributes;

    @Transient
    @JsonIgnore
    public static String[] allAttributeTypes;

    @Transient
    @JsonIgnore
    public static Boolean[] detailAttributes;

    @Transient
    @JsonIgnore
    public static String[] attributeLabels;

    @Transient
    @JsonIgnore
    public static HashMap<String,String> name2labelMap;

    @Transient
    @JsonIgnore
    public static HashMap<String,String> label2NameMap;

    @Transient
    @JsonIgnore
    public static Boolean[] idAttributes;

    @Transient
    @JsonIgnore
    public static String[] listAttributes;



    public static String[] getListAttributes() {
        return listAttributes;
    }
    @Transient
    private String targetDomainId;
    @Transient
    private String sourceDomainId;

    @Transient
    private String nodeOne;
    @Transient
    private String nodeTwo;

    private Float score;

    private String assertedBy;

    private String omimFlags;

    private Integer omimMappingCode;


    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String,Object> values = new HashMap<>();
        values.put("targetDomainId",getTargetDomainId());
        values.put("sourceDomainId",getSourceDomainId());
        values.put("sourceId",id.getId1());
        values.put("targetId",id.getId2());
        values.put("node1",nodeOne);
        values.put("node2",nodeTwo);
        values.put("type",getType());
        values.put("score",getScore());
        values.put("assertedBy",getAssertedBy());
        values.put("omimFlags",getOmimFlags());
        values.put("omimMappingCode", getOmimMappingCode());
        values.put("id",id.getId1()+"-"+id.getId2());
        return values;
    }

    public void setNodeNames(String node1, String node2){
        nodeOne=node1;
        nodeTwo=node2;
    }

    @Override
    public HashMap<String, Object> getAsMap(HashSet<String> attributes) {
        HashMap<String,Object> values = new HashMap<>();
        getAsMap().forEach((k,v)->{
            if(attributes.contains(k))
                values.put(k,v);
        });
        return values;
    }

    public static void setUpNameMaps() {
        label2NameMap=new HashMap<>();
        name2labelMap = new HashMap<>();
        for (int i = 0; i < allAttributes.length; i++) {
            label2NameMap.put(allAttributes[i],attributeLabels[i]);
            name2labelMap.put(attributeLabels[i], allAttributes[i]);
        }
    }

    public GeneAssociatedWithDisorder() {
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

    public Float getScore() {
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

    public void setValues(GeneAssociatedWithDisorder other) {
        this.sourceDomainId = other.sourceDomainId;
        this.targetDomainId = other.targetDomainId;
        this.assertedBy = other.assertedBy;
        this.score = other.score;
    }

    public List<String> getOmimFlags() {
        return StringUtils.stringToList(omimFlags);
    }

    public void setOmimFlags(List<String> omimFlags) {
        this.omimFlags = StringUtils.listToString(omimFlags);
    }

    public Integer getOmimMappingCode() {
        return omimMappingCode;
    }

    public void setOmimMappingCode(Integer omimMappingCode) {
        this.omimMappingCode = omimMappingCode;
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

    public void setTargetDomainId(String targetDomainId) {
        this.targetDomainId = targetDomainId;
    }

    public void setSourceDomainId(String sourceDomainId) {
        this.sourceDomainId = sourceDomainId;
    }
}
