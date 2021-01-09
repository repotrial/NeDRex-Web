package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.FilterKey;
import de.exbio.reposcapeweb.filter.FilterType;

import java.util.*;
import java.util.stream.Collectors;

public class Suggestions {
    private String gid;
    private String query;
    private LinkedList<Suggestion> suggestions;


    public Suggestions(String gid, String query) {
        this.gid = gid;
        this.query = query;
        suggestions = new LinkedList<>();
    }


    public void setDistinct(EnumMap<FilterType, TreeMap<FilterKey, List<FilterEntry>>> distinctMap, HashSet<Integer> idsList) {
        distinctMap.forEach((type, filters) ->
                filters.forEach((key, entries) ->
                        suggestions.add(new Suggestion(type.name(), key.toString(), entries.stream().map(FilterEntry::getNodeId).filter(idsList::contains).collect(Collectors.toSet())))
                )
        );
    }

    public void setDistinct(EnumMap<FilterType, TreeMap<FilterKey, List<FilterEntry>>> distinctMap) {
        distinctMap.forEach((type, filters) ->
                filters.forEach((key, entries) ->
                        suggestions.add(new Suggestion(type.name(), key.toString(), entries.stream().map(FilterEntry::getNodeId).collect(Collectors.toSet())))
                )
        );
    }

    public void setUnique(EnumMap<FilterType, TreeMap<FilterKey, FilterEntry>> uniqueMap, HashSet<Integer> idsList) {
        uniqueMap.forEach((type, filters) ->
                filters.forEach((key, entry) -> {
                    if (idsList.contains(entry.getNodeId()))
                        suggestions.add(new Suggestion(type.name(), key.toString(), entry.getNodeId()));
                })
        );
    }

    public void setUnique(EnumMap<FilterType, TreeMap<FilterKey, FilterEntry>> uniqueMap) {
        uniqueMap.forEach((type, filters) ->
                filters.forEach((key, entry) -> suggestions.add(new Suggestion(type.name(), key.toString(), entry.getNodeId())))
        );
    }

    public String getGid() {
        return gid;
    }

    public String getQuery() {
        return query;
    }

    public LinkedList<Suggestion> getSuggestions() {
        return suggestions;
    }

    private class Suggestion {
        private String text;
        private String value;
        private Integer[] ids;
        private String type;

        public Suggestion(String type, String name) {
            this.type = type;
            this.text = name;
            this.value = name + " [" + type + "]";
        }

        public Suggestion(String type, String name, Collection<Integer> ids) {
            this(type, name);
            this.ids = ids.toArray(Integer[]::new);
        }

        public Suggestion(String type, String name, Integer id) {
            this(type, name);
            this.ids = new Integer[]{id};
        }

        public String getText() {
            return text;
        }

        public String getValue() {
            return value;
        }

        public Integer[] getIds() {
            return ids;
        }

        public String getType() {
            return type;
        }
    }

}
