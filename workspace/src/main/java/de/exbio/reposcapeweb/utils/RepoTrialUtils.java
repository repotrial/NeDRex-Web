package de.exbio.reposcapeweb.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RepoTrialUtils {

    public static HashMap<String, String> getAttributes(String content) {
        HashMap<String, String> map = new HashMap<>();
        System.out.println(content);
        StringUtils.split(content, ",\"").forEach(vals -> {
            System.out.println(vals);
            LinkedList<String> kv = StringUtils.split(vals, ":");
            System.out.println(kv);
            map.put(StringUtils.split(kv.get(0), "\"").get(1), kv.get(1).charAt(0) == '"' ? StringUtils.split(kv.get(1), "\"").get(1) : kv.get(1));
        });
        return map;
    }


}
