<template>
  <v-tooltip bottom v-if="node.length>0">
    <template v-slot:activator="{attrs,on}">
      <v-chip style="font-size: smaller; color: gray;" pill v-on="on" v-bind="attrs">{{ getNiceDBName(db) }}</v-chip>
    </template>
    <span v-if="!directHit(db)">The association between this <b>{{ nodeName }}</b> and <br> at least one of the selected <b>{{ node }}s</b> was extracted from <b><i>{{ getFullDBName(db) }}</i></b></span>
    <span v-else><b>{{ nodeName }}</b> was directly added by searching for <b>{{node}}s</b>. The main information is extracted from <b><i>{{ getFullDBName(db) }}</i></b></span>
  </v-tooltip>
  <v-tooltip bottom v-else>
    <template v-slot:activator="{attrs,on}">
      <v-chip style="font-size: smaller; color: gray;" pill v-on="on" v-bind="attrs">{{ getNiceDBName(db) }}</v-chip>
    </template>
    <span v-if="!directHit(db)">The association between this <b>{{ nodeName }}</b> and <br> at least one of the selected connected nodes was extracted from <b><i>{{ getFullDBName(db) }}</i></b></span>
    <span v-else>This <b>{{ nodeName }}</b> was directly added by searching for names or properties of <b>{{nodeName}}s</b>. The main information is extracted from <b><i>{{ getFullDBName(db) }}</i></b></span>
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
        case "drugbank":
          return "DrugBank"
      }
      return db;
    },
    directHit: function(db){
      switch (db){
        case "drugbank": return true;
        default : return false;
      }
    },

    getFullDBName: function (db) {
      switch (db) {
        case "omim":
          return "OMIM (Online Mendelian Inheritance in Man)";
        case "disgenet":
          return "DisGeNET";
        case "drugbank":
          return "DrugBank"
      }
      return db;
    }
  }
}
</script>

<style scoped>

</style>
