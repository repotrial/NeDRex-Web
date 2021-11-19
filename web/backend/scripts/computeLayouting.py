from graph_tool.all import *
from graph_tool.draw import *
import sys
import json
import math
from matplotlib.colors import colorConverter as Colors

file = sys.argv[1]

mode = sys.argv[2]

makeImage = False
makeLayout = False
if mode == "0":
    out = sys.argv[3]
    makeLayout = True
if mode == "1":
    thumb = sys.argv[3]
    config = sys.argv[4]
    makeImage = True
if mode == "2":
    out = sys.argv[3]
    thumb = sys.argv[4]
    config = sys.argv[5]
    makeLayout = True
    makeImage = True

g = load_graph(file)
pos = sfdp_layout(g, C=0.15, p=1.5, r=2, K=6)

colorMap = {'default': Colors.to_rgba('#ffffff')}


if makeImage:
    with open(config) as fh:
        conf=json.load(fh)
        for vals in conf["nodes"]:
            colorMap[vals["name"]]=Colors.to_rgba(vals["colors"]["light"])
    plot_color = g.new_vertex_property('vector<double>')
    g.vertex_properties['plot_color'] = plot_color
    nodeCount = 0

nodeCount = 0

for x in g.vertices():
    nodeCount += 1
    if makeImage:
        try:
            plot_color[x] = colorMap[g.properties[('v', "type")][x]]
        except:
            plot_color[x] = colorMap["default"]

edgeCount = 0
for e in g.edges():
    edgeCount+=1

scale = edgeCount/nodeCount
imageHeight = int(math.sqrt(nodeCount) * 10 + min(500, int(500 / scale)))

if makeLayout:
    layoutScale = 50
    with open(out, 'w') as fh:
        for x in g.vertices():
            fh.write(
                g.properties[('v', "type")][x] + "\t" + str(g.properties[('v', "primaryDomainId")][x]) + "\t" + str(
                    pos[x][0] * layoutScale) + "\t" + str(pos[x][1] * layoutScale) + "\n")

if makeImage:
    graph_draw(g, pos=pos, output=thumb, vertex_fill_color=g.vertex_properties['plot_color'],
               output_size=(imageHeight, imageHeight), vertex_size=int(10 / math.log10(nodeCount)),  fit_view_ink=True)
