<template>
  <v-card
    class="mb-4"
    min-height="80vh"
  >
    <v-card-subtitle class="headline">{{ step }}. Drug Prioritization Algorithm Selection</v-card-subtitle>
    <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on your seeds
      to identify a ranked list of drug candidates.
    </v-card-subtitle>
    <v-divider style="margin: 15px;"></v-divider>
    <v-container style="height: 80%; max-width: 100%">
      <v-row style="height: 100%">
        <v-col>
          <v-card-title style="margin-left: -25px">Select the Base-Algorithm</v-card-title>
          <v-radio-group v-model="methodModel" row>
            <v-radio v-for="method in methods"
                     :label="method.label"
                     :key="method.label"
                     :disabled="seeds.length===0"
            >
            </v-radio>
          </v-radio-group>
          <template v-if="methodModel!==undefined">
            <v-card-title style="margin-left:-25px">Configure Parameters</v-card-title>
            <div>
              <v-slider
                hide-details
                class="align-center"
                v-model="getAlgorithmModels().topX"
                step="1"
                min="1"
                max="2000"
              >
                <template v-slot:prepend>
                  <v-text-field
                    v-model="getAlgorithmModels().topX"
                    class="mt-0 pt-0"
                    type="integer"
                    style="width: 100px"
                    label="visualize topX"
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
                    <span>A integer X limiting the visualization to the top X drugs that were found.</span>
                  </v-tooltip>
                </template>
              </v-slider>
            </div>
            <div style="display: flex">
              <div>
                <v-switch
                  label="Only use experimentally validated interaction networks"
                  v-model="experimentalSwitch"
                >
                  <template v-slot:append>
                    <v-tooltip left>
                      <template v-slot:activator="{ on, attrs }">
                        <v-icon
                          v-bind="attrs"
                          v-on="on"
                          left> far fa-question-circle
                        </v-icon>
                      </template>
                      <span>Restricts the edges in the {{
                          ['Gene', 'Protein'][seedTypeId] + '-' + ['Gene', 'Protein'][seedTypeId]
                        }} network to experimentally validated ones.</span>
                    </v-tooltip>
                  </template>
                </v-switch>
              </div>
            </div>
            <div style="display:flex; width: 100%">
              <div style="justify-self: flex-start">
                <v-switch
                  label="Only direct Drugs"
                  v-model="getAlgorithmModels().onlyDirect"
                >
                  <template v-slot:append>
                    <v-tooltip left>
                      <template v-slot:activator="{ on, attrs }">
                        <v-icon
                          v-bind="attrs"
                          v-on="on"
                          left> far fa-question-circle
                        </v-icon>
                      </template>
                      <span>If only the drugs interacting directly with seeds should be considered in the ranking,
                             this should be selected. If including the non-direct drugs is desired unselect.</span>
                    </v-tooltip>
                  </template>
                </v-switch>
              </div>
              <div style="justify-self: center; margin-left: auto">
                <v-switch
                  label="Only approved Drugs"
                  v-model="getAlgorithmModels().onlyApproved"
                >
                  <template v-slot:append>
                    <v-tooltip left>
                      <template v-slot:activator="{ on, attrs }">
                        <v-icon
                          v-bind="attrs"
                          v-on="on"
                          left> far fa-question-circle
                        </v-icon>
                      </template>
                      <span>If only approved drugs should be considered in the ranking,
                             this should be selected. If including all approved and unapproved drugs is desired unselect.</span>
                    </v-tooltip>
                  </template>
                </v-switch>
              </div>
              <div style="justify-self: flex-end; margin-left: auto">
                <v-switch
                  label="Filter Element 'Drugs'"
                  v-model="getAlgorithmModels().filterElements"
                >
                  <template v-slot:append>
                    <v-tooltip left>
                      <template v-slot:activator="{ on, attrs }">
                        <v-icon
                          v-bind="attrs"
                          v-on="on"
                          left> far fa-question-circle
                        </v-icon>
                      </template>
                      <span>Filters:<br><b>chemical elements:</b> <i>Gold</i>, <i>Zinc</i>, ...<br><b>metals and metal cations:</b> <i>Cupric Chloride</i>, <i>Aluminium acetoacetate</i>, ...<br><b>minerals and mineral supplements:</b> <i>Calcium silicate</i>, <i>Sodium chloride</i>, ...</span>
                    </v-tooltip>
                  </template>
                </v-switch>
              </div>
            </div>
            <div>
              <v-slider
                v-show="getAlgorithmModels().damping!=null"
                hide-details
                class="align-center"
                v-model="getAlgorithmModels().damping"
                step="0.01"
                min="0"
                max="1"
              >
                <template v-slot:prepend>
                  <v-text-field
                    v-model="getAlgorithmModels().damping"
                    class="mt-0 pt-0"
                    type="float"
                    style="width: 60px"
                    label="damping-factor"
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
                    <span>A float value between 0-1 to be used as damping factor parameter.</span>
                  </v-tooltip>
                </template>
              </v-slider>
            </div>
          </template>

        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>
<script>
export default {
  name: "DPAlgorithmSelect",
  props: {
    seeds: Array,
    seedTypeId: Number,
    step: Number,
    blitz: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      methodModel: undefined,
      rankingMethodModel: undefined,
      experimentalSwitch: true,
      methods: [{
        id: "trustrank",
        label: "TrustRank",
        models: {
          topX: 100,
          onlyApproved: true,
          onlyDirect: true,
          damping: 0.85,
          filterElements: true,
        },
        scores: [{
          id: "score",
          name: "Score",
          decimal: true,
          normalize: true,
          order: "descending",
          primary: true
        }, {id: "rank", name: "Rank"}]
      },
        {
          id: "centrality",
          label: "Closeness Centrality",
          models: {
            topX: 100,
            onlyApproved: true,
            onlyDirect: true,
            filterElements: true,
          },
          scores: [{
            id: "score",
            name: "Score",
            decimal: true,
            normalize: true,
            order: "descending",
            primary: true
          }, {id: "rank", name: "Rank"}]
        }]
    }
  },

  created() {
    if (this.blitz)
      this.methodModel = 1

  },

  watch: {

    methodModel: function (value) {
      this.$emit("algorithmSelectedEvent", value != null)
    }
  },


  methods: {

    getAlgorithm: function () {
      if (this.methodModel != null)
        return this.methods[this.methodModel]
      return undefined;
    },

    getAlgorithmModels: function () {
      if (this.methodModel == null)
        return undefined;
      return this.getAlgorithm().models != null ? this.getAlgorithm().models : {};
    },

    getAlgorithmMethod: function () {
      if (this.methodModel != null)
        return this.getAlgorithm().id
      return undefined
    },

    getParams: async function () {
      let params = {}
      let models = this.getAlgorithmModels()
      params.experimentalOnly = this.experimentalSwitch

      params["addInteractions"] = true
      params["nodesOnly"] = true

      params['direct'] = models.onlyDirect;
      params['approved'] = models.onlyApproved;
      if (this.getAlgorithmMethod() === "trustrank")
        params['damping'] = models.damping;

      params['type'] = ["gene", "protein"][this.seedTypeId]
      params['topX'] = models.topX
      params['elements'] = !models.filterElements
      return params;
    },

    getHeaders: function () {
      if (this.getAlgorithm() != null || this.getAlgorithm().scores != null)
        return this.getAlgorithm().scores;
      return []
    },
    run: async function (gid) {
      let params = await this.getParams();
      let payload = {
        userId: this.$cookies.get("uid"),
        dbVersion: this.$global.metadata.repotrial.version,
        algorithm: this.getAlgorithmMethod(),
        params: params
      }

      payload.experimentalOnly = params.experimentalOnly
      payload.selection = gid==null
      if (gid != null) {
        payload.graphId = gid
      }else {
        payload["nodes"] = this.seeds.map(n => n.id)
      }
      let ctx = this
      if (this.seeds.length === 0) {
        this.printNotification("Cannot execute " + this.getAlgorithm().label + " without seed nodes!", 1)
        return;
      }
      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        if (data.state === "DONE") {
          ctx.$emit("jobEvent", data, true)
        } else {
          this.$socket.subscribeJob(data.jid, "quickRankingFinishedEvent");
          ctx.$emit("jobEvent", data)
        }
      }).catch(console.error)

    }

  }

}
</script>

<style scoped>

</style>
