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

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;


/**
 * @author Andreas Maier
 */
@Entity
@Table(name = "gene_interacts_with_gene")
public class GeneInteractsWithGene extends RepoTrialEdge implements Serializable {


    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(GeneInteractsWithGene.class);

    @JsonIgnore
    @EmbeddedId
    private PairId id;

    @Transient
    private String memberOne;
    @Transient
    private String memberTwo;

    @Transient
    private String nodeOne;
    @Transient
    private String nodeTwo;

    private String evidenceTypes;
    private String assertedBy;


    public GeneInteractsWithGene() {
    }

    public GeneInteractsWithGene(int id1, int id2) {
        if (id1 < id2)
            id = new PairId(id1, id2);
        else
            id = new PairId(id2, id1);
    }


    @Transient
    @JsonIgnore
    public static String[] allAttributes;

    @Transient
    @JsonIgnore
    public static String[] allAttributeTypes;

    @Transient
    @JsonIgnore
    public static Boolean[] idAttributes;

    @Transient
    @JsonIgnore
    public static String[] attributeLabels;

    @Transient
    @JsonIgnore
    public static Boolean[] detailAttributes;

    @Transient
    @JsonIgnore
    public static HashMap<String, String> name2labelMap;

    @Transient
    @JsonIgnore
    public static HashMap<String, String> label2NameMap;

    @Transient
    @JsonIgnore
    public static String[] listAttributes;

    public static String[] getListAttributes() {
        return listAttributes;
    }


    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String, Object> values = new HashMap<>();
        values.put("memberOne", memberOne);
        values.put("memberTwo", memberTwo);
        values.put("idOne", id.getId1());
        values.put("idTwo", id.getId2());
        values.put("node1", nodeOne);
        values.put("node2", nodeTwo);
        values.put("type", getType());
        values.put("assertedBy",getAssertedBy());
        values.put("evidenceTypes", getEvidenceTypes());
        values.put("id", id.getId1() + "-" + id.getId2());
        return values;
    }

    public void setNodeNames(String node1, String node2) {
        nodeOne = node1;
        nodeTwo = node2;
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


    public List<String> getAssertedBy() {
        return StringUtils.stringToList(assertedBy);
    }

    public void setAssertedBy(List<String> databases) {
        this.assertedBy = StringUtils.listToString(databases);
    }


    public List<String> getEvidenceTypes() {
        return StringUtils.stringToList(evidenceTypes);
    }

    public void setEvidenceTypes(List<String> evidenceTypes) {
        this.evidenceTypes = StringUtils.listToString(evidenceTypes);
    }

    public static void setUpNameMaps() {
        label2NameMap = new HashMap<>();
        name2labelMap = new HashMap<>();
        for (int i = 0; i < allAttributes.length; i++) {
            label2NameMap.put(allAttributes[i], attributeLabels[i]);
            name2labelMap.put(attributeLabels[i], allAttributes[i]);
        }
    }

    public String getMemberOne() {
        return memberOne;
    }

    public String getMemberTwo() {
        return memberTwo;
    }

    @JsonGetter
    public String getType() {
        return "GeneInteractsWithGene";
    }

    @JsonSetter
    public void setType(String type) {
    }

    public void setValues(GeneInteractsWithGene other) {
        this.memberOne = other.memberOne;
        this.memberTwo = other.memberTwo;
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
        return new Pair<>(memberOne, memberTwo);
    }

    public void setMemberOne(String memberOne) {
        this.memberOne = memberOne;
    }

    public void setMemberTwo(String memberTwo) {
        this.memberTwo = memberTwo;
    }

    public void addEvidenceTypes(List<String> evidenceTypes) {
        List<String> all;
        if (this.evidenceTypes == null) {
            all = new LinkedList<>(evidenceTypes);
        } else {
            all = getEvidenceTypes();
            for (String t : evidenceTypes) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setEvidenceTypes(all);
    }

public void addAssertedBy(List<String> databases) {
    List<String> all;
    if (this.assertedBy == null) {
        all = new LinkedList<>(databases);
    } else {
        all = getAssertedBy();
        for (String t : databases) {
            if (!all.contains(t))
                all.add(t);
        }
    }
    setAssertedBy(all);
}
}
