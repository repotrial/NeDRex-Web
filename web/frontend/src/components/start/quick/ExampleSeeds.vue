<template>
  <v-row v-show="this.seedTypeId!=null" style="margin-top:0">
    <v-col>
      <v-menu bottom offset-y>
        <template v-slot:activator="{on,attrs}">
          <v-btn small outlined right v-bind="attrs" v-on="on">
            <v-icon left color="primary">
              fas fa-graduation-cap
            </v-icon>
            Examples
          </v-btn>
        </template>
        <v-list style="font-size: smaller; color: gray" dense>
          <v-list-item @click="loadExample(0)">
            <v-icon left size="1em">fas fa-plus</v-icon>
            Alzheimer Disease {{ ["Genes", "Proteins"][this.seedTypeId] }}
          </v-list-item>
          <v-list-item @click="loadExample(1)">
            <v-icon left size="1em">fas fa-plus</v-icon>
            Breast Cancer Resistance Protein Inhibitors: Target {{ ["Genes", "Proteins"][this.seedTypeId] }}
          </v-list-item>
          <v-list-item @click="loadExample(2)">
            <v-icon left size="1em">fas fa-plus</v-icon>
            Some Cancer {{ ["Genes", "Proteins"][this.seedTypeId] }}: TP53, BRCA1, BRCA2
          </v-list-item>
        </v-list>
      </v-menu>
    </v-col>
  </v-row>
</template>

<script>
export default {
  name: "ExampleSeeds",
  props:{
    seedTypeId: Number,
  },


  methods:{
    loadExample: async function(nr){
      let example = await this.$http.getQuickExample(nr, ["gene","protein"][this.seedTypeId])
      let origin = "Example "+(nr+1)+": ("
      switch (nr){
        case 0: origin+="Alzheimer)"; break;
        case 1: origin+="Breast Cancer Resistance Protein Inhibitors"; break;
        case 2: origin+="Cancer Genes"; break;
      }
      origin+=")"
      this.$emit("addSeedsEvent",{data:example, origin:origin})
    },


  }
}
</script>

<style scoped>

</style>
