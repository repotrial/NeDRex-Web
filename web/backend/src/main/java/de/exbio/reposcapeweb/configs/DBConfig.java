package de.exbio.reposcapeweb.configs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class DBConfig {


    private static HashMap<String, HashMap<String, HashSet<String>>> distinctValues;





    public static HashSet<String> getDistinctAttributes(String entity, String type){
        if(distinctValues==null){
            distinctValues= new HashMap<>();
            distinctValues.put("node", new HashMap<>());
            distinctValues.put("edge", new HashMap<>());
            distinctValues.get("node").put("drug",new HashSet<>(Arrays.asList("drugGroups","drugCategories","_cls")));
            distinctValues.get("edge").put("ProteinInteractsWithProtein",new HashSet<>(Arrays.asList("methods","evidenceTypes","databases")));
        }

        return distinctValues.get(entity).get(type);
    }

}
