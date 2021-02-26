package de.exbio.reposcapeweb.utils;

import java.util.Collection;
import java.util.HashSet;

public class StatUtils {

    public static double calculateJaccardIndex(Collection set1, Collection set2){
        if(set1.size()==0 && set2.size()==0)
            return 1;
        HashSet<Object> all = new HashSet<>(set1);
        all.addAll(set2);
        double intersecting = (double) set1.stream().filter(set2::contains).count();
        return intersecting/all.size();
    }
}
