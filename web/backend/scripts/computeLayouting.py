from graph_tool.all import *
from graph_tool.draw import *
import sys
import math
from matplotlib.colors import colorConverter as Colors

file = sys.argv[1]
out = sys.argv[2]
thumb = sys.argv[3]

g = load_graph(file)
pos = sfdp_layout(g)
# for x in pos:
#     print(x)

colorMap = {
    'disorder': Colors.to_rgba("#EF553B"),
    'drug': Colors.to_rgba("#00CC96"),
    'gene': Colors.to_rgba("#636EFA"),
    'pathway': Colors.to_rgba("#fecb52"),
    'protein': Colors.to_rgba('#19d3f3')}

plot_color = g.new_vertex_property('vector<double>')
g.vertex_properties['plot_color'] = plot_color

nodeCount=0
with open(out, 'w') as fh:
    for x in g.vertices():
        fh.write(str(g.properties[('v', "primaryDomainId")][x]) + "\t" + str(pos[x][0]) + "\t" + str(pos[x][1]) + "\n")
        plot_color[x]=colorMap[g.properties[('v',"type")][x]]
        nodeCount+=1


imageHeight=int(math.sqrt(nodeCount)*10)
imageSize=nodeCount*100

scale = math.log10(nodeCount/100)

graph_draw(g, pos=pos, output=thumb, vertex_fill_color=g.vertex_properties['plot_color'], output_size=(imageHeight,imageHeight), vertex_size=4,fit_view=scale, fit_view_ink=True)
