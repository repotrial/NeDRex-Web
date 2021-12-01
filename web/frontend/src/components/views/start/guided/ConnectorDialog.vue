<template>
  <v-dialog persistent v-model="model" @change="$emit('input',model)" max-width="700" style="z-index: 1001">
    <v-card>
      <v-card-title>Select a connector list</v-card-title>
      <v-card-text>
        <div style="height: 800px; max-height: 800px;">
          <template>
            <div style="display: flex">
              <div style="justify-content: flex-start">
                <v-card-title style="margin-left: -25px;" class="subtitle-1">Add drugs associated to
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
                                      :target-node-type="nodeType" :add-all="true"
                                      @addToSelectionEvent="addToSelection"
                                      style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
            </div>
            <NodeInput text="or provide Seed IDs by" @addToSelectionEvent="addToSelection"
                       :idName="nodeSourceType"
                       :nodeType="nodeType"
                       @printNotificationEvent="printNotification"></NodeInput>
          </template>
          <SeedTable ref="connectorTable" :download="true"
                     :remove="true"
                     :filter="true"
                     @printNotificationEvent="printNotification"
                     height="405px"
                     :title="'Selected Drugs ('+($refs.connectorTable ? $refs.connectorTable.getSeeds().length : 0)+')'"
                     nodeName="drug"
          ></SeedTable>
        </div>
      </v-card-text>

      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="warning" @click="resolve(false)">Cancel</v-btn>
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
  name: "ConnectorDialog",
  components: {
    LabeledSwitch,
    SeedTable,
    SuggestionAutocomplete,
    NodeInput
  },

  props: {
    nodeType: String,
    nodeSourceType: String,
  },

  data() {
    return {
      model: false,
      advancedOptions: false,
      suggestionType: "",
    }
  },


  methods: {
    getList: function () {
      if (this.$refs.connectorTable)
        return this.$refs.connectorTable.getSeeds()
      return undefined;
    },

    clear: function () {
      if (this.$refs.connectorTable)
        this.$refs.connectorTable.clear();
      this.$emit("updateConnectorCount")
    },

    printNotification(message, type) {
      this.$emit("printNotification", message, type)
    },

    show: function () {
      this.model = true;
    },

    getSuggestionSelection: function () {
      if (this.nodeType == null)
        return
      let type = this.nodeType
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
      if (!this.advancedOptions) {
        this.suggestionType = out[disorderIdx].value;
      }
      return out
    },


    resolve: function (state) {
      this.model = false
      this.$emit('input', this.model)
      if (!state) {
        this.clear();
        return
      }
      this.$emit("updateConnectorCount")
    },

    addToSelection: function (data) {
      this.$refs.connectorTable.addSeeds(data)
    }
  }
}
</script>

<style scoped>

</style>
