package de.exbio.reposcapeweb.utils;

import de.exbio.reposcapeweb.db.io.Collection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RepoTrialUtils {

    public static HashMap<String, String> getAttributes(String content) {
        HashMap<String, String> map = new HashMap<>();
        StringUtils.split(content, ",\"").forEach(vals -> {
            LinkedList<String> kv = StringUtils.split(vals, ":");
            map.put(StringUtils.split(kv.get(0), "\"").get(1), kv.get(1).charAt(0) == '"' ? StringUtils.split(kv.get(1), "\"").get(1) : kv.get(1));
        });
        return map;
    }

    public static File getCachedFile(Collection c, String ending, String cacheDir) {
        return new File(cacheDir, c.getName() + ending);
    }

    public static File getCachedFile(File c, String cacheDir) {
        return new File(cacheDir, c.getName());
    }

    public static boolean validateFormat(HashSet<String> urlAttributes, HashSet<String> classAttributes) {
        for (String a : classAttributes)
            if (!urlAttributes.remove(a))
                return false;
        return urlAttributes.isEmpty();
    }

    public static void writeNodeMap(File f, HashMap<Integer, Pair<String,String>> idToDomainMap) {
        f.getParentFile().mkdirs();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("#id\tprimaryDomainId\n");
            idToDomainMap.forEach((k, v) -> {
                try {
                    bw.write(k + "\t" + v.first +"\t"+v.second+ "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
