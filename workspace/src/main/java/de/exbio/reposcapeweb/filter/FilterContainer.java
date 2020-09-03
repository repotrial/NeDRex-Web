package de.exbio.reposcapeweb.filter;

import de.exbio.reposcapeweb.db.entities.RepoTrialNode;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.StringUtils;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class FilterContainer {


    //TODO change: make FilterGroup per node
    // FilterGroup contains EnumMap<TYPE,TreeMap<Key,List<Entry>>> + TreeMap<Key,Entry> für primaryDomainIds

    private TreeMap<FilterKey, FilterEntry> entries;

    public FilterContainer() {
        entries = new TreeMap<>();
    }

    public TreeMap<FilterKey, FilterEntry> getEntries() {
        return entries;
    }

    public void setEntries(TreeMap<FilterKey, FilterEntry> entries) {
        this.entries = entries;
    }

    public void addFilters(Map<FilterKey, FilterEntry> add) {
        entries.putAll(add);
    }

    public LinkedList<FilterEntry> filter(String key, int size) {
        LinkedList<FilterEntry> filtered = new LinkedList<>();

        AtomicBoolean done = new AtomicBoolean(false);

        entries.keySet().stream().filter(k -> k.contains(key)).forEach(k -> {
                    if (done.get())
                        return;
                    filtered.add(entries.get(k));
                    if (filtered.size() == size)
                        done.set(true);
                }
        );
        return filtered;
    }

    public void removeByNodeIds(Collection<Integer> ids) {
        LinkedList<FilterKey> del = new LinkedList<>();
        entries.forEach((k, v) -> {
            if (del.contains(v.getNodeId()))
                del.add(k);
        });
        del.forEach(entries::remove);
    }

    public void addNodes(Collection<RepoTrialNode> nodes) {
        nodes.forEach(n -> addFilters(n.toFilter()));
    }

    public void toFile(File target) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(target));
            entries.forEach((k, v) ->
            {
                try {
                    bw.write(k.toString() + "\t" + v.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void fromFile(File source) {
        entries.clear();
        BufferedReader br = ReaderUtils.getBasicReader(source);
        String line = "";
        while (true) {
            try {
                if ((line = br.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            LinkedList<String> values = StringUtils.split(line, '\t');
            entries.put(new FilterKey(values.get(0)), new FilterEntry(values.get(1), FilterEntry.FilterTypes.valueOf(values.get(2)), Integer.parseInt(values.get(3))));
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void builder(Collection<String> list, FilterEntry e, Map<FilterKey, FilterEntry> map) {
        list.forEach(i -> map.put(new FilterKey(i), e));
    }
}
