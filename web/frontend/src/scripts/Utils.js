const Utils =
  {
    getColoring(metagraph, entity, name, colorstyle) {
      if (entity === "nodes") {
        return metagraph.colorMap[name.toLowerCase()][colorstyle ? colorstyle : "main"];
      } else {
        let names = this.getNodes(metagraph, name)
        return [metagraph.colorMap[names[0]][colorstyle ? colorstyle : "main"], metagraph.colorMap[names[1]][colorstyle ? colorstyle : "main"]]
      }
    },

    getColoringExtended(metagraph, entityGraph, entity, name, colorstyle) {
      if (entity === "nodes") {
        return metagraph.colorMap[name.toLowerCase()][colorstyle ? colorstyle : "main"];
      } else {
        let names = this.getNodesExtended(entityGraph, name)
        return [names[0] !== "default" ? metagraph.colorMap[names[0]][colorstyle ? colorstyle : "main"] : "gray", names[1] !== "default" ? metagraph.colorMap[names[1]][colorstyle ? colorstyle : "main"] : "gray"]
      }
    },

    directionExtended: function (entityGraph, edge) {
      let e = Object.values(entityGraph.edges).filter(e => e.name === edge)[0];
      if (e.node1 === e.node2)
        return 0
      return e.directed ? 1 : 2
    },

    getNodesExtended(entityGraph, name) {
      let edge = Object.values(entityGraph.edges).filter(n => n.name === name)[0]
      let n1 = entityGraph.nodes[edge.node1] ? entityGraph.nodes[edge.node1].name : "default";
      let n2 = entityGraph.nodes[edge.node2] ? entityGraph.nodes[edge.node2].name : "default";
      return [n1, n2]
    },

    getNodes(metagraph, name) {
      let edge = Object.values(metagraph.edges).filter(n => n.label === name)[0]
      let n1 = Object.values(metagraph.nodes).filter(n => n.id === edge.from)[0].group
      let n2 = Object.values(metagraph.nodes).filter(n => n.id === edge.to)[0].group
      return [n1, n2]
    },
    isEdgeDirected: function (metagraph, edge) {
      let e = Object.values(metagraph.edges).filter(e => e.label === edge)[0];
      return e.from !== e.to;
    },

    formatTime: function (timestamp) {
      if (timestamp === undefined)
        return ["0", "0"]
      timestamp *= 1000
      let d = new Date();
      let date = new Date(timestamp);
      let diff = ((d.getTime() - (d.getTimezoneOffset() * 60 * 1000)) - timestamp) / 1000;
      let diff_str = "~";
      if (diff < 60)
        diff_str = "<1m";
      else if (diff < 60 * 60)
        diff_str += Math.round(diff / (60)) + "m";
      else if (diff < 60 * 60 * 24)
        diff_str += Math.round(diff / (60 * 60)) + "h";
      else if (diff < 60 * 60 * 24 * 365)
        diff_str += Math.round(diff / (60 * 60 * 24)) + "d";
      else
        diff_str += Math.round(diff / (60 * 60 * 24 * 356)) + "a";
      return [date.toISOString().slice(0, -8), diff_str]
    },

    readFile: function (file) {
      if (file === undefined)
        return undefined
      return new Promise((resolve, reject) => {
        var fr = new FileReader();
        fr.onload = () => {
          resolve(fr.result)
        };
        fr.readAsDataURL(file);
      });
    },

    waitForFileContent: function (file) {
      return this.readFile(file).then(content => {
        return content
      })
    }
    ,
    roundScores: function (data, nodeType, scoreAttrs) {
        data.nodes[nodeType].forEach(item => {
          if (typeof scoreAttrs === 'object') {
            if (scoreAttrs.id)
              this.roundScores(item, scoreAttrs.id)
            else
              scoreAttrs.forEach(scoreAttr => this.roundScore(item, scoreAttr.id))
          } else
            this.roundScore(item, scoreAttrs)
        })
      return data
    },
    roundValue: function (score, decimals) {
      if ((score + "").indexOf('e') > -1) {
        let parts = (score + "").split('e')
        score = parseFloat(this.round(parseFloat(parts[0]), 2) + 'e' + parts[1])
      } else {
        score = this.round(score, decimals)
      }
      return score
    },

    roundScore: function (item, scoreAttr) {
      if (!item[scoreAttr])
        return
      let score = item[scoreAttr]
      if ((score + "").indexOf('e') > -1) {
        let parts = (score + "").split('e')
        score = parseFloat(this.round(parseFloat(parts[0]), 2) + 'e' + parts[1])
      } else {
        score = this.round(score, 6)
      }
      item[scoreAttr] = score
    },

    round: function (value, decimals) {
      let factor = Math.pow(10, decimals)
      return Math.round(value * factor) / factor
    },
    adjustLabels: function (label) {
      if (label.endsWith("_HUMAN"))
        label = label.substring(0, label.length - 6)
      return label
    },

    clone: function (object) {
      return JSON.parse(JSON.stringify(object))
    },

    checkSeparator: function (data, seps) {
      let sep = seps[0]
      let min = 1;
      seps.forEach(s => {
        let spl = data.split(s)
        if (spl.length > min) {
          sep = s;
          min = spl.length
        }
      })
      return sep;
    },

    adjustMetaOptions(metagraph) {
      function drawEllipse({ctx, id, x, y, state: {selected, hover}, style, label}) {
        return {
          // bellow arrows
          // primarily meant for nodes and the labels inside of their boundaries
          drawNode() {
            ctx.fillStyle = "black"
            ctx.beginPath()
            ctx.ellipse(x, y, 50, 25, 0, 0, Math.PI * 2);
            ctx.fill();
          },
          // above arrows
          // primarily meant for labels outside of the node
          drawExternalLabel() {
            ctx.fillText(label, 20, 100)
          },
          // node dimensions defined by node drawing
          nodeDimensions: {width: 50, height: 25},
        };
      }

      metagraph.options.options.groups.pathway["shape"] = "triangle";
      metagraph.options.options.groups.pathway["ctxRenderer"] = drawEllipse;

      return metagraph;
    }
  }
export default Utils
