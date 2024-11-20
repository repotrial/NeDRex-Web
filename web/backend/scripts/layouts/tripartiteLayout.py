from graph_tool.all import *
from graph_tool.draw import *
import sys
import json
import math
from matplotlib.colors import colorConverter as Colors

file = sys.argv[1]
layout = sys.argv[2]
set1 = sys.argv[3]
set2 = sys.argv[4]
#config = sys.argv[5]

g = load_graph(file)
pos = sfdp_layout(g, C=0.15, p=1.5, r=2, K=6)
pin = g.new_vertex_property("boolean")
groups = g.new_vertex_property("int")
domainMap = {}
for v in g.vertices():
    domainMap[g.properties[('v', "primaryDomainId")][v]] = v

i = 0

x_max = 0
x_min = 1000000
y_max = 0
y_min = 1000000

sources = []
with open(set1, 'r') as fh:
    for v in fh:
        line = v.strip()
        spl = line.split("\t")
        id=g.vertex(domainMap[spl[0]])
        groups[id]=1
        if int(spl[1]) > 0:
            x=pos[id][0]
            y= pos[id][1]
            x_max = max(x_max, x)
            x_min = min(x_min, x)
            y_max = max(y_max, y)
            y_min = min(y_min, y)
            sources.append(id)
            pin[id]=True


targets = []
with open(set2, 'r') as fh:
    for v in fh:
        line = v.strip()
        spl = line.split("\t")
        id = g.vertex(domainMap[spl[0]])
        groups[id] = 2
        if int(spl[1]) > 0:
            x = pos[id][0]
            y = pos[id][1]
            x_max = max(x_max, x)
            x_min = min(x_min, x)
            y_max = max(y_max, y)
            y_min = min(y_min, y)
            targets.append(id)
            pin[id] = True

y_dist = y_max-y_min
source_step = y_dist/len(sources)
target_step = y_dist/len(targets)

print(str(x_min)+" - "+str(x_max))
print(str(y_min)+" - "+str(y_max))

i=y_min
for v in sources:
    pos[v]=(x_min,i)
    i+=source_step

i=y_min
for v in targets:
    pos[v]=(x_max,i)
    i+=target_step

#colorMap = {'default': Colors.to_rgba('#ffffff')}
pos = sfdp_layout(g, C=0.15, p=1.5, r=2, K=6, pos=pos, pin=pin)

#with open(config) as fh:
#    conf = json.load(fh)
#    for vals in conf["nodes"]:
#        colorMap[vals["name"]] = Colors.to_rgba(vals["colors"]["light"])
#plot_color = g.new_vertex_property('vector<double>')
#g.vertex_properties['plot_color'] = plot_color

nodeCount = 0

for x in g.vertices():
    nodeCount += 1
#    try:
#        plot_color[x] = colorMap[g.properties[('v', "type")][x]]
#    except:
#        plot_color[x] = colorMap["default"]

#thumb = "/tmp/test.png"

#edgeCount = 0
#for e in g.edges():
#    edgeCount += 1

#scale = edgeCount / nodeCount
#imageHeight = int(math.sqrt(nodeCount) * 10 + min(500, int(500 / scale)))
factor = 10 ** int(math.log10(nodeCount)) *5
with open(layout, 'w') as fh:
    for x in g.vertices():
        fh.write(
            g.properties[('v', "type")][x] + "\t" + str(g.properties[('v', "primaryDomainId")][x]) + "\t" + str(
                pos[x][0] * factor) + "\t" + str(pos[x][1] * factor) + "\n")


#graph_draw(g, pos=pos, output=thumb, vertex_fill_color=g.vertex_properties['plot_color'],
#               output_size=(5000, 5000), vertex_size=40,fit_view_ink=True)
