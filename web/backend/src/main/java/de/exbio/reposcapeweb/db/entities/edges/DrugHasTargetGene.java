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
 * Entity class for the RepoTrial edge "DrugHasTargetGene" (drug_has_target_gene in db).
 * The attributes primaryDomainIds of {@link DrugHasTargetGene#sourceDomainId} and {@link DrugHasTargetGene#targetDomainId} contain the ids of the {@link de.exbio.reposcapeweb.db.entities.nodes.Drug} (source) having an effect on a {@link de.exbio.reposcapeweb.db.entities.nodes.Gene} (target).
 * These primaryDomainIds from RepoTrial are converted to numeric node ids autogenerated on insertion of the nodes into the db.
 * Further the "type" attribute is also not included in the database, having only one possible value.
 * The class extends the {@link RepoTrialEdge} abstract class, for some methods used during import of the different RepoTrial entities.
 *
 * @author Andreas Maier
 */
@Entity
@Table(name = "drug_has_target_gene")
public class DrugHasTargetGene extends RepoTrialEdge implements Serializable {

    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(DrugHasTargetGene.class);

    @JsonIgnore
    @EmbeddedId
    private PairId id;

    @Transient
    private String targetDomainId;
    @Transient
    private String sourceDomainId;

    @Transient
    @JsonIgnore
    public static Boolean[] detailAttributes;

    @Transient
    private String nodeOne;
    @Transient
    private String nodeTwo;

    public DrugHasTargetGene() {
    }

    public DrugHasTargetGene(int drugId, int geneId) {
        this.id=new PairId(drugId,geneId);
    }

    public String getTargetDomainId() {
        return targetDomainId;
    }

    public String getSourceDomainId() {
        return sourceDomainId;
    }

    @Transient
    @JsonIgnore
    public static String[] allAttributes;

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

    private String sourceDatabases;

    private String actions;

    private String tags;


    public static String[] getListAttributes() {
        return listAttributes;
    }

    @JsonGetter
    public String getType() {
        return "DrugHasTargetGene";
    }

    @JsonSetter
    public void setType(String type) {
    }


    @Override
    public HashMap<String, Object> getAsMap() {
        HashMap<String,Object> values = new HashMap<>();
        values.put("targetDomainId",getTargetDomainId());
        values.put("sourceDomainId",getSourceDomainId());
        values.put("sourceId",id.getId1());
        values.put("targetId",id.getId2());
        values.put("node1",nodeOne);
        values.put("node2",nodeTwo);
        values.put("databases",getDatabases());
        values.put("actions",getActions());
        values.put("dataSources", getDataSources());
        values.put("tags",getTags());
        values.put("type",getType());
        values.put("id",id.getId1()+"-"+id.getId2());
        return values;
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

    public List<String> getDatabases() {
        return StringUtils.stringToList(sourceDatabases);
    }

    public void setDatabases(List<String> databases) {
        this.sourceDatabases = StringUtils.listToString(databases);
    }


    public static void setUpNameMaps() {
        label2NameMap=new HashMap<>();
        name2labelMap = new HashMap<>();
        for (int i = 0; i < allAttributes.length; i++) {
            label2NameMap.put(allAttributes[i],attributeLabels[i]);
            name2labelMap.put(attributeLabels[i], allAttributes[i]);
        }
    }

    public void setNodeNames(String node1, String node2){
        nodeOne=node1;
        nodeTwo=node2;
    }

    public List<String> getActions() {
        return StringUtils.stringToList(actions);
    }

    public void setActions(List<String> actions) {
        this.actions = StringUtils.listToString(actions);
    }

    public List<String> getTags() {
        return StringUtils.stringToList(tags);
    }

    public void setTags(List<String> tags) {
        this.tags = StringUtils.listToString(tags);
    }


    public void setValues(DrugHasTargetGene other) {
        this.sourceDomainId = other.sourceDomainId;
        this.targetDomainId = other.targetDomainId;
        this.actions = other.actions;
        this.sourceDatabases = other.sourceDatabases;
        this.tags = other.tags;
        this.dataSources = other.dataSources;
    }

    public void addActions(Collection<String> actions){
        List<String> all;
        if (this.actions == null) {
            all = new LinkedList<>(actions);
        } else {
            all = getActions();
            for (String t : actions) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setActions(all);
    }

    public void addTags(Collection<String> tags){
        List<String> all;
        if (this.tags == null) {
            all = new LinkedList<>(tags);
        } else {
            all = getTags();
            for (String t : tags) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setTags(all);
    }

    public void addDatabases(List<String> databases) {
        List<String> all;
        if (this.sourceDatabases == null) {
            all = new LinkedList<>(databases);
        } else {
            all = getDatabases();
            for (String t : databases) {
                if (!all.contains(t))
                    all.add(t);
            }
        }
        setDatabases(all);
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
