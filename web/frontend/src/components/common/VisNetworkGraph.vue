<template>
  <div ref="containerEl"></div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, shallowRef, watch } from 'vue'
import { Network } from 'vis-network'

const props = defineProps({
  nodes: {type: [Array, Object], default: () => []},
  edges: {type: [Array, Object], default: () => []},
  options: {type: Object, default: () => ({})},
  layout: {type: Object, default: undefined},
  physics: {type: [Object, Boolean], default: undefined},
  events: {type: Array, default: () => []},
})

const emit = defineEmits()

const containerEl = shallowRef(null)
const networkInstance = shallowRef(null)

function translateEvent(event) {
  return event.replace(/([a-z0-9])([A-Z])/g, "$1-$2").toLowerCase()
}

onMounted(() => {
  networkInstance.value = new Network(containerEl.value, {nodes: props.nodes, edges: props.edges}, props.options)
  props.events.forEach(eventName => {
    networkInstance.value.on(eventName, params => emit(translateEvent(eventName), params))
  })
})

onBeforeUnmount(() => {
  if (networkInstance.value)
    networkInstance.value.destroy()
})

watch(() => props.nodes, (nodes) => {
  if (networkInstance.value)
    networkInstance.value.setData({nodes, edges: props.edges})
})

watch(() => props.edges, (edges) => {
  if (networkInstance.value)
    networkInstance.value.setData({nodes: props.nodes, edges})
})

function setOptions(options) {
  networkInstance.value.setOptions(options)
}

function getNode(id) {
  return props.nodes.get(id)
}

function getEdge(id) {
  return props.edges.get(id)
}

function getPositions(nodeIds) {
  return networkInstance.value.getPositions(nodeIds)
}

function selectNodes(nodeIds, highlightEdges) {
  networkInstance.value.selectNodes(nodeIds, highlightEdges)
}

function unselectAll() {
  networkInstance.value.unselectAll()
}

function moveTo(options) {
  networkInstance.value.moveTo(options)
}

function focus(nodeId, options) {
  networkInstance.value.focus(nodeId, options)
}

function fit(options) {
  networkInstance.value.fit(options)
}

function getConnectedNodes(nodeId, direction) {
  return networkInstance.value.getConnectedNodes(nodeId, direction)
}

defineExpose({
  setOptions,
  getNode,
  getEdge,
  getPositions,
  selectNodes,
  unselectAll,
  moveTo,
  focus,
  fit,
  getConnectedNodes,
  network: networkInstance,
})
</script>

<style scoped>
</style>
