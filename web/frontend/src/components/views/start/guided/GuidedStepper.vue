<template>
  <v-card style="margin-bottom: 35px">
    <v-dialog
      v-model="verify"
      persistent
      style="z-index: 1001"
      max-width="500"
    >
      <v-card>
        <v-card-title>Confirm Navigation</v-card-title>
        <v-card-text>Do you really ant to reset the current page?
        </v-card-text>
        <v-divider></v-divider>

        <v-card-actions style="display: flex; justify-content: flex-end">
          <v-btn style="margin-left: 5px; margin-right: 5px;" color="primary"
                 @click="verify = false">
            <v-icon left>fas fa-angle-left</v-icon>
            <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
            Stay
          </v-btn>
          <v-btn style="margin-left: 5px; margin-right: 5px;" color="error"
                 @click="verify = false; makeStep('cancel')">
            <v-icon left>fas fa-times</v-icon>
            <v-divider vertical style="border-color: white; margin-right: 5px;"></v-divider>
            Cancel
          </v-btn>
        </v-card-actions>
      </v-card>

    </v-dialog>
    <ConnectorDialog ref="connectors" :node-source-type="nodeIdTypeList[connectorTypeId]"
                     :node-type="connectorTypeId!=null ? nodeList[connectorTypeId].value: undefined"
                     @printNotification="printNotification"
                     @updateConnectorCount="connectorCount=$refs.connectors && $refs.connectors.getList() ? $refs.connectors.getList().length :0"></ConnectorDialog>
    <div style="display: flex; justify-content: flex-end; margin-left: auto; ">
      <v-tooltip left>
        <template v-slot:activator="{on, attrs}">
          <v-btn icon style="padding:1em" color="red darker" @click="verify=true" v-on="on" v-bind="attrs">
            <v-icon size="2em">far fa-times-circle</v-icon>
          </v-btn>
        </template>
        <div>Close <b>Guided Exploration</b> and return to the according start page.</div>
      </v-tooltip>
    </div>
    <div style="display: flex; color: dimgray; padding-bottom: 8px; padding-top: 25px">
      <v-card-title style="font-size: 2.5em; justify-content: center; margin-left: auto; margin-right: auto">
        Guided Exploration
      </v-card-title>
    </div>
    <v-card-subtitle> Use the <b>Guided Exploration</b> to create a network based on specific <b><i>start</i></b> and
      <b><i>target</i></b> nodes types.
      Select a <b><i>path</i></b> connecting these metanodes and control the result through additional parameters.
    </v-card-subtitle>
    <v-stepper
      alt-labels
      v-model="step"
      flat
    >
      <v-stepper-header>
        <v-stepper-step step="1" :complete="step>1">
          Select Nodes
          <small v-if="sourceTypeId!==undefined & targetTypeId!==undefined">
            <span>{{ nodeList[sourceTypeId].text }}</span>
            ->
            <span>{{ nodeList[targetTypeId].text }}</span>
          </small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="2" :complete="step>2 ">
          Select Path
          <small v-if="selectedPath!==undefined && selectedPath.length>0"
                 style="margin-left: -100px;margin-right: -100px">
          <span>{{ nodeList[sourceTypeId].text }}
          <span v-for="(edge,idx) in selectedPath" :key="'stepper_'+idx+'_'+edge.label">
            -> {{ getNodeLabel(edge.label, [edge.direction ? 1 : 0]) }}
          </span>
          </span>
          </small>
        </v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step step="3">
          Network
        </v-stepper-step>
      </v-stepper-header>

      <v-stepper-items>
        <v-stepper-content step="1">
          <v-card
            v-show="step===1"
            class="mb-4"
            max-height="1350px"
            height="1350px"
          >

            <v-card-subtitle class="headline" style="color: black; text-align: left; margin-left: 5vw">Node
              configuration
              <GuidedExamples style="display: inline-block" @exampleEvent="applyExample"
                              @addNodesEvent="addToSelection"/>
            </v-card-subtitle>
            <v-card-subtitle style="margin-top: -25px">
              <ul>
                <li style="margin-left: 0;">
                  Configure start and target nodes of interest:
                </li>
                <li>
                  <b>1. </b>Source node configuration
                </li>
                <li style="padding-left: 32px;">
                  <b>a. </b>Select the type of the source node
                </li>
                <li style="padding-left: 32px;">
                  <b>b. </b>Select specific entries
                </li>
                <li>
                  <b>2. </b>Target node configuration
                </li>
                <li style="padding-left: 32px;"><b>a. </b>Select the type of the target node</li>
                <li style="padding-left: 32px;"><b>b. </b>(Optionally: select specific target entries)</li>
              </ul>
            </v-card-subtitle>
            <v-tooltip top>
              <template v-slot:activator="{on, attrs}">
                <v-btn @click="copySourcesToTargets" outlined icon style="margin-top: 5px" v-bind="attrs" v-on="on"
                       :disabled="sourceTypeId==null || (targetTypeId!=null && sourceTypeId !== targetTypeId) || (targetTypeId!=null && sourceTypeId===targetTypeId && ($refs.targetTable !=null  &&$refs.targetTable.getSeeds().length>0))|| ($refs.sourceTable == null || $refs.sourceTable.getSeeds().length===0)">
                  <v-icon>fas fa-exchange-alt</v-icon>
                </v-btn>
              </template>
              <div>Set target list to the source list.</div>
            </v-tooltip>

            <div style="height: 940px; display: flex; margin-top:10px;">
              <div style="justify-self: flex-start; width: 48%;">
                <div style="display: flex; justify-content: flex-start;">
                  <v-card-title style="padding-top: 16px;"><i v-show="!sourceTypeId"
                                                              style="color: red">*</i>1a. Select the source node type:
                    <v-select item-value="id" :items="nodeList" v-model="sourceTypeId"
                              placeholder="Select start type"
                              :disabled="$refs.sourceTable!=null && $refs.sourceTable.getSeeds().length>0"
                              style="display: inline-block; min-width: 9.5em; max-width: 11.2em; margin-left: 25px">
                    </v-select>
                  </v-card-title>
                </div>
                <div v-if="sourceTypeId!==undefined" style="margin-top: 25px;">
                  <div style="display: flex">
                    <v-card-title style="padding-top: 16px;justify-self: flex-start"><i
                      v-show="!$refs.sourceTable || $refs.sourceTable.getSeeds().length<1"
                      style="color: red">*</i>1b. Prefilter the sources:
                    </v-card-title>
                  </div>
                  <div style="display: flex; margin-top: 16px">
                    <div style="justify-content: flex-start">
                      <v-card-title style="text-align: left; margin-left: 20px;  color: rgb(128,128,128)" class="title">
                        Option 1: Add
                        {{ nodeList[sourceTypeId].value }} by association to <i
                        style="margin-left: 8px">{{ suggestionType[0] }}</i>
                      </v-card-title>
                    </div>
                  </div>
                  <div style="display: flex">

                    <v-select :items="getSuggestionSelection(0)" v-model="suggestionType[0]"
                              placeholder="connected to" style="width: 35%; justify-self: flex-start"></v-select>
                    <SuggestionAutocomplete :suggestion-type="suggestionType[0]" :index="0"
                                            :target-node-type="this.nodeList[[[this.sourceTypeId, this.targetTypeId][0]]].value"
                                            @addToSelectionEvent="addToSelection"
                                            style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
                  </div>
                  <div style="display: flex; justify-content: flex-start; margin-top: 32px; margin-left: 20px">

                    <NodeInput text="Option 2: Provide node IDs by" @addToSelectionEvent="addToSourceSelection"
                               :idName="nodeIdTypeList[sourceTypeId]" :nodeType="nodeList[sourceTypeId].value"
                               @printNotificationEvent="printNotification"></NodeInput>
                  </div>
                  <SeedTable ref="sourceTable" v-show="sourceTypeId!==undefined" :download="true" :remove="true"
                             :filter="true"
                             @printNotificationEvent="printNotification"
                             height="360px" @updateCount="updateSourceCount"
                             :title="'Source nodes ('+sourceCount+')'"
                             :nodeName="nodeList[sourceTypeId].value"></SeedTable>
                </div>
              </div>
              <div style="justify-self: center; margin-left: auto; padding-bottom:120px">
                <v-divider vertical></v-divider>
              </div>
              <div ref="targetSide" style="justify-self: flex-end; margin-left: auto; width: 48%;">
                <div style="display: flex; justify-content: flex-start;">
                  <v-card-title style=""><i v-show="!sourceTypeId"
                                            style="color: red">*</i>2a. Select the target node type:
                    <v-select item-value="id" :items="nodeList" v-model="targetTypeId"
                              placeholder="Select target type"
                              :disabled="$refs.targetTable!=null && $refs.targetTable.getSeeds().length>0"
                              style="min-width: 9.5em; max-width: 11.2em; margin-left: 25px; display: inline-block">
                    </v-select>
                  </v-card-title>
                </div>
                <div v-if="targetTypeId!==undefined" style="margin-top: 25px;">
                  <div style="display: flex">
                    <v-card-title style="justify-self: flex-start">2b. Prefilter the targets
                      (optional):
                    </v-card-title>
                  </div>
                  <div style="display: flex; margin-top: 16px">
                    <div style="justify-content: flex-start">
                      <v-card-title style="text-align: left; margin-left: 20px;  color: rgb(128,128,128)" class="title">
                        Option 1: Add
                        {{ nodeList[targetTypeId].value }} by association to <i
                        style="margin-left: 8px">{{ suggestionType[1] }}</i>
                      </v-card-title>
                    </div>
                  </div>
                  <div style="display: flex">

                    <v-select :items="getSuggestionSelection(1)" v-model="suggestionType[1]"
                              placeholder="connected to" style="width: 35%; justify-self: flex-start"></v-select>
                    <SuggestionAutocomplete :suggestion-type="suggestionType[1]" :index="1"
                                            :target-node-type="this.nodeList[[this.targetTypeId]].value"
                                            @addToSelectionEvent="addToSelection"
                                            style="justify-self: flex-end;margin-left: auto"></SuggestionAutocomplete>
                  </div>
                  <div style="display: flex; justify-content: flex-start; margin-top: 32px; margin-left: 20px">
                    <NodeInput text="Option 2: Provide node IDs by" @addToSelectionEvent="addToTargetSelection"
                               :idName="nodeIdTypeList[targetTypeId]" :nodeType="nodeList[targetTypeId].value"
                               @printNotificationEvent="printNotification"></NodeInput>
                  </div>

                  <SeedTable ref="targetTable" v-show="targetTypeId!==undefined" :download="true" :remove="true"
                             @printNotificationEvent="printNotification"
                             height="360px"
                             :title="'Target nodes ('+targetCount+')'" @updateCount="updateTargetCount"
                             :nodeName="nodeList[targetTypeId].value"></SeedTable>
                </div>
              </div>
            </div>
          </v-card>
          <v-card-actions style="display: flex; margin-left: 18px; justify-content: flex-end">
            <ButtonNext
              :disabled="sourceTypeId===undefined || targetTypeId===undefined  ||  ($refs.sourceTable &&$refs.sourceTable.getSeeds().length<1)"
              @click="makeStep"></ButtonNext>
            <ButtonCancel @click="makeStep"></ButtonCancel>
          </v-card-actions>
        </v-stepper-content>
        <v-stepper-content step="2">
          <div style="display: flex">
            <v-card
              v-if="step===2"
              class="mb-4"
              max-width="1300px"
              flat
              style="justify-content: center; margin-right: auto; margin-left: auto"
            >
              <v-card-subtitle class="headline" style="color: black; text-align: left; margin-left: 5vw">Path
                Selection
              </v-card-subtitle>
              <v-card-subtitle style="margin-top: -25px">
                <ul>
                  <li style="margin-left: 0;"><b>1. </b>Select a path connecting
                    {{ nodeList[sourceTypeId].text + ' and ' + nodeList[targetTypeId].text }}:
                  </li>
                  <li style="margin-top: 8px">
                    <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                    Either directly
                  </li>
                  <li>
                    <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                    Or inirectly through an intermediate node
                  </li>
                  <li style="margin-left: 0;"><b>2. </b>Configure the result network:</li>
                  <li style="margin-top: 8px">
                    <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                    Decide to keep the
                    intermediate node (in case an indirect path is selected) or to create a new edge type given a user
                    defined name
                  </li>
                  <li>
                    <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                    Allow or filter the use of specific intermediate nodes
                  </li>
                  <li>
                    <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                    Additional path specific configuration may be available
                  </li>
                </ul>
              </v-card-subtitle>
              <v-divider></v-divider>
              <v-row style="min-height: 35vh; margin-bottom: 15px; margin-top: 15px">
                <v-col cols="3">
                  <v-card-title style="text-align: left"><i v-show="pathModel==null" style="color: red">*</i><b>Select a
                    path:</b></v-card-title>
                  <v-radio-group v-model="pathModel">
                    <v-list-item-subtitle class="title">Direct-Paths</v-list-item-subtitle>
                    <v-list>
                      <v-list-item v-for="(path,idx) in paths[0]" :key="'direct'+idx" v-if="paths[0].length>0">
                        <v-list-item-title>
                          <v-tooltip top>
                            <template v-slot:activator="{on,attrs}">
                              <v-icon v-on="on" v-bind="attrs"
                                      :color="getColoring('nodes',nodeList[sourceTypeId].value,'light')"
                                      size="30px">
                                fas fa-genderless
                              </v-icon>
                            </template>
                            <span>{{ nodeList[sourceTypeId].text }}</span>
                          </v-tooltip>
                          <span v-for="(edge,idx2) in path" :key="'0_'+idx+'_'+idx2+'_'+edge.label">
                            <v-tooltip top>
                               <template v-slot:activator="{ on, attrs }">
                                  <v-icon v-bind="attrs"
                                          size="30px"
                                          v-on="on">{{
                                      edge.direction ? "fas fa-long-arrow-alt-right" : "fas fa-long-arrow-alt-left"
                                    }}</v-icon>
                               </template>
                              <span>{{ edge.label }}</span>
                            </v-tooltip>
                        <v-tooltip top>
                        <template v-slot:activator="{on,attrs}">
                          <v-icon v-on="on" v-bind="attrs"
                                  size="30px"
                                  :color="getColoring('edges',edge.label,'light')[edge.direction ? 1:0]">fas fa-genderless</v-icon>
                        </template>
                        <span>{{ getNodeLabel(edge.label, [edge.direction ? 1 : 0]) }}</span>
                      </v-tooltip>
                      </span>
                        </v-list-item-title>
                        <v-list-item-action>
                          <v-radio :value="idx"></v-radio>
                        </v-list-item-action>
                      </v-list-item>
                      <v-list-item v-if="paths[0].length===0">
                        <v-list-item-subtitle><i>no direct path available</i></v-list-item-subtitle>
                      </v-list-item>
                    </v-list>
                    <v-list-item-subtitle class="title">Indirect-Paths</v-list-item-subtitle>
                    <v-list>
                      <v-list-item v-for="(path,idx) in paths[1]" :key="'indirect_'+idx">
                        <v-list-item-title>
                          <v-tooltip top>
                            <template v-slot:activator="{on,attrs}">
                              <v-icon v-on="on" v-bind="attrs" size="30px"
                                      :color="getColoring('nodes',nodeList[sourceTypeId].value,'light')">
                                fas fa-genderless
                              </v-icon>
                            </template>
                            <span>{{ nodeList[sourceTypeId].text }}</span>
                          </v-tooltip>
                          <span v-for="(edge,idx2) in path" :key="'1_'+idx+'_'+idx2+'_'+edge.label">
                            <v-tooltip top>
                               <template v-slot:activator="{ on, attrs }">
                                  <v-icon v-bind="attrs"
                                          size="30px"
                                          v-on="on">{{
                                      edge.direction ? "fas fa-long-arrow-alt-right" : "fas fa-long-arrow-alt-left"
                                    }}</v-icon>
                               </template>
                              <span>{{ edge.label }}</span>
                            </v-tooltip>
                        <v-tooltip top>
                        <template v-slot:activator="{on,attrs}">
                          <v-icon v-on="on" v-bind="attrs"
                                  size="30px"
                                  :color="getColoring('edges',edge.label,'light')[edge.direction ? 1:0]">fas fa-genderless</v-icon>
                        </template>
                        <span>{{ getNodeLabel(edge.label, [edge.direction ? 1 : 0]) }}</span>
                      </v-tooltip>
                      </span>
                        </v-list-item-title>
                        <v-list-item-action>
                          <v-radio :value="idx+paths[0].length">
                          </v-radio>
                        </v-list-item-action>
                      </v-list-item>
                    </v-list>
                  </v-radio-group>
                </v-col>
                <div style="margin-bottom: 20px">
                  <v-divider vertical></v-divider>
                </div>
                <v-col cols="9" v-if="pathModel!=null && pathModel>-1">
                  <v-card-title style="text-align: left"><b>Additional Options</b></v-card-title>
                  <v-card-title style="text-align: left; margin-left: 20px;  color: rgb(128,128,128)" class="title">
                    General
                  </v-card-title>
                  <v-list v-if="!direct">
                    <v-list-item>
                      <v-list-item-content>
                        <v-tooltip top>
                          <template v-slot:activator="{on,attrs}">
                            <LabeledSwitch label-off="Hide Connector Nodes" label-on="Keep Connector Nodes"
                                           v-model="options.general.keep" v-bind="attrs" v-on="on">
                              <template v-slot:tooltip>
                                <div>
                                  Defines if transitive edges are created from the successful paths <br>or if the graph
                                  remains unchanged.
                                </div>
                              </template>
                            </LabeledSwitch>
                          </template>
                          <span>Decide if you want to keep all edges or replace the created paths by generating one connecting your source and target nodes directly.</span>
                        </v-tooltip>
                      </v-list-item-content>
                    </v-list-item>
                    <v-list-item>
                      <v-list-item-action style="min-width: 400px" v-if="!options.general.keep">
                        <v-text-field v-model="options.general.name"
                                      label="Combined Edge Name"
                                      :rules="[value => !!value || 'Required!',value=>$global.metagraph.edges.map(e=>e.label).indexOf(value)===-1 || 'Existing names are not possible!']"></v-text-field>
                      </v-list-item-action>
                      <v-list-item-content v-else>
                        <LabeledSwitch label-off="Keep partial paths"
                                       label-on="Keep only complete paths" v-model="options.general.removePartial">
                          <template v-slot:tooltip>
                            <div> Defines if edges will be added that are only connecting from specified source<br> or
                              target
                              nodes to connector-type nodes but not connecting source and target sets.
                            </div>
                          </template>
                        </LabeledSwitch>
                      </v-list-item-content>
                    </v-list-item>

                    <v-list-item v-show="!direct">
                      <v-list-item-content>
                        <LabeledSwitch :disabled="connectorCount ===0" label-off="Exclude selected connectors"
                                       label-on="Use only selected" v-model="connectorModel">
                          <template v-slot:tooltip>
                            <div>Defines if the user-defined list of connectors is used to exclude them <br>from graph
                              selection or if exclusively the selection is used.
                            </div>
                          </template>
                        </LabeledSwitch>
                      </v-list-item-content>
                      <v-list-item-action>
                        <div>
                          <v-btn @click="$refs.connectors.show()" outlined style="margin-top:-30px;">
                            Specify connectors ({{ connectorCount }})
                            <v-icon right>fas fa-link</v-icon>
                          </v-btn>
                        </div>
                      </v-list-item-action>
                    </v-list-item>
                  </v-list>
                  <v-divider></v-divider>
                  <v-card-title style="text-align: left; margin-left: 20px;  color: rgb(128,128,128)" class="title">Node
                    Specific
                  </v-card-title>
                  <v-list>
                    <v-list-item v-if="isPathNode('gene')">
                      <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                        <LabeledSwitch label-on="Coding Genes Only" label-off="All Genes"
                                       v-model="options.nodes.codingGenesOnly">
                          <template v-slot:tooltip>
                            <div>If activated, applies filter on genes, such that only coding genes are used.</div>
                          </template>
                        </LabeledSwitch>
                      </v-list-item-title>
                    </v-list-item>
                    <v-list-item v-if="isPathNode('drug')">
                      <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                        <LabeledSwitch label-on="Approved Drugs Only" label-off="All Drugs"
                                       v-model="options.nodes.approvedDrugsOnly">
                          <template v-slot:tooltip>
                            <div>If activated, applies filter on drugs, such that only approved drugs are used.</div>
                          </template>
                        </LabeledSwitch>
                      </v-list-item-title>
                    </v-list-item>
                    <v-list-item v-if="isPathNode('drug')">
                      <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                        <LabeledSwitch label-on="Filter Element Drugs" label-off="No Filter"
                                       v-model="options.nodes.filterElementDrugs">
                          <template v-slot:tooltip>
                            <div>If activated, applies filter on drugs, such that only complex drugs are used.
                              <br>Element drugs are:
                              <br><b>chemical element:</b> <i>Gold, Zinc, ...</i>
                              <br><b>metals and metal cations:</b> <i>Cupric Chloride, Aluminium acetoactetate, ...</i>
                              <br><b>minerals and mineral supplements:</b> <i>Calcium silicate, Sodium chloride, ...</i>
                            </div>
                          </template>
                        </LabeledSwitch>
                      </v-list-item-title>
                    </v-list-item>
                  </v-list>
                  <v-divider></v-divider>
                  <v-card-title style="text-align: left; margin-left: 20px;  color: rgb(128,128,128)" class="title">Edge
                    Specific
                  </v-card-title>
                  <v-list>
                    <v-list-item v-if="isPathEdge('GeneGeneInteraction') || isPathEdge('ProteinProteinInteraction')">
                      <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                        <LabeledSwitch label-on="Use experimentally validated"
                                       label-off="Use all interactions"
                                       v-model="options.edges.experimentalInteraction">
                          <template v-slot:tooltip>
                            <div>Defines if only experimentally validated interaction edges are used.
                            </div>
                          </template>
                        </LabeledSwitch>
                      </v-list-item-title>
                    </v-list-item>
                    <v-list-item v-if="isPathEdge('DisorderHierarchy')">
                      <v-list-item-title style="padding-top: 5px; padding-bottom: 3px">
                        <LabeledSwitch label-on="Find parent disorder"
                                       label-off="Find subtypes"
                                       v-model="options.edges.disorderParents">
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
                                       v-model="options.edges.drugTargetsWithAction">
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
                          v-model="options.edges.disorderAssociationCutoff"
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
                </v-col>
              </v-row>
            </v-card>
          </div>
          <v-card-actions style="display: flex; justify-content: flex-end; margin-left: 16px">
            <ButtonBack @click="makeStep"></ButtonBack>
            <ButtonNext @click="makeStep"
                        :disabled="(selectedPath === undefined || selectedPath.length === 0) || (!options.general.keep&&!direct && (options.general.name === undefined || options.general.name.length===0))"></ButtonNext>
            <ButtonCancel @click="makeStep"></ButtonCancel>
          </v-card-actions>
        </v-stepper-content>
        <v-stepper-content step="3">
          <v-card
            v-if="step===3"
            class="mb-4"
          >
            >
            <v-card-subtitle class="headline" style="color: black; text-align: left; margin-left: 5vw">3. Connectivity
              Results
            </v-card-subtitle>

            <v-card-subtitle style="margin-top: -25px">
              <ul>
                <li style="margin-left: 0;">Explore the extracted paths linking your queried entries:</li>
                <li style="margin-top: 8px">
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Explore the network
                </li>
                <li>
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Click on nodes in a list to highlight it in the network
                </li>
                <li>
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Double click a nodes in a list to get details
                </li>
                <li>
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Changed the contents of the tables to check the connector nodes
                </li>
                <li>
                  <v-icon style="font-size: 8px" left>fas fa-circle</v-icon>
                  Use the 'tripartite' layout to organize sources to the left and targets to the right
                </li>
              </ul>
            </v-card-subtitle>

            <v-row>
              <v-col style="padding: 0; max-width: 360px; min-width:350px">

                <v-card-title class="subtitle-1" style="display: flex"><span
                  style="justify-content: flex-start">{{ resultTableModel !== 2 ? 'Sources' : 'Connectors' }}
                  ({{ resultTableModel !== 2 ? sources.length : connectors.length }})</span>
                  <v-tooltip top>
                    <template v-slot:activator="{attrs, on}">
                      <v-icon right size="12pt" v-on="on" v-bind="attrs">far fa-question-circle</v-icon>
                    </template>
                    <div>This is the number of initially selected source entries.<br>
                      If this number is different to the number in the graph, some filter you applied removed some
                      entries!
                    </div>
                  </v-tooltip>
                  <ResultDownload v-show="getNodesForSourceTable() !=null && getNodesForSourceTable().length>0"
                                  seeds style="margin: auto; justify-self: flex-end; display: inline-block"
                                  :label="options.general.keep && resultTableModel===2 ? 'Connectors' :'Sources'"
                                  @downloadEvent="downloadSourceList"></ResultDownload>
                </v-card-title>
                <v-data-table max-height="550px" height="550px" class="overflow-y-auto" fixed-header dense item-key="id"
                              :items="resultTableModel !==2 ? sources :connectors"
                              :headers="getHeaders()"
                              @dblclick:row="sourceDoubleClicked"
                              disable-pagination
                              hide-default-footer @click:row="seedClicked">
                  <template v-slot:item.displayName="{item}">
                    <v-tooltip v-if="item.displayName.length>32" right>
                      <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substr(0, 29) }}...</span>
                      </template>
                      <span>{{ item.displayName }}</span>
                    </v-tooltip>
                    <span v-else>{{ item.displayName }}</span>
                  </template>
                  <template v-slot:item.data-table-expand="{expand, item,isExpanded}">
                    <v-icon v-show="!isExpanded"
                            color="primary" @click="sourceDoubleClicked(null,{item:item},resultTableModel===2)">
                      fas fa-info-circle
                    </v-icon>
                  </template>
                </v-data-table>
              </v-col>
              <v-col>
                <v-list-item>
                  <v-list-item-title>
                    <div style="display: inline-block; margin:4px">
                      <v-icon style="display: block" size="30px"
                              :color="getColoring('nodes',nodeList[sourceTypeId].value,'light')">
                        fas fa-genderless
                      </v-icon>
                      <v-radio-group v-model="radio0" v-if="options.general.keep && !direct">
                        <v-radio style="margin-left: 8px" value="0" @change="setLatestRadio(0)"></v-radio>
                      </v-radio-group>
                      <span style="font-size: .7rem;">{{ nodeList[sourceTypeId].text }}</span>
                    </div>
                    <span v-for="(edge,idx2) in selectedPath" :key="'1_'+idx2+'_'+edge.label">
                      <div style="display: inline-block; margin: 4px">
                                    <v-icon style="display: block"
                                            size="30px"
                                    >{{
                                        edge.direction ? "fas fa-long-arrow-alt-right" : "fas fa-long-arrow-alt-left"
                                      }}</v-icon>

                                <span style="font-size: .7rem;">{{ edge.label }}</span>
                        </div>
                      <div style="display: inline-block; margin: 4px">
                            <v-icon style="display: block"
                                    size="30px"
                                    :color="idx2 === 1 || options.general.keep ? getColoring('edges',edge.label,'light')[edge.direction ? 1:0] : 'gray'">fas fa-genderless</v-icon>
                        <v-radio-group v-if="options.general.keep && !direct && idx2===0" v-model="radio1">
                        <v-radio :style="{'margin-left': '10px'}" :value="(1+idx2)+''"
                                 @change="setLatestRadio(1+idx2)"></v-radio>
                        </v-radio-group>
                        <v-radio-group v-if="options.general.keep && !direct && idx2===1" v-model="radio2">
                        <v-radio :style="{'margin-left': '10px'}" :value="(1+idx2)+''"
                                 @change="setLatestRadio(1+idx2)"></v-radio>
                        </v-radio-group>
                          <span style="font-size: .7rem;">{{
                              getNodeLabel(edge.label, [edge.direction ? 1 : 0])
                            }}</span>
                        </div>
                      </span>
                  </v-list-item-title>
                </v-list-item>
                <!--                <div style="width: 100%; display: flex; padding-left: 50px; padding-right: 50px; margin-bottom: 16px"-->
                <!--                     v-if="options.general.keep && selectedPath!=null && selectedPath.length>1">-->
                <!--                  <v-chip :color="resultTableModel===0? 'green':'primary'"-->
                <!--                          style="justify-self: left; margin-right: auto; color: white"-->
                <!--                          @click="resultTableModel=0">{{ nodeList[sourceTypeId].text }}-->
                <!--                    <v-icon size="18" style="margin-left: 5px; margin-right: 5px">fas fa-angle-right</v-icon>-->
                <!--                    {{ getNodeLabel(selectedPath[0].label, [selectedPath[0].direction ? 1 : 0]) }}-->
                <!--                  </v-chip>-->
                <!--                  <v-chip :color="resultTableModel===1? 'green':'primary'"-->
                <!--                          style="justify-self: center; margin-left: auto; margin-right: auto; color: white"-->
                <!--                          @click="resultTableModel=1">{{ nodeList[sourceTypeId].text }}-->
                <!--                    <v-icon size="18" style="margin-left: 5px; margin-right: 5px">fas fa-angle-double-right</v-icon>-->
                <!--                    {{ nodeList[targetTypeId].text }}-->
                <!--                  </v-chip>-->
                <!--                  <v-chip :color="resultTableModel===2? 'green':'primary'"-->
                <!--                          style="justify-self: right; margin-left: auto;color: white"-->
                <!--                          @click="resultTableModel=2">-->
                <!--                    {{ getNodeLabel(selectedPath[0].label, [selectedPath[0].direction ? 1 : 0]) }}-->
                <!--                    <v-icon size="18" style="margin-left: 5px; margin-right: 5px">fas fa-angle-right</v-icon>-->
                <!--                    {{ nodeList[targetTypeId].text }}-->
                <!--                  </v-chip>-->
                <!--                </div>-->
                <Network ref="graph" :configuration="graphConfig" :window-style="graphWindowStyle"
                         :show-vis-option="showVisOption"
                         :legend="$refs.graph!==undefined" :tools="$refs.graph!==undefined" secondaryViewer="true"
                         @loadIntoAdvancedEvent="$emit('graphLoadEvent',{post: {id: gid}})"
                         @toggleNodeSelectEvent="nodeDoubleclicked">
                  <template v-slot:legend>
                    <Legend :countMap="legend.countMap" :entityGraph="legend.entityGraph" :options="legend.options"
                            @graphViewEvent="toggleGraphElement"
                            @downloadEntries="downloadfromLegend">
                    </Legend>
                  </template>
                  <template v-slot:tools v-if="$refs.graph!==undefined">
                    <Tools physics :cc="false" loops :physicsDisabled="physicsDisabled"
                           @toggleOptionEvent="toggleToolOption" @clickOptionEvent="clickToolOption">
                      <template v-slot:append v-if="options.general.keep">
                        <ToolDropdown
                          :items="[{value:'default', text:'Default'},{value:'tripartite',text:'Tripartite'}, {value:'portrait', text:'Portrait'}, {value:'topographic_x', text:'Topographic (X,Z)'}, {value:'topographic_y', text:'Topographic (Y,Z)'}, {value: 'geodesic', text: 'Geodesic (X,Y)'},{value: 'geodesic_x', text: 'Geodesic (X,Z)'},{value: 'geodesic_y', text: 'Geodesic (Y,Z)'}]"
                          label="Layout" icon="fas fa-project-diagram" @change="$refs.graph.loadLayout">
                          <template v-slot:tooltip>
                            <div style="display: inline-block"><i><b>Change the layout of the network!</b></i> <br>Options:<br>
                              <b>Default:</b> force-directed layout<br>
                              <b>Tripartite:</b> Tripartite layout to separate source, target and connector nodes<br>
                              <b>Portrait:</b> cartoGRAPHs.Portrait layout<br>
                              <b>Topographic (X,Z):</b> cartoGRAPHs Topographic 3D layout reduced to X and Z coordinates <br>
                              <b>Topographic (Y,Z):</b> cartoGRAPHs Topographic 3D layout reduced to Y and Z coordinates <br>
                              <b>Geodesic (X,Y):</b> cartoGRAPHs Geodesic 3D layout reduced to X and Y coordinates <br>
                              <b>Geodesic (X,Z):</b> cartoGRAPHs Geodesic 3D layout reduced to X and Z coordinates <br>
                              <b>Geodesic (Y,Z):</b> cartoGRAPHs Geodesic 3D layout reduced to Y and Z coordinates <br>
                            </div>
                          </template>
                        </ToolDropdown>
                      </template>
                    </Tools>
                  </template>

                </Network>
              </v-col>
              <v-col style="padding: 0 10px 0 0; max-width: 360px; min-width: 350px">
                <v-card-title class="subtitle-1" style="display: flex">
                  <span style="justify-content: flex-start">{{ resultTableModel !== 0 ? 'Targets' : 'Connectors' }} {{
                      (gid != null && targets.length != null ? (" (" + (resultTableModel !== 0 ? targets.length : connectors.length) + ")") : ": Processing")
                    }}</span>
                  <v-progress-circular indeterminate v-if="gid==null || targets.length==null" style="margin-left:15px">
                  </v-progress-circular>
                  <template v-else>
                    <v-tooltip top>
                      <template v-slot:activator="{attrs, on}">
                        <v-icon right size="12pt" v-on="on" v-bind="attrs">far fa-question-circle</v-icon>
                      </template>
                      <div>This is the target entries.<br>
                        If there was an initial selection of those and this number is different to the number in the
                        graph, some filter you applied removed some entries!
                      </div>
                    </v-tooltip>
                    <ResultDownload style="margin: auto; justify-self: flex-end; display: inline-block"
                                    v-show="getNodesForTargetTable() !=null && getNodesForTargetTable().length>0"
                                    seeds
                                    :label="options.general.keep && resultTableModel ===0 ? 'Connectors' :'Targets'"
                                    @downloadEvent="downloadTargetList"></ResultDownload>
                  </template>
                </v-card-title>

                <template v-if="gid!=null && targets.length>0">
                  <v-data-table max-height="550px" height="550px" class="overflow-y-auto" fixed-header dense
                                item-key="id"
                                @dblclick:row="targetDoubleClicked"
                                :items="resultTableModel !==0 ? targets :connectors"
                                :headers="getHeaders()"
                                disable-pagination
                                hide-default-footer @click:row="targetClicked">
                    <template v-slot:item.displayName="{item}">
                      <v-tooltip v-if="item.displayName.length>32" right>
                        <template v-slot:activator="{attr,on }">
                          <span v-bind="attr" v-on="on"
                                style="color: dimgray">{{ item.displayName.substr(0, 29) }}...</span>
                        </template>
                        <span>{{ item.displayName }}</span>
                      </v-tooltip>
                      <span v-else>{{ item.displayName }}</span>
                    </template>
                    <template v-slot:item.data-table-expand="{expand, item,isExpanded}">
                      <v-icon v-show="!isExpanded"
                              color="primary" @click="targetDoubleClicked(null,{item:item}, resultTableModel ===0)">
                        fas fa-info-circle
                      </v-icon>
                    </template>
                  </v-data-table>
                </template>
              </v-col>
            </v-row>
          </v-card>
          <div style="padding-top: 10px">
            <v-card-actions style="margin-right: 16px; display: flex; justify-content: flex-end">
              <ButtonBack @click="makeStep"></ButtonBack>
              <ButtonCancel @click="makeStep"></ButtonCancel>
              <ButtonAdvanced @click="$emit('graphLoadNewTabEvent',{post: {id: gid}})"
                              :disabled="gid==null"></ButtonAdvanced>
            </v-card-actions>
          </div>
        </v-stepper-content>
      </v-stepper-items>
    </v-stepper>
    <DetailDialog ref="details" max-width="25vw"
                  :attributes="detailAttributes"
                  :detail-request="detailRequest"
                  :additions="detailAdditions"
    ></DetailDialog>
  </v-card>
</template>

<script>
import Network from "../../graph/Network";
import SuggestionAutocomplete from "@/components/app/suggestions/SuggestionAutocomplete";
import SeedTable from "@/components/app/tables/SeedTable";
import ResultDownload from "@/components/app/tables/menus/ResultDownload";
import NodeInput from "@/components/app/input/NodeInput";
import Legend from "@/components/views/graph/Legend";
import EntryDetails from "@/components/app/EntryDetails";
import ButtonNext from "@/components/start/quick/ButtonNext";
import ButtonBack from "@/components/start/quick/ButtonBack";
import ButtonCancel from "@/components/start/quick/ButtonCancel";
import GuidedExamples from "@/components/start/guided/GuidedExamples";
import LabeledSwitch from "@/components/app/input/LabeledSwitch";
import ButtonAdvanced from "@/components/start/quick/ButtonAdvanced";
import Tools from "@/components/views/graph/Tools";
import ConnectorDialog from "@/components/views/start/guided/ConnectorDialog";
import ToolDropdown from "@/components/views/graph/tools/ToolDropdown";
import DetailDialog from "@/components/start/quick/DetailDialog";


export default {
  name: "GuidedStepper",

  uid: undefined,

  data() {
    return {
      targetCount: 0,
      sourceCount: 0,
      verify: false,

      sugQuery: [undefined, undefined],
      graphWindowStyle: {
        height: '550px',
        'min-height': '550px',
      },
      graphConfig: {visualized: false},

      sources: [],
      connectors: [],
      targets: [],
      nodeOrigins: [{}, {}],
      fileInputModel: [undefined, undefined],

      sourceTypeId: undefined,
      targetTypeId: undefined,
      step: 1,
      nodeList: [],
      nodeIdTypeList: [],
      selectedPath: [],
      physicsOn: false,

      suggestionType: [undefined, undefined],
      pathModel: undefined,

      gid: undefined,

      graph: {
        physics: false,
      },

      info: undefined,
      direct: false,
      physicsDisabled: false,
      paths: {0: [], 1: []},
      showVisOption: false,
      radio0: "0",
      radio1: null,
      radio2: "2",
      latestRadio: 2,
      options: {
        general: {
          keep: false,
          name: undefined,
          removePartial: true,
        },
        nodes: {
          codingGenesOnly: false,
          approvedDrugsOnly: false,
          filterElementDrugs: true
        },
        edges: {
          drugTargetsWithAction: false,
          disorderAssociationCutoff: 0,
          disorderParents: false,
          experimentalInteraction: false,
        }
      },
      legend: {
        countMap: {},
        entityGraph: {},
        options: {}
      },
      example: undefined,
      connectorTypeId: undefined,
      connectorCount: 0,
      connectorModel: false,
      resultTableModel: 1,
      detailAttributes: [],
      detailRequest: undefined,
      detailAdditions: [],
      allDetailAttributes: {
        'drug': ["Name", "SourceIDs", "Formula", "Indication", "Description", "Synonyms"],
        'gene': ["Name", "SourceIDs", "Symbols", "Chromosome", "Genomic Location", "Synonyms", "Description"],
        'protein': ["Name", "SourceIDs", "Gene", "Synonyms", "Comments"],
        'disorder': ["Name", "SourceIDs", "ICD-10", "Synonyms", "Description"],
        'pathway': ["Name", "SourceIDs"]
      },
    }
  },

  created() {
    this.uid = this.$cookies.get("uid")
    this.$global.metagraph.nodes.forEach((n, index) => {
      this.nodeList.push({id: index, value: n.group, text: n.label})
      this.nodeIdTypeList.push(this.$global.metagraph.data[n.label])
    })
    this.init()
  },

  watch: {

    pathModel: function (val) {
      if (this.pathModel == null)
        return
      this.$set(this.options.general, "keep", false)
      if (val < this.paths[0].length) {
        this.$set(this.options.general, "keep", true)
        this.selectedPath = this.paths[0][val]
        this.direct = true
        this.connectorTypeId = undefined
      } else {
        this.selectedPath = this.paths[1][val - this.paths[0].length]
        this.direct = false
        this.connectorTypeId = Object.values(this.nodeList).filter(n => n.value === this.selectedPath[0].connector)[0].id
      }
      if (this.example != null)
        if (this.example.compress) {
          this.options.general.name = this.example.edge
        } else {
          this.$set(this.options.general, "keep", true)
        }
      this.$refs.connectors.clear()
    }


  },

  methods: {


    init: function () {
      this.step = 1
      this.sugQuery = [undefined, undefined]

      this.sourceTypeId = undefined
      this.targetTypeId = undefined
      this.sources = []
      this.connectors = []
      this.targets = []
      this.nodeOrigins = [{}, {}]
      this.suggestionType = [undefined, undefined]

      this.selectedPath = []
      this.targetCount = 0;
      this.sourcecount = 0;
      this.connectorModel = false
      this.pathModel = undefined
      this.showVisOption = false
      this.resultTableModel = 1
      this.clearPaths()

      this.example = undefined

      this.options = {
        general: {
          keep: false,
          name: undefined,
          removePartial: true,
        },
        nodes: {
          codingGenesOnly: false,
          approvedDrugsOnly: false,
          filterElementDrugs: true
        },
        edges: {
          drugTargetsWithAction: false,
          disorderAssociationCutoff: 0,
          disorderParents: false,
          experimentalInteraction: false,
        }
      }

      this.info = undefined
      if (this.$refs.sourceTable != null)
        this.$refs.sourceTable.clear()
      if (this.$refs.targetTable != null)
        this.$refs.targetTable.clear()
      this.legend = {
        countMap: {},
        entityGraph: {},
        options: {}
      }

    },
    getTargetCount() {
      return this.$refs.targetTable ? this.$refs.targetTable.getSeeds().length : 0
    },

    reset: function () {
      this.init()
    },

    clearPaths: function () {
      this.pathModel = undefined
      this.paths[0] = []
      this.paths[1] = []
    },

    getSuggestionSelection: function (index) {
      let type = this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].value
      let selfAdded = false;
      let nodeId = this.$global.metagraph.nodes.filter(n => n.group === type)[0].id
      let typeList = this.$global.metagraph.edges.filter(e => e.from !== e.to && e.from === nodeId || e.to === nodeId).map(e => e.to === nodeId ? e.from : e.to).map(nid => {
        let node = this.$global.metagraph.nodes.filter(n => n.id === nid)[0]
        if (node.group === type)
          selfAdded = true
        return {value: node.group, text: node.label}
      })
      if (!selfAdded)
        typeList.push({value: type, text: this.nodeList[[this.sourceTypeId, this.targetTypeId][index]].text})
      return typeList
    },

    setLatestRadio: function (index) {
      if (this.latestRadio === index)
        return
      else {
        if (this.latestRadio !== 0)
          this.radio0 = null
        if (this.latestRadio !== 1)
          this.radio1 = null
        if (this.latestRadio !== 2)
          this.radio2 = null
        let radios = [this.radio0, this.radio1, this.radio2]
        radios[index] = "" + index
        this.latestRadio = index
      }
      this.resultTableModel = index + this.latestRadio

    },

    generatePaths: function () {
      let sourceId = this.$global.metagraph.nodes[this.sourceTypeId].id + ""
      let targetId = this.$global.metagraph.nodes[this.targetTypeId].id + ""
      this.$global.metagraph.edges.forEach(e1 => {
        if (e1.to === sourceId || e1.from === sourceId) {
          let i1 = (e1.to === sourceId) ? e1.from : e1.to
          if (i1 === targetId)
            this.paths[0].push([{label: e1.label, direction: e1.from === sourceId}])
          else
            this.$global.metagraph.edges.forEach(e2 => {
              if (e2.to === i1 || e2.from === i1) {
                let i2 = (e2.to === i1) ? e2.from : e2.to
                if (i2 === targetId) {
                  let via = this.$global.metagraph.nodes.filter(n => n.id === (e2.to === targetId ? e2.from : e2.to))[0].group
                  this.paths[1].push([{label: e1.label, connector: via, direction: e1.from === sourceId}, {
                    label: e2.label,
                    connector: via,
                    direction: e2.to === targetId
                  }])
                }
              }
            })
        }
      })
      if (this.example != null) {
        if (this.example.connector != null) {
          let nr = this.paths[0].length
          for (let i = 0; i < this.paths[1].length; i++) {
            if (this.paths[1][i][0].connector === this.example.connector) {
              nr += i
              break;
            }
          }
          this.pathModel = nr;
        } else {
          this.printNotification("not implemented yet", 2)
        }
      }

    },

    copySourcesToTargets: function () {
      this.targetTypeId = this.sourceTypeId;
      this.$nextTick(() => {
        this.$refs.targetTable.setValues(this.$refs.sourceTable.allOrigins(), this.$refs.sourceTable.getSeeds(), this.$refs.sourceTable.getAttributes())
      })
      this.updateTargetCount();
    },

    updateTargetCount: function () {
      this.targetCount = this.$refs.targetTable ? this.$refs.targetTable.getSeeds().length : 0;
    },

    updateSourceCount: function () {
      this.$set(this, "sourceCount", this.$refs.sourceTable ? this.$refs.sourceTable.getSeeds().length : 0);
    },

    nodeDoubleclicked: function (obj) {
      if (obj[0]) {
        let item = obj[0]
        this.rowDoubleClicked(item, item.group)
      }
    },

    sourceDoubleClicked: function (event, obj, connector) {
      if (connector) {
        this.connectorDoubleClicked(event, obj)
      } else {
        let item = obj.item
        this.detailAdditions = [{pos: 1, key: 'Degree', value: item.degree}]
        this.rowDoubleClicked(item, this.nodeList[this.sourceTypeId].value)
      }
    },

    targetDoubleClicked: function (event, obj, connector) {
      if (connector) {
        this.connectorDoubleClicked(event, obj)
      } else {
        let item = obj.item
        this.detailAdditions = [{pos: 1, key: 'Degree', value: item.degree}]
        this.rowDoubleClicked(item, this.nodeList[this.targetTypeId].value)
      }
    },

    connectorDoubleClicked: function (event, obj) {
      let item = obj.item
      this.detailAdditions = [{pos: 1, key: 'Degree', value: item.degree}]
      this.rowDoubleClicked(item, this.nodeList[this.connectorTypeId].value)
    },

    rowDoubleClicked: function (item, type) {
      console.log(item.id + " -> " + type)
      this.detailAttributes = this.allDetailAttributes[type];
      this.detailRequest = undefined
      this.detailRequest = {edge: false, type: type, id: item.id}
      this.$refs.details.showDialog(this.detailRequest)
    },

    focusNode: function (id) {
      if (this.$refs.graph === undefined)
        return
      this.$refs.graph.setSelection([id])
      this.$refs.graph.zoomToNode(id)
    },

    getNodesForSourceTable: function () {
      return this.options.general.keep && this.resultTableModel === 2 ? this.connectors : this.sources;
    },

    getNodesForTargetTable: function () {
      return this.options.general.keep && this.resultTableModel === 0 ? this.connectors : this.targets;
    },

    submitGraphGeneration: function () {
      let payload = {
        uid: this.$cookies.get("uid"),
        sourceType: this.nodeList[this.sourceTypeId].value,
        targetType: this.nodeList[this.targetTypeId].value,
        sources: this.sources.map(n => n.id),
        targets: this.$refs.targetTable.getSeeds().map(n => n.id),
        path: this.selectedPath,
        params: this.options,
      }
      if (this.connectorCount > 0) {
        payload.excludeConnectors = !this.connectorModel
        payload.connectors = this.$refs.connectors.getList().map(c => c.id)
      }
      this.$http.post("/getGuidedGraph", payload).then(response => {
        if (response.data !== undefined) {
          return response.data
        }
      }).then(data => {
        this.$emit('newGraphEvent')
        this.info = data;
        let count = 0;
        Object.values(data.edges).forEach(v => count += v)
        Object.values(data.nodes).forEach(v => count += v)
        this.physicsDisabled = count > 20000;
        this.gid = data.id
        this.prepareLegend()
        this.showVisOption = !this.graphConfig.visualized
        this.$refs.graph.loadNetworkById(this.gid).then(() => {
          this.$refs.graph.showLoops(false)
        })
        this.loadTables(this.gid)
      }).catch(console.error)
    },

    prepareLegend: function () {
      this.$http.get("/getConnectionGraph?gid=" + this.gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.legend.entityGraph = data;
      }).then(() => {
        Object.keys(this.legend.entityGraph).forEach(entity => {
          this.legend.countMap[entity] = {}
          Object.keys(this.info[entity]).forEach(name => {
            this.legend.countMap[entity][name] = {name: name, selected: 0, total: this.info[entity][name]}
          })
        })
      }).catch(console.error)

    },

    addToSourceSelection: function (list, name) {
      this.addToSelection(list, 0, name)
    }

    ,

    addToTargetSelection: function (list, name) {
      this.addToSelection(list, 1, name)
    }
    ,

    addToSelection: function (list, index) {
      this.$refs[["sourceTable", "targetTable"][index]].addSeeds(list)
    }
    ,
    loadTables: function (gid) {
      let targetGroupName = this.nodeList[this.targetTypeId].value
      let sourceGroupName = this.nodeList[this.sourceTypeId].value
      return this.$http.get("/getGraphList?id=" + gid).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        this.targets = data.nodes[targetGroupName].map(n => {
          return {id: n.id, displayName: n.displayName, degree: n.degree}
        })
        if (this.$refs.targetTable.getSeeds().length > 0) {
          let ids = this.$refs.targetTable.getSeeds().map(s => s.id)
          if (this.targets)
            this.targets = this.targets.filter(n => ids.indexOf(n.id) > -1)
        }
        if (this.sourceTypeId === this.targetTypeId) {
          let ids = this.$refs.sourceTable.getSeeds().map(s => s.id)
          if (this.targets) {
            let target_ids = this.$refs.targetTable.getSeeds().map(s => s.id)
            if (target_ids.length > 0)
              this.targets = this.targets.filter(n => target_ids.indexOf(n.id) > -1)
            else
              this.targets = this.targets.filter(n => ids.indexOf(n.id) === -1)
          }
        }
        let sourceDegrees = {}
        data.nodes[sourceGroupName].forEach(n => {
          sourceDegrees[n.id] = n.degree
        })
        this.sources.forEach(n => {
          n.degree = sourceDegrees[n.id]
        })
        if (this.options.general.keep) {
          let connectorGroupName = this.getConnectorType()
          this.connectors = data.nodes[connectorGroupName].map(n => {
            return {id: n.id, displayName: n.displayName, degree: n.degree}
          })
          this.connectors.sort((o1, o2) => o2.degree - o1.degree)
        }
        this.sources.sort((o1, o2) => o2.degree - o1.degree)
        this.targets.sort((o1, o2) => o2.degree - o1.degree)
      }).catch(console.error)
    },

    getConnectorType: function () {
      return this.$utils.getNodes(this.$global.metagraph, this.selectedPath[0].label)[this.selectedPath[0].direction ? 1 : 0]
    },

    getOrigins: function (id, index) {
      if (this.nodeOrigins[index][id] === undefined)
        return ["?"]
      else
        return this.nodeOrigins[index][id].map(item => {
          let sp1 = item.split(":")
          let out = []
          out.push(sp1[0])
          if (out[0] === 'SUG') {
            let sp2 = sp1[1].split("[")
            out.push(sp2[0])
            out.push(sp2[1].substring(0, sp2[1].length - 1))
          } else {
            out.push(sp1[1])
          }
          return out;
        })
    }
    ,
    printNotification: function (message, type) {
      this.$emit("printNotificationEvent", message, type)
    }
    ,
    removeNode: function (id, index) {
      let idx = this[["sources", "targets"][index]].map(e => e.id).indexOf(id)
      this[["sources", "targets"][index]].splice(idx, 1)
      delete this.nodeOrigins[index][id]
    }
    ,

    removeAll: function (index) {
      this[["sources", "targets"][index]] = []
      this.nodeOrigins[index] = {}
    }
    ,
    removeNonIntersecting: function (index) {
      let remove = []
      let seedOrigin = this.nodeOrigins[index]
      let seeds = this[["sources", "targets"][index]]
      Object.keys(seedOrigin).forEach(seed => {
        if (seedOrigin[seed] === undefined || seedOrigin[seed].length < 2) {
          seedOrigin[seed] = undefined
          remove.push(parseInt(seed))
        }
      })
      this[["sources", "targets"][index]] = seeds.filter(s => remove.indexOf(s.id) === -1)
    },

    getPathEdges: function () {
      if (this.selectedPath == null || this.selectedPath.length === 0)
        return []
      return this.selectedPath.map(p => p.label)
    },

    getPathNodes: function () {
      if (this.selectedPath == null || this.selectedPath.length === 0)
        return []
      return (this.selectedPath[0].connector ? this.selectedPath.map(p => p.connector) : []).concat([this.nodeList[this.sourceTypeId].value, this.nodeList[this.targetTypeId].value])
    },

    isPathEdge: function (edge) {
      return this.getPathEdges().indexOf(edge) > -1;
    },

    toggleToolOption: function (option, value) {
      if (option === "physics")
        this.$refs.graph.setPhysics(value);
      if (option === "loops")
        this.$refs.graph.showLoops(value)
      if (option === "unconnected")
        this.$refs.graph.showUnconnected(value)
      if (option === "isolation")
        this.$refs.graph.graphViewEvent(value)
      if (option === 'shadow')
        this.$refs.graph.setShadow(value)
    },

    clickToolOption: function (option) {
      if (option === "fit")
        this.$refs.graph.setSelection()
    },

    isPathNode: function (node) {
      return this.getPathNodes().indexOf(node) > -1
    },

    downloadSourceList: function (names, sep) {

      this.downloadList(this.options.general.keep && this.resultTableModel === 2 ? 1 : 0, names, sep)
    }
    ,
    downloadTargetList: function (names, sep) {
      this.downloadList(this.options.general.keep && this.resultTableModel === 0 ? 1 : 2, names, sep)
    }
    ,
    downloadList: function (index, names, sep) {
      let nodeType = [this.nodeList[this.sourceTypeId].value, this.getConnectorType(), this.nodeList[this.targetTypeId].value][index]
      let list = [this.sources, this.connectors, this.targets][index]
      let name = ["seeds", "connectors", "targets"][index]
      this.$http.post("mapToDomainIds", {
        type: nodeType,
        ids: list.map(n => n.id)
      }).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        let text = "#ID" + (names ? sep + "Name" : "") + "\n";
        let dlName = nodeType + "_" + name + "." + (!names ? "list" : (sep === '\t' ? "tsv" : "csv"))
        if (!names) {
          Object.values(data).forEach(id => text += id + "\n")
        } else {
          list.forEach(s => text += data[s.id] + sep + s.displayName + "\n")
        }
        this.download(dlName, text)
      }).catch(console.error)
    }
    ,
    download: function (name, content) {
      let dl = document.createElement('a')
      dl.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(content))
      dl.setAttribute('download', name)
      dl.style.direction = 'none'
      document.body.appendChild(dl)
      dl.click()
      document.body.removeChild(dl)
    }
    ,
    applyExample: function (example) {
      this.reset()
      this.example = example
      for (let i = 0; i < this.nodeList.length; i++) {
        if (this.nodeList[i].value === example.source)
          this.sourceTypeId = i
        if (this.nodeList[i].value === example.target)
          this.targetTypeId = i
      }
    },

    makeStep: async function (button) {
      if (button === "continue") {
        if (this.step === 1) {
          this.$emit("clearURLEvent")
          await this.$nextTick(() => {
            this.sources = this.$refs.sourceTable.getSeeds()
            this.targets = this.$refs.targetTable.getSeeds()
          })
        }
        this.step++
        if (this.step === 2)
          this.generatePaths()
      }
      if (button === "back") {
        this.step--
        if (this.step === 2) {
          this.gid = undefined
          if (this.$refs.graph !== undefined)
            this.$refs.graph.reload()
          this.info = undefined
          this.targets = {}
        }
        if (this.step === 1)
          this.clearPaths()
      }
      if (button === "cancel") {
        if (this.$refs.graph !== undefined)
          this.$refs.graph.reload()
        this.init()
        this.$emit("resetEvent")
      }
      if (this.step === 3)
        this.submitGraphGeneration()
    }
    ,
    getHeaders: function () {
      return [{text: "Deg.", align: "end", sortable: true, value: "degree", width: "75px"}, {
        text: "Name",
        align: "start",
        sortable: true,
        value: "displayName"
      }, {
        text: "",
        value: "data-table-expand"
      }]
    }
    ,
    seedClicked: function (item) {
      let nodeType = this.options.general.keep && this.resultTableModel === 2 ? this.getConnectorType() : this.nodeList[this.sourceTypeId].value
      this.focusNode(nodeType.substring(0, 3) + '_' + item.id)
    }
    ,
    targetClicked: function (item) {
      let nodeType = this.options.general.keep && this.resultTableModel === 0 ? this.getConnectorType() : this.nodeList[this.targetTypeId].value
      this.focusNode(nodeType.substring(0, 3) + '_' + item.id)
    }
    ,

    getColoring: function (entity, name, style) {
      return this.$utils.getColoring(this.$global.metagraph, entity, name, style);
    }
    ,

    getNodeLabel: function (name, idx) {
      let id = this.$utils.getNodes(this.$global.metagraph, name)[idx]
      return id.substring(0, 1).toUpperCase() + id.substring(1)
    }
    ,

    toggleGraphElement: function (event) {
      this.$refs.graph.graphViewEvent(event)
    }
    ,
    downloadfromLegend: async function (entity, name) {
      let table = await this.$http.getTableDownload(this.gid, entity, name, ["primaryDomainId", "displayName"])
      this.download(this.gid + "_" + name + "-" + entity + ".tsv", table)
    },

    getExtendedNodes: function (name, not) {
      let nodes = this.$utils.getNodesExtended(this.configuration.entityGraph, name)
      if (not === undefined)
        return nodes;
      return nodes[0] === not ? nodes[1] : nodes[0]
    }
    ,
  }
  ,
  components: {
    DetailDialog,
    ToolDropdown,
    ConnectorDialog,
    ButtonAdvanced,
    LabeledSwitch,
    GuidedExamples,
    ButtonCancel,
    ButtonNext,
    ButtonBack,
    SuggestionAutocomplete,
    NodeInput,
    Network,
    SeedTable,
    EntryDetails,
    ResultDownload,
    Legend,
    Tools
  }

}
</script>

<style scoped>

</style>
