const Utils =
  {
    getColoring(metagraph, entity, name, colorstyle) {
      if (entity === "nodes") {
        return metagraph.colorMap[name.toLowerCase()][colorstyle? colorstyle: "main"];
      } else {
        let names = this.getNodes(metagraph, name)
        return [metagraph.colorMap[names[0]][colorstyle? colorstyle: "main"], metagraph.colorMap[names[1]][colorstyle? colorstyle: "main"]]
      }
    },

    getColoringExtended(metagraph, entityGraph, entity, name,colorstyle) {
      if (entity === "nodes") {
        return metagraph.colorMap[name.toLowerCase()][colorstyle? colorstyle: "main"];
      } else {
        let names = this.getNodesExtended(entityGraph, name)
        return [metagraph.colorMap[names[0]][colorstyle? colorstyle: "main"], metagraph.colorMap[names[1]][colorstyle? colorstyle: "main"]]
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
      let n1 = entityGraph.nodes[edge.node1].name;
      let n2 = entityGraph.nodes[edge.node2].name;
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
  }
export default Utils
