<template>
  <v-container>
    <v-card class="mx-auto">
      <v-list>
        <v-list-item>
          <v-list-item-title class="title">
            Disease Modules
          </v-list-item-title>
        </v-list-item>
        <v-list-item>
          <v-list-item-subtitle>
            Selection of seed Genes/Proteins (manual or BiCon) for algorithmic disease module identification
          </v-list-item-subtitle>
        </v-list-item>
        <v-list-item>
          <v-list-item-title class="title">
            Drug Repurposing
          </v-list-item-title>
        </v-list-item>
        <v-list-item>
          <v-list-item-subtitle>
            Starting e.g. with a disorder to derive Genes/Proteins for module identification and subsequent drug
            ranking (e.g. trustrank)
          </v-list-item-subtitle>
        </v-list-item>
      </v-list>
    </v-card>
  </v-container>
</template>

<script>
export default {
  name: "Quick"
}
</script>

<style scoped>

</style>
