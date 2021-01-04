<template>
  <div>
    <v-container>
      <v-card style="margin:5px">
        <v-card-title>History<span
          style="color:gray; padding-left: 7px"> ({{ options.chronological ? "List" : "Tree" }})</span></v-card-title>
        <v-container ref="history">
          <v-row>
            <v-col>
              <v-treeview
                :items="options.chronological? (options.otherUsers ? list:list.filter((l)=>l.user === user)): history"
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
                          icon
                          :disabled="item.id === current || (item.state !== undefined &&item.state !== 'DONE')"
                          @click="loadGraph(item.id)"
                          v-bind="attrs"
                          v-on="on"
                        >
                          <v-icon
                            :color="item.id === current ? 'gray': item.state===undefined ?'primary':'green'"
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
            </v-col>
            <v-divider vertical></v-divider>
            <v-col v-if="selectedId!==undefined">
              <v-card v-if="selected===undefined" style="padding-bottom: 15px">
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
                                style="margin-top: 10px; padding-top: 7px; padding-bottom: 0px; margin-left: -5px">
                      <v-timeline-item
                        right
                        :small="!hoveringTimeline('parent')"
                        :color="selected.parentMethod !=null? 'green':'primary'"
                      >
                        <div style="color: gray; margin-left: -15px">{{ selected.parentName }}</div>
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
                    <v-col cols="9">
                      <v-card-title v-if="!edit">{{ selected.name }}</v-card-title>
                      <v-text-field v-else :placeholder="selected.name" :value="selected.name" label="Name"
                                    @change="setName"></v-text-field>
                    </v-col>
                    <v-col cols="3">
                      <v-btn v-show="selected.owner = $cookies.get('uid')" icon style="margin-top:10px"
                             @click="toggleEdit()">
                        <v-icon>{{ edit ? "fas fa-check" : "fas fa-edit" }}</v-icon>
                      </v-btn>
                      <v-btn icon style="margin-top:10px" @mouseover="hover.star=true" @mouseleave="hover.star=false"
                             @click="toggleStar">
                        <v-icon v-if="showStar(false)">far fa-star</v-icon>
                        <v-icon v-if="showStar(true)">fas fa-star</v-icon>
                      </v-btn>
                      <v-btn v-show="selected.owner = $cookies.get('uid')" icon style="margin-top:10px"
                             @click="deletePopup=true">
                        <v-icon>
                          fas fa-trash
                        </v-icon>
                      </v-btn>
                    </v-col>
                  </v-row>
                  <v-divider style="margin-left:15px; margin-right:15px; margin-top: -10px; margin-bottom:-10px"></v-divider>
                  <v-row dense v-if="Object.keys(selected.children).length>0">
<!--                    <v-col cols="2" class="d-flex align-center justify-end">-->
<!--                      <div>-->
<!--                        <i>Children</i>-->
<!--                      </div>-->
<!--                    </v-col>-->
<!--                    <v-col>-->
                      <v-timeline align-top dense style="margin-top: 10px; padding-top: 7px; padding-bottom: 0px; margin-left: 5px">
                        <v-timeline-item small right v-for="child in Object.keys(selected.children)" :key="child"
                                         :color="selected.children[child].method !==undefined ? 'green':'primary'"
                        >
                          <div style="color: gray; margin-left: -15px">{{ selected.children[child].name }}</div>
                          <template v-slot:icon>
                            <v-btn icon @click="loadGraph(child)">
                              <v-icon x-small color="white">
                                fas fa-play
                              </v-icon>
                            </v-btn>
                          </template>
                        </v-timeline-item>
                      </v-timeline>
<!--                    </v-col>-->
                  </v-row>
                </v-container>
              </v-card>
            </v-col>
          </v-row>
        </v-container>
      </v-card>
    </v-container>
    <v-dialog
      v-model="deletePopup"
      persistent
      max-width="500"
    >
      <v-card>
        <v-card-title>Confirm Deletion</v-card-title>
        <v-card-text>Do you really want to delete the selected Graph?
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
export default {
  name: "History.vue",
  props: {
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
      selectedId: undefined,
      selected: undefined,
      hover: {star: false, timeline: {parent: false, children: {}}},
      edit: false,
      deletePopup: false,
    }
  },

  created() {
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
      // this.selectedId = this.current;
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
        }).catch(err => console.log(err))
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

    handleSelection: function (selected) {
      if (selected[0] === undefined || this.selectedId === selected[0])
        return
      this.selection = selected
      this.selected = undefined;
      this.selectedId = selected[0]
      this.$http.get("getGraphHistory?gid=" + this.selectedId + "&uid=" + this.$cookies.get("uid")).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.selected = data
      }).catch(console.log)

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
      this.selected.starred = !this.selected.starred;
      this.$http.get("toggleStarred?uid=" + this.$cookies.get("uid") + "&gid=" + this.selectedId).catch(console.log)
    },

    saveName: function () {
      this.getGraphsWithId(this.selectedId, this.history)[0].name = this.selected.name
      this.getGraphsWithId(this.selectedId, this.list)[0].name = this.selected.name
      this.$http.post("setGraphName", {gid: this.selectedId, name: this.selected.name}).catch(console.log)
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

    reverseList: function () {
      this.list = this.list.reverse()
    },

    setName: function (newName) {
      this.selected.name = newName
    },

    toggleEdit: function () {
      if (this.edit)
        this.saveName()
      this.edit = !this.edit;
    }


  },


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
