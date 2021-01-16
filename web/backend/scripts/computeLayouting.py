from graph_tool.all import *
from graph_tool.draw import *
import sys
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
    makeImage = True
if mode == "2":
    out = sys.argv[3]
    thumb = sys.argv[4]
    makeLayout = True
    makeImage = True

g = load_graph(file)
pos = sfdp_layout(g)

colorMap = {
    'disorder': Colors.to_rgba("#EF553B"),
    'drug': Colors.to_rgba("#00CC96"),
    'gene': Colors.to_rgba("#636EFA"),
    'pathway': Colors.to_rgba("#fecb52"),
    'protein': Colors.to_rgba('#19d3f3'),
    'default': Colors.to_rgba('#ffffff')}

if makeImage:
    plot_color = g.new_vertex_property('vector<double>')
    g.vertex_properties['plot_color'] = plot_color
    nodeCount = 0

nodeCount = 0

for x in g.vertices():
    nodeCount += 1
    if makeImage:
        print(x)
        print(g.properties[('v', "type")][x])
        try:
            plot_color[x] = colorMap[g.properties[('v', "type")][x]]
        except:
            plot_color[x] = colorMap["default"]

scale = abs(math.log10(nodeCount / 100))
imageHeight = int(math.sqrt(nodeCount) * 10 + min(500, int(500 / scale)))

if makeLayout:
    with open(out, 'w') as fh:
        for x in g.vertices():
            fh.write(
                g.properties[('v', "type")][x] + "\t" + str(g.properties[('v', "primaryDomainId")][x]) + "\t" + str(
                    pos[x][0] * 200 * scale) + "\t" + str(pos[x][1] * 200 * scale) + "\n")

if makeImage:
    graph_draw(g, pos=pos, output=thumb, vertex_fill_color=g.vertex_properties['plot_color'],
               output_size=(imageHeight, imageHeight), vertex_size=int(10 - scale), fit_view=scale, fit_view_ink=True)
