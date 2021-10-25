<template>
  <v-dialog v-model="show"
            persistent
            max-width="1000px"
  >
    <v-card>
      <v-card-title>Adjust the disorder selection</v-card-title>
      <v-card-subtitle style="margin-top:5px"><i><b>Hover</b></i> nodes to read the full disorder name. <i><b>Click</b></i>
        a node to toggle its state. The state of all children changes accordingly.
      </v-card-subtitle>
      <v-progress-circular v-if="loading" indeterminate></v-progress-circular>
      <VisNetwork ref="tree" v-if="!loading && !sizeProblem" :options="treeOptions" :nodes="nodes" :edges="edges"
                  :events="['click','mousedown']"
                  @click="nodeClick"></VisNetwork>
      <v-card-subtitle v-if="sizeProblem">The disorder hierarchy contains more than 500 disorders. A graphical selection
        at this point is not feasible!
      </v-card-subtitle>
      <v-divider></v-divider>

      <v-card-actions>
        <v-btn
          text
          @click="resolvePopup(false)"
        >
          Cancel
        </v-btn>
        <v-btn
          color="green darken-1"
          text
          @click="resolvePopup(true)"
        >
          {{ sizeProblem ? "Add all" : "Add Selected" }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import {tree} from 'vued3tree'
import {DataSet, Network} from "vue-vis-network";

export default {

  name: "DisorderHierarchyDialog",
  components: {
    tree,
    'VisNetwork': Network
  },
  props: {
    value: Boolean,
  },
  data() {
    return {
      selected: {},
      show: false,
      data: {},
      loading: false,
      sizeProblem: false,
      layoutSaved: false,
      treeOptions: {
        autoResize: true,
        height: '500px',
        clickToUse: false,
        layout: {
          hierarchical: {
            direction: 'DU',
            nodeSpacing: 150,
            treeSpacing: 150,
            sortMethod: 'directed'
          }
        },
        physics: {
          enabled: false,
          stabilization: false,
        },
        groups: {
          disorder: {
            color: this.$global.metagraph.options.options.groups.disorder.color,
            shape: "box",
            font: {color: "white"}
          }, unselected: {
            color: {background: 'gray', border: 'black', highlight: {background: 'gray', border: 'black'}},
            shape: "box",
            font: {color: "white"}
          }
        },
        nodes: {
          fixed: true,
        }
      },
      nodes: undefined,
      edges: undefined
    }
  },

  methods: {
    init: function () {
      this.nodes = undefined
      this.edges = undefined
      this.sizeProblem = false;
      this.layoutSaved = false;
    },

    resolvePopup: function (state) {
      this.show = false
      this.model = state;
      this.$emit('input', this.model)
      if (state) {
        this.$emit("addDisorders", this.nodes.get().filter(n => n.group === "disorder").map(n => parseInt(n.id)))
      } else {
        this.$emit("addDisorders", undefined)
      }
      this.init()
    },
    loadDisorder: function (sid) {
      this.loading = true
      this.$http.getDisorderHierarchy(sid).then(result => {
        this.data = result
        if (this.data.nodes.length > 500) {
          this.sizeProblem = true;
        } else {
          this.prepareData(this.data)
        }
        this.loading = false;
      })
      this.show = true;
    },
    prepareData: function (data) {
      data.nodes.forEach(n => {
        n.title = n.label
        if (n.label.length > 20)
          n.label = n.label.substring(0, 17) + "..."
        n.selected = true
      })
      this.nodes = new DataSet(data.nodes)
      this.edges = new DataSet(data.edges)
    },

    toggleSelect: function (id) {
      if (!this.layoutSaved)
        this.saveLayout()
      let newGroup = ""
      let update = [this.$refs.tree.getNode(id)]
      newGroup = update[0].group === "disorder" ? "unselected" : "disorder"
      let ids = [id]
      this.edges.get().filter(e => e.to === id).forEach(e => update.push(this.$refs.tree.getNode(e.from)))
      ids = update.map(n => n.id)
      let idSize = 1
      while (idSize < ids.length) {
        idSize = ids.length
        this.edges.get().filter(e => ids.indexOf(e.to) > -1 && ids.indexOf(e.from) === -1).forEach(e => update.push(this.$refs.tree.getNode(e.from)))
        ids = update.map(n => n.id)
      }

      update.forEach(n => n.group = newGroup)
      this.nodes.update(update)
      this.layoutSaved = true
    },

    saveLayout: function () {
      let updates = Object.entries(this.$refs.tree.getPositions()).map(e => {
        return {id: e[0], x: e[1].x, y: e[1].y}
      })
      this.nodes.update(updates)
    },

    nodeClick: function (element) {
      if (element.nodes.length > 0)
        this.toggleSelect(element.nodes[0])
    }
  }
}
</script>

<style lang="scss">
#preloader {
  display: none;
}


</style>
