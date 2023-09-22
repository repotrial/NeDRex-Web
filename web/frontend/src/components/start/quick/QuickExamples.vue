<template>
  <div>
      <v-menu bottom offset-y :disabled="disabled">
        <template v-slot:activator="{on,attrs}">
          <v-btn small outlined right v-bind="attrs" v-on="on" :disabled="disabled">
            <v-icon small left color="primary">
              fas fa-graduation-cap
            </v-icon>
            <v-divider vertical style="border-color: black; margin-right: 5px;"></v-divider>
            Examples
            <v-icon right>fas fa-caret-down</v-icon>
          </v-btn>
        </template>
        <v-list style="font-size: smaller; color: rgb(128,128,128)" dense>
          <v-list-item v-for="example in examples" @click="loadExample(example.id)" :key="example.id">
            <v-icon left size="1em">fas fa-plus</v-icon>
            {{ example.text }}
          </v-list-item>
        </v-list>
      </v-menu>
      <v-tooltip right>
        <template v-slot:activator="{attrs, on}">
          <v-icon right color="gray" size="10pt" v-on="on" v-bind="attrs">far fa-question-circle</v-icon>
        </template>
        <div>Select an example and this and all further steps are automatically defined.<br> You can still adjust
          anything you want or just <b>continue</b> straight to the result page!
        </div>
      </v-tooltip>
  </div>
</template>

<script>
export default {
  name: "QuickExamples",
  props: {
    disabled: {
      type: Boolean,
      default: false,
    },
    seedType: String,
  },

  data() {
    return {
      examples: [
        {
          id: 0,
          text: "Cystic Fibrosis associated seeds",
          sourceQuery: "cystic fibrosis",
          sourceType: "name",
          source: "disorder",
          mi: {
            algorithm: "diamond",
            params: {
              nModel: 10,
              alphaModel: 1,
              pModel: -3
            }
          },
          dp: {
            algorithm: "trustrank",
            params: {
              onlyDirect: false,
              onlyApproved: false,
            }
          }
        },
        {
          id: 1,
          text: "Alzheimer disorder associated seeds",
          sourceQuery: "Alzheimer disease",
          sourceType: "umbrella_disorder",
          source: "disorder",
          mi: {
            algorithm: "robust",
            params: {}
          },
          dp: {
            algorithm: "trustrank",
            params: {
              onlyDirect: false,
              onlyApproved: false,
            }
          }
        },
        {
          id: 2,
          text: "Non-small cell lung carcinoma associated seeds",
          sourceQuery: "non-small cell lung carcinoma",
          sourceType: "name",
          source: "disorder",
          mi: {
            algorithm: "diamond",
            params: {
              nModel: 60,
              pModel: -3
            }
          },
          dp: {
            algorithm: "centrality",
            params: {
              topX: 75,
              onlyApproved: false,
              onlyDirect: false,
              filterElements: true,
            }
          }
        },
      ]
    }
  },


  methods: {
    loadExample: async function (nr) {
      let example = this.examples[nr]
      this.$emit("exampleEvent", example)
      this.$http.post("getSuggestions", {
        name: example.source,
        query: example.sourceQuery
      }).then(response => {
        if (response.data !== undefined) {
          return response.data
        }
      }).then(data => {
        let entry = data.suggestions.filter(d => d.text === example.sourceQuery && d.type.toLowerCase() === example.sourceType)[0]
        let vals = {
          suggestionType: example.source,
          targetNodeType: this.seedType,
          sid: entry.sid,
          sourceQuery: example.sourceQuery
        }
        this.loadRequest(vals).then(data => {
          data.origin = "Example " + (nr + 1) + ": (" + example.sourceQuery + ")"
          this.$emit("addNodesEvent", data, 1)
          vals.origin = data.origin
          this.emitData(vals)
        })

      })
    },

    loadRequest: function (val) {
      return this.$http.post("getConnectedNodes", {
        sourceType: val.suggestionType,
        targetType: val.targetNodeType,
        sugId: val.sid,
        noloop: val.suggestionType === val.targetNodeType
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.$emit("suggestionEvent", {text: val.sourceQuery+ " (Example)"})
        return {
          data: data,
          source: val.suggestionType
        }
      }).catch(console.error)
    },

    emitData: function (val) {
      if (val.suggestionType === "disorder") {
        let payload = {
          sourceType: val.suggestionType,
          targetType: "drug",
          sugId: val.sid,
          noloop: false
        }
        payload.sugId = val.sid
        this.$http.post("getConnectedNodes", payload).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
          this.$emit("drugsEvent", {
            origin: val.origin,
            data: data,
            source: val.suggestionType
          })
        }).catch(console.error)
      }
      if (val.suggestionType === "disorder") {
        if (val.sid.indexOf("_") > -1) {
          this.$http.get("getSuggestionEntry?nodeType=" + val.suggestionType + "&sid=" + val.sid).then(response => {
            if (response.data !== undefined)
              return response.data
          }).then(data => {
            this.$emit("disorderEvent", data)
          }).catch(console.error)
        } else {
          this.$emit("disorderEvent", [parseInt(val.sid)])
        }
      }
    },
  }
}
</script>

<style scoped>

</style>
