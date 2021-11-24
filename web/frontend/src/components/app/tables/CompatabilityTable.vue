<template>
  <v-simple-table :style="{backgroundColor: bgColor}">
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
</template>

<script>
export default {
  name: "CompatabilityTable",
  props:{
    bgColor: String,
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
}
</script>

<style scoped>

</style>
