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
        v-if="categoryModel >-1 && methodModel ==='diamond'">
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
        </template>
        <v-row>
          <v-col></v-col>
        </v-row>
      </template>
      <v-row>
        <v-col>
          <v-chip outlined color="green"
                  :disabled="nodeModel === undefined || nodeModel.length ===0"
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
  // props:{
  //   show:Boolean,
  // },

  data() {
    return {
      show: false,
      categoryModel: -1,
      methodModel: "",
      nodeModel: undefined,
      selectionSwitch: false,
      advanced: false,
      models: {
        diamond: {
          nModel: 200,
          alphaModel: 1
        }
      },
      categories: [{
        id: 0,
        label: "Disease Modules",
        methods: [{id: "diamond", label: "DIAMOnD"}, {id: "bicon", label: "BiCoN"}]
      }, {
        id: 1,
        label: "Drug Ranking",
        methods: [{id: "trustrank", label: "TrustRank"}, {id: "centrality", label: "Centrality"}]
      }]
    }
  },
  methods: {

    submitAlgorithm: function () {
      let params = {}
      if (this.methodModel === 'diamond') {
        params['type'] = this.nodeModel
        params['n'] = this.models.diamond.nModel;
        params['alpha'] = this.models.diamond.alphaModel;
      }
      if (this.methodModel === 'diamond' || this.methodModel === 'bicon')
        params.selection = this.selectionSwitch
      this.$emit('executeAlgorithmEvent', this.methodModel, params)
    },

    getColoring: function (type, name) {
      return Utils.getColoring(this.metagraph, type, name)
    },
    getNodeNames: function (type) {
      return Utils.getNodes(this.metagraph, type)
    },

    resetAlgorithms: function () {
      this.nodeModel = undefined
      this.selectionSwitch = false
      this.categoryModel = undefined
      this.methodModel = undefined
      this.categories.methodModel = undefined
      this.advanced = false;
      this.show = false
      this.models.diamond.nModel = 200
      this.models.diamond.alphaModel = 1
    },

    formatTime: function (timestamp) {
      return Utils.formatTime(timestamp)
    },
  }


}
</script>

<style scoped>

</style>
