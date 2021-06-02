package de.exbio.reposcapeweb.filter;

import de.exbio.reposcapeweb.communication.requests.Filter;
import de.exbio.reposcapeweb.utils.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class NodeFilter {

    private EnumMap<FilterType, HashMap<Integer, List<FilterEntry>>> distinctMap;
    private EnumMap<FilterType, HashMap<Integer, FilterKey>> distinctID2Keys;
    private EnumMap<FilterType, HashMap<FilterKey, Integer>> distinctKeys2ID;

    private EnumMap<FilterType, HashMap<Integer, FilterEntry>> uniqueMap;
    private EnumMap<FilterType, HashMap<Integer, FilterKey>> uniqueID2Keys;


    //TODO text filter (elastic search?)

    public NodeFilter() {
        distinctMap = new EnumMap<>(FilterType.class);
        distinctKeys2ID = new EnumMap<>(FilterType.class);
        distinctID2Keys = new EnumMap<>(FilterType.class);

        uniqueMap = new EnumMap<>(FilterType.class);
        uniqueID2Keys = new EnumMap<>(FilterType.class);
    }

    public NodeFilter(NodeFilter all, Collection<Integer> ids) {
        set(all.filteredByIds(ids));
    }

    private void set(NodeFilter filterByIds) {
        setUnique(filterByIds.getUniqueMap(),filterByIds.getUniqueID2Keys());
        setDistinct(filterByIds.getDistinctMap(), filterByIds.getDistinctID2Keys(), filterByIds.distinctKeys2ID);
    }

    private NodeFilter filteredByIds(Collection<Integer> ids) {
        NodeFilter filtered = new NodeFilter();

        distinctMap.forEach((type, keys) -> keys.forEach((key, entries) -> {
            List<FilterEntry> es = entries.stream().filter(e -> ids.contains(e.getNodeId())).collect(Collectors.toList());
            if (es.size() > 0) {
                filtered.addDistinct(type, distinctID2Keys.get(type).get(key), es);
            }
        }));
        uniqueMap.forEach((type, keys) -> keys.entrySet().stream().filter(e -> ids.contains(e.getValue().getNodeId())).forEach(e -> filtered.addUnique(type, uniqueID2Keys.get(type).get(e.getKey()), e.getValue())));
        return filtered;
    }


    private void addDistinct(FilterType type, FilterKey key, List<FilterEntry> entries) {
        HashMap<Integer, List<FilterEntry>> typeMap = distinctMap.get(type);
        try {
            Integer id = distinctKeys2ID.get(type).get(key);
            if (id == null) {
                id = distinctKeys2ID.get(type).size();
                distinctID2Keys.get(type).put(id, key);
                distinctKeys2ID.get(type).put(key, id);
                typeMap.put(id, new LinkedList<>());
            }
            typeMap.get(id).addAll(entries);

        } catch (NullPointerException e) {
            distinctMap.put(type, new HashMap<>());
            distinctID2Keys.put(type, new HashMap<>());
            distinctKeys2ID.put(type, new HashMap<>());
            addDistinct(type, key, entries);
        }
    }

    private void addDistinct(FilterType type, String name, List<FilterEntry> entries) {
        addDistinct(type, new FilterKey(StringUtils.normalize(name), name), entries);
    }

    private void addUnique(FilterType type, FilterKey key, FilterEntry entry) {
        try {
            Integer id = uniqueID2Keys.get(type).size();
            uniqueMap.get(type).put(id, entry);
            uniqueID2Keys.get(type).put(id, key);
        } catch (NullPointerException e) {
            uniqueMap.put(type, new HashMap<>());
            uniqueID2Keys.put(type, new HashMap<>());
            addUnique(type, key, entry);
        }
    }

    private void addUnique(FilterType type, String key, FilterEntry entry) {
        addUnique(type, new FilterKey(StringUtils.normalize(key), key), entry);
    }


    public LinkedList<FilterEntry> filterContains(String key, int size) {
        LinkedList<FilterEntry> filtered = new LinkedList<>();

        TreeSet<FilterEntry> entries = new TreeSet<>();
        HashSet<Integer> ids = new HashSet<>();

        uniqueID2Keys.forEach((type, vals) -> vals.entrySet().stream().filter(e -> e.getValue().contains(key)).forEach(e -> entries.add(uniqueMap.get(type).get(e.getKey()))));
        uniqueID2Keys.forEach((type, vals) -> vals.entrySet().stream().filter(e -> e.getValue().contains(key)).forEach(e -> entries.addAll(distinctMap.get(type).get(e.getKey()))));


        AtomicBoolean done = new AtomicBoolean(false);
        entries.forEach(e -> {
                    if (done.get())
                        return;
                    if (ids.add(e.getNodeId()))
                        filtered.add(e);
                    if (filtered.size() == size)
                        done.set(true);
                }
        );
        return filtered;
    }

    public LinkedList<FilterEntry> filterStartsWith(String key, int size) {
        LinkedList<FilterEntry> filtered = new LinkedList<>();

        TreeSet<FilterEntry> entries = new TreeSet<>();
        HashSet<Integer> ids = new HashSet<>();

        uniqueID2Keys.forEach((type, vals) -> vals.entrySet().stream().filter(e -> e.getValue().startsWith(key)).forEach(e -> entries.add(uniqueMap.get(type).get(e.getKey()))));
        uniqueID2Keys.forEach((type, vals) -> vals.entrySet().stream().filter(e -> e.getValue().startsWith(key)).forEach(e -> entries.addAll(distinctMap.get(type).get(e.getKey()))));

        AtomicBoolean done = new AtomicBoolean(false);
        entries.forEach(e -> {
                    if (done.get())
                        return;
                    if (ids.add(e.getNodeId()))
                        filtered.add(e);
                    if (filtered.size() == size)
                        done.set(true);
                }
        );
        return filtered;
    }


    public LinkedList<FilterEntry> filterMatches(String key, int size) {
        LinkedList<FilterEntry> filtered = new LinkedList<>();


        TreeSet<FilterEntry> entries = new TreeSet<>();
        HashSet<Integer> ids = new HashSet<>();

        uniqueID2Keys.forEach((type, vals) -> vals.entrySet().stream().filter(e -> e.getValue().matches(key)).forEach(e -> entries.add(uniqueMap.get(type).get(e.getKey()))));
        uniqueID2Keys.forEach((type, vals) -> vals.entrySet().stream().filter(e -> e.getValue().matches(key)).forEach(e -> entries.addAll(distinctMap.get(type).get(e.getKey()))));

        AtomicBoolean done = new AtomicBoolean(false);
        entries.forEach(e -> {
                    if (done.get())
                        return;
                    if (ids.add(e.getNodeId()))
                        filtered.add(e);
                    if (filtered.size() == size)
                        done.set(true);
                }
        );
        return filtered;
    }

    public NodeFilter startsWith(String key) {
        NodeFilter filtered = new NodeFilter();
        uniqueMap.keySet().forEach(type -> filtered.setUniqueEntry(type, uniqueStartsWith(type, key), this.uniqueID2Keys.get(type)));
        distinctMap.keySet().forEach(type -> filtered.setDistinctEntry(type, distinctStartsWith(type, key), this.distinctID2Keys.get(type)));
        return filtered;
    }

    public NodeFilter matches(String key) {
        NodeFilter filtered = new NodeFilter();
        uniqueMap.keySet().forEach(type -> filtered.setUniqueEntry(type, uniqueMatches(type, key), this.uniqueID2Keys.get(type)));
        distinctMap.keySet().forEach(type -> filtered.setDistinctEntry(type, distinctMatches(type, key), this.distinctID2Keys.get(type)));
        return filtered;
    }

    public NodeFilter contains(String key) {
        NodeFilter filtered = new NodeFilter();
        uniqueMap.keySet().forEach(type -> filtered.setUniqueEntry(type, uniqueContains(type, key), this.uniqueID2Keys.get(type)));
        distinctMap.keySet().forEach(type -> filtered.setDistinctEntry(type, distinctContains(type, key), this.distinctID2Keys.get(type)));
        return filtered;
    }


    public void setUniqueEntry(FilterType type, HashMap<Integer, FilterEntry> entries, HashMap<Integer, FilterKey> keyMap) {
        entries.forEach((k, v) -> addUnique(type, keyMap.get(k), v));
    }

    public void setDistinctEntry(FilterType type, HashMap<Integer, List<FilterEntry>> entries, HashMap<Integer, FilterKey> keyMap) {
        entries.forEach((k, v) -> addDistinct(type, keyMap.get(k), v));
    }


    public LinkedList<FilterEntry> toList(int size) {
        LinkedList<FilterEntry> filtered = new LinkedList<>();


        HashSet<Integer> ids = new HashSet<>();

        TreeSet<FilterEntry> entries = uniqueMap.values().stream().map(HashMap::values).flatMap(Collection::stream).collect(Collectors.toCollection(TreeSet::new));
        entries.addAll(distinctMap.values().stream().map(HashMap::values).flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toSet()));

        AtomicBoolean done = new AtomicBoolean(false);
        entries.forEach(e -> {
                    if (done.get())
                        return;
                    if (ids.add(e.getNodeId()))
                        filtered.add(e);
                    if (filtered.size() == size)
                        done.set(true);
                }
        );
        return filtered;
    }

    private HashSet<FilterEntry> filterUniqueContains(FilterType type, String key) {
        try {
            return uniqueID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().contains(key)).map(e -> uniqueMap.get(type).get(e.getKey())).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    private HashSet<FilterEntry> filterUniqueMatches(FilterType type, String key) {
        try {
            return uniqueID2Keys.get(type).entrySet().stream().filter(k -> k.getValue().matches(key)).map(e -> uniqueMap.get(type).get(e.getKey())).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    private HashSet<FilterEntry> filterUniqueStartsWith(FilterType type, String key) {
        try {
            return uniqueID2Keys.get(type).entrySet().stream().filter(k -> k.getValue().startsWith(key)).map(e -> uniqueMap.get(type).get(e.getKey())).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    public HashMap<Integer, FilterEntry> uniqueStartsWith(FilterType type, String key) {
        HashMap<Integer, FilterEntry> filtered = new HashMap<>();
        try {
            uniqueID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().startsWith(key)).forEach(e -> filtered.put(e.getKey(), uniqueMap.get(type).get(e.getKey())));
        } catch (NullPointerException ignore) {
        }
        return filtered;
    }

    public HashMap<Integer, FilterEntry> uniqueContains(FilterType type, String key) {
        HashMap<Integer, FilterEntry> filtered = new HashMap<>();
        try {
            uniqueID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().contains(key)).forEach(e -> filtered.put(e.getKey(), uniqueMap.get(type).get(e.getKey())));
        } catch (NullPointerException ignore) {
            ignore.printStackTrace();
        }
        return filtered;
    }

    public EnumMap<FilterType, HashMap<Integer, FilterEntry>> uniqueContains(String key) {
        EnumMap<FilterType, HashMap<Integer, FilterEntry>> out = new EnumMap<>(FilterType.class);
        try {
            uniqueID2Keys.keySet().forEach(t -> {
                out.put(t, uniqueContains(t, key));
            });
        } catch (NullPointerException ignore) {
        }
        return out;
    }

    public HashMap<Integer, FilterEntry> uniqueMatches(FilterType type, String key) {
        HashMap<Integer, FilterEntry> filtered = new HashMap<>();
        try {
            uniqueID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().matches(key)).forEach(e -> filtered.put(e.getKey(), uniqueMap.get(type).get(e.getKey())));
        } catch (NullPointerException ignore) {
        }
        return filtered;
    }


    private HashSet<FilterEntry> filterDistinctContains(FilterType type, String key) {
        try {
            return distinctID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().contains(key)).map(e -> distinctMap.get(type).get(e.getKey())).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    private HashSet<FilterEntry> filterDistinctMatches(FilterType type, String key) {
        try {
            return distinctID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().matches(key)).map(e -> distinctMap.get(type).get(e.getKey())).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    private HashSet<FilterEntry> filterDistinctStartsWith(FilterType type, String key) {
        try {
            return distinctID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().startsWith(key)).map(e -> distinctMap.get(type).get(e.getKey())).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    public HashMap<Integer, List<FilterEntry>> distinctContains(FilterType type, String key) {
        HashMap<Integer, List<FilterEntry>> out = new HashMap<>();
        try {
            distinctID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().contains(key)).forEach(e -> out.put(e.getKey(), distinctMap.get(type).get(e.getKey())));
        } catch (NullPointerException ignore) {
        }
        return out;
    }

    public EnumMap<FilterType, HashMap<Integer, List<FilterEntry>>> distinctContains(String key) {
        EnumMap<FilterType, HashMap<Integer, List<FilterEntry>>> out = new EnumMap<>(FilterType.class);
        try {
            distinctID2Keys.keySet().forEach(t -> out.put(t, distinctContains(t, key)));
        } catch (NullPointerException ignore) {
        }
        return out;
    }


    public HashMap<Integer, List<FilterEntry>> distinctStartsWith(FilterType type, String key) {
        HashMap<Integer, List<FilterEntry>> out = new HashMap<>();
        try {
            distinctID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().startsWith(key)).forEach(e -> out.put(e.getKey(), distinctMap.get(type).get(e.getKey())));
        } catch (NullPointerException ignore) {
        }
        return out;
    }


    public HashMap<Integer, List<FilterEntry>> distinctMatches(FilterType type, String key) {
        HashMap<Integer, List<FilterEntry>> out = new HashMap<>();
        try {
            distinctID2Keys.get(type).entrySet().stream().filter(e -> e.getValue().matches(key)).forEach(e -> out.put(e.getKey(), distinctMap.get(type).get(e.getKey())));
        } catch (NullPointerException ignore) {
        }
        return out;
    }


    public EnumMap<FilterType, HashMap<Integer, List<FilterEntry>>> getDistinctMap() {
        return distinctMap;
    }

    public void setDistinct(EnumMap<FilterType, HashMap<Integer, List<FilterEntry>>> distinctMap, EnumMap<FilterType, HashMap<Integer, FilterKey>> distinctID2Keys, EnumMap<FilterType, HashMap<FilterKey, Integer>> distinctKeys2ID) {
        //TODO clone entries
        this.distinctMap = distinctMap;
        this.distinctKeys2ID=distinctKeys2ID;
        this.distinctID2Keys=distinctID2Keys;
    }

    public EnumMap<FilterType, HashMap<Integer, FilterEntry>> getUniqueMap() {
        return uniqueMap;
    }

    public void setUnique(EnumMap<FilterType, HashMap<Integer, FilterEntry>> uniqueMap, EnumMap<FilterType, HashMap<Integer, FilterKey>> uniqueID2Keys) {
        //TODO clone entries
        this.uniqueMap = uniqueMap;
        this.uniqueID2Keys=uniqueID2Keys;
    }


    public void removeByNodeIds(Collection<Integer> ids) {
        removeByNodeIdsDistinct(ids);
        removeByNodeIdsUnique(ids);
    }

    private void removeByNodeIdsUnique(Collection<Integer> ids) {
        EnumMap<FilterType, Set<Integer>> del = new EnumMap<>(FilterType.class);
        uniqueMap.forEach((type, keys) -> {
            Set<Integer> left = keys.entrySet().stream().filter(e -> ids.contains(e.getValue().getNodeId())).map(Map.Entry::getKey).collect(Collectors.toSet());
            del.put(type, left);
        });

        del.forEach((type, keys) -> keys.forEach(id -> {
            uniqueMap.get(type).remove(id);
            FilterKey key = uniqueID2Keys.get(type).remove(id);
        }));
    }


    private void removeByNodeIdsDistinct(Collection<Integer> ids) {
        EnumMap<FilterType, TreeMap<Integer, List<FilterEntry>>> del = new EnumMap<>(FilterType.class);
        distinctMap.forEach((type, keys) -> keys.forEach((k, v) -> {
            List<FilterEntry> left = v.stream().filter(e -> ids.contains(e.getNodeId())).collect(Collectors.toList());
            if (left.size() > 0)
                try {
                    del.get(type).put(k, left);
                } catch (NullPointerException e) {
                    del.put(type, new TreeMap<>());
                    del.get(type).put(k, left);
                }
        }));
        del.forEach((type, keys) -> keys.forEach((k, v) -> {
            distinctMap.get(type).get(k).removeAll(v);
            if (distinctMap.get(type).get(k).size() == 0) {
                distinctMap.get(type).remove(k);
                FilterKey key = distinctID2Keys.get(type).remove(k);
                distinctKeys2ID.get(type).remove(key);
            }
        }));
    }

    public void addUnique(FilterType type, Map<String, FilterEntry> entries) {
        entries.forEach((k, v) -> addUnique(type, k, v));
    }
    public void addUnique(FilterType type, String key, String keyName, String name, int nodeId) {
        addUnique(type, new FilterKey(key, keyName), new FilterEntry(name, type, nodeId));
    }

    public void addDistinct(FilterType type, Map<String, FilterEntry> entries) {
        entries.forEach((key, values) -> addDistinct(type, key, values));
    }

    public void addDistinct(FilterType type, String key, FilterEntry entry) {
        addDistinct(type, key, Collections.singletonList(entry));
    }

    public void addDistinct(FilterType type, FilterKey key, FilterEntry entry) {
        addDistinct(type, key, Collections.singletonList(entry));
    }

    public void addDistinct(FilterType type, String key, String keyName, String name, int nodeId) {
        addDistinct(type, new FilterKey(key, keyName), new FilterEntry(name, type, nodeId));
    }


    public void add(EnumMap<FilterType, Map<String, FilterEntry>> toDistinctFilter, EnumMap<FilterType, Map<String, FilterEntry>> toUniqueFilter) {
        toUniqueFilter.forEach(this::addUnique);
        toDistinctFilter.forEach(this::addDistinct);
    }

    public int size() {
        return distinctMap.size() + uniqueMap.size();
    }


    public NodeFilter apply(String type, String value) {
        switch (type) {
            case "startsWith":
                return startsWith(StringUtils.normalize(value));
            case "contain":
                return contains(StringUtils.normalize(value));
            case "match":
                return matches(value);
        }
        return null;
    }


    public NodeFilter apply(Filter f) {
        return apply(f.type, f.expression);
    }

    public EnumMap<FilterType, HashMap<Integer, FilterKey>> getDistinctID2Keys() {
        return distinctID2Keys;
    }

    public EnumMap<FilterType, HashMap<Integer, FilterKey>> getUniqueID2Keys() {
        return uniqueID2Keys;
    }

    public List<FilterEntry> getEntry(String sid) {
        LinkedList<String> id = StringUtils.split(sid, "_");
        return distinctMap.get(FilterType.values()[Integer.parseInt(id.get(0))]).get(Integer.parseInt(id.get(1)));
    }
}
