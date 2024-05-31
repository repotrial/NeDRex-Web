package de.exbio.reposcapeweb.db.services.edges;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.controller.NedrexService;
import de.exbio.reposcapeweb.db.entities.edges.DisorderComorbidWithDisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.DisorderComorbidWithDisorderRepository;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.utils.FileUtils;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DisorderComorbidWithDisorderService {
    private final Logger log = LoggerFactory.getLogger(DisorderComorbidWithDisorderService.class);

    private final DisorderComorbidWithDisorderRepository disorderComorbidWithDisorderRepository;

    private final DisorderService disorderService;

    private final String[] comorbiditomeBuildID = new String[1];

    private final NedrexService nedrexService;

    private final ObjectMapper objectMapper;

    //TODO set to directed value of config
    private final boolean directed = false;
    private final HashMap<Integer, HashMap<PairId, Boolean>> edges = new HashMap<>();

    @Autowired
    public DisorderComorbidWithDisorderService(ObjectMapper objectMapper, DisorderService disorderService, DisorderComorbidWithDisorderRepository disorderComorbidWithDisorderRepository, NedrexService nedrexService) {
        this.disorderService = disorderService;
        this.nedrexService = nedrexService;
        this.objectMapper = objectMapper;
        this.disorderComorbidWithDisorderRepository = disorderComorbidWithDisorderRepository;
    }


    public void buildComorbiditome() {
        HashSet<String> mondoIds = new HashSet<>();
        disorderService.findAll().forEach(d -> mondoIds.add(d.getPrimaryDomainId()));
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("mondo", mondoIds.toArray());
        String response = this.nedrexService.post("/comorbiditome/submit_comorbiditome_build", payload);
        comorbiditomeBuildID[0] = response.substring(1, response.length() - 2);
    }

    public String statusComorbiditome() {
        String status = this.nedrexService.get("/comorbiditome/comorbiditome_build_status?uid=" + comorbiditomeBuildID[0]);
        try {
            return objectMapper.readValue(status, HashMap.class).get("status").toString();
        } catch (JsonProcessingException e) {
            return "error";
        }
    }

    public void waitForBuild() {
        boolean done = false;
        while (!done) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String status = statusComorbiditome();
            System.out.println(status);
            if (status.equals("completed"))
                done = true;
            else if (status.equals("error"))
                throw new RuntimeException("Comorbiditome build failed!");
        }
    }

    public boolean importComorbiditome(File updateDir) {
        try {
            waitForBuild();
            String responseURL = this.nedrexService.getAPI() + "comorbiditome/download_comorbiditome_build/" + this.comorbiditomeBuildID[0] + "/graphml/comorbiditome.tsv";
            FileUtils.download(responseURL, new File(updateDir, "comorbiditome.tsv"), this.nedrexService.getAPIKey());
            HashMap<PairId, DisorderComorbidWithDisorder> comorbidities = readComorbiditome(new File(updateDir, "comorbiditome.tsv"));
            disorderComorbidWithDisorderRepository.deleteAll();
            log.info("Deleted disorder_comorbid_with_disorder table");
            log.info("Importing " + comorbidities.size() + " comorbidities");
            submitUpdates(comorbidities);
            log.debug("Updated disorder_comorbid_with_disorder table: " + comorbidities.size() + " Inserts");
            return true;
        }catch (Exception e){
            log.error("Could not import comorbiditome: " + e.getMessage());
            return false;
        }
    }

    public HashMap<String, ArrayList<String>> getIcd10ToMondoMap(HashSet<String> icd10Ids) {
        String url = "comorbiditome/icd10_to_mondo";
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("icd10", icd10Ids.toArray());
        String response = this.nedrexService.post(url, payload);
        try {
            HashMap<String, ArrayList<String>> map = objectMapper.readValue(response, HashMap.class);
            return map;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public HashMap<PairId, DisorderComorbidWithDisorder> readComorbiditome(File comorbiditome) {
        HashMap<PairId, DisorderComorbidWithDisorder> comorbidities = new HashMap<>();
        HashMap<String, ArrayList<String>> icd10ToMondoMap = new HashMap<>();
        try {
            BufferedReader br = ReaderUtils.getBasicReader(comorbiditome.getAbsolutePath());
            String line = "";
            boolean first_edge = true;
            boolean reset_edge = false;
            DisorderComorbidWithDisorder dcwd = new DisorderComorbidWithDisorder();
            HashSet<String> icds = new HashSet<>();
            String icd1 = "";
            String icd2 = "";


            while ((line = br.readLine()) != null) {
                line = line.trim();
                try {
                    if (first_edge || reset_edge) {
                        if (line.startsWith("<edge")) {
                            if (first_edge) {
                                icd10ToMondoMap = getIcd10ToMondoMap(icds);
                            }
                            first_edge = false;
                            reset_edge = false;
                        } else {
                            if (line.trim().startsWith("<data key=\"d1\">")) {
                                String icd10 = line.substring(21, line.length() - 7);
                                icds.add(icd10);
                            }
                        }
                    }
                    if (line.startsWith("</edge")) {
                        for (String mondo1 : icd10ToMondoMap.get(icd1)) {
                            for (String mondo2 : icd10ToMondoMap.get(icd2)) {
                                DisorderComorbidWithDisorder add = new DisorderComorbidWithDisorder();
                                int id1 = disorderService.map(mondo1);
                                int id2 = disorderService.map(mondo2);
                                add.setValues(dcwd);
                                add.setId(id1 <= id2 ? new PairId(id1, id2) : new PairId(id2, id1));
                                comorbidities.put(add.getPrimaryIds(), add);
                            }
                        }
                        dcwd = new DisorderComorbidWithDisorder();
                        continue;
                    }
                    if (line.startsWith("<edge")) {
                        LinkedList<String> split = StringUtils.split(line, " ");
                        icd1 = split.get(1).substring(8, split.get(1).length() - 1);
                        icd2 = split.get(2).substring(8, split.get(2).length() - 2);
                        continue;
                    }
                    if (line.startsWith("<data key=\"d6\">")) {
                        int occs = Integer.parseInt(line.trim().substring(15, line.length() - 7));
                        dcwd.setOccurrences(occs);
                        continue;
                    }
                    if (line.startsWith("<data key=\"d7\">")) {
                        float phiCorr = Float.parseFloat(line.trim().substring(15, line.length() - 7));
                        dcwd.setPhiCor(phiCorr);
                        continue;
                    }
                    if (line.startsWith("<data key=\"d8\">")) {
                        float pVal = Float.parseFloat(line.trim().substring(15, line.length() - 7));
                        dcwd.setpValue(pVal);
                        continue;
                    }
                } catch (Exception e) {
                    log.info("Could not read comorbidity data: " + line);
                    reset_edge = true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return comorbidities;
    }

    public boolean submitUpdates(HashMap<PairId, DisorderComorbidWithDisorder> updates) {

        if (updates == null)
            return false;


        HashSet<DisorderComorbidWithDisorder> batch = new HashSet<>();
        updates.values().forEach(p -> {
            batch.add(p);
            if (batch.size() >= 10_000) {
                disorderComorbidWithDisorderRepository.saveAll(batch);
                batch.clear();
            }
        });
        disorderComorbidWithDisorderRepository.saveAll(batch);

        log.debug("Updated comorbid_with_disorder table: " + updates.size() + " Inserts");
        return true;
    }

    public List<DisorderComorbidWithDisorder> getEntries(Collection<PairId> toFind) {
        toFind.forEach(p -> {
            if (p.getId1() > p.getId1())
                p.flipIds();
        });
        return disorderComorbidWithDisorderRepository.findDisorderComorbidWithDisordersByIdIn(toFind);
    }

    //
    public List<DisorderComorbidWithDisorder> findAll() {
        return disorderComorbidWithDisorderRepository.findAll();
    }

    public List<PairId> getAllIDs() {
        LinkedList<PairId> allIDs = new LinkedList<>();
        edges.values().forEach(v -> allIDs.addAll(v.keySet()));
        return allIDs;
    }

    public void importEdges() {
        edges.clear();
        findAll().forEach(edge -> {
            importEdge(edge.getPrimaryIds());
        });
    }

    private void importEdge(PairId edge) {
        if (!edges.containsKey(edge.getId1()))
            edges.put(edge.getId1(), new HashMap<>());
        edges.get(edge.getId1()).put(edge, true);

        if (!edges.containsKey(edge.getId2()))
            edges.put(edge.getId2(), new HashMap<>());
        edges.get(edge.getId2()).put(edge, !directed);
    }

    public boolean isEdge(PairId edge) {
        try {
            return edges.get(edge.getId1()).get(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isEdge(int id1, int id2) {
        return isEdge(new PairId(id1, id2));
    }

    public HashSet<PairId> getEdges(int id) {
        return edges.get(id).entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));
    }


    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(disorderService.map(ids.getFirst()), disorderService.map(ids.getSecond()));
    }

    public Optional<DisorderComorbidWithDisorder> find(PairId id) {
        if (id.getId1() > id.getId2())
            id.flipIds();
        return disorderComorbidWithDisorderRepository.findById(id);
    }

    public DisorderComorbidWithDisorder setDomainIds(DisorderComorbidWithDisorder item) {
        item.setMemberOne(disorderService.map(item.getPrimaryIds().getId1()));
        item.setMemberTwo(disorderService.map(item.getPrimaryIds().getId2()));

        item.setNodeNames(disorderService.getName(item.getPrimaryIds().getId1()), disorderService.getName(item.getPrimaryIds().getId2()));

        return item;
    }

    public boolean isDirected() {
        return directed;
    }

    public Long getCount() {
        return disorderComorbidWithDisorderRepository.count();
    }
}
