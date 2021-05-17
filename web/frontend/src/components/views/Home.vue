<template>

  <v-expansion-panels accordion>
      <v-expansion-panel>
        <v-expansion-panel-header>
        <span class="expansion-panel-head">
        <v-icon left>fas fa-project-diagram</v-icon>
        Quick Start
          </span>
        </v-expansion-panel-header>
        <v-expansion-panel-content>
          <v-card elevation="0">
            <v-card-text>
              Apply available algorithms on the NeDRex-Web graph to derive gene modules, prioritize drugs given a set of
              genes
              or directly identify reusable drugs. Purpose built step-by-step pipelines assist you in your research.
            </v-card-text>
            <v-card-actions>
              <v-btn>
                <v-icon left>fas fa-angle-double-right</v-icon>
                Start
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-expansion-panel-content>
      </v-expansion-panel>
      <v-expansion-panel>
        <v-expansion-panel-header>
        <span>
           <v-icon left>fas fa-project-diagram</v-icon>
            Guided Start
          </span>
        </v-expansion-panel-header>
        <v-expansion-panel-content>
          <v-card elevation="0">
            <v-card-text>
              Derive a sub-graph by defining start and target nodes. Also the connections may be direct or indirect
              ones,
              allowing the creation of induced edges.
            </v-card-text>
            <v-card-actions>
              <v-btn>
                <v-icon left>fas fa-angle-double-right</v-icon>
                Start
              </v-btn>
            </v-card-actions>
          </v-card>

        </v-expansion-panel-content>
      </v-expansion-panel>
      <v-expansion-panel>
        <v-expansion-panel-header>
        <span>
           <v-icon left>fas fa-project-diagram</v-icon>
          Advanced Start
        </span>
        </v-expansion-panel-header>
        <v-expansion-panel-content>
          <v-card elevation="0">
            <v-card-text>
              Freely define the nodes and edges a target sub-graph should contain. Also specify some node filters and
              additional parameters to further control the network generation.
            </v-card-text>
            <v-card-actions>
              <v-btn>
                <v-icon left>fas fa-angle-double-right</v-icon>
                Start
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-expansion-panel-content>
      </v-expansion-panel>
      <v-expansion-panel>
        <v-expansion-panel-header>
        <span>
          <v-icon left>fas fa-cloud-download-alt</v-icon>
          History
        </span></v-expansion-panel-header>
        <v-expansion-panel-content>
          <v-card elevation="0">
            <v-card-text>
              Browse the list of graphs linked with your user
            </v-card-text>
            <v-card-actions>
              <v-btn>
                <v-icon left>fas fa-angle-double-right</v-icon>
                Show
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-expansion-panel-content>
      </v-expansion-panel>
      <v-expansion-panel>
        <v-expansion-panel-header>
        <span>
          <v-icon left>fas fa-cloud-download-alt</v-icon>
          Load GraphID
        </span></v-expansion-panel-header>
        <v-expansion-panel-content>
          <v-card elevation="0">
            <v-card-text>
              Load an already created graph using its unique graph id:
            </v-card-text>
            <v-card-actions>
              <v-text-field label="GraphID">
                <template v-slot:append-outer>
                  <v-btn>
                    <v-icon left>fas fa-angle-double-right</v-icon>
                    Load
                  </v-btn>
                </template>
              </v-text-field>
            </v-card-actions>
          </v-card>
        </v-expansion-panel-content>
      </v-expansion-panel>
      <v-expansion-panel>
        <v-expansion-panel-header>
        <span>
          <v-icon left>fas fa-cloud-download-alt</v-icon>
          Copy from UserID
        </span></v-expansion-panel-header>
        <v-expansion-panel-content>
          <v-card elevation="0">
            <v-card-text>
              Copy all the graphs created under another userid (maybe in another browser or on another machine) and shows
              the
              newly merged history!
              NIY
            </v-card-text>
            <v-card-actions>
              <v-text-field label="UserID">
                <template v-slot:append-outer>
                  <v-btn>
                    <v-icon left>fas fa-angle-double-right</v-icon>
                    Import
                  </v-btn>
                </template>
              </v-text-field>
            </v-card-actions>
          </v-card>
        </v-expansion-panel-content>
      </v-expansion-panel>
  </v-expansion-panels>
</template>

<script>
export default {
  name: "Home"
}
</script>

<style lang="sass">

.v-expansion-panel-header
  span
    font-size: 14pt

.v-text-field
  width: 70%


</style>
