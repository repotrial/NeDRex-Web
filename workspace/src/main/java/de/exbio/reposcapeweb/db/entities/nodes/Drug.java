package de.exbio.reposcapeweb.db.entities.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.db.entities.RepoTrialEntity;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "drugs", indexes = @Index(name = "domainId", columnList = "primaryDomainId", unique = true))
public class Drug implements RepoTrialEntity {
    @Transient
    @JsonIgnore
    private Logger log = LoggerFactory.getLogger(Drug.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private long id;
    @Transient
    @JsonIgnore
    private static HashSet<String> attributes = new HashSet<>(Arrays.asList("molecularFormula", "displayName", "inchi", "type", "domainIds", "primaryDomainId", "smiles", "casNumber", "drugCategories", "drugGroups", "_cls", "sequences", "iupacName", "synonyms", "primaryDataset", "indication", "allDatasets", "description"));



    private String _cls;
    private String primaryDomainId;
    private String domainIds;
    private String displayName;
    @Column(columnDefinition = "TEXT")
    private String synonyms;
    private DrugType type;
    @Column(columnDefinition = "TEXT")
    private String drugCategories;
    private String drugGroups;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String casNumber;
    @Column(columnDefinition = "TEXT")
    private String indication;
    @Column(columnDefinition = "TEXT")
    private String sequences;
    @Column(columnDefinition = "TEXT")
    private String iupacName;
    @Column(columnDefinition = "TEXT")
    private String smiles;
    //    @Lob
    @Column(columnDefinition = "TEXT")
    private String inchi;
    private String primaryDataset;
    private String allDatasets;
    private String molecularFormula;


    public Drug() {
        super();
    }

    public static boolean validateFormat(HashSet<String> attributes) {
        for (String a : Drug.attributes)
            if (!attributes.remove(a))
                return false;
        return attributes.isEmpty();
    }

    @Override
    public String getPrimaryId() {
        return primaryDomainId;
    }

//    @Override
//    public String toTsv() {
//        StringBuilder sb = new StringBuilder();
////        sb.
//        return null;
//    }
//
//    @Override
//    public String getHeader() {
//        return "#casNumber\tdescription\tdisplayName\tdomainIds\t";
//    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPrimaryDomainId() {
        return primaryDomainId;
    }

    public List<String> getDomainIds() {
        return StringUtils.stringToList(domainIds);
    }

    public String get_cls() {
        return _cls;
    }

    public List<String> getSynonyms() {
        return StringUtils.stringToList(synonyms);
    }

    public String getPrimaryDataset() {
        return primaryDataset;
    }

    public List<String> getAllDatasets() {
        return StringUtils.stringToList(allDatasets);
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDrugCategories() {
        return StringUtils.stringToList(drugCategories);
    }

    public List<String> getDrugGroups() {
        return StringUtils.stringToList(drugGroups);
    }

    public String getDescription() {
        return description;
    }

    public String getCasNumber() {
        return casNumber;
    }

    @JsonSetter
    public void setType(String type) {
        this.type = DrugType.valueOf(type);
    }


    public void setType(DrugType type) {
        this.type = type;
    }

    public DrugType getType() {
        return type;
    }

    public String getIndication() {
        return indication;
    }

    public List<String> getSequences() {
        return StringUtils.stringToList(sequences);
    }

    public String getIupacName() {
        return iupacName;
    }

    public String getSmiles() {
        return smiles;
    }

    public String getInchi() {
        return inchi;
    }

    public String getMolecularFormula() {
        return molecularFormula;
    }

    @JsonSetter
    public void setDomainIds(List<String> domainIds) {
        this.domainIds = StringUtils.listToString(domainIds);
    }

    @JsonSetter
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = StringUtils.listToString(synonyms);
    }

    @JsonSetter
    public void setDrugCategories(List<String> drugCategories) {
        this.drugCategories = StringUtils.listToString(drugCategories);
    }

    @JsonSetter
    public void setDrugGroups(List<String> drugGroups) {
        this.drugGroups = StringUtils.listToString(drugGroups);
    }

    @JsonSetter
    public void setSequences(List<String> sequences) {
        this.sequences = StringUtils.listToString(sequences);
    }

    @JsonSetter
    public void setAllDatasets(List<String> allDatasets) {
        this.allDatasets = StringUtils.listToString(allDatasets);
    }

    public void setValues(Drug other) {
        this._cls=other._cls;
        this.domainIds=other.domainIds;
        this.displayName=other.displayName;
        this.synonyms=other.synonyms;
        this.type=other.type;
        this.drugCategories=other.drugCategories;
        this.drugGroups=other.drugGroups;
        this.description=other.description;
        this.casNumber=other.casNumber;
        this.indication=other.indication;
        this.sequences=other.sequences;
        this.iupacName=other.iupacName;
        this.smiles=other.smiles;
        this.inchi=other.inchi;
        this.primaryDataset=other.primaryDataset;
        this.allDatasets=other.allDatasets;
        this.molecularFormula=other.molecularFormula;
    }
}

