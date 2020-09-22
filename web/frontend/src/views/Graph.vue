<template>
  <div class="graph-window">
    <i>{{ text }}</i>
    <v-progress-linear v-show="loading" indeterminate :color=loadingColor></v-progress-linear>
    <network v-show="!loading" class="wrapper" ref="network"
             :key="key"
             :nodes="nodeSet"
             :edges="edges"
             :options="options"
             :layout="layout"
             :physics="physics"
             :events="['stabilizationProgress','stabilizationIterationsDone','click']"
             @click="onClick"
             @stabilizationProgress="onStabilizationProgress"
             @stabilizationIterationsDone="onStabilizationDone"
    >
    </network>
  </div>
</template>

<script>
import {DataSet} from 'vue-vis-network'

export default {
  name: "graph",
  props: {
    payload: Object,
  },
  key: 0,
  nodeSet: Object,
  loading: true,
  text: "",
  nodes: Object,
  edges: Object,
  options: Object,
  layout: Object,
  physics: Object,
  highlight: false,
  colors: {},
  directed: false,
  loadingColor: 'primary',

  created() {
    this.progress = 0
    this.key = 0
    this.loading = true;
    this.colors = {bar: {backend: "#6db33f", vis: 'primary', error: 'red darken-2'}}
    this.highlight = false
    this.loadData(this.payload)
  },

  data() {
    return {
      edges: this.edges,
      nodeSet: this.nodeSet,
      options: this.options,
      layout: this.layout,
      physics: this.physics,
      key: this.key,
      text: this.text,
      loading: this.loading,
      loadingColor: this.loadingColor,
    }
  }
  ,
  methods: {
    getData: function (url) {
      this.loading = true;
      this.directed = false;
      this.loadingColor = this.colors.bar.backend;
      this.$http.get(url).then(response => {
        return response.data
      }).then(graph => {
        this.setGraph(graph)
      }).catch(err => {
        this.loadingColor = this.colors.bar.error;
        console.log(err)
      })
    },

    setGraph: function (graph) {
      this.loadingColor = this.colors.bar.vis
      if (graph) {
        if (graph.directed !== undefined)
          this.directed = graph.directed
        if (graph.edges !== undefined)
          this.edges = new DataSet(graph.edges);

        if (graph.nodes !== undefined)
          this.nodeSet = new DataSet(graph.nodes)

        if (graph["options"] !== undefined)
          this.mergeOptions(graph.options)
      }

      this.drawGraph();
    },
    isLoading: function () {
      return this.loading
    },
    drawGraph: function () {
      if (this.directed) {
        this.options.edges["arrows"] = {to: {enabled: true}}
      } else
        this.options.edges["arrows"] = {};
      this.key += 1
      this.nodes = this.nodeSet.get({returnType: "Object"})
      // this.setNodeColors()
      this.loading = false;
      this.$emit('finishedEvent')
    },
    loadData: function (payload) {
      this.loading = true
      let defaults = this.getDefaults()

      this.options = defaults.options;
      this.layout = defaults.layout;
      this.physics = defaults.physics;
      this.edges = defaults.edges;
      this.nodeSet = defaults.nodes;
      this.directed = defaults.directed;

      if (payload) {
        if (payload.get !== undefined)
          this.getData(payload.get)
        else if (payload.post !== undefined)
          this.postData(payload.post)
        else
          this.drawGraph()
      } else
        this.drawGraph()


    },
    postData: function (post) {
      this.loading = true;
      this.directed = false;
      this.loadingColor = this.colors.bar.backend;
      this.$http.post("/getGraph", post).then(response => {
        return response.data
      }).then(graph => {
        console.log(graph)
        this.setGraph(graph)
      }).catch(err => {
        this.loadingColor = this.colors.bar.error;
        console.log(err)
      })
    }
    ,
    setSelection: function (nodes) {
      this.$refs.network.selectNodes(nodes)
      this.identifyNeighbors(nodes[0])
    },
    // setNodeColors: function () {
    //   let nodesContainColor = false
    //   if (this.options.nodes.color !== undefined) {
    //     this.nodeSet.getIds().forEach(id => {
    //       if (this.nodeSet.get(id).color !== undefined)
    //         nodesContainColor = true;
    //     })
    //     if (!nodesContainColor) {
    //       for (let id in this.nodes) {
    //         this.nodes[id].color = this.options.nodes.color
    //       }
    //     }
    //   }
    //   this.updateNodes()
    // },
    getDefaults: function () {
      return {
        directed: true,
        nodes: new DataSet([
          {id: 1, group: 'drug', label: 'Drug', title: 'Drug', shape: 'circle'},
          {id: 2, group: 'protein', label: 'Protein', title: 'Protein', shape: 'circle'},
          {id: 3, group: 'pathway', label: 'Pathway', title: 'Pathway', shape: 'circle'},
          {id: 4, group: 'gene', label: 'Gene', title: 'Gene', shape: 'circle'},
          {id: 5, group: 'disorder', label: 'Disorder', title: 'Disorder', shape: 'circle'},
        ]),
        edges: new DataSet([
          {from: 1, to: 2, label: 'DrugHasTarget'},
          {from: 1, to: 4, label: 'DrugHasTarget', dashes: true},
          {from: 2, to: 2, label: 'ProteinInteractsWithProtein',},
          {from: 2, to: 3, label: 'ProteinInPathway'},
          {from: 2, to: 4, label: 'ProteinEncodedBy'},
          {from: 2, to: 5, label: 'ProteinAssociatedWithDisorder', dashes: true},
          {from: 4, to: 5, label: 'GeneAssociatedWithDisorder'},
          {from: 5, to: 5, label: 'DisorderComorbidWithDisorder'},
          {from: 5, to: 5, label: 'DisorderIsADisorder'},
          {from: 1, to: 5, label: 'DrugHasIndication'},
        ]),
        layout: {
          improvedLayout: true,
          // clusterThreshold: 1000,
          // hierarchical: {enabled: true}

        },
        options: {
          groups: {
            drug: {
              color: {
                border: '#00CC96',
                background: '#b4cdcc',
                highlight: {border: '#00CC96', background: '#b4cdcc'}
              }
            },
            disorder: {
              color: {
                border: '#EF553B',

                background: '#ecd0cb',
                highlight: {border: '#EF553B', background: '#ecd0cb'}
              }
            },
            gene: {
              color: {
                border: '#636EFA',

                background: '#d6d9f8',
                highlight: {border: '#636EFA', background: '#d6d9f8'}
              }
            },
            protein: {
              color: {
                border: '#19d3f3',

                background: '#bcdfe5',
                highlight: {border: '#19d3f3', background: '#bcdfe5'}
              }
            },
            pathway: {
              color: {
                border: '#fecb52',

                background: '#fae6c1',
                highlight: {border: '#fecb52', background: '#fae6c1'}
              }
            },
            other: {
              color: {border: '#6b6a6a', background: '#D2E5FF', highlight: {border: '#6b6a6a', background: '#D2E5FF'}}
            }
          },
          nodes: {
            fixed: false,
            physics: false,
            borderWidth: 2

          },
          edges: {
            // arrows:{to:{enabled:true}},
            // scaling:{label:{enabled: true}},
            smooth: {enabled: true},
            color: '#454545',
            // hidden: true,
            width: 0.3,
            physics: false,
            // length:300
          }
        },
        physics: {
          enabled: false,
          // solver: 'repulsion',
          stabilization: {enabled: true, updateInterval: 1},
          timestep: 0.3,
          // wind: {x: 20, y: 20}
        }
      }
    }
    ,
    mergeOptions: function (options) {
      //TODO merge
      this.options = options;
    }
    ,
    onStabilizationProgress: function (params) {
      // this.loading = true;
      this.progress = 100 * params.iterations / params.total;
      console.log(this.progress)
      // this.width = Math.max(this.minWidth, this.maxWidth * widthFactor);
      this.text = "Graph generation " + Math.round(this.progress) + "%";
      //
      // console.log(Math.round(widthFactor * 100) + "%")
    }
    ,
    onStabilizationDone: function () {
      this.text = "100%";
      this.width = this.maxWidth
      // this.loading = false;
    }
    ,
    onClick: function (params) {
      if (params.nodes.length > 0) {
        let selected = params.nodes[0];
        this.identifyNeighbors(selected)
      } else if (this.highlight === true) {
        // for (let nodeId in this.nodes) {
        //   this.nodes[nodeId].color = undefined
        //   if (this.nodes[nodeId].hiddenLabel !== undefined) {
        //     this.nodes[nodeId].label = this.nodes[nodeId].hiddenLabel;
        //     this.nodes[nodeId].hiddenLabel = undefined;
        //   }
        this.$emit('selectionEvent')
        // }
        this.highlight = false;
      }


      this.updateNodes()

    }
    ,
    identifyNeighbors: function (selected) {
      //   this.highlight = true;
      //   this.uncolorNodes()
      //
      //   let toColor = []
      //   toColor.push(selected)
      let neighbors = []
      this.getConnectedNodes(selected).forEach(n => {
        // toColor.push(n)
        if (n !== selected)
          neighbors.push(this.nodeSet.get(n))
      })
      //   this.recolorPrimary(toColor)
      this.$emit('selectionEvent', {primary: this.nodeSet.get(selected), neighbors: neighbors})
    },
    uncolorNodes: function () {
      for (let nodeId in this.nodes) {
        this.nodes[nodeId].color = "rgba(200,200,200,0.5)";
        if (this.nodes[nodeId].hiddenLabel === undefined) {
          this.nodes[nodeId].hiddenLabel = this.nodes[nodeId].label;
          this.nodes[nodeId].label = undefined;
        }
      }
    }
    ,
    updateNodes: function () {
      let updates = []

      for (let nodeId in this.nodes) {
        if (this.nodes.hasOwnProperty(nodeId))
          updates.push(this.nodes[nodeId])
      }
      this.nodeSet.update(updates)
      // this.key++;
    }
    ,
    recolorPrimary: function (nodes) {
      nodes.forEach(nodeId => {
        this.nodes[nodeId].color = undefined;
        if (this.nodes[nodeId].hiddenLabel !== undefined) {
          this.nodes[nodeId].label = this.nodes[nodeId].hiddenLabel;
          this.nodes[nodeId].hiddenLabel = undefined;
        }
      })
    }
    ,
    getConnectedNodes: function (node) {
      return this.$refs.network.getConnectedNodes(node)
    }
    ,
    getNetwork: function () {
      return this.$refs.network;
    }


  }
}
</script>

<style scoped lang="sass">
.graph-window
  border: 1px solid gray
  min-height: 75vh
  height: 75vh

.wrapper
  min-height: 100%

  background-color: #ffffff
  padding: 10px
  height: 100%


</style>
