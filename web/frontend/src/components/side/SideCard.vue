<template>
  <v-container :style="{position: 'fixed', width: sideWidth+'px' ,right: 0}">

    <v-card ref="scrollCard" elevation="3" style="padding-top: 15px; overflow-y: auto; max-height: 80vh">
      <v-card elevation="3" style="margin:15px"
              v-if="gid!==undefined && graphInfo !=null && Object.keys(options.list.entityGraph).length >0">

        <v-list-item @click="show.summary=!show.summary">
          <v-list-item-title>
            <v-icon left>{{ show.summary ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Summary
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>
        <template v-if="show.summary">
          <v-card-title v-if="!summaryTitleEdit"><span>{{ graphInfo.name }}
            <v-tooltip left>
              <template v-slot:activator="{attrs, on}">
                <v-btn icon style="top: -3px; left: -3px"  @click="summaryTitleEdit=true">
                  <v-icon size="15"  v-on="on" v-bind="attrs" color="primary">fas fa-edit</v-icon>
                </v-btn>
                </template>
              <div >Edit the name of the network</div>
              </v-tooltip>
          </span></v-card-title>
          <v-card-title v-else>
            <v-text-field v-model="graphInfo.name"></v-text-field>
            <span>
            <v-btn icon style="top: -3px; left: -3px" @click="summaryTitleEdit=false; saveGraphName()">
              <v-icon size="15" color="green">fas fa-check</v-icon>
            </v-btn>
              <v-btn icon style="top: -3px; left: -3px" @click="summaryTitleEdit=false">
              <v-icon size="15" color="red">fas fa-times</v-icon>
            </v-btn>
          </span>
          </v-card-title>

          <v-list>
            <v-list-item class="nedrex-list-item">
              <v-list-item-title class="nedrex-list-item-title">ID</v-list-item-title>
              <v-list-item-subtitle style="font-size: .6rem">{{ gid }}</v-list-item-subtitle>
            </v-list-item>
            <v-list-item>
              <v-list-item-title>Nodes
                ({{ getSelectedNumberOfNodes() }}/{{ getTotalNumberOfNodes() }})
              </v-list-item-title>
            </v-list-item>
            <v-list-item v-for="(count,name) in graphInfo.counts.nodes" :key="name" class="nedrex-list-item">
              <v-list-item-avatar>
                <v-icon left :color="getEntityGraph().nodes !=null ? getExtendedColoring('nodes',name,'light'):''">fas
                  fa-genderless
                </v-icon>
              </v-list-item-avatar>
              <v-list-item-subtitle>{{ name }}</v-list-item-subtitle>
              <v-list-item-subtitle style="min-width: 3rem; max-width: 4.5rem">{{
                  options.list.countMap.nodes[name] != null ? options.list.countMap.nodes[name].selected : 0
                }}/{{ count }}
              </v-list-item-subtitle>
            </v-list-item>
            <v-list-item>
              <v-list-item-title>Edges
                ({{
                  getSelectedNumberOfEdges()
                }}/{{ getTotalNumberOfEdges() }})
              </v-list-item-title>
            </v-list-item>
            <v-list-item v-for="(count,name) in graphInfo.counts.edges" :key="name" class="nedrex-list-item">
              <v-list-item-avatar>
                <v-icon class="nedrex-list-icon" size="15" :color="getExtendedColoring('edges',name,'light')[0]">fas
                  fa-genderless
                </v-icon>
                <template v-if=" directionExtended(name)===0">
                  <v-icon class="nedrex-list-icon" size="15">fas fa-undo-alt</v-icon>
                </template>
                <template v-else>
                  <v-icon class="nedrex-list-icon" size="15" v-if="directionExtended(name)===1">fas
                    fa-long-arrow-alt-right
                  </v-icon>
                  <v-icon class="nedrex-list-icon" size="15" v-else>fas fa-arrows-alt-h</v-icon>
                  <v-icon class="nedrex-list-icon" size="15" :color="getExtendedColoring('edges',name,'light')[1]">fas
                    fa-genderless
                  </v-icon>
                </template>
              </v-list-item-avatar>
              <v-list-item-subtitle>{{ name }}</v-list-item-subtitle>
              <v-list-item-subtitle style="min-width: 3rem; max-width: 4.5rem">{{
                  options.list.countMap.edges[name] != null ? options.list.countMap.edges[name].selected : 0
                }}/{{ count }}
              </v-list-item-subtitle>
            </v-list-item>
            <v-tooltip top>
              <template v-slot:activator="{attrs, on}">
                <v-chip
                  outlined
                  icon
                  v-on="on"
                  v-bind="attrs"
                  style="margin:8px"
                  @click="requestGraphDownload"
                >
                  <v-icon
                    left
                    small
                    color="primary"
                  >
                    far fa-arrow-alt-circle-down
                  </v-icon>
                  Download
                </v-chip>
              </template>
              <div style="width: 250px">Download a .graphml file containing the current network with all available
                attributes.
              </div>
            </v-tooltip>
            <v-tooltip top>
              <template v-slot:activator="{attrs, on}">
                <v-chip
                  v-on="on"
                  v-bind="attrs"
                  outlined
                  icon
                  style="margin:8px"
                  @click="copyLink(); printNotification('Copied graph link to clipboard!',1)"
                >
                  <v-icon
                    left
                    small
                    color="primary"
                  >
                    far fa-copy
                  </v-icon>
                  Copy URL
                </v-chip>
              </template>
              <div style="width: 250px">
                Copies the unique link of this network to your clipboard to save it to some document or to share it with
                others.
              </div>
            </v-tooltip>
          </v-list>
        </template>
      </v-card>

      <v-card-title>Toolbox</v-card-title>


      <v-card ref="options" elevation="3" style="margin:15px" v-if="selectedTab ===3">
        <v-list-item @click="show.options=!show.options">
          <v-list-item-title>
            <v-icon left>{{ show.options ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Options
          </v-list-item-title>
        </v-list-item>
        <template v-if="show.options">
          <v-divider></v-divider>
          <div style="width: 100%; display: flex; justify-content: center">
            <v-switch v-model="options.history.favos" dense label="Favourites Only"
                      @click="$emit('historyReloadEvent')">
            </v-switch>
          </div>
          <div style="width: 100%; display: flex; justify-content: center">
            <v-switch v-model="chronological" dense label="Show Chronological"
                      @click="$emit('historyReloadEvent')"></v-switch>
          </div>
          <div>
            <!--            <div style="width: 100%; display: flex; justify-content: center">-->
            <!--              <v-switch v-model="options.history.otherUsers" :disabled="!chronological" dense label="Show parent graphs of other users"-->
            <!--                        @click="$emit('historyReloadEvent')"></v-switch>-->
            <!--            </div>-->
            <div style="width: 100%; display: flex; justify-content: center">
              <v-chip outlined style="margin:8px" :disabled="!chronological" @click="$emit('reverseSortingEvent')">
                <v-icon small left color="primary">fas fa-sort</v-icon>
                Reverse Sorting
              </v-chip>
            </div>
          </div>
        </template>
      </v-card>

      <v-card ref="options" elevation="3" style="margin:15px" v-if="selectedTab ===2">
        <v-list-item @click="show.selectionTools=!show.selectionTools">
          <v-list-item-title>
            <v-icon left>{{ show.selectionTools ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Selection Tools
          </v-list-item-title>
        </v-list-item>
        <div v-if="show.selectionTools">
          <v-divider></v-divider>
          <v-card-subtitle style="font-size: 14pt"><i>General</i></v-card-subtitle>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <div v-on="on"
                   v-bind="attrs" style="width: 100%; display: flex; justify-content: center">
                <v-switch
                  style="margin:8px; font-size: small"
                  dense
                  v-model="options.list.showAll"
                  @click="$emit('reloadTablesEvent')">
                  <template v-slot:label>
                    <span>Show all Items ({{ options.list.selected }}/{{ options.list.total }})</span>
                  </template>
                </v-switch>
              </div>
            </template>
            <div style="width: 250px">
              Switch between looking all or only the nodes and edges that are in the current selection.
            </div>
          </v-tooltip>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                style="margin:8px"
                v-on="on"
                v-bind="attrs"
                outlined
                @click="$emit('selectionEvent','all','none')"
              >
                <v-icon left color="error" small>fas fa-trash</v-icon>
                Unselect All
              </v-chip>
            </template>
            <div>
              Resets the selection of nodes and edges
            </div>
          </v-tooltip>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                style="margin:8px"
                v-on="on"
                v-bind="attrs"
                outlined
                @click="$emit('graphModificationEvent','subselect');$forceUpdate"
              >
                <v-icon left small color="success">fas fa-project-diagram</v-icon>
                Load Selection
              </v-chip>
            </template>
            <div style="width: 300px">
              Creates a new sub-network based on the current selection. The new network will appear as a child
              of the previous network in the history hierarchy.
            </div>
          </v-tooltip>
          <v-divider style="margin-left: 16px; margin-right: 16px"></v-divider>
          <v-card-subtitle style="font-size: 14pt"><i>Nodes</i></v-card-subtitle>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                style="margin:8px"
                icon
                v-bind="attrs"
                v-on="on"
                outlined
                v-on:click="$emit('selectionEvent','nodes','all')"
              >
                <v-icon left small color="primary">fas fa-check-double</v-icon>
                Select All Nodes
              </v-chip>
            </template>
            <div style="width:250px">
              Adds all nodes in the current node table to the selection.
            </div>
          </v-tooltip>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                v-bind="attrs"
                v-on="on"
                style="margin:8px"
                icon
                outlined
                v-on:click="$emit('selectionEvent','nodes','induced')"
              >
                <v-icon small color="primary" left>fas fa-check</v-icon>
                Add Connected
              </v-chip>
            </template>
            <div style="width:250px">
              Select edges connecting selected nodes or also other nodes based on their connection to already selected
              ones.
            </div>
          </v-tooltip>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                style="margin:8px"
                v-on="on"
                v-bind="attrs"
                icon
                outlined
                v-on:click="$emit('selectionEvent','nodes','none')"
              >
                <v-icon left small color="error">fas fa-ban</v-icon>
                Unselect All Nodes
              </v-chip>
            </template>
            <div style="width:250px">
              Remove all nodes of the currently selected table from the selection!
            </div>
          </v-tooltip>
          <v-divider style="margin-left: 16px; margin-right: 16px"></v-divider>
          <v-card-subtitle style="font-size: 14pt"><i>Edges</i></v-card-subtitle>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                style="margin:8px"
                v-on="on"
                v-bind="attrs"
                icon
                outlined
                v-on:click="$emit('selectionEvent','edges','all')"
              >
                <v-icon left small color="primary">fas fa-check-double</v-icon>
                Select All Edges
              </v-chip>
            </template>
            <div style="width:250px">
              Adds all edges in the current edge table to the selection as well as nodes connected by these edges.
            </div>

          </v-tooltip>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                style="margin:8px"
                v-on="on"
                v-bind="attrs"
                icon
                outlined
                v-on:click="$emit('selectionEvent','edges','none')"
              >
                <v-icon left small color="error">fas fa-ban</v-icon>
                Unselect All Edges
              </v-chip>
            </template>
            <div style="width:250px">
              Remove all edges of the currently selected table from the selection!
            </div>
          </v-tooltip>
        </div>
      </v-card>


      <v-card ref="modify" elevation="3" style="margin:15px" v-if="selectedTab===2">
        <v-list-item @click="show.modify=!show.modify">
          <v-list-item-title>
            <v-icon left>{{ show.modify ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Modify
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>

        <div v-show="show.modify">
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                v-on="on"
                v-bind="attrs"
                v-on:click="$emit('graphModificationEvent','extend')"
                class="pa-3"
                outlined
                style="margin:8px"
              >
                <v-icon left small color="success">fas fa-plus-circle</v-icon>
                Extend Network
              </v-chip>
            </template>
            <div style="width: 250px">
              Add edges to the current network to either explore how the current nodes are interconnected or to add
              additional nodes to the network. This will create a new network which will be listed as a child of the
              current network on the history page.
            </div>
          </v-tooltip>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                v-on="on"
                v-bind="attrs"
                v-on:click="$emit('graphModificationEvent','collapse')"
                class="pa-3"
                outlined
                style="margin:8px"
              >
                <v-icon left small color="success">fas fa-compress-alt</v-icon>
                Infer new edge
              </v-chip>
            </template>
            <div style="width: 250px">
              Create a new, custom edge based on a path of length two in your current network (e.g. Diseasome). This
              will create a new network which will be listed as a child of the
              current network on the history page.
            </div>
          </v-tooltip>
          <v-tooltip top>
            <template v-slot:activator="{attrs, on}">
              <v-chip
                v-on="on"
                v-bind="attrs"
                v-on:click="$emit('colorSelectionEvent')"
                class="pa-3"
                outlined
                style="margin:8px"
              >
                <v-icon left small color="primary">fas fa-palette</v-icon>
                New group from selection
              </v-chip>
            </template>
            <div style="width: 250px">
              Create a new temporary node group based on the currently selected nodes. This will add an entry to the
              legend in the graph view and color all selected nodes in a distinct color which helps with visual
              separation.
            </div>
          </v-tooltip>
        </div>
      </v-card>

      <template v-if="(selectedTab===1 && options.graph.visualized)" :options="options.graph.selection">
        <Selection ref="selection" :options="options.graph.selection"
                   @selectModeEvent="toggleSelectMode"
                   @nodeSelectionEvent="loadSelection"
                   @applyMultiSelect="applyMultiSelect"
        >
        </Selection>

      </template>

      <v-card ref="info" elevation="3" style="margin:15px" v-if="(selectedTab===1 && options.graph.visualized)">

        <v-list-item @click="show.info=!show.info">
          <v-list-item-title>
            <v-icon left>{{ show.info ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
            Neighborhood
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>

        <v-container v-show="show.info">
          <v-card-text>Select a node to view its neighborhood. Double click the currently selected node to change to
            detail view.
          </v-card-text>
          <v-simple-table fixed-header height="300px" dense
                          v-if="selectedTab === 1 && (selectedNode !== undefined || (neighborNodes !== undefined && neighborNodes.length>0))">
            <template v-slot:default>
              <thead>
              <tr>
                <th class="text-center">ID</th>
                <th class="text-center">Name</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="selectedNode !== undefined" :key="selectedNode.id" v-on:dblclick="nodeDetails(selectedNode.id)">
                <td><b>{{ selectedNode.id }}</b></td>
                <td><b>{{ selectedNode.label }}
                </b></td>
              </tr>
              <tr v-for="item in neighborNodes" :key="item.id" v-on:click="setSelectedNode(item.id)">
                <td>{{ item.id }}</td>
                <td>{{ item.label }}
                </td>
              </tr>
              </tbody>
            </template>
          </v-simple-table>
          <i v-else>no selection available</i>
        </v-container>
      </v-card>
      <template v-if="selectedTab===2">
        <Algorithms ref="algorithms" @openAlgorithmDialogEvent="submitAlgorithm"></Algorithms>
        <Jobs ref="jobs" @graphLoadEvent="graphLoadEvent" @printNotificationEvent="printNotification"
              @reloadHistoryEvent="reloadHistory"></Jobs>
      </template>
      <template v-if="selectedTab===2 || selectedTab ===1">
        <v-card ref="detail" elevation="3" style="margin:15px"
                :loading="$global.metagraph==null">

          <v-list-item @click="show.detail=!show.detail">
            <v-list-item-title>
              <v-icon left>{{ show.detail ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
              Detail
            </v-list-item-title>
          </v-list-item>
          <v-divider></v-divider>


          <v-container v-show="show.detail">
            <EntryDetails ref="details" :gid="gid" :entity-graph="this.options.list.entityGraph"></EntryDetails>
          </v-container>

        </v-card>
      </template>
    </v-card>
  </v-container>

</template>

<script>
import Algorithms from "./toolbox/Algorithms.vue"
import Jobs from "./toolbox/Jobs"
import Selection from "./toolbox/Selection";
import * as CONFIG from "../../Config"
import EntryDetails from "@/components/app/EntryDetails";

export default {
  props: {
    options: Object,
    selectedTab: Number,
    filters: Object,
    sideWidth: Number,
  },
  name: "SideCard",
  title: "",
  description: "",
  selectedNode: undefined,
  neighborNodes: [],
  detailedObject: undefined,


  data() {
    return {
      gid: undefined,
      graphInfo: undefined,
      summaryTitleEdit: false,
      chronological: false,
      show: {
        selectionTools: true,
        options: true,
        summary: true,
        info: false,
        legend: false,
        detail: false,
        algorithms: false,
        jobs: false,
        modify: true,
        filter: true,
      },

      menu: {
        options: {
          list: {
            tab: 0,
            tabs: [{id: 0, label: "General"}, {id: 1, label: "Nodes"}, {id: 2, label: "Edges"}]
          }
        }
      },

      selectedNode: this.selectedNode,
      neighborNodes: this.neighborNodes,
      title: this.title,
      description: this.description,
      detailedObject: this.detailedObject,
      hover: {arrow: false,},
      details: {redirected: false},
    }
  },
  created() {
    this.init()
  },

  methods: {

    init: function () {
      this.gid = this.$route.params["gid"]
      if (this.gid != null)
        this.$http.get("getGraphHistory?gid=" + this.gid + "&uid=" + this.$cookies.get("uid")).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
          this.graphInfo = data
        }).catch(console.error)
    },
    isMac: function () {
      return this.$utils.isMac(window.navigator)
    },

    reload: function () {
      this.init()
      if (this.$refs.algorithms !== undefined)
        this.$refs.algorithms.resetAlgorithms()
      if (this.$refs.jobs !== undefined)
        this.$refs.jobs.reload()
      this.detailedObject = undefined
    },
    setAllSelected: function () {
      this.$emit("nodeSelectionEvent")
    },
    setOptions: function (name, options) {
      this.options[name] = options;
    },
    setSelectedNode: function (nodeId) {
      this.$emit("nodeSelectionEvent", nodeId)
    },
    loadSelection: function (params) {
      this.gid = this.$route.params["gid"]
      if (params !== undefined) {
        this.selectedNode = params.primary;
        this.neighborNodes = params.neighbors;
      } else {
        this.selectedNode = undefined;
        this.neighborNodes = [];
      }
    },

    reloadHistory: function () {
      this.$emit("reloadHistoryEvent")
    },

    getTotalNumberOfEdges: function () {
      let totalEdges = Object.values(this.graphInfo.counts.edges)
      if (totalEdges.length > 0)
        return totalEdges.reduce((s, v) => s + v)
      return 0
    },

    getSelectedNumberOfEdges: function () {
      let selectedEdges = Object.values(this.options.list.countMap.edges).map(s => s.selected)
      if (selectedEdges.length === 0)
        return 0
      return selectedEdges.reduce((s, v) => s + v)
    },

    getSelectedNumberOfNodes: function () {
      let selectedNodes = Object.values(this.options.list.countMap.nodes).map(s => s.selected);
      if (selectedNodes.length === 0)
        return 0
      return selectedNodes.reduce((s, v) => s + v)
    },

    getTotalNumberOfNodes: function () {
      let totalNodes = Object.values(this.graphInfo.counts.nodes)
      if (totalNodes.length > 0)
        return totalNodes.reduce((s, v) => s + v)
      return 0
    },


    openExternal: function (item, i) {
      window.open(this.getUrl(item, i), '_blank')
    }
    ,
    getColoring: function (type, name) {
      return this.$utils.getColoring(this.$global.metagraph, type, name)
    },

    getEntityGraph: function () {
      return this.options.list.entityGraph
    },

    getExtendedColoring: function (type, name, style) {
      try {
        return this.$utils.getColoringExtended(this.$global.metagraph, this.options.list.entityGraph, type, name, style)
      } catch (e) {
        console.warn("entityGraph might have not been fully initialized")
        return ''
      }
    },
    directionExtended: function (edge) {
      try {
        let e = Object.values(this.options.list.entityGraph.edges).filter(e => e.name === edge)[0];
        if (e.node1 === e.node2)
          return 0
        return e.directed ? 1 : 2
      } catch (e) {
        console.warn("entityGraph might have not been fully initialized")
        return 0
      }
    },

    saveGraphName: function () {
      this.$http.post("setGraphName", {gid: this.gid, name: this.graphInfo.name}).catch(console.error)
    },
    getNodeNames: function (type) {
      return this.$utils.getNodes(this.$global.metagraph, type)
    },
    getExtendedNodeNames: function (type) {
      return this.$utils.getNodesExtended(this.options.list.entityGraph, type)
    },

    toggleSelectMode: function (select) {
      this.$emit('selectModeEvent', select)
    }
    ,
    setFiltering: function () {
      this.show.filterAdding = true;
    }
    ,
    loadDetails: function (req) {
      this.show.detail = req != null;
      try {
        this.$refs.details.loadDetails(req).then(() => {
          this.$nextTick(() => {
            this.focusTop(this.$refs.detail)
          })
        })
      } catch (ignore) {

      }
    },

    focusTop: function (element) {
      this.scroll(element.$el.offsetTop)
    },

    scroll: function (offset) {
      const panel = this.$refs.scrollCard
      panel.$el.scrollTo({top: offset, behavior: "smooth"})
    },

    loadFilter: function (data) {
      if (data !== undefined) {
        this.filters = data.filters
        if (this.filters === undefined)
          this.filters = []
        this.filterName = data.name
      }
    }
    ,
    applyMultiSelect: function (selection) {
      this.$emit("applyMultiSelect", selection);
    },
    setMultiSelect: function (selection) {
      this.$refs.selection.setSelection(selection)
    },
    nodeDetails: function (id) {
      let str = id.split("_")
      this.$emit("nodeDetailsEvent", {prefix: str[0], id: str[1]})
    },
    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    }
    ,

    submitAlgorithm: function (data) {
      this.$emit('openAlgorithmDialogEvent', data)
    },
    graphViewEvent: function (data) {
      this.$emit("graphViewEvent", data)
    },
    addJob: function (data) {
      this.$refs.algorithms.resetAlgorithms()
      this.$refs.jobs.addJob(data)
    },
    graphLoadEvent: function (data) {
      this.$emit("graphLoadEvent", data)
    },
    formatTime: function (timestamp) {
      this.$utils.formatTime(timestamp)
    },
    requestGraphDownload: function () {
      window.open(CONFIG.HOST_URL + CONFIG.CONTEXT_PATH + '/api/downloadGraph?gid=' + this.gid)
    },
    copyLink: function () {
      const el = document.createElement('textarea');
      el.value = location.host + "/explore/advanced/list/" + this.gid;
      el.setAttribute('readonly', '');
      el.style.position = 'absolute';
      el.style.left = '-9999px';
      document.body.appendChild(el);
      const selected = document.getSelection().rangeCount > 0 ? document.getSelection().getRangeAt(0) : false;
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      if (selected) {
        document.getSelection().removeAllRanges();
        document.getSelection().addRange(selected);
      }
    }
  }
  ,
  components: {
    Algorithms,
    Jobs,
    EntryDetails,
    Selection
  },

}
</script>

<style lang="sass">


.span::-webkit-scrollbar
  display: none


.span
  -ms-overflow-style: none
  scrollbar-width: none

.nedrex-list-item
  max-height: 1rem
  min-height: 2rem !important

.nedrex-list-icon
  padding: 0 !important
  margin: 0 !important
  max-width: 15px !important
  min-width: 15px !important

.nedrex-list-item-title
  max-width: 3rem
</style>
