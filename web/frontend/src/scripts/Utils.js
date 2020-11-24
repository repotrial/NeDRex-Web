

const Utils =
  {
  getColoring(metagraph, entity, name){
    if (entity === "nodes") {
      return metagraph.colorMap[name.toLowerCase()].main;
    } else {
     let names = this.getNodes(metagraph,name)
      return [metagraph.colorMap[names[0]].main, metagraph.colorMap[names[1]].main]
    }
  },

  getNodes(metagraph,name){
    let edge = Object.values(metagraph.edges).filter(n => n.label === name)[0]
    let n1 = Object.values(metagraph.nodes).filter(n => n.id === edge.from)[0].group
    let n2 = Object.values(metagraph.nodes).filter(n => n.id === edge.to)[0].group
    return[n1,n2]
  },
    isEdgeDirected: function (metagraph,edge) {
      let e = Object.values(metagraph.edges).filter(e => e.label === edge)[0];
      return e.from !== e.to;
    },
}
export default Utils
