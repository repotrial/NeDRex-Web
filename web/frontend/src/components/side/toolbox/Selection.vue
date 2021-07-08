<template>
  <v-card ref="legend" elevation="3" style="margin:15px">
    <v-list-item @click="show=!show">
      <v-list-item-title>
        <v-icon left>{{ show ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
        Selection
      </v-list-item-title>
    </v-list-item>
    <v-divider></v-divider>
    <v-container v-show="show">
      <v-row>
        <v-col cols="6">
          <v-switch label="Selection Mode" v-model="options.selectMode"
                    @click="$emit('selectModeEvent',options.selectMode)"></v-switch>
        </v-col>
        <v-col cols="6" style="margin-top:15px">
          <v-chip outlined color="green" @click="applySelection">
            Apply selection
            <v-icon right>fas fa-chevron-right</v-icon>
          </v-chip>
        </v-col>
      </v-row>
      <v-divider></v-divider>
      <v-container>
        <v-card-title>Current Manual Selection ({{ selection.length }})</v-card-title>
        <v-card-text>Current selection in Network which can be used to apply this selection on the lists!
        </v-card-text>
        <v-simple-table fixed-header height="300px" dense v-if="selection.length>0">
          <template v-slot:default>
            <thead>
            <tr>
              <th class="text-center">ID</th>
              <th class="text-center">Name</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in selection" :key="item.id" v-on:click="setSelectedNode(item.id)">
              <td>{{ item.id }}</td>
              <td>{{ item.label }}
              </td>
            </tr>
            </tbody>
          </template>
        </v-simple-table>
        <i v-else>no selection available</i>
      </v-container>
    </v-container>
  </v-card>
</template>

<script>
export default {
  name: "Selection",
  props: {
    options: Object,
  },

  data() {
    return {
      show: true,
      selection: [],
    }
  },

  methods: {

    setSelectedNode: function (nodeId) {
      //TODO focus node
    },


    setSelection: function (items) {
      this.selection = items
    },

    applySelection: function () {
      this.$emit("applyMultiSelect", this.selection.map(n => n.id))
      this.selection = []
    },

  }
}
</script>

<style scoped>

</style>
