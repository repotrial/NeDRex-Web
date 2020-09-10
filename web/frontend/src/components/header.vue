<template>
  <v-app-bar app prominent color="white">
    <div id="particles-js"></div>
    <div id="title-div" style="position:fixed">
      <h1 id="title" >RepoScape-Browser</h1>
    </div>
  </v-app-bar>
</template>

<script>
import '../dist/js/particles.js'

export default {
  name: "headerBar",
  mounted() {
    this.initParticles()
  },
  methods: {
    initParticles() {
      window.particlesJS("particles-js", {
        "particles": {
          "number": {
            "value": 500,
            "density": {
              "enable": true,
              "value_area": 700
            }
          },
          "color": {
            "value": "#000000"
          },
          "shape": {
            "type": "circle",
            "stroke": {
              "width": 0,
              "color": "#000000"
            },
            "polygon": {
              "nb_sides": 5
            },
          },
          "opacity": {
            "value": 0.5,
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
              "speed": 40,
              "size_min": 0.3,
              "sync": false
            }
          },
          "line_linked": {
            "enable": true,
            "distance": 250,
            "color": "#000e30",
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
              "enable": true,
              "mode": "grab"
            },
            "onclick": {
              "enable": true,
              "mode": "push"
            },
            "resize": true
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
        "retina_detect": true
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
  background: white
  height: 95%
  width: 97.5%

#title
  color: white
  text-align: center
  margin: auto
  vertical-align: middle
  text-shadow: 2px 2px 5px #45495b
  font-size: 80px
  user-select: none


#title-div
  display: flex
  justify-content: center
  align-items: center
  width: 95%
  height: 97.5%

</style>
