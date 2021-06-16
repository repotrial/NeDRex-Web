<template>
  <v-card @mouseenter="cardHover=true" @mouseleave="cardHover=false" width="25vw"
          style="margin: 15px">
    <v-card-title :class="cardHover ? 'content':''" style="display: flex; justify-content: center; margin-right: auto; font-size: x-large">{{ title }}</v-card-title>
    <v-img ref='img' :class="cardHover ? 'blur': ''"
           :src="image" width="25vw" contain
    >
      <div v-show="!cardHover"
           style="height: 100%; width: 25vw; display: flex; align-content: center; justify-content: center">
        <v-icon right size="10em" color="rgba(0,0,0,.05)">fas fa-cogs</v-icon>
      </div>
    </v-img>
    <div class="content" v-show="cardHover"
         :style="{marginTop: '-'+imgHeight, minHeight: imgHeight,maxHeight: imgHeight , fontSize: 'medium',height: imgHeight}">
      <div style="padding: 15px; height: 70%; width:100%; display: flex">
        <div style="align-content: center; margin:auto">
        <slot name="description">
        </slot>
        </div>
      </div>
      <div
        style="height: 30%;width: 100%; display: flex;">
        <div style="align-content: flex-end; margin: auto auto 7px auto; justify-content: center">
          <slot name="actions">
          </slot>
        </div>
      </div>
    </div>


  </v-card>
</template>

<script>
export default {
  name: "PipelineCard",

  props: {
    image: String,
    title: String,
  },

  data() {
    return {
      cardHover: false,
      imgHeight: "0px",
    }
  },

  created() {
    this.getImgHeight()
  },

  methods: {

    getImgHeight: async function () {
      if (this.$refs.img != null) {
        this.imgHeight = this.$refs.img.$el.clientHeight + "px"
      } else
        setTimeout(this.getImgHeight, 500)
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
  width: 25vw;
  position: relative;
  z-index: 100;
}

/*.blur {*/
/*  -webkit-filter: blur(0px);*/
/*  -moz-filter: blur(0px);*/
/*  -o-filter: blur(0px);*/
/*  -ms-filter: blur(0px);*/
/*  filter: progid:DXImageTransform.Microsoft.Blur(PixelRadius='0');*/
/*  filter: blur(0px);*/
/*}*/
</style>
