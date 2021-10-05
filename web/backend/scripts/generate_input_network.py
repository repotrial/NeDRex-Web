import os
import sys
import graph_tool as gt
import networkx as nx  # read_graphml from repotrial networks works with networkx 2.2 and not 2.5 which is the lastest one!!!

# Autor: Sepideh Sadegh


apiNetwork_path = sys.argv[2]
inputFiles = sys.argv[1]
os.chdir(apiNetwork_path)

drug_groups_map = dict()
with open(inputFiles + '/drugs.tsv', 'r') as f:
    for i in f:
        line = i.strip().split('\t')
        drug_groups_map[line[0]] = line[1]

data = [{"name": "PPDr", "interactions": "proteinInteractsWithProtein.tsv", "targets": "drugHasTargetProtein.tsv"},
        {"name": "GGDr", "interactions": "geneInteractsWithGene.tsv", "targets": "drugHasTargetGene.tsv"}]

for d in data:

    drug_set = set()
    drug_gene_map = dict()
    with open(inputFiles + '/' + d["targets"], 'r') as f:
        for i in f:
            line = i.strip().split('\t')
            if line[0] not in drug_set:
                drug_set.add(line[0])
                drug_gene_map[line[0]] = set()
            drug_gene_map[line[0]].add(line[1])

    # evidence_map = dict()
    gene_set = set()
    gene_gene_map = dict()
    gene_set_exp = set()
    gene_gene_map_exp = dict()
    with open(inputFiles + '/' + d["interactions"], 'r') as f:
        for i in f:
            line = i.strip().split('\t')
            for pos in range(0, 2):
                if line[pos] not in gene_set:
                    gene_set.add(line[pos])
                    gene_gene_map[line[pos]] = set()
                    # evidence_map[line[pos]] = dict()
                gene_gene_map[line[pos]].add(line[(pos + 1) % 2])
                # evidence_map[line[pos]][line[(pos + 1) % 2]] = line[3]
                if line[2] == "true":
                    if line[pos] not in gene_set_exp:
                        gene_set_exp.add(line[pos])
                        gene_gene_map_exp[line[pos]] = set()
                    gene_gene_map_exp[line[pos]].add(line[(pos + 1) % 2])

    allNet = nx.Graph()

    # add gene nodes
    for g in gene_set:
        allNet.add_node(g, **{'primaryDomainId': g, 'type': 'Protein'})

    # add gene-gene edges
    for g1, gg in gene_gene_map.items():
        for g2 in gg:
            allNet.add_edge(g1, g2,
                            **{'type': 'ProteinInteractsWithProtein'})  # ,'evidenceTypes': evidence_map[g1][g2]})

    expNet = nx.Graph()

    # add gene nodes
    for g in gene_set_exp:
        expNet.add_node(g, **{'primaryDomainId': g, 'type': 'Protein'})

    # add gene-gene edges
    for g1, gg in gene_gene_map_exp.items():
        for g2 in gg:
            expNet.add_edge(g1, g2,
                            **{'type': 'ProteinInteractsWithProtein'})  # , 'evidenceTypes': evidence_map[g1][g2]})

    # add drug nodes and drug-gene edges
    for dr, gg in drug_gene_map.items():
        allNet.add_node(dr, **{'primaryDomainId': dr, 'type': 'Drug', 'drugGroups': drug_groups_map[dr]})
        expNet.add_node(dr, **{'primaryDomainId': dr, 'type': 'Drug', 'drugGroups': drug_groups_map[dr]})
        for g in gg:
            if allNet.has_node(g):
                allNet.add_edge(dr, g, **{'type': 'DrugHasTarget'})
            if expNet.has_node(g):
                expNet.add_edge(dr, g, **{'type': 'DrugHasTarget'})
            else:
                allNet.add_node(g, **{'primaryDomainId': g, 'type': 'Protein'})
                allNet.add_edge(dr, g, **{'type': 'DrugHasTarget'})
                expNet.add_node(g, **{'primaryDomainId': g, 'type': 'Protein'})
                expNet.add_edge(dr, g, **{'type': 'DrugHasTarget'})

    prefix = d["name"]

    nx.write_graphml(allNet, 'temp.graphml')
    gg = gt.load_graph('temp.graphml')
    gg.save(prefix + '_all.gt')
    os.remove('temp.graphml')
    print(allNet.number_of_nodes())  # 22619
    print(allNet.number_of_edges())  # 362801

    nx.write_graphml(expNet, 'exp-temp.graphml')
    gg_exp = gt.load_graph('exp-temp.graphml')
    gg_exp.save(prefix + '_exp.gt')
    os.remove('exp-temp.graphml')

    print(expNet.number_of_nodes())  # 22619
    print(expNet.number_of_edges())
