<template>
  <v-autocomplete
    clearable
    :search-input.sync="nodeSuggestions"
    :disabled="suggestionType===undefined || suggestionType<0"
    :loading="suggestions.loading"
    :items="suggestions.data"
    :filter="()=>{return true}"
    item-value="key"
    v-model="suggestionModel"
    label="by suggestions"
    class="mx-4"
    return-object
    auto-select-first
    style="width: 100%"
  >
    <template v-slot:item="{ item }">
      <SuggestionElement :data="item"></SuggestionElement>
    </template>
  </v-autocomplete>
</template>

<script>
import SuggestionElement from "@/components/app/suggestions/SuggestionElement";

export default {
  name: "SuggestionAutocomplete",

  props: {
    targetNodeType: String,
    suggestionType: String,
    index: Number,
  },

  data() {
    return {
      nodeSuggestions: null,
      suggestions: {loading: false, data: []},
      suggestionModel: null,
    }
  },

  watch: {

    nodeSuggestions: function (val) {
      this.getSuggestions(val, false)
    },

    suggestionModel: function (val) {
      if (val) {
        this.$http.post("getConnectedNodes", {
          sourceType: this.suggestionType,
          targetType: this.targetNodeType,
          sugId: val.sid,
          noloop: this.targetNodeType === this.suggestionType
        }).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
          if (this.index!==undefined)
            this.$emit("addToSelectionEvent", {data:data, origin:"SUG:" + val.text + "[" + this.suggestionType + "]", source:this.suggestionType}, this.index)
          else
            this.$emit("addToSelectionEvent", {data:data, origin:"SUG:" + val.text + "[" + this.suggestionType + "]", source:this.suggestionType},)
        }).then(() => {
          this.suggestionModel = undefined
        }).catch(console.error)
      }
    },

  },
  methods: {
    getSuggestions: function (val, timeouted) {
      if (!timeouted) {
        this.sugQuery = val
        if (val == null || val.length < 3) {
          this.suggestions.data = []
          return
        }
        setTimeout(function () {
          this.getSuggestions(val, true)
        }.bind(this), 500)
      } else {
        if (val !== this.sugQuery) {
          return
        }
        let name = this.suggestionType
        if (this.suggestions.chosen !== undefined)
          return
        this.suggestions.loading = true;
        this.suggestions.data = []
        this.$http.post("getSuggestions", {
          name: name,
          query: val,
        }).then(response => {
          if (response.data !== undefined) {
            return response.data
          }
        }).then(data => {
          data.suggestions.sort((e1, e2) => {
            return e2.size - e1.size
          })
          this.$set(this.suggestions, "data", data.suggestions)
        }).catch(err =>
          console.error(err)
        ).finally(() =>
          this.suggestions.loading = false
        )
      }
    },

  },

  components: {
    SuggestionElement,
  }
}
</script>

<style scoped>

</style>
