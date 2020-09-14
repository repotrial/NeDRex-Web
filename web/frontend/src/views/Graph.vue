<template>
  <div class="graph-window">
    <div>{{ message }}</div>
    <i>{{text}}</i>
    <v-progress-linear :buffer-value="100" :value=progress></v-progress-linear>
    <network class="wrapper" ref="network"
             :key="key"
             :nodes="nodes"
             :edges="edges"
             :options="options"
             :events="['stabilizationProgress','stabilizationIterationsDone']"
             @stabilizationProgress="onStabilizationProgress"
             @stabilizationIterationsDone="onStabilizationDone"
    >
    </network>
  </div>
</template>

<script>

export default {
  name: "graph",
  props: {
    payload: Object,
  },
  key: 0,
  progress:0,
  message: "Default Graph",
  nodes: [],
  edges: [],
  loading: true,
  text: "",
  options: {},
  created() {
    this.progress=0
    this.key = 0
    this.loadData()
  },

  data() {
    return {
      message: this.message,
      edges: this.edges,
      nodes: this.nodes,
      options: this.options,
      key: this.key,
      progress : this.progress,
      text: this.text
    }
  }
  ,
  methods: {
    loadData: function () {
      let defaults = this.getDefaults();
      if (this.payload) {
        if (this.payload.message)
          this.message = this.payload.message;
        else
          this.message = defaults.message

        if (this.payload.edges !== undefined)
          this.edges = this.payload.edges;
        else
          this.edges = defaults.edges;

        if (this.payload.nodes !== undefined)
          this.setNodes(this.payload.nodes)
        else
          this.nodes = defaults.nodes;

        if (this.payload["options"] !== undefined)
          this.mergeOptions(this.payload.options)
        else
          this.options = defaults.options;

        this.key += 1
      } else {
        this.message = defaults.message;
        this.edges = defaults.edges;
        this.nodes = defaults.nodes;
        this.options = defaults.options;
      }
    },
    getDefaults: function () {
      return {
        nodes: [
          {id: 1, label: 'Drug', shape: 'circle'},
          {id: 2, label: 'Protein', shape: 'circle'},
          {id: 3, label: 'Pathway', shape: 'circle'},
          {id: 4, label: 'Gene', shape: 'circle'},
          {id: 5, label: 'Disorder', shape: 'circle'},
        ],
        edges: [
          {from: 1, to: 2, label: 'DrugHasTarget'},
          {from: 1, to: 4, label: 'DrugHasTarget'},
          {from: 2, to: 2, label: 'ProteinInteractsWithProtein'},
          {from: 2, to: 3, label: 'ProteinInPathway'},
          {from: 2, to: 4, label: 'ProteinEncodedBy'},
          {from: 2, to: 5, label: 'ProteinAssociatedWithDisorder'},
          {from: 4, to: 5, label: 'GeneAssociatedWithDisorder'},
          {from: 5, to: 5, label: 'DisorderComorbidWithDisorder'},
          {from: 5, to: 5, label: 'DisorderIsADisorder'},
          {from: 5, to: 1, label: 'DrugHasIndication'},
        ],
        layout: {
          improvedLayout: true,
          // clusterThreshold: 1000,
          // hierarchical: {enabled: true}

        },
        options: {
          nodes: {
            fixed: false,
            physics: false,
            borderWidth: 2
          },
          edges: {
            // arrows:{to:{enabled:true}},
            // scaling:{label:{enabled: true}},
            smooth: {enabled: true},
            color: 'gray',
            // hidden: true,
            width: 0.3,
            physics: false,
            // length:300
          }
        },
        physics: {
          enabled: false,
          // solver: 'repulsion',
          stabilization:{enabled: true,updateInterval:1},
          timestep: 0.3,
          // wind: {x: 20, y: 20}
        }
      }
    },
    mergeOptions: function (options) {
      //TODO merge
      this.options = options;
    },
    setNodes: function (nodes) {
      this.nodes = nodes
    },
    onStabilizationProgress: function (params) {
      // this.loading = true;
      this.progress = 100*params.iterations / params.total;
      console.log(this.progress)
      // this.width = Math.max(this.minWidth, this.maxWidth * widthFactor);
      this.text = "Graph generation "+Math.round(this.progress) + "%";
      //
      // console.log(Math.round(widthFactor * 100) + "%")
    },
    onStabilizationDone: function () {
      this.text = "100%";
      this.width = this.maxWidth
      // this.loading = false;
    }


  }
}
</script>

<style scoped lang="sass">
.graph-window
  min-height: 100vh
  height: 100vh

.wrapper
  min-height: 100%
  border: 1px solid black
  background-color: #ffffff
  padding: 10px
  height: 100%


</style>
