import os
import sys
import networkx as nx

wd = sys.argv[1]
target = sys.argv[2]

g = nx.Graph()

os.chdir(wd + "/nodes")

for file in os.listdir("./"):
    nodeType = file.split(".")[0]
    attrMap = {}
    idIndex = 0
    with open(file, "r") as fh:
        for line in fh.readlines():
            if line.startswith("#"):
                for attr in line.strip()[1:].split("\t"):
                    attrMap[attr] = len(attrMap)
                idIndex = attrMap["primaryDomainId"]
            else:
                data = {}
                line = line.strip().split("\t")
                for attr, idx in attrMap.items():
                    if attr != "id":
                        if attr == "type":
                            data[attr] = nodeType
                        else:
                            data[attr] = line[idx]
                g.add_node(line[idIndex], **data)

os.chdir(wd + "/edges")
for file in os.listdir("./"):
    edgeType = file.split(".")[0]
    attrMap = {}
    idIndex1 = 0
    idIndex2 = 0
    with open(file, "r") as fh:
        for line in fh.readlines():
            if line.startswith("#"):
                for attr in line.strip()[1:].split("\t"):
                    attrMap[attr] = len(attrMap)
                idIndex1 = attrMap["sourceDomainId"] if "sourceDomainId" in attrMap else attrMap["memberOne"]
                idIndex2 = attrMap["targetDomainId"] if "targetDomainId" in attrMap else attrMap["memberTwo"]
            else:
                data = {}
                line = line.strip().split("\t")
                for attr, idx in attrMap.items():
                    if attr != "id":
                        if attr == "type":
                            data[attr] = edgeType
                        else:
                            data[attr] = line[idx]
                g.add_edge(line[idIndex1], line[idIndex2], **data)

nx.write_graphml(g, target)
