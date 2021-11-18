<template>
  <v-app-bar app elevation="0" :height="prominent? 100:50" color="#383838">
    <div v-show="prominent" id="particles-js"></div>
    <div id="title-div" style="position:fixed">
      <img :src="getConfig().STATIC_PATH+'nedrex-web-logo-transparent.png'" :width="prominent?350:250"
           :style="{'padding-top':'25px', cursor: 'pointer'}" @click="$emit('redirectEvent','/home',true)">
    </div>
    <v-row>
      <v-col cols="11"></v-col>
      <v-col cols="1" style="padding-top: 0">
        <v-row>
          <v-col style=" padding-right:0; padding-bottom: 0">
            <v-menu bottom transition="slide-y-transition">
              <template v-slot:activator="{on,attrs}">
                <v-btn icon small right style="background-color: #383838" v-bind="attrs" v-on="on">
                  <v-icon color="primary">
                    fas fa-bars
                  </v-icon>
                </v-btn>
              </template>
              <v-list color="#383838">
                <v-list-item>
                  <v-tooltip left>
                    <template v-slot:activator="{on, attrs}">
                      <v-btn icon small @click="$emit('showVersionEvent')" style="background-color: #383838" v-on="on"
                             v-bind="attrs">
                        <v-icon color="primary">
                          fas fa-info-circle
                        </v-icon>
                      </v-btn>
                    </template>
                    Source Information
                  </v-tooltip>
                </v-list-item>
                <v-list-item>
                  <v-tooltip left>
                    <template v-slot:activator="{on, attrs}">
                      <v-btn small icon @click="$emit('showBugEvent')" style="background-color:  #383838" v-on="on"
                             v-bind="attrs">
                        <v-icon color="primary">
                          fas fa-bug
                        </v-icon>
                      </v-btn>
                    </template>
                    Report Bug
                  </v-tooltip>
                </v-list-item>
                <v-list-item>
                  <v-tooltip left>
                    <template v-slot:activator="{on, attrs}">
                      <v-btn small icon @click="$emit('showHelpEvent')" style="background-color:  #383838" v-on="on" v-bind="attrs">
                        <v-icon color="primary">
                          fas fa-question-circle
                        </v-icon>
                      </v-btn>
                    </template>
                    Contact & Help
                  </v-tooltip>
                </v-list-item>
                <v-list-item>
                  <v-tooltip left>
                    <template v-slot:activator="{on, attrs}">
                      <v-btn small icon @click="$emit('showCompatability')" style="background-color:  #383838" v-on="on" v-bind="attrs">
                        <v-icon color="primary">
                          fas fa-laptop-code
                        </v-icon>
                      </v-btn>
                    </template>
                    Browser Compatability
                  </v-tooltip>
                </v-list-item>
              </v-list>
            </v-menu>
          </v-col>
        </v-row>
      </v-col>
    </v-row>
  </v-app-bar>
</template>

<script>
import '../../js/particles.js'
import * as CONFIG from "../../Config"

export default {
  name: "headerBar",
  props: {
    prominent: true,
  },
  mounted() {
    this.initParticles()
  },
  methods: {
    getConfig: function () {
      return CONFIG
    },
    initParticles() {
      window.particlesJS("particles-js", {
        "particles": {
          "number": {
            "value": 500,
            "density": {
              "enable": true,
              "value_area": 1000
            }
          },
          "color": {
            "value": "#35d0d4"
          },
          "shape": {
            "type": "circle",
            "stroke": {
              "width": 0,
              "color": "#c8c8c8"
            },
            "polygon": {
              "nb_sides": 4
            },
          },
          "opacity": {
            "value": 1,
            "random": true,
            "anim": {
              "enable": false,
              "speed": 1,
              "opacity_min": 0.7,
              "sync": false
            }
          },
          "size": {
            "value": 3,
            "random": true,
            "anim": {
              "enable": false,
              "speed": 30,
              "size_min": 0.3,
              "sync": false
            }
          },
          "line_linked": {
            "enable": true,
            "distance": 200,
            "color": "#66c2c5",
            "opacity": 0.4,
            "width": 1
          },
          "move": {
            "enable": true,
            "speed": 1,
            "direction": "none",
            "random": false,
            "straight": false,
            "out_mode": "out",
            "bounce": false,
            "attract": {
              "enable": false,
              "rotateX": 600,
              "rotateY": 1200
            }
          }
        },
        "interactivity": {
          "detect_on": "canvas",
          "events": {
            "onhover": {
              "enable": false,
              "mode": "grab"
            },
            "onclick": {
              "enable": false,
              "mode": "push"
            },
            "resize": false
          },
          "modes": {
            "grab": {
              "distance": 140,
              "line_linked": {
                "opacity": 1
              }
            },
            "bubble": {
              "distance": 400,
              "size": 40,
              "duration": 2,
              "opacity": 8,
              "speed": 3
            },
            "repulse": {
              "distance": 200,
              "duration": 0.4
            },
            "push": {
              "particles_nb": 4
            },
            "remove": {
              "particles_nb": 2
            }
          }
        },
        "retina_detect": false
      })
    }
  }
}
</script>

<style scoped lang="sass">

#particles-js
  position: fixed
  //filter: blur(1px)
  //-webkit-filter: blur(1px)
  background: #383838
  height: 95%
  width: 97.5%

//#title
//  color: white
//  text-align: center
//  margin: auto
//  vertical-align: middle
//  text-shadow: 2px 2px 5px #45495b
//  user-select: none


#title-div
  display: flex
  justify-content: center
  align-items: center
  width: 95%
  height: 97.5%

</style>
