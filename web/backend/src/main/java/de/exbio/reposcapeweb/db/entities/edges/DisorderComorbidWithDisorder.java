package de.exbio.reposcapeweb.db.entities.edges;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.entities.RepoTrialEdge;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;


/**
 * Entity class for the RepoTrial edge "DisorderComorbidWithDisorder" (disorder_comorbid_with_disorder in db).
 * The attributes primaryDomainIds of {@link DisorderComorbidWithDisorder#memberOne} and {@link DisorderComorbidWithDisorder#memberTwo} describe two source ids of undirected connected {@link de.exbio.reposcapeweb.db.entities.nodes.Disorder} instances by "comorbidity".
 * These primaryDomainIds from RepoTrial are converted to numeric node ids autogenerated on insertion of the nodes into the db.
 * Further the "type" attribute is also not included in the database, having only one possible value.
 * The class extends the {@link RepoTrialEdge} abstract class, for some methods used during import of the different RepoTrial entities.
 *
 * @author Andreas Maier
 */
@Entity
@Table(name = "disorder_comorbid_with_disorder")
public class DisorderComorbidWithDisorder extends RepoTrialEdge implements Serializable {

    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(DisorderComorbidWithDisorder.class);

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
    public static Boolean[] detailAttributes;

    @Transient
    @JsonIgnore
    public static String[] allAttributeTypes;

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
    private String memberOne;
    @Transient
    private String memberTwo;
    private Float phiCor;

    private Float pValue;

    private Integer occurrences;
    @Transient
    private String nodeOne;
    @Transient
    private String nodeTwo;

    public DisorderComorbidWithDisorder() {
    }

    public String getMemberOne() {
        return memberOne;
    }

    public String getMemberTwo() {
        return memberTwo;
    }

    public Float getPhiCor() {
        return phiCor;
    }

    public Float getpValue() {
        return pValue;
    }

    public void setpValue(Float pValue) {
        this.pValue = pValue;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    public void setPhiCor(Float phiCor) {
        this.phiCor = phiCor;
    }

    @JsonGetter
    public String getType() {
        return "DisorderComorbidWithDisorder";
    }

    @JsonSetter
    public void setType(String type) {
    }

    public void setValues(DisorderComorbidWithDisorder other) {
        this.memberOne = other.memberOne;
        this.memberTwo = other.memberTwo;
        this.phiCor = other.phiCor;
        this.occurrences = other.occurrences;
        this.pValue = other.pValue;
        this.dataSources = other.dataSources;
    }

    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("pValue", getpValue());
        values.put("occurences", getOccurrences());
        values.put("type", getType());
        values.put("memberOne", getMemberOne());
        values.put("memberTwo", getMemberTwo());
        values.put("idOne", id.getId1());
        values.put("idTwo", id.getId2());
        values.put("node1", nodeOne);
        values.put("node2", nodeTwo);
        values.put("dataSources", getDataSources());
        values.put("phiCor", getPhiCor());
        values.put("id", id.getId1() + "-" + id.getId2());
        return values;
    }

    public void setNodeNames(String node1, String node2) {
        nodeOne = node1;
        nodeTwo = node2;
    }


    public static void setUpNameMaps() {
        label2NameMap=new HashMap<>();
        name2labelMap = new HashMap<>();
        for (int i = 0; i < allAttributes.length; i++) {
            label2NameMap.put(allAttributes[i],attributeLabels[i]);
            name2labelMap.put(attributeLabels[i], allAttributes[i]);
        }
    }
    @Column(columnDefinition = "TEXT")
    private String dataSources;
    @JsonGetter
    public LinkedList<String> getDataSources() {
        return StringUtils.stringToList(dataSources);
    }

    @JsonSetter
    public void setDataSources(List<String> dataSources) {
        this.dataSources = StringUtils.listToString(dataSources);
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

    @Override
    public PairId getPrimaryIds() {
        return id;
    }

    public void flipIds() {
        this.id.flipIds();
        String tmp = memberOne;
        memberOne = memberTwo;
        memberTwo = tmp;
    }

    public void setId(PairId id) {
        this.id = id;
    }

    @Override
    public Pair<String, String> getIdsToMap() {
        return new Pair<>(memberOne, memberTwo);
    }

    public void setMemberOne(String memberOne) {
        this.memberOne = memberOne;
    }

    public void setMemberTwo(String memberTwo) {
        this.memberTwo = memberTwo;
    }


}
