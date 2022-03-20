<template>
  <v-dialog width="500px" v-model="model" style="z-index: 1001; max-height: 80vh">
    <v-card>
      <v-btn @click="model=false" color="error" icon style="position: absolute; right: 0; left: auto"><v-icon>far fa-times-circle</v-icon></v-btn>
      <div style="width: 100%; display: flex; justify-content: center; padding: 8px;">
        <v-data-table style="max-height: 80vh; overflow-y: auto" :items="drugs" :headers="[{text:'Name', align:'start', sortable:true, value:'displayName'},{text:'SourceID', value:'primaryDomainId', sortable: false, align: 'start'}]">
          <template v-slot:item.primaryDomainId="{item}">
            <v-btn plain @click="redirect(item.primaryDomainId)">{{item.primaryDomainId}}<v-icon right>fas fa-external-link-alt</v-icon></v-btn>
          </template>
        </v-data-table>
      </div>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "DrugsDialog",
  props:{
    drugs:Array,
  },
  data(){
    return{
      model: false
    }
  },
  methods:{
    show: function(){
      this.model=true;
    },
    redirect: function(domainID){
      let url = "https://go.drugbank.com/drugs/"+domainID.split('.')[1]
      window.open(url, '_blank')
    }
  }
}
</script>

<style scoped>

</style>
