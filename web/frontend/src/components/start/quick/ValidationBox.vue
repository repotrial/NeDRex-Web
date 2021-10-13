<template>
  <div style="display: flex;width: 100%; margin-top: -20px">
    <div style="align-self: flex-start; margin-left:15px">
      <v-tooltip bottom v-if="validationStatus!=null">
        <template v-slot:activator="{attr, on}">
          <div>
            <v-chip v-on="on" v-bind="attr" v-if="validationStatus==='no drugs'" color="orange" x-small>could not
              validate
              <v-icon right>fas fa-exclamation-triangle</v-icon>
            </v-chip>
            <a v-on="on" v-bind="attr" v-else target="_blank"
               :href="'https://api.nedrex.net/validation/status?uid='+validationUID" style="text-decoration:none">

              <v-chip v-if="validationStatus==='failed'" color="red" x-small style="color: white">validation error
                <v-icon right x-small>far fa-times-circle</v-icon>
              </v-chip>
              <template v-else>
                <v-chip :color="validationStatus==='completed' ? 'primary' :'green' " style="color: white" x-small v-if="drugs">{{
                    validationStatus !== "completed" ? "Validation: " + validationStatus : "P-values: " + $utils.roundValue(validationScore["empirical DCG-based p-value"], 6)+ ' / ' + $utils.roundValue(validationScore["empirical p-value without considering ranks"], 6)
                  }}
                  <v-icon right x-small>
                    {{
                      validationStatus !== "completed" ? "fas fa-circle-notch fa-spin" : "fas fa-question-circle"
                    }}
                  </v-icon>
                </v-chip>
                <v-chip :color="validationStatus==='completed' ? 'primary' :'green' " style="color: white" x-small v-if="!drugs">{{
                    validationStatus !== "completed" ? "Validation: " + validationStatus : "P-values: " + $utils.roundValue(validationScore["emprirical p-value"],6)+ ' / ' + $utils.roundValue(validationScore["empircal (precision-based) p-value"], 6) }}
                  <v-icon right x-small>
                    {{
                      validationStatus !== "completed" ? "fas fa-circle-notch fa-spin" : "fas fa-question-circle"
                    }}
                  </v-icon>
                </v-chip>
              </template>
            </a>
          </div>
        </template>
          <div style="max-width: 400px">
            <span
              v-if="validationStatus!=='no drugs'"><i>Check the result yourself by clicking on this chip!</i><br></span>
            <span v-if="validationStatus==='completed'">
                      <b>Ranking Validation Scores:</b>
                      <br>
                      <v-container>
                        <v-row v-for="(score,scoreKey) in validationScore" :key="scoreKey"
                               style="margin-top: -10px" v-if="score!=null">
                          <v-col cols="9">{{ scoreKey }}</v-col>
                          <v-col cols="3">{{ score }}</v-col>
                        </v-row>
                        </v-container>
      <b>The validation was executed with following {{ Object.keys(validationDrugs).length }} drugs:<br></b>
      <div style="max-width: 350px;">{{ getDrugList() }}</div>
        <i>This list was derived from your seed selection process</i>
    </span>
            <span
              v-else-if="validationStatus==='failed'">Validation was interrupted by an error:<br>{{
                validationError
              }}</span>
            <span v-else-if="validationStatus==='no drugs'">Based on your query NeDRex-Web was not able to find any associated drugs which are necessary for validation!</span>
          </div>
      </v-tooltip>
    </div>
  </div>
</template>

<script>
export default {
  name: "ValidationBox",

  props: {
    drugs: {
      default: false,
      type: Boolean,
    }
  },

  data() {
    return {
      validationScore: undefined,
      validationStatus: undefined,
      validationDrugs: {},
      validationError: "",
      validationUID: undefined,
      scoreIds: ["empirical DCG-based p-value", "empirical p-value without considering ranks","empircal (precision-based) p-value","emprirical p-value"]
    }
  },


  methods: {

    init: function () {
      this.validationScore = undefined;
      this.validationStatus = undefined;
      this.validationDrugs = {}
      this.validationError = ""
      this.validationUID = undefined
    },

    validate: function (test, refs, approved, type) {
      if (this.drugs)
        this.validateDrugs(test, refs, approved)
      else
        this.validateModule(test, refs, approved, type)
    },

    validateModule: async function (targets, validationDrugs, approved, type) {
      //necessary to avoid errors for the time being
      let filtered = await this.$http.getInteractingOnly(type,targets.map(n => n.id)).catch(console.error)
      this.validationDrugs = validationDrugs;
      this.validationStatus = "preparing"
      let refDrugs = Object.values(validationDrugs).map(d => d.primaryDomainId);
      if (refDrugs.length === 0) {
        this.validationStatus = "no drugs"
        return;
      }
      let module = Object.values(targets).filter(n=>filtered.indexOf(n.id)!==-1).map(node => node.primaryDomainId)
      let data = {
        module_members: module,
        module_member_type: type,
        true_drugs: refDrugs,
        permutations: 1000,
        only_approved_drugs: approved,
      }
      this.$http.validateModule(data).then(response => {
        this.validationUID = response.data;
        this.checkValidationScore(response.data)
      }).catch(console.error)
    },

    validateDrugs: function (targets, validationDrugs, approved) {
      this.validationDrugs = validationDrugs;
      this.validationStatus = "preparing"
      let refDrugs = Object.values(validationDrugs).map(d => d.primaryDomainId);
      if (refDrugs.length === 0) {
        this.validationStatus = "no drugs"
        return;
      }
      let drugs = [];
      targets.forEach(drug => {
        drugs.push([drug.primaryDomainId, drug.rank])
      })
      let data = {
        test_drugs: drugs,
        true_drugs: refDrugs,
        permutations: 1000,
        only_approved_drugs: approved,
      }
      this.$http.validateDrugs(data).then(response => {
        this.validationUID = response.data;
        this.checkValidationScore(response.data)
      }).catch(console.error)
    },

    getDrugList: function () {
      let drugs = ""
      let count = 0
      let cut = 50;
      Object.values(this.validationDrugs).forEach(drug => {
          count++;
          if (count > cut)
            return
          if (count === cut)
            drugs = drugs.substring(0, drugs.length - 1) + " ...and " + (Object.keys(this.validationDrugs).length - cut) + " more!,"
          else
            drugs += drug.displayName + ", "
        }
      )

      return drugs.substring(0, drugs.length - 1)
    },

    checkValidationScore: function (id) {
      if (this.validationScore == null)
        this.$http.getValidationScore(id).then(response => {
          this.validationStatus = response.data.status;
          if (this.validationStatus === "running")
            setTimeout(() => {
              this.checkValidationScore(id)
            }, 2000)
          else {
            if (this.validationStatus === "completed") {
              this.validationScore = {};
              this.scoreIds.forEach(id => this.validationScore[id] = response.data[id])
            } else {
              this.validationScore = {}
              this.validationError = response.data.error;
            }
          }
        }).catch(console.error)
    },


  }
}
</script>

<style scoped>

</style>
