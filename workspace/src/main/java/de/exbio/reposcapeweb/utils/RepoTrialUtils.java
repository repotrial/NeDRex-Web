package de.exbio.reposcapeweb.utils;

import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import de.exbio.reposcapeweb.db.updates.Collection;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
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

    public static File getCachedFile(Collection c, String ending, String cacheDir) {
        return new File(cacheDir, c.getName() + ending);
    }

    public static File getCachedFile(Collection c, String cacheDir) {
        return new File(cacheDir, c.getFile().getName());
    }

    public static boolean validateFormat(HashSet<String> urlAttributes, HashSet<String> classAttributes) {
        for (String a : classAttributes)
            if (!urlAttributes.remove(a))
                return false;
        return urlAttributes.isEmpty();
    }

}
