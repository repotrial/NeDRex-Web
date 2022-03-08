<template>
  <v-dialog v-model="show"
            :max-width=maxWidth
            style="z-index: 1001; max-height: 80vh;"
  >
    <v-card>
      <v-btn @click="show=false" color="error" icon style="position: absolute; right: 0; left: auto"><v-icon>far fa-times-circle</v-icon></v-btn>
      <div style="width: 100%; display: flex; justify-content: center; padding: 8px;">
        <EntryDetails style="margin-top: 24px; max-height: 80vh; overflow-y: auto" :gid="gid" :entity-graph="entityGraph"
                      :detail-request="detailRequest" :max-width="maxWidth" :attributes="attributes"
                      :additions="additions">

        </EntryDetails>
      </div>
    </v-card>
  </v-dialog>
</template>

<script>
import EntryDetails from "@/components/app/EntryDetails";

export default {
  name: "DetailDialog",
  components: {EntryDetails},
  props: {
    gid: String,
    entityGraph: Object,
    detailRequest: Object,
    maxWidth: String,
    attributes: Array,
    additions: Array,
  },

  data() {
    return {
      show: false,
    }
  },

  methods: {
    showDialog: function () {
      this.show = true
    }
  }
}
</script>

<style scoped>

</style>
