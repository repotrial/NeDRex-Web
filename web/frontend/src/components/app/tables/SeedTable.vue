<template>
  <v-data-table :max-height="height" :height="height" class="overflow-y-auto overflow-x-hidden" fixed-header
                dense item-key="id"
                :items="nodes"
                :headers="headers"
                disable-pagination
                hide-default-footer
                style="margin-top: 16px">
    <template v-slot:top>
      <div style="display: flex">
        <v-card-title style="justify-self: flex-start" class="subtitle-1">{{ title }}
        </v-card-title>
      </div>
    </template>
    <template v-slot:item.displayName="{item}">
      <v-tooltip v-if="item.displayName.length>16" right>
        <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substr(0, 16) }}...</span>
        </template>
        <span>{{ item.displayName }}</span>
      </v-tooltip>
      <span v-else>{{ item.displayName }}</span>
    </template>
    <template v-slot:item.origin="{item}">
      <template v-for="o in getOrigins(item.id)">
        <v-tooltip bottom :key="item.id+o">
          <template v-slot:activator="{attr,on }">
            <v-chip style="font-size: smaller; color: gray" pill v-on="on" v-bind="attr">{{
                o[0]
              }}
            </v-chip>
          </template>
          <span v-if="o[2]">Connected to <b>{{ o[2] }}</b>:<br><b>{{ o[1] }}</b></span>
          <span v-else-if="o[0]==='FILE'">Added from user file:<br><b>{{ o[1] }}</b></span>
          <span v-else>Returned by method:<br><b>{{ o[1] }}</b></span>
        </v-tooltip>
      </template>
    </template>
    <template v-slot:header.origin="{header}">
      <v-tooltip bottom>
        <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on">
                          Origin
                          </span>

        </template>
        <span>Holds the sources the seed node was added by:<br><b>SUG=</b>Suggestion, <b>FILE</b>=File input or <b>METH</b>=Other method</span>
      </v-tooltip>
    </template>
    <template v-slot:header.sourcedb="{header}">
      <v-tooltip bottom>
        <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on">
                          SourceDB
                          </span>

        </template>
        <span>Holds the source which provided information about the used association.</span>
      </v-tooltip>
    </template>
    <template v-slot:header.displayName="{header}">
      <v-tooltip bottom>
        <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on">
                          Name
                          </span>
        </template>
        <span>Holds the common name of the {{ nodeName }} if available.</span>
      </v-tooltip>
    </template>
    <template v-slot:item.action="{item}">
      <v-tooltip right>
        <template v-slot:activator="{attr,on }">
          <v-btn icon @click="removeNode(item.id)" color="red">
            <v-icon>far fa-trash-alt</v-icon>
          </v-btn>
        </template>
        <span>Remove the current entry from the seed selection!</span>
      </v-tooltip>
    </template>
    <template v-slot:footer>
      <div style="display: flex; justify-content: center;padding-top: 16px" v-if="nodes !=null && nodes.length>0">
        <SeedDownload v-show="download" @downloadListEvent="downloadNodes"></SeedDownload>
        <SeedRemove v-show="remove" @clearEvent="clear" @intersectionEvent="keepIntersection"></SeedRemove>
      </div>
    </template>
  </v-data-table>
</template>

<script>
import SeedDownload from "@/components/app/tables/menus/SeedDownload";
import SeedRemove from "@/components/app/tables/menus/SeedRemove";

export default {
  name: "SeedTable",

  props: {
    height: String,
    title: String,
    nodeName: String,
    download: Boolean,
    remove: Boolean,
  },

  origins: Object,
  data() {
    return {
      nodes: [],
      headers: [
        {text: 'Name', align: 'start', sortable: true, value: 'displayName'},
        {text: 'Origin', align: 'start', sortable: true, value: 'origin'},
        // {text: 'sourceDB', align: 'start', sortable: true, value: 'sourcedb'},
        {text: 'Action', align: 'end', sortable: false, value: 'action'}]
    }
  },

  created() {
    this.origins = {}
  },

  methods: {


    getOrigins: function (id) {
      if (this.origins[id] === undefined)
        return ["?"]
      else
        return this.origins[id].map(item => {
          let sp1 = item.split(":")
          let out = []
          out.push(sp1[0])
          if (out[0] === 'SUG') {
            let sp2 = sp1[1].split("[")
            out.push(sp2[0])
            out.push(sp2[1].substring(0, sp2[1].length - 1))
          } else {
            out.push(sp1[1])
          }
          return out;
        })
    }
    ,
    removeNode: function (id) {
      let index = this.nodes.map(e => e.id).indexOf(id)
      this.nodes.splice(index, 1)
      delete this.origins[id]
    },

    clear: function () {
      this.nodes = []
      this.origins = {}
    },

    downloadNodes: function (names, sep) {
      this.$http.post("mapToDomainIds", {
        type: this.nodeName,
        ids: this.nodes.map(s => s.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "#ID" + (names ? sep + "Name" : "") + "\n";
        let dlName = this.nodeName + "_nodes." + (!names ? "list" : (sep === '\t' ? "tsv" : "csv"))
        if (!names) {
          Object.values(data).forEach(id => text += id + "\n")
        } else {
          this.nodes.forEach(s => text += data[s.id] + sep + s.displayName + "\n")
        }
        this.execDownload(dlName, text)
      }).catch(console.log)
    },

    execDownload: function (name, content) {
      let dl = document.createElement('a')
      dl.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(content))
      dl.setAttribute('download', name)
      dl.style.direction = 'none'
      document.body.appendChild(dl)
      dl.click()
      document.body.removeChild(dl)
    },
    addSeeds(list, nameFrom) {
      let ids = this.nodes.map(seed => seed.id)
      let count = 0
      list.forEach(e => {
        if (ids.indexOf(e.id) === -1) {
          count++
          this.nodes.push(e)
        }
        if (this.origins[e.id] !== undefined) {
          if (this.origins[e.id].indexOf(nameFrom) === -1)
            this.origins[e.id].push(nameFrom)
        } else
          this.origins[e.id] = [nameFrom]
      })
      this.$emit("printNotificationEvent", "Added " + list.length + "from " + nameFrom + " (" + count + " new) seeds!", 1)
    },

    getSeeds() {
      return this.nodes;
    },
    keepIntersection: function () {
      let remove = []
      Object.keys(this.origins).forEach(seed => {
        if (this.origins[seed].length < 2) {
          delete this.origins[seed]
          remove.push(parseInt(seed))
        }
      })
      this.nodes = this.nodes.filter(s => remove.indexOf(s.id) === -1)
    },
  },


  components: {
    SeedDownload,
    SeedRemove
  }
}


</script>

<style scoped>

</style>
