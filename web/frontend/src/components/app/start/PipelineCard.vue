<template>
  <v-card @mouseenter="cardHover=true" @mouseleave="cardHover=false" width="25vw"
          :style="{margin: '15px', elevation:'4', minWidth:minWidth, minHeight:minHeight}">
    <div :class="cardHover ? 'content':''">
      <v-card-title
        style="display: flex; justify-content: center; margin-right: auto; font-size: x-large">{{ title }}
      </v-card-title>
      <v-card-subtitle :style="{color:cardHover?'white':''}">{{ subtitle }}</v-card-subtitle>
    </div>
    <v-progress-linear v-if="loading" indeterminate></v-progress-linear>
    <template v-show="!loading">
      <v-img ref='img' :class="cardHover ? 'blur': ''"
             :src="image" width="25vw" contain :style="{minWidth:minWidth}">
        <div v-show="!cardHover"
             style="height: 100%; width: 100%; display: flex; align-content: center; justify-content: center">
        </div>
      </v-img>
      <div v-if="imgHeight!=='0' && !loading" class="content" v-show="cardHover"
           :style="{marginTop: 'calc(-1*'+imgHeight+')', minHeight: imgHeight,maxHeight: imgHeight , fontSize: 'medium',height: imgHeight}">
        <div style="padding: 15px; height: 70%; width:100%; display: flex">
          <div style="align-content: center; margin:auto">
            <slot name="description">
            </slot>
          </div>
        </div>
        <div
          style="height: 30%;width: 100%; display: flex;">
          <div style="align-content: flex-end; margin: auto auto 16px auto; justify-content: center">
            <slot name="actions">
            </slot>
          </div>
        </div>
      </div>
    </template>
  </v-card>
</template>

<script>
export default {
  name: "PipelineCard",

  props: {
    image: String,
    title: String,
    subtitle: String,
  },

  data() {
    return {
      cardHover: false,
      loading: true,
      imgHeight: "0",
      minHeight: "316px",
      minWidth: "360px",
    }
  },

  created() {
    this.getImgHeight()
  },

  methods: {
    getImgHeight:function () {
      if (this.$refs.img != null && this.$refs.img.$el.clientHeight > 0 && this.$refs.img.$el.clientWidth > 0) {
        this.loading = false
        this.imgHeight = "calc(max(25vw," + this.minWidth + ")/" + (this.$refs.img.$el.clientWidth / this.$refs.img.$el.clientHeight) + ")"
      } else
        setTimeout(this.getImgHeight, 1000)
    }

  },
}
</script>

<style scoped>
.blur {
  -webkit-filter: blur(10px);
  -moz-filter: blur(10px);
  -o-filter: blur(10px);
  -ms-filter: blur(10px);
  filter: url(#blur);
  filter: blur(10px);
  filter: progid:DXImageTransform.Microsoft.Blur(PixelRadius='3');
  -webkit-transition: 200ms -webkit-filter linear;
  -o-transition: 200ms -o-filter linear;

}

.content {
  background-color: rgba(56, 56, 56, .6);
  color: white;
  width: max(25vw, 360px);
  position: relative;
  z-index: 100;
}
</style>
