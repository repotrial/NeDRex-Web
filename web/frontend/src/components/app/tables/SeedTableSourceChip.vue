<template>
  <v-tooltip bottom v-if="node.length>0">
    <template v-slot:activator="{attrs,on}">
      <v-chip style="font-size: smaller; color: gray;" pill v-on="on" v-bind="attrs">{{ getNiceDBName(db) }}</v-chip>
    </template>
    <span>The association between this <b>{{ nodeName }}</b> and <br> at least one of the selected <b>{{ node }}s</b> was extracted from <b><i>{{ getFullDBName(db) }}</i></b></span>
  </v-tooltip>
  <v-tooltip bottom v-else>
    <template v-slot:activator="{attrs,on}">
      <v-chip style="font-size: smaller; color: gray;" pill v-on="on" v-bind="attrs">{{ getNiceDBName(db) }}</v-chip>
    </template>
    <span>The association between this <b>{{ nodeName }}</b> and <br> at least one of the selected connected nodes was extracted from<b><i>{{ getFullDBName(db) }}</i></b></span>
  </v-tooltip>
</template>

<script>
export default {
  name: "SeedTableSourceChip",

  props: {
    source: String,
    nodeName: String,
  },

  data() {
    return {
      db: "",
      node: ""
    }
  },

  created() {
    if (this.source.indexOf(":") > -1) {
      let sp = this.source.split(":")
      this.node = sp[0]
      this.db = sp[1]
    } else {
      this.db = this.source;
    }
  },
  methods: {
    getNiceDBName: function (db) {
      switch (db) {
        case "omim":
          return "OMIM";
        case "disgenet":
          return "DisGeNET"
      }
      return db;
    },
    getFullDBName: function (db) {
      switch (db) {
        case "omim":
          return "OMIM (Online Mendelian Inheritance in Man)";
        case "disgenet":
          return "DisGeNET";
      }
      return db;
    }
  }
}
</script>

<style scoped>

</style>
