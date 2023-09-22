<template>
  <v-card
    class="mb-4"
    min-height="80vh"
    :flat="flat"
  >
    <template v-if="header">
      <v-card-subtitle class="headline" style="color: black; text-align: left; margin-left: 5vw">{{ step }}. Drug
        Prioritization Algorithm Selection
      </v-card-subtitle>
      <v-card-subtitle style="margin-top: -25px">
        <ul>
          <li style="margin-left: 0;">Select an algorithm to rank drugs based on your selected seeds and configure it:
          </li>
          <li style="margin-top: 8px">
            <b>1.</b> Select network-based (starting from seeds) or expression-based (starting from expression data)
            algorithm group
          </li>
          <li>
            <b>2.</b> Select the algorithm
          </li>
          <li>
            <b>3.</b> Configure the algorithm by changing parameters
          </li>
        </ul>
      </v-card-subtitle>
      <v-divider style="margin: 15px;"></v-divider>
    </template>
    <v-container style="height: 80%; max-width: 100%">
      <v-card-title style="text-align: left"><b>Select the Base-Algorithm:</b>
        <v-radio-group style="margin-left: 32px" row v-model="methodModel" optional>
          <v-radio v-for="(method,index) in methods" :key="method.id" :value="index" :label="method.label">
            {{ method.label }}
          </v-radio>
        </v-radio-group>
      </v-card-title>
      <div v-if="methodModel!==undefined" style="margin-left: 20px">
        <div v-for="method in methods" :key="'desc_'+method.id" v-show="method.id===getAlgorithmMethod()">
          <v-card-title style="margin-left:-15px; padding-top:10px; padding-bottom: 5px">{{ method.descType }}
          </v-card-title>
          <div style="display: flex; justify-content: flex-start; margin-left: 15px">
            <div>
              <div style="text-align: justify; color: dimgray" v-html="getAlgorithm().description">
              </div>
              <div style="display: flex; justify-content: flex-start">
                <v-chip outlined><a :href="getAlgorithm().link" target="_blank" style="text-decoration: none">Read
                  more
                  <v-icon right>fas fa-angle-double-right</v-icon>
                </a></v-chip>
              </div>
            </div>
          </div>
        </div>
        <v-card-title style="margin-left:-15px; padding-top:10px; padding-bottom: 15px">Parameters</v-card-title>
        <div style=" margin-left: 15px">
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
            <v-switch
              style="justify-self: flex-start; margin-left: 0; margin-right: auto"
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
            <v-switch
              v-if="connectionSelect"
              style="justify-self: flex-start; margin-left: 0; margin-right: auto"
              :label="'Add '+['gene','protein'][seedTypeId]+' interactions'"
              v-model="interactionSwitch"
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
                  <span>Adds {{
                      ['Gene', 'Protein'][seedTypeId] + '-' + ['Gene', 'Protein'][seedTypeId]
                    }} interactions between seeds to the generated network.</span>
                </v-tooltip>
              </template>
            </v-switch>
            <v-select :items="tissues" v-model="tissueFilter" outlined dense label="Tissue"
                      style="max-width: 250px; justify-self: center; margin-left: auto; margin-right: auto; margin-top:16px"></v-select>
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
          <div v-if="getAlgorithmMethod() === 'trustrank'">
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
                  style="width: 100px"
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
        </div>
      </div>
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
    socketEvent: String,
    connectionSelect: {
      type: Boolean,
      default: false,
    },
    blitz: {
      type: Boolean,
      default: false
    },
    flat: {
      default: false,
      type: Boolean,
    },
    goal: {
      type: String,
      default: "drug_prioritization",
    },
    header: {
      type: Boolean,
      default: true
    }
  },

  data() {
    return {
      methodModel: undefined,
      experimentalSwitch: true,
      interactionSwitch: this.connectionSelect,
      showDescription: false,
      tissues: undefined,
      tissueFilter: "all",
      methods: [{
        id: "trustrank",
        label: "TrustRank",
        description: "TrustRank is derived from the well known PageRank algorithm. Its  original purpose is to filter spam-sites that are not highly supported by other pages. This is achieved by assigning weights based on in- and out-going edges to favor highly and punish weakly supported nodes. In theory, these weights are iteratively propagated through the network until the assigned scores or ranks stabilize. In a biological application, the ranked websites are drugs, and the most trusted ones are drugs that have high chances of targeting a large number of seeds.",
        link: "https://en.wikipedia.org/wiki/TrustRank",
        descType: "Description",
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
          description: "Like the name suggests, closeness centrality is one of several centrality measurements. These generally are used in network analysis to derive scores that translate to the importance of the nodes in its specific network. Closeness, in particular, is derived by calculating the sum over all shortest paths between the specific node n and any other to the power of the negative one. A low average shortest path is then used as an indicator for possible candidates that target the module.",
          link: "https://en.wikipedia.org/wiki/Closeness_centrality",
          descType: "Description",
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
    this.getTissueTypes()
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

    setMethod: async function (method, params) {
      method = method.toLowerCase()
      let algos = this.methods.filter(m => m.group === this.groupModel)
      for (let i = 0; i < algos.length; i++) {
        if (algos[i].id === method) {
          this.methodModel = i
          break
        }
      }
      if (params) {
        let models = this.getAlgorithmModels()
        Object.keys(models).forEach(key => {
          if (params[key] != null) {
            models[key] = params[key]
          }
        })
      }
    },

    getTissueTypes: function () {
      this.$http.get("/getTissues").then(response => {
        if (response.data != null)
          return response.data
      }).then(data => {
        this.tissues = [{text: 'All', value: 'all'}]
        data.forEach(tissue => {
          this.tissues.push({text: tissue, value: tissue})
        })
      }).catch(console.error)
    },

    getParams: async function () {
      let params = {}
      let models = this.getAlgorithmModels()
      params.experimentalOnly = this.experimentalSwitch
      params.interactions = this.interactionSwitch
      params["addInteractions"] = true
      params["nodesOnly"] = false

      params['direct'] = models.onlyDirect;
      params['approved'] = models.onlyApproved;
      if (this.getAlgorithmMethod() === "trustrank")
        params['damping'] = models.damping;

      params['type'] = ["gene", "protein"][this.seedTypeId]
      params['topX'] = models.topX
      params['elements'] = !models.filterElements
      return params;
    },

    getParamString: async function () {
      let params = await this.getParams()
      delete params.nodesOnly
      delete params.addInteractions
      let str = "Tissue=" + this.tissueFilter + ", "
      Object.keys(params).forEach(key => {
        str += key + "=" + params[key] + ", "
      })
      str = str.substring(0, str.length - 2)
      return str
    },

    getHeaders: function () {
      if (this.getAlgorithm() != null && this.getAlgorithm().scores != null)
        return this.getAlgorithm().scores;
      return []
    },
    runLater: async function (jid) {
      let params = await this.getParams();
      let payload = {
        userId: this.$cookies.get("uid"),
        dbVersion: this.$global.metadata.repotrial.version,
        algorithm: this.getAlgorithmMethod(),
        goal: this.goal,
        params: params,
        tissue: this.tissueFilter
      }

      payload.experimentalOnly = params.experimentalOnly
      if (this.seeds.length === 0 && jid == null) {
        this.printNotification("Cannot execute " + this.getAlgorithm().label + " without seed nodes!", 1)
        return;
      }
      payload.jobId = jid
      let ctx = this

      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        if (data.state === "DONE") {
          ctx.$emit("jobEvent", data, true)
        } else {
          this.$socket.subscribeJob(data.jid, this.socketEvent);
          ctx.$emit("jobEvent", data)
        }
      }).catch(console.error)
    },
    run: async function (gid) {
      let params = await this.getParams();
      let payload = {
        userId: this.$cookies.get("uid"),
        dbVersion: this.$global.metadata.repotrial.version,
        algorithm: this.getAlgorithmMethod(),
        goal: this.goal,
        params: params,
        tissue: this.tissueFilter
      }

      payload.experimentalOnly = params.experimentalOnly
      if (this.seeds.length === 0 && gid == null) {
        this.printNotification("Cannot execute " + this.getAlgorithm().label + " without seed nodes!", 1)
        return;
      }
      payload.selection = gid == null
      if (gid != null) {
        payload.graphId = gid
      } else {
        payload["nodes"] = this.seeds.map(n => n.id)
      }
      let ctx = this

      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        if (data.state === "DONE") {
          ctx.$emit("jobEvent", data, true)
        } else {
          this.$socket.subscribeJob(data.jid, this.socketEvent);
          ctx.$emit("jobEvent", data)
        }
      }).catch(console.error)
    },
    printNotification(message, type) {
      this.$emit("printNotification", message, type)
    },
  }

}
</script>

<style scoped>

</style>
