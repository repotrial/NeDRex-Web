package de.exbio.reposcapeweb.configs;

import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.configs.schema.ColorConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

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
        HashMap<String, Object> groups = (HashMap<String, Object>) ((HashMap<String, Object>) options).get("groups");
        groups.forEach((name, vals) -> {
            if (name.equals("other"))
                return;
            String seedName = "";
            boolean seed = name.startsWith("seed");
            if (seed) {
                seedName = name;
                name = name.substring(4).toLowerCase();
            }
            ColorConfig colors = DBConfig.getConfig().nodes.get(Graphs.getNode(name)).colors;
            HashMap<String, Object> colorMap = new HashMap<>();
            colorMap.put("border", seed ? "#e4ca02" : colors.main);
            colorMap.put("background", seed ? "#fbe223" : colors.light);
            HashMap<String, Object> highlight = new HashMap<>();
            highlight.put("border", seed ? colors.main : "#fbe223");
            highlight.put("background", seed ? "#fbe223" : colors.light);
            colorMap.put("highlight", highlight);
            ((HashMap<String, Object>) groups.get(seed ? seedName : name)).put("color", colorMap);
        });
    }


    public static Object getConfig() {
        return config;
    }

}
