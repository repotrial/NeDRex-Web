<template>
  <v-card
    class="mb-4"
    min-height="80vh"
  >
    <v-card-subtitle class="headline">{{ step }}. Validation</v-card-subtitle>
    <v-card-subtitle style="margin-top: -25px">Validate the results by selecting drugs known to be effective in the
      current scenario.
    </v-card-subtitle>
    <v-divider style="margin: 15px;"></v-divider>
    <v-container style="height: 80%; max-width: 100%">
      <v-row>
        <v-col cols="6">
          <div style="height: 820px; max-height: 820px;">
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

        <v-divider vertical style="margin-bottom: 10px"></v-divider>
        <div style="justify-self: flex-end; margin-left: auto; width: 48%;"
             v-if="$refs.drugTable && $refs.drugTable.getSeeds().length>0">
          <v-card-title class="subtitle-1">Adjust validation parameters</v-card-title>
          <div style="width: 100%; display: flex; justify-content: left">
            <v-tooltip top>
              <template v-slot:activator="{attrs, on}">
                <div v-on="on" v-bind="attrs">
                  <v-switch v-model="models.onlyApproved" label="Approved Drugs only"></v-switch>
                </div>
              </template>
              <div style="width: 350px">Defines if only approved drugs or all should be used for random sampling in
                the
                validation step. For drug-ranking the value is by default set to the state that was chosen in the
                algorithm parametrization step.
              </div>
            </v-tooltip>
          </div>

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
                <div style="width: 300px">A value between 1000 and 10000 which determines the number of permutations
                  created for validation.
                </div>
              </v-tooltip>
            </template>
          </v-slider>
          <v-card-subtitle style="margin-top: 16px; color: #383838">{{ models.perms }} random modules with same node
            count and number of connected components or drug lists with the same number of drugs are computed and
            p-values derived by determining the fraction of cases in which random networks have a better score than the
            observed network.
          </v-card-subtitle>
          <v-btn @click="validate()" color="primary">Run Validation
            <v-icon right>fas fa-angle-double-right</v-icon>
          </v-btn>
          <div v-if="module!=null">
            <v-card-subtitle v-if="moduleValidationStatus !=null" class="title">Module Validation Result:
            </v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">
              <v-tooltip bottom v-if="styleMap[moduleValidationStatus]!=null">
                <template v-slot:activator="{attrs, on}">
                  <v-chip style="margin-left: 10px" v-on="on" v-bind="attrs"
                          :color="styleMap[moduleValidationStatus][0]"><a style="color: white;text-decoration: none"
                                                                          target="_blank"
                                                                          :href="'http://82.148.225.92:8022/validation/status?uid='+moduleValidationUID">
                    {{ moduleValidationStatus }}
                    <v-icon size="10pt">{{ styleMap[moduleValidationStatus][1] }}</v-icon>
                  </a>
                  </v-chip>
                </template>
                <div><b>Current State: </b>{{ moduleValidationStatus }}<br><i>Click here to show the raw server
                  response!</i></div>
              </v-tooltip>
            </v-card-subtitle>
            <div style="display: flex; justify-content: center; width: 100%">
              <v-simple-table v-if="moduleValidationError == null || moduleValidationError.length===0"
                              style="max-width: 400px;">
                <template v-slot:default>
                  <thead>
                  <tr>
                    <v-tooltip top>
                      <template v-slot:activator="{attrs, on}">
                        <th class="text-center">Measure
                          <v-icon small v-on="on" v-bind="attrs">far fa-question-circle</v-icon>
                        </th>
                      </template>
                      <div style="width: 300px">
                        <b>normal:</b> Cases in which #ref-drugs(random) > #ref-drugs(observed)
                        <br>
                        <b>precision based:</b> The precision of a module is defined by the drugs that target the module
                        (#ref-drugs/#all-drugs)
                      </div>
                    </v-tooltip>
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
              <v-tooltip bottom v-if="styleMap[rankingValidationStatus]!=null">
                <template v-slot:activator="{attrs, on}">
                  <v-chip style="color: white; margin-left: 10px" v-on="on" v-bind="attrs"
                          :color="styleMap[rankingValidationStatus][0]"><a style="color: white;text-decoration: none"
                                                                           target="_blank"
                                                                           :href="'http://82.148.225.92:8022/validation/status?uid='+rankingValidationUID">
                    {{ rankingValidationStatus }}
                    <v-icon size="10pt">{{ styleMap[rankingValidationStatus][1] }}</v-icon>
                  </a>
                  </v-chip>
                </template>
                <div><b>Current State: </b>{{ rankingValidationStatus }}<br><i>Click here to show the raw server
                  response!</i></div>
              </v-tooltip>
            </v-card-subtitle>

            <div style="display: flex; justify-content: center; width: 100%">
              <v-simple-table v-if="rankingValidationError == null ||rankingValidationError.length===0"
                              style="max-width: 400px;">
                <template v-slot:default>
                  <thead>
                  <tr>
                    <v-tooltip top>
                      <template v-slot:activator="{attrs, on}">
                    <th class="text-center">Measure <v-icon v-on="on" v-bind="attrs" small>far fa-question-circle</v-icon></th>
                      </template>
                      <div style="width: 300px">
                        <b>DCG-based:</b> Observed drug list is compared to random drug lists by discounted cumulative gain
                        <br>
                        <b>without ranks:</b> Only compares #found-drugs in observed and random lists based on the reference list
                      </div>
                    </v-tooltip>
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
      moduleResubmissions: 0,
      rankingResubmissions: 0,
      resubmissionCount: 3,
      moduleValidationError: "",
      rankingValidationError: "",
      styleMap: {
        "submitted": ["warning", "fas fa-hourglass-start"],
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
      out.push({value: type, text: "Drug"})
      if (!this.advancedOptions) {
        this.suggestionType = out[disorderIdx].value;
      }
      return out
    },
    resetValidation: function () {
      this.moduleValidationUID = ""
      this.rankingValidationUID = ""
      this.moduleValidationStatus = ""
      this.rankingValidationStatus = ""
      this.moduleValidationScore = undefined
      this.rankingValidationScore = undefined
      this.moduleValidationError = ""
      this.rankingValidationError = ""
      this.rankingResubmissions = 0;
      this.moduleResubmissions = 0;
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

    clear: function () {
      this.$refs.drugTable.clear();
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
      let nodes = targets.map(n => n.id)
      //FIXME remove when fixed; necessary to avoid errors for the time being ; also filtering non-experimental edges now
      nodes = await this.$http.getInteractingOnly(type, targets.map(n => n.id)).catch(console.error)
      // let filtered = targets.map(n=>n.id)
      this.moduleValidationStatus = "preparing"
      let refDrugs = Object.values(validationDrugs).map(d => d.primaryDomainId);
      if (refDrugs.length === 0) {
        this.moduleValidationStatus = "no drugs"
        return;
      }
      let module = Object.values(targets).filter(n => nodes.indexOf(n.id) !== -1).map(node => node.primaryDomainId)
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

    validateDrugs: function (targets, validationDrugs) {
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
      if (this[type + "ValidationScore"] == null && this[type + "ValidationUID"] === id)
        this.$http.getValidationScore(id).then(response => {
          this.$set(this, type + "ValidationStatus", response.data.status);
          if (this[type + "ValidationStatus"] === "running" || this[type + "ValidationStatus"] === "submitted")
            setTimeout(() => {
              this.checkValidationScore(id, type)
            }, 5000)
          else {
            if (this[type + "ValidationStatus"] === "completed") {
              this.$set(this, type + "ValidationScore", {})
              this.scoreIds.forEach(id => this.$set(this[type + "ValidationScore"], id, response.data[id]))
            } else {
              this.$set(this, type + "ValidationScore", {})
              if (response.data.error != null && response.data.error.length > 0) {
                this.checkResubmission(response.data, type)
              }
            }
          }
        }).catch(console.error)
    },

    checkResubmission: function (data, type) {
      if (this[type + "Resubmissions"] < this.resubmissionCount) {
        this.$set(this, type + "Resubmissions", this[type + "Resubmissions"] + 1)
        this.$http.postNedrex("/admin/resubmit/validation/" + this[type + "ValidationUID"]).then(() => {
          this[type + "ValidationScore"] = undefined
          this.checkValidationScore(this[type + "ValidationUID"], type)
        }).catch(console.error)
      } else {
        this.$set(this, type + "ValidationError", data.error);
      }
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
