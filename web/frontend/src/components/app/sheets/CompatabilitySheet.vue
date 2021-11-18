<template>
  <v-sheet dark :color="color">
    <v-card>
      <v-card-title>Browser Compatability</v-card-title>
      <v-card-text>
        <v-simple-table>
          <template v-slot:default>
            <thead>
            <tr>
              <th class="text-left">
                OS
              </th>
              <th class="text-left">
                Version
              </th>
              <th class="text-left">
              Chrome
              </th>
              <th class="text-left">
                Firefox
              </th>
              <th class="text-left">
                Safari
              </th>
              <th class="text-left">
                Edge
              </th>
            </tr>
            </thead>
            <tbody>
            <tr
              v-for="item in table"
              :key="item.os"
            >
              <td class="text-left">{{ item.os }}</td>
              <td class="text-left">{{ item.version }}</td>
              <td class="text-left"> {{ item.chrome }}</td>
              <td class="text-left">{{ item.firefox }}</td>
              <td class="text-left">{{ item.safari }}</td>
              <td class="text-left">{{ item.edge }}</td>
            </tr>
            </tbody>
          </template>
        </v-simple-table>
      </v-card-text>
      <v-card-subtitle>Not limited to but explicitly tested for the following browser and OS versions</v-card-subtitle>
    </v-card>
  </v-sheet>
</template>

<script>

export default {
  name: "BugSheet",

  props: {
    color: String,
  },

  data(){
    return{
      table:[
        {os:"Linux", version:"Mint 20.2 Cinnamon", chrome:"95.0.4638.54", firefox:"93.0", safari:"n/a", edge:"95.0.1020.30-1"},
        {os:"MacOS", version:"-", chrome:"TODO", firefox:"TODO", safari:"TODO", edge:"TODO"},
        {os:"Windows", version:"10", chrome:"95.0.4638.54", firefox:"93.0", safari:"n/a", edge:"95.0.1020.30"}
      ]
    }
  },

  methods: {
  }
}
</script>

<style scoped>

</style>
