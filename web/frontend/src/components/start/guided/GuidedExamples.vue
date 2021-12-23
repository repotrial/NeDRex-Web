<template>
  <v-row style="margin-top:0">
    <v-col>
      <v-menu bottom offset-y :disabled="disabled">
        <template v-slot:activator="{on,attrs}">
          <v-btn small outlined right v-bind="attrs" v-on="on" :disabled="disabled">
            <v-icon left color="primary">
              fas fa-graduation-cap
            </v-icon>
            Examples
          </v-btn>
        </template>
        <v-list style="font-size: smaller; color: gray" dense>
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
    </v-col>
  </v-row>
</template>

<script>
export default {
  name: "GuidedExamples",
  props: {
    disabled: {
      type: Boolean,
      default: false,
    }
  },

  data() {
    return {
      examples: [
        {
          id: 0,
          text: "Alzheimer Disorder associated with pathway",
          connector: "protein",
          sourceQuery: "Alzheimer disease",
          sourceType: "umbrella_disorder",
          source: "disorder",
          target: "pathway",
          compress: true,
          edge: "Alzheimer influenced pathway"
        },
        {
          id: 1,
          text: "Aspirin targets disorders trough protein",
          connector: "protein",
          sourceQuery: "Acetylsalicylic acid",
          sourceType: "name",
          source: "drug",
          target: "disorder",
          compress: false,
        },
        {
          id: 2,
          text: "Antidepressive agents target depressive disorder through genes",
          connector: "gene",
          sourceQuery: "Antidepressive Agents",
          sourceType: "category",
          source: "drug",
          target: "disorder",
          targetType: "umbrella_disorder",
          targetQuery: "depressive disorder",
          compress: true,
          edge: "Indirect antidepressant effect"
        }
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
        this.loadRequest({
          suggestionType: example.source,
          targetNodeType: example.source,
          sid: entry.sid,
          sourceQuery: example.sourceQuery
        }).then(data => {
          data.origin = "Example " + (nr + 1) + ": (" + example.sourceQuery + ")"
          this.$emit("addNodesEvent", data, 0)
        })
      })

      if (example.targetQuery) {
        this.$http.post("getSuggestions", {
          name: example.target,
          query: example.targetQuery
        }).then(response => {
          if (response.data !== undefined) {
            return response.data
          }
        }).then(data => {
          let entry = data.suggestions.filter(d => d.text === example.targetQuery && d.type.toLowerCase() === example.targetType)[0]
          this.loadRequest({
            suggestionType: example.target,
            targetNodeType: example.target,
            sid: entry.sid,
            sourceQuery: example.targetQuery
          }).then(data => {
            data.origin = "Example " + (nr + 1) + ": (" + example.targetQuery + ")"
            this.$emit("addNodesEvent", data, 1)
          })
        })
      }
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
        this.$emit("suggestionEvent", val)
        return {
          data: data,
          source: val.suggestionType
        }
      }).catch(console.error)
    },
  }
}
</script>

<style scoped>

</style>
