<template>
  <div v-show="model" style="z-index: 1001;position: fixed; top: 0; left: 0; width: 100%; height: 100%; display: flex; justify-content: center; background-color: rgba(0,0,0,0.45)">
    <v-card style="width:500px; align-self: center; margin-top: auto; margin-bottom: auto;z-index: 1002;">
      <v-card-title>
        Seed interaction network
      </v-card-title>
      <v-progress-circular v-if="loading" indeterminate></v-progress-circular>
      <VisNetwork ref="tree" v-if="!loading" :nodes="nodes" :edges="edges"
                  style="position: sticky;"></VisNetwork>
      <v-divider></v-divider>

      <v-card-actions>
        <v-btn
          text
          @click="model=false"
        >
          Close
        </v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script>
export default {
  name: "InteractionNetworkDialog",
  data(){return{
    model:false,
    loading: true,
  }},

  methods:{

    show: function(type, ids){
      this.model=true;
      this.$http.getInteractionEdges({type:type, ids:ids}).then(data=>{
        console.log(data)
      })
    }

  }

}
</script>

<style scoped>

</style>
