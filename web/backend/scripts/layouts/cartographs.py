import sys
import json
import math
from cartoGRAPHs import *

nodes_in = sys.argv[1]
edges_in = sys.argv[2]
layout = sys.argv[3]
outfile = sys.argv[4]

layouts = ["portrait", "topographic", "geodesic"]

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

elif layout == "topographic":
    posG2D = generate_layout(G,
                            dim = 2,
                            layoutmethod = 'global',
                            dimred_method='umap',
                            )
    d_deg = dict(nx.degree_centrality(G))

    z_list = list(d_deg.values())
    d_z = dict(zip(list(G.nodes()),z_list))
    posG_topographic = layout_topographic(posG2D, d_z)

    layouting = {k:(v[0],v[1]) for k,v in posG_topographic.items()}

elif layout == "geodesic":
    rad_list = list([(1-i) for i in d_deg.values()])
    d_rad = dict(zip(list(G.nodes()), rad_list))
    posG_geodesic = layout_geodesic(G, d_rad)
    layouting = {k:(v[0],v[1]) for k,v in posG_geodesic.items()}




with open(outfile, 'w') as fh:
    for k,v in layouting.items():
        fh.write(node_map[k]+"\t"+ k + "\t" + str((v[0] -0.5) * 200) + "\t" + str((v[1] -0.5) * 200) + "\n")