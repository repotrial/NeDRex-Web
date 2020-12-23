package de.exbio.reposcapeweb.configs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.configs.schema.Config;
import de.exbio.reposcapeweb.db.entities.RepoTrialEdge;
import de.exbio.reposcapeweb.db.entities.edges.*;
import de.exbio.reposcapeweb.db.entities.nodes.*;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.ReaderUtils;

import javax.persistence.Transient;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class DBConfig {

    private static File confFile;
    private static Config config;

    private static HashMap<String, HashMap<Integer, HashSet<String>>> distinctValues;


    public static HashSet<String> getDistinctAttributes(String entity, int type) {
        return distinctValues.get(entity).get(type);
    }

    public static Config getConfig() {
        return config;
    }

    public static void importConfig(File conf, Config object) {

        confFile = conf;
        config = object;

        HashSet<Integer> rem = new HashSet<>();
        for (int i = 0; i < config.nodes.size(); i++) {
            if (config.nodes.get(i).disabled)
                rem.add(i);
        }
        rem.forEach(i -> config.nodes.remove(i));
        rem.clear();
        for (int i = 0; i < config.edges.size(); i++)
            if (config.edges.get(i).disabled)
                rem.add(i);

        rem.forEach(i -> config.edges.remove(i.intValue()));

        setDistinctAttributes();

        Graphs.setUp();

        setUpAttributes();
    }

    private static void setUpAttributes() {

        config.nodes.forEach(node -> {

            HashSet<String> source = new HashSet<>();
            LinkedList<String> allAttributes = new LinkedList<>();
            LinkedList<String> allAttributeTypes = new LinkedList<>();
            LinkedList<Boolean> idAttributes = new LinkedList<>();
            LinkedList<String> listAttributes = new LinkedList<>();

            node.attributes.forEach((name, attr) -> {
                allAttributes.add(name);
                allAttributeTypes.add(attr.type);
                idAttributes.add(attr.id);
                if (attr.source)
                    source.add(name);
                if (attr.list)
                    listAttributes.add(name);
            });

            setAttributes("node", node.name, allAttributes.toArray(new String[]{}), allAttributeTypes.toArray(new String[]{}), idAttributes.toArray(new Boolean[0]), listAttributes.toArray(new String[]{}), source);
        });

        config.edges.forEach(edge -> {

            HashSet<String> source = new HashSet<>();
            LinkedList<String> allAttributes = new LinkedList<>();
            LinkedList<String> allAttributeTypes = new LinkedList<>();
            LinkedList<Boolean> idAttributes = new LinkedList<>();
            LinkedList<String> listAttributes = new LinkedList<>();

            edge.attributes.forEach((name, attr) -> {
                allAttributes.add(name);
                allAttributeTypes.add(attr.type);
                idAttributes.add(attr.id);
                if (attr.source)
                    source.add(name);
                if (attr.list)
                    listAttributes.add(name);
            });

            setAttributes("edge", edge.mapsTo, allAttributes.toArray(new String[]{}), allAttributeTypes.toArray(new String[]{}), idAttributes.toArray(new Boolean[0]), listAttributes.toArray(new String[]{}), source);
        });

    }

    private static void setAttributes(String entity, String mapsTo, String[] all, String[] allTypes, Boolean[] allIds, String[] list, HashSet<String> source) {
        if (entity.equals("edge"))
            switch (mapsTo) {
                case "DrugTargetProtein": {
                    DrugHasTargetProtein.allAttributes = all;
                    DrugHasTargetProtein.sourceAttributes = source;
                    DrugHasTargetProtein.allAttributeTypes = allTypes;
                    DrugHasTargetProtein.idAttributes = allIds;
                    DrugHasTargetProtein.listAttributes = list;
                    break;
                }
                case "DrugTargetGene": {
                    DrugHasTargetGene.allAttributes = all;
                    DrugHasTargetGene.allAttributeTypes = allTypes;
                    DrugHasTargetGene.idAttributes = allIds;
                    DrugHasTargetGene.listAttributes = list;
                    break;
                }

                case "ProteinProteinInteraction": {
                    ProteinInteractsWithProtein.allAttributes = all;
                    ProteinInteractsWithProtein.sourceAttributes = source;
                    ProteinInteractsWithProtein.allAttributeTypes = allTypes;
                    ProteinInteractsWithProtein.idAttributes = allIds;
                    ProteinInteractsWithProtein.listAttributes = list;
                    break;
                }

                case "ProteinPathway": {
                    ProteinInPathway.allAttributes = all;
                    ProteinInPathway.sourceAttributes = source;
                    ProteinInPathway.allAttributeTypes = allTypes;
                    ProteinInPathway.idAttributes = allIds;
                    ProteinInPathway.listAttributes = list;

                }

                case "ProteinEncodedBy": {
                    ProteinEncodedBy.allAttributes = all;
                    ProteinEncodedBy.sourceAttributes = source;
                    ProteinEncodedBy.allAttributeTypes = allTypes;
                    ProteinEncodedBy.idAttributes = allIds;
                    ProteinEncodedBy.listAttributes = list;
                }

                case "ProteinAssociatedWithDisorder": {
                    ProteinAssociatedWithDisorder.allAttributes = all;
                    ProteinAssociatedWithDisorder.allAttributeTypes = allTypes;
                    ProteinAssociatedWithDisorder.idAttributes = allIds;
                    ProteinAssociatedWithDisorder.listAttributes = list;
                    break;
                }

                case "GeneGeneInteraction": {
                    GeneInteractsWithGene.allAttributes = all;
                    GeneInteractsWithGene.allAttributeTypes = allTypes;
                    GeneInteractsWithGene.idAttributes = allIds;
                    GeneInteractsWithGene.listAttributes = list;
                    break;
                }

                case "GeneAssociatedWithDisorder": {
                    GeneAssociatedWithDisorder.allAttributes = all;
                    GeneAssociatedWithDisorder.sourceAttributes = source;
                    GeneAssociatedWithDisorder.allAttributeTypes = allTypes;
                    GeneAssociatedWithDisorder.idAttributes = allIds;
                    GeneAssociatedWithDisorder.listAttributes = list;
                    break;
                }

                case "DisorderComorbidity": {
                    DisorderComorbidWithDisorder.allAttributes = all;
                    DisorderComorbidWithDisorder.sourceAttributes = source;
                    DisorderComorbidWithDisorder.allAttributeTypes = allTypes;
                    DisorderComorbidWithDisorder.idAttributes = allIds;
                    DisorderComorbidWithDisorder.listAttributes = list;
                    break;
                }

                case "DisorderHirarchy": {
                    DisorderIsADisorder.allAttributes = all;
                    DisorderIsADisorder.sourceAttributes = source;
                    DisorderIsADisorder.allAttributeTypes = allTypes;
                    DisorderIsADisorder.idAttributes = allIds;
                    DisorderIsADisorder.listAttributes = list;
                    break;
                }


                case "DrugIndication": {
                    DrugHasIndication.allAttributes = all;
                    DrugHasIndication.sourceAttributes = source;
                    DrugHasIndication.allAttributeTypes = allTypes;
                    DrugHasIndication.idAttributes = allIds;
                    DrugHasIndication.listAttributes = list;
                    break;
                }

                case "DrugContraindication": {
                    DrugHasContraindication.allAttributes = all;
                    DrugHasContraindication.sourceAttributes = source;
                    DrugHasContraindication.allAttributeTypes = allTypes;
                    DrugHasContraindication.idAttributes = allIds;
                    DrugHasContraindication.listAttributes = list;
                    break;
                }
            }
        else
            switch (mapsTo) {

                case "gene": {
                    Gene.allAttributes = all;
                    Gene.sourceAttributes = source;
                    Gene.allAttributeTypes = allTypes;
                    Gene.idAttributes = allIds;
                    Gene.listAttributes = list;
                    break;
                }

                case "drug": {
                    Drug.allAttributes = all;
                    Drug.sourceAttributes = source;
                    Drug.allAttributeTypes = allTypes;
                    Drug.idAttributes = allIds;
                    Drug.listAttributes = list;
                    break;
                }

                case "disorder": {
                    Disorder.allAttributes = all;
                    Disorder.sourceAttributes = source;
                    Disorder.allAttributeTypes = allTypes;
                    Disorder.idAttributes = allIds;
                    Disorder.listAttributes = list;
                    break;
                }

                case "pathway": {
                    Pathway.allAttributes = all;
                    Pathway.sourceAttributes = source;
                    Pathway.allAttributeTypes = allTypes;
                    Pathway.idAttributes = allIds;
                    Pathway.listAttributes = list;

                    break;
                }

                case "protein": {
                    Protein.allAttributes = all;
                    Protein.sourceAttributes = source;
                    Protein.allAttributeTypes = allTypes;
                    Protein.idAttributes = allIds;
                    Protein.listAttributes = list;
                    break;
                }


            }
    }

    private static void setDistinctAttributes() {
        distinctValues = new HashMap<>();
        distinctValues.put("node", new HashMap<>());
        distinctValues.put("edge", new HashMap<>());
        config.nodes.forEach(node -> node.attributes.forEach((name, attr) -> {
            if (attr.distinct) {
                if (!distinctValues.get("node").containsKey(node.id))
                    distinctValues.get("node").put(node.id, new HashSet<>());
                distinctValues.get("node").get(node.id).add(name);
            }
        }));
        config.edges.forEach(edge -> edge.attributes.forEach((name, attr) -> {
            if (attr.distinct) {
                if (!distinctValues.get("edge").containsKey(edge.id))
                    distinctValues.get("edge").put(edge.id, new HashSet<>());
                distinctValues.get("edge").get(edge.id).add(name);
            }
        }));
    }

}
