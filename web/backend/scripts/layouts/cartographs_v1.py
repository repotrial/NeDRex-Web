import sys
import json
import math
from cartoGRAPHs import *

graph = sys.argv[1]
nodes_in = sys.argv[2]
edges_in = sys.argv[3]
outfile = sys.argv[4]
layout = sys.argv[5]

layouts = ["portrait", "topographic_x", "topographic_y" , "geodesic", "geodesic_x", "geodesic_y"]

if layout not in layouts:
    print("Invalid layout. Please choose from: portrait, topographic, geodesic")
    sys.exit(1)

node_map = {}

with open(nodes_in, 'r') as fh:
    for v in fh:
        line = v.strip()
        spl = line.split("\t")
        node_map[spl[0]] = spl[1]

G = nx.read_edgelist(edges_in, delimiter="\t")

d_nodecolors = dict(zip(list(G.nodes()),['#0000FF']*len(list(G.nodes()))))
d_linkcolors = dict(zip(list(G.edges()),['#0000FF']*len(list(G.edges()))))

d_deg=dict(G.degree())
l_annotations_csv = ['Node: '+str(i)+'; Node: '+str(j) for i,j in zip(list(G.nodes()), d_deg.values())]
l_annotations_json = [list(("Node: "+str(i),"Node: "+str(j))) for i,j in zip(list(G.nodes()), d_deg.values())]
d_annotations_csv = dict(zip(list(G.nodes()),l_annotations_csv))
d_annotations_json = dict(zip(list(G.nodes()),l_annotations_json))

layouting = {}

if layout == "portrait":
    posG2D = generate_layout(G,
                            dim = 2,
                            layoutmethod = 'global',
                            dimred_method='umap',
                            )
    layouting = {k:(v[0],v[1]) for k,v in posG2D.items()}

elif "topographic" in layout:
    posG2D = generate_layout(G,
                            dim = 2,
                            layoutmethod = 'global',
                            dimred_method='umap',
                            )
    d_deg = dict(nx.degree_centrality(G))

    z_list = list(d_deg.values())
    d_z = dict(zip(list(G.nodes()),z_list))
    posG_topographic = layout_topographic(posG2D, d_z)
    if "x" in layout:
        layouting = {k:(v[0],v[2]) for k,v in posG_topographic.items()}
    elif "y" in layout:
        layouting = {k:(v[1],v[2]) for k,v in posG_topographic.items()}


elif "geodesic" in layout:
    rad_list = list([(1-i) for i in d_deg.values()])
    d_rad = dict(zip(list(G.nodes()), rad_list))
    posG_geodesic = layout_geodesic(G, d_rad)
    if "x" in layout:
        layouting = {k:(v[0],v[2]) for k,v in posG_geodesic.items()}
    elif "y" in layout:
        layouting = {k:(v[1],v[2]) for k,v in posG_geodesic.items()}
    else:
        layouting = {k:(v[0],v[1]) for k,v in posG_geodesic.items()}


final_layout = {k:((v[0]-0.5)*-100,(v[1]-0.5)*-100) for k,v in layouting.items()}

from graph_tool.all import *
from graph_tool.draw import *

g = load_graph(graph)
pos = sfdp_layout(g, C=0.15, p=1.5, r=2, K=6)
pin = g.new_vertex_property("boolean")
groups = g.new_vertex_property("int")
domainMap = {}
for v in g.vertices():
    domainMap[g.properties[('v', "primaryDomainId")][v]] = v


for k,v in final_layout.items():
    id = g.vertex(domainMap[k])
    pos[id] = (v[0],v[1])
    pin[id] = True

pos = sfdp_layout(g, C=0.15, p=1.5, r=2, K=6, pos=pos, pin=pin)

number_of_nodes = g.num_vertices()
factor = 10 ** int(math.log10(number_of_nodes)) *5

with open(outfile, 'w') as fh:
    for x in g.vertices():
        fh.write(
            g.properties[('v', "type")][x] + "\t" + str(g.properties[('v', "primaryDomainId")][x]) + "\t" + str(
                pos[x][0]*factor) + "\t" + str(pos[x][1]*factor) + "\n")
