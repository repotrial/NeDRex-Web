<template>
  <div>
    <v-container :style="{maxWidth: width+'px', display: 'flex'}">
      <v-card style="margin:5px;" :width="width*(selectedId?0.5:1)+'px'"
              :style="{maxWidth: width*(selectedId?0.5:1)-(selectedId?0:60)+'px'}">
        <v-card-title>History<span
          style="color:gray; padding-left: 7px"> ({{
            options.favos ? "Favourites" : options.chronological ? "List" : "Tree"
          }})</span></v-card-title>
        <div>
          <v-treeview
            :items="getHistoryList()"
            hoverable
            activatable
            dense
            shaped
            :active="selection"
            expand-icon="fas fa-angle-down"
            :transition="true"
            v-on:update:active="handleSelection"
          >
            <template v-slot:prepend="{item}">
              <v-list-item>
                <v-tooltip top>
                  <template v-slot:activator="{ on, attrs }">
                    <v-btn
                      v-if="item.state===undefined"
                      icon
                      :disabled="item.id === current || (item.state !== undefined &&item.state !== 'DONE')"
                      @click="loadGraph(item.id)"
                    >
                      <v-icon v-bind="attrs"
                              v-on="on"
                              :color="item.id === current ? 'gray': 'primary'"
                      >
                        far fa-play-circle
                      </v-icon>
                    </v-btn>
                    <v-btn
                      v-else
                      icon
                      :disabled="item.id === current || (item.state !== undefined &&item.state !== 'DONE')"
                      @click="loadJobFromGraph(item.id)"
                    >
                      <v-icon v-bind="attrs"
                              v-on="on"
                              :color="item.id === current ? 'gray':'green'"
                      >
                        far fa-play-circle
                      </v-icon>
                    </v-btn>
                  </template>
                  <span>{{ item.state !== undefined ? item.method : "MANUAL" }}</span>
                </v-tooltip>

                <v-divider vertical style="margin: 10px"></v-divider>
                <span style="color: darkgray; font-size: 10pt">
                    <v-tooltip top>
                      <template v-slot:activator="{ on, attrs }">
                        <v-icon
                          size="17px"
                          color="primary"
                          dark
                          v-bind="attrs"
                          v-on="on"
                        >
                          far fa-clock
                        </v-icon>
                      </template>
                    <span class="noselect">{{ formatTime(item.created)[0] }}</span>
                  </v-tooltip>

                    ({{ formatTime(item.created)[1] }} ago)
                  </span>
              </v-list-item>
            </template>
            <template v-slot:label="{item}"
            >
              <span class="noselect; cut-text; text-button">{{ getName(item) }}</span>
            </template>

          </v-treeview>
        </div>
      </v-card>

      <v-card v-if="selectedId!==undefined"
              :style="{position: 'fixed', overflowY: 'auto', maxHeight: '77vh', maxWidth: width*0.45+'px', width: width*0.45+'px', marginTop: '5px', left: width*0.55+'px', zIndex: 1000}">
        <div>
          <v-card v-if="selected===undefined" style="padding-bottom: 15px;">
            <v-card-title>{{ selectedId }}</v-card-title>
            <v-progress-circular
              color="primary"
              size="50"
              width="5"
              indeterminate
            ></v-progress-circular>
          </v-card>
          <v-card v-else style="padding-bottom: 15px">
            <v-container>
              <v-row v-if="selected.parentId !=null">
                <v-timeline align-top dense
                            style="margin-top: 10px; padding-top: 7px; margin-bottom: -29px; margin-left: -25px">
                  <v-timeline-item
                    right
                    :small="!hoveringTimeline('parent')"
                    :color="selected.parentMethod !=null? 'green':'primary'"
                  >
                    <v-btn plain style="color: gray; margin-left: -15px; margin-bottom:-25px; margin-top:-25px"
                           @click="handleSelection([selected.parentId])">{{ selected.parentName }}
                    </v-btn>
                    <br><br>
                    <template v-slot:icon>
                      <v-btn icon @click="loadGraph(selected.parentId)">
                        <v-icon x-small color="white">
                          fas fa-play
                        </v-icon>
                      </v-btn>
                    </template>
                  </v-timeline-item>
                </v-timeline>
              </v-row>
              <v-row dense>
                <v-col cols="1">
                  <v-btn icon v-if="selected.method!=null" color="green" x-large
                         style="background-color: white; margin-top: 5px;" @click="loadJob(selected.jobid)">
                    <v-icon>far fa-play-circle</v-icon>
                  </v-btn>
                  <v-btn icon v-else color='primary' x-large
                         style="background-color: white; margin-top: 5px;" @click="loadGraph(selectedId)">
                    <v-icon>far fa-play-circle</v-icon>
                  </v-btn>
                </v-col>
                <v-col cols="9">
                  <v-card-title v-if="!edit">{{ selected.name }}</v-card-title>
                  <v-text-field v-else :placeholder="selected.name" :value="selected.name" label="Name"
                                @change="setName" style="margin-left: 20px"></v-text-field>
                </v-col>
                <v-col cols="2" style="padding-top: 15px">
                  <template v-show="selected.owner = $cookies.get('uid')">
                    <v-btn v-if="!edit" icon style="margin-top:10px" x-small color="primary"
                           @click="toggleEdit()">
                      <v-icon>fas fa-edit</v-icon>
                    </v-btn>
                    <template v-else>
                      <v-btn icon style="margin-top:10px" x-small @click="toggleEdit()" color="success">
                        <v-icon>fas fa-check</v-icon>
                      </v-btn>
                      <v-btn icon style="margin-top:10px" x-small @click="resetEdit()" color="error">
                        <v-icon>fas fa-times</v-icon>
                      </v-btn>
                    </template>
                  </template>
                  <v-btn icon style="margin-top:10px" @mouseover="hover.star=true"
                         @mouseleave="hover.star=false"
                         x-small
                         @click="toggleStar" color="warning">
                    <v-icon v-if="showStar(false)">far fa-star</v-icon>
                    <v-icon v-if="showStar(true)">fas fa-star</v-icon>
                  </v-btn>
                  <v-btn v-show="selected.owner = $cookies.get('uid')" icon style="margin-top:10px" x-small color="error"
                         @click="deletePopup=true">
                    <v-icon>
                      fas fa-trash
                    </v-icon>
                  </v-btn>
                </v-col>
              </v-row>

              <v-divider
                style="margin: -10px 15px -10px 10px;"></v-divider>
              <v-row dense v-if="Object.keys(selected.children).length>0">
                <v-timeline align-top dense
                            style="margin-top: 10px; padding-top: 7px; padding-bottom: 0px; margin-left: 5px">
                  <v-timeline-item small right v-for="child in Object.keys(selected.children)" :key="child"
                                   :color="selected.children[child].method !==undefined ? 'green':'primary'"
                  >
                    <v-btn plain style="color: gray; margin-left: -15px; margin-bottom:-25px; margin-top:-25px"
                           @click="handleSelection([child])">{{ selected.children[child].name }}
                    </v-btn>
                    <template v-slot:icon>
                      <v-btn icon @click="loadGraph(child)" style="margin-left:2px">
                        <v-icon x-small color="white">
                          fas fa-play
                        </v-icon>
                      </v-btn>
                    </template>
                  </v-timeline-item>
                </v-timeline>
              </v-row>
              <v-row style="margin: 25px"></v-row>
              <!--/*              <v-row style="margin: 25px" v-if="selected.jobid!=null">*/-->
              <v-chip v-show="selected.jobid" outlined @click="loadJob(selected.jobid)">
                <v-icon left color="success">far fa-play-circle</v-icon>
                Reload Result View
              </v-chip>
              <v-chip v-show="selected.jobid" outlined @click="loadGraph(selectedId)">
                <v-icon left color="primary">far fa-play-circle</v-icon>
                Reload Network
              </v-chip>
              <v-chip v-show="selected.jobid" outlined @click="downloadJob(selected.jobid)" style="margin:8px">

                <v-icon left small color="primary">fas fa-download</v-icon>
                {{ selected.method }} Results
              </v-chip>
              <v-chip v-show="selected.jobid && selected.params!=null" outlined style="margin: 8px"
                      @click="showParams=!showParams">
                <v-icon left color="primary">{{ showParams ? 'fas fa-caret-up' : 'fas fa-caret-down' }}</v-icon>
                Chosen Parameters
              </v-chip>
              <!--              </v-row>-->
              <div v-if="selected.params" style="width: 100% ; display: flex; justify-content: center">
                <div style="max-width: 300px">
                  <v-simple-table v-show="showParams">
                    <template v-slot:default>
                      <thead>
                      <tr>
                        <th class="text-left">
                          Parameter
                        </th>
                        <th class="text-left">Value</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="(param,key) in selected.params"
                          v-if="['nodesOnly','addInteractions','type'].indexOf(key)===-1">
                        <td class="text-left">{{ key }}</td>
                        <td class="text-left">{{ param }}</td>
                      </tr>
                      </tbody>
                    </template>
                  </v-simple-table>
                </div>
              </div>
              <v-row v-if="$global.metagraph!=null &&selected!==undefined">
                <v-container>
                  <v-row>
                    <v-col>
                      <v-list>
                        <v-list-item>
                          <b>Nodes ({{ getCounts('nodes') }})</b>
                        </v-list-item>
                        <v-list-item v-for="node in Object.keys(selected.counts.nodes)"
                                     :key="node">
                          <v-chip outlined>
                            <v-icon left :color="getExtendedColoring('nodes',node)">fas fa-genderless</v-icon>
                            {{ node }} ({{ selected.counts.nodes[node] }})
                          </v-chip>
                        </v-list-item>

                      </v-list>
                    </v-col>
                    <v-col>
                      <v-list>
                        <v-list-item>
                          <b>Edges ({{ getCounts('edges') }})</b>
                        </v-list-item>
                        <v-list-item v-for="edge in Object.keys(selected.counts.edges)" :key="edge">
                          <v-chip outlined>
                            <v-icon left :color="getExtendedColoring('edges',edge)[0]">fas fa-genderless</v-icon>
                            <template v-if="directionExtended(edge)===0">
                              <v-icon left>fas fa-undo-alt</v-icon>
                            </template>
                            <template v-else>
                              <v-icon v-if="directionExtended(edge)===1" left>fas fa-long-arrow-alt-right</v-icon>
                              <v-icon v-else left>fas fa-arrows-alt-h</v-icon>
                              <v-icon left :color="getExtendedColoring('edges',edge)[1]">fas fa-genderless</v-icon>
                            </template>
                            {{ edge }} ({{ selected.counts.edges[edge] }})

                          </v-chip>

                        </v-list-item>
                      </v-list>
                    </v-col>
                  </v-row>


                </v-container>
              </v-row>
              <v-divider></v-divider>
              <v-row v-if="selected.thumbnailReady" style="padding:15px;display: flex;justify-content: center">
                <v-img max-height="28vw" max-width="28vw" :src="getThumbnail(selectedId)">
                </v-img>
              </v-row>
              <v-row v-else style="padding:15px;display: flex;justify-content: center">
                <i style="color:gray">creating preview...</i>
                <v-progress-linear
                  indeterminate
                  color="primary"
                ></v-progress-linear>
              </v-row>
              <v-row style="margin-top:10px">
                <v-textarea outlined label="Description" @change="updateDesc" :value="description" rows="5"
                            no-resize style="margin: 15px">
                  <template v-slot:append>
                    <v-container style="margin-right: -5px">
                      <v-row>
                        <v-btn icon @click="saveDescription(true)" :disabled="!showSaveDescription()">
                          <v-icon color="green">fas fa-check</v-icon>
                        </v-btn>
                      </v-row>
                      <v-row>
                        <v-btn icon @click="saveDescription(false)" :disabled="!showSaveDescription()">
                          <v-icon color="red">far fa-times-circle</v-icon>
                        </v-btn>
                      </v-row>
                    </v-container>
                  </template>
                </v-textarea>
              </v-row>
            </v-container>
          </v-card>
        </div>
      </v-card>
    </v-container>
    <v-dialog
      v-model="deletePopup"
      persistent
      style="z-index: 1001"
      max-width="500"
    >
      <v-card>
        <v-card-title>Confirm Deletion</v-card-title>
        <v-card-text>Do you really want to delete the selected Network?
        </v-card-text>
        <v-divider></v-divider>

        <v-card-actions>
          <v-btn
            color="green darken-1"
            text
            @click="closeDeletePop(false)"
          >
            Dismiss
          </v-btn>
          <v-btn
            color="red darken-1"
            text
            @click="closeDeletePop(true)"
          >
            Delete
          </v-btn>
        </v-card-actions>
      </v-card>

    </v-dialog>
  </div>
</template>

<script>
import * as CONFIG from "../../Config"

export default {
  name: "History.vue",
  props: {
    width: Number,
    options: Object,
  },
  data() {
    return {
      history: [],
      current: undefined,
      sort: false,
      showOtherUsers: false,
      user: undefined,
      list: [],
      reverseSorting: false,
      selection: [],
      description: "",
      selectedId: undefined,
      selected: undefined,
      showParams: false,
      hover: {star: false, timeline: {parent: false, children: {}}},
      edit: false,
      deletePopup: false,
    }
  },

  created() {
    this.$socket.$on("thumbnailReady", this.thumbnailReady)
    this.init()
  },

  methods: {
    init: function () {
      if (this.user === undefined)
        this.user = this.$cookies.get("uid")
      this.loadHistory()
    },
    reload: function () {
      this.loadHistory()
    },

    loadHistory: function () {
      this.current = this.$route.params["gid"];
      if (this.user !== undefined)
        this.$http.get("/getUser?user=" + this.$cookies.get("uid")).then(response => {
          if (response.data !== undefined)
            return response.data;
        }).then(data => {
          this.history = data.history.sort((a, b) => {
            return b.created - a.created
          });
          this.list = data.chronology;
        }).then(() => {
          this.handleSelection([this.current])
        }).catch(err => console.error(err))
    },
    loadGraph: function (graphid) {
      this.$emit("graphLoadEvent", {post: {id: graphid}})
    },
    hoveringTimeline: function (label) {
      return this.hover[label]
    },
    getName: function (item) {
      if (item.name === item.id) {
        let edges = 0;
        Object.values(item.edges).forEach(i => edges += i)
        let nodes = 0;
        Object.values(item.nodes).forEach(i => nodes += i)
        return "Edges: " + edges + "[" + Object.keys(item.edges).length + "]; Nodes: " + nodes + "[" + Object.keys(item.nodes).length + "]"
      }
      return item.name
    },
    directionExtended: function (edge) {
      let e = Object.values(this.selected.entityGraph.edges).filter(e => e.name === edge)[0];
      if (e.node1 === e.node2)
        return 0
      return e.directed ? 1 : 2
    },

    thumbnailReady: function (response) {
      let params = JSON.parse(response)
      if (params.gid === this.selectedId) {
        this.selected.thumbnailReady = true
      }
      this.$socket.unsubscribeThumbnail(params.gid)
    },
    getExtendedColoring: function (entity, name) {
      return this.$utils.getColoringExtended(this.$global.metagraph, this.selected.entityGraph, entity, name, 'light')
    },
    handleSelection: function (selected) {
      if (selected == null || selected[0] === undefined || this.selectedId === selected[0]) {
        this.selection = undefined
        this.selected = undefined;
        this.selectedId = undefined;
        return
      }
      this.selection = selected
      this.selected = undefined;
      this.selectedId = selected[0]
      this.$set(this, "selectedId", selected[0])
      this.$http.get("getGraphHistory?gid=" + this.selectedId + "&uid=" + this.$cookies.get("uid")).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.selected = data
        this.resetEdit()
        this.description = data.comment
      }).then(() => {
        if (!this.selected.thumbnailReady) {
          this.$socket.subscribeThumbnail(this.selectedId, "thumbnailReady")
        }
      }).catch(console.error)
    },
    closeDeletePop: function (apply) {
      this.deletePopup = false
      if (!apply)
        return
      this.$http.get("removeGraph?gid=" + this.selectedId).then(() => {
        if (this.current === this.selectedId)
          this.$router.push("/" + this.current + "/history")
        else
          this.reload()
      })
    },

    updateDesc: function (event) {
      this.description = event
    },
    showSaveDescription: function () {
      return this.description !== this.selected.comment
    },
    saveDescription: function (apply) {
      if (apply) {
        this.$http.post("saveGraphDescription", {"gid": this.selectedId, "desc": this.description}).then(() => {
          this.selected.comment = this.description
        })
      } else
        this.description = this.selected.comment
    },
    getCounts: function (entity) {
      let objects = Object.values(this.selected.counts[entity]);
      return objects === undefined || objects.length === 0 ? 0 : objects.reduce((i, j) => i + j)
    },

    formatTime: function (timestamp) {
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

    showStar: function (starred) {
      if (starred) {
        return (this.hover.star && !this.selected.starred) || (this.selected.starred && !this.hover.star)
      }
      return (this.hover.star && this.selected.starred) || (!this.selected.starred && !this.hover.star)
    },

    toggleStar: function () {
      this.$set(this.selected, "starred", !this.selected.starred);
      this.$set(this.list.filter(e => e.id === this.selectedId)[0], "starred", this.selected.starred)
      this.$http.get("toggleStarred?uid=" + this.$cookies.get("uid") + "&gid=" + this.selectedId).catch(console.error)
    },

    saveName: function () {
      this.getGraphsWithId(this.selectedId, this.history)[0].name = this.selected.name
      this.getGraphsWithId(this.selectedId, this.list)[0].name = this.selected.name
      this.$http.post("setGraphName", {gid: this.selectedId, name: this.selected.name}).catch(console.error)
    },

    getGraphsWithId: function (id, list) {
      let out = []
      list.forEach(h => {
        if (h.id === id)
          out.push(h)
        if (h.children !== undefined && h.children.length > 0) {
          let rest = this.getGraphsWithId(id, h.children)
          rest.forEach(r => out.push(r))
        }
      })
      return out;
    },

    getHistoryList: function () {
      let out = this.history;
      if (this.options.chronological) {
        out = this.getChronologicalList()
      }
      if (this.options.favos) {
        out = this.getChronologicalList().filter(l => l.starred)
      }
      return out

    },

    getChronologicalList: function () {
      if (!this.otherUsers)
        return this.list.filter(l => l.user === this.user)
      else
        return this.list
    },


    getThumbnail: function (graph_id) {
      return CONFIG.HOST_URL + CONFIG.CONTEXT_PATH + "/api/getThumbnailPath?gid=" + graph_id
    },

    // thumbnailExists: function (graph_id) {
    //   let http = new XMLHttpRequest()
    //   http.open("HEAD", this.getThumbnail(graph_id), false)
    //   let status = 2
    //   return http.onloadend = function () {
    //     if (http.status === 404) {
    //       status = 2
    //       return false
    //     } else
    //       return true
    //   }
    // },
    downloadJob: function (jobid) {
      window.open(CONFIG.HOST_URL + CONFIG.CONTEXT_PATH + '/api/downloadJobResult?jid=' + jobid)
    },

    loadJob: function (jobid) {
      this.$router.push({path: "/explore/quick/start?job=" + jobid})
      this.$router.go()
    },

    loadJobFromGraph: function (gid) {
      this.$http.get("getGraphHistory?gid=" + gid + "&uid=" + this.$cookies.get("uid")).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.loadJob(data.jobid)
      })
    },

    reverseList: function () {
      this.list = this.list.reverse()
    },

    setName: function (newName) {
      this.selected.name = newName
    },
    resetEdit: function () {
      if (this.selected.name_backup)
        this.selected.name = this.selected.name_backup
      this.edit = false
    },

    toggleEdit: function () {
      if (this.edit) {
        this.saveName()
      } else
        this.selected.name_backup = this.selected.name
      this.edit = !this.edit;
    }


  }
  ,


}
</script>

<style lang="sass">

.cut-text
  text-overflow: ellipsis
  overflow: hidden
  width: 160px
  height: 1.2em
  white-space: nowrap

.noselect
  -webkit-touch-callout: none
    -webkit-user-select: none
      -khtml-user-select: none
        -moz-user-select: none
          -ms-user-select: none
            user-select: none


</style>
