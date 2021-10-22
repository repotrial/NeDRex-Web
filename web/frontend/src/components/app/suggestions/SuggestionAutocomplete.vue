<template>
  <v-autocomplete
    clearable
    :search-input.sync="nodeSuggestions"
    :disabled="suggestionType===undefined || suggestionType<0"
    :loading="suggestions.loading"
    :items="suggestions.data"
    :filter="()=>{return true}"
    item-value="id"
    v-model="suggestionModel"
    label="by suggestions"
    class="mx-4"
    return-object
    auto-select-first
    style="width: 100%"
  >
    <template v-slot:item="{ item }">
      <SuggestionElement :data="item" :source-type="suggestionType" :target-type="targetNodeType"></SuggestionElement>
    </template>
    <template v-slot:append-outer v-if="sortSwitch">
      <v-tooltip top>
        <template v-slot:activator="{on, attrs}">
          <v-icon v-on="on" v-bind="attrs" @click="switchSorting()" style="width:25px">
            {{ sortings[sortingModel].icon }}
          </v-icon>
        </template>
        <span>{{ sortings[sortingModel].tooltip }}</span>
      </v-tooltip>
    </template>
    <template v-slot:append-item>
      <v-list-item v-show="suggestions.data != null && suggestions.data.length >0">
        <v-list-item-content>
          <div style="width: 100%; color: dimgray; display: flex; justify-content: center;">
            <i style="max-width: 400px;">
              There might be more matches but they are
              not associated to any entries of the current target type!</i>
          </div>
        </v-list-item-content>
      </v-list-item>
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
    emitDrugs: {
      type: Boolean,
      default: false,
    },
    emitDisorders: {
      type: Boolean,
      default: false,
    },
    sortSwitch: {
      type: Boolean,
      default: true,
    }
  },

  data() {
    return {
      nodeSuggestions: null,
      suggestions: {loading: false, data: []},
      suggestionModel: null,
      disorderPopup: false,
      sortings: [{
        icon: "fas fa-sort-amount-down",
        tooltip: "High to low source entry count!",
        value: "source-down"
      }, {
        icon: "fas fa-sort-amount-up",
        tooltip: "Low to high source entry count!",
        value: "source-up"
      }, {
        icon: "fas fa-sort-alpha-down",
        tooltip: "Lexicographic sorting!",
        value: "alpha-down"
      },
        {icon: "fas fa-sort-alpha-up", tooltip: "Reversed lexicographic sorting", value: "alpha-up"},
        {
          icon: "fas fa-sort-numeric-down",
          tooltip: "High to low target entry count!",
          value: "target-down"
        },
        {
          icon: "fas fa-sort-numeric-up",
          tooltip: "Low to high target entry count!",
          value: "target-up"
        },],
      sortingModel: 4
    }
  },

  watch: {

    nodeSuggestions: function (val) {
      this.getSuggestions(val, false)
    },

    suggestionModel: function (val) {
      if (val) {
        if (this.suggestionType === "disorder" && val.type === "UMBRELLA_DISORDER") {
          this.$emit("subtypeSelection", this.suggestionModel)
        } else {
          this.loadVals()
        }
      }
    },
  },
  methods: {
    switchSorting: function () {
      this.sortingModel = (this.sortingModel + 1) % this.sortings.length
      this.sortData(this.suggestions.data, this.sortings[this.sortingModel].value)
    },

    loadDisorders: function (ids) {
      this.$http.post("getConnectedNodes", {
        sourceType: this.suggestionType,
        targetType: this.targetNodeType,
        ids: ids,
        noloop: this.targetNodeType === this.suggestionType,
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.loadByIds(data)
      }).catch(console.error)
    },

    loadVals: function () {
      this.$http.post("getConnectedNodes", {
        sourceType: this.suggestionType,
        targetType: this.targetNodeType,
        sugId: this.suggestionModel.sid,
        noloop: this.targetNodeType === this.suggestionType,
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.loadByIds(data)
      }).catch(console.error)
    },
    loadByIds: function (data) {
      let payload = {
        data: data,
        origin: "SUG:" + this.suggestionModel.text + "[" + this.suggestionType + "]",
        source: this.suggestionType
      }
      if (this.index !== undefined)
        this.$emit("addToSelectionEvent", payload, this.index)
      else
        this.$emit("addToSelectionEvent", payload,)
      this.emitData({...this.suggestionModel})
      this.suggestionModel = undefined
    },
    emitData: function (val) {
      if (this.emitDrugs && this.suggestionType === "disorder") {
        this.$http.post("getConnectedNodes", {
          sourceType: this.suggestionType,
          targetType: "drug",
          sugId: val.sid,
          noloop: false
        }).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
          this.$emit("drugsEvent", {
            origin: "SUG:" + val.text + "[" + this.suggestionType + "]",
            data: data,
            source: this.suggestionType
          })
        }).catch(console.error)
      }
      if (this.emitDisorders && this.suggestionType === "disorder") {
        if (val.sid.indexOf("_") > -1) {
          this.$http.get("getSuggestionEntry?nodeType=" + this.suggestionType + "&sid=" + val.sid).then(response => {
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

    sortData: function (data, method) {
      switch (method) {
        case "alpha-down": {
          data.sort((e1, e2) => {
            return e1.text.localeCompare(e2.text)
          })
          break;
        }
        case "alpha-up": {
          data.sort((e1, e2) => {
            return e2.text.localeCompare(e1.text)
          })
          break;
        }
        case "source-down": {
          data.sort((e1, e2) => {
            return e2.size - e1.size
          })
          break;
        }
        case "source-up": {
          data.sort((e1, e2) => {
            return e1.size - e2.size
          })
          break;
        }
        case "target-down": {
          data.sort((e1, e2) => {
            return e2.targetCount - e1.targetCount
          })
          break;
        }
        case "target-up": {
          data.sort((e1, e2) => {
            return e1.targetCount - e2.targetCount
          })
        }
      }
      this.suggestions.data = data
    },
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
          typeCount: this.targetNodeType,
        }).then(response => {
          if (response.data !== undefined) {
            return response.data
          }
        }).then(data => {
          data.suggestions.forEach(s => s.id = s.sid + ":" + s.type + ":" + s.key)
          this.sortData(data.suggestions, this.sortings[this.sortingModel].value)
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
