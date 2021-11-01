<template>
  <v-card
    class="mb-4"
    min-height="80vh"
  >
    <v-card-subtitle class="headline">{{ step }}. Validation</v-card-subtitle>
    <v-card-subtitle style="margin-top: -25px">Validate the results.
    </v-card-subtitle>
    <v-divider style="margin: 15px;"></v-divider>
    <v-container style="height: 80%; max-width: 100%">
      <v-row>
        <v-col cols="6">
          <div style="height: 800px; max-height: 800px;">
            <template>
              <div style="display: flex">
                <div style="justify-content: flex-start">
                  <v-card-title style="margin-left: -25px;" class="subtitle-1">Add drugs associated to
                  </v-card-title>
                </div>
                <v-tooltip top>
                  <template v-slot:activator="{on,attrs}">
                    <div v-on="on" v-bind="attrs" style="justify-content: flex-end; margin-left: auto">
                      <LabeledSwitch v-model="advancedOptions"
                                     @click="suggestionType = advancedOptions ? suggestionType : 'disorder'"
                                     label-off="Limited" label-on="Full" v-on="on"
                                     v-bind="attrs"></LabeledSwitch>
                    </div>
                  </template>
                  <div style="width: 300px"><b>Limited Mode:</b><br>The options are limited to the most
                    interesting and generally used ones to not overcomplicate the user interface <br>
                    <b>Full Mode:</b><br> The full mode provides a wider list of options to select from for more
                    specific queries.
                  </div>
                </v-tooltip>
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
                                        target-node-type="drug" :add-all="true"
                                        @addToSelectionEvent="addToSelection"
                                        style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
              </div>
              <NodeInput text="or provide Seed IDs by" @addToSelectionEvent="addToSelection"
                         idName="drugbank"
                         nodeType="drug"
                         @printNotificationEvent="printNotification"></NodeInput>
            </template>
            <SeedTable ref="drugTable" :download="true"
                       :remove="true"
                       :filter="true"
                       @printNotificationEvent="printNotification"
                       height="405px"
                       :title="'Selected Drugs ('+($refs.drugTable ? $refs.drugTable.getSeeds().length : 0)+')'"
                       nodeName="drug"
            ></SeedTable>
          </div>
        </v-col>

        <v-divider vertical></v-divider>
        <div style="justify-self: flex-end; margin-left: auto; width: 48%;"
             v-if="$refs.drugTable && $refs.drugTable.getSeeds().length>0">
          <v-card-title class="subtitle-1">Adjust validation parameters</v-card-title>
          <v-switch v-model="models.onlyApproved" label="Approved Drugs only"></v-switch>
          <v-slider
            hide-details
            class="align-center"
            v-model="models.perms"
            step="1"
            min="1000"
            max="10000"
          >
            <template v-slot:prepend>
              <v-text-field
                v-model="models.perms"
                class="mt-0 pt-0"
                type="number"
                style="width: 60px"
                label="permutations"
              ></v-text-field>
            </template>
            <template v-slot:append>
              <v-tooltip left>
                <template v-slot:activator="{ on, attrs }">
                  <v-icon
                    v-bind="attrs"
                    v-on="on"
                    left> far fa-question-circle
                  </v-icon>
                </template>
                <span>A value between 1000 and 10000 which determines the number of permutations created for validation.</span>
              </v-tooltip>
            </template>
          </v-slider>

          <v-btn @click="validate()" color="primary">Run Validation
            <v-icon right>fas fa-angle-double-right</v-icon>
          </v-btn>
          <div v-if="module!=null">
            <v-card-subtitle v-if="moduleValidationStatus !=null" class="title">Module Validation Result:
            </v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">
              <v-chip v-if="styleMap[moduleValidationStatus]!=null" style="margin-left: 10px"
                      :color="styleMap[moduleValidationStatus][0]"><a style="color: white;text-decoration: none"
                                                                      target="_blank"
                                                                      :href="'https://api.nedrex.net/validation/status?uid='+moduleValidationUID">
                {{ moduleValidationStatus }}
                <v-icon right size="10pt">{{ styleMap[moduleValidationStatus][1] }}</v-icon>
              </a>
              </v-chip>
            </v-card-subtitle>
            <div style="display: flex; justify-content: center; width: 100%">
              <v-simple-table v-if="moduleValidationError.length===0" style="max-width: 400px;">
                <template v-slot:default>
                  <thead>
                  <tr>
                    <th class="text-center">Measure</th>
                    <th class="text-center">Value</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(score,id) in moduleValidationScore" v-if="score!=null">
                    <td>{{ id }}</td>
                    <td>{{ score }}
                    </td>
                  </tr>
                  </tbody>
                </template>
              </v-simple-table>
            <div v-else>
              Error:
              <div style="color: dimgray">{{ moduleValidationError }}</div>
            </div>
            </div>
          </div>
          <div v-if="ranking!=null">
            <v-card-subtitle v-if="rankingValidationStatus !=null" class="title">Ranking Validation Result:
            </v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">
              <v-chip v-if="styleMap[rankingValidationStatus]!=null" style="color: white; margin-left: 10px"
                      :color="styleMap[rankingValidationStatus][0]"><a style="color: white;text-decoration: none"
                                                                       target="_blank"
                                                                       :href="'https://api.nedrex.net/validation/status?uid='+rankingValidationUID">
                {{ rankingValidationStatus }}
                <v-icon right size="10pt">{{ styleMap[rankingValidationStatus][1] }}</v-icon>
              </a>
              </v-chip>
            </v-card-subtitle>

            <div style="display: flex; justify-content: center; width: 100%">
              <v-simple-table v-if="rankingValidationError.length===0" style="max-width: 400px;">
                <template v-slot:default>
                  <thead>
                  <tr>
                    <th class="text-center">Measure</th>
                    <th class="text-center">Value</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(score,id) in rankingValidationScore" v-if="score!=null">
                    <td>{{ id }}</td>
                    <td>{{ score }}
                    </td>
                  </tr>
                  </tbody>
                </template>
              </v-simple-table>
              <div v-else>
                Error:
                <div style="color: dimgray">{{ rankingValidationError }}</div>
              </div>
            </div>
          </div>
        </div>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
import LabeledSwitch from "@/components/app/input/LabeledSwitch";
import SeedTable from "@/components/app/tables/SeedTable";
import NodeInput from "@/components/app/input/NodeInput";
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";

export default {
  name: "Validation",
  props: {
    step: Number,
    seedTypeId: Number,
    module: Array,
    ranking: Array,
    moduleApproved: Boolean,
    rankingApproved: Boolean,
  },

  data() {
    return {
      models: {
        perms: 1000,
        onlyApproved: true,
      },
      advancedOptions: false,
      suggestionType: "",
      moduleValidationUID: "",
      rankingValidationUID: "",
      moduleValidationStatus: "",
      rankingValidationStatus: "",
      moduleValidationScore: undefined,
      rankingValidationScore: undefined,
      moduleValidationError: "",
      rankingValidationError: "",
      styleMap: {
        "running": ["primary", "fas fa-circle-notch fa-spin"],
        "failed": ["red", "far fa-times-circle"],
        "completed": ["green", "fas fa-check"]
      },
      scoreIds: ["empirical DCG-based p-value", "empirical p-value without considering ranks", "empirical (precision-based) p-value", "empirical p-value"]
    }
  },

  methods: {
    getSuggestionSelection: function () {
      let type = "drug"
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
      out.push({value:type, text:"Drug"})
      if (!this.advancedOptions) {
        this.suggestionType = out[disorderIdx].value;
      }
      return out
    },
    resetValidation: function(){
      this.moduleValidationUID = ""
      this.rankingValidationUID = ""
      this.moduleValidationStatus = ""
      this.rankingValidationStatus = ""
      this.moduleValidationScore = undefined
      this.rankingValidationScore = undefined
      this.moduleValidationError = ""
      this.rankingValidationError = ""
    },

    addToSelection: function (data) {
      this.$refs.drugTable.addSeeds(data)
    }
    ,
    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    },

    addDrugs: function (drugs) {
      this.$refs.drugTable.addSeeds(drugs)
      this.updateCount()
    },

    updateCount: function () {
      this.validationDrugCount = this.getDrugs().length
      this.$emit("drugCountUpdate")
    },

    getDrugs: function () {
      return this.$refs.drugTable.getSeeds();
    },


    validate: function () {
      this.resetValidation()
      if (this.module != null && this.module.length > 0)
        this.validateModule(this.module, this.getDrugs(), this.moduleApproved, ["gene", "protein"][this.seedTypeId])
      if (this.ranking != null && this.ranking.length > 0)
        this.validateDrugs(this.ranking, this.getDrugs(), this.rankingApproved)
    },

    validateModule: async function (targets, validationDrugs, approved, type) {
      //FIXME remove when fixed; necessary to avoid errors for the time being
      let filtered = await this.$http.getInteractingOnly(type, targets.map(n => n.id)).catch(console.error)
      this.moduleValidationStatus = "preparing"
      let refDrugs = Object.values(validationDrugs).map(d => d.primaryDomainId);
      if (refDrugs.length === 0) {
        this.moduleValidationStatus = "no drugs"
        return;
      }
      let module = Object.values(targets).filter(n => filtered.indexOf(n.id) !== -1).map(node => node.primaryDomainId)
      let data = {
        module_members: module,
        module_member_type: type,
        true_drugs: refDrugs,
        permutations: this.models.perms,
        only_approved_drugs: this.models.onlyApproved,
      }
      this.$http.validateModule(data).then(response => {
        this.moduleValidationUID = response.data;
        this.checkValidationScore(response.data, "module")
      }).catch(console.error)
    },

    validateDrugs: function (targets, validationDrugs, approved) {
      this.rankingValidationStatus = "preparing"
      let refDrugs = Object.values(validationDrugs).map(d => d.primaryDomainId);
      if (refDrugs.length === 0) {
        this.rankingValidationStatus = "no drugs"
        return;
      }
      let drugs = [];
      targets.forEach(drug => {
        drugs.push([drug.primaryDomainId, drug.rank])
      })
      let data = {
        test_drugs: drugs,
        true_drugs: refDrugs,
        permutations: this.models.perms,
        only_approved_drugs: this.models.onlyApproved,
      }
      this.$http.validateDrugs(data).then(response => {
        this.rankingValidationUID = response.data;
        this.checkValidationScore(response.data, "ranking")
      }).catch(console.error)
    },

    checkValidationScore: function (id, type) {
      if (this[type + "ValidationScore"] == null)
        this.$http.getValidationScore(id).then(response => {
          this.$set(this, type + "ValidationStatus", response.data.status);
          if (this[type + "ValidationStatus"] === "running")
            setTimeout(() => {
              this.checkValidationScore(id, type)
            }, 5000)
          else {
            if (this[type + "ValidationStatus"] === "completed") {
              this.$set(this, type + "ValidationScore", {})
              this.scoreIds.forEach(id => this.$set(this[type + "ValidationScore"], id, response.data[id]))
            } else {
              this.$set(this, type + "ValidationScore", {})
              this.$set(this, type + "ValidationError", response.data.error);
            }
          }
        }).catch(console.error)
    },

  },

  components: {
    LabeledSwitch,
    SeedTable,
    NodeInput,
    SuggestionAutocomplete,
  }
}
</script>

<style scoped>

</style>
