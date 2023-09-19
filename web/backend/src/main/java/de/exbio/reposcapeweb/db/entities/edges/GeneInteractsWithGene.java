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

import jakarta.persistence.*;
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

    @Column(columnDefinition = "TEXT")
    private String developmentStages;
    @Column(columnDefinition = "TEXT")
    private String tissues;
    @Column(columnDefinition = "TEXT")
    private String subcellularLocations;
    @Column(columnDefinition = "TEXT")
    private String jointTissues;
    @Column(columnDefinition = "TEXT")
    private String brainTissues;
    @Column(columnDefinition = "TEXT")
    private String methods;


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
        values.put("evidenceTypes", getEvidenceTypes());
        values.put("id", id.getId1() + "-" + id.getId2());
        values.put("methods", getMethods());
        values.put("dataSources",getDataSources());
        values.put("developmentStages", getDevelopmentStages());
        values.put("tissues", getTissues());
        values.put("subcellularLocations", getSubcellularLocations());
        values.put("jointTissues", getJointTissues());
        values.put("brainTissues", getBrainTissues());
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

    public List<String> getMethods() {
        return StringUtils.stringToList(methods);
    }

    public void setMethods(List<String> methods) {
        this.methods = StringUtils.listToString(methods);
    }

    public List<String> getDevelopmentStages() {
        return StringUtils.stringToList(developmentStages);
    }

    public void setDevelopmentStages(List<String> developmentStages) {
        this.developmentStages = StringUtils.listToString(developmentStages);
    }

    public List<String> getTissues() {
        return StringUtils.stringToList(tissues);
    }

    public void setTissues(List<String> tissues) {
        this.tissues = StringUtils.listToString(tissues);
    }

    public List<String> getSubcellularLocations() {
        return StringUtils.stringToList(subcellularLocations);
    }

    public void setSubcellularLocations(List<String> subcellularLocations) {
        this.subcellularLocations = StringUtils.listToString(subcellularLocations);
    }

    public List<String> getJointTissues() {
        return StringUtils.stringToList(jointTissues);
    }

    public void setJointTissues(List<String> jointTissues) {
        this.jointTissues = StringUtils.listToString(jointTissues);
    }

    public List<String> getBrainTissues() {
        return StringUtils.stringToList(brainTissues);
    }

    public void setBrainTissues(List<String> brainTissues) {
        this.brainTissues = StringUtils.listToString(brainTissues);
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
        this.methods = other.methods;
        this.developmentStages = other.developmentStages;
        this.brainTissues = other.brainTissues;
        this.jointTissues = other.jointTissues;
        this.tissues = other.tissues;
        this.subcellularLocations = other.subcellularLocations;
        this.evidenceTypes = other.evidenceTypes;
        this.dataSources = other.dataSources;
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

    public void addDataSources(List<String> databases) {
        List<String> all;
        if (this.dataSources == null) {
            all = new LinkedList<>(databases);
        } else {
            all = getDataSources();
            for (String t : databases) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setDataSources(all);
    }

    public void addMethod(List<String> methods) {
        List<String> all;
        if (this.methods == null) {
            all = new LinkedList<>(methods);
        } else {
            all = getMethods();
            for (String t : methods) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setMethods(all);
    }

    public void addDevelopmentStages(List<String> developmentStages) {
        List<String> all;
        if (this.developmentStages == null) {
            all = new LinkedList<>(developmentStages);
        } else {
            all = getDevelopmentStages();
            for (String t : developmentStages) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setDevelopmentStages(all);
    }

    public void addTissues(List<String> tissues) {
        List<String> all;
        if (this.tissues == null) {
            all = new LinkedList<>(tissues);
        } else {
            all = getTissues();
            for (String t : tissues) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setTissues(all);
    }

    public void addSubcellularLocations(List<String> subcellularLocations) {
        List<String> all;
        if (this.subcellularLocations == null) {
            all = new LinkedList<>(subcellularLocations);
        } else {
            all = getSubcellularLocations();
            for (String t : subcellularLocations) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setSubcellularLocations(all);
    }

    public void addJointTissues(List<String> jointTissues) {
        List<String> all;
        if (this.jointTissues == null) {
            all = new LinkedList<>(jointTissues);
        } else {
            all = getJointTissues();
            for (String t : jointTissues) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setJointTissues(all);
    }

    public void addBrainTissues(List<String> brainTissues) {
        List<String> all;
        if (this.brainTissues == null) {
            all = new LinkedList<>(brainTissues);
        } else {
            all = getBrainTissues();
            for (String t : brainTissues) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setBrainTissues(all);
    }

}
