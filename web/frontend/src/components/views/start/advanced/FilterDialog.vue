<template>
  <v-dialog v-model="model" max-width="800px" style="z-index: 1001" persistent>
    <v-card v-if="nodeId!=null">
      <v-card-title>Filter the {{ nodeList[nodeId].text }} nodes</v-card-title>
      <v-card-subtitle>
        <LabeledSwitch v-if="model" label-off="Node selection" label-on="Text filter"
                       v-model="filterType[nodeList[nodeId].value]" style="margin-bottom: -25px">
          <template v-slot:tooltip>
            <div style="width: 400px">
              The <b>Node selection</b> lets you select the specific nodes you want to be used in the network.
              The <b>Text filter</b> allows to generally apply a filter over most node attributes. Both selections <b>cannot</b>
              be combined.
            </div>
          </template>
        </LabeledSwitch>
      </v-card-subtitle>
      <v-divider></v-divider>
      <v-card-text style="max-height: 700px; overflow-y: auto">
        <v-card-title style="margin-left: -25px; margin-bottom: -25px" class="subtitle-1;"
                      v-show="['gene','drug'].indexOf(nodeList[nodeId].value)>-1">General options
        </v-card-title>
        <div>
          <LabeledSwitch v-if="nodeList[nodeId].value ==='gene'" label-on="Coding Genes Only" label-off="All Genes"
                         v-model="options.codingGenesOnly">
            <template v-slot:tooltip>
              <div>If activated, applies filter on genes, such that only coding genes are used.</div>
            </template>
          </LabeledSwitch>
        </div>
        <div>
          <LabeledSwitch v-if="nodeList[nodeId].value === 'drug'" label-on="Approved Drugs Only" label-off="All Drugs"
                         v-model="options.approvedDrugsOnly">
            <template v-slot:tooltip>
              <div>If activated, applies filter on drugs, such that only approved drugs are used.</div>
            </template>
          </LabeledSwitch>
        </div>
        <div>
          <LabeledSwitch v-if="nodeList[nodeId].value === 'drug'" label-on="Filter Element Drugs" label-off="No Filter"
                         v-model="options.filterElementDrugs">
            <template v-slot:tooltip>
              <div>If activated, applies filter on drugs, such that only complex drugs are used.
                <br>Element drugs are:
                <br><b>chemical element:</b> <i>Gold, Zinc, ...</i>
                <br><b>metals and metal cations:</b> <i>Cupric Chloride, Aluminium acetoactetate, ...</i>
                <br><b>minerals and mineral supplements:</b> <i>Calcium silicate, Sodium chloride, ...</i>
              </div>
            </template>
          </LabeledSwitch>
        </div>
        <div style="height: 800px; max-height: 800px;" v-show="!filterType[nodeList[nodeId].value]">
          <template>
            <div style="display: flex">
              <div style="justify-content: flex-start">
                <v-card-title style="margin-left: -25px;" class="subtitle-1">Add {{ nodeList[nodeId].value }} nodes
                  associated to
                </v-card-title>
              </div>
              <div style="justify-content: flex-end; margin-left: auto">
                <LabeledSwitch v-model="advancedOptions"
                               @click="suggestionType = advancedOptions ? suggestionType : 'disorder'"
                               label-off="Limited" label-on="Full">
                  <template v-slot:tooltip>
                    <div style="width: 300px"><b>Limited Mode:</b><br>The options are limited to the most
                      interesting and generally used ones to not overcomplicate the user interface <br>
                      <b>Full Mode:</b><br> The full mode provides a wider list of options to select from for
                      more
                      specific queries.
                    </div>
                  </template>

                </LabeledSwitch>
              </div>
            </div>

            <div style="display: flex">
              <v-tooltip top>
                <template v-slot:activator="{on, attrs}">
                  <div v-on="on" v-bind="attrs" style="width: 35%;justify-self: flex-start">
                    <v-select :items="getSuggestionSelection()" v-model="suggestionType"
                              placeholder="connected to" style="width: 100%"
                              :disabled="!advancedOptions"></v-select>
                  </div>
                </template>
                <div v-if="advancedOptions" style="width: 300px"><b>Full Mode:</b><br>A node type with
                  direct association to drug nodes can freely be selected and are to add additional seeds.
                </div>
                <div v-else style="width: 300px"><b>Limited Mode:</b><br>Disorders can be used to add known drug
                  associations as seed nodes. For the use of all available node types for the selection
                  through association the 'Limited' switch has to be toggled.
                </div>
              </v-tooltip>
              <SuggestionAutocomplete :suggestion-type="suggestionType"
                                      :target-node-type="nodeList[nodeId].value" :add-all="true"
                                      @addToSelectionEvent="addToSelection"
                                      style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
            </div>
            <NodeInput text="or provide Seed IDs by" @addToSelectionEvent="addToSelection"
                       :idName="nodeIdTypeList[nodeId]"
                       :nodeType="nodeList[nodeId].value"
                       @printNotificationEvent="printNotification"></NodeInput>
          </template>
          <SeedTable v-for="node in nodeList" ref="table" :download="true" v-show="nodeId===node.id"
                     :remove="true"
                     :filter="true"
                     @printNotificationEvent="printNotification"
                     height="405px"
                     :title="'Selected '+nodeList[nodeId].text+' ('+($refs.table && $refs.table[nodeId] ? $refs.table[nodeId].getSeeds().length : 0)+')'"
                     :nodeName="nodeList[nodeId].value" :key="node.value+'_table'"
          ></SeedTable>
        </div>
        <div style="height: 800px; max-height: 800px;" v-show="filterType[nodeList[nodeId].value]">
          <v-card-title style="margin-left: -25px;" class="subtitle-1">Filters</v-card-title>
          <v-simple-table fixed-header ref="filterTable">
            <template v-slot:default>
              <thead>
              <tr>
                <th class="text-center">Type</th>
                <th class="text-center">Filter</th>
                <th class="text-center">Operation</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(item,index) in filters[nodeList[nodeId].value]" :key="item.type+item.expression">
                <td>{{ item.type }}</td>
                <td>{{ item.expression }}</td>
                <td>
                  <v-chip outlined v-on:click="removeFilter(index)">
                    <v-icon dense>fas fa-trash</v-icon>
                  </v-chip>
                </td>
              </tr>
              <tr>
                <td>
                  <v-select
                    v-model="filterTypeModel"
                    :items="filterTypes"
                    label="type"
                  ></v-select>
                </td>
                <td>
                  <v-text-field
                    v-model="filterModel"
                    :label="filterLabel"
                    placeholder="Pattern"
                  ></v-text-field>
                </td>
                <td>
                  <v-chip outlined v-on:click="saveFilter"
                          :disabled="filterModel ===undefined|| filterModel.length ===0 ||filterTypeModel ===undefined">
                    <v-icon dense>fas fa-plus</v-icon>
                  </v-chip>
                </td>
              </tr>
              </tbody>
            </template>
          </v-simple-table>
        </div>
      </v-card-text>

      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="error" @click="resolve(false)">Cancel</v-btn>
        <v-btn color="green darken-1" style="color: white" @click="resolve(true)">Accept</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import LabeledSwitch from "@/components/app/input/LabeledSwitch";
import SeedTable from "@/components/app/tables/SeedTable";
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";
import NodeInput from "@/components/app/input/NodeInput";

export default {
  name: "FilterDialog",
  components: {
    LabeledSwitch,
    SeedTable,
    SuggestionAutocomplete,
    NodeInput
  },
  props: {
    filterType: Object,
    nodeId: Number,
  },
  data() {
    return {
      nodeList: [],
      nodeIdTypeList: [],
      model: false,
      advancedOptions: false,
      suggestionType: "",

      filterTypes: ['startsWith', 'contain', 'match'],
      filterModel: "",
      filterEntity: "",
      filterLabel: "",
      filterTypeModel: [],
      filters: {},
      options: {
        codingGenesOnly: false,
        approvedDrugsOnly: false,
        filterElementDrugs: true
      }
    }
  },

  created() {
    this.$global.metagraph.nodes.forEach((n, index) => {
      this.nodeList.push({id: index, value: n.group, text: n.label})
      this.nodeIdTypeList.push(this.$global.metagraph.data[n.label])
    })
  },

  methods: {
    show: function () {
      this.model = true;
    },

    clear: function (all) {
      if (all) {
        this.filters = {}
        this.options = {
          codingGenesOnly: false,
          approvedDrugsOnly: false,
          filterElementDrugs: true
        }
      } else {
        if (this.$refs.table && this.$refs.table[this.nodeId]) {
          this.$refs.table[this.nodeId].clear();
          if (this.filters[this.nodeList[this.nodeId].value])
            for (let i = this.filters[this.nodeList[this.nodeId].value].length - 1; i >= 0; i--) {
              this.removeFilter(i)
            }
        }
        this.resetOptions(this.nodeList[this.nodeId].value)
        this.$emit("updateNodeCount", {node: this.nodeList[this.nodeId].value, count: 0})
      }
    },

    addToSelection: function (data) {
      this.$refs.table[this.nodeId].addSeeds(data)
    },

    printNotification(message, type) {
      this.$emit("printNotification", message, type)
    },
    removeFilter: function (idx) {
      this.filters[this.nodeList[this.nodeId].value].splice(idx, 1)
      this.$refs.filterTable.$forceUpdate()
    },
    saveFilter: function () {
      let filterEntity = this.nodeList[this.nodeId].value;
      let data = {type: this.filterTypeModel, expression: this.filterModel};

      if (this.filters[filterEntity] === undefined)
        this.filters[filterEntity] = []

      if (this.filters[filterEntity].filter(f => (f.type === this.filterTypeModel && f.expression === this.filterModel)).length === 0) {
        this.filters[filterEntity].push(data)
      }
      this.filterTypeModel = ""
      this.filterModel = ""
    },

    getSuggestionSelection: function () {
      if (this.nodeId == null)
        return
      let type = this.nodeList[this.nodeId].value
      let nodeId = this.$global.metagraph.nodes.filter(n => n.group === type)[0].id
      let disorderIdx = -1
      let out = this.$global.metagraph.edges.filter(e => e.from !== e.to && e.from === nodeId || e.to === nodeId).map(e => e.to === nodeId ? e.from : e.to).map(nid => {
        let node = this.$global.metagraph.nodes.filter(n => n.id === nid)[0]
        if (node.label === "Disorder" && disorderIdx < 0) {
          disorderIdx = -(disorderIdx + 1)
        } else {
          if (disorderIdx < 0)
            disorderIdx--;
        }
        return {value: node.group, text: node.label}
      })
      out.push({value: type, text: type.substring(0, 1).toUpperCase() + type.substring(1)})
      if (disorderIdx < 0) {
        this.suggestionType = out[0].value
      } else (!this.advancedOptions)
      {
        this.suggestionType = out[disorderIdx].value;
      }
      return out
    },

    getFilter: function (node) {
      if (this.filterType[node])
        return {filters: this.filters[node]}
      let id = 0
      for (let i = 0; i < this.nodeList.length; i++)
        if (this.nodeList[i].value === node)
          id = i
      if (this.$refs.table && this.$refs.table[id] && this.$refs.table[id].getSeeds().length > 0)
        return {ids: this.$refs.table[id].getSeeds().map(n => n.id)}
      return undefined
    },

    resetOptions: function (value) {
      if (value === 'gene') {
        this.options.codingGenesOnly = false
      }
      if (value === 'drug') {
        this.options.approvedDrugsOnly = false
        this.options.filterElementDrugs = true
      }
    },
    getOptions: function () {
      return this.options;
    },
    resolve: function (state) {
      this.model = false
      if (!state) {
        this.clear();
        return
      }
      let node = this.nodeList[this.nodeId].value
      this.$emit("updateNodeCount", {
        node: node,
        count: this.filterType[node] || !this.$refs.table[this.nodeId] ? 0 : this.$refs.table[this.nodeId].getSeeds().length
      })
    }
  }
}
</script>

<style scoped>

</style>
