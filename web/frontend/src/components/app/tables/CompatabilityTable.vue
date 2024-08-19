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
        {os:"Linux", version:"Mint 22 Cinnamon", chrome:"127.0.6533.119", firefox:"129.0", safari:"n/a", edge:"n/a"},
        {os:"MacOS", version:"Big Sur 11.6.1", chrome:"127.0.6533.119", firefox:"123.0.1", safari:"17.5", edge:"n/a"},
        {os:"Windows", version:"10/11", chrome:"127.0.6533.200", firefox:"192.0.1", safari:"n/a", edge:"127.0.2651.98"}
      ]
    }
  },
}
</script>

<style scoped>

</style>
