package de.exbio.reposcapeweb;

import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.jobs.JobController;
import de.exbio.reposcapeweb.communication.reponses.Suggestions;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import de.exbio.reposcapeweb.communication.requests.SuggestionRequest;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.edges.DrugHasIndication;
import de.exbio.reposcapeweb.db.entities.edges.ProteinAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.nodes.Disorder;
import de.exbio.reposcapeweb.db.io.ImportService;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.edges.ProteinInteractsWithProteinService;
import de.exbio.reposcapeweb.db.updates.UpdateService;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.StringUtils;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class ReposcapewebApplication extends SpringBootServletInitializer {

    private Logger log = LoggerFactory.getLogger(ReposcapewebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ReposcapewebApplication.class, args);
    }

    @Autowired
    private UpdateService updateService;
    @Autowired
    private Environment env;
    @Autowired
    private ImportService importService;
    @Autowired
    private ToolService toolService;
    @Autowired
    private JobController jobController;
    @Autowired
    private EdgeController edgeController;
    @Autowired
    private NodeController nodeController;
    @Autowired
    private DbCommunicationService dbService;
    @Autowired
    WebGraphService webGraphService;

    @Autowired
    private ProteinInteractsWithProteinService ppiService;

//    @Autowired
//    public ReposcapewebApplication(DbCommunicationService dbService, ProteinInteractsWithProteinService proteinInteractsWithProteinService, JobController jobController, NodeController nodeController, ObjectMapper objectMapper, EdgeController edgeController, DisorderService disorderService, UpdateService updateService, Environment environment, ImportService importService, FilterService filterService, ToolService toolService, WebGraphService graphService) {
//        this.updateService = updateService;
//        this.importService = importService;
//        this.env = environment;
//        this.toolService = toolService;
//        this.jobController = jobController;
//        this.edgeController = edgeController;
//        this.nodeController = nodeController;
//        this.proteinInteractsWithProteinService = proteinInteractsWithProteinService;
//        this.dbService = dbService;
//
//    }

    public int hash(String params, TreeSet<Integer> ids, String method) {
        return Objects.hash(params, Arrays.hashCode(ids.toArray()), method);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void postConstruct() throws IOException {

        long start = System.currentTimeMillis();
        updateService.readMetadata();
        dbService.setImportInProgress(true);
        importService.importNodeData();



//        simnetmedFileCreation();

        //TODO maybe move importJob and importHistory to after update?
        jobController.importJobsHistory();
        importService.importHistory();


        toolService.validateTools();
        dbService.setImportInProgress(false);

        if (Boolean.parseBoolean(env.getProperty("update.onstartup"))) {
            updateService.scheduleDataUpdate();
        } else {
            importService.importEdges(false);
            log.warn("Startup Database update is deactivated! Activate it by setting 'update.onstartup=true' in the application.properties.");
        }

        if (env.getProperty("update.db-dump").equals("true"))
            updateService.renewDBDumps();
        log.info("Startup took " + (int) ((System.currentTimeMillis() - start) / 1000) + "s");
        log.debug("Current RAM usage: " + (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
                + "MB");
        log.info("Loaded " + nodeController.getCount() + " nodes and " + edgeController.getSize() + " edges!");
        log.info("Service can be used!");

//        drugstoneFileCreation();

    }

    private void simnetmedFileCreation() {
        System.out.println("Creating SimNetMed files");
        BufferedWriter bw = WriterUtils.getBasicWriter(new File("/home/andim/projects/SimNetMed/data/disorders.map"));
        HashSet<String> ids = new HashSet<>();
        LinkedList<String> order = new LinkedList<>();
        nodeController.findAll(Graphs.getNode("disorder")).forEach(disorder -> {
            Disorder dis = (Disorder) disorder;
            dis.getDomainIds().forEach(id -> {
                String idType = id.substring(0, id.indexOf("."));
                if(ids.add(idType))
                    order.add(idType);
            });
        });
        order.add("ICD-10");

        String header = "";
        for (String id : order) {
            if (header.length() == 0)
                header += "#";
            else
                header += "\t";
            header += id;
        }
        try {
            bw.write(header + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        nodeController.findAll(Graphs.getNode("disorder")).forEach(disorder -> {
            Disorder dis = (Disorder) disorder;
            HashMap<String, String> domainIds = new HashMap<>();
            dis.getDomainIds().forEach(id -> {
                int pos = id.indexOf(".");
                domainIds.put(id.substring(0, pos), id.substring(pos+1));
            });
            String icd = StringUtils.listToString(dis.getIcd10());
            domainIds.put("ICD-10", icd.substring(1,icd.length()-1));
            for (String idType : order) {
                try {
                    if (domainIds.containsKey(idType)) {
                        bw.write(domainIds.get(idType));
                    }
                    if (!idType.equals(order.getLast()))
                        bw.write("\t");
                    else bw.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finishing simnet file");


    }

    private void drugstoneFileCreation() throws IOException {
        System.out.println("Creating Drugstone files");
        HashMap<Integer, String> disorders = new HashMap<>();
        HashMap<Integer, String> proteins = new HashMap<>();
        HashMap<Integer, String> drugs = new HashMap<>();

        edgeController.findAll(5).forEach(e -> {
            ProteinAssociatedWithDisorder pad = (ProteinAssociatedWithDisorder) e;
            if (pad.getAssertedBy().contains("disgenet")) {
                proteins.put(pad.getPrimaryIds().getId1(), null);
                disorders.put(pad.getPrimaryIds().getId2(), null);
            }
        });

        edgeController.findAll(10).forEach(e -> {
            DrugHasIndication di = (DrugHasIndication) e;
            drugs.put(di.getPrimaryIds().getId1(), null);
            disorders.put(di.getPrimaryIds().getId2(), null);
        });


        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/andim/projects/drugstone/backend/data-NetExpander/Disorders/disorders.tsv"));
        bw.write("mondo_id\tlabel\ticd10\n");
        BufferedWriter finalBw = bw;
        nodeController.findDisorders(disorders.keySet()).forEach(disorder -> {
            try {
                String domainId = StringUtils.split(disorder.getPrimaryDomainId(), '.').get(1);
                disorders.put(disorder.getId(), domainId);
                finalBw.write(domainId + "\t" + disorder.getDisplayName() + "\t" + StringUtils.listToString(disorder.getIcd10()) + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bw.close();

        nodeController.findProteins(proteins.keySet()).forEach(protein -> {
            proteins.put(protein.getId(), StringUtils.split(protein.getPrimaryDomainId(), '.').get(1));
        });


        bw = new BufferedWriter(new FileWriter("/home/andim/projects/drugstone/backend/data-NetExpander/PDi/disgenet-protein_disorder_association.tsv"));
        bw.write("protein_name\tdisorder_name\tscore\n");
        BufferedWriter finalBw1 = bw;
        edgeController.findAll(5).forEach(e -> {
            ProteinAssociatedWithDisorder pad = (ProteinAssociatedWithDisorder) e;
            if (pad.getAssertedBy().contains("disgenet")) {
                try {
                    finalBw1.write(proteins.get(pad.getPrimaryIds().getId1()) + "\t" + disorders.get(pad.getPrimaryIds().getId2()) + "\t" + pad.getScore() + "\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        bw.close();

        proteins.clear();

        nodeController.findDrugs(drugs.keySet()).forEach(drug -> {
            drugs.put(drug.getId(), StringUtils.split(drug.getPrimaryDomainId(), '.').get(1));
        });


        bw = new BufferedWriter(new FileWriter("/home/andim/projects/drugstone/backend/data-NetExpander/DrDi/drugbank-drug_disorder_indication.tsv"));
        bw.write("drugbank_id\tmondo_id\n");
        BufferedWriter finalBw2 = bw;
        edgeController.findAll(10).forEach(e -> {
            DrugHasIndication di = (DrugHasIndication) e;
            try {
                finalBw2.write(drugs.get(di.getPrimaryIds().getId1()) + "\t" + disorders.get(di.getPrimaryIds().getId2()) + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        bw.close();
        System.out.println("Done creating Drugstone files");
    }


}
