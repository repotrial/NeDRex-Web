<template>
  <v-card elevation="3" style="margin:15px">
    <v-list-item @click="show=!show">
      <v-list-item-title>
        <v-icon left>{{ show ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
        Algorithms
      </v-list-item-title>
    </v-list-item>
    <v-container v-show="show">
      <v-row>
        <v-col cols="6">
          <v-select
            v-model="categoryModel"
            :items="categories"
            item-text="label"
            item-value="id"
            label="Algorithm Category"
          >
          </v-select>
        </v-col>
        <v-col cols="6" v-if="categoryModel >-1">
          <v-select
            v-model="methodModel"
            :items="categories[categoryModel].methods"
            item-text="label"
            item-value="id"
            label="Method"
          >
          </v-select>
        </v-col>
      </v-row>
      <v-row
        v-if="categoryModel >-1 && (methodModel ==='diamond'|| methodModel ==='trustrank' || methodModel==='centrality' || methodModel ==='must')">
        <v-col cols="6">
          <v-switch
            label="Use Selection"
            v-model="selectionSwitch"
          >
          </v-switch>
        </v-col>
        <v-col cols="6">
          <v-select
            item-text="label"
            item-value="id"
            :items="[{id:'gene',label:'Gene'},{id:'protein',label:'Protein'}]"
            label="Node"
            v-model="nodeModel"
          >
          </v-select>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-switch
            label="Experimental Interactions only"
            v-model="expSwitch"
          >
          </v-switch>

        </v-col>

      </v-row>
      <v-row v-if="methodModel==='bicon'" style="margin: 0">
        <v-file-input
          v-on:change="onFileSelected"
          show-size
          placeholder="Load an expression file"
          prepend-icon="fas fa-file-medical"
          label="Expression File"
          dense
        >
        </v-file-input>

      </v-row>

      <v-divider></v-divider>
      <v-row>
        <v-col cols="1">
          <v-tooltip right>
            <template v-slot:activator="{ on, attrs }">
              <v-btn icon @click="advanced=!advanced" color="gray" v-bind="attrs"
                     v-on="on" :disabled="methodModel ===undefined">
                <v-icon>
                  {{ advanced ? 'fas fa-ellipsis-v' : 'fas fa-ellipsis-h' }}
                </v-icon>
              </v-btn>
            </template>
            <span>Advanced</span>
          </v-tooltip>
        </v-col>
      </v-row>
      <template v-if="advanced">
        <template v-if="methodModel !==undefined && methodModel!=='must'">
          <v-row>
            <v-col>
              <v-switch v-model="models.advanced.keepNodesOnly" label="Keep only derived Graph"></v-switch>
            </v-col>
            <v-col>
              <v-switch v-model="models.advanced.addInteractions" label="Add new interaction Edges"></v-switch>
            </v-col>
          </v-row>
          <v-divider></v-divider>
        </template>
        <template v-if="methodModel==='bicon'">
          <v-row>
            <v-col>
              <v-range-slider
                hide-details
                class="align-center"
                v-model="models.bicon.lg"
                min="1"
                max="1000"
              >
                <template v-slot:prepend>
                  <v-text-field
                    v-model="models.bicon.lg[0]"
                    class="mt-0 pt-0"
                    type="number"
                    style="width: 60px"
                    label="min"
                  ></v-text-field>
                </template>
                <template v-slot:append>
                  <v-text-field
                    v-model="models.bicon.lg[1]"
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
            </v-col>
          </v-row>

        </template>
        <template v-if="methodModel==='diamond'">
          <v-row>
            <v-col>
              <v-slider
                hide-details
                class="align-center"
                v-model="models.diamond.nModel"
                min="1"
                max="1000"
              >
                <template v-slot:prepend>
                  <v-text-field
                    v-model="models.diamond.nModel"
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
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-slider
                hide-details
                class="align-center"
                v-model="models.diamond.alphaModel"
                min="1"
                max="100"
              >
                <template v-slot:prepend>
                  <v-text-field
                    v-model="models.diamond.alphaModel"
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
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-slider
                hide-details
                class="align-center"
                v-model="models.diamond.pModel"
                min="-100"
                max="0"
              >
                <template v-slot:prepend>
                  <v-text-field
                    prefix="10^"
                    v-model="models.diamond.pModel"
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
            </v-col>
          </v-row>
        </template>
        <template v-if="methodModel==='trustrank'||methodModel==='centrality'">
          <v-row>
            <v-col>
              <v-switch
                label="Only direct Drugs"
                v-model="models.trustrank.onlyDirect"
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
            </v-col>
            <v-col>
              <v-switch
                label="Only approved Drugs"
                v-model="models.trustrank.onlyApproved"
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
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-slider
                hide-details
                class="align-center"
                v-model="models.trustrank.damping"
                step="0.01"
                min="0"
                max="1"
              >
                <template v-slot:prepend>
                  <v-text-field
                    v-model="models.trustrank.damping"
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
            </v-col>
          </v-row>
        </template>
        <template v-if="methodModel==='must'">
          <v-row>
            <v-col>
              <v-slider
                hide-details
                class="align-center"
                v-model="models.must.hubpenalty"
                min="0"
                max="1"
                step="0.01"
              >
                <template v-slot:prepend>
                  <v-text-field
                    v-model="models.must.hubpenalty"
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
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-switch
                label="Multiple"
                v-model="models.must.multiple"
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
            </v-col>
          </v-row>
          <v-row v-show="models.must.multiple">
            <v-col>
              <v-slider
                hide-details
                class="align-center"
                v-model="models.must.trees"
                min="2"
                max="50"
              >
                <template v-slot:prepend>
                  <v-text-field
                    v-model="models.must.trees"
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
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-slider
                hide-details
                class="align-center"
                v-model="models.must.maxit"
                min="0"
                max="20"
              >
                <template v-slot:prepend>
                  <v-text-field
                    v-model="models.must.maxit"
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
            </v-col>
          </v-row>
        </template>
        <v-row>
          <v-col></v-col>
        </v-row>
      </template>

      <v-row>
        <v-col>
          <v-chip outlined color="green"
                  :disabled="isDisabled()"
                  @click="submitAlgorithm">
            Submit
          </v-chip>
        </v-col>
        <v-col>
          <v-chip outlined color="red" @click="resetAlgorithms">
            Reset
          </v-chip>
        </v-col>
      </v-row>
    </v-container>

  </v-card>
</template>

<script>
import Utils from "../../scripts/Utils";

export default {
  name: "Algorithms",
  data() {
    return {
      show: false,
      categoryModel: -1,
      methodModel: "",
      nodeModel: undefined,
      selectionSwitch: false,
      expSwitch: true,
      advanced: false,
      models: {
        advanced: {
          keepNodesOnly: true,
          addInteractions: true
        },
        diamond: {
          nModel: 200,
          alphaModel: 1,
          pModel: 0
        },
        bicon: {
          exprFile: undefined,
          lg: [10, 15]
        },
        trustrank: {
          onlyApproved: true,
          onlyDirect: true,
          damping: 0.85
        },
        centrality: {
          onlyApproved: true,
          onlyDirect: true,
          damping: 0.85
        },
        must: {
          hubpenalty: 0,
          multiple: false,
          trees: 10,
          maxit: 10,
        }
      },
      categories:
        [{
          id: 0,
          label: "Disease Modules",
          methods: [{id: "diamond", label: "DIAMOnD"}, {id: "bicon", label: "BiCoN"}, {id: "must", label: "MuST"}]
        }, {
          id: 1,
          label: "Drug Ranking",
          methods: [{id: "trustrank", label: "TrustRank"}, {id: "centrality", label: "Centrality"}]
        }]
    }
  },
  methods: {
    onFileSelected: function (file) {
      this.models.bicon.exprFile = file
    },
    submitAlgorithm: function () {
      let params = {}
      params.experimentalOnly = this.expSwitch

      params["addInteractions"] = this.models.advanced.addInteractions
      params["nodesOnly"] = this.models.advanced.keepNodesOnly
      if (this.methodModel === 'bicon') {
        params['lg_min'] = this.models.bicon.lg[0];
        params['lg_max'] = this.models.bicon.lg[1];
        params['type'] = "gene"
        Utils.readFile(this.models.bicon.exprFile).then(content => {
          params['exprData'] = content
          this.$emit('executeAlgorithmEvent', this.methodModel, params)
        })
        return
      }

      if (this.methodModel === 'diamond') {
        params['n'] = this.models.diamond.nModel;
        params['alpha'] = this.models.diamond.alphaModel;
        params['pcutoff'] = this.models.diamond.pModel;
      }

      if (this.methodModel === 'trustrank' || this.methodModel === 'centrality') {
        params['direct'] = this.models[this.methodModel].onlyDirect;
        params['approved'] = this.models[this.methodModel].onlyApproved;
        params['damping'] = this.models[this.methodModel].damping;
      }
      if (this.methodModel === 'must') {
        params["penalty"] = this.models.must.hubpenalty;
        params["multiple"] = this.models.must.multiple
        params["trees"] = this.models.must.trees
        params["maxit"] = this.models.must.maxit
      }
      params.selection = this.selectionSwitch
      params['type'] = this.nodeModel
      this.$emit('executeAlgorithmEvent', this.methodModel, params)
    },


    getColoring: function (type, name) {
      return Utils.getColoring(this.metagraph, type, name)
    },
    getNodeNames: function (type) {
      return Utils.getNodes(this.metagraph, type)
    },
    isDisabled: function () {
      if (this.methodModel === undefined || this.methodModel.length === 0)
        return true;
      if (this.methodModel === "bicon")
        return this.models.bicon.exprFile === undefined
      return (this.nodeModel === undefined || this.nodeModel.length === 0)

    },
    resetAlgorithms: function () {
      this.nodeModel = undefined
      this.selectionSwitch = false
      this.expSwitch = true
      this.categoryModel = undefined
      this.methodModel = undefined
      this.categories.methodModel = undefined
      this.advanced = false;
      this.show = false
      this.models.diamond.nModel = 200
      this.models.diamond.alphaModel = 1
      this.models.diamond.pModel = 0
      this.models.bicon.exprFile = undefined
      this.models.bicon.lg = [10, 15]
      this.models.trustrank.onlyApproved = true
      this.models.trustrank.onlyDirect = true
      this.models.trustrank.damping = 0.85
      this.models.centrality.onlyApproved = true
      this.models.centrality.onlyDirect = true
      this.models.centrality.damping = 0.85
      this.models.must.hubpenalty = 0
      this.models.must.multiple = false;
      this.models.must.trees = 10;
      this.models.must.maxit = 10;
    },

    formatTime: function (timestamp) {
      return Utils.formatTime(timestamp)
    },
  }


}
</script>

<style scoped>

</style>
