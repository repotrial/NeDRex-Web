<template>
  <v-container :style="{maxWidth: modus===0? '50vw':'95vw'}">
    <template v-if="modus===0">
      <v-card>
        <v-card-title style="display: flex; justify-content: center; font-size: x-large; margin-bottom: 15px;">Guided Connectivity Search
        </v-card-title>
        <v-card-text style="display: flex;">
          <div style="text-align: justify; margin: 15px">
            This <b>Guided Connectivity Search</b> provides an easy way to explore some initial input set of any supported type.
            <b>Direct</b> or <b>indirect (transitive) associations</b> to any data type we have can be constructed with
            or without
            limiting the target entries. Out of a proposed list of <b>available direct and indirect paths</b> connecting
            the source and
            target entries the current path of interest can be selected. This path can be kept unchanged or already <b>condensed</b>
            into a <b>new connection-type</b> containing this inferred/transitive association.<br>
            The resulting network is then <b>visualized</b> with all source and target nodes and the selected
            connections which
            also can be <b>exported</b> in the form of <b>edge-lists</b> on request.
          </div>
          <div>
            <v-img style="justify-self: flex-end; margin-left: auto; cursor: pointer ; margin-right: 5px;"
                   :gradient="imgHover?'to top right, rgba(69,69,69,.3),rgba(69,69,69,.3)':''"
                   :src="$config.STATIC_PATH+'/assets/guided_visual_description.png'" max-width="20vw" width="20vw"
                   @mouseenter="imgHover=true" @mouseleave="imgHover=false"
                   @click="imagePopup"></v-img>
            <v-card-subtitle style="padding:1px"><i>click to view</i></v-card-subtitle>
          </div>

        </v-card-text>
        <v-btn @click="modus=1; $emit('clearURLEvent')" color="primary" style="margin-bottom: 15px">Start
          <v-icon right>fas fa-angle-double-right</v-icon>
        </v-btn>
      </v-card>
    </template>
    <GuidedStepper ref="guidedSteps" v-if="modus===1"
                   @resetEvent="reset()"
                   @printNotificationEvent="printNotification"
                   @graphLoadEvent="graphLoadEvent"
                   @graphLoadNewTabEvent="graphLoadNewTabEvent"
                   @newGraphEvent="$emit('newGraphEvent')"
    ></GuidedStepper>
    <v-dialog v-model="bigImage"
              style="z-index: 1001; max-height: 70vh" max-width="700px">
      <v-sheet style="padding:10px">
        <div style="display: flex; justify-content: flex-end; margin-left: auto; ">
          <v-tooltip left>
            <template v-slot:activator="{on, attrs}">
              <v-btn icon style="padding:1em" color="red darker" @click="bigImage=false" v-on="on" v-bind="attrs">
                <v-icon size="2em">far fa-times-circle</v-icon>
              </v-btn>
            </template>
            <div>Close image</div>
          </v-tooltip>
        </div>
        <v-img :src="$config.STATIC_PATH+'/assets/guided_visual_description.png'" width="700px"></v-img>
        <div style="text-align: justify-all">The <b>Guided Exploration</b> can be used to research the direct or
          indirect association between a known selection of starting elements and either also defined target set or
          other than type-wise unspecific target definition. Paths to construct these indirect associations can be
          selected and either shown as they are or a new transitive edge-type based on these paths may be created and
          ultimately visualized.
        </div>
      </v-sheet>
    </v-dialog>
  </v-container>


</template>

<script>
import GuidedStepper from "@/components/views/start/guided/GuidedStepper";

export default {
  name: "Guided",
  components: {
    GuidedStepper,
  },
  data() {
    return {
      modus: 0,
      bigImage: false,
      imgHover: false,
    }
  },

  methods: {
    reset: function () {
      this.modus = 0;
      if (this.$refs.guidedSteps)
        this.$refs.guidedSteps.init()
    },
    printNotification: function (message, type) {
      this.$emit('printNotificationEvent', message, type)
    },
    graphLoadEvent: function (data) {
      this.$emit('graphLoadEvent', data)
    },

    graphLoadNewTabEvent: function (data) {
      this.$emit('graphLoadNewTabEvent', data)
    },
    imagePopup: function () {
      this.bigImage = true;
    }
  }

}
</script>

<style scoped>

</style>
