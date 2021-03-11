package de.exbio.reposcapeweb.configs;

import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.configs.schema.ColorConfig;

import java.io.File;
import java.util.HashMap;

public class VisConfig {

    private static File confFile;
    private static Object config;


    public static void importConfig(File conf, Object object) {
        confFile = conf;
        config = object;

        setColors();
    }

    private static void setColors() {
        Object options = ((HashMap<String, Object>) config).get("options");
        HashMap<String,Object> groups = (HashMap<String,Object>) ((HashMap<String, Object>) options).get("groups");
        groups.forEach((name,vals)->{
            if(name.equals("other"))
                return;
            String moduleName = "";
            boolean module = name.endsWith("Module");
            if(module){
                moduleName = name;
                name = name.substring(0,name.length()-6);
            }
            ColorConfig colors = DBConfig.getConfig().nodes.get(Graphs.getNode(name)).colors;
            HashMap<String,Object> colorMap = new HashMap<>();
            colorMap.put("border", module? colors.light : colors.main);
            colorMap.put("background",colors.light);
            HashMap<String,Object> highlight = new HashMap<>();
            highlight.put("border", module? colors.light : colors.main);
            highlight.put("background",colors.light);
            colorMap.put("highlight",highlight);
            ((HashMap<String,Object>) groups.get(module ? moduleName: name)).put("color",colorMap);
        });
    }


    public static Object getConfig() {
        return config;
    }

}
