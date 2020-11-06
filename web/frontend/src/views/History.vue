<template>
  <div>
    <v-container>
      <v-card style="margin:5px">
        <v-card-title>History</v-card-title>
        <v-container>
          <v-row>
            <v-col>
              <v-switch v-model="sort" label="Show chronological"></v-switch>
            </v-col>
            <v-col>
              <v-switch v-show="sort" v-model="showOtherUsers" label="Show parent graphs of other users"></v-switch>
            </v-col>
            <v-col>

              <v-chip v-show="sort" @click="reverseList">Reverse sorting</v-chip>

            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-treeview :items="sort? (showOtherUsers ? list:list.filter((l)=>l.user === user)): history"
                          hoverable
                          activatable
                          :active="[current]"
                          expand-icon="fas fa-angle-down"
                          open-on-click
                          :transition="true"
              >
                <template v-slot:prepend="{item}">

                  <span style="color: darkgray; font-size: 10pt">
                    <v-tooltip right>
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
                    <span>{{ formatTime(item.created)[0] }}</span>
                  </v-tooltip>

                    ({{ formatTime(item.created)[1] }} ago)


                  </span>

                </template>
                <template v-slot:label="{item}"
                >
                  <v-chip :disabled="item.id === current" :color="item.id === current ? 'gray':'primary'"
                          @click="loadGraph(item.id)">
                    <span>{{ getName(item) }}</span>
                  </v-chip>
                </template>

              </v-treeview>
            </v-col>
            <v-col>
              <v-card>


              </v-card>
            </v-col>
          </v-row>
        </v-container>
      </v-card>

    </v-container>
  </div>
</template>

<script>
export default {
  name: "History.vue",

  data() {
    return {
      history: [],
      current: undefined,
      sort: false,
      showOtherUsers: false,
      user: undefined,
      list: [],
      reverseSorting: false,
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
      if (this.user !== undefined)
        this.$http.get("/getUser?user=" + this.$cookies.get("uid")).then(response => {
          if (response.data !== undefined)
            return response.data;
        }).then(data => {
          this.history = data.history;
          this.list = data.chronology;
        }).catch(err => console.log(err))
    },
    loadGraph: function (graphid) {
      this.$emit("graphLoadEvent", {post: {id: graphid}})
    },
    getName: function (item) {
      let edges = 0;
      Object.values(item.edges).forEach(i => edges += i)
      let nodes = 0;
      Object.values(item.nodes).forEach(i => nodes += i)
      return "Edges: " + edges + "[" + Object.keys(item.edges).length + "]; Nodes: " + nodes + "[" + Object.keys(item.nodes).length + "]"
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

    reverseList: function () {
      this.list = this.list.reverse()
    },


  },


}
</script>

<style scoped>

</style>
