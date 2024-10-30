<template>
  <v-card
    class="mb-4"
    min-height="80vh"
    :flat="flat"
  >
    <template v-if="header">
      <v-card-subtitle class="headline" style="color: black; text-align: left; margin-left: 5vw">2. Module Identification Algorithm Selection</v-card-subtitle>
      <v-card-subtitle style="margin-top: -25px">
        <ul>
          <li style="margin-left: 0;">Select an algorithm for disease module identification and configure it:</li>
          <li style="margin-top: 8px">
            <b>1.</b> Select network-based (starting from seeds) or expression-based (starting from expression data) algorithm group
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
      <v-card-title style="text-align: left"><b>Select the Algorithm group!</b>
        <v-radio-group row v-model="groupModel" style="display: inline-block; margin-left: 32px">
          <v-tooltip bottom v-for="group in groups" :key="group.id">
            <template v-slot:activator="{attrs, on}">
              <v-radio :label="group.label" :value="group.id" v-on="on" v-bind="attrs"
                       :disabled="group.id==='nw' && (seeds==null || seeds.length ===0)">
              </v-radio>
            </template>
            <span>{{ group.tooltip }}</span>
          </v-tooltip>
        </v-radio-group>
      </v-card-title>
      <v-row style="height: 100%">
        <v-col>
          <v-card-title style="text-align: left"><b>Select the Base-Algorithm:</b>
          <v-radio-group style="margin-left: 32px" row v-model="methodModel" v-for="group in groups" :key="'tabs_'+group.id" v-if="group.id===groupModel"
                  optional>
            <v-radio v-for="(method,index) in methods.filter(m=>m.group===group.id)" @click="methodModel=index" :value="index" :label="method.label" :key="method.id"></v-radio>
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
                    <v-chip outlined class="mx-2"><a :href="getAlgorithm().link" target="_blank" style="text-decoration: none;">Read
                      more
                      <v-icon right>fas fa-angle-double-right</v-icon>
                    </a></v-chip>
                    <v-chip outlined class="mx-2"><a :href="getAlgorithm().docu_link" target="_blank" style="text-decoration: none">Documentation
                      <v-icon right>fas fa-angle-double-right</v-icon>
                    </a></v-chip>
                  </div>
                </div>
              </div>
            </div>
            <v-card-title style="margin-left:-15px; padding-top:10px; padding-bottom: 15px">Parameters</v-card-title>
            <div style=" margin-left: 15px">
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
                          style="flex-grow: 0"
                        </v-icon>
                      </template>
                      <span>Restricts the edges in the {{
                          ['Gene', 'Protein'][seedTypeId] + '-' + ['Gene', 'Protein'][seedTypeId]
                        }} interaction network to experimentally validated ones.</span>
                    </v-tooltip>
                  </template>
                </v-switch>
                  <v-select :items="tissues" v-model="tissueFilter" outlined dense label="Tissue" style="max-width: 250px; justify-self: center; margin-left: auto; margin-right: auto"></v-select>
              </div>
              <div>

                <template v-if="getAlgorithmMethod()==='bicon'">
                  <div style="display: flex; width: 100%">
                    <div style="width: 200px; justify-self: flex-start">
                      <v-select :items="exprIds" item-text="id" item-value="id"
                                v-model="getAlgorithmModels().exprIDType">
                        <template v-slot:append-outer>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                                style="flex-grow: 0"
                              </v-icon>
                            </template>
                            <span>Select the gene identifiers your expression matrix uses.</span>
                          </v-tooltip>
                        </template>
                      </v-select>
                    </div>
                    <div
                      style="justify-self: flex-end; margin-left: auto; margin-right: auto; width: calc(100% - 400px); min-width: 250px; margin-top: 14px;">
                      <v-file-input
                        ref="upload"
                        v-on:change="biconFile"
                        outlined
                        solo
                        show-size
                        prepend-icon="fas fa-file-medical"
                        style="width: 400px;"
                        dense
                      >
                        <template v-slot:label>
                          <v-icon small>fas fa-file-upload</v-icon>
                          Upload Expression Matrix
                        </template>
                        <template v-slot:append-outer>
                          <v-tooltip left>
                            <template v-slot:activator="{ on, attrs }">
                              <v-icon
                                v-bind="attrs"
                                v-on="on"
                                left> far fa-question-circle
                              </v-icon>
                            </template>
                            <div>Select your expression matrix that should be uploaded. The server removes your data
                              once the the algorithm successfully terminated.
                            </div>
                          </v-tooltip>
                          <v-tooltip left>
                            <template v-slot:activator="{on,attrs}">
                              <a
                                href="https://drive.google.com/file/d/1zyKDy9rN_3KBDsKmzrqhodLmwAf0Sw3W/view?usp=sharing"
                                target="_blank" style="text-decoration: none">
                                <v-icon
                                  v-bind="attrs"
                                  v-on="on"
                                  left
                                >
                                  fas fa-download
                                </v-icon>
                              </a>
                            </template>
                            <div>Download an example expression matrix from the BiCon repository.</div>
                          </v-tooltip>
                        </template>
                      </v-file-input>
                    </div>
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
                              style="margin-top: -15px;"
                              v-bind="attrs"
                              v-on="on"
                              left> far fa-question-circle
                            </v-icon>
                          </template>
                          <span>Defines the minimal and maximal solution network size or genes used to bicluster the conditions / patients.</span>
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
                          <span>Integer representing the weight of the seeds,default
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
                          <div>The parameter α ∈ (0, 1] modifies the initial values for integrating<br>
                            non-seeds into the tree. This implicitly represents the allowed diversion<br>
                            from the cheapest Steiner tree. For α = 0, the algorithm would only<br>
                            return the best Steiner tree it can find but not allow any diversion from<br>
                            it. The larger α, the more diverse and but also more expensive the<br>
                            returned Steiner trees are allowed to become.
                          </div>
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
                          style="width: 100px"
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
                          <div>The parameter β ∈ [0, 1) modifies the decrease of the values for<br>
                            integrating non-seeds into the trees. Setting β = 0 will only give<br>
                            a value to a non-seed until its first appearance in one tree. This can<br>
                            quickly exhaust the available non-seeds and then has the same problem<br>
                            as α = 0. A too high value, on the other hand, might not be able<br>
                            to reduce the values sufficiently to make the other non-seeds more<br>
                            attractive. Hence, more trees need to be generated to achieve diversity.
                          </div>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().threshold"
                      min="0"
                      step="0.001"
                      max="1"
                    >
                      <template v-slot:prepend>
                        <v-text-field
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
                          <div>The threshold τ ∈ (0, 1] provides a trade-off between robustness and<br>
                            explorativeness. The larger τ , the more robust but less explorative the<br>
                            disease module computed by ROBUST
                          </div>
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
                          <span>Number of individual Steiner trees to be computed.</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>


                </template>
                <template v-if="getAlgorithmMethod() ==='kpm'">
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().k"
                      min="0"
                      max="50"
                    >
                      <template v-slot:prepend>
                        <v-text-field
                          v-model="getAlgorithmModels().k"
                          class="mt-0 pt-0"
                          type="number"
                          style="width: 100px"
                          label="Gene exceptions"
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
                          <div>Max. Gene Exceptions (K) - the maximum number of exception genes each pathway is
                            allowed to have. This means the number of nodes that are allowed to be taken into
                            consideration that are not already contained in the seeds to construct maximum connected
                            subnetworks (key pathways).
                          </div>
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
                          <span>Defines a penalty for hubs and preferring lower network-degree nodes (0 = no penalty)</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div style="display: flex; justify-content: flex-start">
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
                          <span>Enables the generation of multiple Steiner Trees with the same parameters and the combination of the individual results.</span>
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
                          <span>Number of individual trees to be constructed.</span>
                        </v-tooltip>
                      </template>
                    </v-slider>
                  </div>
                  <div>
                    <v-slider
                      hide-details
                      class="align-center"
                      v-model="getAlgorithmModels().maxit"
                      min="1"
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
    socketEvent: String,
    goal: {
      type: String,
      default: "module_identification",
    },
    flat: {
      default: false,
      type: Boolean,
    },
    header: {
      default: true,
      type: Boolean,
    },
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
      showDescription: false,
      exprIds: undefined,
      tissues: undefined,
      tissueFilter: "all",
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
          descType: "Description",
          description: "DIAMOnD stands for DIseAse Module Detection and it iteratively identifies the best \"addition\" to the module by calculating the <i>connectivity significance</i> of all nodes connected to at least one of the nodes within the current selection. It was developed and evaluated on the known network topologies of 70 complex disorders. The approach aims to not over-prefer high-degree nodes in general but only such those that are highly connected to the input, or by each iteration extended, node list.",
          link: "https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1004120",
          docu_link:"https://docs.google.com/document/d/1BGp0wovJk_ERonojc6s9XQ7W2sV_Bgb-ljtjI7yj2bg/edit?tab=t.0#heading=h.3dy6vkm",
          models: {
            nModel: 200,
            alphaModel: 1,
            pModel: -3
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
            exprIDType: 'entrez',
            lg: [10, 15]
          },
          descType: "Abstract",
          description: "BiCoN (Biclustering Constrained by Networks) enriches general expression data-based biclustering of genes and patients/conditions by gene interaction information. It \"restricts biclusters to functionally related genes connected in molecular interaction networks\" while \"maximizing the expression difference between two subgroups of patients\". For the module, both clusters of genes are seen as relevant under that condition and thus combined in the resulting module.",
          link: "https://biomedical-big-data.de/publication/lazareva-2020-bicon/",
          docu_link: "https://docs.google.com/document/d/1BGp0wovJk_ERonojc6s9XQ7W2sV_Bgb-ljtjI7yj2bg/edit?tab=t.0#heading=h.lnxbz9"
        }, {
          id: "must", group: "nw", label: "MuST", scores: [], models: {
            hubpenalty: 0,
            multiple: false,
            trees: 10,
            maxit: 10,
          },
          descType: "Description",
          description: "Minimal Steiner trees are generated, meaning a minimal scoring tree is identified that is still only one connected component. This way, minimal cost paths within the network are identified. The reasoning behind using multiple trees is that PPI clusters are highly connected leading to several shortest path options. This means that using only one tree would not really resemble the best solution but be drawn randomly from multiple equal ones. These multiple trees are then used to derive a prioritized list of target nodes the seeds are connected to, where the number of 'visits' of a non-seed node indicates the priority or significance of it.",
          link: "https://en.wikipedia.org/wiki/Steiner_tree_problem"
        },
          {
            id: "kpm", group: "nw", label: "KPM", scores: [], models: {
              // l: 0,
              k: 1,
            },
            descType: "Description",
            description: "The network enricher <b>K</b>ey<b>P</b>athway<b>M</b>iner was developed to identify condition-specific subnetworks. Here, the seeds are used to define a \"condition\" and to let KPM identify such <i>key pathways</i> those are involved in. KPM will try to generate maximally connected subnetworks allowing at most <b><i>K</i></b> additional nodes that are not contained in the seed list.<br><i>KPM might identify unconnected mechanisms!</i>",
            link: "https://exbio.wzw.tum.de/keypathwayminer/",
            docu_link:"https://docs.google.com/document/d/1BGp0wovJk_ERonojc6s9XQ7W2sV_Bgb-ljtjI7yj2bg/edit?tab=t.0#heading=h.3dy6vkm",
          },
          {
            id: "domino",
            group: "nw",
            label: "DOMINO",
            scores: []
            ,
            descType: "Description",
            description: "<b>D</b>iscovery <b>o</b>f <b>M</b>odules <b>I</b>n <b>N</b>etworks using <b>O</b>mic is an active module identification tool that aims to resolve the issue of by chance over-representation of specific GO terms and was validated to yield a high rate of empirically significant GO terms. DOMINO uses the seeds as \"active nodes\" and based on those identifies \"disjoint connected subnetworks\" with an over-representation of active nodes. For this first all disjoint but highly connected subnetworks are identified, before either removing this subnetwork from the list of potential results or repartitioning it and keeping it. The final list of subnetworks is returned as a result.<br><i>DOMINO might identify unconnected mechanisms!</i>",
            link: "https://www.embopress.org/doi/full/10.15252/msb.20209593",
            docu_link:"https://docs.google.com/document/d/1BGp0wovJk_ERonojc6s9XQ7W2sV_Bgb-ljtjI7yj2bg/edit?tab=t.0#heading=h.3dy6vkm",

          }, {
          id: "robust",
          label: "ROBUST",
          group: "nw",
          descType: "Description",
          description: "ROBUST was developed as a result of the realization that other disease module identification algorithms are not robust to input permutation or not even deterministic overall. It employs \"diverse prize-collecting Steiner trees\" to reproducibly identify key nodes to add to the module.",
          link: "https://github.com/bionetslab/robust",
          docu_link:"https://docs.google.com/document/d/1BGp0wovJk_ERonojc6s9XQ7W2sV_Bgb-ljtjI7yj2bg/edit?tab=t.0#heading=h.3dy6vkm",
          models: {
            initFract: 0.25,
            reductionFactor: 0.9,
            trees: 30,
            threshold: 0.1
          },
          scores: [{
            id: "occs_abs",
            name: "Occs (Abs)",
            order: "descending",
            seed: "n/a"
          }, {
            id: "occs_rel", group: "nw", name: "Occs (%)",
            primary: true, decimal: true, order: "descending", seed: 1
          }]
        }
        ],
    }
  },

  created() {
    if (this.blitz) {
      this.groupModel = "nw"
      this.methodModel = 0
      this.$emit("algorithmSelectedEvent", true)
    }
    this.getExprIDTypes()
    this.getTissueTypes()
  },

  watch: {
    groupModel: function () {
      if (!this.blitz)
        this.methodModel = undefined
    },

    methodModel: function (value) {
      this.$emit("algorithmSelectedEvent", value != null)
    }
  },


  methods: {

    getAlgorithm: function () {
      if (this.methodModel != null) {
        return this.methods.filter(m => m.group === this.groupModel)[this.methodModel]
      }
      return undefined;
    },

    getGroup: function () {
      return this.groupModel
    },

    setMethod: async function (method) {
      method = method.toLowerCase()
      let m = this.methods.filter(m => m.id === method)[0]
      if (m.group === "nw")
        await this.setNWMethod(method)
      else
        await this.setExpMethod(method)
    },

    setNWMethod: async function (method, params) {
      this.groupModel = "nw"
      this.$nextTick(() => {
        let algos = this.methods.filter(m => m.group === this.groupModel)
        for (let i = 0; i < algos.length; i++) {
          if (algos[i].id === method) {
            this.methodModel = i
            return
          }
        }
      })
      if (params)
        this.$nextTick(() => {
          let models = this.getAlgorithmModels()
          Object.keys(models).forEach(key => {
            if (params[key] != null)
              models[key] = params[key]
          })
        })
    },
    setExpMethod: async function (method, params) {
      this.groupModel = "exp"
      this.$nextTick(() => {
        let algos = this.methods.filter(m => m.group === this.groupModel)
        for (let i = 0; i < algos.length; i++) {
          if (algos[i].id === method) {
            this.methodModel = i
            return
          }
        }
      })
      if (params)
        this.$nextTick(() => {
          let models = this.getAlgorithmModels()
          Object.keys(models).forEach(key => {
            if (params[key] != null)
              models[key] = params[key]
          })
        })
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

    getParamString: async function () {
      let params = await this.getParams()
      delete params.nodesOnly
      delete params.addInteractions
      let str = "Tissue="+this.tissueFilter+", "
      Object.keys(params).forEach(key => {
        if (key === "exprData")
          str += "Expression-File=" + this.getAlgorithmModels().exprFile.name
        else
          str += key + "=" + params[key]
        str += ", "
      })
      str = str.substring(0, str.length - 2)
      return str
    },

    getParams: async function () {
      let params = {}
      let method = this.getAlgorithmMethod()
      params.experimentalOnly = this.experimentalSwitch
      params["addInteractions"] = true
      params["nodesOnly"] = false
      if (method === 'bicon') {
        this.$emit("clearSeedsEvent")
        params['lg_min'] = this.getAlgorithmModels().lg[0];
        params['lg_max'] = this.getAlgorithmModels().lg[1];
        //TODO maybe make just dependent on typeID
        params['type'] = "gene"
        params['exprIDType'] = this.getAlgorithmModels().exprIDType
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
        params["threshold"] = this.getAlgorithmModels().threshold;
        params["reductionFactor"] = this.getAlgorithmModels().reductionFactor;
      }
      if (method === 'kpm') {
        Object.keys(this.getAlgorithmModels()).forEach(key => {
          params[key] = this.getAlgorithmModels()[key]
        })
      }
      params['type'] = ["gene", "protein"][this.seedTypeId]
      return params;
    },
    biconFile: function (file) {
      this.getAlgorithmModels().exprFile = file
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
        goal: this.goal,
        params: params,
        tissue: this.tissueFilter
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
        ctx.$emit("jobSubmitted")
      }).catch(console.error)
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

    getExprIDTypes: function () {
      this.$http.get("/getAllowedExpressionIDs").then(response => {
        if (response.data != null)
          return response.data
      }).then(data => {
        this.exprIds = data.map(id => {
          return {id: id}
        })
      }).catch(console.error)
    }
  },
}
</script>

<style scoped>

</style>
