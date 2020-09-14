package de.exbio.reposcapeweb.reponses;

import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.services.edges.DisorderComorbidWithDisorderService;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

@Service
public class WebGraphService {

    private final DisorderService disorderService;
    private final DisorderComorbidWithDisorderService disorderComorbidWithDisorderService;

    @Autowired
    public WebGraphService(DisorderService disorderService, DisorderComorbidWithDisorderService disorderComorbidWithDisorderService) {
        this.disorderService = disorderService;
        this.disorderComorbidWithDisorderService = disorderComorbidWithDisorderService;
    }


    public WebGraph getCancerComorbidity() {
        WebGraph graph = new WebGraph("Cancer comorbidity Network");
        HashMap<Integer, WebNode> ids = new HashMap<>();
        disorderService.getFilter().filterMatches(".*(cancer)|(tumor)|(tumour).*", 1000).forEach(entry -> {
            WebNode n = new WebNode(entry.getNodeId(), entry.getName());
            ids.put(entry.getNodeId(), n);
        });

        ids.keySet().forEach(id1 -> ids.keySet().forEach(id2 -> {
            if (disorderComorbidWithDisorderService.isEdge(id1, id2)) {
                graph.addEdge(new WebEdge(id1, id2));
                ids.get(id1).hasEdge = true;
                ids.get(id2).hasEdge = true;
            }
        }));

        ids.values()/*.stream().filter(n -> n.hasEdge)*/.forEach(graph::addNode);
//        graph.drawCircular();
        graph.drawDoubleCircular();
        //TODO cache edges

        return graph;
    }
}
