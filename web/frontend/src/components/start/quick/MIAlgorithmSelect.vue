<template>
  <v-card
    class="mb-4"
    min-height="80vh"
  >
    <v-card-subtitle class="headline">2. Module Identification Algorithm Selection</v-card-subtitle>
    <v-card-subtitle style="margin-top: -25px">Select and adjust the algorithm you want to apply on your seeds
      to construct a disease module.
    </v-card-subtitle>
    <v-divider style="margin: 15px;"></v-divider>
    <v-container style="height: 80%; max-width: 100%">
      <v-list-item-title>Select the Algorithm group!</v-list-item-title>
      <v-list-item-action>

        <v-radio-group row v-model="groupModel">
          <v-tooltip bottom v-for="group in groups" :key="group.id">
            <template v-slot:activator="{attrs, on}">
              <v-radio :label="group.label" :value="group.id" v-on="on" v-bind="attrs">
              </v-radio>
            </template>
            <span>{{ group.tooltip }}</span>
          </v-tooltip>
        </v-radio-group>

      </v-list-item-action>
      <v-row style="height: 100%">
        <v-col>
          <v-card-title style="margin-left: -25px">Select the Base-Algorithm</v-card-title>
          <v-tabs v-model="methodModel" v-for="group in groups" :key="'tabs_'+group.id" v-if="group.id===groupModel">
            <v-tab v-for="method in methods.filter(m=>m.group===group.id)" :key="method.id">{{ method.label }}</v-tab>
          </v-tabs>
          <div v-if="methodModel!==undefined" style="margin-left: 20px">
            <div v-for="method in methods" :key="'desc_'+method.id" v-show="method.id===getAlgorithmMethod()">
              <v-card-title style="margin-left:-15px; padding-top:10px; padding-bottom: 5px" @click="showDescription = !showDescription">{{ method.descType }}
                <v-icon right>{{showDescription ? 'fas fa-angle-up':'fas fa-angle-down'}}</v-icon>
              </v-card-title>
              <div style="display: flex; justify-content: left; margin-left: 15px" v-show="showDescription">
                <div  style="text-align: justify; color: dimgray">
                  {{ getAlgorithm().description }}
                  <v-chip outlined><a :href="getAlgorithm().link" target="_blank" style="text-decoration: none">Read
                    more
                    <v-icon right>fas fa-angle-double-right</v-icon>
                  </a></v-chip>
                </div>
              </div>
            </div>
            <v-card-title style="margin-left:-15px; padding-top:10px; padding-bottom: 15px">Parameters</v-card-title>
            <div style=" margin-left: 15px">
              <div style="display: flex; justify-content: flex-start">
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
                          style="flex-grow: 0"
                        </v-icon>
                      </template>
                      <span>Restricts the edges in the {{
                          ['Gene', 'Protein'][seedTypeId] + '-' + ['Gene', 'Protein'][seedTypeId]
                        }} interaction network to experimentally validated ones.</span>
                    </v-tooltip>
                  </template>
                </v-switch>
              </div>
              <div>

                <template v-if="getAlgorithmMethod()==='bicon'">
                  <div style="justify-self: flex-end">
                    <v-file-input
                      v-on:change="biconFile"
                      show-size
                      prepend-icon="fas fa-file-medical"
                      label="Expression File"
                      dense
                    >
                    </v-file-input>
                  </div>

                  <div>
                    <v-range-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().lg"
                      min="1"
                      max="1000"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().lg[0]"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 60px"
                          label="min"
                        ></v-text-field>
                      </template>
                      <template v-slot:append>
                        <v-text-field
                          v-model="getAlgorithmModels().lg[1]"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 60px"
                          label="max"
                        ></v-text-field>
                        <v-tooltip left>
                          <template v-slot:activator="{ on, attrs }">
                            <v-icon
                              v-bind="attrs"
                              v-on="on"
                              left> far fa-question-circle
                            </v-icon>
                          </template>
                          <span>Maximal solution subnetwork size.</span>
                        </v-tooltip>
                      </template>
                    </v-range-slider>
                  </div>
                </template>
                <template v-if="getAlgorithmMethod()==='diamond'">
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().nModel"
                      min="1"
                      max="1000"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().nModel"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 60px"
                          label="n"
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
                          <span>Desired number of DIAMOnD genes, 200 is a reasonable
                       starting point.</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().alphaModel"
                      min="1"
                      max="100"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().alphaModel"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 60px"
                          label="alpha"
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
                          <span>an integer representing weight of the seeds,default
                       value is set to 1.</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().pModel"
                      min="-100"
                      max="0"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          prefix="10^"
                          v-model="getAlgorithmModels().pModel"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 80px"
                          label="p-cutoff"
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
                          <span>Choose a cutoff for the resulting p_hyper scores.</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                </template>
                <template v-if="getAlgorithmMethod()==='robust'">
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().initFract"
                      min="0"
                      step="0.01"
                      max="1"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().initFract"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 80px"
                          label="Initial Fraction"
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
                          <span>initial fraction</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().reductionFactor"
                      min="0"
                      step="0.01"
                      max="1"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().reductionFactor"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 80px"
                          label="Reduction Factor"
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
                          <span>reduction factor</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().threshold"
                      min="-100"
                      max="0"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          prefix="10^"
                          v-model="getAlgorithmModels().threshold"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 80px"
                          label="threshold"
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
                          <span>Threshold</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().trees"
                      min="2"
                      max="100"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().trees"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 70px"
                          label="trees"
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
                          <span>Number of steiner trees to be computed (Integer).</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>


                </template>
                <template v-if="getAlgorithmMethod()==='must'">
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().hubpenalty"
                      min="0"
                      max="1"
                      step="0.01"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().hubpenalty"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 70px"
                          label="hub-penalty"
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
                          <span>Choose this option if you want to return multiple results.</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div>
                    <v-switch
                      label="Multiple"
                      v-model="getAlgorithmModels().multiple"
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
                          <span>Specify hub penalty between 0.0 and 1.0. If none is specified, there will be no hub penalty.</span>
                        </v-tooltip>
                      </template>
                    </v-switch>
                  </div>
                  <div v-show="getAlgorithmModels().multiple">
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().trees"
                      min="2"
                      max="50"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().trees"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 70px"
                          label="trees"
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
                          <span>Number of Trees to be returned (Integer).</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().maxit"
                      min="0"
                      max="20"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().maxit"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 70px"
                          label="iterations"
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
                          <span>The maximum number of iterations is defined as trees + iterations.</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>
<script>
import LabeledSwitch from "@/components/app/input/LabeledSwitch";

export default {
  name: "MIAlgorithmSelect",
  components: {LabeledSwitch},
  props: {
    seeds: Array,
    seedTypeId: Number,
    socketEvent:String,
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
      groupModel: "nw",
      showDescription:false,
      groups: [
        {
          id: "nw",
          label: "Network Based",
          tooltip: "This algorithm group uses seed genes and the NeDRex networks to identify modules."
        },
        {
          id: "exp",
          label: "Expression Based",
          tooltip: "This algorithm group uses user specific expression data to derive disorder modules."
        }
      ],
      methods:
        [{
          id: "diamond",
          group: "nw",
          label: "DIAMOnD",
          descType: "Abstract",
          description: "\"Diseases are rarely the result of an abnormality in a single gene, but involve a whole cascade of interactions between several cellular processes. To disentangle these complex interactions it is necessary to study genotype-phenotype relationships in the context of protein-protein interaction networks. Our analysis of 70 diseases shows that disease proteins are not randomly scattered within these networks, but agglomerate in specific regions, suggesting the existence of specific disease modules for each disease. The identification of these modules is the first step towards elucidating the biological mechanisms of a disease or for a targeted search of drug targets. We present a systematic analysis of the connectivity patterns of disease proteins and determine the most predictive topological property for their identification. This allows us to rationally design a reliable and efficient Disease Module Detection algorithm (DIAMOnD).\"",
          link: "https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1004120",
          models: {
            nModel: 200,
            alphaModel: 1,
            pModel: 0
          },
          scores: [{id: "rank", name: "Rank", order: "ascending", primary: true, seed: 0}, {
            id: "p_hyper",
            name: "P-Value",
            decimal: true,
            order: "ascending",
            seed: 0.0
          }]
        }, {
          id: "bicon", group: "exp", label: "BiCoN", scores: [], models: {
            exprFile: undefined,
            lg: [10, 15]
          },
          descType: "Abstract",
          description: "\"Unsupervised learning approaches are frequently employed to identify patient subgroups and biomarkers such as disease-associated genes. Biclustering is a powerful technique often used with expression data to cluster genes along with patients. However, the genes forming biclusters are often not functionally related, complicating interpretation of the results.\n" +
            "\n" +
            "To alleviate this, we developed the network-constrained biclustering approach BiCoN which (i) restricts biclusters to functionally related genes connected in molecular interaction networks and (ii) maximizes the expression difference between two subgroups of patients.\"",
          link: "https://biomedical-big-data.de/publication/lazareva-2020-bicon/",
        }, {
          id: "must", group: "nw", label: "MuST", scores: [], models: {
            hubpenalty: 0,
            multiple: false,
            trees: 10,
            maxit: 10,
          },
          descType: "Explanation",
          description: "Minimal Steiner trees are generated, meaning a minimal scoring tree is identified that is still only one connected component. This way, minimal cost paths within the network are identified. The reasoning behind using multiple trees is, that PPI clusters are highly connected leading to several shortest path options. This means that using only one tree would not really resemble the best solution but be drawn randomly from multiple equal ones. These multiple trees are then used to derive a prioritized list of target nodes the seeds are connected to, where the number of 'visits' of a non-seed node indicates the priority or significance of it.",
          link: "https://en.wikipedia.org/wiki/Steiner_tree_problem"
        },
          {
            id: "domino",
            group: "nw",
            label: "DOMINO",
            scores: []
            ,
            descType: "Abstract",
            description: "\"We designed a permutation-based method that empirically evaluates GO terms reported by AMI methods. We used the method to fashion five novel AMI performance criteria. Last, we developed DOMINO, a novel AMI algorithm, that outperformed the other six algorithms in extensive testing on GE and GWAS data. Software is available at\"",
            link: "https://www.embopress.org/doi/full/10.15252/msb.20209593"

          }, {
          id: "robust",
          label: "ROBUST",
          group: "nw",
          descType: "Abstract",
          description: "\"Disease module mining methods (DMMMs) extract subgraphs that constitute candidate\n" +
            "disease mechanisms from molecular interaction networks such as protein-protein interaction (PPI)\n" +
            "networks. Irrespective of the employed models, DMMMs typically include non-robust steps in their\n" +
            "workflows, i. e., the computed subnetworks vary when running the DMMMs multiple times on equivalent\n" +
            "input. This lack of robustness has a negative effect on the trustworthiness of the obtained subnetworks\n" +
            "and is hence detrimental for the wide-spread adoption of DMMMs in the biomedical sciences.\n" +
            "Results: To overcome this problem, we present a new DMMM called ROBUST (robust disease module\n" +
            "mining via enumeration of diverse prize-collecting Steiner trees). In a large-scale empirical evaluation,\n" +
            "we show that ROBUST outperforms competing methods in terms of robustness, scalability and, in most\n" +
            "settings, functional relevance of the produced modules, measured via KEGG gene set enrichment scores\n" +
            "and overlap with DisGeNET disease genes. With ROBUST, we make an important contribution to network\n" +
            "medicine by overcoming the lack of robustness in disease module extraction.\"",
          link: "https://github.com/bionetslab/robust",
          models: {
            initFract: 0.25,
            reductionFactor: 0.9,
            trees: 30,
            threshold: -1
          },
          scores: [{
            id: "occs_abs",
            name: "Occs (Abs)",
            order: "descending",
            seed: -1
          }, {
            id: "occs_rel", group: "nw", name: "Occs (%)",
            primary: true, decimal: true, order: "descending", seed: 1
          }]
        }
        ],
    }
  },

  created() {
    if (this.blitz)
      this.methodModel = 0
  },

  watch: {
    groupModel: function (newVal, oldVal) {
      this.methodModel = 0
    },

    methodModel: function (value) {
      this.$emit("algorithmSelectedEvent", value != null)
    }
  },


  methods: {

    getAlgorithm: function () {
      if (this.methodModel != null)
        return this.methods.filter(m => m.group === this.groupModel)[this.methodModel]
      return undefined;
    },

    getAlgorithmModels: function () {
      if (this.methodModel == null)
        return undefined;
      return this.getAlgorithm().models != null ? this.getAlgorithm().models : {};
    },

    getAlgorithmMethod: function () {
      if (this.methodModel != null && this.getAlgorithm() != null)
        return this.getAlgorithm().id
      return undefined
    },

    getParams: async function () {
      let params = {}
      let method = this.getAlgorithmMethod()
      params.experimentalOnly = this.experimentalSwitch
      params["addInteractions"] = true
      params["nodesOnly"] = true
      if (method === 'bicon') {
        this.seeds = []
        params['lg_min'] = this.getAlgorithmModels().lg[0];
        params['lg_max'] = this.getAlgorithmModels().lg[1];
        params['type'] = "gene"
        await this.$utils.readFile(this.getAlgorithmModels().exprFile).then(content => {
          params['exprData'] = content
        })
        return params
      }
      if (method === 'diamond') {
        params['n'] = this.getAlgorithmModels().nModel;
        params['alpha'] = this.getAlgorithmModels().alphaModel;
        params['pcutoff'] = this.getAlgorithmModels().pModel;
      }
      if (method === 'must') {
        params["penalty"] = this.getAlgorithmModels().hubpenalty;
        params["multiple"] = this.getAlgorithmModels().multiple
        params["trees"] = this.getAlgorithmModels().trees
        params["maxit"] = this.getAlgorithmModels().maxit
      }
      if (method === 'robust') {
        params["trees"] = this.getAlgorithmModels().trees;
        params["initFract"] = this.getAlgorithmModels().initFract;
        params["threshold"] = Math.pow(10, this.getAlgorithmModels().threshold);
        params["reductionFactor"] = this.getAlgorithmModels().reductionFactor;
      }
      params['type'] = ["gene", "protein"][this.seedTypeId]
      return params;
    },
    biconFile: function (file) {
      this.models.bicon.exprFile = file
    }
    ,
    getHeaders: function () {
      if (this.getAlgorithm() != null || this.getAlgorithm().scores != null)
        return this.getAlgorithm().scores;
      return []

    },
    run: async function () {
      let params = await this.getParams();
      let payload = {
        userId: this.$cookies.get("uid"),
        dbVersion: this.$global.metadata.repotrial.version,
        algorithm: this.getAlgorithmMethod(),
        params: params
      }
      payload.selection = true
      payload.experimentalOnly = params.experimentalOnly
      payload["nodes"] = this.seeds.map(n => n.id)
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
    }

  }

}
</script>

<style scoped>

</style>
