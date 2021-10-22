<template>
  <v-dialog v-model="show"
            persistent
            max-width="1000px"
  >
    <v-card>
      <v-card-title>Set graph name</v-card-title>
      <v-progress-circular v-if="loading" indeterminate></v-progress-circular>
      <tree ref="tree" style="height: 600px; width: 1000px;" :radius="8" :zoomable="true"
            :leafTextMargin="10" :duration="0" :identifier="(item)=>item.id" :nodeTextMargin="10" v-else :data="data"
            layoutType="horizontal" linkLayout="orthogonal"  @clickedNode="nodeClick">
        <template #node="{data, node: {depth}, radius, isSelected,actions}">
          <circle v-if="selected[data.id].selected" r="6" stroke="blue">
            <title>{{data.label}}</title>
          </circle>
          <circle v-else r="6" stroke="red">
            <title>{{data.label}}</title>
          </circle>
        </template>
      </tree>

      <v-divider></v-divider>

      <v-card-actions>
        <v-btn
          text
          @click="resolvePopup(false)"
        >
          Cancel
        </v-btn>
        <v-btn
          color="green darken-1"
          text
          @click="resolvePopup(true)"
        >
          Add Selected
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import {tree} from 'vued3tree'

export default {

  name: "DisorderHierarchyDialog",
  components: {
    tree,
  },
  props: {
    value: Boolean,
  },
  data() {
    return {
      selected: {},
      show: false,
      data: {},
      loading: false,
    }
  },

  methods: {
    resolvePopup: function (state) {
      this.show = false
      this.model = state;
      this.$emit('input', this.model)
      if (state) {
        this.$emit("addDisorders",Object.values(this.selected).filter(e=>e.selected).map(e=>e.id))
      }
    },
    loadDisorder: function (sid) {
      this.loading = true
      this.$http.getDisorderHierarchy(sid).then(result => {
        this.data = result
        this.prepareData(this.data)
        this.loading = false;
      })
      this.show = true;
    },
    prepareData: function (data) {
      data.selected = true
      this.selected[data.id] = data
      data.label = data.name
      data.name = data.name.substring(0, Math.min(20, data.name.length))
      if (data.children)
        data.children.forEach(this.prepareData)
    },

    toggleSelect: function (node,state) {
      node.selected = state
      if(node.children){
        node.children.forEach(c=>this.toggleSelect(c,state))
      }
      this.$refs.tree.resetZoom()
    },

    nodeClick: function (element) {
      this.toggleSelect(element.data, !element.data.selected)
    }
  }
}
</script>

<style lang="scss">
#preloader {
  display: none;
}


</style>
