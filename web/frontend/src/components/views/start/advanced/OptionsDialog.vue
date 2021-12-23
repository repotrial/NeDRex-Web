<template>
  <v-dialog v-model="model" max-width="800px" style="z-index: 1001">
    <v-card v-if="edgeId!=null && edges !=null">
      <v-card-title>Filter the {{ edges.filter(e => e.index === edgeId)[0].label }} edge</v-card-title>
      <v-divider></v-divider>
      <v-card-text style="max-height: 700px; overflow-y: auto">
        <div>
          <v-list>
            <v-list-item v-if="isPathEdge('GeneGeneInteraction') || isPathEdge('ProteinProteinInteraction')">
              <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                <LabeledSwitch label-on="Use experimentally validated"
                               label-off="Use all interactions"
                               v-model="options.experimentalInteraction">
                  <template v-slot:tooltip>
                    <div>Defines if only experimentally validated interaction edges are used.
                    </div>
                  </template>
                </LabeledSwitch>
              </v-list-item-title>
            </v-list-item>
            <v-list-item v-if="isPathEdge('GeneGeneInteraction')">
              <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                <LabeledSwitch label-off="Create internal connections"
                               label-on="Add additional Genes"
                               v-model="options.extendGGI">
                  <template v-slot:tooltip>
                    <div>Defines if edges are just used to connect otherwise defined gene nodes or if these protein nodes are used to connect to additional ones.
                    </div>
                  </template>
                </LabeledSwitch>
              </v-list-item-title>
            </v-list-item>
            <v-list-item v-if="isPathEdge('ProteinProteinInteraction')">
              <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                <LabeledSwitch label-off="Create internal connections"
                               label-on="Add additional Proteins"
                               v-model="options.extendPPI">
                  <template v-slot:tooltip>
                    <div>Defines if edges are just used to connect otherwise defined protein nodes or if these protein nodes are used to connect to additional ones.
                    </div>
                  </template>
                </LabeledSwitch>
              </v-list-item-title>
            </v-list-item>
            <v-list-item v-if="isPathEdge('DisorderHierarchy')">
              <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                <LabeledSwitch label-on="Find parent disorder"
                               label-off="Find subtypes"
                               v-model="options.disorderParents">
                  <template v-slot:tooltip>
                    <div>Defines the direction of the disorder hierarchy edge type.
                    </div>
                  </template>
                </LabeledSwitch>
              </v-list-item-title>
            </v-list-item>
            <v-list-item v-if="isPathEdge('DrugTargetGene') || isPathEdge('DrugTargetProtein')">
              <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                <LabeledSwitch label-on="Only with known Drug-Target action"
                               label-off="All Drug-Target connections"
                               v-model="options.drugTargetsWithAction">
                  <template v-slot:tooltip>
                    <div>If activated, applies filter on gene/protein - drug edges, such that only edges with
                      known actions are used.
                    </div>
                  </template>
                </LabeledSwitch>
              </v-list-item-title>
            </v-list-item>
            <v-list-item
              v-if="isPathEdge('GeneAssociatedWithDisorder') || isPathEdge('ProteinAssociatedWithDisorder')">
              <v-list-item-content style="padding-top: 32px;padding-left: 20px; padding-right: 20px">
                <v-slider
                  hide-details
                  class="align-center"
                  min="0"
                  step="0.01"
                  thumb-size="30"
                  thumb-color="primary"
                  thumb-label="always"
                  v-model="options.disorderAssociationCutoff"
                  max="1"
                >
                  <template v-slot:label>
                    Association Score Cutoff
                    <v-tooltip left>
                      <template v-slot:activator="{ on, attrs }">
                        <a style="text-decoration: none"
                           href="https://www.disgenet.org/help#:~:text=The%20DisGeNET%20score%20for%20GDAs,range%20from%200%20to%201."
                           target="_blank">
                          <v-icon
                            v-bind="attrs"
                            v-on="on"
                            style="margin-top: -3px"> far fa-question-circle
                          </v-icon>
                        </a>
                      </template>
                      <span>A cutoff for the DisGeNet score of how strongly associated the {{
                          isPathEdge('GeneAssociatedWithDisorder') ? "Gene" : "Protein"
                        }} {{
                          (isPathEdge('GeneAssociatedWithDisorder') && isPathEdge('ProteinAssociatedWithDisorder') ? 'and Protein' : '')
                        }} nodes are.<br>
                              <i>Click here to see how this score is defined</i></span>
                    </v-tooltip>
                  </template>
                </v-slider>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </div>
      </v-card-text>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="error" @click="resolve(false)">Reset</v-btn>
        <v-btn color="green darken-1" style="color: white" @click="resolve(true)">Accept</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import LabeledSwitch from "@/components/app/input/LabeledSwitch";

export default {
  name: "OptionsDialog",
  components: {
    LabeledSwitch
  },
  props: {
    edges: Array,
    edgeId: Number,
  },

  data() {
    return {
      model: false,
      options: {
        experimentalInteraction: true,
        disorderParents: false,
        drugTargetsWithAction: false,
        disorderAssociationCutoff: 0,
        extendGGI:false,
        extendPPI:false,
      }
    }
  },
  methods: {
    show: function () {
      this.model = true;
    },

    reset: function (id) {
      if (id == null) {
        this.options = {
          experimentalInteraction: true,
          disorderParents: false,
          drugTargetsWithAction: false,
          disorderAssociationCutoff: 0,
          extendGGI:false,
          extendPPI:false,
        }
      } else {
        let label = this.edges.filter(e => e.index === id)[0].label
        if (label === 'ProteinAssociatedWithDisorder' || label === 'GeneAssociatedWithDisorder') {
          this.options.disorderAssociationCutoff = 0
        }
        if (label === 'GeneGeneInteraction' || label === 'ProteinProteinInteraction') {
          this.options.experimentalInteraction = true
          if(label==='GeneGeneInteraction')
            this.options.extendGGI=false
          if(label==='ProteinProteinInteraction')
            this.options.extendPPI=false
        }
        if (label === 'DisorderHierarchy') {
          this.options.disorderParents = false
        }
        if (label === 'DrugTargetGene' || label === 'DrugTargetProtein') {
          this.options.drugTargetsWithAction = false
        }
      }
    },

    getOptions: function () {
      return this.options;
    },

    resolve: function (state) {
      if (state)
        this.model = false
      else
        this.reset(this.edgeId)
    },

    isPathEdge: function (edgeName) {
      return this.edges.filter(e => e.index === this.edgeId)[0].label === edgeName
    }
  }
}
</script>

<style scoped>

</style>
