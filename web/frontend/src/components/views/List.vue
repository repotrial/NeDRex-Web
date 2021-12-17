<template>
  <div>
    <v-container>
      <v-card style="margin:5px;padding-bottom: 15px" :loading="loading">
        <template slot="progress">
          <v-card-title>Nodes</v-card-title>
          <v-progress-circular v-if="!waiting"
                               color="primary"
                               size="50"
                               width="5"
                               indeterminate
          ></v-progress-circular>
          <i v-else>No data available!</i>

        </template>
        <template v-if="!loading" v-show="!nodeTabLoading">
          <i v-if="!update.nodes && nodes!=null &&Object.keys(nodes).length === 0">no node entries</i>
          <template v-if="nodes !=null && Object.keys(nodes).length>0">
            <v-tabs next-icon="mdi-arrow-right-bold-box-outline"
                    prev-icon="mdi-arrow-left-bold-box-outline"
                    show-arrows
                    v-model="nodeTab"
                    @change="resetFilters('nodes')"
            >
              <v-card-title id="nodes" ref="nodeTitle" style="color: black">
                Nodes:
              </v-card-title>
              <v-tab v-for="node in Object.keys(nodes)" :key="node" style="margin-top: 16px">
                {{ node }}
              </v-tab>
            </v-tabs>
            <v-tabs-items>
              <v-data-table
                ref="nodeTab"
                fixed-header
                class="elevation-1"
                :headers="headers('nodes',Object.keys(nodes)[nodeTab])"
                :items="configuration.showAll ? (suggestion ? nodes[Object.keys(nodes)[nodeTab]].filter(n=>n.suggested) :filters.nodes.attribute.dist ? distinctFilter('nodes',nodes[Object.keys(nodes)[nodeTab]],nodeTab): nodes[Object.keys(nodes)[nodeTab]]) : filterSelected(nodes[Object.keys(nodes)[nodeTab]])"
                item-key="id"
                :loading="nodeTabLoading"
                loading-text="Loading... Please wait"
                :search="filters.nodes.query"
                :custom-filter="filterNode"
              >
                <template v-slot:header.info>
                  <div style="display: flex">
                    <div style="justify-self: flex-start;  margin-right: auto; margin-top: 4px;">
                      Info
                    </div>
                    <div style="justify-self: flex-end; margin-left: auto;">
                      <v-tooltip right>
                        <template v-slot:activator="{attrs, on}">
                          <v-btn small
                                 style="margin-top: -2px;"
                                 icon
                                 @click="downloadNodeTable()">
                            <v-icon small v-bind="attrs" v-on="on">fas fa-download</v-icon>
                          </v-btn>
                        </template>
                        <span>Download table</span>
                      </v-tooltip>
                    </div>
                  </div>
                </template>
                <template v-slot:header.selected>
                  <v-tooltip right>
                    <template v-slot:activator="{attrs, on}">
                      <v-btn small style="margin-left: -10px; margin-top: -2px;" icon
                             @click="nodeOptions">
                        <v-icon small v-bind="attrs" v-on="on">fas fa-cog</v-icon>
                      </v-btn>
                    </template>
                    <span>Edit table headers</span>
                  </v-tooltip>
                  Select
                </template>
                <template v-slot:top>
                  <v-container style="margin-top: 15px;margin-bottom: -20px">
                    <v-row>
                      <v-col
                      >
                        <v-list-item>
                          <v-switch
                            label="Suggestions"
                            v-model="filters.nodes.suggestions"
                            v-on:click="toggleSuggestions('nodes')">
                          </v-switch>
                        </v-list-item>
                      </v-col>
                    </v-row>
                    <div style="display: flex;width: 100%">
                      <div
                        v-show="!filters.nodes.suggestions"
                        style="justify-self: flex-start; margin-left: 16px; margin-right: 8px; width: 25%"
                      >
                        <v-select
                          dense
                          :items="headerNames('nodes',Object.keys(nodes)[nodeTab])"
                          label="Attribute"
                          v-model="filters.nodes.attribute.name"
                          @change="resetFilter('nodes')"
                          outlined
                          style="width: 100%; min-width: 150px;"
                        ></v-select>
                      </div>
                      <div
                        v-if="!filters.nodes.suggestions"
                        style="justify-self: flex-start; width: 20%; margin-left: 8px; margin-right: 8px"
                      >
                        <v-select
                          dense
                          v-if="filters.nodes.attribute.name === undefined || filters.nodes.attribute.name == null || !isDistinctAttribute('nodes',filters.nodes.attribute.name)"
                          v-model="filters.nodes.attribute.operator"
                          :disabled="filters.nodes.attribute.name === undefined || filters.nodes.attribute.name == null || filters.nodes.attribute.name.length ===0"
                          :items="operatorNames('nodes',Object.keys(nodes)[nodeTab],filters.nodes.attribute.name)"
                          label="Operator"
                          outlined
                          style="min-width: 150px"
                        ></v-select>
                        <v-select
                          v-else
                          dense
                          v-model="filters.nodes.attribute.operator"
                          @change="filters.nodes.attribute.dist=true"
                          :items="attributes.nodes[Object.keys(nodes)[nodeTab]].filter(a=>a.label===filters.nodes.attribute.name)[0].values"
                          label="Value"
                          outlined
                          style="min-width: 100px"
                        >
                        </v-select>
                      </div>
                      <div
                        style="display: flex; justify-self: flex-start; justify-content: center; width: 100%; margin-left: 8px; margin-right: 16px"
                      >
                        <v-text-field
                          dense
                          :disabled="filters.nodes.attribute.name == null || filters.nodes.attribute.name.length ===0 || filters.nodes.attribute.operator == null|| filters.nodes.attribute.dist"
                          clearable
                          v-show="!filters.nodes.suggestions"
                          v-model="filters.nodes.query"
                          label="Query (case sensitive)"
                          class="mx-4"
                        ></v-text-field>
                        <v-autocomplete
                          dense
                          chips
                          :search-input.sync="findNodeSuggestions"
                          v-show="filters.nodes.suggestions"
                          :loading="suggestions.nodes.loading"
                          :items="suggestions.nodes.data"
                          :filter="()=>{return true}"
                          v-model="filterNodeModel"
                          item-value="key"
                          label="Query (case insensitive)"
                          class="mx-4"
                          style="max-width: 500px; margin-bottom: 4px"
                        >
                          <template v-slot:item="{ item }">
                            <SuggestionElement :data="item"></SuggestionElement>
                          </template>
                        </v-autocomplete>
                      </div>
                    </div>
                  </v-container>
                </template>
                <template v-slot:item.selected="{item}">
                  <v-row>
                    <v-col cols="2">
                      <v-checkbox
                        style="margin-top: -4px; margin-left:24px"
                        :key="item.id"
                        v-model="item.selected"
                        hide-details
                        primary
                        @click="countClick('nodes',nodeTab,item.selected)"
                      >
                      </v-checkbox>
                    </v-col>
                  </v-row>

                </template>
                <template v-slot:item.info="{item}">
                  <v-row>
                    <v-col cols="1">
                      <v-tooltip right>
                        <template v-slot:activator="{ on, attrs }">
                          <v-icon
                            small
                            left
                            color="primary"
                            dark
                            v-bind="attrs"
                            v-on="on"
                            v-on:click="showInGraph('node',item)"
                          >
                            fas fa-project-diagram
                          </v-icon>
                        </template>
                        <span>Focus in network</span>
                      </v-tooltip>
                    </v-col>
                    <v-col cols="1">
                      <v-tooltip right>
                        <template v-slot:activator="{ on, attrs }">
                          <v-icon
                            color="primary"
                            dark
                            right
                            v-bind="attrs"
                            v-on="on"
                            v-on:click="nodeDetails(item.id)"
                          >
                            fas fa-info-circle
                          </v-icon>
                        </template>
                        <span>Details (ID:{{ item.id }})</span>
                      </v-tooltip>
                    </v-col>
                  </v-row>

                </template>
              </v-data-table>
            </v-tabs-items>
          </template>
        </template>
      </v-card>
      <v-card style="margin:5px;padding-bottom: 15px" :loading="loading">
        <template slot="progress">
          <v-card-title>Edges</v-card-title>
          <v-progress-circular v-if="!waiting"
                               color="primary"
                               size="50"
                               width="5"
                               indeterminate
          ></v-progress-circular>
          <i v-else>No data available!</i>
        </template>
        <template v-if="!loading">
          <i v-if="edges !=null && Object.keys(edges).length === 0">no edge entries</i>
          <template v-if="edges !=null && Object.keys(edges).length>0">
            <v-tabs
              next-icon="mdi-arrow-right-bold-box-outline"
              prev-icon="mdi-arrow-left-bold-box-outline"
              show-arrows
              v-model="edgeTab"
            >
              <v-card-title ref="edgeTitle" style="color: black">Edges:
              </v-card-title>
              <v-tab v-for="edge in Object.keys(edges)" :key="edge" style="margin-top: 16px">
                <template v-if="edge.length<36">
                  {{ edge }}
                </template>
                <v-tooltip v-else top>
                  <template v-slot:activator="{on, attrs}">
                    <span v-on="on" v-bind="attrs">{{ edge.substring(0, 32) }}...</span>
                  </template>
                  {{ edge }}
                </v-tooltip>
              </v-tab>
            </v-tabs>
            <v-tabs-items>
              <v-data-table
                ref="edgeTab"
                fixed-header
                class="elevation-1"
                :headers="headers('edges',Object.keys(edges)[edgeTab])"
                :items="configuration.showAll ? (filters.edges.attribute.dist ? distinctFilter('edges',edges[Object.keys(edges)[edgeTab]],edgeTab): edges[Object.keys(edges)[edgeTab]]) : filterSelected(edges[Object.keys(edges)[edgeTab]])"
                :search="filters.edges.query"
                :custom-filter="filterEdge"
                loading-text="Loading... Please wait"
                item-key="id"
              >
                <template v-slot:header.info>
                  <div style="display: flex">
                    <div style="justify-self: flex-start;  margin-right: auto; margin-top: 4px;">
                      Info
                    </div>
                    <div style="justify-self: flex-end; margin-left: auto;">
                      <v-tooltip right>
                        <template v-slot:activator="{attrs, on}">
                          <v-btn small
                                 style="margin-top: -2px;"
                                 icon
                                 @click="downloadEdgeTable()">
                            <v-icon small v-bind="attrs" v-on="on">fas fa-download</v-icon>
                          </v-btn>
                        </template>
                        <span>Download table</span>
                      </v-tooltip>
                    </div>
                  </div>
                </template>
                <template v-slot:header.selected>
                  <v-tooltip right>
                    <template v-slot:activator="{attrs, on}">
                      <v-btn small style="margin-left: -10px; margin-top: -2px;" icon
                             @click="edgeOptions">
                        <v-icon small v-bind="attrs" v-on="on">fas fa-cog</v-icon>
                      </v-btn>
                    </template>
                    <span>Edit table headers</span>
                  </v-tooltip>
                  Select
                </template>
                <template v-slot:top>
                  <v-container style="margin-top: 15px;margin-bottom: -30px">
                    <div style="display: flex;width: 100%">
                      <div style="justify-self: flex-start; margin-left: 16px; margin-right: 8px; width:25%">
                        <v-select
                          dense
                          :items="headerNames('edges',Object.keys(edges)[edgeTab])"
                          label="Attribute"
                          v-model="filters.edges.attribute.name"
                          @change="resetFilter('edges')"
                          outlined
                          style="width: 100%"
                        ></v-select>
                      </div>
                      <div style="justify-self: flex-start; width: 20%; margin-left: 8px; margin-right: 8px">
                        <v-select
                          dense
                          v-if="filters.edges.attribute.name === undefined || filters.edges.attribute.name == null || !isDistinctAttribute('edges',filters.edges.attribute.name)"
                          v-model="filters.edges.attribute.operator"
                          :items="operatorNames('edges',Object.keys(edges)[edgeTab],filters.edges.attribute.name)"
                          :disabled="filters.edges.attribute.name === undefined || filters.edges.attribute.name == null || filters.edges.attribute.name.length ===0"
                          label="Operator"
                          outlined
                          style="width: 100%"
                        ></v-select>
                        <v-select
                          dense
                          v-else
                          v-model="filters.edges.attribute.operator"
                          @change="filters.edges.attribute.dist=true"
                          :items="attributes.edges[Object.keys(edges)[edgeTab]].filter(a=>a.label===filters.edges.attribute.name)[0].values"
                          label="Value"
                          outlined
                          style="width: 100%"
                        >
                        </v-select>
                      </div>
                      <div
                        style="display: flex; justify-self: flex-start; justify-content: center; width: 100%; margin-left: 8px; margin-right: 16px"
                      >
                        <v-text-field
                          :disabled="filters.edges.attribute.name == null || filters.edges.attribute.name.length ===0 || filters.edges.attribute.operator == null|| filters.edges.attribute.dist"
                          clearable
                          dense
                          v-model="filters.edges.query"
                          label="Query (case sensitive)"
                          class="mx-4"
                        ></v-text-field>
                      </div>
                    </div>
                  </v-container>
                </template>
                <template v-slot:item.selected="{item}">
                  <v-row>
                    <v-col cols="2">
                      <v-checkbox
                        :key="item.id"
                        v-model="item.selected"
                        hide-details
                        primary
                        style="margin-top: -4px; margin-left: 24px"
                        @click="countClick('edges',edgeTab,item)"
                      ></v-checkbox>
                    </v-col>
                  </v-row>
                </template>
                <template v-slot:item.edgeid="{item}">
                  <template v-if="item.sourceId !== undefined">
                    {{ item.sourceId }}
                    <v-icon>fas fa-long-arrow-alt-right</v-icon>
                    {{ item.targetId }}
                  </template>
                  <template v-else>
                    {{ item.idOne }}
                    <v-icon>fas fa-arrows-alt-h</v-icon>
                    {{ item.idTwo }}
                  </template>
                </template>
                <template v-slot:item.info="{item}">
                  <v-row>
                    <v-col cols="1">
                      <v-tooltip right>
                        <template v-slot:activator="{ on, attrs }">
                          <v-icon
                            color="primary"
                            dark
                            v-bind="attrs"
                            v-on="on"
                            v-on:click="edgeDetails(item)"
                          >
                            fas fa-info-circle
                          </v-icon>
                        </template>
                        <span>Details (ID:{{ item.id }})</span>
                      </v-tooltip>
                    </v-col>
                  </v-row>

                </template>
              </v-data-table>
            </v-tabs-items>
          </template>
        </template>
      </v-card>
    </v-container>
    <v-dialog
      v-model="extension.show"
      persistent
      max-width="500"
      ref="extensionDialog"
      style="z-index: 1001"
    >
      <v-card v-if="extension.show">
        <v-card-title class="headline">
          Add more edges to your current network
        </v-card-title>
        <v-card-text>Select edge types to be added to the current network. Additional options may be displayed on
          selection.
        </v-card-text>
        <v-divider></v-divider>
        <v-list>
          <template v-for="attr in extension.edges">
            <v-list-item :key="attr.name">
              <v-switch v-model="attr.selected" :disabled="attr.disabled"></v-switch>
              <span>
                <v-icon :color="getColoring('edges',attr.name,'light')[0]">fas fa-genderless</v-icon>
                    <template v-if="direction(attr.name)===0">
                      <v-icon>fas fa-undo-alt</v-icon>
                    </template>
                    <template v-else>
                      <v-icon>fas fa-long-arrow-alt-right</v-icon>
                      <v-icon :color="getColoring('edges',attr.name,'light')[1]">fas fa-genderless</v-icon>
                    </template>
                {{ attr.name }}
              </span>
            </v-list-item>
            <template v-if="attr.both>0 && attr.selected">
              <v-list-item :key="attr.name+'_addition'">
                <v-divider vertical style="margin-right:15px; margin-left:15px"></v-divider>
                <LabeledSwitch v-model="attr.induced" label-off="Extend by new nodes" label-on="Add induced edges only" style="margin-left: 5px">
                  <template v-slot:tooltip>
                    <div style="width: 300px">
                      When <b>disabled</b> adds also new nodes to the network. When <b>enabled</b> only edges that can connect two already existing nodes will be added.
                    </div>
                  </template>
                </LabeledSwitch>
              </v-list-item>
              <v-list-item v-if="attr.name==='DisorderHierarchy'">
                <v-divider vertical style="margin-right:15px; margin-left:15px"></v-divider>

                <LabeledSwitch v-model="attr.switch" label-off="Add children" label-on="Add parents" style="margin-left: 5px">
                  <template v-slot:tooltip>
                    <div style="width: 300px">
                      When <b>disabled</b> all subtypes of the current disorders are added. When <b>enabled</b> the umbrella disorders are added.
                    </div>
                  </template>
                </LabeledSwitch>
              </v-list-item>
            </template>
          </template>
        </v-list>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="red darken-1"
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
      style="z-index: 1001"
    >
      <v-card v-if="collapse.show" ref="dialog">
        <v-card-title class="headline">
          Edge Creation Configuration
        </v-card-title>
        <v-card-text>Select a node and one or two edges which should be used to create a new user-named edge.
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
                <template v-for="attr in collapse.nodes">
                  <v-list-item :key="attr.name">
                    <v-switch v-model="attr.selected" :disabled="attr.disabled" @click="isDisabled('nodes',attr.name)">
                    </v-switch>
                    <span>
                <v-icon left :color="getColoring('nodes',attr.name,'light')">fas fa-genderless</v-icon>
                {{ attr.name }}
              </span>
                  </v-list-item>
                </template>
              </v-list>

            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-list>
                <v-list-item>
                  <v-card-title>Edges</v-card-title>
                </v-list-item>
                <template v-for="attr in collapse.edges">
                  <v-list-item :key="attr.name">
                    <v-switch v-model="attr.selected" :disabled="attr.disabled" @click="isDisabled('edges',attr.name)">
                    </v-switch>
                    <span>
                    <v-icon left :color="getExtendedColoring('edges',attr.name,'light')[0]">fas fa-genderless</v-icon>
                    <template v-if="directionExtended(attr.name)===0">
                      <v-icon left>fas fa-undo-alt</v-icon>
                    </template>
                    <template v-else>
                      <v-icon>fas fa-long-arrow-alt-right</v-icon>
                      <v-icon left :color="getExtendedColoring('edges',attr.name,'light')[1]">fas fa-genderless</v-icon>
                    </template>
                {{ attr.name }}
              </span>
                  </v-list-item>
                </template>
              </v-list>
            </v-col>
          </v-row>
        </v-container>
        <v-divider></v-divider>
        <div style="display: flex; justify-content: center">
          <v-text-field
            label="Edge Name"
            outlined
            v-model="collapse.edgeName" style="margin:25px">
          </v-text-field>
        </div>
        <div v-if="collapse.accept" style="display: flex; justify-content: center">
          <v-icon
            :color="getExtendedColoring('nodes',getExtendedNodes(collapse.edge1,collapse.nodes.filter(n => n.selected)[0].name),'light')">
            fas fa-genderless
          </v-icon>
          <v-icon>fas fa-long-arrow-alt-right</v-icon>
          <v-icon :color="getExtendedColoring('nodes',collapse.nodes.filter(n => n.selected)[0].name,'light')">fas
            fa-genderless
          </v-icon>
          <v-icon>fas fa-long-arrow-alt-right</v-icon>
          <v-icon v-if="collapse.self.selected"
                  :color="getExtendedColoring('nodes',getExtendedNodes(collapse.edge1,collapse.nodes.filter(n => n.selected)[0].name),'light')">
            fas
            fa-genderless
          </v-icon>
          <v-icon v-else
                  :color="getExtendedColoring('nodes',getExtendedNodes(collapse.edge2,collapse.nodes.filter(n => n.selected)[0].name),'light')">
            fas fa-genderless
          </v-icon>
        </div>
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
              <v-chip outlined @click="switchCollapseEdges">Switch edges</v-chip>
            </v-col>
            <v-col style="margin-top:-15px">
              <v-switch
                v-model="collapse.keep"
              >
                <template v-slot:label>
                  Keep
                  <v-tooltip
                    right>
                    <template v-slot:activator="{ on, attrs }">
                      <v-icon
                        right
                        color="grey"
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
      style="z-index: 1001"
    >
      <v-card
        v-if="options !== undefined && options.type != null && options.attributes !=null && attributes[options.type]!=null">
        <v-card-title class="headline">
          Organize {{ options.title }} Attributes
        </v-card-title>
        <v-card-text>Adjust the attributes of the
          {{ options.type === "nodes" ? Object.keys(this.nodes)[this.nodeTab] : Object.keys(this.edges)[this.edgeTab] }}
          edge table header.
        </v-card-text>
        <v-divider></v-divider>
        <template v-if="Object.keys(options.type).length>0">
          <v-list>
            <v-list-item v-for="attr in options.attributes" :key="attr.name" dense >
              <v-switch v-model="attr.selected" dense :label="attr.label" :disabled="(attr.name === 'id')" style="margin-top: 0">
              </v-switch>
            </v-list-item>
          </v-list>
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
      style="z-index: 1001"
    >
      <v-card v-if="selectionDialog.show && selectionDialog.type !== undefined">
        <v-list>
          <v-list-item>
            <v-list-item-title class="text-h4">
              Select induced network
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-card-text>Extend your selection based on already selected nodes. Make sure to select the "extend" option
              to not only select edges but also nodes that are directly reachable.
            </v-card-text>
          </v-list-item>
        </v-list>
        <v-card-subtitle>Nodes</v-card-subtitle>
        <v-divider></v-divider>
        <v-tabs-items>
          <v-list>
            <template v-for="attr in selectionDialog.seeds">
              <v-list-item :key="attr.name">
                <v-switch v-model="attr.select" :disabled="attr.disabled"></v-switch>
                <span>
                <v-icon left :color="getColoring('nodes',attr.name,'light')">fas fa-genderless</v-icon>
                {{ attr.name }} ({{ countSelected('nodes', attr.name) }})
              </span>
              </v-list-item>
            </template>

          </v-list>
        </v-tabs-items>

        <v-card-subtitle>Edges</v-card-subtitle>
        <v-divider></v-divider>
        <v-tabs-items>
          <v-list>
            <template v-for="attr in selectionDialog.targets">
              <v-list-item :key="attr.name">
                <v-switch v-model="attr.select" :disabled="attr.disabled">
                </v-switch>
                <span>
                <v-icon left :color="getExtendedColoring('edges',attr.name,'light')[0]">fas fa-genderless</v-icon>
                    <template v-if="directionExtended(attr.name)===0">
                      <v-icon left>fas fa-undo-alt</v-icon>
                    </template>
                    <template v-else>
                      <v-icon>fas fa-long-arrow-alt-right</v-icon>
                      <v-icon left :color="getExtendedColoring('edges',attr.name,'light')[1]">fas fa-genderless</v-icon>
                    </template>
                {{ attr.name }} ({{ countSelected('edges', attr.name) }})
              </span>
              </v-list-item>
            </template>
          </v-list>
        </v-tabs-items>

        <v-card-subtitle>Options</v-card-subtitle>
        <v-container>
          <v-row>
            <v-col v-show="selectionDialog.type === 'nodes'">
              <v-switch v-model="selectionDialog.extendSelect" label=''>
                <template v-slot:label>
                  Extend start selection
                  <v-tooltip
                    right>
                    <template v-slot:activator="{ on, attrs }">
                      <v-icon
                        color="grey"
                        dark
                        v-bind="attrs"
                        v-on="on"
                        right
                      >
                        far fa-question-circle
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
    <AlgorithmExecution ref="algorithmDialog" @newJobEvent="newJobEvent" :node-type="algorithmDialogParams.nodeType" :type="algorithmDialogParams.type" :nodes="algorithmDialogParams.nodes"></AlgorithmExecution>
    <v-dialog
      v-model="selectionColor.show"
      persistent
      max-width="500"
      style="z-index: 1001"
    >
      <v-card>
        <v-card-title class="headline">
          Regroup Selected Nodes
        </v-card-title>
        <v-card-text>Create a temporary new group with a custom color and name for your node selection in the visualized
          network. New groups are also visible in the legend and can be toggled equally to normal node-groups. This
          overrides the original group of any node selected!
        </v-card-text>
        <v-divider></v-divider>
        <v-list>
          <v-list-item>
            <v-list-item-content>
              <v-text-field v-model=selectionColor.name label="Group-Name"
                            :rules="[value=>!!value|| 'Required!']"></v-text-field>
            </v-list-item-content>
          </v-list-item>
          <v-list-item style="justify-content: center">
            <v-color-picker v-model=selectionColor.color dot-size="20" mode="hexa"></v-color-picker>
          </v-list-item>
        </v-list>
        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="green darken-1"
            text
            @click="resolveRecoloring(false)"
          >
            Cancel
          </v-btn>
          <v-btn
            color="green darken-1"
            text
            @click="resolveRecoloring(true,selectionColor.name, selectionColor.color.hex)"
            :disabled="selectionColor.name==null ||selectionColor.name.length===0"
          >
            Recolor
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>

import SuggestionElement from "@/components/app/suggestions/SuggestionElement";
import AlgorithmExecution from "@/components/app/dialogs/AlgorithmExecution";
import LabeledSwitch from "@/components/app/input/LabeledSwitch";

export default {

  components: {
    LabeledSwitch,
    AlgorithmExecution,
    SuggestionElement,
  },

  props: {
    configuration: {
      showAll: Boolean,
      countMap: Object,
    },
  },
  name: "List",
  gid: undefined,
  uid: undefined,
  suqQuery: "",
  prefixMap: undefined,


  data() {
    return {
      edges: {},
      nodes: {},
      attributes: {},
      countMap: undefined,
      nodeTab: undefined,
      edgeTab: undefined,
      selectAllModel: {nodes: {}, edges: {}},
      nodepage: {},
      optionDialog: false,
      waiting: true,
      suggestion: false,
      selectionDialog: {
        show: false,
        type: "",
        title: "",
        seeds: [],
        targets: [],
        extendDescription: "Adding all edges of the target categories, which contain at least one node. The 'missing' nodes are also added to selection.",
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
      loading: true,
      nodeTabLoading: false,
      selectionColor: {show: false, name: "", color: {}},
      algorithmDialogParams:{type:undefined, nodeType:undefined, nodes: []}
    }
  },

  created() {
    this.uid = this.$cookies.get("uid")
    this.nodes = {}
    this.attributes = {}
    this.edges = {}
    this.countMap = {nodes: {}, edges: {}}
    this.configuration.countMap = this.countMap
    this.backup = {nodes: {}, edges: {}}
    this.gid = this.$route.params["gid"]
    if (this.gid !== undefined)
      this.getList(this.gid)
  },

  watch: {

    filterNodeModel: function (val) {
      this.applySuggestion(val)
    },
    findNodeSuggestions: function (val) {
      if (val != null && val.length === 0)
        val = null
      this.getNodeSuggestions(val, false)
    },
  },
  methods: {
    reload: function () {
      this.nodes = {}
      this.waiting = true
      this.attributes = {}
      this.edges = {}
      this.backup = {nodes: {}, edges: {}}
      this.gid = this.$route.params["gid"]
      if (this.gid !== undefined)
        this.getList(this.gid)
    },
    init: function () {
      if (this.gid === undefined)
        return
      this.$http.get("/getConnectionGraph?gid=" + this.gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.configuration.entityGraph = data
        this.reloadCountMap(true)
        this.loading = false
        this.$emit("loadLegendEvent", true)
      })
        .catch(err => console.error(err))
    },
    filterSelected(items) {
      return items.filter(item => item.selected)
    },
    clearLists: function () {
      this.loadList(undefined)
      this.selectAllModel = {nodes: {}, edges: {}}
      this.nodepage = {}
    },
    applyMultiSelect: function (selection) {
      if (this.prefixMap === undefined) {
        this.prefixMap = {}
        Object.values(this.$global.metagraph.nodes).forEach(n => this.prefixMap[n.group.substring(0, 3)] = n.group)
      }
      let selectionMap = {}
      selection.forEach(node => {
        let spl = node.split("_")
        let group = this.prefixMap[spl[0]]
        let id = parseInt(spl[1])
        if (selectionMap[group] === undefined)
          selectionMap[group] = [id]
        else
          selectionMap[group].push(id)
      })
      Object.keys(selectionMap).forEach(group => {
        this.select("nodes", group, selectionMap[group])
      })
      this.reloadCountMap()
    },
    receiveEvent: function (event) {
      switch (event) {
        case "collapse":
          this.collapseGraph()
          break
        case "extend":
          this.extendGraph()
          break
        case "subselect":
          this.loadSelection()
          break
      }
    },
    showInGraph: function (type, item) {
      let prefix = ""
      if (type === "node")
        prefix = Object.keys(this.attributes.nodes)[this.nodeTab].substr(0, 3) + "_"
      this.$emit("focusInGraphEvent", type, prefix + item.id)
    },
    loadSuggestionNodes: async function (item) {
      if (item.sid.indexOf('_') > -1) {
        let res = await this.$http.get("getSuggestionEntry?gid=" + this.gid + "&nodeType=" + Object.keys(this.nodes)[this.nodeTab] + "&sid=" + item.sid)
        return res.data
      } else {
        return [parseInt(item.sid)]
      }
    },
    getNodeSuggestions: function (val, timeouted) {
      if (!timeouted) {
        if (val == null || val.length < 2) {
          this.suggestions.data = []
          return
        }
        this.sugQuery = val
        setTimeout(function () {
          this.getNodeSuggestions(val, true)
        }.bind(this), 500)
      } else {
        if (val !== this.sugQuery) {
          return
        }
        let type = "nodes";
        let name = Object.keys(this.nodes)[this.nodeTab];
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
            data.suggestions.sort((e1, e2) => {
              return e2.size - e1.size
            })
            this.suggestions[type].data = data.suggestions;
          }).catch(err =>
            console.error(err)
          ).finally(() =>
            this.suggestions[type].loading = false
          )
        }).catch(err =>
          console.error(err))
      }
    },
    loadList: function (data) {
      this.attributes = {};
      this.edges = {};
      this.nodes = {};
      this.nodeTab = 0
      this.edgeTab = 0
      this.resetFilters('nodes')
      this.resetFilters('edges')
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
          let columns = this.attributes.nodes[node].map(n => n.name)
          let p_hyper = columns.indexOf("p_hyper") != -1
          let rank = columns.indexOf("rank") != -1
          for (let idx in this.nodes[node]) {
            let n = this.nodes[node][idx]
            n["selected"] = false;
            if (p_hyper && n.p_hyper == null)
              n.p_hyper = 0
            if (rank && n.rank == null)
              n.rank = 0;
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
    reloadTables: function () {
      this.$nextTick().then(() => {
        let tmp = this.nodeTab;
        this.nodeTab = -1
        this.nodeTab = tmp
        tmp = this.edgeTab
        this.edgeTab = -1
        this.edgeTab = tmp
        // this.$refs.nodeTab.$forceUpdate()
        // this.$refs.edgeTab.$forceUpdate()
      })
    },
    countSelected: function (type, name) {
      return this[type][name].filter(item => item.selected).length
    },
    getSelectedCount: function (type, name) {
      return this.configuration.countMap[type][name].selected
    },
    applySuggestion: async function (val) {
      this.nodeTabLoading = true
      this.suggestion = true;
      if (val != null) {
        let nodes = this.nodes[Object.keys(this.nodes)[this.nodeTab]]
        this.suggestions.nodes.chosen = await this.loadSuggestionNodes(this.suggestions.nodes.data.filter(item => item.key === val)[0])
        nodes.forEach(n => n.suggested = this.suggestions.nodes.chosen.indexOf(n.id) > -1)
      } else {
        this.resetSuggestions(true)
      }
      this.nodeTabLoading = false
    },
    resetSuggestions: function (full) {
      this.suggestion = false;
      if (full) {
        this.suggestions.nodes.chosen = undefined
      }
      if (this.backup.nodes[this.nodeTab])
        this.nodes[Object.keys(this.nodes)[this.nodeTab]].forEach(n => n.suggested = false)
      // this.nodes[Object.keys(this.nodes)[this.nodeTab]] = this.backup.nodes[this.nodeTab]
    },
    toggleSuggestions: function (type) {
      this.filterNodeModel = null
      this.filters[type].query = null
      this.suggestions[type].data = []
    }
    ,
    filterNode: function (value, search, item) {
      if (!this.filters.nodes.suggestions) {
        let attribute = this.filters.nodes.attribute.name
        attribute = Object.values(this.attributes.nodes)[this.nodeTab].filter(a => a.label === attribute).map(a => a.name)[0]
        return this.filter(item[attribute], search, item, attribute, this.filters.nodes.attribute.operator)
      } else {
        return true;
      }
    }
    ,
    filterEdge: function (value, search, item) {
      let attribute = this.filters.edges.attribute.name
      attribute = Object.values(this.attributes.edges)[this.edgeTab].filter(a => a.label === attribute).map(a => a.name)[0]
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
          return value > Number(search)
        case "equals":
          return value === search
        case "contains":
          return value !== undefined && value != null && value.indexOf(search) !== -1
        case "matches":
          try {
            return value !== undefined && value != null && value.match(search) != null
          } catch (err) {
            return false
          }
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
              ["<", "<=", ">", ">=", "<>"].forEach(o => out.push(o))
          } else {
            out = ["equals"]
            if (!attr.id)
              ["contains", "matches"].forEach(o => out.push(o))
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
    isDistinctAttribute: function (type, attribute) {
      let object = this.attributes[type][Object.keys(this[type])[type === "nodes" ? this.nodeTab : this.edgeTab]].filter(a => a.label === attribute)[0]
      return object !== undefined && object.values !== undefined && object.values != null && object.values.length > 0
    },


    isDisabled: function (type, name) {
      if (type !== undefined && name !== undefined) {
        let clicked = this.collapse[type].filter(x => x.name === name)[0]
        if (clicked.disabled)
          return

        if (type === "edges") {
          if (this.collapse.edge1.length === 0 || this.collapse.self.selected)
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
        if (node === undefined || (!e.selected && (((e.from !== node.id && e.to !== node.id)) || edges.length === 2 || (edges.length === 1 && this.collapse.self.selected))))
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
    nodeOptions: function () {
      this.optionDialog = true;
      this.optionsTab = 0
      this.options["title"] = (Object.keys(this.nodes).length > 1) ? "Nodes" : "Node"

      this.options["attributes"] = {}
      let models = []
      let node = Object.keys(this.nodes)[this.nodeTab]
      this.attributes.nodes[node].forEach(attr => {
        if (attr.name !== "type")
          models.push({name: attr.name, label: attr.label, selected: attr.list})
      })
      this.options.attributes = models;
      this.options.type = "nodes";
    }
    ,
    edgeOptions: function () {
      this.optionsTab = 0
      this.options["title"] = (Object.keys(this.edges).length > 1) ? "Edges" : "Edge"

      this.options["attributes"] = {}
      let models = []
      let edge = Object.keys(this.edges)[this.edgeTab]
      this.attributes.edges[edge].forEach(attr => {
        if (attr.name !== "type")
          models.push({name: attr.name, label: attr.label, selected: attr.list})
      })
      this.options.attributes = models;
      this.options.type = "edges";
      this.optionDialog = true;
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
      let name = this.options.type ==="nodes" ? Object.keys(this.nodes)[this.nodeTab]  : Object.keys(this.edges)[this.edgeTab]
      for (let attrIndex in this.options.attributes) {
        if (this.options.attributes[attrIndex].selected && !comparison[name][attrIndex].sent) {
          reloadNeeded.push(name)
          break;
        }
        comparison[name][attrIndex].list = this.options.attributes[attrIndex].selected;
      }
      if (reloadNeeded.length > 0) {
        let payload = {id: this.gid, attributes: {nodes: {}, edges: {}}}
        let attrs = []
        payload.attributes[this.options.type][name] = attrs
        for (let attrIndex in this.options.attributes) {
          if (this.options.attributes[attrIndex].selected)
            attrs.push(this.options.attributes[attrIndex].name)
        }
        this.$http.post("/getCustomGraphList", payload).then(response => {
          if (response.data !== undefined)
            return response.data
        }).then(data => {
            //TODO just add new data?
            if (this.options.type === "nodes") {
              for (let name in data.nodes) {
                let attributesShown = this.attributes.nodes[name].filter(a => a.list).map(a => a.label)
                let attributes = data.attributes.nodes[name].filter(a => a.list && attributesShown.indexOf(a.label) === -1).map(a => a.name)
                let newAttrs = {}

                data.nodes[name].forEach(n => {
                  newAttrs[n.id] = {}
                  attributes.forEach(a => {
                    newAttrs[n.id][a] = n[a]
                  })
                })
                this.attributes.nodes[name] = data.attributes.nodes[name]
                this.nodes[name].forEach(n => {
                  let atts = newAttrs[n.id];
                  Object.keys(atts).forEach(a => n[a] = atts[a])
                })
              }
              this.update.nodes = true;
            } else {
              for (let name in data.edges) {
                let attributesShown = this.attributes.edges[name].filter(a => a.list).map(a => a.label)
                let attributes = data.attributes.edges[name].filter(a => a.list && attributesShown.indexOf(a.label) === -1).map(a => a.name)
                let newAttrs = {}

                data.edges[name].forEach(e => {
                  newAttrs[e.id] = {}
                  attributes.forEach(a => {
                    newAttrs[e.id][a] = e[a]
                  })
                })

                this.attributes.edges[name] = data.attributes.edges[name]
                this.edges[name].forEach(e => {
                  let atts = newAttrs[e.id];
                  Object.keys(atts).forEach(a => e[a] = atts[a])
                })
              }
              this.update.edges = true
            }
          }
        ).catch(err => {
          console.error(err)
        })
      } else {
        this.update.nodes = true;
      }

    },
    extensionDialogResolve: function (apply) {
      let selected = this.extension.edges.filter(e => e.selected)
      let payload = {
        gid: this.gid,
        edges: selected.map(e => e.name),
        induced: selected.filter(e => e.induced).map(e => e.name),
        switchDirection: selected.filter(e => e.switch).map(e => e.name)
      }
      this.extension.nodes = []
      this.extension.edges = []
      this.extension.show = false;
      if (!apply) {
        return
      }
      this.loading = true
      this.$http.post("/extendGraph", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(info => {
        this.$emit("updateInfo", info)
      }).catch(err => console.error(err))
    },
    // downloadFromLegend: function (entity, name) {
    //   if (entity === "nodes") {
    //     let out = "#ID\tName\n"
    //     this.nodes[name].forEach(node => out += node.primaryDomainId + "\t" + node.displayName + "\n")
    //     this.download(name + "_nodes_" + this.gid + ".tsv", out)
    //   }
    //   if (entity === "edges") {
    //     let out = "#ID1\tID2\tName1\tName2\n"
    //     let edgeType = this.getExtendedNodes(name)
    //     let nodeMap1 = {}
    //     this.nodes[edgeType[0]].forEach(n => nodeMap1[n.id] = n.primaryDomainId)
    //     let nodeMap2 = {}
    //     if (edgeType[0] === edgeType[1])
    //       nodeMap2 = nodeMap1
    //     else
    //       this.nodes[edgeType[1]].forEach(n => nodeMap2[n.id] = n.primaryDomainId)
    //     this.edges[name].forEach(e => {
    //       let ids = e.id.split('-')
    //       out += nodeMap1[parseInt(ids[0])] + "\t" + nodeMap2[parseInt(ids[1])] + "\t" + e.node1 + "\t" + e.node2 + "\n"
    //     })
    //     this.download(name + "_edges_" + edgeType[0] + "-" + edgeType[1] + "_" + this.gid + ".tsv", out)
    //   }
    // },

    download: function (name, content) {
      let dl = document.createElement('a')
      dl.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(content))
      dl.setAttribute('download', name)
      dl.style.direction = 'none'
      document.body.appendChild(dl)
      dl.click()
      document.body.removeChild(dl)
    },
    resetCollapseDialog: function () {
      this.collapse.nodes = [];
      this.collapse.edges = []
      this.collapse.self.disabled = false;
      this.collapse.edge1 = ""
      this.collapse.edge2 = ""
    },
    distinctFilter: function (type, nodes, tab) {
      let value = this.filters[type].attribute.operator
      let attr = this.filters[type].attribute.name
      attr = Object.values(this.attributes[type])[tab].filter(a => a.label === attr)[0].name
      return nodes.filter(n => {
        let data = n[attr]
        if (typeof data === "object")
          return data.indexOf(value) > -1
        else
          return data === value
      })

    },
    collapseDialogResolve: function (apply) {
      if (!apply) {
        this.collapse.show = false;
        this.resetCollapseDialog()
        this.$nextTick()
        return
      }
      if (this.$global.metagraph.edges.flatMap(e => [e.label, e.title]).indexOf(this.collapse.edgeName) > -1) {
        this.printNotification("Edge-Name is already Taken. Please choose another one", 2)
        return
      }
      this.collapse.show = false;

      this.loading = true;
      let payload = {
        gid: this.gid,
        self: this.collapse.self.selected,
        edgeName: this.collapse.edgeName,
        edge1: this.collapse.edge1,
        edge2: this.collapse.edge2.length === 0 ? this.collapse.edge1 : this.collapse.edge2,
        node: this.collapse.nodes.filter(n => n.selected)[0].name,
        keep: this.collapse.keep
      }
      this.resetCollapseDialog()
      this.$http.post("/collapseGraph", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(info => {
        this.$emit("updateInfo", info)
      }).catch(err => console.error(err))
    },
    setLoading: function (boolean) {
      this.loading = boolean
    },
    resolveRecoloring: function (apply, name, color) {
      this.selectionColor.show = false
      if (!apply)
        return
      this.filterNodeModel = null
      let selectionIds = []
      Object.keys(this.nodes).forEach(name => this.nodes[name].filter(n => n.selected).forEach(n => selectionIds.push({id: name.substring(0, 3) + "_" + n.id})))
      this.$emit("recolorGraphEvent", {ids: selectionIds, color: color, name: name})
    },
    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    },
    selectionDialogResolve: function (apply) {
      this.selectionDialog.show = false
      if (!apply || (this.selectionDialog.type === "nodes" && this.selectionDialog.seeds.filter(k => k.select).length < 1)) {
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
        this.reloadCountMap()
      }).catch(err => {
        console.error(err)
      })
    }
    ,
    downloadEntries: async function (entity, name, attributes) {
      let table = await this.$http.getTableDownload(this.gid, entity, name, attributes)
      this.download(this.gid + "_" + name + "-" + entity + ".tsv", table)
    },

    downloadEdgeTable: function () {
      let type = Object.keys(this.edges)[this.edgeTab]
      let attributes = this.attributes.edges[type].filter(a => a.list).map(a => a.name)
      this.downloadEntries("edge", type, attributes)
    },

    downloadNodeTable: function () {
      let type = Object.keys(this.nodes)[this.nodeTab]
      this.downloadEntries("nodes", type, this.attributes.nodes[type].filter(a => a.list).map(a => a.name))
    },

    loadSelection: function () {

      //TODO check if correct (nodes connected by edge)
      let update = {id: this.gid, nodes: {}, edges: {}}
      if (Object.values(this.configuration.countMap.nodes).map(n => n.selected).reduce((i, v) => i + v) === 0) {
        this.printNotification("Please select some nodes first!", 1)
        return;
      }
      this.filterNodeModel = null
      for (let type in this.nodes) {
        update.nodes[type] = []
        this.nodes[type] = this.filterSelected(this.nodes[type])
        this.nodes[type].forEach(node => update.nodes[type].push(node.id))
      }
      for (let type in this.edges) {
        update.edges[type] = []
        this.edges[type] = this.filterSelected(this.edges[type])
        this.edges[type].forEach(edge => update.edges[type].push(edge.id === undefined ? edge.ID : edge.id))
      }
      this.filters.nodes.suggestions = false;
      this.filterNodeModel = null
      this.$http.post("/updateGraph", update).then(response => {
        this.loading = true;
        if (response.data !== undefined)
          return response.data;
      }).then(info => {
        this.$emit("updateInfo", info)
      }).catch(err => {
        console.error(err)
      })
    },
    select: function (type, name, ids) {
      let items = this[type][name]
      if (type === "nodes")
        items.forEach(item => {
          if (ids.indexOf(item.id) !== -1)
            item.selected = true;
        })
      else {
        items.forEach(item => {
          let id = item.id.split("-")
          let node2s = ids[parseInt(id[0])]
          if (node2s !== undefined && node2s.indexOf(parseInt(id[1])) !== -1) {
            item.selected = true;
          }
        })
      }
    },

    unselect: function (type, name, ids) {
      let items = this[type][name]
      if (type === "nodes")
        items.forEach(item => {
          if (ids.indexOf(item.id) !== -1)
            item.selected = false;
        })
      else {
        items.forEach(item => {
          let id = item.id.split("-")
          let node2s = ids[parseInt(id[0])]
          if (node2s !== undefined && node2s.indexOf(parseInt(id[1])) !== -1) {
            item.selected = false;
          }
        })
      }
    },

    selectDependentNodes: function (type, edges) {
      this.setDependentNodeSelection(type, edges, true)
    },
    newJobEvent: function(data){
      this.$emit('newJobEvent',data);
    },

    setDependentNodeSelection: function (type, edges, state) {
      let nodes = this.$utils.getNodesExtended(this.configuration.entityGraph, type)
      let nodeName1 = nodes[0];
      let nodeName2 = nodes[1];
      let nodeTab1 = Object.keys(this.attributes.nodes).indexOf(nodeName1)
      let nodeTab2 = Object.keys(this.attributes.nodes).indexOf(nodeName2)
      let nodeIds1 = []
      let nodeIds2 = []
      edges.forEach(edge => {
        let ids = (edge.id !== undefined ? edge.id : edge.ID).split("-");
        nodeIds1.push(parseInt(ids[0]))
        nodeIds2.push(parseInt(ids[1]))
      })
      this.nodes[nodeName1].filter(n => nodeIds1.indexOf(n.id) > -1 && n.selected !== state).forEach(n => {
        n.selected = state
        this.countClick('nodes', nodeTab1, state)
      })

      this.nodes[nodeName2].filter(n => nodeIds2.indexOf(n.id) > -1 && n.selected !== state).forEach(n => {
        n.selected = state
        this.countClick('nodes', nodeTab2, state)
      })
    },

    selectAll: function (type) {
      let tab = (type === "nodes") ? this.nodeTab : this.edgeTab
      let name = Object.keys(this[type])[tab]
      let items = this[type][name]
      let isDistinct = this.isDistinctAttribute(type, this.filters[type].attribute.name)
      if (isDistinct) {
        this.distinctFilter(type, items, tab).forEach(item => {
          this.$set(item, "selected", true)
          // item.selected = true
        })
      } else {
        let filterActive = this.filters[type].attribute.name !== undefined && this.filters[type].attribute.name.length > 0 && this.filters[type].query !== null && this.filters[type].query.length > 0 && this.filters[type].attribute.operator !== undefined && this.filters[type].attribute.operator.length > 0
        items.forEach(item => {
            if (type === "nodes") {
              if (this.suggestion) {
                if (item.suggested)
                  this.$set(item, "selected", true)
              } else if (!filterActive || (filterActive && this.filterNode(undefined, this.filters[type].query, item)))
                this.$set(item, "selected", true)
              // item.selected = true;
            } else if (!filterActive || (filterActive && this.filterEdge(undefined, this.filters[type].query, item)))
              this.$set(item, "selected", true)
            // item.selected = true
          }
        )
      }
      if (type === "edges")
        this.selectDependentNodes(name, items)
      this.reloadCountMap()
      this.$refs.nodeTab.$forceUpdate();
      if (type !== "nodes")
        this.$refs.edgeTab.$forceUpdate();
    }
    ,
    deselectAll: function (type) {
      let data = {nodes: this.nodes, edges: this.edges}
      if (type === "all") {
        this.filterNodeModel = null
        Object.values(data).forEach(set => Object.values(set).forEach(type => type.forEach(n => this.$set(n, "selected", false))))
      } else {
        let tab = (type === "nodes") ? this.nodeTab : this.edgeTab
        let items = data[type][Object.keys(data[type])[tab]]
        let isDistinct = this.isDistinctAttribute(type, this.filters[type].attribute.name)
        if (isDistinct) {
          this.distinctFilter(type, items, tab).forEach(item => {
            item.selected = false
          })
        } else {
          let filterActive = this.filters[type].attribute.name !== undefined && this.filters[type].attribute.name.length > 0 && this.filters[type].query !== null && this.filters[type].query.length > 0 && this.filters[type].attribute.operator !== undefined && this.filters[type].attribute.operator.length > 0
          items.forEach(item => {
            if (type === "nodes") {
              if (this.suggestion) {
                if (item.suggested)
                  item.selected = false;
              } else if (!filterActive || (filterActive && this.filterNode(undefined, this.filters[type].query, item)))
                item.selected = false;
            } else if (!filterActive || (filterActive && this.filterEdge(undefined, this.filters[type].query, item)))
              item.selected = false;
          })
        }
      }
      this.reloadCountMap()
      this.$refs.nodeTab.$forceUpdate();
      if (type !== "nodes")
        this.$refs.edgeTab.$forceUpdate();
    }
    ,
    nodeDetails: function (nodeId) {
      this.$emit("selectionEvent", {edge: false, type: Object.keys(this.nodes)[this.nodeTab], id: nodeId})
    }
    ,
    resetFilters: function (type) {
      this.filters[type].suggestions = false;
      this.filters[type].query = "";
      this.filters[type].attribute = {operator: undefined, dist: false, name: undefined}
    },
    resetFilter: function (type) {
      this.filters[type].query = "";
      this.filters[type].attribute.dist = false;
      this.filters[type].operator = undefined;
    },
    edgeDetails: function (item) {
      let ids = (item.id === undefined ? item.ID : item.id).split("-")
      this.$emit("selectionEvent", {
        edge: true,
        type: Object.keys(this.edges)[this.edgeTab],
        id1: ids[0],
        id2: ids[1]
      })
    }
    ,
    headers: function (entity, node) {
      let out = [{text: "Select", align: 'start', sortable: false, value: "selected", width: "90px"}]
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
          text: attr.label,
          align: 'start',
          sortable: attr.numeric,
          list: attr.array,
          value: name,
          id: attr.id,
          // groupable: attr.name ==="evidenceTypes",
          numeric: attr.numeric
        })
      })
      this.update[entity] = false;
      out.push({text: "Info", align: 'start', sortable: false, value: "info"})
      return out
    }
    ,
    headerNames: function (entity, node, remove) {
      let headers = this.headers(entity, node);
      let out = []
      headers.forEach(header => out.push(header.text))
      let idx = out.indexOf("Select")
      if (idx > -1)
        out.splice(idx, 1)
      idx = out.indexOf("info")
      if (idx > -1)
        out.splice(idx, 1)
      return out;
    }
    ,
    selectEdges: function () {
      this.filterNodeModel = null
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
    },
    collapseGraph: function () {
      this.filterNodeModel = null
      this.collapse.nodes = Object.values(this.configuration.entityGraph.nodes).map(n => {
        return {name: n.name, selected: false, id: n.id, disabled: false}
      })
      this.collapse.edges = Object.values(this.configuration.entityGraph.edges).map(e => {
        return {name: e.name, selected: false, from: e.node1, to: e.node2, disabled: true}
      })
      this.collapse.self.selected = false
      if (this.collapse.edges.length < 2) {
        this.collapse.self.selected = true
        this.collapse.self.disabled = true
        this.collapse.edges.forEach(e => {
          e.selected = true
          this.collapse.edge1 = e.name
        })
      }
      if (this.collapse.nodes.length === 1) {
        this.collapse.nodes.forEach(n => {
          n.selected = true
        })
      }
      this.collapse.accept = this.isConnectedCollapse()
      this.collapse.show = true;
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
      this.filterNodeModel = null
      this.extension.edges = []
      this.$global.metagraph.edges.map(e => e.label).map(e => {
        let idx1 = Object.keys(this.attributes.nodes).indexOf(this.$utils.getNodes(this.$global.metagraph, e)[0])
        let idx2 = Object.keys(this.attributes.nodes).indexOf(this.$utils.getNodes(this.$global.metagraph, e)[1])

        if ((Object.keys(this.attributes.edges).indexOf(e) === -1 || idx1 === idx2) && idx1 + idx2 > -2)
          this.extension.edges.push({name: e, both: idx1 > -1 && idx2 > -1, induced: false, switch: false})
      })
      this.extension.show = true;
      this.$refs.extensionDialog.$forceUpdate()

    },
    getList: function (gid) {
      this.waiting = false
      this.clearLists()
      this.$http.get("/getGraphList?id=" + gid + "&cached=true").then(response => {
        if (response.data === null) {
          this.printNotification("The chosen graph does not exist!", 2)
          this.$router.push("/")
        } else
          this.loadList(response.data)
      }).then(() => {
        this.gid = gid;
      }).then(() => {
        this.init()
      }).then(() => {
        this.$emit("finishedEvent")
      }).catch(err => {
        console.error(err)
      })
    },
    getTotalCounts: function (entity) {
      let objects = Object.values(this[entity]);
      return objects === undefined || objects.length === 0 ? 0 : objects.map(e => e !== undefined ? e.length : 0).reduce((i, j) => i + j)
    },
    reloadCountMap: function (full) {
      this.configuration.countMap.nodes = this.getCountMap("nodes")
      this.configuration.countMap.edges = this.getCountMap("edges");

      if (Object.values(this.configuration.countMap.nodes).length > 0) {
        if (full)
          this.configuration.total = Object.values(this.configuration.countMap.nodes).map(o => o.total).reduce((i, s) => i + s)
        this.configuration.selected = Object.values(this.configuration.countMap.nodes).map(o => o.selected).reduce((i, s) => i + s)
      }
      if (Object.values(this.configuration.countMap.edges).length > 0) {
        if (full)
          this.configuration.total += Object.values(this.configuration.countMap.edges).map(o => o.total).reduce((i, s) => i + s)
        this.configuration.selected += Object.values(this.configuration.countMap.edges).map(o => o.selected).reduce((i, s) => i + s)
      }
      this.$emit("reloadSide")
    },
    getCountMap: function (entity) {
      let out = {}
      Object.keys(this[entity]).forEach(k => {
          out[k] = {name: k, total: this[entity][k].length, selected: this.filterSelected(this[entity][k]).length}
        }
      )
      return out
    },
    getExtendedColoring: function (entity, name, style) {
      return this.$utils.getColoringExtended(this.$global.metagraph, this.configuration.entityGraph, entity, name, style)
    },
    getExtendedNodes: function (name, not) {
      let nodes = this.$utils.getNodesExtended(this.configuration.entityGraph, name)
      if (not === undefined)
        return nodes;
      return nodes[0] === not ? nodes[1] : nodes[0]
    },
    getColoring: function (entity, name, style) {
      return this.$utils.getColoring(this.$global.metagraph, entity, name, style)
    },
    openAlgorithmDialogEvent: function (data) {
      this.filterNodeModel = null
      try {
        this.algorithmDialogParams.nodes = (data.selection ? this.nodes[data.type].filter(n => n.selected ) : this.nodes[data.type])
      }catch (e) {
        this.printNotification("The selected node-type is not part of your current network!",2)
        return;
      }
      if(this.algorithmDialogParams.nodes.length ===0){
        this.printNotification("No nodes are selected by your current settings!",2)
        return
      }
      this.algorithmDialogParams.nodeType=data.type;
      this.algorithmDialogParams.type=data.algorithms;
      this.$refs.algorithmDialog.show()
    },
    directionExtended: function (edge) {
      let e = Object.values(this.configuration.entityGraph.edges).filter(e => e.name === edge)[0];
      if (e.node1 === e.node2)
        return 0
      return e.directed ? 1 : 2
    },
    direction: function (edge) {
      let e = Object.values(this.$global.metagraph.edges).filter(e => e.label === edge)[0];
      if (e.from === e.to)
        return 0
      return e.directed ? 1 : 2
    },
    selectColor: function () {
      if (Object.values(this.configuration.countMap.nodes).map(n => n.selected).reduce((i, v) => i + v) === 0) {
        this.printNotification("Please select some nodes first!", 2)
        return;
      }
      this.selectionColor.show = true
    },
    countClick: function (entity, tabId, item) {
      let bool = entity === 'nodes' ? item : item.selected;
      let name = Object.keys(this.attributes[entity])[tabId]
      this.configuration.countMap[entity][name].selected += (bool ? 1 : -1)
      this.configuration.selected += (bool ? 1 : -1)
      if (entity === 'edges' && bool)
        this.selectDependentNodes(name, [item])
      this.$emit("reloadSide")
    },

    focus: function (entity, tabId) {
      if (entity === "nodes") {
        this.nodeTab = tabId;
        this.scrollFocus("nodeTitle")
      } else {
        this.edgeTab = tabId;
        this.scrollFocus("edgeTitle")
      }
    },
    scrollFocus: function (refName) {
      let element = this.$refs[refName];
      element.scrollIntoView()
    },
    toggleEdges: function (edgeList) {
      this.printNotification("Rewrite selection system!", 2)
      let inactive = false;
      let edges = {};
      let combinedIds = {};
      edgeList.forEach(e => {
        let id1 = parseInt(e.from.substring(4))
        let id2 = parseInt(e.to.substring(4))
        let combid = id1 + "-" + id2
        if (edges[e.group] === undefined) {
          edges[e.group] = {}
          combinedIds[e.group] = []
        }
        combinedIds[e.group].push({id: combid})
        if (edges[e.group][id1] === undefined)
          edges[e.group][id1] = []
        edges[e.group][id1].push(id2)
        if (!this.edges[e.group].filter(e2 => e2.id === combid)[0].selected)
          inactive = true;
      })
      Object.keys(edges).forEach(group => {
        if (inactive) {
          this.select("edges", group, edges[group])
        } else {
          this.unselect("edges", group, edges[group])
        }
        this.setDependentNodeSelection(group, combinedIds[group], inactive)
      })
      this.reloadCountMap()
    },
    toggleNodes: function (nodeList) {
      let inactive = false;
      let nodes = {}
      nodeList.forEach(n => {
        if (nodes[n.group] == null)
          nodes[n.group] = []
        nodes[n.group].push(n.id);
        if (!this.nodes[n.group].filter(n2 => n2.id === n.id)[0].selected) {
          inactive = true
        }
      })
      Object.keys(nodes).forEach(group => {
        if (inactive) {
          this.select("nodes", group, nodes[group])
        } else {
          this.unselect("nodes", group, nodes[group])
        }
        this.reloadCountMap()
      })
    },
  }
}
</script>

<style lang="sass">


</style>
