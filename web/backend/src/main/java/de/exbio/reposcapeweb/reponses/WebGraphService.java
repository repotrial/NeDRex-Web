package de.exbio.reposcapeweb.reponses;

import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class WebGraphService {

    private final DisorderService disorderService;

    @Autowired
    public WebGraphService(DisorderService disorderService) {
        this.disorderService = disorderService;
    }


    public WebGraph getCancerComorbidity() {
        WebGraph graph = new WebGraph("Cancer comorbidity Network");
        //TODO fix caching of filter
        System.out.println(disorderService.getFilter().size());
        HashSet<Integer> ids = new HashSet<>();
        System.out.println(disorderService.getFilter());
        disorderService.getFilter().filterMatches("(cancer)|(tumor)|(tumour)", 1000).forEach(entry -> {
            graph.addNode(new WebNode(entry.getNodeId(), entry.getName()));
            ids.add(entry.getNodeId());
        });

        //TODO cache edges

        return graph;
    }
}
