<template>
  <div>
    <v-container v-if="metagraph!==undefined">
      <v-card style="margin:5px;padding-bottom:15px" :loading="loading" ref="info">
        <template slot="progress">
          <v-card-title>General Information</v-card-title>
          <v-progress-circular v-if="!waiting"
                               color="primary"
                               size="50"
                               width="5"
                               indeterminate
          ></v-progress-circular>
          <i v-else>No data available!</i>
        </template>
        <v-container v-if="!loading">
          <v-card-title>General Information</v-card-title>
          <v-row>
            <v-col>
              <v-list v-if="attributes.nodes !=null">
                <v-list-item>
                  <b>Nodes ({{ getCounts('nodes') }})</b>
                </v-list-item>
                <v-list-item v-for="node in Object.values(configuration.countMap.nodes)"
                             :key="node.name">
                  <v-chip outlined @click="focus('nodes',Object.keys(attributes.nodes).indexOf(node.name))">
                    <v-icon left :color="getExtendedColoring('nodes',node.name,'light')">fas fa-genderless</v-icon>
                    {{ node.name }} ({{ node.selected }}/{{ node.total }})
                  </v-chip>
                </v-list-item>

              </v-list>
            </v-col>
            <v-col>
              <v-list v-if="attributes.edges!=null">
                <v-list-item>
                  <b>Edges ({{ getCounts('edges') }})</b>
                </v-list-item>
                <v-list-item v-for="edge in Object.values(configuration.countMap.edges)" :key="edge.name">
                  <v-chip outlined @click="focus('edges',Object.keys(attributes.edges).indexOf(edge.name))">
                    <v-icon left :color="getExtendedColoring('edges',edge.name,'light')[0]">fas fa-genderless</v-icon>
                    <template v-if="directionExtended(edge.name)===0">
                      <v-icon left>fas fa-undo-alt</v-icon>
                    </template>
                    <template v-else>
                      <v-icon v-if="directionExtended(edge.name)===1" left>fas fa-long-arrow-alt-right</v-icon>
                      <v-icon v-else left>fas fa-arrows-alt-h</v-icon>
                      <v-icon left :color="getExtendedColoring('edges',edge.name,'light')[1]">fas fa-genderless</v-icon>
                    </template>
                    {{ edge.name }} ({{ edge.selected }}/{{ edge.total }})

                  </v-chip>

                </v-list-item>
              </v-list>
            </v-col>
          </v-row>


        </v-container>
      </v-card>
      <!--      <v-card style="margin:5px">-->
      <!--        <v-card-title>General Options</v-card-title>-->
      <!--        <v-container>-->
      <!--          <v-row>-->
      <!--            -->
      <!--          </v-row>-->
      <!--        </v-container>-->
      <!--      </v-card>-->
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
          <!--          <v-progress-linear-->
          <!--            color="primary"-->
          <!--            height="5"-->
          <!--            indeterminate-->
          <!--          ></v-progress-linear>-->
          <!--          <v-card-title>Nodes</v-card-title>-->
        </template>
        <template v-if="!loading" v-show="!nodeTabLoading">
          <v-card-title id="nodes" ref="nodeTitle" v-on:mouseenter="nodeOptionHover=true"
                        v-on:mouseleave="nodeOptionHover=false">
            Nodes
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
          <i v-if="!update.nodes && nodes!=null &&Object.keys(nodes).length === 0">no node entries</i>
          <template v-if="nodes !=null && Object.keys(nodes).length>0">
            <v-tabs next-icon="mdi-arrow-right-bold-box-outline"
                    prev-icon="mdi-arrow-left-bold-box-outline"
                    show-arrows
                    v-model="nodeTab"
                    @change="resetFilters('nodes')"
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
                class="elevation-1"
                :headers="headers('nodes',Object.keys(nodes)[nodeTab])"
                :items="configuration.showAll ? (filters.nodes.attribute.dist ? distinctFilter('nodes',nodes[Object.keys(nodes)[nodeTab]],nodeTab): nodes[Object.keys(nodes)[nodeTab]]) : filterSelected(nodes[Object.keys(nodes)[nodeTab]])"
                item-key="id"
                :loading="nodeTabLoading"
                loading-text="Loading... Please wait"
                :search="filters.nodes.query"
                :custom-filter="filterNode"
              >

                <template v-slot:top>
                  <v-container style="margin-top: 15px;margin-bottom: -40px">
                    <v-row>
                      <v-col
                      >
                        <v-list-item>
                          <v-switch
                            label="Suggestions"
                            v-model="filters.nodes.suggestions"
                            v-on:click="toggleSuggestions('nodes')"
                          >
                          </v-switch>
                        </v-list-item>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col
                        cols="2"
                        v-show="!filters.nodes.suggestions"
                      >
                        <v-select
                          :items="headerNames('nodes',Object.keys(nodes)[nodeTab])"
                          label="Attribute"
                          v-model="filters.nodes.attribute.name"
                          @change="resetFilter('nodes')"
                          outlined
                          style="width: 100%"
                        ></v-select>
                      </v-col>
                      <v-col
                        cols="2"
                        v-if="!filters.nodes.suggestions"
                      >
                        <v-select
                          v-if="filters.nodes.attribute.name === undefined || filters.nodes.attribute.name == null || !isDistinctAttribute('nodes',filters.nodes.attribute.name)"
                          v-model="filters.nodes.attribute.operator"
                          :disabled="filters.nodes.attribute.name === undefined || filters.nodes.attribute.name == null || filters.nodes.attribute.name.length ===0"
                          :items="operatorNames('nodes',Object.keys(nodes)[nodeTab],filters.nodes.attribute.name)"
                          label="Operator"
                          outlined
                          style="width: 100%"
                        ></v-select>
                        <v-select
                          v-else
                          v-model="filters.nodes.attribute.operator"
                          @change="filters.nodes.attribute.dist=true"
                          :items="attributes.nodes[Object.keys(nodes)[nodeTab]].filter(a=>a.label===filters.nodes.attribute.name)[0].values"
                          label="Value"
                          outlined
                          style="width: 100%"
                        >
                        </v-select>
                      </v-col>
                      <v-col
                        :cols="(filters.nodes.suggestions)?12:8"
                        style="display: flex; justify-content: center"
                      >
                        <v-text-field
                          :disabled="filters.nodes.attribute.name == null || filters.nodes.attribute.name.length ===0 || filters.nodes.attribute.operator == null|| filters.nodes.attribute.dist"
                          clearable
                          v-show="!filters.nodes.suggestions"
                          v-model="filters.nodes.query"
                          label="Query (case sensitive)"
                          class="mx-4"
                        ></v-text-field>
                        <v-autocomplete
                          chips
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

                    </v-row>

                  </v-container>
                </template>
                <template v-slot:item.selected="{item}">
                  <v-row>
                    <v-col cols="2">
                      <v-checkbox
                        style="margin-top: -4px"
                        :key="item.id"
                        v-model="item.selected"
                        hide-details
                        primary
                        @click="countClick('nodes',nodeTab,item.selected)"
                      >
                      </v-checkbox>
                    </v-col>
                    <v-col cols="1">
                      <v-tooltip right>
                        <template v-slot:activator="{ on, attrs }"
                                  v-if="marks.nodes && marks.nodes[Object.keys(attributes.nodes)[nodeTab]] &&marks.nodes[Object.keys(attributes.nodes)[nodeTab]].indexOf(item.id)>-1">
                          <v-icon
                            color="primary"
                            dark
                            v-bind="attrs"
                            v-on="on"
                          >
                            fas fa-star
                          </v-icon>
                        </template>
                        <span>Added by Algorithm</span>
                      </v-tooltip>
                    </v-col>
                  </v-row>

                </template>
                <template v-slot:item.actions="{item}">
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
                        <span>Focus in graph</span>
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
                        <span>id:{{ item.id }}</span>
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
          <!--          <v-progress-linear-->
          <!--            color="primary"-->
          <!--            height="5"-->
          <!--            indeterminate-->
          <!--          ></v-progress-linear>-->
          <!--          <v-card-title>Edges</v-card-title>-->
        </template>
        <template v-if="!loading">
          <v-card-title id="edges" ref="edgeTitle" v-on:mouseenter="edgeOptionHover=true"
                        v-on:mouseleave="edgeOptionHover=false">Edges
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
          <i v-if="edges !=null && Object.keys(edges).length === 0">no edge entries</i>
          <template v-if="edges !=null && Object.keys(edges).length>0">
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
                class="elevation-1"
                :headers="headers('edges',Object.keys(edges)[edgeTab])"
                :items="configuration.showAll ? (filters.edges.attribute.dist ? distinctFilter('edges',edges[Object.keys(edges)[edgeTab]],edgeTab): edges[Object.keys(edges)[edgeTab]]) : filterSelected(edges[Object.keys(edges)[edgeTab]])"
                :search="filters.edges.query"
                :custom-filter="filterEdge"
                loading-text="Loading... Please wait"
                item-key="id"
              >
                <template v-slot:top>
                  <v-container style="margin-top: 15px;margin-bottom: -40px">
                    <v-row>
                      <v-col
                        cols="2"
                      >
                        <v-select
                          :items="headerNames('edges',Object.keys(edges)[edgeTab])"
                          label="Attribute"
                          v-model="filters.edges.attribute.name"
                          @change="resetFilter('edges')"
                          outlined
                          style="width: 100%"
                        ></v-select>
                      </v-col>
                      <v-col
                        cols="1"
                        v-if="!filters.edges.suggestions"
                      >

                        <v-select
                          v-if="filters.edges.attribute.name === undefined || filters.edges.attribute.name == null || !isDistinctAttribute('edges',filters.edges.attribute.name)"
                          v-model="filters.edges.attribute.operator"
                          :items="operatorNames('edges',Object.keys(edges)[edgeTab],filters.edges.attribute.name)"
                          :disabled="filters.edges.attribute.name === undefined || filters.edges.attribute.name == null || filters.edges.attribute.name.length ===0"
                          label="Operator"
                          outlined
                          style="width: 100%"
                        ></v-select>
                        <v-select
                          v-else
                          v-model="filters.edges.attribute.operator"
                          @change="filters.edges.attribute.dist=true"
                          :items="attributes.edges[Object.keys(edges)[edgeTab]].filter(a=>a.label===filters.edges.attribute.name)[0].values"
                          label="Value"
                          outlined
                          style="width: 100%"
                        >
                        </v-select>
                      </v-col>
                      <v-col
                        cols="9"
                      >
                        <v-text-field
                          :disabled="filters.edges.attribute.name == null || filters.edges.attribute.name.length ===0 || filters.edges.attribute.operator == null|| filters.edges.attribute.dist"
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
                        style="margin-top: -4px"
                        @click="countClick('edges',edgeTab,item)"
                      ></v-checkbox>
                    </v-col>
                    <v-col cols="1">
                      <v-tooltip right>
                        <template v-slot:activator="{ on, attrs }"
                                  v-if="marks.edges && marks.edges[Object.keys(attributes.edges)[edgeTab]] &&marks.edges[Object.keys(attributes.edges)[edgeTab]].indexOf(item.id)>-1">
                          <v-icon
                            color="primary"
                            dark
                            v-bind="attrs"
                            v-on="on"
                          >
                            fas fa-star
                          </v-icon>
                        </template>
                        <span>Added by Algorithm</span>
                      </v-tooltip>
                    </v-col>
                  </v-row>
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
                <template v-slot:item.actions="{item}">
                  <v-row>
                    <!--                    <v-col cols="1">-->
                    <!--                      <v-tooltip right>-->
                    <!--                        <template v-slot:activator="{ on, attrs }">-->
                    <!--                          <v-icon-->
                    <!--                            small-->
                    <!--                            left-->
                    <!--                            color="primary"-->
                    <!--                            dark-->
                    <!--                            v-bind="attrs"-->
                    <!--                            v-on="on"-->
                    <!--                            v-on:click="showInGraph('node',item)"-->
                    <!--                          >-->
                    <!--                            fas fa-project-diagram-->
                    <!--                          </v-icon>-->
                    <!--                        </template>-->
                    <!--                        <span>Focus in graph</span>-->
                    <!--                      </v-tooltip>-->
                    <!--                    </v-col>-->
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
                        <span>id:{{ item.id }}</span>
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
      style="z-index: 1000"
    >
      <v-card v-if="extension.show">
        <v-card-title class="headline">
          Add more edges to your current graph
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
                <v-icon left :color="getColoring('edges',attr.name,'light')[0]">fas fa-genderless</v-icon>
                    <template v-if="direction(attr.name)===0">
                      <v-icon left>fas fa-undo-alt</v-icon>
                    </template>
                    <template v-else>
                      <v-icon>fas fa-long-arrow-alt-right</v-icon>
                      <v-icon left :color="getColoring('edges',attr.name,'light')[1]">fas fa-genderless</v-icon>
                    </template>
                {{ attr.name }}
              </span>
            </v-list-item>
            <template v-if="attr.both>0 && attr.selected">
              <v-list-item :key="attr.name+'_addition'">
                <v-divider vertical style="margin-right:15px; margin-left:15px"></v-divider>
                <span>Extend by new nodes</span>
                <v-switch v-model="attr.induced" style="margin-left:5px"></v-switch>
                <span>Add induced edges only</span>
              </v-list-item>
              <v-list-item v-if="attr.name==='DisorderHierarchy'">
                <v-divider vertical style="margin-right:15px; margin-left:15px"></v-divider>
                <span>Add children</span>
                <v-switch v-model="attr.switch" style="margin-left:5px"></v-switch>
                <span>Add parents</span>
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
      style="z-index: 1000"
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
      style="z-index: 1000"
    >
      <v-card
        v-if="options !== undefined && options.type != null && options.attributes !=null && attributes[options.type]!=null">
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
              {{ Object.keys(attributes[options.type])[name] }}
            </v-tab>
          </v-tabs>
          <v-tabs-items>
            <v-list>
              <v-list-item v-for="attr in options.attributes[optionTab]" :key="attr.name">
                <v-switch v-model="attr.selected" :label="attr.label" :disabled="(attr.name === 'id')">
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
      style="z-index: 1000"
    >
      <v-card v-if="selectionDialog.show && selectionDialog.type !== undefined">
        <v-list>
          <v-list-item>
            <v-list-item-title class="text-h4">
              Select induced graph
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
    <v-dialog
      v-model="selectionColor.show"
      persistent
      max-width="500"
      style="z-index: 1000"
    >
      <v-card>
        <v-card-title class="headline">
          Recolor Selected Nodes
        </v-card-title>
        <v-card-text>Adjust temporary color and name of your selection in the graph. The name is for the legend.
        </v-card-text>
        <v-divider></v-divider>
        <v-list>
          <v-list-item>
            <v-list-item-content>
              <v-text-field v-model=selectionColor.name label="Name"
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
import Utils from "../../scripts/Utils";

export default {
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
      marks: {},
      attributes: {},
      countMap: undefined,
      nodeTab: undefined,
      edgeTab: undefined,
      selectAllModel: {nodes: {}, edges: {}},
      nodepage: {},
      nodeOptionHover: false,
      edgeOptionHover: false,
      optionDialog: false,
      metagraph: undefined,
      waiting: true,
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
      selectionColor: {show: false, name: "", color: {}}
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
    this.reloadMetagraph()
    if (this.gid !== undefined)
      this.getList(this.gid, this.metagraph)
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
      this.marks = {}
      this.backup = {nodes: {}, edges: {}}
      this.gid = this.$route.params["gid"]
      if (this.gid !== undefined)
        this.getList(this.gid, this.metagraph)
    },
    init: function () {
      if (this.gid === undefined)
        return
      this.$http.get("/getConnectionGraph?gid=" + this.gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.configuration.entityGraph = data
        this.reloadCountMap()
        this.loading = false
        this.$emit("loadLegendEvent", true)
      })
        .catch(err => console.log(err))
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
    },
    applyMultiSelect: function (selection) {
      if (this.prefixMap === undefined) {
        this.prefixMap = {}
        Object.values(this.metagraph.nodes).forEach(n => this.prefixMap[n.group.substring(0, 3)] = n.group)
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

    }
    ,
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
              return e2.ids.length - e1.ids.length
            })
            this.suggestions[type].data = data.suggestions;
          }).catch(err =>
            console.log(err)
          ).finally(() =>
            this.suggestions[type].loading = false
          )
        }).catch(err =>
          console.log(err))
      }
    },
    loadList: function (data) {
      this.attributes = {};
      this.edges = {};
      this.nodes = {};
      this.marks = {};
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
        this.marks = data.marks;
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
    reloadTables: function () {
      this.$nextTick().then(() => {
        let tmp = this.nodeTab;
        this.nodeTab = -1
        this.nodeTab = tmp
        tmp = this.edgeTab
        this.edgeTab = -1
        this.edgeTab = tmp
        this.$refs.nodeTab.$forceUpdate()
        this.$refs.edgeTab.$forceUpdate()
      })
    },
    countSelected: function (type, name) {
      return this[type][name].filter(item => item.selected).length
    },
    getSelectedCount: function (type, name) {
      return this.configuration.countMap[type][name].selected
    },
    applySuggestion: function (val) {
      this.nodeTabLoading = true
      if (val != null) {
        this.resetSuggestions()
        let nodes = this.nodes[Object.keys(this.nodes)[this.nodeTab]]
        this.backup.nodes[this.nodeTab] = nodes
        this.suggestions.nodes.chosen = this.suggestions.nodes.data.filter(item => item.value === val).flatMap(item => item.ids);
        this.nodes[Object.keys(this.nodes)[this.nodeTab]] = nodes.filter((i, k) => this.suggestions.nodes.chosen.indexOf(i.id) !== -1)
      } else {
        this.resetSuggestions(true)
      }
      this.nodeTabLoading = false
    },
    resetSuggestions: function (full) {
      if (full) {
        this.suggestions.nodes.chosen = undefined
      }
      if (this.backup.nodes[this.nodeTab])
        this.nodes[Object.keys(this.nodes)[this.nodeTab]] = this.backup.nodes[this.nodeTab]
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
      for (let node in Object.keys(this.attributes.nodes)) {
        let models = []
        this.attributes.nodes[Object.keys(this.attributes.nodes)[node]].forEach(attr => {
          models.push({name: attr.name, label: attr.label, selected: attr.list})
        })
        this.options.attributes[node] = models;
      }
      this.options.type = "nodes";
    }
    ,
    edgeOptions: function () {
      this.optionsTab = 0
      this.options["title"] = (Object.keys(this.edges).length > 1) ? "Edges" : "Edge"

      this.options["attributes"] = {}
      for (let edge in Object.keys(this.attributes.edges)) {
        let models = []
        this.attributes.edges[Object.keys(this.attributes.edges)[edge]].forEach(attr => {
          models.push({name: attr.name, label: attr.label, selected: attr.list})
        })
        this.options.attributes[edge] = models;
      }
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
          console.log(err)
        })
      } else {
        this.update.nodes = true;
      }

    },
    extensionDialogResolve: function (apply) {
      let selected = this.extension.edges.filter(e => e.selected)
      let payload = {
        gid: this.gid,
        // nodes: this.extension.nodes.filter(n => n.selected).map(n => n.name),
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
      }).catch(err => console.log(err))
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
      if (this.metagraph.edges.flatMap(e => [e.label, e.title]).indexOf(this.collapse.edgeName) > -1) {
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
      }).catch(err => console.log(err))
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
        console.log(err)
      })
    }
    ,
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
        console.log(err)
      })
    }
    ,
    select: function (type, name, ids) {
      // let data = {nodes: this.nodes, edges: this.edges}
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
      this.$nextTick().then(() => {
        if ("nodes" === type)
          this.$refs.nodeTab.$forceUpdate()
        else
          this.$refs.edgeTab.$forceUpdate()
      })
    },
    selectDependentNodes: function (type, edges) {
      let nodes = Utils.getNodesExtended(this.configuration.entityGraph, type)
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
      this.nodes[nodeName1].filter(n => nodeIds1.indexOf(n.id) > -1 && !n.selected).forEach(n => {
        if (!n.selected) {
          n.selected = true
          this.countClick('nodes', nodeTab1, true)
        }
      })

      this.nodes[nodeName2].filter(n => nodeIds2.indexOf(n.id) > -1 && !n.selected).forEach(n => {
        if (!n.selected) {
          n.selected = true
          this.countClick('nodes', nodeTab2, true)
        }
      })
    },

    selectAll: function (type) {
      let tab = (type === "nodes") ? this.nodeTab : this.edgeTab
      let name = Object.keys(this[type])[tab]
      let items = this[type][name]
      let isDistinct = this.isDistinctAttribute(type, this.filters[type].attribute.name)
      if (isDistinct) {
        this.distinctFilter(type, items).forEach(item => {
          item.selected = true
        })
      } else {
        let filterActive = this.filters[type].attribute.name !== undefined && this.filters[type].attribute.name.length > 0 && this.filters[type].query !== null && this.filters[type].query.length > 0 && this.filters[type].attribute.operator !== undefined && this.filters[type].attribute.operator.length > 0
        items.forEach(item => {
            if (type === "nodes") {
              if (!filterActive || (filterActive && this.filterNode(undefined, this.filters[type].query, item)))
                item.selected = true;
            } else if (!filterActive || (filterActive && this.filterEdge(undefined, this.filters[type].query, item)))
              item.selected = true
          }
        )
      }
      if (type === "edges")
        this.selectDependentNodes(name, items)
      this.$nextTick().then(() => {
        this.reloadCountMap()
      }).then(() => {
        if ("edges" === type)
          this.$refs.edgeTab.$forceUpdate()
        this.$refs.nodeTab.$forceUpdate()
      })
    }
    ,
    deselectAll: function (type) {
      let data = {nodes: this.nodes, edges: this.edges}
      if (type === "all") {
        this.filterNodeModel = null
        this.$nextTick(()=> Object.values(data).forEach(set => Object.values(set).forEach(type => type.forEach(n => this.$set(n,"selected",false)))))
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
              if (!filterActive || (filterActive && this.filterNode(undefined, this.filters[type].query, item)))
                item.selected = false;
            } else if (!filterActive || (filterActive && this.filterEdge(undefined, this.filters[type].query, item)))
              item.selected = false;
          })
        }
      }

      this.$nextTick().then(() => {
        this.reloadCountMap()
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
        type: "edge",
        name: Object.keys(this.edges)[this.edgeTab],
        id1: ids[0],
        id2: ids[1]
      })
    }
    ,
    headers: function (entity, node) {
      let out = [{text: "Select", align: 'start', sortable: false, value: "selected"}]
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
          numeric: attr.numeric
        })
      })
      this.update[entity] = false;
      out.push({text: "actions", align: 'start', sortable: false, value: "actions"})
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
      idx = out.indexOf("actions")
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
        return {name: e.name, selected: false, from: e.node1, to: e.node2, disabled: false}
      })
      this.collapse.self.selected = false
      if (this.collapse.edges.length < 2) {
        this.collapse.self.selected = true
        this.collapse.self.disabled = true
        this.collapse.edges.forEach(e => {
          e.selected = true
          this.collapse.edge1 = e.name
        })
      } else if (this.collapse.edges.length === 2 && !this.collapse.self.selected) {
        this.collapse.edges.forEach(e => {
          e.selected = true
          if (this.collapse.edge1.length === 0)
            this.collapse.edge1 = e.name
          else
            this.collapse.edge2 = e.name
        })
      } else if (this.collapse.nodes.length > 1) {
        this.collapse.edges.forEach(e => {
          e.disabled = true;
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
      this.metagraph.edges.map(e => e.label).map(e => {
        let idx1 = Object.keys(this.attributes.nodes).indexOf(Utils.getNodes(this.metagraph, e)[0])
        let idx2 = Object.keys(this.attributes.nodes).indexOf(Utils.getNodes(this.metagraph, e)[1])

        if ((Object.keys(this.attributes.edges).indexOf(e) === -1 || idx1 === idx2) && idx1 + idx2 > -2)
          this.extension.edges.push({name: e, both: idx1 > -1 && idx2 > -1, induced: false, switch: false})
      })
      this.extension.show = true;
      this.$refs.extensionDialog.$forceUpdate()

    },
    reloadMetagraph: function () {
      this.$http.get("/getMetagraph").then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(response => {
        this.setMetagraph(response)
      }).catch(err => console.log(err))
    },
    getList: function (gid, metagraph) {
      this.waiting = false
      this.setMetagraph(metagraph)
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
        console.log(err)
      })
    },
    getCounts: function (entity) {
      let objects = Object.values(this[entity]);
      return objects === undefined || objects.length === 0 ? 0 : objects.map(e => e !== undefined ? e.length : 0).reduce((i, j) => i + j)
    },
    reloadCountMap: function () {
      this.configuration.countMap.nodes = this.getCountMap("nodes")
      this.configuration.countMap.edges = this.getCountMap("edges");

      if (Object.values(this.configuration.countMap.nodes).length > 0) {
        this.configuration.total = Object.values(this.configuration.countMap.nodes).map(o => o.total).reduce((i, s) => i + s)
        this.configuration.selected = Object.values(this.configuration.countMap.nodes).map(o => o.selected).reduce((i, s) => i + s)
      }
      if (Object.values(this.configuration.countMap.edges).length > 0) {
        this.configuration.selected += Object.values(this.configuration.countMap.edges).map(o => o.selected).reduce((i, s) => i + s)
        this.configuration.total += Object.values(this.configuration.countMap.edges).map(o => o.total).reduce((i, s) => i + s)
      }
      this.$emit("reloadSide")
      if (this.$refs.info !== undefined) {
        this.$refs.info.$forceUpdate()
      }
    },
    getCountMap: function (entity) {
      let out = {}
      Object.keys(this[entity]).forEach(k =>
        out[k] = {name: k, total: this[entity][k].length, selected: this.filterSelected(this[entity][k]).length}
      )
      return out
    },
    getExtendedColoring: function (entity, name, style) {
      if (this.metagraph === undefined) {
        return this.reloadMetagraph().then(function () {
          return this.getExtendedColoring(entity, name, style)
        })
      }
      return Utils.getColoringExtended(this.metagraph, this.configuration.entityGraph, entity, name, style)
    },
    getExtendedNodes: function (name, not) {
      let nodes = Utils.getNodesExtended(this.configuration.entityGraph, name)
      if (not === undefined)
        return nodes;
      return nodes[0] === not ? nodes[1] : nodes[0]
    },
    getColoring: function (entity, name, style) {
      if (this.metagraph === undefined) {
        return this.reloadMetagraph().then(function () {
          return this.getColoring(entity, name, style)
        })
      }
      return Utils.getColoring(this.metagraph, entity, name, style)
    },
    executeAlgorithm: function (algorithm, params) {
      this.filterNodeModel = null
      let payload = {userId: this.uid, graphId: this.gid, algorithm: algorithm, params: params}
      payload.selection = params.selection
      payload.experimentalOnly = params.experimentalOnly
      if (params.selection)
        payload["nodes"] = this.nodes[params.type].filter(n => n.selected).map(n => n.id)
      if (algorithm === "diamond" || algorithm === "trustrank" || algorithm === "centrality" || algorithm === "must") {
        if (this.configuration.countMap.nodes[params.type] === undefined || (params.selection && this.configuration.countMap.nodes[params.type].selected === 0)) {
          this.printNotification("Cannot execute " + algorithm + " without seed nodes!", 1)
          return;
        }
      }
      this.$http.post("/submitJob", payload).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.$emit("addJobEvent", data)
      }).catch(console.log)
    },
    directionExtended: function (edge) {
      let e = Object.values(this.configuration.entityGraph.edges).filter(e => e.name === edge)[0];
      if (e.node1 === e.node2)
        return 0
      return e.directed ? 1 : 2
    },
    direction: function (edge) {
      let e = Object.values(this.metagraph.edges).filter(e => e.label === edge)[0];
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
    }
  }
}
</script>

<style lang="sass">


</style>
