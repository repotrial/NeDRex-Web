<template>
  <div>
    <v-card-subtitle>{{ text }}
      <v-tooltip top>
        <template v-slot:activator="{on,attrs}">
          <v-chip v-on="on" v-bind="attrs" outlined @click="seedInput=true">text input
            <v-icon right>fas fa-caret-right</v-icon>
          </v-chip>
        </template>
        <span>Open a text-box to paste a list of seed ids.</span>
      </v-tooltip>
      or file upload
    </v-card-subtitle>
    <div style="justify-content: center; display: flex; width: 100%">
      <v-file-input
        ref="upload"
        v-on:change="onFileSelected"
        @focus="$refs.upload.blur()"
        show-size
        outlined
        v-model="fileInputModel"
        prepend-icon=""
        dense
        solo
        style="width:300px; max-width: 300px;"
      >
        <template v-slot:label>
          <v-icon small>fas fa-file-upload</v-icon>
          Upload {{ idName }} ID list
        </template>
        <template v-slot:append-outer>
          <v-tooltip left>
            <template v-slot:activator="{on,attrs}">
              <a :href="exampleFile"
                 target="_blank" style="text-decoration: none">
                <v-icon
                  v-bind="attrs"
                  v-on="on"
                  left
                >
                  fas fa-download
                </v-icon>
              </a>
            </template>
            <div style="width: 400px">The ID list has to be newline ("\n") separated but can contain multiple columns,
              separated by tab ("\t") or comma (",") as long as the ID is in the first column. Headers or lines without
              ids should be marked as comment with the prefix "#"!
              <br>
              <i>Also you can click here to look at an example file!</i>
            </div>
          </v-tooltip>
        </template>
      </v-file-input>
    </div>
    <v-dialog v-model="seedInput"
              persistent
              style="z-index: 1001"
              max-width="500">
      <v-card>
        <v-card-title>Paste Nodes</v-card-title>
        <v-card-subtitle style="margin-top:0">Paste a list of <i><b>{{ nodeType }}</b></i> nodes by their
          <i><b>{{ idName }}</b></i> id to be added to the main selection.
        </v-card-subtitle>
        <v-card-text>
          <div
            style="justify-self: center; display: flex; width: 90%; margin-left: auto; margin-right: auto; margin-top: 15px">
            <v-textarea outlined height="25vh" full-width label="Seed Input" no-resize v-model="data"></v-textarea>
          </div>
        </v-card-text>
        <v-card-subtitle>Select the separator used in the data or stick to the automatically identified one.
        </v-card-subtitle>
        <v-card-actions style="width: 90%; justify-self: center; display: flex; margin-left: auto; margin-right: auto">
          <v-checkbox v-model="sepModel[',']" @click="setSep(',')" label="','" style="margin-right: 25px"></v-checkbox>
          <v-checkbox v-model="sepModel[';']" @click="setSep(';')" label="';'" style="margin-right: 25px"></v-checkbox>
          <v-checkbox v-model="sepModel['\t']" @click="setSep('\t')" label="'\t'"
                      style="margin-right: 25px"></v-checkbox>
          <v-checkbox v-model="sepModel['\n']" @click="setSep('\n')" label="'\n'"
                      style="margin-right: 25px"></v-checkbox>
          <v-text-field v-model="customSep" label="custom"></v-text-field>
        </v-card-actions>
        <v-divider></v-divider>
        <v-card-actions>
          <v-btn
            text
            @click="clear()"
          >
            Cancel
          </v-btn>
          <v-btn
            v-if="separator!=null && list.length>0"
            color="green darken-1"
            text
            @click="mapNodes()"
          >
            Add {{ list.length }} Nodes
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
export default {
  name: "NodeInput",
  props: {
    text: String,
    idName: String,
    nodeType: String,
  },

  data() {
    return {
      seedInput: false,
      fileInputModel: undefined,
      sepModel: {
        ',': false,
        ';': false,
        '\t': false,
        '\n': false,
      },
      separator: undefined,
      data: "",
      list: [],
      customSep: undefined,
      exampleFile: undefined,
    }
  },

  watch: {
    nodeType: function(value){
      this.updateExampleFile(value)
    },

    data: function (value) {
      if (value.length === 0) {
        this.list = []
        return;
      }
      if (this.customSep == null)
        this.setSep(this.$utils.checkSeparator(value, Object.keys(this.sepModel)), true)
      this.prepareList()
    },

    customSep: function (value) {
      if (value == null)
        return
      if (value.length === 0) {
        this.customSep = undefined;
        this.setSep(this.$utils.checkSeparator(this.data, Object.keys(this.sepModel)), true)
      } else {
        this.setSep(value, false)
        this.separator = value
      }
      this.prepareList()
    },
    separator: function (value) {
      if (value != null) {
        this.prepareList()
      }
    },
  },
  created() {
    if(this.nodeType!=null)
      this.updateExampleFile(this.nodeType);
  },

  methods: {
    clear: function () {
      this.seedInput = false;
      this.data = ""
      this.separator = undefined
      this.customSep = undefined;
      this.list = []
      Object.keys(this.sepModel).forEach(s => {
        this.$set(this.sepModel, s, false)
      })
    },

    onFileSelected: function (file) {
      if (file == null) {
        return
      }
      this.$utils.readFile(file).then(content => {
        this.$http.post("mapFileListToItems", {
          type: this.nodeType,
          file: content
        }).then(response => {
          if (response.data)
            return response.data
        }).then(data => {
          this.$emit("addToSelectionEvent", {data: data, origin: "FILE:" + file.name})
        }).then(() => {
          this.fileInputModel = undefined
        }).then(() => {
          this.$nextTick(() => {
            this.$refs.upload.blur()
          })
        }).catch(console.error)
      }).catch(console.error)
    },

    updateExampleFile: function(type){
      if(type!=null){
        return this.$http.getExampleInputFile(type).then(data=>{
          this.exampleFile=data;
        }).catch(console.error)
      }else{
        this.exampleFile=undefined;
      }
    },

    setSep: function (sep, state) {
      if (state)
        this.$set(this.sepModel, sep, true)
      Object.keys(this.sepModel).forEach(s => {
        if (sep !== s)
          this.$set(this.sepModel, s, false)
      })
      this.separator = this.sepModel[sep] ? sep : undefined;
    },
    prepareList: function () {
      this.list = this.data.split(this.separator).map(e => e.trim()).filter(e => e.length > 0)
    },
    mapNodes: function () {
      this.$http.post("mapListToItems", {
        type: this.nodeType,
        list: this.list
      }).then(response => {
        if (response.data)
          return response.data
      }).then(data => {
        this.$emit("addToSelectionEvent", {data: data, origin: "PASTED"})
      }).then(() => {
        this.clear()
      }).catch(error => {
        console.error(error)
        this.$emit("printNotificationEvent", "Something went wrong when converting your seeds!", 2)
      })
    }
  }
}
</script>

<style lang="scss">


.v-input__slot {
  cursor: pointer !important;
}

</style>
