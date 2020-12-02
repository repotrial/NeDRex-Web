import time
from urllib.request import urlretrieve
import requests
import os
import sys
import graph_tool as gt
import networkx as nx   # read_graphml from repotrial networks works with networkx 2.2 and not 2.5 which is the lastest one!!!
#Autor: Sepideh Sadegh

apiNetwork_path = sys.argv[1]
os.chdir(apiNetwork_path)
# get the network containing protein-protein and protein-drug interactions with proper parameters via API
base_url = "https://api.repotrial.net"
submit_url = f"{base_url}/graph_builder"

basedata = [
    {"data":{
    "nodes" : [],
    "edges" : ["protein_interacts_with_protein", "drug_has_target"],
    "drug_groups" : ['approved', 'experimental', 'investigational', 'nutraceutical', 'vet_approved', 'withdrawn', 'illicit'],
    "concise" : True
}, "name":"PPdr",
    "dropNodeAttributes":{'geneName', 'taxid', 'domainIds', 'synonyms', 'indication', 'displayName'},
    "dropEdgeAttributes":{'memberOne', 'memberTwo', 'reversible', 'sourceDomainId', 'targetDomainId'}
}
]


for data in basedata:
    print("Submitting request")
    gbuild = requests.post(submit_url, json=data["data"])
    print(f"UID for job: {gbuild.json()}")
    uid = gbuild.json()

    while True:
        progress = requests.get(f"{base_url}/graph_details/{uid}")
        built = (progress.json()["status"] == "completed")
        if built:
            break
        print("Waiting for build to complete, sleeping for 10 seconds")
        time.sleep(10)

    fname = "temp-"+data["name"]
    urlretrieve(f"{base_url}/graph_download_v2/{uid}/{fname}.graphml", f"{fname}.graphml")


    G = nx.read_graphml(f"{fname}.graphml")
    G_und = G.to_undirected()

    node_list = set(G_und.nodes)
    nodeAttr_list = data["dropNodeAttributes"]
    for n in node_list:
        for attr in nodeAttr_list:
            if attr in G_und.nodes[n].keys():
                del G_und.nodes[n][attr]

    edge_list = set(G_und.edges)
    edgeAttr_list = data["dropEdgeAttributes"]
    for e in edge_list:
        for attr in edgeAttr_list:
            if attr in G_und.edges[e].keys():
                del G_und.edges[e][attr]

    network_name = data["name"]+"-temp-graph.graphml"
    nx.write_graphml(G_und, f"{network_name}")

    gg = gt.load_graph(data["name"]+"-temp-graph.graphml")
    gg.save(data["name"]+"-for-ranking.gt")