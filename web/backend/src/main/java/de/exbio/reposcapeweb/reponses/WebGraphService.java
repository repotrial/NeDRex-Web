package de.exbio.reposcapeweb.reponses;

import de.exbio.reposcapeweb.db.services.edges.DisorderComorbidWithDisorderService;
import de.exbio.reposcapeweb.db.services.edges.DrugHasIndicationService;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;

@Service
public class WebGraphService {

    private final DisorderService disorderService;
    private final DisorderComorbidWithDisorderService disorderComorbidWithDisorderService;
    private final DrugHasIndicationService drugHasIndicationService;
    private final DrugService drugService;

    @Autowired
    public WebGraphService(DisorderService disorderService,
                           DisorderComorbidWithDisorderService disorderComorbidWithDisorderService,
                           DrugHasIndicationService drugHasIndicationService,
                           DrugService drugService) {
        this.disorderService = disorderService;
        this.disorderComorbidWithDisorderService = disorderComorbidWithDisorderService;
        this.drugHasIndicationService = drugHasIndicationService;
        this.drugService = drugService;
    }


    public WebGraph getCancerComorbidity() {
        WebGraph graph = new WebGraph("Cancer comorbidity Network", false);
        HashMap<Integer, WebNode> ids = new HashMap<>();
        disorderService.getFilter().filterMatches(".*(cancer|tumor|tumour|carc).*", -1).forEach(entry -> {
            WebNode n = new WebNode(entry.getNodeId(), entry.getNodeId() + "", "disorder");
            ids.put(entry.getNodeId(), n);
            n.setTitle(entry.getName());
        });

        ids.keySet().forEach(id1 -> ids.keySet().forEach(id2 -> {
            if (disorderComorbidWithDisorderService.isEdge(id1, id2)) {
                graph.addEdge(new WebEdge(id1, id2));
                ids.get(id1).hasEdge = true;
                ids.get(id2).hasEdge = true;
            }
        }));

        ids.values().forEach(graph::addNode);
        graph.drawDoubleCircular();
        return graph;
    }

    public WebGraph getNeuroDrugs() {
        WebGraph graph = new WebGraph("Neuro Disorer Drugs", true);
        HashMap<Integer, WebNode> ids = new HashMap<>();
        HashSet<Integer> drugs = new HashSet<>();
        disorderService.getFilter().filterMatches(".*((neur)|(prion)|(brain)|(enceph)|(cogni)).*", -1).forEach(entry -> {
            WebNode n = new WebNode("dis_", entry.getNodeId(), entry.getName(), "disorder");
            ids.put(entry.getNodeId(), n);
            n.setTitle(entry.getName());
            HashSet<Integer> indications = drugHasIndicationService.getEdgesTo(entry.getNodeId());
            graph.addNode(n);
            try {
                drugs.addAll(indications);
                indications.forEach(drug -> {
                    graph.addEdge(new WebEdge("dr_"+drug, "dis_"+entry.getNodeId()));
                    n.hasEdge = true;
                });
            } catch (NullPointerException e){
            }
        });
        drugService.findAllByIds(drugs).forEach(d -> {
            WebNode n = new WebNode("dr_", d.getId(), d.getDisplayName(), "drugs");
            n.hasEdge = true;
            n.setTitle(d.getDisplayName());
            graph.addNode(n);
        });
        graph.drawDoubleCircular();
        return graph;
    }


}
