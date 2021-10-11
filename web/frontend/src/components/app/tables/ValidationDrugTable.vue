<template>
<SeedTable ref="drugTable" :download="true"
           :remove="true"
           @remove="$emit('drugCountUpdate',updateCount())"
           :filter="true"
           @printNotificationEvent="printNotification"
           height="40vh"
           :title="'Selected Validation Drugs ('+validationDrugCount+')'"
           nodeName="drug"></SeedTable>
</template>

<script>
import SeedTable from "@/components/app/tables/SeedTable";
export default {
  name: "ValidationDrugTable",
  components: {SeedTable},
  data() {
    return {
      validationDrugCount :0
    }
  },

  methods: {
    addDrugs: function (drugs) {
      this.$refs.drugTable.addSeeds(drugs)
      this.updateCount()
    },

    updateCount: function(){
      this.validationDrugCount=this.getDrugs().length
      this.$emit("drugCountUpdate")
    },

    getDrugs: function (){
      return this.$refs.drugTable.getSeeds();
    },

    printNotification: function(arg1, arg2){
      this.$emit("printNotificationEvent",arg1,arg2)
    }
  }


}
</script>

<style scoped>

</style>
