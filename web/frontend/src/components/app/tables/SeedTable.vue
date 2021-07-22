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
                o[0] + (o[2]!=null ? ":" + o[2].substring(0, 3).toUpperCase():"")
              }}
            </v-chip>
          </template>
          <span v-if="o[2]">Connected to <b>{{ o[2] }}</b>:<br><b>{{ o[1] }}</b></span>
          <span v-else-if="o[0]==='FILE'">Added from user file:<br><b>{{ o[1] }}</b></span>
          <span v-else>Returned by method:<br><b>{{ o[1] }}</b></span>
        </v-tooltip>
      </template>
    </template>
    <template v-slot:item.sourceDBs="{item}">
      <template v-for="o in item.sourceDBs">
        <v-tooltip bottom :key="item.id+o" v-if="o==='omim'">
          <template v-slot:activator="{attrs,on}">
            <v-chip style="font-size: smaller; color: gray" pill v-on="on" v-bind="attrs">OMIM</v-chip>
          </template>
          <span>The association between this <b>{{nodeName}}</b> and <br> one of the selected <b>disorders</b> was asserted by <b><i>OMIM (Online Mendelian Inheritance in Man)</i></b></span>
        </v-tooltip>
        <v-tooltip bottom :key="item.id+o" v-else-if="o==='disgenet'">
          <template v-slot:activator="{attrs,on}">
            <v-chip style="font-size: smaller; color: gray" pill v-on="on" v-bind="attrs">DisGeNET</v-chip>
          </template>
          <span>The association between this <b>{{nodeName}}</b> and <br> one of the selected <b>disorders</b> was asserted by <b><i>DisGeNET</i></b></span>
        </v-tooltip>
        <v-tooltip bottom :key="item.id+o" v-else>
          <template v-slot:activator="{attrs,on}">
            <v-chip style="font-size: smaller; color: gray" pill v-on="on" v-bind="attrs">{{o}}</v-chip>
          </template>
          <span>TODO SOURCE_DB TOOLTIP</span>
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
    <template v-slot:header.sourceDBs="{header}">
      <v-tooltip bottom>
        <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on">
                          SourceDBs
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
        <SeedRemove v-show="remove" @clearEvent="clear" @intersectionEvent="keepIntersection"
                    :attributes="attributes" @removeEvent="removeNodes"></SeedRemove>
        <SeedFilter v-show="filter" :attributes="attributes" @filterEvent="filterNodes"></SeedFilter>
      </div>
    </template>
  </v-data-table>
</template>

<script>
import SeedDownload from "@/components/app/tables/menus/SeedDownload";
import SeedRemove from "@/components/app/tables/menus/SeedRemove";
import SeedFilter from "@/components/app/tables/menus/SeedFilter";

export default {
  name: "SeedTable",

  props: {
    height: String,
    title: String,
    nodeName: String,
    download: Boolean,
    remove: Boolean,
    filter: Boolean,
  },

  origins: Object,
  data() {
    return {
      nodes: [],
      attributes: undefined,
      headers: [
        {text: 'Name', align: 'start', sortable: true, value: 'displayName'},
        {text: 'SourceDB', align: 'start', sortable: true, value: 'sourceDBs'},
        {text: 'Origin', align: 'start', sortable: true, value: 'origin'},
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

    removeNodes: function(data){
      let all = data.all;
      let attribute = data.attribute;
      let value= data.value;
      this.nodes.filter(n=>(n[attribute]!=null && (n[attribute].indexOf(value)>-1 && (n[attribute].length===1 || all)))).map(n=>n.id).forEach(this.removeNode)
      this.updateAttributes()
    },

    filterNodes: function(data){
      let all = data.all;
      let attribute = data.attribute;
      let value= data.value;
      this.nodes.filter(n=> !(n[attribute]!=null && n[attribute].indexOf(value)>-1 && (n[attribute].length===1 || all))).map(n=>n.id).forEach(this.removeNode)
      this.updateAttributes()
    },

    updateAttributes: function(){
      let attributes = undefined
      this.nodes.forEach(n=>{
        if(n.sourceDBs !=null){
          if(attributes==null)
            attributes = {}
          if(attributes.sourceDBs ==null)
            attributes.sourceDBs=[]
          n.sourceDBs.filter(s=>attributes.sourceDBs.indexOf(s)===-1).forEach(s=>attributes.sourceDBs.push(s))
        }
      })
      this.$set(this,"attributes",attributes)
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
      }).catch(console.error)
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
    addSeeds(entries) {
      let ids = {}
      this.nodes.forEach(seed => ids[seed.id]=seed)
      let count = 0
      entries.data.forEach(e => {
        let exists = e.id in ids
        if (!exists) {
          count++
          this.nodes.push(e)
        } else {
          if (e.sourceDBs != null) {
            let node = ids[e.id]
            if (node.sourceDBs != null)
              node.sourceDBs.concat(entries.sourceDBs)
            else node.sourceDBs = [].concat(e.sourceDBs)
          }
        }
        if (this.origins[e.id] !== undefined) {
          if (this.origins[e.id].indexOf(entries.origin) === -1)
            this.origins[e.id].push(entries.origin)
        } else
          this.origins[e.id] = [entries.origin]
      })
      this.updateAttributes()
      this.$emit("printNotificationEvent", "Added " + entries.data.length + "from " + entries.origin + " (" + count + " new) seeds!", 1)
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
    SeedRemove,
    SeedFilter,
  }
}


</script>

<style scoped>

</style>
