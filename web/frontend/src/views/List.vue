<template>
  <div>
    <v-container>
      <v-card style="margin:5px">
        <v-container>
          <v-row>
            <v-col>
              <v-switch
                v-model="showAllLists"
                label="Show all Items"
              ></v-switch>
            </v-col>
            <v-col>
              <v-chip
                v-on:click="extendGraph()"
                class="pa-3"
              >Extend Graph
              </v-chip>
            </v-col>
            <v-col>
              <v-chip
                v-on:click="collapseGraph()"
                class="pa-3"
              >Collapse Graph
              </v-chip>
            </v-col>
            <v-col>
              <v-chip
                icon
                v-on:click="loadSelection"
              >
                Load Selection
                <v-icon>fas fa-project-diagram</v-icon>
              </v-chip>
            </v-col>
          </v-row>
        </v-container>
      </v-card>
      <v-card style="margin:5px">
        <v-card-title v-on:mouseenter="nodeOptionHover=true" v-on:mouseleave="nodeOptionHover=false">Nodes
          <v-tooltip
            right>
            <template v-slot:activator="{ on, attrs }">
              <v-icon
                v-show="nodeOptionHover"
                size="17px"
                color="primary"
                dark
                v-bind="attrs"
                v-on="on"
                v-on:click="nodeOptions"
              >
                fas fa-cog
              </v-icon>
            </template>
            <span>options</span>
          </v-tooltip>
        </v-card-title>
        <i v-if="!update.nodes && Object.keys(nodes).length === 0">no node entries</i>
        <template v-if="Object.keys(nodes).length>0">
          <v-tabs next-icon="mdi-arrow-right-bold-box-outline"
                  prev-icon="mdi-arrow-left-bold-box-outline"
                  show-arrows
                  v-model="nodeTab"
          >
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab v-for="node in Object.keys(nodes)" :key="node">
              {{ node }}
            </v-tab>
          </v-tabs>
          <v-tabs-items>
            <v-data-table
              ref="nodeTab"
              fixed-header
              dense
              class="elevation-1"
              :headers="headers('nodes',Object.keys(nodes)[nodeTab])"
              :items="showAllLists ? nodes[Object.keys(nodes)[nodeTab]]: filterSelected(nodes[Object.keys(nodes)[nodeTab]])"
              item-key="id"
              :search="filters.nodes.query"
              :custom-filter="filterNode"
            >
              <template v-slot:top>
                <v-card
                  elevation="1"
                  outlined
                  style="margin:20px"
                >
                  <v-card-title style="font-size: large">
                    <v-btn
                      icon
                      @click="selectionMenu.nodes=!selectionMenu.nodes"
                    >
                      <v-icon v-if="selectionMenu.nodes" style="font-size: inherit">fas fa-minus-square</v-icon>
                      <v-icon v-else style="font-size: inherit">fas fa-plus-square</v-icon>
                    </v-btn>
                    Selection Toolbox
                  </v-card-title>
                  <v-divider></v-divider>
                  <v-container v-show="selectionMenu.nodes">
                    <v-row>
                      <v-col
                        cols="2"
                        v-show="!filters.nodes.suggestions"
                      >
                        <v-select
                          :items="headerNames('nodes',Object.keys(nodes)[nodeTab])"
                          label="Attribute"
                          v-model="filters.nodes.attribute.name"
                          outlined
                        ></v-select>
                      </v-col>
                      <v-col
                        cols="1"
                        v-if="!filters.nodes.suggestions"
                      >
                        <v-select
                          v-model="filters.nodes.attribute.operator"
                          :disabled="filters.nodes.attribute.name === undefined || filters.nodes.attribute.name == null || filters.nodes.attribute.name.length ===0"
                          :items="operatorNames('nodes',Object.keys(nodes)[nodeTab],filters.nodes.attribute.name)"
                          label="Operator"
                          outlined
                        ></v-select>
                      </v-col>
                      <v-col
                        :cols="(filters.nodes.suggestions)?10:7"
                      >
                        <v-text-field
                          clearable
                          v-show="!filters.nodes.suggestions"
                          v-model="filters.nodes.query"
                          label="Query (case sensitive)"
                          class="mx-4"
                        ></v-text-field>
                        <v-autocomplete
                          clearable
                          :search-input.sync="findNodeSuggestions"
                          v-show="filters.nodes.suggestions"
                          :loading="suggestions.nodes.loading"
                          :items="suggestions.nodes.data"
                          v-model="filterNodeModel"
                          label="Query (case sensitive)"
                          class="mx-4"
                        >
                          <template v-slot:item="{ item }" v-on:select="suggestions.nodes.chosen=item">
                            <v-list-item-avatar
                            >
                              <v-icon v-if="item.type==='DOMAIN_ID'">fas fa-fingerprint</v-icon>
                              <v-icon v-if="item.type==='DISPLAY_NAME' || item.type==='SYMBOLS'">fas fa-tv</v-icon>
                              <v-icon v-if="item.type==='ICD10'">fas fa-disease</v-icon>
                              <v-icon v-if="item.type==='SYNONYM'">fas fa-sync</v-icon>
                              <v-icon v-if="item.type==='IUPAC'">mdi-molecule</v-icon>
                              <v-icon v-if="item.type==='ORIGIN'">fas fa-dna</v-icon>
                              <v-icon v-if="item.type==='DESCRIPTION' || item.type==='COMMENTS'">fas fa-info</v-icon>
                              <v-icon v-if="item.type==='INDICATION'">fas fa-pills</v-icon>
                              <v-icon v-if="item.type==='TYPE' || item.type==='GROUP' || item.type==='CATEGORY'">fas
                                fa-layer-group
                              </v-icon>
                            </v-list-item-avatar>
                            <v-list-item-content>
                              <v-list-item-title v-text="item.text"></v-list-item-title>
                              <v-list-item-subtitle
                                v-text="item.type"></v-list-item-subtitle>
                            </v-list-item-content>
                            <v-list-item-action>
                              <v-chip>
                                {{ item.ids.length }}
                              </v-chip>
                            </v-list-item-action>
                          </template>
                        </v-autocomplete>
                      </v-col>
                      <v-col
                        cols="2"
                      >
                        <v-switch
                          label="Suggestions"
                          v-model="filters.nodes.suggestions"
                          v-on:click="toggleSuggestions('nodes')"
                        >
                        </v-switch>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col>
                        <v-chip
                          v-on:click="selectAll('nodes',nodeTab)"
                          class="pa-3"
                        >Select All Nodes
                        </v-chip>
                      </v-col>
                      <v-col>
                        <v-chip
                          v-on:click="selectEdges()"
                          class="pa-3"
                        >Select Needed Edges
                        </v-chip>
                      </v-col>
                      <v-col>
                        <v-chip
                          v-on:click="deselectAll('nodes',nodeTab)"
                          class="pa-3"
                        >Unselect All Nodes
                        </v-chip>
                      </v-col>
                    </v-row>
                  </v-container>
                </v-card>
              </template>
              <template v-slot:item.selected="{item}">
                <v-checkbox
                  :key="item.id"
                  v-model="item.selected"
                  hide-details
                  primary
                ></v-checkbox>
              </template>
              <template v-slot:item.displayName="{item}">
                <v-tooltip right>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon
                      color="primary"
                      dark
                      v-bind="attrs"
                      v-on="on"
                      v-on:click="nodeDetails(item.id)"
                    >
                      fas fa-info-circle
                    </v-icon>
                    {{ item.displayName }}
                  </template>
                  <span>id:{{ item.id }}</span>
                </v-tooltip>
              </template>
            </v-data-table>
          </v-tabs-items>
        </template>
      </v-card>

      <v-card style="margin:5px">
        <v-card-title v-on:mouseenter="edgeOptionHover=true" v-on:mouseleave="edgeOptionHover=false">Edges
          <v-tooltip
            right>
            <template v-slot:activator="{ on, attrs }">
              <v-icon
                v-show="edgeOptionHover"
                size="17px"
                color="primary"
                dark
                v-bind="attrs"
                v-on="on"
                v-on:click="edgeOptions"
              >
                fas fa-cog
              </v-icon>
            </template>
            <span>options</span>
          </v-tooltip>
        </v-card-title>
        <i v-if="Object.keys(edges).length === 0">no edge entries</i>
        <template v-if="Object.keys(edges).length>0">
          <v-tabs
            next-icon="mdi-arrow-right-bold-box-outline"
            prev-icon="mdi-arrow-left-bold-box-outline"
            show-arrows
            v-model="edgeTab"
          >
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab v-for="edge in Object.keys(edges)" :key="edge">
              {{ edge }}
            </v-tab>
          </v-tabs>
          <v-tabs-items>
            <v-data-table
              ref="edgeTab"
              fixed-header
              dense
              class="elevation-1"
              :headers="headers('edges',Object.keys(edges)[edgeTab])"
              :items="showAllLists ? edges[Object.keys(edges)[edgeTab]] : filterSelected(edges[Object.keys(edges)[edgeTab]])"
              :search="filters.edges.query"
              :custom-filter="filterEdge"
              item-key="id"
            >
              <template v-slot:top>
                <v-card
                  elevation="1"
                  outlined
                  style="margin:20px"
                >
                  <v-card-title style="font-size: large">
                    <v-btn
                      icon
                      @click="selectionMenu.edges=!selectionMenu.edges"
                    >
                      <v-icon v-if="selectionMenu.edges" style="font-size: inherit">fas fa-minus-square</v-icon>
                      <v-icon v-else style="font-size: inherit">fas fa-plus-square</v-icon>
                    </v-btn>
                    Selection Toolbox
                  </v-card-title>
                  <v-divider></v-divider>
                  <v-container
                    v-show="selectionMenu.edges">
                    <v-row>
                      <v-col
                        cols="2"
                      >
                        <v-select
                          :items="headerNames('edges',Object.keys(edges)[edgeTab])"
                          label="Attribute"
                          v-model="filters.edges.attribute.name"
                          outlined
                        ></v-select>
                      </v-col>
                      <v-col
                        cols="1"
                        v-if="!filters.edges.suggestions"
                      >
                        <v-select
                          v-model="filters.edges.attribute.operator"
                          :items="operatorNames('edges',Object.keys(edges)[edgeTab],filters.edges.attribute.name)"
                          label="Operator"
                          outlined
                        ></v-select>
                      </v-col>
                      <v-col
                        cols="7"
                      >
                        <v-text-field
                          clearable
                          v-model="filters.edges.query"
                          label="Query (case sensitive)"
                          class="mx-4"
                        ></v-text-field>
                      </v-col>
                      <v-col
                        cols="2"
                      >
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col>
                        <v-chip
                          v-on:click="selectAll('edges',edgeTab)"
                          class="pa-3"
                        >Select All Edges
                        </v-chip>
                      </v-col>
                      <v-col>
                        <v-chip
                          v-on:click="deselectAll('edges',edgeTab)"
                          class="pa-3"
                        >Unselect All Edges
                        </v-chip>
                      </v-col>
                      <v-col>
                        <v-chip
                          v-on:click="selectNodes()"
                          class="pa-3"
                        >Add Needed Nodes
                        </v-chip>
                      </v-col>
                    </v-row>
                  </v-container>
                </v-card>
              </template>
              <template v-slot:item.selected="{item}">
                <v-checkbox
                  :key="item.id"
                  v-model="item.selected"
                  hide-details
                  primary
                ></v-checkbox>
              </template>
              <template v-slot:item.edgeid="{item}">
                <template v-if="item.sourceId !== undefined">
                  <v-icon
                    color="primary"
                    dark
                    v-on:click="edgeDetails(item.sourceId, item.targetId)"
                  >
                    fas fa-info-circle
                  </v-icon>
                  {{ item.sourceId }}
                  <v-icon>fas fa-long-arrow-alt-right</v-icon>
                  {{ item.targetId }}
                </template>
                <template v-else>
                  <v-icon
                    color="primary"
                    dark
                    v-on:click="edgeDetails(item.idOne, item.idTwo)"
                  >
                    fas fa-info-circle
                  </v-icon>
                  {{ item.idOne }}
                  <v-icon>fas fa-arrows-alt-h</v-icon>
                  {{ item.idTwo }}
                </template>
              </template>
            </v-data-table>
          </v-tabs-items>
        </template>
      </v-card>
    </v-container>
    <v-dialog
      v-model="extension.show"
      persistent
      max-width="500"
    >
      <v-card v-if="extension.show">
        <v-card-title class="headline">
          Add more edges to our current graph
        </v-card-title>
        <v-card-text>Adjust the attributes of the general item tables.
        </v-card-text>
        <v-divider></v-divider>
        <v-tabs-items>
          <v-list>
            <v-list-item v-for="attr in extension.edges" :key="attr.name">
              <v-switch v-model="attr.selected" :label="attr.name" :disabled="attr.disabled">
              </v-switch>
            </v-list-item>
          </v-list>
        </v-tabs-items>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="green darken-1"
            text
            @click="extensionDialogResolve(false)"
          >
            Cancel
          </v-btn>
          <v-btn
            color="green darken-1"
            text
            @click="extensionDialogResolve(true)"
          >
            Apply
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog
      v-model="collapse.show"
      persistent
      max-width="500"
    >
      <v-card v-if="collapse.show" ref="dialog">
        <v-card-title class="headline">
          Collapse Configuration
        </v-card-title>
        <v-card-text>Select a node and two edges which should be replaced by a new edge.
        </v-card-text>
        <v-divider></v-divider>
        <v-container>
          <v-row>
            <v-col>
              <v-switch
                v-model="collapse.self.selected"
                :disabled="collapse.self.disabled"
                @click="isDisabled()"
                label="Allow Single Edge"
              ></v-switch>
            </v-col>
          </v-row>
          <v-divider></v-divider>
          <v-row>
            <v-col>
              <v-list>
                <v-list-item>
                  <v-card-title>Nodes</v-card-title>
                </v-list-item>
                <v-list-item v-for="attr in collapse.nodes" :key="attr.name">
                  <v-switch v-model="attr.selected" @click="isDisabled('nodes',attr.name)" :label="attr.name"
                            :disabled="attr.disabled">
                  </v-switch>
                </v-list-item>
              </v-list>
            </v-col>
            <v-col>
              <v-list>
                <v-list-item>
                  <v-card-title>Edges</v-card-title>
                </v-list-item>
                <v-list-item v-for="attr in collapse.edges" :key="attr.name">
                  <v-switch v-model="attr.selected" @click="isDisabled('edges',attr.name)" :label="attr.name"
                            :disabled="attr.disabled">
                  </v-switch>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
        </v-container>
        <v-divider></v-divider>
        <v-text-field
          label="Edge Name"
          outlined
          v-model="collapse.edgeName">
        </v-text-field>
        <v-card-text v-if="collapse.accept">
          <template v-if="!collapse.self.selected">
            Generate edge by merging {{ collapse.edge1 }} and
            {{ collapse.edge2 }} (via {{ collapse.nodes.filter(n => n.selected)[0].name }})?
          </template>
          <template v-if="collapse.self.selected"> Generate {{ collapse.edgeName }} edge by looping via
            {{ collapse.edge1 }} and {{ collapse.nodes.filter(n => n.selected)[0].name }}?
          </template>
        </v-card-text>
        <v-container v-if="collapse.accept">
          <v-row>
            <v-col v-if="!collapse.self.selected">
              <v-chip @click="switchCollapseEdges">Switch edges</v-chip>
            </v-col>
            <v-col>
              <v-switch
                v-model="collapse.keep"
              >
                <template v-slot:label>
                  Keep
                  <v-tooltip
                    right>
                    <template v-slot:activator="{ on, attrs }">
                      <v-icon
                        size="17px"
                        color="primary"
                        dark
                        v-bind="attrs"
                        v-on="on"
                      >
                        fas fa-question-circle
                      </v-icon>
                    </template>
                    <span>Activate to keep collapsed entities even if there are no other entities connected.</span>
                  </v-tooltip>
                </template>
              </v-switch>
            </v-col>
          </v-row>
        </v-container>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="green darken-1"
            text
            @click="collapseDialogResolve(false)"
          >
            Cancel
          </v-btn>
          <v-btn
            color="green darken-1"
            text
            :disabled="!collapse.accept"
            @click="collapseDialogResolve(true)"
          >
            Apply
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog
      v-model="optionDialog"
      persistent
      max-width="500"
    >
      <v-card v-if="options !== undefined && options.type !== undefined">
        <v-card-title class="headline">
          Organize {{ options.title }} Attributes
        </v-card-title>
        <v-card-text>Adjust the attributes of the general item tables.
        </v-card-text>
        <v-divider></v-divider>
        <template v-if="Object.keys(options.type).length>0">
          <v-tabs v-model="optionTab">
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab v-for="name in Object.keys(options.attributes)" :key="name">
              {{ Object.keys(nodes)[name] }}
            </v-tab>
          </v-tabs>
          <v-tabs-items>
            <v-list>
              <v-list-item v-for="attr in options.attributes[optionTab]" :key="attr.name">
                <v-switch v-model="attr.selected" :label="attr.name" :disabled="(attr.name === 'id')">
                </v-switch>
              </v-list-item>
            </v-list>
          </v-tabs-items>
        </template>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="green darken-1"
            text
            @click="dialogResolve(false)"
          >
            Cancel
          </v-btn>
          <v-btn
            color="green darken-1"
            text
            @click="dialogResolve(true)"
          >
            Apply
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog
      v-model="optionDialog"
      persistent
      max-width="500"
    >
      <v-card v-if="options !== undefined && options.type !== undefined">
        <v-card-title class="headline">
          Organize {{ options.title }} Attributes
        </v-card-title>
        <v-card-text>Adjust the attributes of the general item tables.
        </v-card-text>
        <v-divider></v-divider>
        <template v-if="Object.keys(options.type).length>0">
          <v-tabs v-model="optionTab">
            <v-tabs-slider color="blue"></v-tabs-slider>
            <v-tab v-for="name in Object.keys(options.attributes)" :key="name">
              {{ Object.keys(nodes)[name] }}
            </v-tab>
          </v-tabs>
          <v-tabs-items>
            <v-list>
              <v-list-item v-for="attr in options.attributes[optionTab]" :key="attr.name">
                <v-switch v-model="attr.selected" :label="attr.name" :disabled="(attr.name === 'id')">
                </v-switch>
              </v-list-item>
            </v-list>
          </v-tabs-items>
        </template>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="green darken-1"
            text
            @click="dialogResolve(false)"
          >
            Cancel
          </v-btn>
          <v-btn
            color="green darken-1"
            text
            @click="dialogResolve(true)"
          >
            Apply
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog
      v-model="selectionDialog.show"
      persistent
      max-width="500"
    >
      <v-card v-if="selectionDialog.show && selectionDialog.type !== undefined">
        <v-card-title class="headline">
          Advanced {{ selectionDialog.title }} Selection
        </v-card-title>
        <v-card-text>Adjust the attributes of the general item tables.
        </v-card-text>
        <v-card-subtitle>Seeds ({{ selectionDialog.type }})</v-card-subtitle>
        <v-divider></v-divider>
        <v-tabs-items>
          <v-list>
            <v-list-item v-for="attr in selectionDialog.seeds" :key="attr.name">
              <v-switch v-model="attr.select"
                        :label="attr.name+ ' ('+countSelected(selectionDialog.type ,attr.name)+')'">
              </v-switch>
            </v-list-item>
          </v-list>
        </v-tabs-items>

        <v-card-subtitle>Targets ({{ selectionDialog.type === "nodes" ? "edges" : "nodes" }})</v-card-subtitle>
        <v-divider></v-divider>
        <v-tabs-items>
          <v-list>
            <v-list-item v-for="attr in selectionDialog.targets" :key="attr.name">
              <v-switch v-model="attr.select"
                        :label="attr.name + ' ('+countSelected(selectionDialog.type === 'nodes' ? 'edges' : 'nodes' ,attr.name)+')'">
              </v-switch>
            </v-list-item>
          </v-list>
        </v-tabs-items>

        <v-card-subtitle>Options</v-card-subtitle>
        <v-container>
          <v-row>
            <v-col v-show="selectionDialog.type === 'nodes'">
              <v-switch v-model="selectionDialog.extendSelect" label=''>
                <template v-slot:label>
                  extend seed selection
                  <v-tooltip
                    right>
                    <template v-slot:activator="{ on, attrs }">
                      <v-icon
                        size="17px"
                        color="primary"
                        dark
                        v-bind="attrs"
                        v-on="on"
                      >
                        fas fa-question-circle
                      </v-icon>
                    </template>
                    <span>{{ selectionDialog.extendDescription }}</span>
                  </v-tooltip>
                </template>
              </v-switch>
            </v-col>
          </v-row>
        </v-container>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="green darken-1"
            text
            @click="selectionDialogResolve(false)"
          >
            Cancel
          </v-btn>
          <v-btn
            color="green darken-1"
            text
            @click="selectionDialogResolve(true)"
          >
            Apply
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
export default {
  name: "List",
  nodes: {},
  edges: {},
  metagraph: {},
  entityGraph: {},
  attributes: {},
  nodeTab: undefined,
  edgeTab: undefined,
  gid: undefined,

  created() {
    this.nodes = {}
    this.attributes = {}
    this.edges = {}
    this.backup = {nodes: {}, edges: {}}
    this.init()
  },

  data() {
    return {
      edges: this.edges,
      nodes: this.nodes,
      nodeTab: this.nodeTab,
      edgeTab: this.edgeTab,
      selectAllModel: {nodes: {}, edges: {}},
      nodepage: {},
      showAllLists: true,
      nodeOptionHover: false,
      edgeOptionHover: false,
      optionDialog: false,
      selectionDialog: {
        show: false,
        type: "",
        title: "",
        seeds: [],
        targets: [],
        extendDescription: "Adding all edges of the seed categories, which contain at least one node. The 'missing' nodes are also added to selection.",
        extendSelect: false,
        extendScope: false
      },
      options: {},
      collapse: {
        show: false,
        nodes: [],
        self: {selected: false, disabled: false},
        edges: [],
        edgeName: "",
        accept: false,
        edge1: "",
        edge2: "",
        keep: false,
      },
      extension: {show: false, nodes: [], edges: []},
      optionTab: undefined,
      update: {nodes: false, edges: false},
      findNodeSuggestions: null,
      selectionMenu: {
        nodes: false,
        edges: false,
      },
      suggestions: {
        nodes: {model: undefined, data: [], loading: false, chosen: undefined},
        edges: {model: undefined, data: [], loading: false, chosen: undefined}
      },
      filters: {
        nodes: {query: '', attribute: {}, suggestions: false},
        edges: {query: '', attribute: {}, suggestions: false}
      },
      filterNodeModel: null,
    }
  },
  watch: {
    filterNodeModel: function (val) {
      this.applySuggestion(val)
    },
    // nodeTab: function (val) {
    //   this.applySuggestion(undefined)
    //   this.findNodeSuggestions = null;
    // },
    findNodeSuggestions: function (val) {
      let type = "nodes";
      let name = Object.keys(this.nodes)[this.nodeTab];
      if (this.suggestions[type].chosen !== undefined)
        return
      this.suggestions[type].loading = true;
      this.suggestions[type].data = []
      this.$nextTick().then(() => {
        this.$http.post("getSuggestions", {
          gid: this.gid,
          type: type,
          name: name,
          query: val,
        }).then(response => {
          if (response.data !== undefined) {
            return response.data
          }
        }).then(data => {
          this.suggestions[type].data = data.suggestions;
        }).catch(err =>
          console.log(err)
        ).finally(() =>
          this.suggestions[type].loading = false
        )
      }).catch(err =>
        console.log(err))
    },
  },
  methods: {
    init: function () {
      if (this.gid === undefined)
        return
      this.$http.get("/getConnectionGraph?gid=" + this.gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.entityGraph = data
      }).catch(err => console.log(err))
    },
    setMetagraph: function (metagraph) {
      this.metagraph = metagraph;
    },
    filterSelected(items) {
      return items.filter(item => item.selected)
    },
    clearLists: function () {
      this.loadList(undefined)
      this.selectAllModel = {nodes: {}, edges: {}}
      this.nodepage = {}
    }
    ,
    loadList: function (data) {
      this.attributes = {};
      this.edges = {};
      this.nodes = {};
      this.nodeTab = 0
      this.edgeTab = 0
      this.filters.edges.query = ''
      this.filters.nodes.query = ''
      if (data !== undefined) {
        this.attributes = data.attributes;
        for (let ni = 0; ni < Object.keys(this.attributes.nodes).length; ni++) {
          this.selectAllModel.nodes[ni] = false
          this.nodepage[ni] = 0
        }
        for (let ei = 0; ei < Object.keys(this.attributes.edges).length; ei++) {
          this.selectAllModel.edges[ei] = false
        }
        this.edges = data.edges;
        this.nodes = data.nodes;
        Object.keys(this.nodes).forEach(node => {
          for (let idx in this.nodes[node]) {
            this.nodes[node][idx]["selected"] = false;
          }
        })
        Object.keys(this.edges).forEach(edge => {
          for (let idx in this.edges[edge]) {
            this.edges[edge][idx]["selected"] = false;
          }
        })
        this.nodeTab = 0
        this.edgeTab = 0
      }
    }
    ,
    countSelected: function (type, name) {
      return this[type][name].filter(item => item.selected).length
    },
    applySuggestion: function (val) {
      if (val !== undefined) {
        let nodes = this.nodes[Object.keys(this.nodes)[this.nodeTab]]
        this.suggestions.nodes.chosen = this.suggestions.nodes.data.filter(item => item.value === val).flatMap(item => item.ids);
        this.backup.nodes[this.nodeTab] = nodes
        this.nodes[Object.keys(this.nodes)[this.nodeTab]] = nodes.filter((i, k) => this.suggestions.nodes.chosen.indexOf(i.id) !== -1)
        this.filters.nodes.query = val
      } else {
        this.nodes[Object.keys(this.nodes)[this.nodeTab]] = this.backup.nodes[this.nodeTab]
        this.suggestions.nodes.chosen = undefined
      }
    },
    toggleSuggestions: function (type) {
      if (!this.filters[type].suggestions) {
        this.filters[type].query = ""
      }
    }
    ,
    filterNode: function (value, search, item) {
      if (!this.filters.nodes.suggestions) {
        let attribute = this.filters.nodes.attribute.name
        return this.filter(item[attribute], search, item, attribute, this.filters.nodes.attribute.operator)
      } else {
        return true;
      }
    }
    ,
    filterEdge: function (value, search, item) {
      let attribute = this.filters.edges.attribute.name
      return this.filter(item[attribute], search, item, attribute, this.filters.edges.attribute.operator)
    },
    filter: function (value, search, item, attribute, operator) {
      if ((attribute === undefined || attribute.length === 0) || (operator === undefined || operator.length === 0))
        return false;
      if (value !== undefined && value != null && typeof value === "object") {
        let out = false;
        value.forEach(el => {
          if (out)
            return
          if (this.filter(el, search, item, attribute, operator)) {
            out = true
          }
        })
        return out;
      }
      switch (operator) {
        //TODO between case????
        case "empty":
          return value === undefined || value == null || value === ""
        case  "null":
          return value === undefined || value == null || value === ""
        case "=":
          return value == search
        case "!=":
          return value != search
        case "<":
          return value < Number(search)
        case "<=":
          return value <= Number(search)
        case ">=":
          return value >= Number(search)
        case ">":
          return value >= Number(search)
        case "equals":
          return value === search
        case "contains":
          return value !== undefined && value != null && value.indexOf(search) !== -1
        case "matches":
          return value !== undefined && value != null && value.match(search) != null
        default:
          return false
      }

    },
    operatorNames: function (type, name, attribute) {
      let headers = this.headers(type, name);
      let out = []
      headers.forEach(attr => {
        if (attr.text === attribute) {
          if (attr.numeric) {
            out = ["=", "!="]
            if (!attr.id)
              ["<", "<=", ">", ">=", "<>", "null"].forEach(o => out.push(o))
          } else {
            out = ["equals"]
            if (!attr.id)
              ["contains", "matches", "empty"].forEach(o => out.push(o))
          }
        }
      })
      return out;

    },

    switchCollapseEdges: function () {
      let tmp = this.collapse.edge1
      this.collapse.edge1 = this.collapse.edge2
      this.collapse.edge2 = tmp
      this.collapse.edgeName = this.collapse.edge1 + "_and_" + this.collapse.edge2
    },

    nodeOptions: function () {
      this.optionDialog = true;
      this.optionsTab = 0
      this.options["title"] = (Object.keys(this.nodes).length > 1) ? "Nodes" : "Node"

      this.options["attributes"] = {}
      for (let node in Object.keys(this.attributes.nodes)) {
        let models = []
        this.attributes.nodes[Object.keys(this.attributes.nodes)[node]].forEach(attr => {
          models.push({name: attr.name, selected: attr.list})
        })
        this.options.attributes[node] = models;
      }
      console.log(this.options)
      this.options["type"] = "nodes";
    }
    ,
    isDisabled: function (type, name) {
      if (type !== undefined && name !== undefined) {
        let clicked = this.collapse[type].filter(x => x.name === name)[0]
        if (clicked.disabled)
          return

        if (type === "edges") {
          if (this.collapse.edge1.length === 0)
            this.collapse.edge1 = name
          else
            this.collapse.edge2 = name
        }

      }
      let node = undefined;
      this.collapse.nodes.forEach(n => {
        if (n.selected)
          node = n
      })

      let edges = this.collapse.edges.filter(e => e.selected)

      this.collapse.edges.forEach(e => {

        if (!e.selected && (((node !== undefined && (e.from !== node.id && e.to !== node.id))) || edges.length === 2 || (edges.length === 1 && this.collapse.self.selected)))
          e.disabled = true
        else
          e.disabled = false
      })
      let middlenodeId = undefined
      this.collapse.self.disabled = false
      if (edges.length === 2) {
        let nodeids = [edges[0].to, edges[0].from]
        if (nodeids.indexOf(edges[1].to) !== -1)
          middlenodeId = edges[1].to
        else if (nodeids.indexOf(edges[1].from !== -1))
          middlenodeId = edges[1].from
        this.collapse.self.disabled = true

        this.collapse.edgeName = this.collapse.edge1 + "_and_" + this.collapse.edge2
      } else if (edges.length === 1) {
        this.collapse.edgeName = this.collapse.edge1 + "_loop"
      }
      this.collapse.nodes.forEach(n => {
        let disable = true;
        this.collapse.edges.forEach(e => {
          if (n.selected || (node === undefined && (edges.length === 0 || (e.selected && (e.to === n.id || e.from === n.id) && (edges.length < 2 || middlenodeId === n.id)))))
            disable = false
        })
        n.disabled = disable
      })

      this.collapse.accept = this.isConnectedCollapse()
      this.$refs.dialog.$forceUpdate()
    },
    edgeOptions: function () {
      this.optionDialog = true;
      this.optionsTab = 0
      this.options["title"] = (Object.keys(this.edges).length > 1) ? "Edges" : "Edge"

      this.options["attributes"] = {}
      for (let edge in Object.keys(this.attributes.edges)) {
        let models = []
        this.attributes.edges[Object.keys(this.attributes.edges)[edge]].forEach(attr => {
          models.push({name: attr.name, selected: attr.list})
        })
        this.options.attributes[edge] = models;
      }
      console.log(this.options)
      this.options["type"] = "edges";
    }
    ,
    dialogResolve(apply) {
      this.optionDialog = false;
      if (!apply) {
        this.$nextTick()
        return;
      }
      let comparison = this.attributes[this.options.type];

      let reloadNeeded = [];
      for (let index in this.options.attributes) {
        let name = Object.keys(comparison)[index]
        for (let attrIndex in this.options.attributes[index]) {
          if (this.options.attributes[index][attrIndex].selected && !comparison[name][attrIndex].sent) {
            reloadNeeded.push(name)
            break;
          }
          comparison[name][attrIndex].list = this.options.attributes[index][attrIndex].selected;
        }
      }
      if (reloadNeeded.length > 0) {
        let payload = {id: this.gid, attributes: {nodes: {}, edges: {}}}
        for (let index in this.options.attributes) {
          let name = Object.keys(comparison)[index]
          if (reloadNeeded.indexOf(name) === -1)
            continue
          let attrs = []
          payload.attributes[this.options.type][name] = attrs
          for (let attrIndex in this.options.attributes[index]) {
            if (this.options.attributes[index][attrIndex].selected)
              attrs.push(this.options.attributes[index][attrIndex].name)
          }
        }
        this.$http.post("/getCustomGraphList", payload).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
          //TODO just add new data?
          if (this.options.type === "nodes") {
            for (let name in data.nodes) {
              this.attributes.nodes[name] = data.attributes.nodes[name]
              this.nodes[name] = data.nodes[name]
            }
            this.update.nodes = true;
          } else {
            for (let name in data.edges) {
              this.attributes.edges[name] = data.attributes.edges[name]
              this.edges[name] = data.edges[name]
            }
            this.update.edges = true
          }
        }).catch(err => {
          console.log(err)
        })
      } else {
        this.update.nodes = true;
      }

    },
    extensionDialogResolve: function (apply) {
      let payload = {
        gid: this.gid,
        nodes: this.extension.nodes.filter(n => n.selected).map(n => n.name),
        edges: this.extension.edges.filter(n => n.selected && !n.disabled).map(n => n.name)
      }
      this.extension = {nodes: [], edges: []}
      this.extension.show = false;
      if (!apply) {
        this.$nextTick()
        return
      }
      console.log(payload)
      this.$http.post("/extendGraph", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(info => {
        this.$emit("updateInfo", info)
      }).catch(err => console.log(err))
    },
    resetCollapseDialog: function () {
      this.collapse.nodes = [];
      this.collapse.edges = []
      this.collapse.self.disabled = false;
      this.collapse.edge1 = ""
      this.collapse.edge2 = ""
    },
    collapseDialogResolve: function (apply) {
      this.collapse.show = false;
      if (!apply) {
        this.resetCollapseDialog()
        this.$nextTick()
        return
      }
      let payload = {
        gid: this.gid,
        self: this.collapse.self.selected,
        edgeName: this.collapse.edgeName,
        edge1: this.collapse.edge1,
        edge2: this.collapse.edge2,
        node: this.collapse.nodes.filter(n => n.selected)[0].name,
        keep: this.collapse.keep
      }
      this.resetCollapseDialog()
      console.log(payload)
      this.$http.post("/collapseGraph", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(info => {
        this.$emit("updateInfo", info)
      }).catch(err => console.log(err))
    },
    selectionDialogResolve: function (apply) {
      this.selectionDialog.show = false
      if (!apply || (this.selectionDialog.type === "nodes" && this.selectionDialog.seeds.filter(k => k.select).length < 2)) {
        this.selectionDialog.extendSelect = false;
        this.$nextTick()
        return;
      }

      let payload = {
        gid: this.gid,
        node: this.selectionDialog.type === "edges",
        edge: this.selectionDialog.type === "nodes",
        extend: this.selectionDialog.extendSelect,
        nodes: {},
        edges: {}
      };

      let edges = (this.selectionDialog.type === "edges" ? this.selectionDialog.seeds : this.selectionDialog.targets);
      edges.forEach(k => {
        if (k.select) {
          if (this.selectionDialog.type === "edges") {
            let v = this.edges[k.name]
            let directed = this.attributes.edges[k.name].filter(attr => attr.name === "sourceId").length > 0;
            let id1 = directed ? "sourceId" : "idOne"
            let id2 = directed ? "targetId" : "idTwo"
            payload.edges[k.name] = v.filter((v1, k1) => v1.selected).map((v1, k1) => {
                return {id1: v1[id1], id2: v1[id2]}
              }
            )
          } else
            payload.edges[k.name] = []
        }
      })
      let nodes = (this.selectionDialog.type === "nodes" ? this.selectionDialog.seeds : this.selectionDialog.targets);
      nodes.forEach(k => {
        if (k.select) {
          if (this.selectionDialog.type === "nodes") {
            let v = this.nodes[k.name]
            payload.nodes[k.name] = v.filter((v1, k1) => v1.selected).map((v1, k1) => {
                return v1.id
              }
            )
          } else
            payload.nodes[k.name] = []
        }
      })
      console.log(payload)
      this.selectionDialog.extendSelect = false;
      this.$http.post("/getConnectedSelection", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        // if (this.selectionDialog.type === "edges")
        for (let ntype in data.nodes) {
          this.select("nodes", ntype, data.nodes[ntype])
        }
        // else
        for (let etype in data.edges) {
          this.select("edges", etype, data.edges[etype])
        }
      }).catch(err => {
        console.log(err)
      })
    }
    ,
    loadSelection: function () {
      //TODO check if correct (nodes connected by edge)
      let update = {id: this.gid, nodes: {}, edges: {}}
      for (let type in this.nodes) {
        update.nodes[type] = []
        this.nodes[type] = this.filterSelected(this.nodes[type])
        this.nodes[type].forEach(node => update.nodes[type].push(node.id))
      }
      for (let type in this.edges) {
        update.edges[type] = []
        this.edges[type] = this.filterSelected(this.edges[type])
        this.edges[type].forEach(edge => update.edges[type].push(edge.id))
      }
      this.filters.nodes.suggestions = false;
      this.filterNodeModel = null
      this.$http.post("/updateGraph", update).then(response => {
        if (response.data !== undefined)
          return response.data;
      }).then(info => {
        this.$emit("updateInfo", info)
      }).catch(err => {
        console.log(err)
      })
    }
    ,
    select: function (type, name, ids) {
      let data = {nodes: this.nodes, edges: this.edges}
      let items = data[type][name]
      let count = 0;
      if (type === "nodes")
        items.forEach(item => {
          if (ids.indexOf(item.id) !== -1)
            item.selected = true;
        })
      else {
        let directed = this.attributes.edges[name].filter(attr => attr.name === "sourceId").length > 0;
        let id1 = directed ? "sourceId" : "idOne"
        let id2 = directed ? "targetId" : "idTwo"
        items.forEach(item => {
          let node2s = ids[item[id1]]
          if (node2s !== undefined && node2s.indexOf(item[id2]) !== -1) {
            item.selected = true;
            count++
          }
        })
      }
      this.$nextTick().then(() => {
        if ("nodes" === type)
          this.$refs.nodeTab.$forceUpdate()
        else
          this.$refs.edgeTab.$forceUpdate()
      })
    },
    selectAll: function (type, tab) {
      let name = Object.keys(this[type])[tab]
      let items = this[type][name]

      let filterActive = this.filters[type].attribute.name !== undefined && this.filters[type].attribute.name.length > 0 && this.filters[type].query !== null && this.filters[type].query.length > 0 && this.filters[type].attribute.operator !== undefined && this.filters[type].attribute.operator.length > 0
      items.forEach(item => {
          if (type === "nodes") {
            if (!filterActive || (filterActive && this.filterNode(undefined, this.filters[type].query, item)))
              item.selected = true;
          } else if (!filterActive || (filterActive && this.filterEdge(undefined, this.filters[type].query, item)))
            item.selected = true
        }
      )

      if (type === "edges") {
        this.selectionDialog.seeds = [{select: true, name: name}]
        let edge = Object.values(this.entityGraph.edges).filter(e => e.name === name)[0]
        let nodes = Object.values(this.entityGraph.nodes).filter(n => n.id === edge.node1 || n.id === edge.node2)
        this.selectionDialog.targets = nodes.map(n => {
          return {name: n.name, select: true}
        })
        this.selectionDialog.type = type
        this.selectionDialog.extendSelect = false
        this.selectionDialog.extendScope = false
        this.selectionDialogResolve(true)
      }

      this.$nextTick().then(() => {
        if ("edges" === type)
          this.$refs.edgeTab.$forceUpdate()
        this.$refs.nodeTab.$forceUpdate()

      })


    }
    ,
    deselectAll: function (type, tab) {
      let data = {nodes: this.nodes, edges: this.edges}
      let items = data[type][Object.keys(data[type])[tab]]
      let filterActive = this.filters[type].attribute.name !== undefined && this.filters[type].attribute.name.length > 0 && this.filters[type].query !== null && this.filters[type].query.length > 0 && this.filters[type].attribute.operator !== undefined && this.filters[type].attribute.operator.length > 0
      items.forEach(item => {
        if (type === "nodes") {
          if (!filterActive || (filterActive && this.filterNode(undefined, this.filters[type].query, item)))
            item.selected = false;
        } else if (!filterActive || (filterActive && this.filterEdge(undefined, this.filters[type].query, item)))
          item.selected = false;

      })
      this.$nextTick().then(() => {
        if ("nodes" === type)
          this.$refs.nodeTab.$forceUpdate()
        else
          this.$refs.edgeTab.$forceUpdate()
      })
    }
    ,
    nodeDetails: function (nodeId) {
      this.$emit("selectionEvent", {type: "node", name: Object.keys(this.nodes)[this.nodeTab], id: nodeId})
    }
    ,
    edgeDetails: function (id1, id2) {
      this.$emit("selectionEvent", {type: "edge", name: Object.keys(this.edges)[this.edgeTab], id1: id1, id2: id2})
    }
    ,
    headers: function (entity, node) {
      let out = [{text: "select", align: 'start', sortable: false, value: "selected"}]
      this.attributes[entity][node].forEach(attr => {
        if (!attr.list)
          return
        let name = attr.name
        if (name === "id")
          return;
        if (name === "sourceId" || name === "idOne") {
          out.push({
            text: "EdgeId",
            align: 'start',
            sortable: false,
            value: "edgeid",
            list: false,
            id: true,
            numeric: true
          })
        }
        out.push({
          text: name,
          align: 'start',
          sortable: attr.numeric,
          list: attr.array,
          value: name,
          id: attr.id,
          numeric: attr.numeric
        })
      })
      this.update[entity] = false;
      return out
    }
    ,
    headerNames: function (entity, node) {
      let headers = this.headers(entity, node);
      let out = []
      headers.forEach(header => out.push(header.text))
      let idx = out.indexOf("select")
      if (idx > -1)
        out.splice(idx, 1)
      return out;
    }
    , selectEdges: function () {
      this.selectionDialog.type = "nodes"
      this.selectionDialog.title = "Edges"


      if (this.attributes.nodes !== undefined)
        this.selectionDialog.seeds = Object.keys(this.attributes.nodes).map(name => {
          return {name: name, select: false}
        })
      if (this.attributes.edges !== undefined)
        this.selectionDialog.targets = Object.keys(this.attributes.edges).map(name => {
          return {name: name, select: false}
        })
      this.selectionDialog.show = true;
      this.$nextTick()
    }
    , selectNodes: function () {
      this.selectionDialog.title = "Nodes"
      this.selectionDialog.type = "edges"

      if (this.attributes.edges !== undefined)
        this.selectionDialog.seeds = Object.keys(this.attributes.edges).map(name => {
          return {name: name, select: false}
        })
      if (this.attributes.nodes !== undefined)
        this.selectionDialog.targets = Object.keys(this.attributes.nodes).map(name => {
          return {name: name, select: false}
        })


      this.selectionDialog.show = true;
      this.$nextTick()
    },
    collapseGraph: function () {
      // show: false, nodes: [], self:false, edges:[]
      //TODO implement popup menu
      this.collapse.nodes = Object.values(this.entityGraph.nodes).map(n => {
        return {name: n.name, selected: false, id: n.id, disabled: false}
      })
      this.collapse.edges = Object.values(this.entityGraph.edges).map(e => {
        return {name: e.name, selected: false, from: e.node1, to: e.node2, disabled: false}
      })
      //TODO add already collapsed edges (need to get connecting nodes)
      if (this.collapse.edges.length < 2) {
        this.collapse.self.selected = true
        this.collapse.self.disabled = true
        this.collapse.edges.forEach(e => {
          e.disabled = true
          e.selected = true
          this.collapse.edge1 = e.name
        })
      } else if (this.collapse.edges.length === 2 && !this.collapse.self.selected) {
        this.collapse.edges.forEach(e => {
          e.disabled = true
          e.selected = true
          if (this.collapse.edge1.length === 0)
            this.collapse.edge1 = e.name
          else
            this.collapse.edge2 = e.name
        })
      }
      if (this.collapse.nodes.length === 1) {
        this.collapse.nodes.forEach(n => {
          n.disabled = true
          n.selected = true
        })
      }
      this.collapse.accept = this.isConnectedCollapse()
      this.collapse.show = true;
      console.log(this.collapse)
      this.$nextTick()

    },
    isConnectedCollapse: function () {
      let node = undefined;
      this.collapse.nodes.forEach(n => {
        if (n.selected)
          node = n.id
      })
      if (node === undefined)
        return false;
      let edges = this.collapse.edges.filter(e => e.selected)
      if (edges.length === 0 || (!this.collapse.self.selected && edges.length < 2))
        return false;
      edges.forEach(e => {
        if (e.from !== node && e.to !== node)
          return false
      })
      return true
    },
    extendGraph: function () {
      this.extension.edges = this.metagraph.edges.map(e => e.label).map(e => {
        let there = Object.keys(this.attributes.edges).indexOf(e) !== -1
        return {name: e, selected: there, disabled: there}
      })
      this.extension.show = true;

      this.$nextTick()

    },
    getList: function (gid, metagraph) {
      this.setMetagraph(metagraph)
      this.clearLists()
      this.$http.get("/getGraphList?id=" + gid + "&cached=true").then(response => {
        this.loadList(response.data)
      }).then(() => {
        this.gid = gid;
      }).then(() => {
        this.init()
      }).then(() => {
        this.$emit("finishedEvent")
      }).catch(err => {
        console.log(err)
      })
    }
  }
}
</script>

<style scoped>

</style>
